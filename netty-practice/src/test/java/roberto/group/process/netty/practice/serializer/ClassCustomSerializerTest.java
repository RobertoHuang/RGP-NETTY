/**
 * FileName: ClassCustomSerializerTest
 * Author:   HuangTaiHong
 * Date:     2019/1/16 9:53
 * Description: Custom Serializer Test: Normal, Exception included.
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
import roberto.group.process.netty.practice.configuration.support.ConfigsSupport;
import roberto.group.process.netty.practice.connection.enums.ConnectionEventTypeEnum;
import roberto.group.process.netty.practice.entrance.client.RGPDefaultRemoteClient;
import roberto.group.process.netty.practice.entrance.server.impl.RGPDefaultRemoteServer;
import roberto.group.process.netty.practice.exception.DeserializationException;
import roberto.group.process.netty.practice.exception.SerializationException;
import roberto.group.process.netty.practice.remote.invoke.callback.InvokeCallback;
import roberto.group.process.netty.practice.context.InvokeContext;
import roberto.group.process.netty.practice.remote.remote.RPCResponseFuture;
import roberto.group.process.netty.practice.serialize.custom.CustomSerializerManager;
import roberto.group.process.netty.practice.serialize.serialize.manager.SerializerManager;
import roberto.group.process.netty.practice.utils.PortScanner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Custom Serializer Test: Normal, Exception included.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/16
 * @since 1.0.0
 */
