/**
 * FileName: CustomSerializerCodecTest
 * Author:   HuangTaiHong
 * Date:     2019/1/16 11:21
 * Description: test custom serializer codec.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.serializer;

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
import roberto.group.process.netty.practice.remote.invoke.context.InvokeContext;
import roberto.group.process.netty.practice.remote.remote.RPCResponseFuture;
import roberto.group.process.netty.practice.serialize.custom.manager.CustomSerializerManager;
import roberto.group.process.netty.practice.utils.PortScanner;
import roberto.group.process.netty.practice.utils.RemotingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 〈一句话功能简述〉<br> 
 * 〈test custom serializer codec.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/16
 * @since 1.0.0
 */
@Slf4j
public class CustomSerializerCodecTest {
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

    private SimpleServerUserProcessor serverUserProcessor = new SimpleServerUserProcessor();
    private SimpleClientUserProcessor clientUserProcessor = new SimpleClientUserProcessor();

    @Before
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
        CustomSerializerManager.clear();
        try {
            server.stop();
            Thread.sleep(100);
        } catch (InterruptedException e) {
            log.error("Stop server failed!", e);
        }
    }

    @Test
    public void testOneway() throws InterruptedException {
        NormalStringCustomSerializer normalStringCustomSerializer = new NormalStringCustomSerializer();
        NormalRequestBodyCustomSerializer normalRequestBodyCustomSerializer = new NormalRequestBodyCustomSerializer();
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), normalStringCustomSerializer);
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), normalRequestBodyCustomSerializer);

        RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_ONEWAY_STR);
        for (int i = 0; i < invokeTimes; i++) {
            try {
                byte testCodec = (byte) i;
                InvokeContext invokeContext = new InvokeContext();
                invokeContext.put(InvokeContext.BOLT_CUSTOM_SERIALIZER, testCodec);
                client.oneway(address, requestBody, invokeContext);
                Assert.assertEquals(testCodec, normalRequestBodyCustomSerializer.getContentSerializer());
                Assert.assertEquals(-1, normalStringCustomSerializer.getContentSerializer());
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
        NormalStringCustomSerializer normalStringCustomSerializer = new NormalStringCustomSerializer();
        NormalRequestBodyCustomSerializer normalRequestBodyCustomSerializer = new NormalRequestBodyCustomSerializer();
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), normalStringCustomSerializer);
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), normalRequestBodyCustomSerializer);

        RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_SYNC_STR);
        for (int i = 0; i < invokeTimes; i++) {
            try {
                byte testCodec = (byte) i;
                InvokeContext invokeContext = new InvokeContext();
                invokeContext.put(InvokeContext.BOLT_CUSTOM_SERIALIZER, testCodec);
                String serverReturn = (String) client.invokeSync(address, requestBody, invokeContext, 3000);
                log.warn("Result received in sync: " + serverReturn);
                Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR + "RANDOM", serverReturn);
                Assert.assertEquals(testCodec, normalRequestBodyCustomSerializer.getContentSerializer());
                Assert.assertEquals(testCodec, normalStringCustomSerializer.getContentSerializer());
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
    public void testFuture() throws InterruptedException {
        NormalStringCustomSerializer normalStringCustomSerializer = new NormalStringCustomSerializer();
        NormalRequestBodyCustomSerializer normalRequestBodyCustomSerializer = new NormalRequestBodyCustomSerializer();
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), normalStringCustomSerializer);
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), normalRequestBodyCustomSerializer);

        RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_FUTURE_STR);
        for (int i = 0; i < invokeTimes; i++) {
            try {
                byte testCodec = (byte) i;
                InvokeContext invokeContext = new InvokeContext();
                invokeContext.put(InvokeContext.BOLT_CUSTOM_SERIALIZER, testCodec);
                RPCResponseFuture future = client.invokeWithFuture(address, requestBody, invokeContext, 3000);
                String serverReturn = (String) future.get();
                Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR + "RANDOM", serverReturn);
                Assert.assertEquals(testCodec, normalRequestBodyCustomSerializer.getContentSerializer());
                Assert.assertEquals(testCodec, normalStringCustomSerializer.getContentSerializer());
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
        NormalStringCustomSerializer normalStringCustomSerializer = new NormalStringCustomSerializer();
        NormalRequestBodyCustomSerializer normalRequestBodyCustomSerializer = new NormalRequestBodyCustomSerializer();
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), normalStringCustomSerializer);
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), normalRequestBodyCustomSerializer);

        RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_CALLBACK_STR);
        final List<String> resultList = new ArrayList<String>(1);
        for (int i = 0; i < invokeTimes; i++) {
            byte testCodec = (byte) i;
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            InvokeContext invokeContext = new InvokeContext();
            invokeContext.put(InvokeContext.BOLT_CUSTOM_SERIALIZER, testCodec);
            try {
                client.invokeWithCallback(address, requestBody, invokeContext, new InvokeCallback() {
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
            Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR + "RANDOM", resultList.get(0));
            resultList.clear();
            Assert.assertEquals(testCodec, normalRequestBodyCustomSerializer.getContentSerializer());
            Assert.assertEquals(testCodec, normalStringCustomSerializer.getContentSerializer());
        }

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }

    @Test
    public void testServerSyncUsingConnection() throws Exception {
        NormalStringCustomSerializer normalStringCustomSerializer = new NormalStringCustomSerializer();
        NormalRequestBodyCustomSerializer normalRequestBodyCustomSerializer = new NormalRequestBodyCustomSerializer();
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), normalStringCustomSerializer);
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), normalRequestBodyCustomSerializer);

        Connection clientConnection = client.createStandaloneConnection(ip, port, 1000);
        for (int i = 0; i < invokeTimes; i++) {
            byte testCodec = (byte) i;
            InvokeContext invokeContext = new InvokeContext();
            invokeContext.put(InvokeContext.BOLT_CUSTOM_SERIALIZER, testCodec);
            RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_CLIENT_STR);
            String serverReturn = (String) client.invokeSync(clientConnection, requestBody, invokeContext, 1000);
            Assert.assertEquals(serverReturn, RequestBody.DEFAULT_SERVER_RETURN_STR + "RANDOM");
            Assert.assertEquals(testCodec, normalRequestBodyCustomSerializer.getContentSerializer());
            Assert.assertEquals(testCodec, normalStringCustomSerializer.getContentSerializer());

            Assert.assertNotNull(serverConnectProcessor.getConnection());
            Connection serverConnection = serverConnectProcessor.getConnection();
            invokeContext.clear();
            invokeContext.put(InvokeContext.BOLT_CUSTOM_SERIALIZER, (byte) (testCodec + 1));
            normalRequestBodyCustomSerializer.reset();
            normalStringCustomSerializer.reset();

            RequestBody requestBody2 = new RequestBody(1, RequestBody.DEFAULT_SERVER_STR);
            String clientReturn = (String) server.invokeSync(serverConnection, requestBody2, invokeContext, 1000);
            Assert.assertEquals(clientReturn, RequestBody.DEFAULT_CLIENT_RETURN_STR + "RANDOM");
            Assert.assertEquals(testCodec + 1, normalRequestBodyCustomSerializer.getContentSerializer());
            Assert.assertEquals(testCodec + 1, normalStringCustomSerializer.getContentSerializer());
        }

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }

    @Test
    public void testServerSyncUsingAddress() throws Exception {
        NormalStringCustomSerializer normalStringCustomSerializer = new NormalStringCustomSerializer();
        NormalRequestBodyCustomSerializer normalRequestBodyCustomSerializer = new NormalRequestBodyCustomSerializer();
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), normalStringCustomSerializer);
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), normalRequestBodyCustomSerializer);

        Connection clientConnection = client.createStandaloneConnection(ip, port, 1000);
        String local = RemotingUtil.parseLocalAddress(clientConnection.getChannel());
        String remote = RemotingUtil.parseRemoteAddress(clientConnection.getChannel());
        log.warn("Client say local:" + local);
        log.warn("Client say remote:" + remote);
        for (int i = 0; i < invokeTimes; i++) {
            byte testCodec = (byte) i;
            InvokeContext invokeContext = new InvokeContext();
            invokeContext.put(InvokeContext.BOLT_CUSTOM_SERIALIZER, testCodec);
            RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_CLIENT_STR);
            String serverReturn = (String) client.invokeSync(clientConnection, requestBody, invokeContext, 1000);
            Assert.assertEquals(serverReturn, RequestBody.DEFAULT_SERVER_RETURN_STR + "RANDOM");
            Assert.assertEquals(testCodec, normalRequestBodyCustomSerializer.getContentSerializer());
            Assert.assertEquals(testCodec, normalStringCustomSerializer.getContentSerializer());

            invokeContext.clear();
            invokeContext.put(InvokeContext.BOLT_CUSTOM_SERIALIZER, (byte) (testCodec + 1));
            normalRequestBodyCustomSerializer.reset();
            normalStringCustomSerializer.reset();
            Assert.assertNotNull(serverConnectProcessor.getConnection());
            // only when client invoked, the remote address can be get by UserProcessor otherwise, please use ConnectionEventProcessor
            String remoteAddress = serverUserProcessor.getRemoteAddress();

            RequestBody requestBody2 = new RequestBody(1, RequestBody.DEFAULT_SERVER_STR);
            String clientres = (String) server.invokeSync(remoteAddress, requestBody2, invokeContext, 1000);
            Assert.assertEquals(clientres, RequestBody.DEFAULT_CLIENT_RETURN_STR + "RANDOM");
            Assert.assertEquals(testCodec + 1, normalRequestBodyCustomSerializer.getContentSerializer());
            Assert.assertEquals(testCodec + 1, normalStringCustomSerializer.getContentSerializer());
        }

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }

    @Test
    public void testIllegalType() throws Exception {
        NormalStringCustomSerializer normalStringCustomSerializer = new NormalStringCustomSerializer();
        NormalRequestBodyCustomSerializer normalRequestBodyCustomSerializer = new NormalRequestBodyCustomSerializer();
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), normalStringCustomSerializer);
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), normalRequestBodyCustomSerializer);

        RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_SYNC_STR);
        for (int i = 0; i < invokeTimes; i++) {
            try {
                byte testCodec = (byte) i;
                InvokeContext invokeContext = new InvokeContext();
                invokeContext.put(InvokeContext.BOLT_CUSTOM_SERIALIZER, (int) testCodec);
                String serverReturn = (String) client.invokeSync(address, requestBody, invokeContext, 3000);
                log.warn("Result received in sync: " + serverReturn);
                Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR + "RANDOM", serverReturn);
                Assert.assertEquals(testCodec, normalRequestBodyCustomSerializer.getContentSerializer());
                Assert.assertEquals(testCodec, normalStringCustomSerializer.getContentSerializer());
            } catch (IllegalArgumentException e) {
                log.error("IllegalArgumentException", e);
                Assert.assertTrue(true);
                return;
            } catch (InterruptedException e) {
                String errorMessage = "InterruptedException caught in sync!";
                log.error(errorMessage, e);
                Assert.fail(errorMessage);
            }
            Assert.fail("Should not reach here!");
        }

        Assert.assertTrue(serverConnectProcessor.isConnected());
        Assert.assertEquals(1, serverConnectProcessor.getConnectTimes());
        Assert.assertEquals(invokeTimes, serverUserProcessor.getInvokeTimes());
    }
}