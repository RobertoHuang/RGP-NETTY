/**
 * FileName: InvokeFuture
 * Author:   HuangTaiHong
 * Date:     2019/1/2 10:55
 * Description: 异步执行器接口
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.invoke;

import io.netty.util.Timeout;
import roberto.group.process.netty.practice.command.command.RemotingCommand;

import java.net.InetSocketAddress;

/**
 * 〈一句话功能简述〉<br>
 * 〈异步执行器接口〉
 *
 * @author HuangTaiHong
 * @create 2019/1/2
 * @since 1.0.0
 */
public interface InvokeFuture {
    /**
     * 功能描述: <br>
     * 〈获取任务ID〉
     *
     * @return > int
     * @author HuangTaiHong
     * @date 2019.01.02 10:58:59
     */
    int invokeId();

    /**
     * 功能描述: <br>
     * 〈异步任务是否已完成〉
     *
     * @return > boolean
     * @author HuangTaiHong
     * @date 2019.01.02 11:03:21
     */
    boolean isDone();

    /**
     * 功能描述: <br>
     * 〈设置异常信息〉
     *
     * @param cause
     * @author HuangTaiHong
     * @date 2019.01.02 11:00:48
     */
    void setCause(Throwable cause);

    /**
     * 功能描述: <br>
     * 〈获取异常信息〉
     *
     * @return > java.lang.Throwable
     * @author HuangTaiHong
     * @date 2019.01.02 11:01:07
     */
    Throwable getCause();

    /**
     * 功能描述: <br>
     * 〈设置响应结果〉
     *
     * @param response
     * @author HuangTaiHong
     * @date 2019.01.02 10:58:25
     */
    void putResponse(final RemotingCommand response);

    /**
     * 功能描述: <br>
     * 〈阻塞等待响应〉
     *
     * @return > roberto.group.process.netty.practice.command.command.RemotingCommand
     * @throws InterruptedException
     * @author HuangTaiHong
     * @date 2019.01.02 10:56:02
     */
    RemotingCommand waitResponse() throws InterruptedException;

    /**
     * 功能描述: <br>
     * 〈超时阻塞等待响应〉
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
     * 〈创建连接已关闭响应〉
     *
     * @param responseHost
     * @return > roberto.group.process.netty.practice.command.command.RemotingCommand
     * @author HuangTaiHong
     * @date 2019.01.02 10:57:15
     */
    RemotingCommand createConnectionClosedResponse(InetSocketAddress responseHost);

    /**
     * 功能描述: <br>
     * 〈获取回调处理器〉
     *
     * @return > roberto.group.process.netty.practice.invoke.InvokeCallback
     * @author HuangTaiHong
     * @date 2019.01.02 11:02:07
     */
    InvokeCallback getInvokeCallback();

    /**
     * 功能描述: <br>
     * 〈执行回调〉
     *
     * @author HuangTaiHong
     * @date 2019.01.02 10:59:21
     */
    void executeInvokeCallback();

    /**
     * 功能描述: <br>
     * 〈异步执行异常回调〉
     *
     * @author HuangTaiHong
     * @date 2019.01.02 10:59:49
     */
    void tryAsyncExecuteInvokeCallbackAbnormally();

    /**
     * 功能描述: <br>
     * 〈添加超时时间〉
     *
     * @param timeout
     * @author HuangTaiHong
     * @date 2019.01.02 11:02:49
     */
    void addTimeout(Timeout timeout);

    /**
     * 功能描述: <br>
     * 〈关闭超时机制〉
     *
     * @author HuangTaiHong
     * @date 2019.01.02 11:03:08
     */
    void cancelTimeout();

    /**
     * 功能描述: <br>
     * 〈设置任务执行上下文〉
     *
     * @param invokeContext
     * @author HuangTaiHong
     * @date 2019.01.02 11:04:23
     */
    void setInvokeContext(InvokeContext invokeContext);

    /**
     * 功能描述: <br>
     * 〈获取任务执行上下文〉
     *
     * @return > roberto.group.process.netty.practice.invoke.InvokeContext
     * @author HuangTaiHong
     * @date 2019.01.02 11:04:35
     */
    InvokeContext getInvokeContext();

    /**
     * 功能描述: <br>
     * 〈获取类加载器〉
     *
     * @return > java.lang.ClassLoader
     * @author HuangTaiHong
     * @date 2019.01.02 11:03:52
     */
    ClassLoader getAppClassLoader();
}