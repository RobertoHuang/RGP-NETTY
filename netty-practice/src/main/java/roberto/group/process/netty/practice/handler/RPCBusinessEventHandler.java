/**
 * FileName: RPCBusinessEventHandler
 * Author:   HuangTaiHong
 * Date:     2019/1/6 17:10
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import roberto.group.process.netty.practice.command.processor.CustomProcessor;
import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.protocol.Protocol;
import roberto.group.process.netty.practice.protocol.ProtocolCode;
import roberto.group.process.netty.practice.protocol.ProtocolManager;
import roberto.group.process.netty.practice.remote.invoke.context.InvokeContext;
import roberto.group.process.netty.practice.remote.remote.RPCRemotingContext;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author HuangTaiHong
 * @create 2019/1/6
 * @since 1.0.0
 */
@ChannelHandler.Sharable
public class RPCBusinessEventHandler extends ChannelInboundHandlerAdapter {
    private boolean serverSide;

    private ConcurrentHashMap<String, CustomProcessor<?>> customProcessors;

    public RPCBusinessEventHandler() {
        this.serverSide = false;
    }

    public RPCBusinessEventHandler(ConcurrentHashMap<String, CustomProcessor<?>> customProcessors) {
        this.serverSide = false;
        this.customProcessors = customProcessors;
    }

    public RPCBusinessEventHandler(boolean serverSide, ConcurrentHashMap<String, CustomProcessor<?>> customProcessors) {
        this.serverSide = serverSide;
        this.customProcessors = customProcessors;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ProtocolCode protocolCode = ctx.channel().attr(Connection.PROTOCOL).get();
        Protocol protocol = ProtocolManager.getProtocol(protocolCode);
        protocol.getCommandHandler().handleCommand(new RPCRemotingContext(ctx, new InvokeContext(), serverSide, customProcessors), msg);
    }
}