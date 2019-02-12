/**
 * FileName: ProtocolCodeBasedDecoder
 * Author:   HuangTaiHong
 * Date:     2019/1/2 19:07
 * Description: Protocol code based decoder.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CodecException;
import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.protocol.Protocol;
import roberto.group.process.netty.practice.protocol.ProtocolCode;
import roberto.group.process.netty.practice.protocol.ProtocolManager;

import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈Protocol code based decoder, the main decoder for a certain protocol, which is lead by one or multi bytes (magic code).〉
 *
 *  Decoder是有状态的 不能加@ChannelHandler.Sharable注解
 *
 * @author HuangTaiHong
 * @create 2019/1/2
 * @since 1.0.0
 */
public class ProtocolCodeBasedDecoder extends AbstractBatchDecoder {
    /** the length of protocol code */
    protected int protocolCodeLength;
    /** by default, suggest design a single byte for protocol version */
    public static final int DEFAULT_PROTOCOL_VERSION_LENGTH = 1;
    /** protocol version should be a positive number, we use -1 to represent illegal */
    public static final int DEFAULT_ILLEGAL_PROTOCOL_VERSION_LENGTH = -1;

    public ProtocolCodeBasedDecoder(int protocolCodeLength) {
        this.protocolCodeLength = protocolCodeLength;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        in.markReaderIndex();
        ProtocolCode protocolCode = decodeProtocolCode(in);
        if (null != protocolCode) {
            byte protocolVersion = decodeProtocolVersion(in);
            if (ctx.channel().attr(Connection.PROTOCOL).get() == null) {
                // 为连接设置协议码信息
                ctx.channel().attr(Connection.PROTOCOL).set(protocolCode);
                if (protocolVersion != DEFAULT_ILLEGAL_PROTOCOL_VERSION_LENGTH) {
                    // 为连接设置协议版本号信息
                    ctx.channel().attr(Connection.PROTOCOL_VERSION).set(protocolVersion);
                }
            }
            Protocol protocol = ProtocolManager.getProtocol(protocolCode);
            if (null != protocol) {
                in.resetReaderIndex();
                protocol.getDecoder().decode(ctx, in, out);
            } else {
                throw new CodecException("Unknown protocol code: [" + protocolCode + "] while decode in ProtocolDecoder.");
            }
        }
    }

    /**
     * 功能描述: <br>
     * 〈decode the protocol code.〉
     *
     * @param in
     * @return > roberto.group.process.netty.practice.protocol.ProtocolCode
     * @author HuangTaiHong
     * @date 2019.01.02 19:13:39
     */
    protected ProtocolCode decodeProtocolCode(ByteBuf in) {
        if (in.readableBytes() >= protocolCodeLength) {
            byte[] protocolCodeBytes = new byte[protocolCodeLength];
            in.readBytes(protocolCodeBytes);
            return ProtocolCode.fromBytes(protocolCodeBytes);
        }
        return null;
    }

    /**
     * 功能描述: <br>
     * 〈decode the protocol version.〉
     *
     * @param in
     * @return > byte
     * @author HuangTaiHong
     * @date 2019.01.02 19:16:37
     */
    protected byte decodeProtocolVersion(ByteBuf in) {
        if (in.readableBytes() >= DEFAULT_PROTOCOL_VERSION_LENGTH) {
            return in.readByte();
        }
        return DEFAULT_ILLEGAL_PROTOCOL_VERSION_LENGTH;
    }
}