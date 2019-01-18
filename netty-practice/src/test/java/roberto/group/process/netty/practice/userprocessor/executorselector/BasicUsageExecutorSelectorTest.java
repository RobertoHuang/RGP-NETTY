/**
 * FileName: BasicUsageExecutorSelectorTest
 * Author:   HuangTaiHong
 * Date:     2019/1/16 15:42
 * Description: basic usage test.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.userprocessor.executorselector;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import roberto.group.process.netty.practice.command.processor.custom.UserProcessor;
import roberto.group.process.netty.practice.common.CONNECTEventProcessor;
import roberto.group.process.netty.practice.common.DISCONNECTEventProcessor;
import roberto.group.process.netty.practice.common.RequestBody;
import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.connection.enums.ConnectionEventTypeEnum;
import roberto.group.process.netty.practice.entrance.client.RGPDefaultRemoteClient;
import roberto.group.process.netty.practice.entrance.server.impl.RGPDefaultRemoteServer;
import roberto.group.process.netty.practice.exception.RemotingException;
import roberto.group.process.netty.practice.remote.invoke.callback.InvokeCallback;
import roberto.group.process.netty.practice.remote.remote.RPCResponseFuture;
import roberto.group.process.netty.practice.serialize.custom.manager.CustomSerializerManager;
import static roberto.group.process.netty.practice.userprocessor.executorselector.DefaultExecutorSelector.EXECUTOR0;
import static roberto.group.process.netty.practice.userprocessor.executorselector.DefaultExecutorSelector.EXECUTOR1;
import roberto.group.process.netty.practice.utils.PortScanner;
import roberto.group.process.netty.practice.utils.RemotingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 〈一句话功能简述〉<br> 
 * 〈basic usage test.〉
 *
 *  basic usage of RPC client and RPC server using executor selector.
 *
 * @author HuangTaiHong
 * @create 2019/1/16
 * @since 1.0.0
 */
@Slf4j
public class BasicUsageExecutorSelectorTest {
    private int invokeTimes = 5;

    private String ip = "127.0.0.1";
    private int port = PortScanner.select();
    private String address = "127.0.0.1:" + port;

    private RGPDefaultRemoteServer server;
    private RGPDefaultRemoteClient client;

    private CONNECTEventProcessor clientConnectProcessor = new CONNECTEventProcessor();
    private CONNECTEventProcessor serverConnectProcessor = new CONNECTEventProcessor();

    private DISCONNECTEventProcessor clientDisConnectProcessor = new DISCONNECTEventProcessor();
    private DISCONNECTEventProcessor serverDisConnectProcessor = new DISCONNECTEventProcessor();

    private SpecificServerUserProcessor serverUserProcessor = new SpecificServerUserProcessor();
    private SpecificClientUserProcessor clientUserProcessor = new SpecificClientUserProcessor();

    private UserProcessor.ExecutorSelector selector0 = new DefaultExecutorSelector(EXECUTOR0);
    private UserProcessor.ExecutorSelector selector1 = new DefaultExecutorSelector(EXECUTOR1);

