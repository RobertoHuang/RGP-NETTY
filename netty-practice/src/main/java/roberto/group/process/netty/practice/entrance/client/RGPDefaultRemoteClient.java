/**
 * FileName: RGPDefaultRemoteClient
 * Author:   HuangTaiHong
 * Date:     2019/1/9 16:55
 * Description: Client for RPC.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.entrance.client;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import roberto.group.process.netty.practice.command.factory.impl.RPCCommandFactory;
import roberto.group.process.netty.practice.command.processor.custom.UserProcessor;
import roberto.group.process.netty.practice.command.processor.custom.UserProcessorRegisterHelper;
import roberto.group.process.netty.practice.configuration.configs.impl.AbstractConfigurableInstance;
import roberto.group.process.netty.practice.configuration.container.ConfigTypeEnum;
import roberto.group.process.netty.practice.configuration.support.RPCConfigsSupport;
import roberto.group.process.netty.practice.configuration.switches.impl.GlobalSwitch;
import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.connection.RandomConnectionSelectStrategy;
import roberto.group.process.netty.practice.connection.processor.ConnectionEventListener;
import roberto.group.process.netty.practice.connection.processor.ConnectionEventProcessor;
import roberto.group.process.netty.practice.connection.ConnectionURL;
import roberto.group.process.netty.practice.connection.manager.ReconnectManager;
import roberto.group.process.netty.practice.connection.enums.ConnectionEventTypeEnum;
import roberto.group.process.netty.practice.connection.factory.ConnectionFactory;
import roberto.group.process.netty.practice.connection.factory.DefaultRPCConnectionFactory;
import roberto.group.process.netty.practice.connection.manager.DefaultConnectionManager;
import roberto.group.process.netty.practice.connection.monitor.DefaultConnectionMonitor;
import roberto.group.process.netty.practice.connection.monitor.ConnectionMonitorStrategy;
import roberto.group.process.netty.practice.connection.ConnectionSelectStrategy;
import roberto.group.process.netty.practice.connection.monitor.ScheduledDisconnectStrategy;
import roberto.group.process.netty.practice.exception.RemotingException;
import roberto.group.process.netty.practice.handler.ConnectionEventHandler;
import roberto.group.process.netty.practice.handler.RPCConnectionEventHandler;
import roberto.group.process.netty.practice.remote.help.RemotingAddressParser;
import roberto.group.process.netty.practice.remote.help.impl.RPCAddressParser;
import roberto.group.process.netty.practice.remote.invoke.callback.InvokeCallback;
import roberto.group.process.netty.practice.remote.invoke.context.InvokeContext;
import roberto.group.process.netty.practice.remote.remote.RPCRemoting;
import roberto.group.process.netty.practice.remote.remote.RPCResponseFuture;
import roberto.group.process.netty.practice.remote.remote.client.RPCClientRemoting;
import roberto.group.process.netty.practice.scanner.ScannerTask;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 〈一句话功能简述〉<br>
 * 〈Client for RPC.〉
 *
 * address:
 *      You can use {@link RPCConfigsSupport#CONNECT_TIMEOUT_KEY} to specify connection timeout, time unit is milliseconds, e.g [127.0.0.1:12200?_CONNECTTIMEOUT=3000.
 *      You can use {@link RPCConfigsSupport#CONNECTION_NUM_KEY} to specify connection number for each ip and port, e.g [127.0.0.1:12200?_CONNECTIONNUM=30].
 *      You can use {@link RPCConfigsSupport#CONNECTION_WARMUP_KEY} to specify whether need warmup all connections for the first time you call this method, e.g [127.0.0.1:12200?_CONNECTIONWARMUP=false].
 *
 * connectionURL:
 *      You can use {@link ConnectionURL#setConnectTimeout} to specify connection timeout, time unit is milliseconds.
 *      You can use {@link ConnectionURL#setConnectionNumber} to specify connection number for each ip and port.
 *      You can use {@link ConnectionURL#setConnectionWarmup} to specify whether need warmup all connections for the first time you call this method.
 *
 * @author HuangTaiHong
 * @create 2019/1/9
 * @since 1.0.0
 */
@Slf4j
public class RGPDefaultRemoteClient extends AbstractConfigurableInstance {
    /** RPC remoting */
    protected RPCRemoting remoting;

