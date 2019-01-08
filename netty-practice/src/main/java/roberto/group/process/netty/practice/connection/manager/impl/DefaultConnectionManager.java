/**
 * FileName: DefaultConnectionManager
 * Author:   HuangTaiHong
 * Date:     2019/1/4 15:56
 * Description: default connection manager
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.connection.manager.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import roberto.group.process.netty.practice.configuration.configs.ConfigManager;
import roberto.group.process.netty.practice.configuration.switches.impl.GlobalSwitch;
import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.connection.ConnectionEventListener;
import roberto.group.process.netty.practice.connection.ConnectionURL;
import roberto.group.process.netty.practice.connection.factory.ConnectionFactory;
import roberto.group.process.netty.practice.connection.manager.ConnectionManager;
import roberto.group.process.netty.practice.connection.manager.HeartbeatStatusManager;
import roberto.group.process.netty.practice.connection.ConnectionPool;
import roberto.group.process.netty.practice.connection.strategy.ConnectionSelectStrategy;
import roberto.group.process.netty.practice.connection.strategy.impl.RandomSelectStrategy;
import roberto.group.process.netty.practice.exception.RemotingException;
import roberto.group.process.netty.practice.handler.ConnectionEventHandler;
import roberto.group.process.netty.practice.remote.help.RemotingAddressParser;
import roberto.group.process.netty.practice.thread.NamedThreadFactory;
import roberto.group.process.netty.practice.utils.FutureTaskUtil;
import roberto.group.process.netty.practice.utils.RunStateRecordedFutureTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 〈一句话功能简述〉<br>
 * 〈default connection manager〉
 *
 * @author HuangTaiHong
 * @create 2019/1/4
 * @since 1.0.0
 */
@Slf4j
public class DefaultConnectionManager implements ConnectionManager, HeartbeatStatusManager {
    /** switch status **/
    private GlobalSwitch globalSwitch;
    @Getter
    @Setter
    /** address parser **/
    protected RemotingAddressParser addressParser;

    /** executor initialie status **/
    private volatile boolean executorInitialized;
    /** executor to create connections in async way **/
    private Executor asyncCreateConnectionExecutor;
    /** min connectionPool size for asyncCreateConnectionExecutor **/
    private int minPoolSize = ConfigManager.conn_create_tp_min_size();
    /** max connectionPool size for asyncCreateConnectionExecutor **/
    private int maxPoolSize = ConfigManager.conn_create_tp_max_size();
    /** queue size for asyncCreateConnectionExecutor **/
    private int queueSize = ConfigManager.conn_create_tp_queue_size();
    /** keep alive time for asyncCreateConnectionExecutor **/
    private long keepAliveTime = ConfigManager.conn_create_tp_keepalive();

    /** default retry times when falied to get result of FutureTask **/
    private static final int DEFAULT_RETRY_TIMES = 2;
    /** default expire time to remove connection connectionPool, time unit: milliseconds **/
    private static final int DEFAULT_EXPIRE_TIME = 10 * 60 * 1000;

    /** heal connection tasks **/
    protected ConcurrentHashMap<String, FutureTask<Integer>> healTasks;
    /** connection connectionPool initialize tasks **/
    protected ConcurrentHashMap<String, RunStateRecordedFutureTask<ConnectionPool>> connectionTasks;

    @Getter
    @Setter
    /** connection factory **/
    protected ConnectionFactory connectionFactory;
    @Getter
    @Setter
    /** connection connectionPool select strategy **/
    protected ConnectionSelectStrategy connectionSelectStrategy;
    @Getter
    @Setter
    /** connection event handler **/
    protected ConnectionEventHandler connectionEventHandler;
    @Getter
    @Setter
    /** connection event listener **/
    protected ConnectionEventListener connectionEventListener;

    public DefaultConnectionManager() {
        this.healTasks = new ConcurrentHashMap();
        this.connectionTasks = new ConcurrentHashMap();
        this.connectionSelectStrategy = new RandomSelectStrategy(globalSwitch);
    }

    public DefaultConnectionManager(ConnectionSelectStrategy connectionSelectStrategy) {
        this();
        this.connectionSelectStrategy = connectionSelectStrategy;
    }

    public DefaultConnectionManager(ConnectionSelectStrategy connectionSelectStrategy, ConnectionFactory connectionFactory) {
        this(connectionSelectStrategy);
        this.connectionFactory = connectionFactory;
    }