    @BeforeClass
    public static void classInit() {
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), new CustomHeaderSerializer());
    }

    @Before
    public void init() {
        server = new RGPDefaultRemoteServer(port, true);
        server.start();
        server.addConnectionEventProcessor(ConnectionEventTypeEnum.CONNECT, serverConnectProcessor);
        server.addConnectionEventProcessor(ConnectionEventTypeEnum.CLOSE, serverDisConnectProcessor);
        serverUserProcessor.setExecutorSelector(this.selector0);
        server.registerUserProcessor(serverUserProcessor);

        client = new RGPDefaultRemoteClient();
        client.addConnectionEventProcessor(ConnectionEventTypeEnum.CONNECT, clientConnectProcessor);
        client.addConnectionEventProcessor(ConnectionEventTypeEnum.CLOSE, clientDisConnectProcessor);
        clientUserProcessor.setExecutorSelector(this.selector1);
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
    @SuppressWarnings("all")
    public void testOneway() throws InterruptedException {
        RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_ONEWAY_STR);
        for (int i = 0; i < invokeTimes; i++) {
            try {
                client.oneway(address, requestBody);
                Thread.sleep(100);
            } catch (RemotingException e) {
                String errorMessage = "RemotingException caught in oneway!";
                log.error(errorMessage, e);
                Assert.fail(errorMessage);
            }
        }

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }

    @Test
    @SuppressWarnings("all")
    public void testSync() throws InterruptedException {
        RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_SYNC_STR);
        for (int i = 0; i < invokeTimes; i++) {
            try {
                String result = (String) client.invokeSync(address, requestBody, 3000);
                log.warn("Result received in sync: " + result);
                Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR, result);
            } catch (RemotingException e) {
                String errorMessage = "RemotingException caught in sync!";
                log.error(errorMessage, e);
                Assert.fail(errorMessage);
            } catch (InterruptedException e) {
                String errorMessage = "InterruptedException caught in sync!";
                log.error(errorMessage, e);
                Assert.fail(errorMessage);
            }
        }

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }

    @Test
    @SuppressWarnings("all")
    public void testFuture() throws InterruptedException {
        RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_FUTURE_STR);
        for (int i = 0; i < invokeTimes; i++) {
            try {
                RPCResponseFuture future = client.invokeWithFuture(address, requestBody, 3000);
                String result = (String) future.get();
                Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR, result);
            } catch (RemotingException e) {
                String errorMessage = "RemotingException caught in future!";
                log.error(errorMessage, e);
                Assert.fail(errorMessage);
            } catch (InterruptedException e) {
                String errorMessage = "InterruptedException caught in future!";
                log.error(errorMessage, e);
                Assert.fail(errorMessage);
            }
        }

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }

    @Test
    @SuppressWarnings("all")
    public void testCallback() throws InterruptedException {
        RequestBody requestBody = new RequestBody(1, "hello world callback");
        final List<String> resultList = new ArrayList<String>(1);
        for (int i = 0; i < invokeTimes; i++) {
            final CountDownLatch latch = new CountDownLatch(1);
            try {
                client.invokeWithCallback(address, requestBody, new InvokeCallback() {
                    Executor executor = Executors.newCachedThreadPool();

                    @Override
                    public void onResponse(Object result) {
                        log.warn("Result received in callback: " + result);
                        resultList.add((String) result);
                        latch.countDown();
                    }

                    @Override
                    public void onException(Throwable e) {
                        log.error("Process exception in callback.", e);
                        latch.countDown();
                    }

                    @Override
                    public Executor getExecutor() {
                        return executor;
                    }
                }, 1000);
            } catch (RemotingException e) {
                latch.countDown();
                String errorMessage = "RemotingException caught in callback!";
                log.error(errorMessage, e);
                Assert.fail(errorMessage);
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                String errorMessage = "InterruptedException caught in callback!";
                log.error(errorMessage, e);
                Assert.fail(errorMessage);
            }
            if (resultList.size() == 0) {
                Assert.fail("No result! Maybe exception caught!");
            }
            Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR, resultList.get(0));
            resultList.clear();
        }
        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }

    @Test
    @SuppressWarnings("all")
    public void testServerSyncUsingConnection() throws Exception {
        Connection clientConn = client.createStandaloneConnection(ip, port, 1000);
        for (int i = 0; i < invokeTimes; i++) {
            RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_CLIENT_STR);
            String serverres = (String) client.invokeSync(clientConn, requestBody, 1000);
            Assert.assertEquals(serverres, RequestBody.DEFAULT_SERVER_RETURN_STR);

            Assert.assertNotNull(serverConnectProcessor.getConnection());
            Connection serverConn = serverConnectProcessor.getConnection();
            RequestBody req = new RequestBody(1, RequestBody.DEFAULT_SERVER_STR);
            String clientres = (String) server.invokeSync(serverConn, req, 1000);
            Assert.assertEquals(clientres, RequestBody.DEFAULT_CLIENT_RETURN_STR);
        }

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }

    @Test
    @SuppressWarnings("all")
    public void testServerSyncUsingAddress() throws Exception {
        Connection clientConn = client.createStandaloneConnection(ip, port, 1000);
        String local = RemotingUtil.parseLocalAddress(clientConn.getChannel());
        String remote = RemotingUtil.parseRemoteAddress(clientConn.getChannel());
        log.warn("Client say local:" + local);
        log.warn("Client say remote:" + remote);

        for (int i = 0; i < invokeTimes; i++) {
            RequestBody req1 = new RequestBody(1, RequestBody.DEFAULT_CLIENT_STR);
            String serverres = (String) client.invokeSync(clientConn, req1, 1000);
            Assert.assertEquals(serverres, RequestBody.DEFAULT_SERVER_RETURN_STR);

            Assert.assertNotNull(serverConnectProcessor.getConnection());
            // only when client invoked, the remote address can be get by UserProcessor. otherwise, please use ConnectionEventProcessor
            String remoteAddr = serverUserProcessor.getRemoteAddress();
            RequestBody req = new RequestBody(1, RequestBody.DEFAULT_SERVER_STR);
            String clientres = (String) server.invokeSync(remoteAddr, req, 1000);
            Assert.assertEquals(clientres, RequestBody.DEFAULT_CLIENT_RETURN_STR);
        }

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }
}