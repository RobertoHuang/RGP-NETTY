/**
 * FileName: DefaultInvokeFuture
 * Author:   HuangTaiHong
 * Date:     2019/1/2 11:11
 * Description: 异步执行器默认实现
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.invoke.impl;

import io.netty.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import roberto.group.process.netty.practice.command.command.RemotingCommand;
import roberto.group.process.netty.practice.invoke.InvokeCallback;
import roberto.group.process.netty.practice.invoke.InvokeContext;
import roberto.group.process.netty.practice.invoke.InvokeFuture;

import java.net.InetSocketAddress;

/**
 * 〈一句话功能简述〉<br>
 * 〈异步执行器默认实现〉
 *
 * @author HuangTaiHong
 * @create 2019/1/2
 * @since 1.0.0
 */
public class DefaultInvokeFuture implements InvokeFuture {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultInvokeFuture.class);

    private int invokeId;

    private InvokeCallback callback;

//    private volatile ResponseCommand responseCommand;

    public int invokeId() {
        return 0;
    }

    public boolean isDone() {
        return false;
    }

    public void setCause(Throwable cause) {

    }

    public Throwable getCause() {
        return null;
    }

    public void putResponse(RemotingCommand response) {

    }

    public RemotingCommand waitResponse() throws InterruptedException {
        return null;
    }

    public RemotingCommand waitResponse(long timeoutMillis) throws InterruptedException {
        return null;
    }

    public RemotingCommand createConnectionClosedResponse(InetSocketAddress responseHost) {
        return null;
    }

    public InvokeCallback getInvokeCallback() {
        return null;
    }

    public void executeInvokeCallback() {

    }

    public void tryAsyncExecuteInvokeCallbackAbnormally() {

    }

    public void addTimeout(Timeout timeout) {

    }

    public void cancelTimeout() {

    }

    public void setInvokeContext(InvokeContext invokeContext) {

    }

    public InvokeContext getInvokeContext() {
        return null;
    }

    public ClassLoader getAppClassLoader() {
        return null;
    }
}