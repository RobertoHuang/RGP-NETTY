/**
 * FileName: SimpleServerMultiInterestUserProcessor
 * Author:   HuangTaiHong
 * Date:     2019/1/16 16:38
 * Description: simple server multi interest user processor.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.userprocessor.multiinterestprocessor;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import roberto.group.process.netty.practice.command.processor.custom.impl.SyncMutiInterestUserProcessor;
import roberto.group.process.netty.practice.common.RequestBody;
import roberto.group.process.netty.practice.remote.biz.BizContext;
import roberto.group.process.netty.practice.remote.invoke.context.InvokeContext;
import roberto.group.process.netty.practice.thread.NamedThreadFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 〈一句话功能简述〉<br> 
 * 〈simple server multi interest user processor.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/16
 * @since 1.0.0
 */
@Slf4j
public class SimpleServerMultiInterestUserProcessor extends SyncMutiInterestUserProcessor<MultiInterestBaseRequestBody> {
    /** delay milliseconds */
    private long delayMs;

    /** whether delay or not */
    private boolean delaySwitch;

    /** executor */
    private ThreadPoolExecutor executor;

    /** default is true */
    @Getter
    @Setter
    private boolean timeoutDiscard = true;

    private AtomicInteger c1invokeTimes = new AtomicInteger();
    private AtomicInteger c1onewayTimes = new AtomicInteger();
    private AtomicInteger c1syncTimes = new AtomicInteger();
    private AtomicInteger c1futureTimes = new AtomicInteger();
    private AtomicInteger c1callbackTimes = new AtomicInteger();

    private AtomicInteger c2invokeTimes = new AtomicInteger();
    private AtomicInteger c2onewayTimes = new AtomicInteger();
    private AtomicInteger c2syncTimes = new AtomicInteger();
    private AtomicInteger c2futureTimes = new AtomicInteger();
    private AtomicInteger c2callbackTimes = new AtomicInteger();

    private String remoteAddress;
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public SimpleServerMultiInterestUserProcessor() {
        this.delayMs = 0;
        this.delaySwitch = false;
        this.executor = new ThreadPoolExecutor(1, 3, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(4), new NamedThreadFactory("request-process-pool"));
    }

