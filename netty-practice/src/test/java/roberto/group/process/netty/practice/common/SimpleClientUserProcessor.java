/**
 * FileName: SimpleClientUserProcessor
 * Author:   HuangTaiHong
 * Date:     2019/1/10 11:09
 * Description: a demo user processor for RPC client.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.common;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import roberto.group.process.netty.practice.command.processor.custom.impl.SyncUserProcessor;
import roberto.group.process.netty.practice.remote.biz.BizContext;
import roberto.group.process.netty.practice.remote.invoke.context.InvokeContext;
import roberto.group.process.netty.practice.thread.NamedThreadFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 〈一句话功能简述〉<br> 
 * 〈a demo user processor for RPC client.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/10
 * @since 1.0.0
 */
@Slf4j
public class SimpleClientUserProcessor extends SyncUserProcessor<RequestBody> {
    /** delay milliseconds */
    private long delayMs;

    /** whether delay or not */
    private boolean delaySwitch;

    /** executor */
    private ThreadPoolExecutor executor;

    @Getter
    @Setter
    /** default is true */
    private boolean timeoutDiscard = true;

    private AtomicInteger invokeTimes = new AtomicInteger();
    private AtomicInteger onewayTimes = new AtomicInteger();
    private AtomicInteger syncTimes = new AtomicInteger();
    private AtomicInteger futureTimes = new AtomicInteger();
    private AtomicInteger callbackTimes = new AtomicInteger();

    public SimpleClientUserProcessor() {
        this.delayMs = 0;
        this.delaySwitch = false;
        this.executor = new ThreadPoolExecutor(1, 3, 60, TimeUnit.SECONDS, new ArrayBlockingQueue(4), new NamedThreadFactory("request-process-pool"));
    }

    public SimpleClientUserProcessor(long delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("delay time illegal!");
        } else {
            this.delayMs = delay;
            this.delaySwitch = true;
            this.executor = new ThreadPoolExecutor(1, 3, 60, TimeUnit.SECONDS, new ArrayBlockingQueue(4), new NamedThreadFactory("request-process-pool"));
        }
    }

    public SimpleClientUserProcessor(long delay, int core, int max, int keepaliveSeconds, int workQueue) {
        if (delay < 0) {
            throw new IllegalArgumentException("delay time illegal!");
        } else {
            this.delayMs = delay;
            this.delaySwitch = true;
            this.executor = new ThreadPoolExecutor(core, max, keepaliveSeconds, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(workQueue), new NamedThreadFactory("request-process-pool"));
        }
    }

    @Override
    public Object handleRequest(BizContext bizContext, RequestBody request) throws Exception {
        log.warn("Request received:" + request);
        if (bizContext.isRequestTimeout()) {
            String errMsg = "Stop process in client biz thread, already timeout!";
            log.warn(errMsg);
            processTimes(request);
            throw new Exception(errMsg);
        }
        Assert.assertEquals(RequestBody.class, request.getClass());

        Long waittime = bizContext.getInvokeContext().get(InvokeContext.BOLT_PROCESS_WAIT_TIME);
        Assert.assertNotNull(waittime);
        log.info("Client User processor process wait time {}", waittime);
        processTimes(request);

        if (!delaySwitch) {
            return RequestBody.DEFAULT_CLIENT_RETURN_STR;
        } else {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return RequestBody.DEFAULT_CLIENT_RETURN_STR;
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

    @Override
    public boolean timeoutDiscard() {
        return this.timeoutDiscard;
    }

    public int getInvokeTimes() {
        return this.invokeTimes.get();
    }

    public int getInvokeTimesEachCallType(RequestBody.InvokeType type) {
        return new int[]{this.onewayTimes.get(), this.syncTimes.get(), this.futureTimes.get(), this.callbackTimes.get()}[type.ordinal()];
    }

    @SuppressWarnings("all")
    private void processTimes(RequestBody requestBody) {
        this.invokeTimes.incrementAndGet();
        if (requestBody.getMsg().equals(RequestBody.DEFAULT_ONEWAY_STR)) {
            this.onewayTimes.incrementAndGet();
        } else if (requestBody.getMsg().equals(RequestBody.DEFAULT_SYNC_STR)) {
            this.syncTimes.incrementAndGet();
        } else if (requestBody.getMsg().equals(RequestBody.DEFAULT_FUTURE_STR)) {
            this.futureTimes.incrementAndGet();
        } else if (requestBody.getMsg().equals(RequestBody.DEFAULT_CALLBACK_STR)) {
            this.callbackTimes.incrementAndGet();
        }
    }
}