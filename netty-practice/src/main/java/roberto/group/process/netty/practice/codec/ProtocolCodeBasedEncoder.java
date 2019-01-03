/**
 * FileName: ProtocolCodeBasedEncoder
 * Author:   HuangTaiHong
 * Date:     2019/1/2 19:46
 * Description: 基础编码器实现类
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.Attribute;
import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.protocol.Protocol;
import roberto.group.process.netty.practice.protocol.ProtocolCode;
import roberto.group.process.netty.practice.protocol.ProtocolManager;

import java.io.Serializable;

/**
 * 〈一句话功能简述〉<br> 
 * 〈基础编码器实现类〉
 *
 * @author HuangTaiHong
 * @create 2019/1/2
 * @since 1.0.0
 */
@ChannelHandler.Sharable
public class ProtocolCodeBasedEncoder extends MessageToByteEncoder<Serializable> {
    protected ProtocolCode defaultProtocolCode;

    public ProtocolCodeBasedEncoder(ProtocolCode defaultProtocolCode) {
        super();
        this.defaultProtocolCode = defaultProtocolCode;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out) throws Exception {
        Attribute<ProtocolCode> att = ctx.channel().attr(Connection.PROTOCOL);
        ProtocolCode protocolCode;
        if (att == null || att.get() == null) {
            protocolCode = this.defaultProtocolCode;
        } else {
            protocolCode = att.get();
        }
        Protocol protocol = ProtocolManager.getProtocol(protocolCode);
        protocol.getEncoder().encode(ctx, msg, out);
    }
}