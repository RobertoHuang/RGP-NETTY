/**
 * FileName: RPCBusinessEventHandler
 * Author:   HuangTaiHong
 * Date:     2019/1/6 17:10
 * Description: Dispatch messages to corresponding protocol.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import roberto.group.process.netty.practice.command.processor.custom.UserProcessor;
import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.protocol.Protocol;
import roberto.group.process.netty.practice.protocol.ProtocolCode;
import roberto.group.process.netty.practice.protocol.ProtocolManager;
import roberto.group.process.netty.practice.context.InvokeContext;
import roberto.group.process.netty.practice.context.RemotingContext;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 〈一句话功能简述〉<br>
 * 〈Dispatch messages to corresponding protocol.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/6
 * @since 1.0.0
 */
@ChannelHandler.Sharable
public class RPCBusinessEventHandler extends ChannelInboundHandlerAdapter {
    private boolean serverSide;

    private ConcurrentHashMap<String, UserProcessor<?>> userProcessors;

    public RPCBusinessEventHandler() {
        this.serverSide = false;
    }

    public RPCBusinessEventHandler(ConcurrentHashMap<String, UserProcessor<?>> userProcessors) {
        this.serverSide = false;
        this.userProcessors = userProcessors;
    }

    public RPCBusinessEventHandler(boolean serverSide, ConcurrentHashMap<String, UserProcessor<?>> userProcessors) {
        this.serverSide = serverSide;
        this.userProcessors = userProcessors;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        ProtocolCode protocolCode = ctx.channel().attr(Connection.PROTOCOL).get();
        Protocol protocol = ProtocolManager.getProtocol(protocolCode);
        protocol.getCommandHandler().handleCommand(new RemotingContext(ctx, new InvokeContext(), serverSide, userProcessors), message);
    }
}