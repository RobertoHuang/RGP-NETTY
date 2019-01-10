/**
 * FileName: CONNECTEventProcessor
 * Author:   HuangTaiHong
 * Date:     2019/1/10 11:39
 * Description: ConnectionEventProcessor for ConnectionEventType.CONNECT.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.common;

import org.junit.Assert;
import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.connection.ConnectionEventProcessor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 〈一句话功能简述〉<br> 
 * 〈ConnectionEventProcessor for ConnectionEventType.CONNECT.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/10
 * @since 1.0.0
 */
public class CONNECTEventProcessor implements ConnectionEventProcessor {
    private Connection connection;
    private String remoteAddress;
    private AtomicBoolean connected = new AtomicBoolean();
    private AtomicInteger connectTimes = new AtomicInteger();
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    @Override
    public void onEvent(String remoteAddress, Connection connection) {
        Assert.assertNotNull(remoteAddress);
        doCheckConnection(connection);
        this.remoteAddress = remoteAddress;
        this.connection = connection;
        connected.set(true);
        connectTimes.incrementAndGet();
        countDownLatch.countDown();
    }

    /**
     * do check connection
     * @param conn
     */
    private void doCheckConnection(Connection conn) {
        Assert.assertNotNull(conn);
        Assert.assertNotNull(conn.getPoolKeys());
        Assert.assertTrue(conn.getPoolKeys().size() > 0);
        Assert.assertNotNull(conn.getChannel());
        Assert.assertNotNull(conn.getConnectionURL());
        Assert.assertNotNull(conn.getChannel().attr(Connection.CONNECTION).get());
    }

    public void reset() {
        this.connectTimes.set(0);
        this.connected.set(false);
        this.connection = null;
    }

    public boolean isConnected() throws InterruptedException {
        countDownLatch.await();
        return this.connected.get();
    }

    public int getConnectTimes() throws InterruptedException {
        countDownLatch.await();
        return this.connectTimes.get();
    }

    public Connection getConnection() throws InterruptedException {
        countDownLatch.await();
        return this.connection;
    }

    public String getRemoteAddress() throws InterruptedException {
        countDownLatch.await();
        return this.remoteAddress;
    }
}