/**
 * FileName: DefaultInvokeFuture
 * Author:   HuangTaiHong
 * Date:     2019/1/2 11:11
 * Description: The default implementation of InvokeFuture.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.remote.invoke.future;

import io.netty.util.Timeout;
import lombok.extern.slf4j.Slf4j;
import roberto.group.process.netty.practice.command.command.RemotingCommand;
import roberto.group.process.netty.practice.command.command.response.ResponseCommand;
import roberto.group.process.netty.practice.command.factory.CommandFactory;
import roberto.group.process.netty.practice.command.handler.CommandHandler;
import roberto.group.process.netty.practice.protocol.Protocol;
import roberto.group.process.netty.practice.protocol.ProtocolCode;
import roberto.group.process.netty.practice.protocol.ProtocolManager;
import roberto.group.process.netty.practice.remote.invoke.callback.InvokeCallback;
import roberto.group.process.netty.practice.context.InvokeContext;
import roberto.group.process.netty.practice.remote.invoke.callback.InvokeCallbackListener;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 〈一句话功能简述〉<br>
 * 〈The default implementation of InvokeFuture.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/2
 * @since 1.0.0
 */
@Slf4j
public class DefaultInvokeFuture implements InvokeFuture {
    private int invokeId;

    private byte protocol;

    private Timeout timeout;

    private Throwable cause;

    private ClassLoader classLoader;

    private CommandFactory commandFactory;

    private InvokeCallback callback;

    private InvokeContext invokeContext;

    private InvokeCallbackListener callbackListener;

    private volatile ResponseCommand responseCommand;

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private final AtomicBoolean executeCallbackOnlyOnce = new AtomicBoolean(false);

    public DefaultInvokeFuture(int invokeId, InvokeCallbackListener callbackListener, InvokeCallback callback, byte protocol, CommandFactory commandFactory) {
        this.invokeId = invokeId;
        this.callbackListener = callbackListener;
        this.callback = callback;
        this.classLoader = Thread.currentThread().getContextClassLoader();
        this.protocol = protocol;
        this.commandFactory = commandFactory;
    }

    public DefaultInvokeFuture(int invokeId, InvokeCallbackListener callbackListener, InvokeCallback callback, byte protocol, CommandFactory commandFactory, InvokeContext invokeContext) {
        this(invokeId, callbackListener, callback, protocol, commandFactory);
        this.invokeContext = invokeContext;
    }


    public int invokeId() {
        return this.invokeId;
    }

    public boolean isDone() {
        return this.countDownLatch.getCount() <= 0;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public Throwable getCause() {
        return this.cause;
    }

    public void putResponse(RemotingCommand response) {
        this.responseCommand = (ResponseCommand) response;
        this.countDownLatch.countDown();
    }

    public RemotingCommand waitResponse() throws InterruptedException {
        this.countDownLatch.await();
        return this.responseCommand;
    }

    public RemotingCommand waitResponse(long timeoutMillis) throws InterruptedException {
        this.countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        return this.responseCommand;
    }

    public RemotingCommand createConnectionClosedResponse(InetSocketAddress responseHost) {
        return this.commandFactory.createConnectionClosedResponse(responseHost, null);
    }

    public InvokeCallback getInvokeCallback() {
        return this.callback;
    }

    public void executeInvokeCallback() {
        if (callbackListener != null) {
            if (this.executeCallbackOnlyOnce.compareAndSet(false, true)) {
                callbackListener.onResponse(this);
            }
        }
    }

    public void tryAsyncExecuteInvokeCallbackAbnormally() {
        try {
            Protocol protocol = ProtocolManager.getProtocol(ProtocolCode.fromBytes(this.protocol));
            if (null != protocol) {
                CommandHandler commandHandler = protocol.getCommandHandler();
                if (null != commandHandler) {
                    ExecutorService executor = commandHandler.getDefaultExecutor();
                    if (null != executor) {
                        executor.execute(() -> {
                            ClassLoader oldClassLoader = null;
                            try {
                                if (DefaultInvokeFuture.this.getAppClassLoader() != null) {
                                    oldClassLoader = Thread.currentThread().getContextClassLoader();
                                    Thread.currentThread().setContextClassLoader(DefaultInvokeFuture.this.getAppClassLoader());
                                }
                                DefaultInvokeFuture.this.executeInvokeCallback();
                            } finally {
                                if (null != oldClassLoader) {
                                    Thread.currentThread().setContextClassLoader(oldClassLoader);
                                }
                            }
                        });
                    }
                } else {
                    log.error("Executor null in commandHandler of protocolCode [{}].", this.protocol);
                }
            } else {
                log.error("protocolCode [{}] not registered!", this.protocol);
            }
        } catch (Exception e) {
            log.error("Exception caught when executing invoke callback abnormally.", e);
        }
    }

    public InvokeContext getInvokeContext() {
        return invokeContext;
    }

    public void setInvokeContext(InvokeContext invokeContext) {
        this.invokeContext = invokeContext;
    }

    public void addTimeout(Timeout timeout) {
        this.timeout = timeout;
    }

    public void cancelTimeout() {
        if (this.timeout != null) {
            this.timeout.cancel();
        }
    }

    public ClassLoader getAppClassLoader() {
        return this.classLoader;
    }
}