/**
 * FileName: BasicUsageMultiInterestUserProcessorTest
 * Author:   HuangTaiHong
 * Date:     2019/1/16 16:36
 * Description: basic usage test.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.userprocessor.multiinterestprocessor;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import roberto.group.process.netty.practice.common.CONNECTEventProcessor;
import roberto.group.process.netty.practice.common.DISCONNECTEventProcessor;
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
 * 〈basic usage test.〉
 *
 *  basic usage of rpc client and rpc server using multi interest user processor.
 *
 * @author HuangTaiHong
 * @create 2019/1/16
 * @since 1.0.0
 */
@Slf4j
public class BasicUsageMultiInterestUserProcessorTest {
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

    private SimpleServerMultiInterestUserProcessor serverUserProcessor = new SimpleServerMultiInterestUserProcessor();
    private SimpleClientMultiInterestUserProcessor clientUserProcessor = new SimpleClientMultiInterestUserProcessor();

    @Before
    @SuppressWarnings("all")
    public void init() {
        server = new RGPDefaultRemoteServer(port, true);
        server.start();
        server.addConnectionEventProcessor(ConnectionEventTypeEnum.CONNECT, serverConnectProcessor);
        server.addConnectionEventProcessor(ConnectionEventTypeEnum.CLOSE, serverDisConnectProcessor);
        server.registerUserProcessor(serverUserProcessor);

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
        MultiInterestBaseRequestBody bodyFromClient1 = new RequestBodyFromClient1(1, RequestBodyFromClient1.DEFAULT_ONEWAY_STR);
        MultiInterestBaseRequestBody bodyFromClient2 = new RequestBodyFromClient2(1, RequestBodyFromClient2.DEFAULT_ONEWAY_STR);
        for (int i = 0; i < invokeTimes; i++) {
            try {
                client.oneway(address, bodyFromClient1);
                Thread.sleep(100);
            } catch (RemotingException e) {
                String errorMessage = "RemotingException caught in oneway!";
                log.error(errorMessage, e);
                Assert.fail(errorMessage);
            }
        }

        for (int j = 0; j < invokeTimes; j++) {
            try {
                client.oneway(address, bodyFromClient2);
                Thread.sleep(100);
            } catch (RemotingException e) {
                String errorMessage = "RemotingException caught in oneway!";
                log.error(errorMessage, e);
                Assert.fail(errorMessage);
            }
        }
        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimesC1());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimesC2());
    }

    @Test
    public void testSync() throws InterruptedException {
        MultiInterestBaseRequestBody bodyFromClient1 = new RequestBodyFromClient1(1, RequestBodyFromClient1.DEFAULT_SYNC_STR);
        MultiInterestBaseRequestBody bodyFromClient2 = new RequestBodyFromClient2(1, RequestBodyFromClient1.DEFAULT_SYNC_STR);
        for (int i = 0; i < invokeTimes; i++) {
            try {
                String result = (String) client.invokeSync(address, bodyFromClient1, 3000);
                log.warn("Result received in sync: " + result);
                Assert.assertEquals(RequestBodyFromClient1.DEFAULT_SERVER_RETURN_STR, result);
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

        for (int i = 0; i < invokeTimes; i++) {
            try {
                String result = (String) client.invokeSync(address, bodyFromClient2, 3000);
                log.warn("Result received in sync: " + result);
                Assert.assertEquals(RequestBodyFromClient2.DEFAULT_SERVER_RETURN_STR, result);
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
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimesC1());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimesC2());
    }

    @Test
    public void testFuture() throws InterruptedException {
        MultiInterestBaseRequestBody requestBodyFromClient1 = new RequestBodyFromClient1(1, RequestBodyFromClient1.DEFAULT_FUTURE_STR);
        MultiInterestBaseRequestBody requestBodyFromClient2 = new RequestBodyFromClient2(1, RequestBodyFromClient1.DEFAULT_FUTURE_STR);
        for (int i = 0; i < invokeTimes; i++) {
            try {
                RPCResponseFuture future = client.invokeWithFuture(address, requestBodyFromClient1, 3000);
                String result = (String) future.get();
                Assert.assertEquals(RequestBodyFromClient1.DEFAULT_SERVER_RETURN_STR, result);
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
        for (int i = 0; i < invokeTimes; i++) {
            try {
                RPCResponseFuture future = client.invokeWithFuture(address, requestBodyFromClient2, 3000);
                String res = (String) future.get();
                Assert.assertEquals(RequestBodyFromClient2.DEFAULT_SERVER_RETURN_STR, res);
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
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimesC1());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimesC2());
    }

    @Test
    @SuppressWarnings("all")
    public void testCallback() throws InterruptedException {
        MultiInterestBaseRequestBody requestBodyFromClient1 = new RequestBodyFromClient1(1, RequestBodyFromClient1.DEFAULT_CALLBACK_STR);
        MultiInterestBaseRequestBody requestBodyFromClient2 = new RequestBodyFromClient2(1, RequestBodyFromClient1.DEFAULT_CALLBACK_STR);
        final List<String> resultList = new ArrayList<>(1);
        for (int i = 0; i < invokeTimes; i++) {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            try {
                client.invokeWithCallback(address, requestBodyFromClient1, new InvokeCallback() {
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
            Assert.assertEquals(RequestBodyFromClient1.DEFAULT_SERVER_RETURN_STR, resultList.get(0));
            resultList.clear();
        }

        final List<String> resultList2 = new ArrayList<String>(1);
        for (int i = 0; i < invokeTimes; i++) {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            try {
                client.invokeWithCallback(address, requestBodyFromClient2, new InvokeCallback() {
                    Executor executor = Executors.newCachedThreadPool();

                    @Override
                    public void onResponse(Object result) {
                        log.warn("Result received in callback: " + result);
                        resultList2.add((String) result);
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
            if (resultList2.size() == 0) {
                Assert.fail("No result! Maybe exception caught!");
            }
            Assert.assertEquals(RequestBodyFromClient2.DEFAULT_SERVER_RETURN_STR, resultList2.get(0));
            resultList.clear();
        }
        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimesC1());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimesC2());
    }
}