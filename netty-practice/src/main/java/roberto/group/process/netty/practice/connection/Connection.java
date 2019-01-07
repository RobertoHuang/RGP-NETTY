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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import roberto.group.process.netty.practice.protocol.ProtocolCode;
import roberto.group.process.netty.practice.remote.invoke.future.InvokeFuture;
import roberto.group.process.netty.practice.utils.ConcurrentHashSet;
import roberto.group.process.netty.practice.utils.RemotingUtil;

import java.net.InetSocketAddress;
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
public class Connection {
    private static final Logger LOGGER = LoggerFactory.getLogger(Connection.class);

    @Getter
    private Channel channel;

    @Getter
    private ConnectionURL connectionURL;

    private final ConcurrentHashMap<Integer, InvokeFuture> invokeFutureMap = new ConcurrentHashMap<Integer, InvokeFuture>(4);


    public static final AttributeKey<Byte> VERSION = AttributeKey.valueOf("version");
    public static final AttributeKey<ProtocolCode> PROTOCOL = AttributeKey.valueOf("protocol");
    public static final AttributeKey<Connection> CONNECTION = AttributeKey.valueOf("connection");

    /** 连接标识符 **/
    private Set<String> poolKeys = new ConcurrentHashSet<>();

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

    public String getLocalIP() {
        return RemotingUtil.parseLocalIP(this.channel);
    }

    public int getLocalPort() {
        return RemotingUtil.parseLocalPort(this.channel);
    }

    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) this.channel.remoteAddress();
    }

    public String getRemoteIP() {
        return RemotingUtil.parseRemoteIP(this.channel);
    }

    public int getRemotePort() {
        return RemotingUtil.parseRemotePort(this.channel);
    }

    public void increaseRef() {
        this.referenceCount.getAndIncrement();
    }

    public void decreaseRef() {
        this.referenceCount.getAndDecrement();
    }

    public boolean noRef() {
        return this.referenceCount.get() == 0;
    }

    public boolean isFine() {
        return this.channel != null && this.channel.isActive();
    }

    public InvokeFuture addInvokeFuture(InvokeFuture future) {
        return this.invokeFutureMap.putIfAbsent(future.invokeId(), future);
    }

    public InvokeFuture removeInvokeFuture(int invokeId) {
        return this.invokeFutureMap.remove(invokeId);
    }

    /** 连接自定义属性START **/
    public void clearAttributes() {
        attributes.clear();
    }

    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public Object setAttributeIfAbsent(String key, Object value) {
        return attributes.putIfAbsent(key, value);
    }
    /** 连接自定义属性END **/


    /**
     * 功能描述: <br>
     * 〈连接被关闭〉
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
     * 〈关闭连接〉
     *
     * @author HuangTaiHong
     * @date 2019.01.03 10:19:47
     */
    public void close() {
        if (closed.compareAndSet(false, true)) {
            try {
                if (this.getChannel() != null) {
                    this.getChannel().close().addListener((ChannelFutureListener) future -> LOGGER.info("Close the connection to remote address={}, result={}, cause={}", RemotingUtil.parseRemoteAddress(Connection.this.getChannel()), future.isSuccess(), future.cause()));
                }
            } catch (Exception e) {
                LOGGER.warn("Exception caught when closing connection {}", RemotingUtil.parseRemoteAddress(Connection.this.getChannel()), e);
            }
        }
    }
}