@Slf4j
public class ClassCustomSerializerTest {
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
        server = new RGPDefaultRemoteServer(port);
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
        CustomSerializerManager.clear();
        server.stop();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            log.error("Stop server failed!", e);
        }
    }

    @Test
    public void testNormalCustomSerializer() throws Exception {
        NormalStringCustomSerializer normalStringCustomSerializer = new NormalStringCustomSerializer();
        NormalRequestBodyCustomSerializer normalRequestBodyCustomSerializer = new NormalRequestBodyCustomSerializer();
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), normalStringCustomSerializer);
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), normalRequestBodyCustomSerializer);

        RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_SYNC_STR);
        String serverReturn = (String) client.invokeSync(address, requestBody, 1000);
        Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR + "RANDOM", serverReturn);

        Assert.assertTrue(normalRequestBodyCustomSerializer.isSerialized());
        Assert.assertTrue(normalRequestBodyCustomSerializer.isDeserialized());

        Assert.assertTrue(normalStringCustomSerializer.isSerialized());
        Assert.assertTrue(normalStringCustomSerializer.isDeserialized());
    }

    @Test
    @SuppressWarnings("all")
    public void testRequestSerialException() throws Exception {
        NormalStringCustomSerializer normalStringCustomSerializer = new NormalStringCustomSerializer();
        ExceptionRequestBodyCustomSerializer exceptionRequestBodyCustomSerializer = new ExceptionRequestBodyCustomSerializer(true, false, false, false);
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), normalStringCustomSerializer);
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), exceptionRequestBodyCustomSerializer);

        String serverReutrn = null;
        try {
            RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_SYNC_STR);
            serverReutrn = (String) client.invokeSync(address, requestBody, 1000);
            Assert.fail("Should not reach here!");
        } catch (SerializationException e) {
            log.error("Serialize exception.", e);
            Assert.assertFalse(e.isServerSide());
            Assert.assertEquals(null, serverReutrn);

            Assert.assertTrue(exceptionRequestBodyCustomSerializer.isSerialized());
            Assert.assertFalse(exceptionRequestBodyCustomSerializer.isDeserialized());

            Assert.assertFalse(normalStringCustomSerializer.isSerialized());
            Assert.assertFalse(normalStringCustomSerializer.isDeserialized());
        } catch (Throwable t) {
            Assert.fail("Should not reach here!");
        }
    }

    @Test
    @SuppressWarnings("all")
    public void testRequestSerialRuntimeException() throws Exception {
        NormalStringCustomSerializer normalStringCustomSerializer = new NormalStringCustomSerializer();
        ExceptionRequestBodyCustomSerializer exceptionRequestBodyCustomSerializer = new ExceptionRequestBodyCustomSerializer(false, true, false, false);
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), normalStringCustomSerializer);
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), exceptionRequestBodyCustomSerializer);

        String serverReturn = null;
        try {
            RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_SYNC_STR);
            serverReturn = (String) client.invokeSync(address, requestBody, 1000);
            Assert.fail("Should not reach here!");
        } catch (SerializationException e) {
            log.error("Serialize exception.", e);
            Assert.assertFalse(e.isServerSide());
            Assert.assertEquals(null, serverReturn);

            Assert.assertTrue(exceptionRequestBodyCustomSerializer.isSerialized());
            Assert.assertFalse(exceptionRequestBodyCustomSerializer.isDeserialized());

            Assert.assertFalse(normalStringCustomSerializer.isSerialized());
            Assert.assertFalse(normalStringCustomSerializer.isDeserialized());
        } catch (Throwable t) {
            Assert.fail("Should not reach here!");
        }
    }

    @Test
    @SuppressWarnings("all")
    public void testRequestDeserialException() throws Exception {
        System.setProperty(ConfigsSupport.SERIALIZER, Byte.toString(SerializerManager.HESSIAN2));
        NormalStringCustomSerializer normalStringCustomSerializer = new NormalStringCustomSerializer();
        ExceptionRequestBodyCustomSerializer exceptionRequestBodyCustomSerializer = new ExceptionRequestBodyCustomSerializer(false, false, true, false);
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), normalStringCustomSerializer);
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), exceptionRequestBodyCustomSerializer);

        String serverReturn = null;
        try {
            RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_SYNC_STR);
            serverReturn = (String) client.invokeSync(address, requestBody, 1000);
            Assert.fail("Should not reach here!");
        } catch (DeserializationException e) {
            log.error("Serialize exception.", e);
            Assert.assertTrue(e.isServerSide());
            Assert.assertEquals(null, serverReturn);

            Assert.assertTrue(exceptionRequestBodyCustomSerializer.isSerialized());
            Assert.assertTrue(exceptionRequestBodyCustomSerializer.isDeserialized());

            Assert.assertFalse(normalStringCustomSerializer.isSerialized());
            Assert.assertFalse(normalStringCustomSerializer.isDeserialized());
        } catch (Throwable t) {
            Assert.fail("Should not reach here!");
        }
    }

    @Test
    @SuppressWarnings("all")
    public void testRequestDeserialRuntimeException() throws Exception {
        System.setProperty(ConfigsSupport.SERIALIZER, Byte.toString(SerializerManager.HESSIAN2));
        NormalStringCustomSerializer normalStringCustomSerializer = new NormalStringCustomSerializer();
        ExceptionRequestBodyCustomSerializer exceptionRequestBodyCustomSerializer = new ExceptionRequestBodyCustomSerializer(false, false, false, true);
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), normalStringCustomSerializer);
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), exceptionRequestBodyCustomSerializer);

        String serverReturn = null;
        try {
            RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_SYNC_STR);
            serverReturn = (String) client.invokeSync(address, requestBody, 1000);
            Assert.fail("Should not reach here!");
        } catch (DeserializationException e) {
            log.error("Serialize exception.", e);
            Assert.assertTrue(e.isServerSide());
            Assert.assertEquals(null, serverReturn);

            Assert.assertTrue(exceptionRequestBodyCustomSerializer.isSerialized());
            Assert.assertTrue(exceptionRequestBodyCustomSerializer.isDeserialized());

            Assert.assertFalse(normalStringCustomSerializer.isSerialized());
            Assert.assertFalse(normalStringCustomSerializer.isDeserialized());
        } catch (Throwable t) {
            Assert.fail("Should not reach here!");
        }
    }

    @Test
    @SuppressWarnings("all")
    public void testResponseSerialException() throws Exception {
        NormalRequestBodyCustomSerializer normalRequestBodyCustomSerializer = new NormalRequestBodyCustomSerializer();
        ExceptionStringCustomSerializer exceptionStringCustomSerializer = new ExceptionStringCustomSerializer(true, false, false, false);
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), exceptionStringCustomSerializer);
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), normalRequestBodyCustomSerializer);

        String serverReturn = null;
        try {
            RequestBody body = new RequestBody(1, RequestBody.DEFAULT_SYNC_STR);
            serverReturn = (String) client.invokeSync(address, body, 1000);
            Assert.fail("Should not reach here!");
        } catch (SerializationException e) {
            log.error("Serialize exception.", e);
            Assert.assertTrue(e.isServerSide());
            Assert.assertEquals(null, serverReturn);

            Assert.assertTrue(normalRequestBodyCustomSerializer.isSerialized());
            Assert.assertTrue(normalRequestBodyCustomSerializer.isDeserialized());

            Assert.assertTrue(exceptionStringCustomSerializer.isSerialized());
            Assert.assertFalse(exceptionStringCustomSerializer.isDeserialized());
        } catch (Throwable t) {
            Assert.fail("Should not reach here!");
        }
    }


    @Test
    @SuppressWarnings("all")
    public void testResponseSerialRuntimeException() throws Exception {
        NormalRequestBodyCustomSerializer normalRequestBodyCustomSerializer = new NormalRequestBodyCustomSerializer();
        ExceptionStringCustomSerializer exceptionStringCustomSerializer = new ExceptionStringCustomSerializer(false, true, false, false);
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), exceptionStringCustomSerializer);
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), normalRequestBodyCustomSerializer);

        String serverReturn = null;
        try {
            RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_SYNC_STR);
            serverReturn = (String) client.invokeSync(address, requestBody, 1000);
            Assert.fail("Should not reach here!");
        } catch (SerializationException e) {
            log.error("Serialize exception.", e);
            Assert.assertTrue(e.isServerSide());
            Assert.assertEquals(null, serverReturn);

            Assert.assertTrue(normalRequestBodyCustomSerializer.isSerialized());
            Assert.assertTrue(normalRequestBodyCustomSerializer.isDeserialized());

            Assert.assertTrue(exceptionStringCustomSerializer.isSerialized());
            Assert.assertFalse(exceptionStringCustomSerializer.isDeserialized());
        } catch (Throwable t) {
            Assert.fail("Should not reach here!");
        }
    }

    @Test
    @SuppressWarnings("all")
    public void testResponseDeserialzeException() throws Exception {
        NormalRequestBodyCustomSerializer normalRequestBodyCustomSerializer = new NormalRequestBodyCustomSerializer();
        ExceptionStringCustomSerializer exceptionStringCustomSerializer = new ExceptionStringCustomSerializer(false, false, true, false);
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), exceptionStringCustomSerializer);
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), normalRequestBodyCustomSerializer);

        String serverReturn = null;
        try {
            RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_SYNC_STR);
            serverReturn = (String) client.invokeSync(address, requestBody, 1000);
            Assert.fail("Should not reach here!");
        } catch (DeserializationException e) {
            log.error("Serialize exception.", e);
            Assert.assertFalse(e.isServerSide());
            Assert.assertEquals(null, serverReturn);

            Assert.assertTrue(normalRequestBodyCustomSerializer.isSerialized());
            Assert.assertTrue(normalRequestBodyCustomSerializer.isDeserialized());

            Assert.assertTrue(exceptionStringCustomSerializer.isSerialized());
            Assert.assertTrue(exceptionStringCustomSerializer.isDeserialized());
        } catch (Throwable t) {
            Assert.fail("Should not reach here!");
        }
    }

    @Test
    @SuppressWarnings("all")
    public void testResponseDeserialzeRuntimeException() throws Exception {
        NormalRequestBodyCustomSerializer normalRequestBodyCustomSerializer = new NormalRequestBodyCustomSerializer();
        ExceptionStringCustomSerializer exceptionStringCustomSerializer = new ExceptionStringCustomSerializer(false, false, false, true);
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), exceptionStringCustomSerializer);
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), normalRequestBodyCustomSerializer);

        String serverReturn = null;
        try {
            RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_SYNC_STR);
            serverReturn = (String) client.invokeSync(address, requestBody, 1000);
            Assert.fail("Should not reach here!");
        } catch (DeserializationException e) {
            log.error("Serialize exception.", e);
            Assert.assertFalse(e.isServerSide());
            Assert.assertEquals(null, serverReturn);

            Assert.assertTrue(normalRequestBodyCustomSerializer.isSerialized());
            Assert.assertTrue(normalRequestBodyCustomSerializer.isDeserialized());

            Assert.assertTrue(exceptionStringCustomSerializer.isSerialized());
            Assert.assertTrue(exceptionStringCustomSerializer.isDeserialized());
        } catch (Throwable t) {
            Assert.fail("Should not reach here!");
        }
    }

    @Test
    public void testInvokeContextCustomSerializer_SYNC() throws Exception {
        NormalStringCustomSerializerWithInvokeContext normalStringCustomSerializerWithInvokeContext = new NormalStringCustomSerializerWithInvokeContext();
        NormalRequestBodyCustomSerializerWithInvokeContext normalRequestBodyCustomSerializerWithInvokeContext = new NormalRequestBodyCustomSerializerWithInvokeContext();
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), normalStringCustomSerializerWithInvokeContext);
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), normalRequestBodyCustomSerializerWithInvokeContext);

        RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_SYNC_STR);
        InvokeContext invokeContext = new InvokeContext();
        invokeContext.putIfAbsent(NormalRequestBodyCustomSerializerWithInvokeContext.SERIALTYPE_KEY, NormalRequestBodyCustomSerializerWithInvokeContext.SERIALTYPE1_VALUE);
        String serverReturn = (String) client.invokeSync(address, requestBody, invokeContext, 1000);
        Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR + "RANDOM", serverReturn);
        Assert.assertTrue(normalRequestBodyCustomSerializerWithInvokeContext.isSerialized());
        Assert.assertTrue(normalRequestBodyCustomSerializerWithInvokeContext.isDeserialized());

        invokeContext.clear();
        invokeContext.putIfAbsent(NormalRequestBodyCustomSerializerWithInvokeContext.SERIALTYPE_KEY, NormalRequestBodyCustomSerializerWithInvokeContext.SERIALTYPE2_VALUE);
        serverReturn = (String) client.invokeSync(address, requestBody, invokeContext, 1000);
        Assert.assertEquals(NormalStringCustomSerializerWithInvokeContext.UNIVERSAL_RESP, serverReturn);
        Assert.assertTrue(normalRequestBodyCustomSerializerWithInvokeContext.isSerialized());
        Assert.assertTrue(normalRequestBodyCustomSerializerWithInvokeContext.isDeserialized());
    }

    @Test
    public void testInvokeContextCustomSerializer_FUTURE() throws Exception {
        NormalStringCustomSerializerWithInvokeContext normalStringCustomSerializerWithInvokeContext = new NormalStringCustomSerializerWithInvokeContext();
        NormalRequestBodyCustomSerializerWithInvokeContext normalRequestBodyCustomSerializerWithInvokeContext = new NormalRequestBodyCustomSerializerWithInvokeContext();
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), normalStringCustomSerializerWithInvokeContext);
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), normalRequestBodyCustomSerializerWithInvokeContext);

        RequestBody requestBody = new RequestBody(1, RequestBody.DEFAULT_SYNC_STR);
        InvokeContext invokeContext = new InvokeContext();
        invokeContext.putIfAbsent(NormalRequestBodyCustomSerializerWithInvokeContext.SERIALTYPE_KEY, NormalRequestBodyCustomSerializerWithInvokeContext.SERIALTYPE1_VALUE);
        RPCResponseFuture future = client.invokeWithFuture(address, requestBody, invokeContext, 1000);
        String serverReturn = (String) future.get(1000);
        Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR + "RANDOM", serverReturn);
        Assert.assertTrue(normalRequestBodyCustomSerializerWithInvokeContext.isSerialized());
        Assert.assertTrue(normalRequestBodyCustomSerializerWithInvokeContext.isDeserialized());

        invokeContext.clear();
        invokeContext.putIfAbsent(NormalRequestBodyCustomSerializerWithInvokeContext.SERIALTYPE_KEY, NormalRequestBodyCustomSerializerWithInvokeContext.SERIALTYPE2_VALUE);
        future = client.invokeWithFuture(address, requestBody, invokeContext, 1000);
        serverReturn = (String) future.get(1000);
        Assert.assertEquals(NormalStringCustomSerializerWithInvokeContext.UNIVERSAL_RESP, serverReturn);
        Assert.assertTrue(normalRequestBodyCustomSerializerWithInvokeContext.isSerialized());
        Assert.assertTrue(normalRequestBodyCustomSerializerWithInvokeContext.isDeserialized());
    }

    @Test
    @SuppressWarnings("all")
    public void testInvokeContextCustomSerializer_CALLBACK() throws Exception {
        NormalStringCustomSerializerWithInvokeContext normalStringCustomSerializerWithInvokeContext = new NormalStringCustomSerializerWithInvokeContext();
        NormalRequestBodyCustomSerializerWithInvokeContext normalRequestBodyCustomSerializerWithInvokeContext = new NormalRequestBodyCustomSerializerWithInvokeContext();
        CustomSerializerManager.registerCustomSerializer(String.class.getName(), normalStringCustomSerializerWithInvokeContext);
        CustomSerializerManager.registerCustomSerializer(RequestBody.class.getName(), normalRequestBodyCustomSerializerWithInvokeContext);

        RequestBody requestBody = new RequestBody(1, "hello world!");
        InvokeContext invokeContext = new InvokeContext();
        invokeContext.putIfAbsent(NormalRequestBodyCustomSerializerWithInvokeContext.SERIALTYPE_KEY, NormalRequestBodyCustomSerializerWithInvokeContext.SERIALTYPE1_VALUE);
        final List<Object> resultList = new ArrayList();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        client.invokeWithCallback(address, requestBody, invokeContext, new InvokeCallback() {
            @Override
            public void onResponse(Object result) {
                resultList.clear();
                resultList.add(result);
                countDownLatch.countDown();
            }

            @Override
            public void onException(Throwable e) {

            }

            @Override
            public Executor getExecutor() {
                return null;
            }
        }, 1000);
        countDownLatch.await();
        String serverReturn = (String) resultList.get(0);
        Assert.assertEquals(RequestBody.DEFAULT_SERVER_RETURN_STR + "RANDOM", serverReturn);
        Assert.assertTrue(normalRequestBodyCustomSerializerWithInvokeContext.isSerialized());
        Assert.assertTrue(normalRequestBodyCustomSerializerWithInvokeContext.isDeserialized());

        invokeContext.clear();
        invokeContext.putIfAbsent(NormalRequestBodyCustomSerializerWithInvokeContext.SERIALTYPE_KEY, NormalRequestBodyCustomSerializerWithInvokeContext.SERIALTYPE2_VALUE);
        final CountDownLatch countDownLatch2 = new CountDownLatch(1);
        client.invokeWithCallback(address, requestBody, invokeContext, new InvokeCallback() {
            @Override
            public void onResponse(Object result) {
                resultList.clear();
                resultList.add(result);
                countDownLatch2.countDown();
            }

            @Override
            public void onException(Throwable e) {

            }

            @Override
            public Executor getExecutor() {
                return null;
            }
        }, 1000);
        countDownLatch2.await();
        serverReturn = (String) resultList.get(0);
        Assert.assertEquals(NormalStringCustomSerializerWithInvokeContext.UNIVERSAL_RESP, serverReturn);
        Assert.assertTrue(normalRequestBodyCustomSerializerWithInvokeContext.isSerialized());
        Assert.assertTrue(normalRequestBodyCustomSerializerWithInvokeContext.isDeserialized());
    }
}