    @Getter
    @Setter
    /** address parser to get custom args */
    private RemotingAddressParser addressParser;

    /** store user processor KEY:interestClassName Value:UserProcessor **/
    private ConcurrentHashMap<String, UserProcessor<?>> userProcessors = new ConcurrentHashMap();

    private ScannerTask scannerTask;

    /** reconnect manager */
    private ReconnectManager reconnectManager;

    /** connection monitor */
    private DefaultConnectionMonitor connectionMonitor;

    @Setter
    /** connection monitor strategy */
    private ConnectionMonitorStrategy monitorStrategy;

    /** connection select strategy */
    private ConnectionSelectStrategy connectionSelectStrategy = new RandomConnectionSelectStrategy(switches());

    /** connection factory */
    private ConnectionFactory connectionFactory = new DefaultRPCConnectionFactory(userProcessors, this);

    /** connection event handler */
    private ConnectionEventHandler connectionEventHandler = new RPCConnectionEventHandler(switches());

    /** connection event listener */
    private ConnectionEventListener connectionEventListener = new ConnectionEventListener();

    @Getter
    /** connection manager */
    private DefaultConnectionManager connectionManager = new DefaultConnectionManager(connectionSelectStrategy, connectionFactory, connectionEventHandler, connectionEventListener, switches());

    public RGPDefaultRemoteClient() {
        super(ConfigTypeEnum.CLIENT_SIDE);
    }

    public void init() {
        this.addressParser = addressParser == null ? new RPCAddressParser() : addressParser;
        this.connectionManager.setAddressParser(this.addressParser);
        this.connectionManager.init();
        this.remoting = new RPCClientRemoting(new RPCCommandFactory(), this.addressParser, this.connectionManager);

        // start canner task
        this.scannerTask = new ScannerTask();
        scannerTask.add(this.connectionManager);
        scannerTask.start();

        // init connection monitor
        if (switches().isOn(GlobalSwitch.CONN_MONITOR_SWITCH)) {
            ConnectionMonitorStrategy defaultStrategy = new ScheduledDisconnectStrategy();
            monitorStrategy = monitorStrategy == null ? defaultStrategy : monitorStrategy;
            (connectionMonitor = new DefaultConnectionMonitor(monitorStrategy, this.connectionManager)).start();
            log.warn("Switch on connection monitor");
        }

        // init reconnect manager
        if (switches().isOn(GlobalSwitch.CONN_RECONNECT_SWITCH)) {
            reconnectManager = new ReconnectManager(connectionManager);
            connectionEventHandler.setReconnectManager(reconnectManager);
            log.warn("Switch on reconnect manager");
        }
    }


    /**
     * 功能描述: <br>
     * 〈Shutdown.〉
     *
     * RPC client can not be used any more after shutdown.
     * If you need, you should destroy it, and instantiate another one.
     *
     * @author HuangTaiHong
     * @date 2019.01.10 09:48:16
     */
    public void shutdown() {
        this.connectionManager.removeAll();
        log.warn("Close all connections from client side!");
        this.scannerTask.shutdown();
        log.warn("RPC client shutdown! it can not be used any more after shutdown.");
        if (reconnectManager != null) {
            reconnectManager.stop();
        }
        if (connectionMonitor != null) {
            connectionMonitor.destroy();
        }
    }

    /*********************************************网络通信相关方法开始*********************************************/
    public void oneway(final String address, final Object request) throws RemotingException, InterruptedException {
        this.remoting.oneway(address, request, null);
    }

    public void oneway(final String address, final Object request, final InvokeContext invokeContext) throws RemotingException, InterruptedException {
        this.remoting.oneway(address, request, invokeContext);
    }

    public void oneway(final ConnectionURL connectionURL, final Object request) throws RemotingException, InterruptedException {
        this.remoting.oneway(connectionURL, request, null);
    }

    public void oneway(final ConnectionURL connectionURL, final Object request, final InvokeContext invokeContext) throws RemotingException, InterruptedException {
        this.remoting.oneway(connectionURL, request, invokeContext);
    }

    public void oneway(final Connection connection, final Object request) throws RemotingException {
        this.remoting.oneway(connection, request, null);
    }

