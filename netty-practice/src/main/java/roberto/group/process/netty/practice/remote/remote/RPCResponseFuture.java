/**
 * FileName: RPCResponseFuture
 * Author:   HuangTaiHong
 * Date:     2019/1/5 16:58
 * Description: The future for response.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.remote.remote;

import lombok.AllArgsConstructor;
import roberto.group.process.netty.practice.command.command.response.ResponseCommand;
import roberto.group.process.netty.practice.exception.RemotingException;
import roberto.group.process.netty.practice.exception.remote.InvokeTimeoutException;
import roberto.group.process.netty.practice.remote.invoke.future.InvokeFuture;

/**
 * 〈一句话功能简述〉<br>
 * 〈The future for response.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/5
 * @since 1.0.0
 */
@AllArgsConstructor
public class RPCResponseFuture {
    private String address;

    private InvokeFuture future;

    public boolean isDone() {
        return this.future.isDone();
    }

    public Object get() throws RemotingException, InterruptedException {
        ResponseCommand responseCommand = (ResponseCommand) this.future.waitResponse();
        responseCommand.setInvokeContext(this.future.getInvokeContext());
        return RPCResponseResolver.resolveResponseObject(responseCommand, address);
    }

    public Object get(int timeoutMillis) throws RemotingException, InterruptedException {
        this.future.waitResponse(timeoutMillis);
        if (!isDone()) {
            throw new InvokeTimeoutException("Future get result timeout!");
        }else{
            ResponseCommand responseCommand = (ResponseCommand) this.future.waitResponse();
            responseCommand.setInvokeContext(this.future.getInvokeContext());
            return RPCResponseResolver.resolveResponseObject(responseCommand, address);
        }
    }
}