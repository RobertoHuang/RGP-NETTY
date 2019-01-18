/**
 * FileName: RPCResponseProcessor
 * Author:   HuangTaiHong
 * Date:     2019/1/7 11:29
 * Description: Processor to process RPCResponse.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.processor.processor.impl;

import io.netty.channel.Channel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import roberto.group.process.netty.practice.command.command.response.impl.RPCResponseCommand;
import roberto.group.process.netty.practice.command.processor.processor.RPCRemotingProcessor;
import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.remote.invoke.future.InvokeFuture;
import roberto.group.process.netty.practice.context.RemotingContext;
import roberto.group.process.netty.practice.utils.RemotingAddressUtil;

import java.util.concurrent.ExecutorService;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Processor to process RPCResponse.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/7
 * @since 1.0.0
 */
@Slf4j
@NoArgsConstructor
public class RPCResponseProcessor extends RPCRemotingProcessor<RPCResponseCommand> {
    public RPCResponseProcessor(ExecutorService executor) {
        super(executor);
    }

    @Override
    public void doProcess(RemotingContext context, RPCResponseCommand responseCommand) throws Exception {
        ClassLoader oldClassLoader = null;
        Channel channel = context.getChannelContext().channel();
        Connection connection = channel.attr(Connection.CONNECTION).get();
        InvokeFuture future = connection.removeInvokeFuture(responseCommand.getId());
        try {
            if (future != null) {
                if (future.getAppClassLoader() != null) {
                    oldClassLoader = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(future.getAppClassLoader());
                }
                future.putResponse(responseCommand);
                future.cancelTimeout();
                try {
                    future.executeInvokeCallback();
                } catch (Exception e) {
                    log.error("Exception caught when executing invoke callback, id={}", responseCommand.getId(), e);
                }
            } else {
                log.warn("Cannot find InvokeFuture, maybe already timeout, id={}, from={} ", responseCommand.getId(), RemotingAddressUtil.parseRemoteAddress(context.getChannelContext().channel()));
            }
        } finally {
            if (null != oldClassLoader) {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }
        }
    }
}