    public void oneway(final Connection connection, final Object request, final InvokeContext invokeContext) throws RemotingException {
        this.remoting.oneway(connection, request, invokeContext);
    }

    public Object invokeSync(final String address, final Object request, final int timeoutMillis) throws RemotingException, InterruptedException {
        return this.remoting.invokeSync(address, request, null, timeoutMillis);
    }

    public Object invokeSync(final String address, final Object request, final InvokeContext invokeContext, final int timeoutMillis) throws RemotingException, InterruptedException {
        return this.remoting.invokeSync(address, request, invokeContext, timeoutMillis);
    }

    public Object invokeSync(final ConnectionURL connectionURL, final Object request, final int timeoutMillis) throws RemotingException, InterruptedException {
        return this.invokeSync(connectionURL, request, null, timeoutMillis);
    }

    public Object invokeSync(final ConnectionURL connectionURL, final Object request, final InvokeContext invokeContext, final int timeoutMillis) throws RemotingException, InterruptedException {
        return this.remoting.invokeSync(connectionURL, request, invokeContext, timeoutMillis);
    }

    public Object invokeSync(final Connection connection, final Object request, final int timeoutMillis) throws RemotingException, InterruptedException {
        return this.remoting.invokeSync(connection, request, null, timeoutMillis);
    }

    public Object invokeSync(final Connection connection, final Object request, final InvokeContext invokeContext, final int timeoutMillis) throws RemotingException, InterruptedException {
        return this.remoting.invokeSync(connection, request, invokeContext, timeoutMillis);
    }

    public RPCResponseFuture invokeWithFuture(final String address, final Object request, final int timeoutMillis) throws RemotingException, InterruptedException {
        return this.remoting.invokeWithFuture(address, request, null, timeoutMillis);
    }

    public RPCResponseFuture invokeWithFuture(final String address, final Object request, final InvokeContext invokeContext, final int timeoutMillis) throws RemotingException, InterruptedException {
        return this.remoting.invokeWithFuture(address, request, invokeContext, timeoutMillis);
    }

    public RPCResponseFuture invokeWithFuture(final ConnectionURL connectionURL, final Object request, final int timeoutMillis) throws RemotingException, InterruptedException {
        return this.remoting.invokeWithFuture(connectionURL, request, null, timeoutMillis);
    }

    public RPCResponseFuture invokeWithFuture(final ConnectionURL connectionURL, final Object request, final InvokeContext invokeContext, final int timeoutMillis) throws RemotingException, InterruptedException {
        return this.remoting.invokeWithFuture(connectionURL, request, invokeContext, timeoutMillis);
    }

    public RPCResponseFuture invokeWithFuture(final Connection connection, final Object request, int timeoutMillis) throws RemotingException {
        return this.remoting.invokeWithFuture(connection, request, null, timeoutMillis);
    }

    public RPCResponseFuture invokeWithFuture(final Connection connection, final Object request, final InvokeContext invokeContext, int timeoutMillis) throws RemotingException {
        return this.remoting.invokeWithFuture(connection, request, invokeContext, timeoutMillis);
    }

    public void invokeWithCallback(final String address, final Object request, final InvokeCallback invokeCallback, final int timeoutMillis) throws RemotingException, InterruptedException {
        this.remoting.invokeWithCallback(address, request, null, invokeCallback, timeoutMillis);
    }

    public void invokeWithCallback(final String address, final Object request, final InvokeContext invokeContext, final InvokeCallback invokeCallback, final int timeoutMillis) throws RemotingException, InterruptedException {
        this.remoting.invokeWithCallback(address, request, invokeContext, invokeCallback, timeoutMillis);
    }

    public void invokeWithCallback(final ConnectionURL connectionURL, final Object request, final InvokeCallback invokeCallback, final int timeoutMillis) throws RemotingException, InterruptedException {
        this.remoting.invokeWithCallback(connectionURL, request, null, invokeCallback, timeoutMillis);
    }

    public void invokeWithCallback(final ConnectionURL connectionURL, final Object request, final InvokeContext invokeContext, final InvokeCallback invokeCallback, final int timeoutMillis) throws RemotingException, InterruptedException {
        this.remoting.invokeWithCallback(connectionURL, request, invokeContext, invokeCallback, timeoutMillis);
    }

