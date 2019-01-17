/**
 * FileName: WaterMarkUserPropertyExceptionTest
 * Author:   HuangTaiHong
 * Date:     2019/1/16 19:41
 * Description: water mark exception test, set a small buffer mark by system property, and trigger write over flow.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.watermark;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import roberto.group.process.netty.practice.common.CONNECTEventProcessor;
import roberto.group.process.netty.practice.common.DISCONNECTEventProcessor;
import roberto.group.process.netty.practice.common.RequestBody;
import roberto.group.process.netty.practice.common.SimpleClientUserProcessor;
import roberto.group.process.netty.practice.common.SimpleServerUserProcessor;
import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.connection.enums.ConnectionEventTypeEnum;
import roberto.group.process.netty.practice.entrance.client.RGPDefaultRemoteClient;
import roberto.group.process.netty.practice.entrance.server.impl.RGPDefaultRemoteServer;
import roberto.group.process.netty.practice.exception.RemotingException;
import roberto.group.process.netty.practice.utils.PortScanner;

import java.util.ArrayList;
import java.util.List;

/**
 * 〈一句话功能简述〉<br> 
 * 〈water mark exception test, set a small buffer mark by system property, and trigger write over flow.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/16
 * @since 1.0.0
 */
@Slf4j
public class WaterMarkUserPropertyExceptionTest {
    private int invokeTimes = 10;

    private String ip = "127.0.0.1";
    private int port = PortScanner.select();
    private String address = "127.0.0.1:" + port;

    private RGPDefaultRemoteServer server;
    private RGPDefaultRemoteClient client;

    private CONNECTEventProcessor clientConnectProcessor = new CONNECTEventProcessor();
    private CONNECTEventProcessor serverConnectProcessor = new CONNECTEventProcessor();

    private DISCONNECTEventProcessor clientDisConnectProcessor = new DISCONNECTEventProcessor();
    private DISCONNECTEventProcessor serverDisConnectProcessor = new DISCONNECTEventProcessor();

    private SimpleServerUserProcessor serverUserProcessor = new SimpleServerUserProcessor(0, 20, 20, 60, 100);
    private SimpleClientUserProcessor clientUserProcessor = new SimpleClientUserProcessor(0, 20, 20, 60, 100);

    @Before
    public void init() {
        server = new RGPDefaultRemoteServer(port, true);
        server.initWriteBufferWaterMark(1, 2);
        server.start();
        server.addConnectionEventProcessor(ConnectionEventTypeEnum.CONNECT, serverConnectProcessor);
        server.addConnectionEventProcessor(ConnectionEventTypeEnum.CLOSE, serverDisConnectProcessor);
        server.registerUserProcessor(serverUserProcessor);

        client = new RGPDefaultRemoteClient();
        client.addConnectionEventProcessor(ConnectionEventTypeEnum.CONNECT, clientConnectProcessor);
        client.addConnectionEventProcessor(ConnectionEventTypeEnum.CLOSE, clientDisConnectProcessor);
        client.registerUserProcessor(clientUserProcessor);
        client.initWriteBufferWaterMark(1, 2);
        client.init();
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
    @SuppressWarnings("all")
    public void testSync() throws InterruptedException {
        final RequestBody req = new RequestBody(1, 1024);
        final List<Boolean> overFlow = new ArrayList<Boolean>();
        for (int i = 0; i < invokeTimes; i++) {
            new Thread(() -> {
                String result = null;
                try {
                    for (int i1 = 0; i1 < invokeTimes; i1++) {
                        result = (String) client.invokeSync(address, req, 3000);
                    }
                } catch (RemotingException e) {
                    if (e.getMessage().contains("overflow")) {
                        log.error("overflow exception!");
                        overFlow.add(true);
                    }
                } catch (InterruptedException e) {
                    String errorMessage = "InterruptedException caught in sync!";
                    log.error(errorMessage, e);
                    Assert.fail(errorMessage);
                }
                log.warn("Result received in sync: " + result);
                Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR, result);
            }).start();
        }
        Thread.sleep(3000);
        if (overFlow.size() > 0 && overFlow.get(0)) {
            Assert.assertTrue(serverConnectProcessor.isConnected());
            Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
            Assert.assertTrue(invokeTimes * invokeTimes > serverUserProcessor.getInvokeTimes());
        } else {
            Assert.fail("Should not reach here");
        }
    }

    @Test
    @SuppressWarnings("all")
    public void testServerSyncUsingConnection() throws Exception {
        Connection clientConn = client.createStandaloneConnection(ip, port, 1000);
        RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_CLIENT_STR);
        String serverReturn = (String) client.invokeSync(clientConn, requestBody, 1000);
        Assert.assertEquals(serverReturn, RequestBody.DEFAULT_SERVER_RETURN_STR);

        final String remoteAddress = serverUserProcessor.getRemoteAddress();
        Assert.assertNotNull(remoteAddress);
        final List<Boolean> overFlow = new ArrayList<>();
        final RequestBody requestBody2 = new RequestBody(1, 1024);
        for (int i = 0; i < invokeTimes; i++) {
            new Thread(() -> {
                try {
                    for (int i1 = 0; i1 < invokeTimes; i1++) {
                        String clientres = (String) server.invokeSync(remoteAddress, requestBody2, 1000);
                        Assert.assertEquals(clientres, RequestBody.DEFAULT_CLIENT_RETURN_STR);
                    }
                } catch (RemotingException e) {
                    if (e.getMessage().contains("overflow")) {
                        log.error("overflow exception!");
                        overFlow.add(true);
                    }
                } catch (InterruptedException e) {
                    String errorMessage = "InterruptedException caught in sync!";
                    log.error(errorMessage, e);
                    Assert.fail(errorMessage);
                }
            }).start();
        }
        Thread.sleep(3000);
        if (overFlow.size() > 0 && overFlow.get(0)) {
            Assert.assertTrue(serverConnectProcessor.isConnected());
            Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
            Assert.assertTrue(invokeTimes * invokeTimes > clientUserProcessor.getInvokeTimes());
        } else {
            Assert.fail("Should not reach here");
        }
    }
}