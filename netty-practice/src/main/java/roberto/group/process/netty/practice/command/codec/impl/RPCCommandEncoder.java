/**
 * FileName: RPCCommandEncoder
 * Author:   HuangTaiHong
 * Date:     2019/1/8 17:55
 * Description: Encode remoting command into ByteBuf.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.codec.impl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;
import roberto.group.process.netty.practice.command.codec.CommandEncoder;
import roberto.group.process.netty.practice.command.command.RPCRemotingCommand;
import roberto.group.process.netty.practice.command.command.request.RequestCommand;
import roberto.group.process.netty.practice.command.command.response.ResponseCommand;
import roberto.group.process.netty.practice.configuration.switches.impl.ProtocolSwitch;
import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.protocol.impl.RPCProtocol;
import roberto.group.process.netty.practice.utils.CRCUtil;

import java.io.Serializable;

/**
 * 〈一句话功能简述〉<br>
 * 〈Encode remoting command into ByteBuf.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/8
 * @since 1.0.0
 */
@Slf4j
public class RPCCommandEncoder implements CommandEncoder {
    @Override
    public void encode(ChannelHandlerContext ctx, Serializable message, ByteBuf out) throws Exception {
        try {
            if (message instanceof RPCRemotingCommand) {
                int index = out.writerIndex();
                /** proto: magic code for protocol **/
                RPCRemotingCommand remotingCommand = (RPCRemotingCommand) message;
                out.writeByte(RPCProtocol.PROTOCOL_CODE);

                /** version: version for protocol **/
                Attribute<Byte> versionAttr = ctx.channel().attr(Connection.VERSION);
                byte defaultVersion = RPCProtocol.PROTOCOL_VERSION_1;
                defaultVersion = (versionAttr != null && versionAttr.get() != null) ? versionAttr.get() : defaultVersion;
                out.writeByte(defaultVersion);

                /** type: request/response/request oneway **/
                out.writeByte(remotingCommand.getType());

                /** remotingCommandCode: code for remoting command **/
                out.writeShort(remotingCommand.getRemoteCommandCode().value());

                /** version: version for remoting command **/
                out.writeByte(remotingCommand.getVersion());

                /** requestId: id of request **/
                out.writeInt(remotingCommand.getId());

                /** codec: code for codec **/
                out.writeByte(remotingCommand.getSerializer());

                /** switch: function switch **/
                out.writeByte(remotingCommand.getProtocolSwitch().toByte());

                if (remotingCommand instanceof RequestCommand) {
                    // (request) timeout: request timeout.
                    out.writeInt(((RequestCommand) remotingCommand).getTimeout());
                }

                if (remotingCommand instanceof ResponseCommand) {
                    // (resp) respStatus: response status
                    ResponseCommand response = (ResponseCommand) remotingCommand;
                    out.writeShort(response.getResponseStatus().getValue());
                }

                /** classLen: length of class **/
                out.writeShort(remotingCommand.getClazzLength());
                /** headerLen: length of header **/
                out.writeShort(remotingCommand.getHeaderLength());
                /** cotentLen: length of content **/
                out.writeInt(remotingCommand.getContentLength());

                if (remotingCommand.getClazzLength() > 0) {
                    /** className **/
                    out.writeBytes(remotingCommand.getClazz());
                }

                if (remotingCommand.getHeaderLength() > 0) {
                    /** header **/
                    out.writeBytes(remotingCommand.getHeader());
                }
                if (remotingCommand.getContentLength() > 0) {
                    /** content **/
                    out.writeBytes(remotingCommand.getContent());
                }

                if (defaultVersion == RPCProtocol.PROTOCOL_VERSION_2 && remotingCommand.getProtocolSwitch().isOn(ProtocolSwitch.CRC_SWITCH_INDEX)) {
                    // compute the crc32 and write to out
                    byte[] frame = new byte[out.readableBytes()];
                    out.getBytes(index, frame).writeInt(CRCUtil.crc32(frame));
                }
            } else {
                log.warn("message type [{}] is not subclass of RPCRemotingCommand", message.getClass());
            }
        } catch (Exception e) {
            log.error("Exception caught when command encode!", e);
            throw e;
        }
    }
}