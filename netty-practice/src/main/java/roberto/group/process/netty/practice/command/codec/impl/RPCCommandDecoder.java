/**
 * FileName: RPCCommandDecoder
 * Author:   HuangTaiHong
 * Date:     2019/1/8 17:55
 * Description: Command decoder for RPC.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.codec.impl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import roberto.group.process.netty.practice.command.code.RemoteCommandCode;
import roberto.group.process.netty.practice.command.codec.CommandDecoder;
import roberto.group.process.netty.practice.command.command.RPCCommandType;
import roberto.group.process.netty.practice.command.command.request.RequestCommand;
import roberto.group.process.netty.practice.command.command.request.impl.HeartbeatCommand;
import roberto.group.process.netty.practice.command.command.request.impl.RPCRequestCommand;
import roberto.group.process.netty.practice.command.command.response.ResponseCommand;
import roberto.group.process.netty.practice.command.command.response.ResponseStatusEnum;
import roberto.group.process.netty.practice.command.command.response.impl.HeartbeatAckCommand;
import roberto.group.process.netty.practice.command.command.response.impl.RPCResponseCommand;
import roberto.group.process.netty.practice.configuration.switches.impl.ProtocolSwitch;
import roberto.group.process.netty.practice.protocol.impl.RPCProtocol;
import roberto.group.process.netty.practice.utils.CRCUtil;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈Command decoder for RPC.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/8
 * @since 1.0.0
 */
@Slf4j
@SuppressWarnings({"all"})
public class RPCCommandDecoder implements CommandDecoder {
    private int lessLength;

