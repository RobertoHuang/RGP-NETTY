/**
 * FileName: Remoting
 * Author:   HuangTaiHong
 * Date:     2019/1/4 14:01
 * Description: Base remoting capability.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.remote;

import io.netty.channel.ChannelFutureListener;
import io.netty.util.Timeout;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import roberto.group.process.netty.practice.command.factory.CommandFactory;
import roberto.group.process.netty.practice.command.command.RemotingCommand;
import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.remote.invoke.callback.InvokeCallback;
import roberto.group.process.netty.practice.remote.invoke.context.InvokeContext;
import roberto.group.process.netty.practice.remote.invoke.future.InvokeFuture;
import roberto.group.process.netty.practice.thread.DelayedOperation;
import roberto.group.process.netty.practice.utils.RemotingUtil;

import java.util.concurrent.TimeUnit;

/**
 * 〈一句话功能简述〉<br>
 * 〈Base remoting capability.〉
 *
 *  Remoting主要职责提供protected方法 往指定连接发送RemotingCommand
 *
 * @author HuangTaiHong
 * @create 2019/1/4
 * @since 1.0.0
 */
@Slf4j
@AllArgsConstructor
public abstract class Remoting {
    @Getter
    protected CommandFactory commandFactory;

    /**
     * 功能描述: <br>
     * 〈Synchronous invocation〉
     *
     * @param connection
     * @param request
     * @param timeoutMillis
     * @return > roberto.group.process.netty.practice.command.command.RemotingCommand
     * @throws InterruptedException
     * @author HuangTaiHong
     * @date 2019.01.04 14:22:03
     */
    protected RemotingCommand invokeSync(final Connection connection, final RemotingCommand request, final int timeoutMillis) throws InterruptedException {
        final InvokeFuture future = createInvokeFuture(request, request.getInvokeContext());
        connection.addInvokeFuture(future);
        try {
            connection.getChannel().writeAndFlush(request).addListener((ChannelFutureListener) channelFuture -> {
                if (!channelFuture.isSuccess()) {
                    connection.removeInvokeFuture(request.getId());
                    future.putResponse(commandFactory.createSendFailedResponse(connection.getRemoteAddress(), channelFuture.cause()));
                    log.error("Invoke send failed, id={}", request.getId(), channelFuture.cause());
                }
            });
        } catch (Exception e) {
            InvokeFuture exceptionFuture = connection.removeInvokeFuture(request.getId());
            if (exceptionFuture != null) {
                exceptionFuture.putResponse(commandFactory.createSendFailedResponse(connection.getRemoteAddress(), e));
            }
            log.error("Exception caught when sending invocation, id={}", request.getId(), e);
        }

        RemotingCommand response = future.waitResponse(timeoutMillis);
        if (response == null) {
            connection.removeInvokeFuture(request.getId());
            response = this.commandFactory.createTimeoutResponse(connection.getRemoteAddress());
            log.warn("Wait response, request id={} timeout!", request.getId());
        }
        return response;
    }

    /**
     * 功能描述: <br>
     * 〈Invocation with callback.〉
     *
     * @param connection
     * @param request
     * @param invokeCallback
     * @param timeoutMillis
     * @author HuangTaiHong
     * @date 2019.01.04 14:50:58
     */
    protected void invokeWithCallback(final Connection connection, final RemotingCommand request, final InvokeCallback invokeCallback, final int timeoutMillis) {
        final InvokeFuture future = createInvokeFuture(connection, request, request.getInvokeContext(), invokeCallback);
        connection.addInvokeFuture(future);
        try {
            // 添加定时任务 监听异步任务是否已超时
            Timeout timeout = DelayedOperation.getTimer().newTimeout(timeoutTemp -> {
                InvokeFuture timeoutFuture = connection.removeInvokeFuture(request.getId());
                if (timeoutFuture != null) {
                    timeoutFuture.putResponse(commandFactory.createTimeoutResponse(connection.getRemoteAddress()));
                    timeoutFuture.tryAsyncExecuteInvokeCallbackAbnormally();
                }
            }, timeoutMillis, TimeUnit.MILLISECONDS);
            future.addTimeout(timeout);

            connection.getChannel().writeAndFlush(request).addListener((ChannelFutureListener) channelFuture -> {
                if (!channelFuture.isSuccess()) {
                    InvokeFuture failedFuture = connection.removeInvokeFuture(request.getId());
                    if (failedFuture != null) {
                        failedFuture.cancelTimeout();
                        failedFuture.putResponse(commandFactory.createSendFailedResponse(connection.getRemoteAddress(), channelFuture.cause()));
                        failedFuture.tryAsyncExecuteInvokeCallbackAbnormally();
                    }
                    log.error("Invoke send failed. The address is {}", RemotingUtil.parseRemoteAddress(connection.getChannel()), channelFuture.cause());
                }
            });
        } catch (Exception e) {
            InvokeFuture exceptionFuture = connection.removeInvokeFuture(request.getId());
            if (exceptionFuture != null) {
                exceptionFuture.cancelTimeout();
                exceptionFuture.putResponse(commandFactory.createSendFailedResponse(connection.getRemoteAddress(), e));
                exceptionFuture.tryAsyncExecuteInvokeCallbackAbnormally();
            }
            log.error("Exception caught when sending invocation. The address is {}", RemotingUtil.parseRemoteAddress(connection.getChannel()), e);
        }
    }

