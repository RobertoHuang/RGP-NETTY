/**
 * FileName: ServerHeartBeatTest
 * Author:   HuangTaiHong
 * Date:     2019/1/15 19:13
 * Description: Server heart beat test.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.heartbeat;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import roberto.group.process.netty.practice.common.CONNECTEventProcessor;
import roberto.group.process.netty.practice.common.DISCONNECTEventProcessor;
import roberto.group.process.netty.practice.common.SimpleClientUserProcessor;
import roberto.group.process.netty.practice.common.SimpleServerUserProcessor;
import roberto.group.process.netty.practice.configuration.support.ConfigsSupport;
import roberto.group.process.netty.practice.connection.enums.ConnectionEventTypeEnum;
import roberto.group.process.netty.practice.entrance.client.RGPDefaultRemoteClient;
import roberto.group.process.netty.practice.entrance.server.impl.RGPDefaultRemoteServer;
import roberto.group.process.netty.practice.exception.RemotingException;
import roberto.group.process.netty.practice.utils.PortScanner;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Server heart beat test.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/15
 * @since 1.0.0
 */
@Slf4j
public class ServerHeartBeatTest {
    private int port = PortScanner.select();
    private String addr = "127.0.0.1:" + port;

    private RGPDefaultRemoteServer server;
    private RGPDefaultRemoteClient client;

    private CONNECTEventProcessor clientConnectProcessor = new CONNECTEventProcessor();
    private CONNECTEventProcessor serverConnectProcessor = new CONNECTEventProcessor();

    private DISCONNECTEventProcessor clientDisConnectProcessor = new DISCONNECTEventProcessor();
    private DISCONNECTEventProcessor serverDisConnectProcessor = new DISCONNECTEventProcessor();

    private SimpleServerUserProcessor serverUserProcessor = new SimpleServerUserProcessor();
    private SimpleClientUserProcessor clientUserProcessor = new SimpleClientUserProcessor();

    @Before
    public void init() {
        System.setProperty(ConfigsSupport.TCP_CLIENT_IDLE, "1000");
        System.setProperty(ConfigsSupport.TCP_SERVER_IDLE, "100");
        System.setProperty(ConfigsSupport.TCP_IDLE_SWITCH, Boolean.toString(true));

        server = new RGPDefaultRemoteServer(port);
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
    public void testServerIdle() throws InterruptedException {
        try {
            client.createStandaloneConnection(addr, 1000);
        } catch (RemotingException e) {
            log.error("create standalone connection failure.", e);
        }
        Thread.sleep(500);
        Assert.assertEquals(1, clientConnectProcessor.getConnectTimes());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(1, clientDisConnectProcessor.getDisConnectTimes());
        Assert.assertEquals(1, serverDisConnectProcessor.getDisConnectTimes());
    }
}