/**
 * FileName: DISCONNECTEventProcessor
 * Author:   HuangTaiHong
 * Date:     2019/1/10 13:38
 * Description: ConnectionEventProcessor for ConnectionEventType.CLOSE.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.common;

import org.junit.Assert;
import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.connection.processor.ConnectionEventProcessor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 〈一句话功能简述〉<br>
 * 〈ConnectionEventProcessor for ConnectionEventType.CLOSE.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/10
 * @since 1.0.0
 */
public class DISCONNECTEventProcessor implements ConnectionEventProcessor {
    private AtomicBoolean disConnected = new AtomicBoolean();
    private AtomicInteger disConnectTimes = new AtomicInteger();


    @Override
    public void onEvent(String remoteAddress, Connection connection) {
        Assert.assertNotNull(connection);
        disConnected.set(true);
        disConnectTimes.incrementAndGet();
    }

    public void reset() {
        this.disConnectTimes.set(0);
        this.disConnected.set(false);
    }

    public boolean isDisConnected() {
        return this.disConnected.get();
    }

    public int getDisConnectTimes() {
        return this.disConnectTimes.get();
    }
}