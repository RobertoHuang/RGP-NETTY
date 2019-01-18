/**
 * FileName: AsyncServerUserProcessor
 * Author:   HuangTaiHong
 * Date:     2019/1/15 16:14
 * Description: a demo aysnc user processor for RPC server.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.common;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import roberto.group.process.netty.practice.context.AsyncContext;
import roberto.group.process.netty.practice.command.processor.custom.AsyncUserProcessor;
import roberto.group.process.netty.practice.context.BizContext;
import roberto.group.process.netty.practice.thread.NamedThreadFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 〈一句话功能简述〉<br> 
 * 〈a demo aysnc user processor for RPC server.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/15
 * @since 1.0.0
 */
@Slf4j
public class AsyncServerUserProcessor extends AsyncUserProcessor<RequestBody> {
    /** delay milliseconds */
    private long delayMs;

    /** whether delay or not */
    private boolean delaySwitch;

    /** whether exception */
    private boolean isException;

    /** whether null */
    private boolean isNull;

    /** executor */
    private ThreadPoolExecutor executor;

    private ThreadPoolExecutor asyncExecutor;

    private AtomicInteger invokeTimes = new AtomicInteger();
    private AtomicInteger onewayTimes = new AtomicInteger();
    private AtomicInteger syncTimes = new AtomicInteger();
    private AtomicInteger futureTimes = new AtomicInteger();
    private AtomicInteger callbackTimes = new AtomicInteger();

    private String remoteAddress;
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public AsyncServerUserProcessor() {
        this.delayMs = 0;
        this.delaySwitch = false;
        this.isException = false;
        this.executor = new ThreadPoolExecutor(1, 3, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(4), new NamedThreadFactory("request-process-pool"));
        this.asyncExecutor = new ThreadPoolExecutor(1, 3, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(4), new NamedThreadFactory("another-aysnc-process-pool"));
    }

    public AsyncServerUserProcessor(boolean isException, boolean isNull) {
        this();
        this.isNull = isNull;
        this.isException = isException;
    }

    public AsyncServerUserProcessor(long delay) {
        this();
        if (delay < 0) {
            throw new IllegalArgumentException("delay time illegal!");
        }
        this.delayMs = delay;
        this.delaySwitch = true;
    }

    public AsyncServerUserProcessor(long delay, int core, int max, int keepaliveSeconds, int workQueue) {
        this(delay);
        this.executor = new ThreadPoolExecutor(core, max, keepaliveSeconds, TimeUnit.SECONDS, new ArrayBlockingQueue<>(workQueue), new NamedThreadFactory("request-process-pool"));
    }

    @Override
    public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, RequestBody request) {
        this.asyncExecutor.execute(new InnerTask(bizCtx, asyncCtx, request));
    }

    class InnerTask implements Runnable {
        private BizContext bizCtx;
        private AsyncContext asyncCtx;
        private RequestBody request;

        public InnerTask(BizContext bizCtx, AsyncContext asyncCtx, RequestBody request) {
            this.bizCtx = bizCtx;
            this.asyncCtx = asyncCtx;
            this.request = request;
        }

        public void run() {
            log.warn("Request received:" + request);
            remoteAddress = bizCtx.getRemoteAddress();
            countDownLatch.countDown();
            log.warn("Server User processor say, remote address is [" + remoteAddress + "].");
            Assert.assertEquals(RequestBody.class, request.getClass());
            processTimes(request);
            if (isException) {
                this.asyncCtx.sendResponse(new IllegalArgumentException("Exception test"));
            } else if (isNull) {
                this.asyncCtx.sendResponse(null);
            } else {
                if (!delaySwitch) {
                    this.asyncCtx.sendResponse(RequestBody.DEFAULT_SERVER_RETURN_STR);
                    return;
                }
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.asyncCtx.sendResponse(RequestBody.DEFAULT_SERVER_RETURN_STR);
            }
        }
    }

    @SuppressWarnings("all")
    private void processTimes(RequestBody req) {
        this.invokeTimes.incrementAndGet();
        if (req.getMsg().equals(RequestBody.DEFAULT_ONEWAY_STR)) {
            this.onewayTimes.incrementAndGet();
        } else if (req.getMsg().equals(RequestBody.DEFAULT_SYNC_STR)) {
            this.syncTimes.incrementAndGet();
        } else if (req.getMsg().equals(RequestBody.DEFAULT_FUTURE_STR)) {
            this.futureTimes.incrementAndGet();
        } else if (req.getMsg().equals(RequestBody.DEFAULT_CALLBACK_STR)) {
            this.callbackTimes.incrementAndGet();
        }
    }

    @Override
    public String interest() {
        return RequestBody.class.getName();
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }

    public int getInvokeTimes() {
        return this.invokeTimes.get();
    }

    public int getInvokeTimesEachCallType(RequestBody.InvokeType type) {
        return new int[]{this.onewayTimes.get(), this.syncTimes.get(), this.futureTimes.get(), this.callbackTimes.get()}[type.ordinal()];
    }

    public String getRemoteAddress() throws InterruptedException {
        countDownLatch.await(100, TimeUnit.MILLISECONDS);
        return this.remoteAddress;
    }
}