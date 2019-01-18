/**
 * FileName: BasicUsageDemo
 * Author:   HuangTaiHong
 * Date:     2019/1/11 9:39
 * Description: basic usage demo.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.entrance;

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
import roberto.group.process.netty.practice.remote.invoke.callback.InvokeCallback;
import roberto.group.process.netty.practice.remote.remote.RPCResponseFuture;
import roberto.group.process.netty.practice.utils.PortScanner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 〈一句话功能简述〉<br>
 * 〈basic usage demo.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/11
 * @since 1.0.0
 */
@Slf4j
@SuppressWarnings("all")
public class BasicUsageDemo {
    private int invokeTimes = 5;

    private RGPDefaultRemoteServer server;
    private RGPDefaultRemoteClient client;

    private String ip = "127.0.0.1";
    private int port = PortScanner.select();
    private String address = "127.0.0.1:" + port;

    private SimpleServerUserProcessor serverUserProcessor = new SimpleServerUserProcessor();
    private SimpleClientUserProcessor clientUserProcessor = new SimpleClientUserProcessor();

    private CONNECTEventProcessor clientConnectProcessor = new CONNECTEventProcessor();
    private DISCONNECTEventProcessor clientDisConnectProcessor = new DISCONNECTEventProcessor();

    private CONNECTEventProcessor serverConnectProcessor = new CONNECTEventProcessor();
    private DISCONNECTEventProcessor serverDisConnectProcessor = new DISCONNECTEventProcessor();

    @Before
    public void init() {
        server = new RGPDefaultRemoteServer(port, true);
        server.registerUserProcessor(serverUserProcessor);
        server.addConnectionEventProcessor(ConnectionEventTypeEnum.CONNECT, serverConnectProcessor);
        server.addConnectionEventProcessor(ConnectionEventTypeEnum.CLOSE, serverDisConnectProcessor);
        server.start();

        client = new RGPDefaultRemoteClient();
        client.registerUserProcessor(clientUserProcessor);
        client.addConnectionEventProcessor(ConnectionEventTypeEnum.CONNECT, clientConnectProcessor);
        client.addConnectionEventProcessor(ConnectionEventTypeEnum.CLOSE, clientDisConnectProcessor);
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
    public void testOneway() throws InterruptedException {
        RequestBody requestBody = new RequestBody(2, RequestBody.DEFAULT_ONEWAY_STR);
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
    public void testSync() throws InterruptedException {
        RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_SYNC_STR);
        for (int i = 0; i < invokeTimes; i++) {
            try {
                String result = (String) client.invokeSync(address, requestBody, 3000);
                log.warn("Result received in sync: " + result);
                Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR, result);
            } catch (RemotingException e) {
                String errorMsg = "RemotingException caught in sync!";
                log.error(errorMsg, e);
                Assert.fail(errorMsg);
            } catch (InterruptedException e) {
                String errorMsg = "InterruptedException caught in sync!";
                log.error(errorMsg, e);
                Assert.fail(errorMsg);
            }
        }

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }

    @Test
    public void testFuture() throws InterruptedException {
        RequestBody requestBody = new RequestBody(2, RequestBody.DEFAULT_FUTURE_STR);
        for (int i = 0; i < invokeTimes; i++) {
            try {
                RPCResponseFuture future = client.invokeWithFuture(address, requestBody, 3000);
                String result = (String) future.get();
                Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR, result);
            } catch (RemotingException e) {
                String errorMsg = "RemotingException caught in future!";
                log.error(errorMsg, e);
                Assert.fail(errorMsg);
            } catch (InterruptedException e) {
                String errorMsg = "InterruptedException caught in future!";
                log.error(errorMsg, e);
                Assert.fail(errorMsg);
            }
        }

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }

    @Test
    public void testCallback() throws InterruptedException {
        RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_CALLBACK_STR);
        final List<String> resultList = new ArrayList(1);
        for (int i = 0; i < invokeTimes; i++) {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            try {
                client.invokeWithCallback(address, requestBody, new InvokeCallback() {
                    Executor executor = Executors.newCachedThreadPool();

                    @Override
                    public void onResponse(Object result) {
                        log.warn("Result received in callback: " + result);
                        resultList.add((String) result);
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onException(Throwable e) {
                        log.error("Process exception in callback.", e);
                        countDownLatch.countDown();
                    }

                    @Override
                    public Executor getExecutor() {
                        return executor;
                    }
                }, 1000);
            } catch (RemotingException e) {
                countDownLatch.countDown();
                String errorMessage = "RemotingException caught in callback!";
                log.error(errorMessage, e);
                Assert.fail(errorMessage);
            }
            try {
                countDownLatch.await();
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
    public void testServerSyncUsingConnection() throws Exception {
        for (int i = 0; i < invokeTimes; i++) {
            RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_CLIENT_STR);
            String serverReturn = (String) client.invokeSync(address, requestBody, 1000);
            Assert.assertEquals(serverReturn, RequestBody.DEFAULT_SERVER_RETURN_STR);

            Assert.assertNotNull(serverConnectProcessor.getConnection());
            Connection serverConnection = serverConnectProcessor.getConnection();
            RequestBody requestBody2 = new RequestBody(1, RequestBody.DEFAULT_SERVER_STR);
            String clientReturn = (String) server.invokeSync(serverConnection, requestBody2, 1000);
            Assert.assertEquals(clientReturn, RequestBody.DEFAULT_CLIENT_RETURN_STR);
        }

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }

    @Test
    public void testServerSyncUsingConnection2() throws Exception {
        Connection clientConnection = client.createStandaloneConnection(ip, port, 1000);
        for (int i = 0; i < invokeTimes; i++) {
            RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_CLIENT_STR);
            String serverReturn = (String) client.invokeSync(clientConnection, requestBody, 1000);
            Assert.assertEquals(serverReturn, RequestBody.DEFAULT_SERVER_RETURN_STR);

            Assert.assertNotNull(serverConnectProcessor.getConnection());
            Connection serverConnection = serverConnectProcessor.getConnection();
            RequestBody requestBody2 = new RequestBody(1, RequestBody.DEFAULT_SERVER_STR);
            String clientres = (String) server.invokeSync(serverConnection, requestBody2, 1000);
            Assert.assertEquals(clientres, RequestBody.DEFAULT_CLIENT_RETURN_STR);
        }

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }

    @Test
    public void testServerSyncUsingAddress() throws Exception {
        Connection clientConnection = client.createStandaloneConnection(ip, port, 1000);
        for (int i = 0; i < invokeTimes; i++) {
            RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_CLIENT_STR);
            String serverReturn = (String) client.invokeSync(clientConnection, requestBody, 1000);
            Assert.assertEquals(serverReturn, RequestBody.DEFAULT_SERVER_RETURN_STR);

            Assert.assertNotNull(serverConnectProcessor.getConnection());
            String remoteAddress = serverUserProcessor.getRemoteAddress();
            RequestBody requestBody2 = new RequestBody(1, RequestBody.DEFAULT_SERVER_STR);
            String clientReturns = (String) server.invokeSync(remoteAddress, requestBody2, 1000);
            Assert.assertEquals(clientReturns, RequestBody.DEFAULT_CLIENT_RETURN_STR);
        }

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }
}