/**
 * FileName: SpecificClientUserProcessor
 * Author:   HuangTaiHong
 * Date:     2019/1/16 17:20
 * Description: a demo specific user processor for rpc client.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.userprocessor.processinio;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import roberto.group.process.netty.practice.command.processor.custom.impl.SyncUserProcessor;
import roberto.group.process.netty.practice.common.RequestBody;
import roberto.group.process.netty.practice.remote.biz.BizContext;
import roberto.group.process.netty.practice.remote.invoke.context.InvokeContext;
import roberto.group.process.netty.practice.thread.NamedThreadFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 〈一句话功能简述〉<br> 
 * 〈a demo specific user processor for rpc client.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/16
 * @since 1.0.0
 */
@Slf4j
public class SpecificClientUserProcessor extends SyncUserProcessor<RequestBody> {
    /** delay milliseconds */
    private long delayMs;

    /** whether delay or not */
    private boolean delaySwitch;

    private ThreadPoolExecutor executor;

    private AtomicInteger invokeTimes = new AtomicInteger();

    public SpecificClientUserProcessor() {
        this.delayMs = 0;
        this.delaySwitch = false;
        this.executor = new ThreadPoolExecutor(1, 3, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(4), new NamedThreadFactory("rpc-common-executor"));
    }

    public SpecificClientUserProcessor(long delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("delay time illegal!");
        }
        this.delayMs = delay;
        this.delaySwitch = true;
        this.executor = new ThreadPoolExecutor(1, 3, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(4), new NamedThreadFactory("rpc-common-executor"));
    }

    @Override
    public Object handleRequest(BizContext bizCtx, RequestBody request) throws Exception {
        String threadName = Thread.currentThread().getName();
        Assert.assertTrue(threadName.contains("bolt-netty-client-worker"));

        log.warn("Request received:" + request);
        Assert.assertEquals(RequestBody.class, request.getClass());

        long waittime = bizCtx.getInvokeContext().get(InvokeContext.BOLT_PROCESS_WAIT_TIME);
        log.warn("Client User processor process wait time [" + waittime + "].");

        invokeTimes.incrementAndGet();
        if (!delaySwitch) {
            return RequestBody.DEFAULT_CLIENT_RETURN_STR;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return RequestBody.DEFAULT_CLIENT_RETURN_STR;
    }

    @Override
    public String interest() {
        return RequestBody.class.getName();
    }

    @Override
    public boolean processInIOThread() {
        return true;
    }

    public int getInvokeTimes() {
        return this.invokeTimes.get();
    }
}