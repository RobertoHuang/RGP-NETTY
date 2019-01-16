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
import roberto.group.process.netty.practice.command.processor.context.AsyncContext;
import roberto.group.process.netty.practice.command.processor.custom.impl.AsyncUserProcessor;
import roberto.group.process.netty.practice.remote.biz.BizContext;
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

    private String remoteAddr;
    private CountDownLatch latch = new CountDownLatch(1);

    public AsyncServerUserProcessor() {
        this.delaySwitch = false;
        this.isException = false;
        this.delayMs = 0;
        this.executor = new ThreadPoolExecutor(1, 3, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(4), new NamedThreadFactory("Request-process-pool"));
        this.asyncExecutor = new ThreadPoolExecutor(1, 3, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(4), new NamedThreadFactory(
                "Another-aysnc-process-pool"));
    }

    public AsyncServerUserProcessor(boolean isException, boolean isNull) {
        this();
        this.isException = isException;
        this.isNull = isNull;
    }

    public AsyncServerUserProcessor(long delay) {
        this();
        if (delay < 0) {
            throw new IllegalArgumentException("delay time illegal!");
        }
        this.delaySwitch = true;
        this.delayMs = delay;
    }

    public AsyncServerUserProcessor(long delay, int core, int max, int keepaliveSeconds, int workQueue) {
        this(delay);
        this.executor = new ThreadPoolExecutor(core, max, keepaliveSeconds, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(workQueue), new NamedThreadFactory("Request-process-pool"));
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
            remoteAddr = bizCtx.getRemoteAddress();
            latch.countDown();
            log.warn("Server User processor say, remote address is [" + remoteAddr + "].");
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
        return new int[]{this.onewayTimes.get(), this.syncTimes.get(), this.futureTimes.get(),
                this.callbackTimes.get()}[type.ordinal()];
    }

    public String getRemoteAddr() throws InterruptedException {
        latch.await(100, TimeUnit.MILLISECONDS);
        return this.remoteAddr;
    }
}