    public SimpleServerMultiInterestUserProcessor(long delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("delay time illegal!");
        }
        this.delayMs = delay;
        this.delaySwitch = true;
        this.executor = new ThreadPoolExecutor(1, 3, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(4), new NamedThreadFactory("request-process-pool"));
    }

    public SimpleServerMultiInterestUserProcessor(long delay, int core, int max, int keepaliveSeconds, int workQueue) {
        if (delay < 0) {
            throw new IllegalArgumentException("delay time illegal!");
        }
        this.delayMs = delay;
        this.delaySwitch = true;
        this.executor = new ThreadPoolExecutor(core, max, keepaliveSeconds, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(workQueue), new NamedThreadFactory("request-process-pool"));
    }

    @Override
    public Object handleRequest(BizContext bizCtx, MultiInterestBaseRequestBody request) throws Exception {
        log.warn("Request received:" + request + ", timeout:" + bizCtx.getClientTimeout() + ", arriveTimestamp:" + bizCtx.getArriveTimestamp());
        if (bizCtx.isRequestTimeout()) {
            String errorMessage = "Stop process in server biz thread, already timeout!";
            log.warn(errorMessage);
            throw new Exception(errorMessage);
        }

        this.remoteAddress = bizCtx.getRemoteAddress();
        //test biz context get connection
        Assert.assertNotNull(bizCtx.getConnection());
        Assert.assertTrue(bizCtx.getConnection().isFine());
        Long waittime = bizCtx.getInvokeContext().get(InvokeContext.BOLT_PROCESS_WAIT_TIME);
        Assert.assertNotNull(waittime);

        if (log.isInfoEnabled()) {
            log.info("Server User processor process wait time {}", waittime);
        }

        countDownLatch.countDown();
        log.warn("Server User processor say, remote address is [" + this.remoteAddress + "].");
        if (request instanceof RequestBodyFromClient1) {
            Assert.assertEquals(RequestBodyFromClient1.class, request.getClass());
            return handleRequest(bizCtx, (RequestBodyFromClient1) request);
        } else if (request instanceof RequestBodyFromClient2) {
            Assert.assertEquals(RequestBodyFromClient2.class, request.getClass());
            return handleRequest(bizCtx, (RequestBodyFromClient2) request);
        } else {
            throw new Exception("RequestBody does not belong to defined interests !");
        }
    }

    private Object handleRequest(BizContext bizCtx, RequestBodyFromClient1 request) {
        Long waittime = bizCtx.getInvokeContext().get(InvokeContext.BOLT_PROCESS_WAIT_TIME);
        Assert.assertNotNull(waittime);
        if (log.isInfoEnabled()) {
            log.info("Client User processor process wait time {}", waittime);
        }

        processTimes(request);
        if (!delaySwitch) {
            return RequestBodyFromClient1.DEFAULT_SERVER_RETURN_STR;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return RequestBodyFromClient1.DEFAULT_SERVER_RETURN_STR;
    }

    private Object handleRequest(BizContext bizCtx, RequestBodyFromClient2 request) {
        Long waittime = bizCtx.getInvokeContext().get(InvokeContext.BOLT_PROCESS_WAIT_TIME);
        Assert.assertNotNull(waittime);
        if (log.isInfoEnabled()) {
            log.info("Client User processor process wait time {}", waittime);
        }

        processTimes(request);
        if (!delaySwitch) {
            return RequestBodyFromClient2.DEFAULT_SERVER_RETURN_STR;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return RequestBodyFromClient2.DEFAULT_SERVER_RETURN_STR;
    }

    @Override
    public List<String> multiInterest() {
        List<String> list = new ArrayList<String>();
        list.add(RequestBodyFromClient1.class.getName());
        list.add(RequestBodyFromClient2.class.getName());
        return list;
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }

    @Override
    public boolean timeoutDiscard() {
        return this.timeoutDiscard;
    }

    public int getInvokeTimesC1() {
        return c1invokeTimes.get();
    }

    public int getInvokeTimesC2() {
        return c2invokeTimes.get();
    }

    public int getInvokeTimesEachCallTypeC1(RequestBody.InvokeType type) {
        return new int[]{this.c1onewayTimes.get(), this.c1syncTimes.get(), this.c1futureTimes.get(), this.c1callbackTimes.get()}[type.ordinal()];
    }

    public int getInvokeTimesEachCallTypeC2(RequestBody.InvokeType type) {
        return new int[]{this.c2onewayTimes.get(), this.c2syncTimes.get(), this.c2futureTimes.get(), this.c2callbackTimes.get()}[type.ordinal()];
    }

    public String getRemoteAddress() throws InterruptedException {
        countDownLatch.await(100, TimeUnit.MILLISECONDS);
        return this.remoteAddress;
    }

    private void processTimes(RequestBodyFromClient1 requestBodyFromClient1) {
        this.c1invokeTimes.incrementAndGet();
        if (requestBodyFromClient1.getMsg().equals(RequestBodyFromClient1.DEFAULT_ONEWAY_STR)) {
            this.c1onewayTimes.incrementAndGet();
        } else if (requestBodyFromClient1.getMsg().equals(RequestBodyFromClient1.DEFAULT_SYNC_STR)) {
            this.c1syncTimes.incrementAndGet();
        } else if (requestBodyFromClient1.getMsg().equals(RequestBodyFromClient1.DEFAULT_FUTURE_STR)) {
            this.c1futureTimes.incrementAndGet();
        } else if (requestBodyFromClient1.getMsg().equals(RequestBodyFromClient1.DEFAULT_CALLBACK_STR)) {
            this.c1callbackTimes.incrementAndGet();
        }
    }

    private void processTimes(RequestBodyFromClient2 requestBodyFromClient2) {
        this.c2invokeTimes.incrementAndGet();
        if (requestBodyFromClient2.getMsg().equals(RequestBodyFromClient2.DEFAULT_ONEWAY_STR)) {
            this.c2onewayTimes.incrementAndGet();
        } else if (requestBodyFromClient2.getMsg().equals(RequestBodyFromClient2.DEFAULT_SYNC_STR)) {
            this.c2syncTimes.incrementAndGet();
        } else if (requestBodyFromClient2.getMsg().equals(RequestBodyFromClient2.DEFAULT_FUTURE_STR)) {
            this.c2futureTimes.incrementAndGet();
        } else if (requestBodyFromClient2.getMsg().equals(RequestBodyFromClient2.DEFAULT_CALLBACK_STR)) {
            this.c2callbackTimes.incrementAndGet();
        }
    }
}