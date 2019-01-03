/**
 * FileName: AbstractBatchDecoder
 * Author:   HuangTaiHong
 * Date:     2019/1/2 14:41
 * Description: 抽象消息解码器-支持批处理
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.codec;

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
 * 〈抽象消息解码器-支持批处理〉
 * <pre>
 *   if (msg instanceof List) {
 *       processorManager.getDefaultExecutor().execute(new Runnable() {
 *           @Override
 *           public void run() {
 *               // batch submit to an executor
 *               for (Object m : (List<?>) msg) {
 *                   RpcCommandHandler.this.process(ctx, m);
 *               }
 *           }
 *       });
 *   } else {
 *       process(ctx, msg);
 *   }
 * </pre>
 *
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
    /**
     * 〈一句话功能简述〉
     * 〈消息累加器〉
     *
     * @author HuangTaiHong
     * @create 2019.01.02
     * @since 1.0.0
     */
    public interface Cumulator {
        /**
         * 功能描述: <br>
         * 〈累加消息〉
         *
         * @param alloc
         * @param cumulation
         * @param in
         * @return > io.netty.buffer.ByteBuf
         * @author HuangTaiHong
         * @date 2019.01.02 14:44:46
         */
        ByteBuf cumulate(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf in);
    }

    /** 定义消息累加器 每次都将读取到的数据通过内存拷贝的方式拼接到一个大的字节容器中 **/
    public static final Cumulator MERGE_CUMULATOR = new Cumulator() {
        public ByteBuf cumulate(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf in) {
            ByteBuf buffer;
            if (cumulation.writerIndex() > cumulation.maxCapacity() - in.readableBytes() || cumulation.refCnt() > 1) {
                buffer = expandCumulation(alloc, cumulation, in.readableBytes());
            } else {
                buffer = cumulation;
            }
            buffer.writeBytes(in);
            in.release();
            return buffer;
        }
    };

    /**
     * 功能描述: <br>
     * 〈累加器扩容操作〉
     *
     * @param alloc
     * @param cumulation
     * @param readable
     * @return > io.netty.buffer.ByteBuf
     * @author HuangTaiHong
     * @date 2019.01.02 14:47:19
     */
    static ByteBuf expandCumulation(ByteBufAllocator alloc, ByteBuf cumulation, int readable) {
        ByteBuf oldCumulation = cumulation;
        cumulation = alloc.buffer(oldCumulation.readableBytes() + readable);
        cumulation.writeBytes(oldCumulation);
        oldCumulation.release();
        return cumulation;
    }

    ByteBuf cumulation;
    private Cumulator cumulator = MERGE_CUMULATOR;
    private boolean singleDecode;
    private boolean decodeWasNull;
    private boolean first;
    private int discardAfterReads = 16;
    private int numReads;

    public void setSingleDecode(boolean singleDecode) {
        this.singleDecode = singleDecode;
    }

    public boolean isSingleDecode() {
        return singleDecode;
    }

    public void setCumulator(Cumulator cumulator) {
        if (cumulator == null) {
            throw new NullPointerException("cumulator");
        }
        this.cumulator = cumulator;
    }

    public void setDiscardAfterReads(int discardAfterReads) {
        if (discardAfterReads <= 0) {
            throw new IllegalArgumentException("discardAfterReads must be > 0");
        }
        this.discardAfterReads = discardAfterReads;
    }

    protected int actualReadableBytes() {
        return internalBuffer().readableBytes();
    }

    protected ByteBuf internalBuffer() {
        if (cumulation != null) {
            return cumulation;
        } else {
            return Unpooled.EMPTY_BUFFER;
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        ByteBuf buf = internalBuffer();
        int readable = buf.readableBytes();
        if (readable > 0) {
            ByteBuf bytes = buf.readBytes(readable);
            buf.release();
            ctx.fireChannelRead(bytes);
        } else {
            buf.release();
        }
        cumulation = null;
        numReads = 0;
        ctx.fireChannelReadComplete();
        handlerRemovedProcess(ctx);
    }

    protected void handlerRemovedProcess(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            RecyclableArrayList out = RecyclableArrayList.newInstance();
            try {
                // 累加数据
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
                    ArrayList<Object> ret = new ArrayList<Object>(size);
                    for (int i = 0; i < size; i++) {
                        ret.add(out.get(i));
                    }
                    ctx.fireChannelRead(ret);
                }
                out.recycle();
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        numReads = 0;
        discardSomeReadBytes();
        if (decodeWasNull) {
            decodeWasNull = false;
            if (!ctx.channel().config().isAutoRead()) {
                ctx.read();
            }
        }
        ctx.fireChannelReadComplete();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
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
                    ArrayList<Object> ret = new ArrayList<Object>(size);
                    for (int i = 0; i < size; i++) {
                        ret.add(out.get(i));
                    }
                    ctx.fireChannelRead(ret);
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
     * 〈尝试将字节容器的数据拆分成业务数据包塞到业务数据容器out中〉
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

                // 是否只解码一次
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
     * 〈清理字节容器〉
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
     * 〈通道转为非激活状态最后一次解码〉
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
}