    public RPCCommandDecoder() {
        int requestLength = RPCProtocol.getRequestHeaderLength();
        int responseLength = RPCProtocol.getResponseHeaderLength();
        this.lessLength = responseLength < requestLength ? responseLength : requestLength;
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // the less length between response header and request header
        if (in.readableBytes() >= lessLength) {
            in.markReaderIndex();
            byte protocol = in.readByte();
            in.resetReaderIndex();
            if (protocol == RPCProtocol.PROTOCOL_CODE) {
                if (in.readableBytes() > 2 + 1) {
                    int startIndex = in.readerIndex();
                    in.markReaderIndex();
                    /** proto: magic code for protocol **/
                    in.readByte();
                    /** version: version for protocol **/
                    byte version = in.readByte();
                    /** type: request/response/request oneway **/
                    byte type = in.readByte();
                    if (type == RPCCommandType.REQUEST || type == RPCCommandType.REQUEST_ONEWAY) {
                        decodeRequest(in, out, startIndex, version, type);
                    } else if (type == RPCCommandType.RESPONSE) {
                        decodeResponse(ctx, in, out, startIndex, version, type);
                    } else {
                        String errorMsg = "Unknown command type: " + type;
                        log.error(errorMsg);
                        throw new RuntimeException(errorMsg);
                    }
                }
            } else {
                String errorMsg = "Unknown protocol: " + protocol;
                log.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
        }
    }

    private void decodeRequest(ByteBuf in, List<Object> out, int startIndex, byte version, byte type) {
        // decode request
        if (in.readableBytes() >= RPCProtocol.getRequestHeaderLength() - 3) {
            /** remotingCommandCode: code for remoting command **/
            short commandCode = in.readShort();
            /** version: version for remoting command **/
            byte version2 = in.readByte();
            /** requestId: id of request **/
            int requestId = in.readInt();
            /** codec: code for codec **/
            byte serializer = in.readByte();
            /** switch: function switch **/
            byte protocolSwitchValue = in.readByte();
            /** (request) timeout: request timeout. **/
            int timeout = in.readInt();
            /** classLen: length of request or response class name **/
            short classLen = in.readShort();
            /** headerLen: length of header **/
            short headerLen = in.readShort();
            /** cotentLen: length of content **/
            int contentLen = in.readInt();

            byte[] clazz = null;
            byte[] header = null;
            byte[] content = null;

            // decide the at-least bytes length for each version
            int lengthAtLeastForV1 = classLen + headerLen + contentLen;
            boolean crcSwitchOn = ProtocolSwitch.isOn(ProtocolSwitch.CRC_SWITCH_INDEX, protocolSwitchValue);
            int lengthAtLeastForV2 = crcSwitchOn ? lengthAtLeastForV1 + 4 : lengthAtLeastForV1;
            // continue read ensure that the number of readable bytes is greater than lengthAtLeast
            if ((version == RPCProtocol.PROTOCOL_VERSION_1 && in.readableBytes() >= lengthAtLeastForV1) || (version == RPCProtocol.PROTOCOL_VERSION_2 && in.readableBytes() >= lengthAtLeastForV2)) {
                if (classLen > 0) {
                    /** className **/
                    clazz = new byte[classLen];
                    in.readBytes(clazz);
                }

                if (headerLen > 0) {
                    /** header **/
                    header = new byte[headerLen];
                    in.readBytes(header);
                }

                if (contentLen > 0) {
                    /** content **/
                    content = new byte[contentLen];
                    in.readBytes(content);
                }

                if (version == RPCProtocol.PROTOCOL_VERSION_2 && crcSwitchOn) {
                    checkCRC(in, startIndex);
                }
            } else {
                // not enough data
                in.resetReaderIndex();
                return;
            }

            RequestCommand command;
            if (commandCode == RemoteCommandCode.HEARTBEAT.value()) {
                command = new HeartbeatCommand();
            } else {
                command = createRequestCommand(commandCode);
            }
            command.setType(type);
            command.setVersion(version2);
            command.setId(requestId);
            command.setSerializer(serializer);
            command.setProtocolSwitch(ProtocolSwitch.create(protocolSwitchValue));
            command.setTimeout(timeout);
            command.setClazz(clazz);
            command.setHeader(header);
            command.setContent(content);
            out.add(command);
        } else {
            in.resetReaderIndex();
        }
    }

    private void decodeResponse(ChannelHandlerContext ctx, ByteBuf in, List<Object> out, int startIndex, byte version, byte type) {
        // decode response
        if (in.readableBytes() >= RPCProtocol.getResponseHeaderLength() - 3) {
            /** remotingCommandCode: code for remoting command **/
            short commandCode = in.readShort();
            /** version: version for remoting command **/
            byte version2 = in.readByte();
            /** requestId: id of request **/
            int requestId = in.readInt();
            /** codec: code for codec **/
            byte serializer = in.readByte();
            /** switch: function switch **/
            byte protocolSwitchValue = in.readByte();
            /** (resp) respStatus: response status **/
            short status = in.readShort();
            /** classLen: length of request or response class name **/
            short classLen = in.readShort();
            /** headerLen: length of header **/
            short headerLen = in.readShort();
            /** cotentLen: length of content **/
            int contentLen = in.readInt();
            byte[] clazz = null;
            byte[] header = null;
            byte[] content = null;

            // decide the at-least bytes length for each version
            int lengthAtLeastForV1 = classLen + headerLen + contentLen;
            boolean crcSwitchOn = ProtocolSwitch.isOn(ProtocolSwitch.CRC_SWITCH_INDEX, protocolSwitchValue);
            int lengthAtLeastForV2 = crcSwitchOn ? lengthAtLeastForV1 + 4 : lengthAtLeastForV1;
            // continue read
            if ((version == RPCProtocol.PROTOCOL_VERSION_1 && in.readableBytes() >= lengthAtLeastForV1) || (version == RPCProtocol.PROTOCOL_VERSION_2 && in.readableBytes() >= lengthAtLeastForV2)) {
                if (classLen > 0) {
                    /** className **/
                    clazz = new byte[classLen];
                    in.readBytes(clazz);
                }
                if (headerLen > 0) {
                    /** header **/
                    header = new byte[headerLen];
                    in.readBytes(header);
                }
                if (contentLen > 0) {
                    /** content **/
                    content = new byte[contentLen];
                    in.readBytes(content);
                }
                if (version == RPCProtocol.PROTOCOL_VERSION_2 && crcSwitchOn) {
                    checkCRC(in, startIndex);
                }
            } else {
                // not enough data
                in.resetReaderIndex();
                return;
            }
            ResponseCommand command;
            if (commandCode == RemoteCommandCode.HEARTBEAT.value()) {
                command = new HeartbeatAckCommand();
            } else {
                command = createResponseCommand(commandCode);
            }
            command.setType(type);
            command.setVersion(version2);
            command.setId(requestId);
            command.setSerializer(serializer);
            command.setProtocolSwitch(ProtocolSwitch.create(protocolSwitchValue));
            command.setResponseStatus(ResponseStatusEnum.valueOf(status));
            command.setClazz(clazz);
            command.setHeader(header);
            command.setContent(content);
            command.setResponseTimeMillis(System.currentTimeMillis());
            command.setResponseHost((InetSocketAddress) ctx.channel().remoteAddress());
            out.add(command);
        } else {
            in.resetReaderIndex();
        }
    }

    /**
     * 功能描述: <br>
     * 〈check CRC32.〉
     *
     * @param in
     * @param startIndex
     * @author HuangTaiHong
     * @date 2019.01.08 20:04:27
     */
    private void checkCRC(ByteBuf in, int startIndex) {
        int endIndex = in.readerIndex();
        int expectedCRC = in.readInt();
        byte[] frame = new byte[endIndex - startIndex];
        in.getBytes(startIndex, frame, 0, endIndex - startIndex);
        if (expectedCRC != CRCUtil.crc32(frame)) {
            throw new RuntimeException("CRC check failed!");
        }
    }

    /**
     * 功能描述: <br>
     * 〈create RPCRequestCommand.〉
     *
     * @param commandCode
     * @return > roberto.group.process.netty.practice.command.command.request.impl.RPCRequestCommand
     * @author HuangTaiHong
     * @date 2019.01.08 20:10:02
     */
    private RPCRequestCommand createRequestCommand(short commandCode) {
        RPCRequestCommand command = new RPCRequestCommand();
        command.setRemoteCommandCode(RemoteCommandCode.valueOf(commandCode));
        command.setArriveTime(System.currentTimeMillis());
        return command;
    }

    /**
     * 功能描述: <br>
     * 〈create ResponseCommand.〉
     *
     * @param commandCode
     * @return > roberto.group.process.netty.practice.command.command.response.ResponseCommand
     * @author HuangTaiHong
     * @date 2019.01.08 20:21:46
     */
    private ResponseCommand createResponseCommand(short commandCode) {
        ResponseCommand command = new RPCResponseCommand();
        command.setRemoteCommandCode(RemoteCommandCode.valueOf(commandCode));
        return command;
    }
}