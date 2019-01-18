/**
 * FileName: RPCHeartBeatProcessor
 * Author:   HuangTaiHong
 * Date:     2019/1/7 11:31
 * Description: Processor for heart beat.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.processor.processor.impl;

import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import roberto.group.process.netty.practice.command.command.RemotingCommand;
import roberto.group.process.netty.practice.command.command.request.impl.HeartbeatCommand;
import roberto.group.process.netty.practice.command.command.response.impl.HeartbeatAckCommand;
import roberto.group.process.netty.practice.command.processor.processor.RPCRemotingProcessor;
import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.remote.invoke.future.InvokeFuture;
import roberto.group.process.netty.practice.context.RemotingContext;
import roberto.group.process.netty.practice.utils.RemotingAddressUtil;

/**
 * 〈一句话功能简述〉<br>
 * 〈Processor for heart beat.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/7
 * @since 1.0.0
 */
@Slf4j
public class RPCHeartBeatProcessor extends RPCRemotingProcessor {
    @Override
    public void doProcess(final RemotingContext remotingContext, RemotingCommand remotingCommand) {
        // process the heartbeat
        if (remotingCommand instanceof HeartbeatCommand) {
            final int id = remotingCommand.getId();
            log.debug("Heartbeat received! Id=" + id + ", from " + RemotingAddressUtil.parseRemoteAddress(remotingContext.getChannelContext().channel()));
            remotingContext.writeAndFlush(new HeartbeatAckCommand(id)).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.debug("Send heartbeat ack done! Id={}, to remoteAddr={}", id, RemotingAddressUtil.parseRemoteAddress(remotingContext.getChannelContext().channel()));
                } else {
                    log.error("Send heartbeat ack failed! Id={}, to remoteAddr={}", id, RemotingAddressUtil.parseRemoteAddress(remotingContext.getChannelContext().channel()));
                }
            });
        } else if (remotingCommand instanceof HeartbeatAckCommand) {
            Connection connection = remotingContext.getChannelContext().channel().attr(Connection.CONNECTION).get();
            InvokeFuture future = connection.removeInvokeFuture(remotingCommand.getId());
            if (future != null) {
                future.putResponse(remotingCommand);
                future.cancelTimeout();
                try {
                    future.executeInvokeCallback();
                } catch (Exception e) {
                    log.error("Exception caught when executing heartbeat invoke callback. From {}", RemotingAddressUtil.parseRemoteAddress(remotingContext.getChannelContext().channel()), e);
                }
            } else {
                log.warn("Cannot find heartbeat InvokeFuture, maybe already timeout. Id={}, From {}", remotingCommand.getId(), RemotingAddressUtil.parseRemoteAddress(remotingContext.getChannelContext().channel()));
            }
        } else {
            throw new RuntimeException("Cannot process command: " + remotingCommand.getClass().getName());
        }
    }
}