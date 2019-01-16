/**
 * FileName: TimeoutTest
 * Author:   HuangTaiHong
 * Date:     2019/1/16 14:56
 * Description: 
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.timeout;

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
import roberto.group.process.netty.practice.connection.enums.ConnectionEventTypeEnum;
import roberto.group.process.netty.practice.entrance.client.RGPDefaultRemoteClient;
import roberto.group.process.netty.practice.entrance.server.impl.RGPDefaultRemoteServer;
import roberto.group.process.netty.practice.exception.RemotingException;
import roberto.group.process.netty.practice.exception.remote.InvokeTimeoutException;
import roberto.group.process.netty.practice.remote.invoke.callback.InvokeCallback;
import roberto.group.process.netty.practice.remote.remote.RPCResponseFuture;
import roberto.group.process.netty.practice.utils.PortScanner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Timeout Test.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/16
 * @since 1.0.0
 */
@Slf4j
public class TimeoutTest {
    private int timeout = 250;

    private int port = PortScanner.select();
    private String address = "127.0.0.1:" + port;

    private RGPDefaultRemoteServer server;
    private RGPDefaultRemoteClient client;

    private CONNECTEventProcessor clientConnectProcessor = new CONNECTEventProcessor();
    private CONNECTEventProcessor serverConnectProcessor = new CONNECTEventProcessor();

    private DISCONNECTEventProcessor clientDisConnectProcessor = new DISCONNECTEventProcessor();
    private DISCONNECTEventProcessor serverDisConnectProcessor = new DISCONNECTEventProcessor();

    private SimpleServerUserProcessor serverUserProcessor = new SimpleServerUserProcessor(timeout * 2);
    private SimpleClientUserProcessor clientUserProcessor = new SimpleClientUserProcessor();

    @Before
    public void init() {
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
    public void testSyncTimeout() {
        Object serverReturn = null;
        try {
            RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_SYNC_STR);
            serverReturn = client.invokeSync(address, requestBody, timeout);
            Assert.fail("Should not reach here!");
        } catch (InvokeTimeoutException e) {
            Assert.assertNull(serverReturn);
        } catch (RemotingException e) {
            log.error("Other RemotingException but InvokeTimeoutException occurred in sync", e);
            Assert.fail("Should not reach here!");
        } catch (InterruptedException e) {
            log.error("InterruptedException in sync", e);
            Assert.fail("Should not reach here!");
        }
    }

    @Test
    public void testSyncOK() {
        try {
            RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_SYNC_STR);
            Object serverReturn = client.invokeSync(address, requestBody, timeout + 500);
            Assert.assertEquals(serverReturn, RequestBody.DEFAULT_SERVER_RETURN_STR);
        } catch (InvokeTimeoutException e) {
            Assert.fail("Should not reach here!");
        } catch (RemotingException e) {
            log.error("Other RemotingException but InvokeTimeoutException occurred in sync", e);
            Assert.fail("Should not reach here!");
        } catch (InterruptedException e) {
            log.error("InterruptedException in sync", e);
            Assert.fail("Should not reach here!");
        }
    }

    @Test
    public void testFutureTimeout() {
        Object serverReturn = null;
        try {
            RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_FUTURE_STR);
            RPCResponseFuture future = client.invokeWithFuture(address, requestBody, timeout);
            serverReturn = future.get(timeout - 50);
            Assert.fail("Should not reach here!");
        } catch (InvokeTimeoutException e) {
            Assert.assertNull(serverReturn);
        } catch (RemotingException e) {
            log.error("Should not catch any exception here", e);
            Assert.fail("Should not reach here!");
        } catch (InterruptedException e) {
            log.error("InterruptedException in sync", e);
            Assert.fail("Should not reach here!");
        }
    }

    @Test
    public void testFutureOK() {
        Object serverReturn = null;
        try {
            RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_FUTURE_STR);
            RPCResponseFuture future = client.invokeWithFuture(address, requestBody, timeout);
            serverReturn = future.get(timeout + 100);
            Assert.fail("Should not reach here!");
        } catch (InvokeTimeoutException e) {
            Assert.assertNull(serverReturn);
        } catch (RemotingException e) {
            log.error("Other RemotingException but InvokeTimeoutException occurred in future", e);
            Assert.fail("Should not reach here!");
        } catch (InterruptedException e) {
            log.error("InterruptedException in sync", e);
            Assert.fail("Should not reach here!");
        }
    }

    @Test
    public void testCallback() throws InterruptedException {
        RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_CALLBACK_STR);
        final CountDownLatch latch = new CountDownLatch(1);
        final List<Class<?>> resultList = new ArrayList<>();
        try {
            client.invokeWithCallback(address, requestBody, new InvokeCallback() {
                @Override
                public void onResponse(Object result) {
                    Assert.fail("Should not reach here!");
                }

                @Override
                public void onException(Throwable e) {
                    log.error("Process exception in callback.", e);
                    resultList.add(e.getClass());
                    latch.countDown();
                }

                @Override
                public Executor getExecutor() {
                    return null;
                }
            }, timeout);
        } catch (RemotingException e) {
            log.error("Other RemotingException but InvokeTimeoutException occurred in future", e);
            Assert.fail("Should not reach here!");
        }
        latch.await();
        Assert.assertEquals(InvokeTimeoutException.class, resultList.get(0));
    }
}