    public DefaultConnectionManager(ConnectionFactory connectionFactory, RemotingAddressParser addressParser, ConnectionEventHandler connectionEventHandler) {
        this(new RandomSelectStrategy(), connectionFactory);
        this.addressParser = addressParser;
        this.connectionEventHandler = connectionEventHandler;
    }

    public DefaultConnectionManager(ConnectionSelectStrategy connectionSelectStrategy, ConnectionFactory connectionFactory, ConnectionEventHandler connectionEventHandler, ConnectionEventListener connectionEventListener) {
        this(connectionSelectStrategy, connectionFactory);
        this.connectionEventHandler = connectionEventHandler;
        this.connectionEventListener = connectionEventListener;
    }

    public DefaultConnectionManager(ConnectionSelectStrategy connectionSelectStrategy, ConnectionFactory connctionFactory, ConnectionEventHandler connectionEventHandler, ConnectionEventListener connectionEventListener, GlobalSwitch globalSwitch) {
        this(connectionSelectStrategy, connctionFactory, connectionEventHandler, connectionEventListener);
        this.globalSwitch = globalSwitch;
    }

    @Override
    public void init() {
        this.connectionEventHandler.setConnectionManager(this);
        this.connectionEventHandler.setConnectionEventListener(connectionEventListener);
        this.connectionFactory.init(connectionEventHandler);
    }

    @Override
    public void add(Connection connection) {
        Set<String> poolKeys = connection.getPoolKeys();
        for (String poolKey : poolKeys) {
            this.add(poolKey, connection);
        }
    }

    @Override
    public void add(String poolKey, Connection connection) {
        ConnectionPool connectionPool = null;
        try {
            // get or create an empty connection connectionPool
            connectionPool = this.getConnectionPoolAndCreateIfAbsent(poolKey, new ConnectionPoolCall());
        } catch (Exception e) {
            // should not reach here.
            log.error("[NOTIFYME] Exception occurred when getOrCreateIfAbsent an empty ConnectionPool!", e);
        }
        if (connectionPool != null) {
            connectionPool.add(connection);
        } else {
            // should not reach here.
            log.error("[NOTIFYME] Connection connectionPool NULL!");
        }
    }

    @Override
    public Connection get(String poolKey) {
        ConnectionPool connectionPool = this.getConnectionPool(this.connectionTasks.get(poolKey));
        return connectionPool == null ? null : connectionPool.get();
    }

    @Override
    public List<Connection> getAll(String poolKey) {
        ConnectionPool connectionPool = this.getConnectionPool(this.connectionTasks.get(poolKey));
        return connectionPool == null ? new ArrayList<>() : connectionPool.getAll();
    }

    @Override
    public Map<String, List<Connection>> getAll() {
        Map<String, List<Connection>> allConnections = new HashMap<>();
        this.connectionTasks.entrySet().forEach(entry -> {
            ConnectionPool connectionPool = FutureTaskUtil.getFutureTaskResult(entry.getValue(), log);
            if (null != connectionPool) {
                allConnections.put(entry.getKey(), connectionPool.getAll());
            }
        });
        return allConnections;
    }

    @Override
    public void remove(Connection connection) {
        if (connection == null) {
            return;
        } else {
            Set<String> poolKeys = connection.getPoolKeys();
            if (null == poolKeys || poolKeys.isEmpty()) {
                connection.close();
                log.warn("Remove and close a standalone connection.");
            } else {
                poolKeys.forEach(poolKey -> this.remove(poolKey, connection));
            }
        }
    }

    @Override
    public void remove(String poolKey, Connection connection) {
        if (null == connection || StringUtils.isBlank(poolKey)) {
            return;
        } else {
            ConnectionPool connectionPool = this.getConnectionPool(this.connectionTasks.get(poolKey));
            if (connectionPool == null) {
                connection.close();
                log.warn("Remove and close a standalone connection.");
            } else {
                connectionPool.removeAndTryClose(connection);
                if (connectionPool.isEmpty()) {
                    this.removeTask(poolKey);
                    log.warn("Remove and close the last connection in ConnectionPool with poolKey {}", poolKey);
                } else {
                    log.warn("Remove and close a connection in ConnectionPool with poolKey {}, {} connections left.", poolKey, connectionPool.size());
                }
            }
        }
    }

    @Override
    public void remove(String poolKey) {
        if (StringUtils.isBlank(poolKey)) {
            return;
        } else {
            RunStateRecordedFutureTask<ConnectionPool> task = this.connectionTasks.remove(poolKey);
            if (null != task) {
                ConnectionPool connectionPool = this.getConnectionPool(task);
                if (null != connectionPool) {
                    connectionPool.removeAllAndTryClose();
                    log.warn("Remove and close all connections in ConnectionPool of poolKey={}", poolKey);
                }
            }
        }
    }

