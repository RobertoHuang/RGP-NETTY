/**
 * FileName: InvokeFuture
 * Author:   HuangTaiHong
 * Date:     2019/1/2 10:55
 * Description: The future of an invocation.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.remote.invoke.future;

import io.netty.util.Timeout;
import roberto.group.process.netty.practice.command.command.RemotingCommand;
import roberto.group.process.netty.practice.remote.invoke.callback.InvokeCallback;
import roberto.group.process.netty.practice.context.InvokeContext;

import java.net.InetSocketAddress;

/**
 * 〈一句话功能简述〉<br>
 * 〈The future of an invocation.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/2
 * @since 1.0.0
 */
public interface InvokeFuture {
    /**
     * 功能描述: <br>
     * 〈Get the id of the invocation.〉
     *
     * @return > int
     * @author HuangTaiHong
     * @date 2019.01.02 10:58:59
     */
    int invokeId();

    /**
     * 功能描述: <br>
     * 〈Whether the future is done.〉
     *
     * @return > boolean
     * @author HuangTaiHong
     * @date 2019.01.02 11:03:21
     */
    boolean isDone();

    /**
     * 功能描述: <br>
     * 〈Set the cause if exception caught.〉
     *
     * @param cause
     * @author HuangTaiHong
     * @date 2019.01.02 11:00:48
     */
    void setCause(Throwable cause);

    /**
     * 功能描述: <br>
     * 〈Get the cause of exception of the future.〉
     *
     * @return > java.lang.Throwable
     * @author HuangTaiHong
     * @date 2019.01.02 11:01:07
     */
    Throwable getCause();

    /**
     * 功能描述: <br>
     * 〈Put the response to the future.〉
     *
     * @param response
     * @author HuangTaiHong
     * @date 2019.01.02 10:58:25
     */
    void putResponse(final RemotingCommand response);

    /**
     * 功能描述: <br>
     * 〈Wait response with unlimit timeout.〉
     *
     * @return > roberto.group.process.netty.practice.command.command.RemotingCommand
     * @throws InterruptedException
     * @author HuangTaiHong
     * @date 2019.01.02 10:56:02
     */
    RemotingCommand waitResponse() throws InterruptedException;

    /**
     * 功能描述: <br>
     * 〈Wait response with timeout.〉
     *
     * @param timeoutMillis
     * @return > roberto.group.process.netty.practice.command.command.RemotingCommand
     * @throws InterruptedException
     * @author HuangTaiHong
     * @date 2019.01.02 10:56:19
     */
    RemotingCommand waitResponse(final long timeoutMillis) throws InterruptedException;

    /**
     * 功能描述: <br>
     * 〈Create a remoting command response when connection closed.〉
     *
     * @param responseHost
     * @return > roberto.group.process.netty.practice.command.command.RemotingCommand
     * @author HuangTaiHong
     * @date 2019.01.02 10:57:15
     */
    RemotingCommand createConnectionClosedResponse(InetSocketAddress responseHost);

    /**
     * 功能描述: <br>
     * 〈Get the application callback of the future.〉
     *
     * @return > roberto.group.process.netty.practice.remote.invoke.callback.InvokeCallback
     * @author HuangTaiHong
     * @date 2019.01.02 11:02:07
     */
    InvokeCallback getInvokeCallback();

    /**
     * 功能描述: <br>
     * 〈Execute the callback.〉
     *
     * @author HuangTaiHong
     * @date 2019.01.02 10:59:21
     */
    void executeInvokeCallback();

    /**
     * 功能描述: <br>
     * 〈Asynchronous execute the callback abnormally.〉
     *
     * @author HuangTaiHong
     * @date 2019.01.02 10:59:49
     */
    void tryAsyncExecuteInvokeCallbackAbnormally();

    /**
     * 功能描述: <br>
     * 〈Get invoke context.〉
     *
     * @return > roberto.group.process.netty.practice.context.InvokeContext
     * @author HuangTaiHong
     * @date 2019.01.02 11:04:35
     */
    InvokeContext getInvokeContext();

    /**
     * 功能描述: <br>
     * 〈set invoke context.〉
     *
     * @param invokeContext
     * @author HuangTaiHong
     * @date 2019.01.02 11:04:23
     */
    void setInvokeContext(InvokeContext invokeContext);

    /**
     * 功能描述: <br>
     * 〈Add timeout for the future.〉
     *
     * @param timeout
     * @author HuangTaiHong
     * @date 2019.01.02 11:02:49
     */
    void addTimeout(Timeout timeout);

    /**
     * 功能描述: <br>
     * 〈Cancel the timeout.〉
     *
     *  Added a scheduled task when performing an asynchronous request
     *
     *  Need to close the previous scheduled task after the request is successful
     *
     * @author HuangTaiHong
     * @date 2019.01.02 11:03:08
     */
    void cancelTimeout();

    /**
     * 功能描述: <br>
     * 〈Get application classloader.〉
     *
     * @return > java.lang.ClassLoader
     * @author HuangTaiHong
     * @date 2019.01.02 11:03:52
     */
    ClassLoader getAppClassLoader();
}