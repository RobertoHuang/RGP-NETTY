/**
 * FileName: RPCAsyncContext
 * Author:   HuangTaiHong
 * Date:     2019/1/7 19:13
 * Description: Async biz context of RPC.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.processor.context.impl;

import lombok.AllArgsConstructor;
import roberto.group.process.netty.practice.command.command.request.impl.RPCRequestCommand;
import roberto.group.process.netty.practice.command.processor.context.AsyncContext;
import roberto.group.process.netty.practice.command.processor.processor.impl.RPCRequestProcessor;
import roberto.group.process.netty.practice.remote.remote.RemotingContext;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 〈一句话功能简述〉<br>
 * 〈Async biz context of RPC.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/7
 * @since 1.0.0
 */
@AllArgsConstructor
public class RPCAsyncContext implements AsyncContext {
    /** remoting context */
    private RemotingContext remotingContext;

    /** rpc request command */
    private RPCRequestCommand requestCommand;

    /** rpc request processor **/
    private RPCRequestProcessor requestProcessor;

    /** is response sent already */
    private final AtomicBoolean isResponseSentAlready = new AtomicBoolean();

    @Override
    public void sendResponse(Object responseObject) {
        if (!isResponseSentAlready.compareAndSet(false, true)) {
            throw new IllegalStateException("should not send RPC response repeatedly!");
        } else {
            requestProcessor.sendResponseIfNecessary(this.remotingContext, requestCommand.getType(), requestProcessor.getCommandFactory().createResponse(responseObject, this.requestCommand));
        }
    }
}