    @Override
    public void removeAll() {
        if (MapUtils.isEmpty(this.connectionTasks)) {
            return;
        } else {
            Iterator<String> iterator = this.connectionTasks.keySet().iterator();
            while (iterator.hasNext()) {
                String poolKey = iterator.next();
                this.removeTask(poolKey);
                iterator.remove();
            }
            log.warn("All connection connectionPool and connections have been removed!");
        }
    }

    @Override
    public void check(Connection connection) throws RemotingException {
        if (connection == null) {
            throw new RemotingException("Connection is null when do check!");
        }
        if (connection.getChannel() == null || !connection.getChannel().isActive()) {
            this.remove(connection);
            throw new RemotingException("Check connection failed for address: " + connection.getConnectionURL());
        }
        if (!connection.getChannel().isWritable()) {
            // No remove. Most of the time it is unwritable temporarily.
            throw new RemotingException("Check connection failed for address: " + connection.getConnectionURL() + ", maybe write overflow!");
        }
    }

    @Override
    public int count(String poolKey) {
        if (StringUtils.isBlank(poolKey)) {
            return 0;
        } else {
            ConnectionPool connectionPool;
            return (connectionPool = this.getConnectionPool(this.connectionTasks.get(poolKey))) == null ? 0 : connectionPool.size();
        }
    }

    @Override
    public Connection create(ConnectionURL connectionURL) throws RemotingException {
        Connection connection;
        try {
            connection = this.connectionFactory.createConnection(connectionURL);
        } catch (Exception e) {
            throw new RemotingException("Create connection failed. The address is " + connectionURL.getOriginUrl(), e);
        }
        return connection;
    }

    @Override
    public Connection create(String address, int connectTimeout) throws RemotingException {
        ConnectionURL connectionURL = this.addressParser.parse(address);
        connectionURL.setConnectTimeout(connectTimeout);
        return create(connectionURL);
    }

    @Override
    public Connection create(String ip, int port, int connectTimeout) throws RemotingException {
        Connection connection;
        try {
            connection = this.connectionFactory.createConnection(ip, port, connectTimeout);
        } catch (Exception e) {
            throw new RemotingException("Create connection failed. The address is " + ip + ":" + port, e);
        }
        return connection;
    }

    @Override
    public Connection getAndCreateIfAbsent(ConnectionURL connectionURL) throws InterruptedException, RemotingException {
        // get and create a connection pool with initialized connections.
        ConnectionPool connectionPool = this.getConnectionPoolAndCreateIfAbsent(connectionURL.getUniqueKey(), new ConnectionPoolCall(connectionURL));
        if (null != connectionPool) {
            return connectionPool.get();
        } else {
            log.error("[NOTIFYME] bug detected! pool here must not be null!");
            return null;
        }
    }

    @Override
    public void createConnectionAndHealIfNeed(ConnectionURL connectionURL) throws InterruptedException, RemotingException {
        ConnectionPool connectionPool = this.getConnectionPoolAndCreateIfAbsent(connectionURL.getUniqueKey(), new ConnectionPoolCall(connectionURL));
        if (null != connectionPool) {
            healIfNeed(connectionPool, connectionURL);
        } else {
            log.error("[NOTIFYME] bug detected! connectionPool here must not be null!");
        }
    }

