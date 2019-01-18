/**
 * FileName: BasicUsageAsyncProcessorTest
 * Author:   HuangTaiHong
 * Date:     2019/1/16 19:47
 * Description: async basic usage test.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.userprocessor.asyncprocessor;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import roberto.group.process.netty.practice.common.AsyncClientUserProcessor;
import roberto.group.process.netty.practice.common.AsyncServerUserProcessor;
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
import roberto.group.process.netty.practice.utils.PortScanner;
import roberto.group.process.netty.practice.utils.RemotingAddressUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 〈一句话功能简述〉<br> 
 * 〈async basic usage test.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/16
 * @since 1.0.0
 */
@Slf4j
public class BasicUsageAsyncProcessorTest {
    private RGPDefaultRemoteServer server;
    private RGPDefaultRemoteClient client;

    private String ip = "127.0.0.1";
    private int port = PortScanner.select();
    private String addr = "127.0.0.1:" + port;

    private int invokeTimes = 5;

    private CONNECTEventProcessor clientConnectProcessor = new CONNECTEventProcessor();
    private CONNECTEventProcessor serverConnectProcessor = new CONNECTEventProcessor();

    private DISCONNECTEventProcessor clientDisConnectProcessor = new DISCONNECTEventProcessor();
    private DISCONNECTEventProcessor serverDisConnectProcessor = new DISCONNECTEventProcessor();

    private AsyncServerUserProcessor serverUserProcessor = new AsyncServerUserProcessor();
    private AsyncClientUserProcessor clientUserProcessor = new AsyncClientUserProcessor();

    @Before
    @SuppressWarnings("all")
    public void init() {
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
    @SuppressWarnings("all")
    public void testOneway() throws InterruptedException {
        RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_ONEWAY_STR);
        for (int i = 0; i < invokeTimes; i++) {
            try {
                client.oneway(addr, requestBody);
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
                String serverReturn = (String) client.invokeSync(addr, requestBody, 30000000);
                log.warn("Result received in sync: " + serverReturn);
                Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR, serverReturn);
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
        RequestBody requestBody = new RequestBody(2, "hello world future");
        for (int i = 0; i < invokeTimes; i++) {
            try {
                RPCResponseFuture future = client.invokeWithFuture(addr, requestBody, 3000);
                String serverReturn = (String) future.get();
                Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR, serverReturn);
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
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            try {
                client.invokeWithCallback(addr, requestBody, new InvokeCallback() {
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
    @SuppressWarnings("all")
    public void testServerSyncUsingConnection() throws Exception {
        Connection clientConnection = client.createStandaloneConnection(ip, port, 1000);
        for (int i = 0; i < invokeTimes; i++) {
            RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_CLIENT_STR);
            String serverres = (String) client.invokeSync(clientConnection, requestBody, 1000);
            Assert.assertEquals(serverres, RequestBody.DEFAULT_SERVER_RETURN_STR);

            Assert.assertNotNull(serverConnectProcessor.getConnection());
            Connection serverConn = serverConnectProcessor.getConnection();
            RequestBody requestBody2 = new RequestBody(1, RequestBody.DEFAULT_SERVER_STR);
            String clientres = (String) server.invokeSync(serverConn, requestBody2, 1000);
            Assert.assertEquals(clientres, RequestBody.DEFAULT_CLIENT_RETURN_STR);
        }
        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }

    @Test
    public void testServerSyncUsingAddress() throws Exception {
        Connection clientConnection = client.createStandaloneConnection(ip, port, 1000);
        String local = RemotingAddressUtil.parseLocalAddress(clientConnection.getChannel());
        String remote = RemotingAddressUtil.parseRemoteAddress(clientConnection.getChannel());
        log.warn("Client say local:" + local);
        log.warn("Client say remote:" + remote);
        for (int i = 0; i < invokeTimes; i++) {
            RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_CLIENT_STR);
            String serverReturn = (String) client.invokeSync(clientConnection, requestBody, 1000);
            Assert.assertEquals(serverReturn, RequestBody.DEFAULT_SERVER_RETURN_STR);

            Assert.assertNotNull(serverConnectProcessor.getConnection());
            // only when client invoked, the remote address can be get by UserProcessor. otherwise, please use ConnectionEventProcessor
            String remoteAddress = serverUserProcessor.getRemoteAddress();
            RequestBody requestBody2 = new RequestBody(1, RequestBody.DEFAULT_SERVER_STR);
            String clientReturn = (String) server.invokeSync(remoteAddress, requestBody2, 1000);
            Assert.assertEquals(clientReturn, RequestBody.DEFAULT_CLIENT_RETURN_STR);
        }
        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }
}