    public void invokeWithCallback(final Connection connection, final Object request, final InvokeCallback invokeCallback, final int timeoutMillis) throws RemotingException {
        this.remoting.invokeWithCallback(connection, request, null, invokeCallback, timeoutMillis);
    }

    public void invokeWithCallback(final Connection connection, final Object request, final InvokeContext invokeContext, final InvokeCallback invokeCallback, final int timeoutMillis) throws RemotingException {
        this.remoting.invokeWithCallback(connection, request, invokeContext, invokeCallback, timeoutMillis);
    }

    /*********************************************网络通信相关方法结束*********************************************/

    public void registerUserProcessor(UserProcessor<?> processor) {
        UserProcessorRegisterHelper.registerUserProcessor(processor, this.userProcessors);
    }

    public void addConnectionEventProcessor(ConnectionEventTypeEnum type, ConnectionEventProcessor processor) {
        this.connectionEventListener.addConnectionEventProcessor(type, processor);
    }

    public Connection createStandaloneConnection(String ip, int port, int connectTimeout) throws RemotingException {
        return this.connectionManager.create(ip, port, connectTimeout);
    }

    public Connection createStandaloneConnection(String address, int connectTimeout) throws RemotingException {
        return this.connectionManager.create(address, connectTimeout);
    }

    public void closeStandaloneConnection(Connection connection) {
        if (null != connection) {
            connection.close();
        }
    }

    public Connection getConnection(String address, int connectTimeout) throws RemotingException, InterruptedException {
        ConnectionURL connectionURL = this.addressParser.parse(address);
        return this.getConnection(connectionURL, connectTimeout);
    }

    public Connection getConnection(ConnectionURL connectionURL, int connectTimeout) throws RemotingException, InterruptedException {
        connectionURL.setConnectTimeout(connectTimeout);
        return this.connectionManager.getAndCreateIfAbsent(connectionURL);
    }

    public Map<String, List<Connection>> getAllManagedConnections() {
        return this.connectionManager.getAll();
    }

    public boolean checkConnection(String address) {
        ConnectionURL connectionURL = this.addressParser.parse(address);
        Connection connection = this.connectionManager.get(connectionURL.getUniqueKey());
        try {
            this.connectionManager.check(connection);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void closeConnection(String address) {
        ConnectionURL connectionURL = this.addressParser.parse(address);
        this.connectionManager.remove(connectionURL.getUniqueKey());
    }

    public void closeConnection(ConnectionURL connectionURL) {
        this.connectionManager.remove(connectionURL.getUniqueKey());
    }

    public void enableConnHeartbeat(String address) {
        ConnectionURL connectionURL = this.addressParser.parse(address);
        this.enableConnHeartbeat(connectionURL);
    }

    public void enableConnHeartbeat(ConnectionURL connectionURL) {
        if (null != connectionURL) {
            this.connectionManager.enableHeartbeat(this.connectionManager.get(connectionURL.getUniqueKey()));
        }
    }

    public void disableConnHeartbeat(String address) {
        ConnectionURL connectionURL = this.addressParser.parse(address);
        this.disableConnHeartbeat(connectionURL);
    }

    public void disableConnHeartbeat(ConnectionURL connectionURL) {
        if (null != connectionURL) {
            this.connectionManager.disableHeartbeat(this.connectionManager.get(connectionURL.getUniqueKey()));
        }
    }

    public void enableReconnectSwitch() {
        this.switches().turnOn(GlobalSwitch.CONN_RECONNECT_SWITCH);
    }

    public void disableReconnectSwith() {
        this.switches().turnOff(GlobalSwitch.CONN_RECONNECT_SWITCH);
    }

    public boolean isReconnectSwitchOn() {
        return this.switches().isOn(GlobalSwitch.CONN_RECONNECT_SWITCH);
    }

    public void enableConnectionMonitorSwitch() {
        this.switches().turnOn(GlobalSwitch.CONN_MONITOR_SWITCH);
    }

    public void disableConnectionMonitorSwitch() {
        this.switches().turnOff(GlobalSwitch.CONN_MONITOR_SWITCH);
    }

    public boolean isConnectionMonitorSwitchOn() {
        return this.switches().isOn(GlobalSwitch.CONN_MONITOR_SWITCH);
    }
}