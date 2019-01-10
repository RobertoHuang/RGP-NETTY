/**
 * FileName: Connection
 * Author:   HuangTaiHong
 * Date:     2019/1/2 10:37
 * Description: 连接实体类
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.AttributeKey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import roberto.group.process.netty.practice.protocol.ProtocolCode;
import roberto.group.process.netty.practice.protocol.impl.RPCProtocol;
import roberto.group.process.netty.practice.remote.invoke.future.InvokeFuture;
import roberto.group.process.netty.practice.utils.ConcurrentHashSet;
import roberto.group.process.netty.practice.utils.RemotingUtil;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 〈一句话功能简述〉<br>
 * 〈连接实体类〉
 *
 * @author HuangTaiHong
 * @create 2019/1/2
 * @since 1.0.0
 */
@Slf4j
public class Connection {
    @Getter
    private Channel channel;

    @Getter
    private ConnectionURL connectionURL;

    private byte version = RPCProtocol.PROTOCOL_VERSION_1;

    private final ConcurrentHashMap<Integer, InvokeFuture> invokeFutureMap = new ConcurrentHashMap(4);

    /** Attribute key for protocol */
    public static final AttributeKey<ProtocolCode> PROTOCOL = AttributeKey.valueOf("protocol");
    /** Attribute key for version */
    public static final AttributeKey<Byte> VERSION = AttributeKey.valueOf("version");
    /** Attribute key for connection */
    public static final AttributeKey<Connection> CONNECTION = AttributeKey.valueOf("connection");
    /** Attribute key for heartbeat switch for each connection */
    public static final AttributeKey<Boolean> HEARTBEAT_SWITCH = AttributeKey.valueOf("heartbeatSwitch");
    /** Attribute key for heartbeat count */
    public static final AttributeKey<Integer> HEARTBEAT_COUNT = AttributeKey.valueOf("heartbeatCount");

    private ProtocolCode protocolCode;

    /** 连接标识符 **/
    private Set<String> poolKeys = new ConcurrentHashSet<>();

    private final ConcurrentHashMap<Integer, String> id2PoolKey = new ConcurrentHashMap(256);

    /** 连接关闭状态 **/
    private AtomicBoolean closed = new AtomicBoolean(false);

    /** 用于记录连接被引用次数 - 主要提供连接回收使用 **/
    private final AtomicInteger referenceCount = new AtomicInteger();

    /** 用户保存连接自定义属性 **/
    private final ConcurrentHashMap<String, Object> attributes = new ConcurrentHashMap();

    public Connection(Channel channel) {
        this.channel = channel;
        this.channel.attr(CONNECTION).set(this);
    }

    public Connection(Channel channel, ConnectionURL connectionURL) {
        this(channel);
        this.connectionURL = connectionURL;
        this.poolKeys.add(connectionURL.getUniqueKey());
    }

    public Connection(Channel channel, ProtocolCode protocolCode, ConnectionURL connectionURL) {
        this(channel, connectionURL);
        this.protocolCode = protocolCode;
        this.init();
    }

    public Connection(Channel channel, ProtocolCode protocolCode, byte version, ConnectionURL connectionURL) {
        this(channel, protocolCode, connectionURL);
        this.version = version;
        this.init();
    }

    /**
     * 功能描述: <br>
     * 〈Initialization.〉
     *
     * @author HuangTaiHong
     * @date 2019.01.08 16:20:35
     */
    private void init() {
        this.channel.attr(PROTOCOL).set(this.protocolCode);
        this.channel.attr(VERSION).set(this.version);
        this.channel.attr(HEARTBEAT_SWITCH).set(true);
        this.channel.attr(HEARTBEAT_COUNT).set(new Integer(0));
    }

    public boolean isFine() {
        return this.channel != null && this.channel.isActive();
    }

    public String getLocalIP() {
        return RemotingUtil.parseLocalIP(this.channel);
    }

    public int getLocalPort() {
        return RemotingUtil.parseLocalPort(this.channel);
    }

    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress) this.channel.localAddress();
    }

    public String getRemoteIP() {
        return RemotingUtil.parseRemoteIP(this.channel);
    }

    public int getRemotePort() {
        return RemotingUtil.parseRemotePort(this.channel);
    }

    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) this.channel.remoteAddress();
    }

    public void increaseRef() {
        this.referenceCount.getAndIncrement();
    }

    public void decreaseRef() {
        this.referenceCount.getAndDecrement();
    }

    public boolean noReference() {
        return this.referenceCount.get() == 0;
    }

    public void clearAttributes() {
        attributes.clear();
    }

    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Object setAttributeIfAbsent(String key, Object value) {
        return attributes.putIfAbsent(key, value);
    }

    public Set<String> getPoolKeys() {
        return Collections.unmodifiableSet(poolKeys);
    }

    public void addPoolKey(String poolKey) {
        poolKeys.add(poolKey);
    }

    public void removePoolKey(String poolKey) {
        poolKeys.remove(poolKey);
    }

    public boolean isInvokeFutureMapFinish() {
        return invokeFutureMap.isEmpty();
    }

    public InvokeFuture getInvokeFuture(int id) {
        return this.invokeFutureMap.get(id);
    }

    public InvokeFuture addInvokeFuture(InvokeFuture future) {
        return this.invokeFutureMap.putIfAbsent(future.invokeId(), future);
    }

    public InvokeFuture removeInvokeFuture(int invokeId) {
        return this.invokeFutureMap.remove(invokeId);
    }

    public void addIdPoolKeyMapping(Integer id, String poolKey) {
        this.id2PoolKey.put(id, poolKey);
    }

    public String removeIdPoolKeyMapping(Integer id) {
        return this.id2PoolKey.remove(id);
    }
    /**
     * 功能描述: <br>
     * 〈Do something when closing.〉
     *
     * @author HuangTaiHong
     * @date 2019.01.03 18:53:40
     */
    public void onClose() {
        Iterator<Map.Entry<Integer, InvokeFuture>> iterator = invokeFutureMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, InvokeFuture> entry = iterator.next();
            iterator.remove();
            InvokeFuture future = entry.getValue();
            if (future != null) {
                future.putResponse(future.createConnectionClosedResponse(this.getRemoteAddress()));
                future.cancelTimeout();
                future.tryAsyncExecuteInvokeCallbackAbnormally();
            }
        }
    }

    /**
     * 功能描述: <br>
     * 〈Close the connection.〉
     *
     * @author HuangTaiHong
     * @date 2019.01.03 10:19:47
     */
    public void close() {
        if (closed.compareAndSet(false, true)) {
            try {
                if (this.getChannel() != null) {
                    this.getChannel().close().addListener((ChannelFutureListener) future -> log.info("Close the connection to remote address={}, result={}, cause={}", RemotingUtil.parseRemoteAddress(Connection.this.getChannel()), future.isSuccess(), future.cause()));
                }
            } catch (Exception e) {
                log.warn("Exception caught when closing connection {}", RemotingUtil.parseRemoteAddress(Connection.this.getChannel()), e);
            }
        }
    }
}