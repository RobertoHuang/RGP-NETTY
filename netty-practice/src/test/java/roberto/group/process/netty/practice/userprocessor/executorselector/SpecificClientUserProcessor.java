/**
 * FileName: SpecificClientUserProcessor
 * Author:   HuangTaiHong
 * Date:     2019/1/16 15:45
 * Description: a demo specific user processor for RPC client.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.userprocessor.executorselector;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import roberto.group.process.netty.practice.command.processor.custom.SyncUserProcessor;
import roberto.group.process.netty.practice.common.RequestBody;
import roberto.group.process.netty.practice.remote.biz.BizContext;
import roberto.group.process.netty.practice.remote.invoke.context.InvokeContext;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 〈一句话功能简述〉<br> 
 * 〈a demo specific user processor for RPC client.〉
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

    private AtomicInteger invokeTimes = new AtomicInteger();

    public SpecificClientUserProcessor() {
        this.delayMs = 0;
        this.delaySwitch = false;
    }

    public SpecificClientUserProcessor(long delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("delay time illegal!");
        }
        this.delayMs = delay;
        this.delaySwitch = true;
    }

    @Override
    public Object handleRequest(BizContext bizCtx, RequestBody request) throws Exception {
        String threadName = Thread.currentThread().getName();
        Assert.assertTrue(threadName.contains("RPC-specific1-executor"));

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

    public int getInvokeTimes() {
        return this.invokeTimes.get();
    }
}