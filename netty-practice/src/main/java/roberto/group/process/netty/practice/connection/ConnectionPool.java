/**
 * FileName: ConnectionPool
 * Author:   HuangTaiHong
 * Date:     2019/1/3 9:56
 * Description: 连接池
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.connection;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import roberto.group.process.netty.practice.scanner.Scannable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 〈一句话功能简述〉<br>
 * 〈连接池〉
 *
 * @author HuangTaiHong
 * @create 2019/1/3
 * @since 1.0.0
 */
public class ConnectionPool implements Scannable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPool.class);

    /** 连接选择策略 **/
    private ConnectionSelectStrategy strategy;

    /** 用于保存连接 **/
    private CopyOnWriteArrayList<Connection> connections = new CopyOnWriteArrayList<Connection>();

    /** 用于记录最后一次访问该连接池时间 **/
    private volatile long lastAccessTimestamp;

    /** 用于标记异步创建连接是否完成 **/
    private volatile boolean asyncCreationDone = true;

    public ConnectionPool(ConnectionSelectStrategy strategy) {
        this.strategy = strategy;
    }

    public Connection get() {
        markAccess();
        if (CollectionUtils.isEmpty(connections)) {
            return this.strategy.select(Collections.unmodifiableList(this.connections));
        } else {
            return null;
        }
    }

    public List<Connection> getAll() {
        markAccess();
        return Collections.unmodifiableList(this.connections);
    }

    public int size() {
        return this.connections.size();
    }

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(connections);
    }

    public void add(Connection connection) {
        markAccess();
        if (null == connection) {
            return;
        }
        boolean res = this.connections.addIfAbsent(connection);
        if (res) {
            connection.increaseRef();
        }
    }

    /**
     * 功能描述: <br>
     * 〈移除并尝试关闭连接 - 是否关闭取决于连接被引用次数〉
     *
     * @param connection
     * @author HuangTaiHong
     * @date 2019.01.03 10:27:17
     */
    public void removeAndTryClose(Connection connection) {
        if (null == connection) {
            return;
        }
        boolean res = this.connections.remove(connection);
        if (res) {
            connection.decreaseRef();
        }
        if (connection.noRef()) {
            connection.close();
        }
    }

    public boolean contains(Connection connection) {
        return this.connections.contains(connection);
    }

    private void markAccess() {
        this.lastAccessTimestamp = System.currentTimeMillis();
    }

    public long getLastAccessTimestamp() {
        return this.lastAccessTimestamp;
    }

    public void markAsyncCreationStart() {
        this.asyncCreationDone = false;
    }

    public void markAsyncCreationDone() {
        this.asyncCreationDone = true;
    }

    public boolean isAsyncCreationDone() {
        return this.asyncCreationDone;
    }

    public void scan() {
        if (CollectionUtils.isNotEmpty(this.connections)) {
            for (Connection connection : connections) {
                if (!connection.isFine()) {
                    LOGGER.warn("Remove bad connection when scanning connections of ConnectionPool - {}:{}", connection.getRemoteIP(), connection.getRemotePort());
                    connection.close();
                    this.removeAndTryClose(connection);
                }
            }
        }
    }
}