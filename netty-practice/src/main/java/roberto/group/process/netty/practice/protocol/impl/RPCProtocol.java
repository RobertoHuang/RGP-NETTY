/**
 * FileName: RPCProtocol
 * Author:   HuangTaiHong
 * Date:     2019/1/4 10:54
 * Description: RPC协议
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.protocol.impl;

import roberto.group.process.netty.practice.command.codec.CommandDecoder;
import roberto.group.process.netty.practice.command.codec.CommandEncoder;
import roberto.group.process.netty.practice.command.codec.impl.RPCCommandDecoder;
import roberto.group.process.netty.practice.command.codec.impl.RPCCommandEncoder;
import roberto.group.process.netty.practice.command.factory.CommandFactory;
import roberto.group.process.netty.practice.command.factory.impl.RPCCommandFactory;
import roberto.group.process.netty.practice.command.handler.CommandHandler;
import roberto.group.process.netty.practice.command.handler.impl.RPCCommandHandler;
import roberto.group.process.netty.practice.heartbeat.HeartbeatTrigger;
import roberto.group.process.netty.practice.heartbeat.impl.RPCHeartbeatTrigger;
import roberto.group.process.netty.practice.protocol.Protocol;

/**
 * 〈一句话功能简述〉<br>
 * 〈RPC协议〉
 *
 * @author HuangTaiHong
 * @create 2019/1/4
 * @since 1.0.0
 */
public class RPCProtocol implements Protocol {
    public static final byte PROTOCOL_CODE = (byte) 1;

    public static final int DEFAULT_PROTOCOL_CODE_LENGTH = 1;

    /** version 1, is the same with RPCProtocol */
    public static final byte PROTOCOL_VERSION_1 = (byte) 1;
    /** version 2, is the protocol version for RPCProtocolV2 */
    public static final byte PROTOCOL_VERSION_2  = (byte) 2;

    private static final int REQUEST_HEADER_LEN  = 22 + 2;
    private static final int RESPONSE_HEADER_LEN = 20 + 2;

    private CommandEncoder   encoder;
    private CommandDecoder   decoder;
    private CommandHandler commandHandler;
    private CommandFactory commandFactory;
    private HeartbeatTrigger heartbeatTrigger;

    public RPCProtocol() {
        this.encoder = new RPCCommandEncoder();
        this.decoder = new RPCCommandDecoder();
        this.commandFactory = new RPCCommandFactory();
        this.commandHandler = new RPCCommandHandler(this.commandFactory);
        this.heartbeatTrigger = new RPCHeartbeatTrigger(this.commandFactory);
    }

    @Override
    public CommandEncoder getEncoder() {
        return this.encoder;
    }

    @Override
    public CommandDecoder getDecoder() {
        return this.decoder;
    }

    @Override
    public HeartbeatTrigger getHeartbeatTrigger() {
        return this.heartbeatTrigger;
    }

    @Override
    public CommandHandler getCommandHandler() {
        return this.commandHandler;
    }

    @Override
    public CommandFactory getCommandFactory() {
        return this.commandFactory;
    }

    public static int getRequestHeaderLength() {
        return RPCProtocol.REQUEST_HEADER_LEN;
    }

    public static int getResponseHeaderLength() {
        return RPCProtocol.RESPONSE_HEADER_LEN;
    }
}