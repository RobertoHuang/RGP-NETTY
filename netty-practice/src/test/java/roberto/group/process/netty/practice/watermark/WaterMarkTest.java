/**
 * FileName: WaterMarkTest
 * Author:   HuangTaiHong
 * Date:     2019/1/16 18:50
 * Description: water mark normal test, set a large enough buffer mark, and not trigger write over flow.
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
import roberto.group.process.netty.practice.configuration.support.ConfigsSupport;
import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.connection.enums.ConnectionEventTypeEnum;
import roberto.group.process.netty.practice.entrance.client.RGPDefaultRemoteClient;
import roberto.group.process.netty.practice.entrance.server.impl.RGPDefaultRemoteServer;
import roberto.group.process.netty.practice.exception.RemotingException;
import roberto.group.process.netty.practice.utils.PortScanner;

/**
 * 〈一句话功能简述〉<br> 
 * 〈water mark normal test, set a large enough buffer mark, and not trigger write over flow.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/16
 * @since 1.0.0
 */
@Slf4j
public class WaterMarkTest {
    private int invokeTimes = 50;

    private String ip = "127.0.0.1";
    private int port = PortScanner.select();
    private String address = "127.0.0.1:" + port;

    private RGPDefaultRemoteClient client;
    private RGPDefaultRemoteServer server;

    private CONNECTEventProcessor clientConnectProcessor = new CONNECTEventProcessor();
    private CONNECTEventProcessor serverConnectProcessor = new CONNECTEventProcessor();

    private DISCONNECTEventProcessor clientDisConnectProcessor = new DISCONNECTEventProcessor();
    private DISCONNECTEventProcessor serverDisConnectProcessor = new DISCONNECTEventProcessor();

    private SimpleClientUserProcessor clientUserProcessor = new SimpleClientUserProcessor(0, 100, 100, 60, 100);
    private SimpleServerUserProcessor serverUserProcessor = new SimpleServerUserProcessor(0, 100, 100, 60, 100);

    @Before
    public void init() {
        System.setProperty(ConfigsSupport.NETTY_BUFFER_HIGH_WATERMARK, Integer.toString(128 * 1024));
        System.setProperty(ConfigsSupport.NETTY_BUFFER_LOW_WATERMARK, Integer.toString(32 * 1024));

        server = new RGPDefaultRemoteServer(port, true);
        server.addConnectionEventProcessor(ConnectionEventTypeEnum.CONNECT, serverConnectProcessor);
        server.addConnectionEventProcessor(ConnectionEventTypeEnum.CLOSE, serverDisConnectProcessor);
        server.registerUserProcessor(serverUserProcessor);
        server.start();

        client = new RGPDefaultRemoteClient();
        client.addConnectionEventProcessor(ConnectionEventTypeEnum.CONNECT, clientConnectProcessor);
        client.addConnectionEventProcessor(ConnectionEventTypeEnum.CLOSE, clientDisConnectProcessor);
        client.registerUserProcessor(clientUserProcessor);
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
    public void testSync() throws InterruptedException {
        final RequestBody requestBody = new RequestBody(1, 1024);
        for (int i = 0; i < invokeTimes; i++) {
            new Thread(() -> {
                String result = null;
                try {
                    for (int i1 = 0; i1 < invokeTimes; i1++) {
                        result = (String) client.invokeSync(address, requestBody, 3000);
                    }
                } catch (RemotingException e) {
                    String errorMessage = "RemotingException caught in sync!";
                    log.error(errorMessage, e);
                    Assert.fail(errorMessage);
                } catch (InterruptedException e) {
                    String errorMessage = "InterruptedException caught in sync!";
                    log.error(errorMessage, e);
                    Assert.fail(errorMessage);
                }
                log.warn("Result received in sync: " + result);
                Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR, result);
            }).start();
        }

        Thread.sleep(5000);
        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes * invokeTimes, serverUserProcessor.getInvokeTimes());
    }

    @Test
    public void testServerSyncUsingConnection() throws Exception {
        Connection clientConnection = client.createStandaloneConnection(ip, port, 1000);
        RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_CLIENT_STR);
        String serverReturn = (String) client.invokeSync(clientConnection, requestBody, 1000);
        Assert.assertEquals(serverReturn, RequestBody.DEFAULT_SERVER_RETURN_STR);
        for (int i = 0; i < invokeTimes; i++) {
            new Thread(() -> {
                try {
                    String remoteAddress = serverUserProcessor.getRemoteAddress();
                    Assert.assertNotNull(remoteAddress);
                    RequestBody requestBody2 = new RequestBody(1, 1024);
                    for (int i1 = 0; i1 < invokeTimes; i1++) {
                        String clientReturn = (String) server.invokeSync(remoteAddress, requestBody2, 1000);
                        Assert.assertEquals(clientReturn, RequestBody.DEFAULT_CLIENT_RETURN_STR);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (RemotingException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        Thread.sleep(5000);
        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes * invokeTimes, clientUserProcessor.getInvokeTimes());
    }
}