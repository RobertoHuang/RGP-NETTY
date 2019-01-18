/**
 * FileName: RPCInvokeCallbackListener
 * Author:   HuangTaiHong
 * Date:     2019/1/5 18:24
 * Description: Listener which listens the Rpc invoke result, and then invokes the call back.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.remote.invoke.callback.impl;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import roberto.group.process.netty.practice.command.command.response.ResponseCommand;
import roberto.group.process.netty.practice.command.command.response.ResponseStatusEnum;
import roberto.group.process.netty.practice.command.command.response.impl.RPCResponseCommand;
import roberto.group.process.netty.practice.exception.CodecException;
import roberto.group.process.netty.practice.exception.ConnectionClosedException;
import roberto.group.process.netty.practice.exception.remote.InvokeException;
import roberto.group.process.netty.practice.exception.remote.InvokeServerBusyException;
import roberto.group.process.netty.practice.exception.remote.InvokeServerException;
import roberto.group.process.netty.practice.exception.remote.InvokeTimeoutException;
import roberto.group.process.netty.practice.remote.invoke.callback.InvokeCallback;
import roberto.group.process.netty.practice.remote.invoke.future.InvokeFuture;
import roberto.group.process.netty.practice.remote.invoke.callback.InvokeCallbackListener;
import roberto.group.process.netty.practice.thread.NamedThreadFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 〈一句话功能简述〉<br>
 * 〈Listener which listens the Rpc invoke result, and then invokes the call back.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/5
 * @since 1.0.0
 */
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class RPCInvokeCallbackListener implements InvokeCallbackListener {
    private String address;

    private static final ExecutorService DEFAULT_CALLBACK_EXECUTOR = new ThreadPoolExecutor(30, 50, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10000), new NamedThreadFactory("RGP-CALLBACK-EXECUTOR", false));

    @Override
    public String getRemoteAddress() {
        return this.address;
    }

    @Override
    public void onResponse(InvokeFuture future) {
        InvokeCallback callback = future.getInvokeCallback();
        if (callback != null) {
            CallbackTask callbackTask = new CallbackTask(this.getRemoteAddress(), future);
            Executor executor = callback.getExecutor() == null ? DEFAULT_CALLBACK_EXECUTOR : callback.getExecutor();
            try {
                executor.execute(callbackTask);
            } catch (RejectedExecutionException e) {
                log.warn("Callback thread pool busy.");
            }
        }
    }

    @AllArgsConstructor
    class CallbackTask implements Runnable {
        private String remoteAddress;
        private InvokeFuture invokeFuture;

        @Override
        public void run() {
            ResponseCommand response = null;
            InvokeCallback callback = invokeFuture.getInvokeCallback();
            try {
                response = (ResponseCommand) invokeFuture.waitResponse(0);
            } catch (InterruptedException e) {
                log.error("Exception caught when getting response from InvokeFuture. The address is {}", this.remoteAddress, e);
            }
            if (response == null || response.getResponseStatus() != ResponseStatusEnum.SUCCESS) {
                try {
                    Exception exception;
                    if (response == null) {
                        exception = new InvokeException("Exception caught in invocation. The address is " + this.remoteAddress + " responseStatus:" + ResponseStatusEnum.UNKNOWN, invokeFuture.getCause());
                    } else {
                        response.setInvokeContext(invokeFuture.getInvokeContext());
                        switch (response.getResponseStatus()) {
                            case TIMEOUT:
                                exception = new InvokeTimeoutException("Invoke timeout when invoke with callback.The address is " + this.remoteAddress);
                                break;
                            case CONNECTION_CLOSED:
                                exception = new ConnectionClosedException("Connection closed when invoke with callback.The address is " + this.remoteAddress);
                                break;
                            case SERVER_THREAD_POOL_BUSY:
                                exception = new InvokeServerBusyException("Server thread pool busy when invoke with callback.The address is " + this.remoteAddress);
                                break;
                            case SERVER_EXCEPTION:
                                Throwable throwable = toThrowable(response);
                                String errorMsg = "Server exception when invoke with callback.Please check the server log! The address is " + this.remoteAddress;
                                if (throwable == null) {
                                    exception = new InvokeServerException(errorMsg);
                                } else {
                                    exception = new InvokeServerException(errorMsg, throwable);
                                }
                                break;
                            default:
                                exception = new InvokeException("Exception caught in invocation. The address is " + this.remoteAddress + " responseStatus:" + response.getResponseStatus(), invokeFuture.getCause());
                        }
                    }
                    callback.onException(exception);
                } catch (Throwable e) {
                    log.error("Exception occurred in user defined InvokeCallback#onException() logic, The address is {}", this.remoteAddress, e);
                }
            } else {
                ClassLoader oldClassLoader = null;
                try {
                    if (invokeFuture.getAppClassLoader() != null) {
                        oldClassLoader = Thread.currentThread().getContextClassLoader();
                        Thread.currentThread().setContextClassLoader(invokeFuture.getAppClassLoader());
                    }
                    response.setInvokeContext(invokeFuture.getInvokeContext());
                    RPCResponseCommand responseCommand = (RPCResponseCommand) response;
                    response.deserialize();
                    try {
                        callback.onResponse(responseCommand.getResponseObject());
                    } catch (Throwable e) {
                        log.error("Exception occurred in user defined InvokeCallback#onResponse() logic.", e);
                    }
                } catch (CodecException e) {
                    log.error("CodecException caught on when deserialize response in RpcInvokeCallbackListener. The address is {}.", this.remoteAddress, e);
                } catch (Throwable e) {
                    log.error("Exception caught in RpcInvokeCallbackListener. The address is {}", this.remoteAddress, e);
                } finally {
                    if (oldClassLoader != null) {
                        Thread.currentThread().setContextClassLoader(oldClassLoader);
                    }
                }
            }
        }
    }

    /**
     * 功能描述: <br>
     * 〈Convert remoting response command to throwable if it is a throwable, otherwise return null.〉
     *
     * @param responseCommand
     * @return > java.lang.Throwable
     * @throws CodecException
     * @author HuangTaiHong
     * @date 2019.01.05 16:54:50
     */
    private static Throwable toThrowable(ResponseCommand responseCommand) throws CodecException {
        RPCResponseCommand response = (RPCResponseCommand) responseCommand;
        response.deserialize();
        Object throwable = response.getResponseObject();
        return (throwable != null && throwable instanceof Throwable) ? (Throwable) throwable : null;
    }
}