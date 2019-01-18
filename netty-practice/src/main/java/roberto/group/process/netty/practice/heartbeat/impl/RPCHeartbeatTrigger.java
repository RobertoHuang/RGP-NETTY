/**
 * FileName: RPCHeartbeatTrigger
 * Author:   HuangTaiHong
 * Date:     2019/1/8 16:59
 * Description: Handler for heart beat.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.heartbeat.impl;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import roberto.group.process.netty.practice.command.command.request.impl.HeartbeatCommand;
import roberto.group.process.netty.practice.command.command.response.ResponseCommand;
import roberto.group.process.netty.practice.command.command.response.ResponseStatusEnum;
import roberto.group.process.netty.practice.command.factory.CommandFactory;
import roberto.group.process.netty.practice.configuration.manager.ConfigManager;
import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.heartbeat.HeartbeatTrigger;
import roberto.group.process.netty.practice.remote.invoke.callback.InvokeCallbackListener;
import roberto.group.process.netty.practice.remote.invoke.future.InvokeFuture;
import roberto.group.process.netty.practice.remote.invoke.future.DefaultInvokeFuture;
import roberto.group.process.netty.practice.thread.DelayedOperation;
import roberto.group.process.netty.practice.utils.RemotingAddressUtil;

import java.util.concurrent.TimeUnit;

/**
 * 〈一句话功能简述〉<br>
 * 〈Handler for heart beat.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/8
 * @since 1.0.0
 */
@Slf4j
public class RPCHeartbeatTrigger implements HeartbeatTrigger {
    private CommandFactory commandFactory;

    private static final long heartbeatTimeoutMillis = 1000;

    /** max trigger times */
    public static final Integer maxCount = ConfigManager.tcp_idle_maxtimes();

    public RPCHeartbeatTrigger(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    @Override
    public void heartbeatTriggered(final ChannelHandlerContext ctx) throws Exception {
        Integer heartbeatTimes = ctx.channel().attr(Connection.HEARTBEAT_COUNT).get();
        final Connection connection = ctx.channel().attr(Connection.CONNECTION).get();
        if (heartbeatTimes >= maxCount) {
            try {
                connection.close();
                log.error("Heartbeat failed for {} times, close the connection from client side: {} ", heartbeatTimes, RemotingAddressUtil.parseRemoteAddress(ctx.channel()));
            } catch (Exception e) {
                log.warn("Exception caught when closing connection in SharableHandler.", e);
            }
        } else {
            boolean heartbeatSwitch = ctx.channel().attr(Connection.HEARTBEAT_SWITCH).get();
            if (!heartbeatSwitch) {
                return;
            } else {
                final HeartbeatCommand heartbeatCommand = new HeartbeatCommand();
                final InvokeFuture future = new DefaultInvokeFuture(heartbeatCommand.getId(), new InvokeCallbackListener() {
                    @Override
                    public void onResponse(InvokeFuture future) {
                        ResponseCommand response;
                        try {
                            response = (ResponseCommand) future.waitResponse(0);
                        } catch (InterruptedException e) {
                            log.error("Heartbeat ack process error! Id={}, from remoteAddr={}", heartbeatCommand.getId(), RemotingAddressUtil.parseRemoteAddress(ctx.channel()), e);
                            return;
                        }

                        if (response != null && response.getResponseStatus() == ResponseStatusEnum.SUCCESS) {
                            log.debug("Heartbeat ack received! Id={}, from remoteAddr={}", response.getId(), RemotingAddressUtil.parseRemoteAddress(ctx.channel()));
                            ctx.channel().attr(Connection.HEARTBEAT_COUNT).set(0);
                        } else {
                            if (response == null) {
                                log.error("Heartbeat timeout! The address is {}", RemotingAddressUtil.parseRemoteAddress(ctx.channel()));
                            } else {
                                log.error("Heartbeat exception caught! Error code={}, The address is {}", response.getResponseStatus(), RemotingAddressUtil.parseRemoteAddress(ctx.channel()));
                            }
                            Integer times = ctx.channel().attr(Connection.HEARTBEAT_COUNT).get();
                            ctx.channel().attr(Connection.HEARTBEAT_COUNT).set(times + 1);
                        }
                    }

                    @Override
                    public String getRemoteAddress() {
                        return ctx.channel().remoteAddress().toString();
                    }
                }, null, heartbeatCommand.getProtocolCode().getFirstByte(), this.commandFactory);

                connection.addInvokeFuture(future);
                final int heartbeatId = heartbeatCommand.getId();
                log.debug("Send heartbeat, successive count={}, Id={}, to remoteAddr={}", heartbeatTimes, heartbeatId, RemotingAddressUtil.parseRemoteAddress(ctx.channel()));
                ctx.writeAndFlush(heartbeatCommand).addListener((ChannelFutureListener) channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        log.debug("Send heartbeat done! Id={}, to remoteAddr={}", heartbeatId, RemotingAddressUtil.parseRemoteAddress(ctx.channel()));
                    } else {
                        log.error("Send heartbeat failed! Id={}, to remoteAddr={}", heartbeatId, RemotingAddressUtil.parseRemoteAddress(ctx.channel()));
                    }
                });

                DelayedOperation.getTimer().newTimeout(timeout -> {
                    InvokeFuture invokeFuture = connection.removeInvokeFuture(heartbeatId);
                    if (invokeFuture != null) {
                        invokeFuture.putResponse(commandFactory.createTimeoutResponse(connection.getRemoteAddress()));
                        invokeFuture.tryAsyncExecuteInvokeCallbackAbnormally();
                    }
                }, heartbeatTimeoutMillis, TimeUnit.MILLISECONDS);
            }
        }
    }
}