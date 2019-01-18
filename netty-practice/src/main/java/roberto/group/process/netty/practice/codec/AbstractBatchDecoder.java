/**
 * FileName: AbstractBatchDecoder
 * Author:   HuangTaiHong
 * Date:     2019/1/2 14:41
 * Description: Abstract Message Decoder - Support Batch Processing.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.codec;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderException;
import io.netty.util.internal.RecyclableArrayList;
import io.netty.util.internal.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈Abstract Message Decoder - Support Batch Processing.〉
 *
 * 1.如果当前读取的数据不足以拼接成一个完整的业务数据包，那就保留该数据继续从TCP缓冲区中读取，直到得到一个完整的数据包
 *
 * 2.如果当前读到的数据加上已经读取的数据足够拼接成一个数据包，那就将已经读取的数据拼接上本次读取的数据够成一个完整的业务数据包传递到业务逻辑。多余的数据仍然保留，以便和下次读到的数据尝试拼接
 *
 * @author HuangTaiHong
 * @create 2019/1/2
 * @since 1.0.0
 */
public abstract class AbstractBatchDecoder extends ChannelInboundHandlerAdapter {
    /** 定义消息累加器 每次都将读取到的数据通过内存拷贝的方式拼接到一个大的字节容器中 **/
    public static final Cumulator MERGE_CUMULATOR = new Cumulator() {
        @Override
        ByteBuf cumulate(ByteBufAllocator allocator, ByteBuf cumulation, ByteBuf in) {
            try {
                // if the accumulator needs to be expanded
                boolean needExpand = cumulation.writerIndex() > cumulation.maxCapacity() - in.readableBytes() || cumulation.refCnt() > 1;

                return (needExpand ? expandCumulation(allocator, cumulation, in.readableBytes()) : cumulation).writeBytes(in);
            } finally {
                in.release();
            }
        }
    };

    private boolean first;
    private ByteBuf cumulation;
    private boolean singleDecode;
    private boolean decodeWasNull;
    private Cumulator cumulator = MERGE_CUMULATOR;

    /** Bytebuf cleanup properties **/
    private int numReads;
    private int discardAfterReads = 16;

    protected ByteBuf internalBuffer() {
        if (cumulation != null) {
            return cumulation;
        } else {
            return Unpooled.EMPTY_BUFFER;
        }
    }

    public boolean isSingleDecode() {
        return singleDecode;
    }

    public void setSingleDecode(boolean singleDecode) {
        this.singleDecode = singleDecode;
    }

    public void setCumulator(Cumulator cumulator) {
        if (cumulator == null) {
            throw new NullPointerException("cumulator must no be null.");
        }
        this.cumulator = cumulator;
    }

    public void setDiscardAfterReads(int discardAfterReads) {
        if (discardAfterReads <= 0) {
            throw new IllegalArgumentException("discardAfterReads must be > 0");
        }
        this.discardAfterReads = discardAfterReads;
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        ByteBuf byteBuf = internalBuffer();
        int readable = byteBuf.readableBytes();
        if (readable < 0) {
            // fireChannelRead if there has data in the current buffer
            ctx.fireChannelRead(byteBuf.readBytes(readable));
        }
        // clean up resources
        numReads = 0;
        byteBuf.release();
        cumulation = null;
        ctx.fireChannelReadComplete();
        // call callback function - provide extension
        handlerRemovedProcess(ctx);
    }

