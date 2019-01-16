/**
 * FileName: AsyncClientUserProcessor
 * Author:   HuangTaiHong
 * Date:     2019/1/15 16:16
 * Description: a demo aysnc user processor for RPC client.
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
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 〈一句话功能简述〉<br> 
 * 〈a demo aysnc user processor for RPC client.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/15
 * @since 1.0.0
 */
@Slf4j
public class AsyncClientUserProcessor extends AsyncUserProcessor<RequestBody> {
    /** delay milliseconds */
    private long                delayMs;

    /** whether delay or not */
    private boolean             delaySwitch;

    /** whether exception */
    private boolean             isException;

    /** whether null */
    private boolean             isNull;

    /** executor */
    private ThreadPoolExecutor executor;

    private ThreadPoolExecutor  asyncExecutor;

    private AtomicInteger invokeTimes = new AtomicInteger();

    public AsyncClientUserProcessor() {
        this.delaySwitch = false;
        this.isException = false;
        this.isNull = false;
        this.delayMs = 0;
        this.executor = new ThreadPoolExecutor(1, 3, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(4), new NamedThreadFactory("Request-process-pool"));
        this.asyncExecutor = new ThreadPoolExecutor(1, 3, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(4), new NamedThreadFactory(
                "Another-aysnc-process-pool"));
    }

    public AsyncClientUserProcessor(boolean isException, boolean isNull) {
        this();
        this.isException = isException;
        this.isNull = isNull;
    }

    public AsyncClientUserProcessor(long delay) {
        this();
        if (delay < 0) {
            throw new IllegalArgumentException("delay time illegal!");
        }
        this.delaySwitch = true;
        this.delayMs = delay;
    }

    public AsyncClientUserProcessor(long delay, int core, int max, int keepaliveSeconds,
                                    int workQueue) {
        this(delay);
        this.executor = new ThreadPoolExecutor(core, max, keepaliveSeconds, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(workQueue), new NamedThreadFactory(
                "Request-process-pool"));
    }

    @Override
    public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, RequestBody request) {
        this.asyncExecutor.execute(new InnerTask(asyncCtx, request));
    }

    class InnerTask implements Runnable {
        private AsyncContext asyncCtx;
        private RequestBody  request;

        public InnerTask(AsyncContext asyncCtx, RequestBody request) {
            this.asyncCtx = asyncCtx;
            this.request = request;
        }

        public void run() {
            log.warn("Request received:" + request);
            Assert.assertEquals(RequestBody.class, request.getClass());
            invokeTimes.incrementAndGet();
            if (isException) {
                this.asyncCtx.sendResponse(new IllegalArgumentException("Exception test"));
            } else if (isNull) {
                this.asyncCtx.sendResponse(null);
            } else {
                if (!delaySwitch) {
                    this.asyncCtx.sendResponse(RequestBody.DEFAULT_CLIENT_RETURN_STR);
                    return;
                }
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.asyncCtx.sendResponse(RequestBody.DEFAULT_CLIENT_RETURN_STR);
            }
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
}