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
import roberto.group.process.netty.practice.context.AsyncContext;
import roberto.group.process.netty.practice.command.processor.custom.AsyncUserProcessor;
import roberto.group.process.netty.practice.context.BizContext;
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

    public AsyncClientUserProcessor() {
        this.isNull = false;
        this.isException = false;
        this.delayMs = 0;
        this.delaySwitch = false;
        this.executor = new ThreadPoolExecutor(1, 3, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(4), new NamedThreadFactory("request-process-pool"));
        this.asyncExecutor = new ThreadPoolExecutor(1, 3, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(4), new NamedThreadFactory("another-aysnc-process-pool"));
    }

    public AsyncClientUserProcessor(long delay) {
        this();
        if (delay < 0) {
            throw new IllegalArgumentException("delay time illegal!");
        }
        this.delayMs = delay;
        this.delaySwitch = true;
    }


    public AsyncClientUserProcessor(boolean isException, boolean isNull) {
        this();
        this.isNull = isNull;
        this.isException = isException;
    }


    public AsyncClientUserProcessor(long delay, int core, int max, int keepaliveSeconds, int workQueue) {
        this(delay);
        this.executor = new ThreadPoolExecutor(core, max, keepaliveSeconds, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(workQueue), new NamedThreadFactory("request-process-pool"));
    }

    @Override
    public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, RequestBody request) {
        this.asyncExecutor.execute(new InnerTask(asyncCtx, request));
    }

    class InnerTask implements Runnable {
        private RequestBody request;
        private AsyncContext asyncCtx;

        public InnerTask(AsyncContext asyncCtx, RequestBody request) {
            this.request = request;
            this.asyncCtx = asyncCtx;
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