    /**
     * 功能描述: <br>
     * 〈Gets called after the AbstractBatchDecoder was removed from the actual context and it doesn't handle events anymore.〉
     *
     * @param ctx
     * @author HuangTaiHong
     * @date 2019.01.09 11:23:24
     */
    protected void handlerRemovedProcess(ChannelHandlerContext ctx) {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof ByteBuf) {
            RecyclableArrayList out = RecyclableArrayList.newInstance();
            try {
                // cumulated
                ByteBuf data = (ByteBuf) msg;
                first = cumulation == null;
                if (first) {
                    cumulation = data;
                } else {
                    cumulation = cumulator.cumulate(ctx.alloc(), cumulation, data);
                }
                // 将累加到的数据传递给业务进行业务拆包
                callDecode(ctx, cumulation, out);
            } catch (DecoderException e) {
                throw e;
            } catch (Throwable t) {
                throw new DecoderException(t);
            } finally {
                // 清理字节容器
                if (cumulation != null && !cumulation.isReadable()) {
                    numReads = 0;
                    cumulation.release();
                    cumulation = null;
                } else if (++numReads >= discardAfterReads) {
                    // 正常情况下每次读取完数据Netty都会在下面这个方法中将字节容器清理，只不过当发送端发送数据过快ChannelReadComplete可能会很久才被调用一次
                    // 所以为了防止发送端发送数据过快Netty会在每次读取到一次数据，业务拆包之后对字节字节容器做清理【当读取次数超过配置的值时开始清理】
                    numReads = 0;
                    discardSomeReadBytes();
                }

                // 传递业务数据包给业务解码器处理
                int size = out.size();
                if (size == 0) {
                    decodeWasNull = true;
                } else if (size == 1) {
                    ctx.fireChannelRead(out.get(0));
                } else {
                    ArrayList<Object> result = Lists.newArrayList(out);
                    ctx.fireChannelRead(result);
                }
                out.recycle();
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        numReads = 0;
        discardSomeReadBytes();
        if (decodeWasNull) {
            decodeWasNull = false;
            if (!ctx.channel().config().isAutoRead()) {
                // 如果一次数据读取完毕之后(可能接收端一边收，发送端一边发，这里的读取完毕指的是接收端在某个时间不再接受到数据为止)
                // 发现仍然没有拆到一个完整的用户数据包，即使该channel的设置为非自动读取也会触发一次读取操作 ctx.read()
                // 该操作会重新向selector注册op_read事件，以便于下一次能读到数据之后拼接成一个完整的数据包
                ctx.read();
            }
        }
        ctx.fireChannelReadComplete();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        RecyclableArrayList out = RecyclableArrayList.newInstance();
        try {
            if (cumulation != null) {
                callDecode(ctx, cumulation, out);
                decodeLast(ctx, cumulation, out);
            } else {
                decodeLast(ctx, Unpooled.EMPTY_BUFFER, out);
            }
        } catch (DecoderException e) {
            throw e;
        } catch (Exception e) {
            throw new DecoderException(e);
        } finally {
            try {
                if (cumulation != null) {
                    cumulation.release();
                    cumulation = null;
                }
                int size = out.size();
                if (size == 0) {
                    decodeWasNull = true;
                } else if (size == 1) {
                    ctx.fireChannelRead(out.get(0));
                    ctx.fireChannelReadComplete();
                } else {
                    ArrayList<Object> result = Lists.newArrayList(out);
                    ctx.fireChannelRead(result);
                    ctx.fireChannelReadComplete();
                }
                ctx.fireChannelInactive();
            } finally {
                out.recycle();
            }
        }
    }

    /**
     * 功能描述: <br>
     * 〈Called once data should be decoded from the given ByteBuf.〉
     *
     * @param ctx
     * @param in
     * @param out
     * @author HuangTaiHong
     * @date 2019.01.02 16:22:17
     */
    protected void callDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            while (in.isReadable()) {
                int outSize = out.size();
                int oldInputLength = in.readableBytes();
                // 解析数据并放入out集合中
                decode(ctx, in, out);
                // 如果Handler已被删除继续操作缓冲区则不安全
                if (ctx.isRemoved()) {
                    break;
                }

                if (outSize == out.size()) {
                    // 拆包器未读取任何数据
                    if (oldInputLength == in.readableBytes()) {
                        break;
                    } else {
                        // 拆包器已读取部分数据，还需要继续
                        continue;
                    }
                }

                // 如果成功解码出数据但是ByteBuf中数据长度不变可能会导致死循环
                if (oldInputLength == in.readableBytes()) {
                    throw new DecoderException(StringUtil.simpleClassName(getClass()) + ".decode() did not read anything but decoded a message.");
                }

                // 是否每次只解码一条消息
                if (isSingleDecode()) {
                    break;
                }
            }
        } catch (DecoderException e) {
            throw e;
        } catch (Throwable cause) {
            throw new DecoderException(cause);
        }
    }

    /**
     * 功能描述: <br>
     * 〈清理字节容器.〉
     *
     * 业务拆包完成之后只是从字节容器中取走了数据，但是这部分空间对于字节容器来说依然保留着
     * 而字节容器每次累加字节数据的时候都是将字节数据追加到尾部，如果不对字节容器做清理那么时间一长就会OOM
     *
     * @author HuangTaiHong
     * @date 2019.01.02 16:41:14
     */
    protected final void discardSomeReadBytes() {
        if (cumulation != null && !first && cumulation.refCnt() == 1) {
            cumulation.discardSomeReadBytes();
        }
    }

    /**
     * 功能描述: <br>
     * 〈通道转为非激活状态最后一次解码.〉
     *
     * @param ctx
     * @param in
     * @param out
     * @throws Exception
     * @author HuangTaiHong
     * @date 2019.01.02 19:02:25
     */
    protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        decode(ctx, in, out);
    }

    protected abstract void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception;

    /**
     * 〈一句话功能简述〉
     * 〈Cumulate ByteBuf.〉
     *
     * @author HuangTaiHong
     * @create 2019.01.02
     * @since 1.0.0
     */
    public static abstract class Cumulator {
        /**
         * 功能描述: <br>
         * 〈Cumulate the given ByteBuf and return the ByteBuf that holds the cumulated bytes.〉
         *
         * The implementation is responsible to correctly handle the life-cycle of the given ByteBuf and so call ByteBuf.release() if a ByteBuf is fully consumed.
         *
         * @param allocator
         * @param cumulation
         * @param in
         * @return > io.netty.buffer.ByteBuf
         * @author HuangTaiHong
         * @date 2019.01.02 14:44:46
         */
        abstract ByteBuf cumulate(ByteBufAllocator allocator, ByteBuf cumulation, ByteBuf in);

        /**
         * 功能描述: <br>
         * 〈expand cumulation.〉
         *
         * @param allocator
         * @param cumulation
         * @param readable
         * @return > io.netty.buffer.ByteBuf
         * @author HuangTaiHong
         * @date 2019.01.09 11:01:11
         */
        protected ByteBuf expandCumulation(ByteBufAllocator allocator, ByteBuf cumulation, int readable) {
            try {
                return allocator.buffer(cumulation.readableBytes() + readable).writeBytes(cumulation);
            } finally {
                cumulation.release();
            }
        }
    }
}