    /**
     * 功能描述: <br>
     * 〈Invocation with future returned.〉
     *
     * @param connection
     * @param request
     * @param timeoutMillis
     * @return > roberto.group.process.netty.practice.remote.invoke.future.InvokeFuture
     * @author HuangTaiHong
     * @date 2019.01.04 15:35:47
     */
    protected InvokeFuture invokeWithFuture(final Connection connection, final RemotingCommand request, final int timeoutMillis) {
        final InvokeFuture future = createInvokeFuture(request, request.getInvokeContext());
        connection.addInvokeFuture(future);
        try {
            // 添加定时任务 监听异步任务是否已超时
            Timeout timeout = DelayedOperation.getTimer().newTimeout(timeoutTemp -> {
                InvokeFuture timeoutFuture = connection.removeInvokeFuture(request.getId());
                if (timeoutFuture != null) {
                    timeoutFuture.putResponse(commandFactory.createTimeoutResponse(connection.getRemoteAddress()));
                }
            }, timeoutMillis, TimeUnit.MILLISECONDS);
            future.addTimeout(timeout);

            connection.getChannel().writeAndFlush(request).addListener((ChannelFutureListener) channelFuture -> {
                if (!channelFuture.isSuccess()) {
                    InvokeFuture failedFuture = connection.removeInvokeFuture(request.getId());
                    if (failedFuture != null) {
                        failedFuture.cancelTimeout();
                        failedFuture.putResponse(commandFactory.createSendFailedResponse(connection.getRemoteAddress(), channelFuture.cause()));
                    }
                    log.error("Invoke send failed. The address is {}", RemotingUtil.parseRemoteAddress(connection.getChannel()), channelFuture.cause());
                }
            });
        } catch (Exception e) {
            InvokeFuture exceptionFuture = connection.removeInvokeFuture(request.getId());
            if (exceptionFuture != null) {
                exceptionFuture.cancelTimeout();
                exceptionFuture.putResponse(commandFactory.createSendFailedResponse(connection.getRemoteAddress(), e));
            }
            log.error("Exception caught when sending invocation. The address is {}", RemotingUtil.parseRemoteAddress(connection.getChannel()), e);
        }
        return future;
    }

    /**
     * 功能描述: <br>
     * 〈Oneway invocation.〉
     *
     * @param connection
     * @param request
     * @author HuangTaiHong
     * @date 2019.01.04 15:50:59
     */
    protected void oneway(final Connection connection, final RemotingCommand request) {
        try {
            connection.getChannel().writeAndFlush(request).addListener((ChannelFutureListener) channelFuture -> {
                if (!channelFuture.isSuccess()) {
                    log.error("Invoke send failed. The address is {}", RemotingUtil.parseRemoteAddress(connection.getChannel()), channelFuture.cause());
                }
            });
        } catch (Exception e) {
            if (null == connection) {
                log.error("Connection is null");
            } else {
                log.error("Exception caught when sending invocation. The address is {}", RemotingUtil.parseRemoteAddress(connection.getChannel()), e);
            }
        }
    }

    /**
     * 功能描述: <br>
     * 〈Create invoke future〉
     *
     * @param request
     * @param invokeContext
     * @return > roberto.group.process.netty.practice.remote.invoke.future.InvokeFuture
     * @author HuangTaiHong
     * @date 2019.01.04 14:25:27
     */
    protected abstract InvokeFuture createInvokeFuture(final RemotingCommand request, final InvokeContext invokeContext);

    /**
     * 功能描述: <br>
     * 〈Create invoke future〉
     *
     * @param connection
     * @param request
     * @param invokeContext
     * @param invokeCallback
     * @return > roberto.group.process.netty.practice.remote.invoke.future.InvokeFuture
     * @author HuangTaiHong
     * @date 2019.01.04 14:25:30
     */
    protected abstract InvokeFuture createInvokeFuture(final Connection connection, final RemotingCommand request, final InvokeContext invokeContext, final InvokeCallback invokeCallback);
}