    @Override
    public void scan() {
        if (null != this.connectionTasks && !this.connectionTasks.isEmpty()) {
            Iterator<String> iterator = this.connectionTasks.keySet().iterator();
            iterator.forEachRemaining(poolKey -> {
                ConnectionPool connectionPool = this.getConnectionPool(this.connectionTasks.get(poolKey));
                if (connectionPool != null) {
                    connectionPool.scan();
                    if (connectionPool.isEmpty()) {
                        if ((System.currentTimeMillis() - connectionPool.getLastAccessTimestamp()) > DEFAULT_EXPIRE_TIME) {
                            iterator.remove();
                            log.warn("Remove expired pool task of poolKey {} which is empty.", poolKey);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void disableHeartbeat(Connection connection) {
        if (null != connection) {
            connection.getChannel().attr(Connection.HEARTBEAT_SWITCH).set(false);
        }
    }

    @Override
    public void enableHeartbeat(Connection connection) {
        if (null != connection) {
            connection.getChannel().attr(Connection.HEARTBEAT_SWITCH).set(true);
        }
    }

    private ConnectionPool getConnectionPool(RunStateRecordedFutureTask<ConnectionPool> task) {
        return FutureTaskUtil.getFutureTaskResult(task, log);
    }

    /**
     * 功能描述: <br>
     * 〈remove task and remove all connections.〉
     *
     * @param poolKey
     * @author HuangTaiHong
     * @date 2019.01.08 15:48:50
     */
    private void removeTask(String poolKey) {
        RunStateRecordedFutureTask<ConnectionPool> task = this.connectionTasks.remove(poolKey);
        if (null != task) {
            ConnectionPool connectionPool = FutureTaskUtil.getFutureTaskResult(task, log);
            if (null != connectionPool) {
                connectionPool.removeAllAndTryClose();
            }
        }
    }

    /**
     * 功能描述: <br>
     * 〈Get the mapping instance of ConnectionPool with the specified poolKey, or create one if there is none mapping in connectionTasks.〉
     *
     * @param poolKey
     * @param callable
     * @return > roberto.group.process.netty.practice.connection.ConnectionPool
     * @throws RemotingException
     * @throws InterruptedException
     * @author HuangTaiHong
     * @date 2019.01.08 15:47:58
     */
    private ConnectionPool getConnectionPoolAndCreateIfAbsent(String poolKey, Callable<ConnectionPool> callable) throws RemotingException, InterruptedException {
        int timesOfInterrupt = 0;
        int timesOfResultNull = 0;
        ConnectionPool pool = null;
        int retry = DEFAULT_RETRY_TIMES;
        RunStateRecordedFutureTask<ConnectionPool> initialTask;
        for (int i = 0; (i < retry) && (pool == null); ++i) {
            initialTask = this.connectionTasks.get(poolKey);
            if (null == initialTask) {
                initialTask = new RunStateRecordedFutureTask<>(callable);
                initialTask = this.connectionTasks.putIfAbsent(poolKey, initialTask);
                if (null == initialTask) {
                    initialTask = this.connectionTasks.get(poolKey);
                    initialTask.run();
                }
            }

            try {
                pool = initialTask.get();
                if (null == pool) {
                    if (i + 1 < retry) {
                        timesOfResultNull++;
                        continue;
                    }
                    this.connectionTasks.remove(poolKey);
                    String errMsg = "Get future task result null for poolKey [" + poolKey + "] after [" + (timesOfResultNull + 1) + "] times try.";
                    throw new RemotingException(errMsg);
                }
            } catch (InterruptedException e) {
                if (i + 1 < retry) {
                    timesOfInterrupt++;
                    continue;
                }
                this.connectionTasks.remove(poolKey);
                log.warn("Future task of poolKey {} interrupted {} times. InterruptedException thrown and stop retry.", poolKey, (timesOfInterrupt + 1), e);
                throw e;
            } catch (ExecutionException e) {
                // DO NOT retry if ExecutionException occurred
                this.connectionTasks.remove(poolKey);
                Throwable cause = e.getCause();
                if (cause instanceof RemotingException) {
                    throw (RemotingException) cause;
                } else {
                    FutureTaskUtil.launderThrowable(cause);
                }
            }
        }
        return pool;
    }

    /**
     * 功能描述: <br>
     * 〈do create connections〉
     *
     * whether the warm up connection is required by the ConnectionURL.connectionWarmup property
     *
     * @param connectionURL
     * @param connectionPool
     * @param taskName
     * @param syncCreateNumWhenNotWarmup
     * @throws RemotingException
     * @author HuangTaiHong
     * @date 2019.01.08 11:34:05
     */
    private void doCreate(final ConnectionURL connectionURL, final ConnectionPool connectionPool, final String taskName, final int syncCreateNumWhenNotWarmup) throws RemotingException {
        final int actualNum = connectionPool.size();
        final int expectNum = connectionURL.getConnectionNumber();
        if (actualNum < expectNum) {
            log.debug("actual num {}, expect num {}, task name {}", actualNum, expectNum, taskName);
            if (connectionURL.isConnectionWarmup()) {
                for (int i = actualNum; i < expectNum; ++i) {
                    Connection connection = create(connectionURL);
                    connectionPool.add(connection);
                }
            } else {
                if (syncCreateNumWhenNotWarmup < 0 || syncCreateNumWhenNotWarmup > connectionURL.getConnectionNumber()) {
                    throw new IllegalArgumentException("sync create number when not warmup should be [0," + connectionURL.getConnectionNumber() + "]");
                }
                // create connection in sync way
                if (syncCreateNumWhenNotWarmup > 0) {
                    for (int i = 0; i < syncCreateNumWhenNotWarmup; ++i) {
                        Connection connection = create(connectionURL);
                        connectionPool.add(connection);
                    }
                    if (syncCreateNumWhenNotWarmup == connectionURL.getConnectionNumber()) {
                        return;
                    }
                }
                // initialize executor in lazy way
                initializeExecutor();
                // mark the start of async
                connectionPool.markAsyncCreationStart();
                try {
                    this.asyncCreateConnectionExecutor.execute(() -> {
                        try {
                            for (int i = connectionPool.size(); i < connectionURL.getConnectionNumber(); ++i) {
                                Connection connection = null;
                                try {
                                    connection = create(connectionURL);
                                } catch (RemotingException e) {
                                    log.error("Exception occurred in async create connection thread for {}, taskName {}", connectionURL.getUniqueKey(), taskName, e);
                                }
                                connectionPool.add(connection);
                            }
                        } finally {
                            // mark the end of async
                            connectionPool.markAsyncCreationDone();
                        }
                    });
                } catch (RejectedExecutionException e) {
                    // mark the end of async when reject
                    connectionPool.markAsyncCreationDone();
                    throw e;
                }
            }
        }
    }

    /**
     * 功能描述: <br>
     * 〈initialize executor〉
     *
     * @author HuangTaiHong
     * @date 2019.01.08 11:32:01
     */
    private void initializeExecutor() {
        if (!this.executorInitialized) {
            this.executorInitialized = true;
            this.asyncCreateConnectionExecutor = new ThreadPoolExecutor(minPoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new ArrayBlockingQueue(queueSize), new NamedThreadFactory("bolt-conn-warmup-executor", true));
        }
    }

    /**
     * 功能描述: <br>
     * 〈execute heal connection tasks if the actual number of connections in pool is less than expected.〉
     *
     * @param connectionPool
     * @param connectionURL
     * @throws RemotingException
     * @throws InterruptedException
     * @author HuangTaiHong
     * @date 2019.01.08 15:49:01
     */
    private void healIfNeed(ConnectionPool connectionPool, ConnectionURL connectionURL) throws RemotingException, InterruptedException {
        String poolKey = connectionURL.getUniqueKey();
        // only when async creating connections done
        // and the actual size of connections less than expected, the healing task can be run.
        if (connectionPool.isAsyncCreationDone() && connectionPool.size() < connectionURL.getConnectionNumber()) {
            FutureTask<Integer> task = this.healTasks.get(poolKey);
            if (null == task) {
                task = new FutureTask<>(new HealConnectionCall(connectionURL, connectionPool));
                task = this.healTasks.putIfAbsent(poolKey, task);
                if (null == task) {
                    task = this.healTasks.get(poolKey);
                    task.run();
                }
            }
            try {
                int numAfterHeal = task.get();
                log.debug("[NOTIFYME] - conn num after heal {}, expected {}, warmup {}", numAfterHeal, connectionURL.getConnectionNumber(), connectionURL.isConnectionWarmup());
            } catch (InterruptedException e) {
                this.healTasks.remove(poolKey);
                throw e;
            } catch (ExecutionException e) {
                this.healTasks.remove(poolKey);
                Throwable cause = e.getCause();
                if (cause instanceof RemotingException) {
                    throw (RemotingException) cause;
                } else {
                    FutureTaskUtil.launderThrowable(cause);
                }
            }
            // heal task is one-off, remove from cache directly after run
            this.healTasks.remove(poolKey);
        }
    }

    public class ConnectionPoolCall implements Callable<ConnectionPool> {
        private ConnectionURL connectionURL;
        private boolean whetherInitConnection;

        public ConnectionPoolCall() {
            this.whetherInitConnection = false;
        }

        public ConnectionPoolCall(ConnectionURL connectionURL) {
            this.whetherInitConnection = true;
            this.connectionURL = connectionURL;
        }

        @Override
        public ConnectionPool call() throws Exception {
            final ConnectionPool pool = new ConnectionPool(connectionSelectStrategy);
            if (whetherInitConnection) {
                try {
                    doCreate(this.connectionURL, pool, this.getClass().getSimpleName(), 1);
                } catch (Exception e) {
                    pool.removeAllAndTryClose();
                    throw e;
                }
            }
            return pool;
        }
    }

    @AllArgsConstructor
    private class HealConnectionCall implements Callable<Integer> {
        private ConnectionURL connectionURL;
        private ConnectionPool connectionPool;

        @Override
        public Integer call() throws Exception {
            doCreate(this.connectionURL, this.connectionPool, this.getClass().getSimpleName(), 0);
            return this.connectionPool.size();
        }
    }
}