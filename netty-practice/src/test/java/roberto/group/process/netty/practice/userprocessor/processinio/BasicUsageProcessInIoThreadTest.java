/**
 * FileName: BasicUsageProcessInIoThreadTest
 * Author:   HuangTaiHong
 * Date:     2019/1/16 17:29
 * Description: basic usage test.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.userprocessor.processinio;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
 *  basic usage of rpc client and rpc server using specific user processor.
 *
 * @author HuangTaiHong
 * @create 2019/1/16
 * @since 1.0.0
 */
@Slf4j
public class BasicUsageProcessInIoThreadTest {
    private int invokeTimes = 5;

    private RGPDefaultRemoteServer server;
    private RGPDefaultRemoteClient client;

    private String ip = "127.0.0.1";
    private int port = PortScanner.select();
    private String address = "127.0.0.1:" + port;

    private CONNECTEventProcessor clientConnectProcessor = new CONNECTEventProcessor();
    private CONNECTEventProcessor serverConnectProcessor = new CONNECTEventProcessor();

    private DISCONNECTEventProcessor clientDisConnectProcessor = new DISCONNECTEventProcessor();
    private DISCONNECTEventProcessor serverDisConnectProcessor = new DISCONNECTEventProcessor();

    private SpecificServerUserProcessor serverUserProcessor = new SpecificServerUserProcessor();
    private SpecificClientUserProcessor clientUserProcessor = new SpecificClientUserProcessor();

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
        RequestBody requestBody = new RequestBody(2, "hello world oneway");
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
        RequestBody requestBody = new RequestBody(1, "hello world sync");
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
        RequestBody requestBody = new RequestBody(2, "hello world future");
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
    @SuppressWarnings("all")
    public void testServerSyncUsingConnection() throws Exception {
        Connection clientConnection = client.createStandaloneConnection(ip, port, 1000);
        for (int i = 0; i < invokeTimes; i++) {
            RequestBody req1 = new RequestBody(1, RequestBody.DEFAULT_CLIENT_STR);
            String serverReturn = (String) client.invokeSync(clientConnection, req1, 1000);
            Assert.assertEquals(serverReturn, RequestBody.DEFAULT_SERVER_RETURN_STR);

            Assert.assertNotNull(serverConnectProcessor.getConnection());
            Connection serverConnection = serverConnectProcessor.getConnection();
            RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_SERVER_STR);
            String clientres = (String) server.invokeSync(serverConnection, requestBody, 1000);
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
            RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_CLIENT_STR);
            String serverres = (String) client.invokeSync(clientConn, requestBody, 1000);
            Assert.assertEquals(serverres, RequestBody.DEFAULT_SERVER_RETURN_STR);

            Assert.assertNotNull(serverConnectProcessor.getConnection());
            // only when client invoked, the remote address can be get by UserProcessor. otherwise, please use ConnectionEventProcessor
            String remoteAddr = serverUserProcessor.getRemoteAddress();
            RequestBody requestBody2 = new RequestBody(1, RequestBody.DEFAULT_SERVER_STR);
            String clientres = (String) server.invokeSync(remoteAddr, requestBody2, 1000);
            Assert.assertEquals(clientres, RequestBody.DEFAULT_CLIENT_RETURN_STR);
        }

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }
}