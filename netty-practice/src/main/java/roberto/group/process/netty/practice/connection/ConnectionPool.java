/**
 * FileName: ConnectionPool
 * Author:   HuangTaiHong
 * Date:     2019/1/3 9:56
 * Description: Connection pool.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.connection;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import roberto.group.process.netty.practice.scanner.Scannable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 〈一句话功能简述〉<br>
 * 〈Connection pool.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/3
 * @since 1.0.0
 */
@Slf4j
public class ConnectionPool implements Scannable {
    /** connetion select strategy **/
    private ConnectionSelectStrategy strategy;

    /** whether async create connection done */
    private volatile boolean asyncCreationDone = true;

    /** timestamp to record the last time this pool be accessed */
    private volatile long lastAccessTimestamp;

    /** used to save the connection **/
    private CopyOnWriteArrayList<Connection> connections = new CopyOnWriteArrayList<>();

    public ConnectionPool(ConnectionSelectStrategy strategy) {
        this.strategy = strategy;
    }

    public int size() {
        return this.connections.size();
    }

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(connections);
    }

    public void add(Connection connection) {
        markAccess();
        if (connection == null) {
            return;
        } else {
            boolean ifAbsent = this.connections.addIfAbsent(connection);
            if (ifAbsent) {
                connection.increaseRef();
            }
        }
    }

    public Connection get() {
        markAccess();
        return CollectionUtils.isEmpty(connections) ? null : this.strategy.select(Collections.unmodifiableList(this.connections));
    }

    public List<Connection> getAll() {
        markAccess();
        return Collections.unmodifiableList(this.connections);
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

    public void removeAndTryClose(Connection connection) {
        if (connection == null) {
            return;
        } else {
            boolean exist = this.connections.remove(connection);
            if (exist) {
                connection.decreaseRef();
            }

            if (connection.noReference()) {
                connection.close();
            }
        }
    }

    public void removeAllAndTryClose() {
        for (Connection connection : this.connections) {
            removeAndTryClose(connection);
        }
        this.connections.clear();
    }

    @Override
    public void scan() {
        if (CollectionUtils.isNotEmpty(this.connections)) {
            for (Connection connection : connections) {
                if (!connection.isFine()) {
                    log.warn("Remove bad connection when scanning connections of ConnectionPool - {}:{}", connection.getRemoteIP(), connection.getRemotePort());
                    connection.close();
                    this.removeAndTryClose(connection);
                }
            }
        }
    }
}