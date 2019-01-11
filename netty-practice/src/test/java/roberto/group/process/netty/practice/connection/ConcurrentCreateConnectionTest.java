/**
 * FileName: ConcurrentCreateConnectionTest
 * Author:   HuangTaiHong
 * Date:     2019/1/11 11:01
 * Description: Concurrent create connection test.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.connection;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import roberto.group.process.netty.practice.common.CONNECTEventProcessor;
import roberto.group.process.netty.practice.connection.enums.ConnectionEventTypeEnum;
import roberto.group.process.netty.practice.entrance.client.RGPDefaultRemoteClient;
import roberto.group.process.netty.practice.entrance.server.impl.RGPDefaultRemoteServer;
import roberto.group.process.netty.practice.exception.RemotingException;
import roberto.group.process.netty.practice.remote.help.impl.RPCAddressParser;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Concurrent create connection test.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/11
 * @since 1.0.0
 */
@Slf4j
public class ConcurrentCreateConnectionTest {
    private int port = 1111;
    private String ip = "127.0.0.1";
    private RGPDefaultRemoteClient client;
    private RGPDefaultRemoteServer server;
    CONNECTEventProcessor serverConnectProcessor = new CONNECTEventProcessor();

    @Before
    public void init() {
        (client = new RGPDefaultRemoteClient()).init();
        (server = new RGPDefaultRemoteServer(port)).start();
        server.addConnectionEventProcessor(ConnectionEventTypeEnum.CONNECT, serverConnectProcessor);
    }

    @After
    public void stop() {
        try {
            server.stop();
            Thread.sleep(100);
        } catch (InterruptedException e) {
            log.error("Stop server failed!", e);
        }
    }

    @Test
    public void testGetAndCheckConnection() throws InterruptedException {
        final int connectionNumber = 1;
        final boolean connectionWarmup = false;
        final ConnectionURL connectionURL = new ConnectionURL(ip, port);
        for (int i = 0; i < 10; ++i) {
            MyThread thread = new MyThread(connectionNumber, connectionWarmup, connectionURL);
            new Thread(thread).start();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error("", e);
        }
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
    }

    @Test
    public void testGetAndCheckConnectionMulti() throws InterruptedException {
        final int connectionNumber = 10;
        final boolean connectionWarmup = true;
        final ConnectionURL connectionURL = new ConnectionURL(ip, port);
        for (int i = 0; i < 10; ++i) {
            // warmup in one thread, the other threads will try lock failed.
            MyThread thread = new MyThread(connectionNumber, connectionWarmup, connectionURL);
            new Thread(thread).start();
        }

        Thread.sleep(3000);
        Assert.assertEquals(10, serverConnectProcessor.getConnectTimes());
    }

    @AllArgsConstructor
    class MyThread implements Runnable {
        private int connectionNumber;
        private boolean connectionWarmup;
        private ConnectionURL connectionURL;
        private final RPCAddressParser addressParser = new RPCAddressParser();

        @Override
        public void run() {
            try {
                this.addressParser.initUrlArgs(connectionURL);
                connectionURL.setConnectionNumber(connectionNumber);
                connectionURL.setConnectionWarmup(connectionWarmup);
                Connection connection = client.getConnection(connectionURL, 3000);
                Assert.assertNotNull(connection);
                Assert.assertTrue(connection.isFine());
            } catch (RemotingException e) {
                log.error("error!", e);
                Assert.assertTrue(false);
            } catch (Exception e) {
                log.error("error!", e);
                Assert.assertTrue(false);
            }
        }
    }
}