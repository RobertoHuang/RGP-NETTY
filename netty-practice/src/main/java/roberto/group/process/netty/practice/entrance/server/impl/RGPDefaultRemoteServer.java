/**
 * FileName: RGPDefaultRemoteServer
 * Author:   HuangTaiHong
 * Date:     2019/1/2 10:18
 * Description: Server for RPC.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.entrance.server.impl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import roberto.group.process.netty.practice.codec.ProtocolCodeBasedDecoder;
import roberto.group.process.netty.practice.codec.ProtocolCodeBasedEncoder;
import roberto.group.process.netty.practice.command.code.RemoteCommandCode;
import roberto.group.process.netty.practice.command.factory.impl.RPCCommandFactory;
import roberto.group.process.netty.practice.command.processor.custom.UserProcessor;
import roberto.group.process.netty.practice.command.processor.custom.UserProcessorRegisterHelper;
import roberto.group.process.netty.practice.command.processor.processor.RemotingProcessor;
import roberto.group.process.netty.practice.configuration.manager.ConfigManager;
import roberto.group.process.netty.practice.configuration.switches.impl.GlobalSwitch;
import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.connection.processor.ConnectionEventListener;
import roberto.group.process.netty.practice.connection.processor.ConnectionEventProcessor;
import roberto.group.process.netty.practice.connection.ConnectionURL;
import roberto.group.process.netty.practice.connection.enums.ConnectionEventTypeEnum;
import roberto.group.process.netty.practice.connection.manager.DefaultConnectionManager;
import roberto.group.process.netty.practice.connection.RandomConnectionSelectStrategy;
import roberto.group.process.netty.practice.exception.RemotingException;
import roberto.group.process.netty.practice.handler.AcceptorIdleStateTrigger;
import roberto.group.process.netty.practice.handler.ConnectionEventHandler;
import roberto.group.process.netty.practice.handler.RPCBusinessEventHandler;
import roberto.group.process.netty.practice.handler.RPCConnectionEventHandler;
import roberto.group.process.netty.practice.protocol.ProtocolCode;
import roberto.group.process.netty.practice.protocol.ProtocolManager;
import roberto.group.process.netty.practice.protocol.impl.RPCProtocol;
import roberto.group.process.netty.practice.remote.help.RemotingAddressParser;
import roberto.group.process.netty.practice.remote.help.impl.RPCAddressParser;
import roberto.group.process.netty.practice.remote.invoke.callback.InvokeCallback;
import roberto.group.process.netty.practice.context.InvokeContext;
import roberto.group.process.netty.practice.remote.remote.RPCRemoting;
import roberto.group.process.netty.practice.remote.remote.RPCResponseFuture;
import roberto.group.process.netty.practice.remote.remote.server.RPCServerRemoting;
import roberto.group.process.netty.practice.thread.NamedThreadFactory;
import roberto.group.process.netty.practice.utils.NettyEventLoopUtil;
import roberto.group.process.netty.practice.utils.RemotingAddressUtil;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 〈一句话功能简述〉<br>
 * 〈Server for RPC.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/2
 * @since 1.0.0
 */
@Slf4j
public class RGPDefaultRemoteServer extends AbstractRemotingServer {
    /** channelFuture */
    private ChannelFuture channelFuture;

    /** ServerBootstrap **/
    private ServerBootstrap serverBootstrap;

    /** boss event loop group, boss group should not be daemon, need shutdown manually **/
    private final EventLoopGroup bossGroup = NettyEventLoopUtil.newEventLoopGroup(1, new NamedThreadFactory("bolt-netty-server-boss", false));

    /** worker event loop group. Reuse I/O worker threads between RPC servers. **/
    private static final EventLoopGroup workerGroup = NettyEventLoopUtil.newEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2, new NamedThreadFactory("bolt-netty-server-worker", true));

    /** RPC remoting */
    protected RPCRemoting remoting;

    /** address parser to get custom args **/
    private RemotingAddressParser addressParser;

    /** connection manager */
    private DefaultConnectionManager connectionManager;

    /** connection event handler */
    private ConnectionEventHandler connectionEventHandler;

    /** connection event listener */
    private ConnectionEventListener connectionEventListener = new ConnectionEventListener();

    /** user processors of RPC server */
    private ConcurrentHashMap<String, UserProcessor<?>> userProcessors = new ConcurrentHashMap(4);

    static {
        if (workerGroup instanceof NioEventLoopGroup) {
            ((NioEventLoopGroup) workerGroup).setIoRatio(ConfigManager.netty_io_ratio());
        } else if (workerGroup instanceof EpollEventLoopGroup) {
            ((EpollEventLoopGroup) workerGroup).setIoRatio(ConfigManager.netty_io_ratio());
        }
    }

    public RGPDefaultRemoteServer(int port) {
        this(port, false);
    }

    public RGPDefaultRemoteServer(String ip, int port) {
        this(ip, port, false);
    }

    public RGPDefaultRemoteServer(int port, boolean manageConnection) {
        super(port);
        if (manageConnection) {
            this.switches().turnOn(GlobalSwitch.SERVER_MANAGE_CONNECTION_SWITCH);
        }
    }

    public RGPDefaultRemoteServer(String ip, int port, boolean manageConnection) {
        super(ip, port);
        if (manageConnection) {
            this.switches().turnOn(GlobalSwitch.SERVER_MANAGE_CONNECTION_SWITCH);
        }
    }

    public RGPDefaultRemoteServer(int port, boolean manageConnection, boolean syncStop) {
        this(port, manageConnection);
        if (syncStop) {
            this.switches().turnOn(GlobalSwitch.SERVER_SYNC_STOP);
        }
    }

    @Override
    protected void doInit() {
        if (this.addressParser == null) {
            this.addressParser = new RPCAddressParser();
        }

        /** init connection event handler. init connection manager if need **/
        if (this.switches().isOn(GlobalSwitch.SERVER_MANAGE_CONNECTION_SWITCH)) {
            this.connectionEventHandler = new RPCConnectionEventHandler(switches());
            this.connectionEventHandler.setConnectionEventListener(this.connectionEventListener);
            this.connectionEventHandler.setConnectionManager(this.connectionManager = new DefaultConnectionManager(new RandomConnectionSelectStrategy()));
        } else {
            this.connectionEventHandler = new ConnectionEventHandler(switches());
            this.connectionEventHandler.setConnectionEventListener(this.connectionEventListener);
        }

        this.initRPCRemoting();
        this.serverBootstrap = new ServerBootstrap();
        this.serverBootstrap.group(bossGroup, workerGroup)
                .channel(NettyEventLoopUtil.getServerSocketChannelClass())
                .option(ChannelOption.SO_BACKLOG, ConfigManager.tcp_so_backlog())
                .option(ChannelOption.SO_REUSEADDR, ConfigManager.tcp_so_reuseaddr())
                .childOption(ChannelOption.TCP_NODELAY, ConfigManager.tcp_nodelay())
                .childOption(ChannelOption.SO_KEEPALIVE, ConfigManager.tcp_so_keepalive());
        // set write buffer water mark
        this.initWriteBufferWaterMark();
        // init byte buf allocator
        if (ConfigManager.netty_buffer_pooled()) {
            this.serverBootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT).childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        } else {
            this.serverBootstrap.option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT).childOption(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT);
        }
        // enable trigger mode for epoll if need
        NettyEventLoopUtil.enableTriggeredMode(serverBootstrap);
        final boolean idleSwitch = ConfigManager.tcp_idle_switch();
        this.serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                pipeline.addLast("decoder", new ProtocolCodeBasedDecoder(RPCProtocol.DEFAULT_PROTOCOL_CODE_LENGTH));
                pipeline.addLast("encoder", new ProtocolCodeBasedEncoder(ProtocolCode.fromBytes(RPCProtocol.PROTOCOL_CODE)));
                if (idleSwitch) {
                    final int idleTime = ConfigManager.tcp_server_idle();
                    pipeline.addLast("idleStateHandler", new IdleStateHandler(0, 0, idleTime, TimeUnit.MILLISECONDS));
                    pipeline.addLast("acceptorIdleStateTrigger", new AcceptorIdleStateTrigger());
                }
                pipeline.addLast("connectionEventHandler", connectionEventHandler);
                pipeline.addLast("handler", new RPCBusinessEventHandler(true, RGPDefaultRemoteServer.this.userProcessors));
                this.createConnection(socketChannel);
            }

            private void createConnection(SocketChannel channel) {
                ConnectionURL connectionURL = addressParser.parse(RemotingAddressUtil.parseRemoteAddress(channel));
                if (!switches().isOn(GlobalSwitch.SERVER_MANAGE_CONNECTION_SWITCH)) {
                    new Connection(channel, connectionURL);
                } else {
                    connectionManager.add(connectionURL.getUniqueKey(), new Connection(channel, connectionURL));
                }
                channel.pipeline().fireUserEventTriggered(ConnectionEventTypeEnum.CONNECT);
            }
        });
    }

    @Override
    protected boolean doStart() throws InterruptedException {
        this.channelFuture = this.serverBootstrap.bind(new InetSocketAddress(getIp(), getPort())).sync();
        return this.channelFuture.isSuccess();
    }

    @Override
    protected boolean doStop() {
        if (this.channelFuture != null) {
            this.channelFuture.channel().close();
        }
        if (!this.switches().isOn(GlobalSwitch.SERVER_SYNC_STOP)) {
            this.bossGroup.shutdownGracefully();
        } else {
            this.bossGroup.shutdownGracefully().awaitUninterruptibly();
        }
        if (this.switches().isOn(GlobalSwitch.SERVER_MANAGE_CONNECTION_SWITCH) && null != this.connectionManager) {
            this.connectionManager.removeAll();
            log.warn("Close all connections from server side!");
        }
        log.warn("RPC Server stopped!");
        return true;
    }

    @Override
    public void registerDefaultExecutor(byte protocolCode, ExecutorService executor) {
        ProtocolManager.getProtocol(ProtocolCode.fromBytes(protocolCode)).getCommandHandler().registerDefaultExecutor(executor);
    }

    @Override
    public void registerUserProcessor(UserProcessor<?> processor) {
        UserProcessorRegisterHelper.registerUserProcessor(processor, this.userProcessors);
    }

    @Override
    public void registerProcessor(byte protocolCode, RemoteCommandCode commandCode, RemotingProcessor<?> processor) {
        ProtocolManager.getProtocol(ProtocolCode.fromBytes(protocolCode)).getCommandHandler().registerProcessor(commandCode, processor);
    }

    public void addConnectionEventProcessor(ConnectionEventTypeEnum type, ConnectionEventProcessor processor) {
        this.connectionEventListener.addConnectionEventProcessor(type, processor);
    }

    /**
     * 功能描述: <br>
     * 〈init RPC remoting〉
     *
     * @author HuangTaiHong
     * @date 2019.01.07 10:23:00
     */
    protected void initRPCRemoting() {
        RPCCommandFactory commandFactory = new RPCCommandFactory();
        this.remoting = new RPCServerRemoting(commandFactory, this.addressParser, this.connectionManager);
    }

    /**
     * 功能描述: <br>
     * 〈init netty write buffer water mark〉
     *
     * @author HuangTaiHong
     * @date 2019.01.06 16:42:10
     */
    private void initWriteBufferWaterMark() {
        int lowWaterMark = this.netty_buffer_low_watermark();
        int highWaterMark = this.netty_buffer_high_watermark();
        if (lowWaterMark > highWaterMark) {
            throw new IllegalArgumentException(String.format("[server side] bolt netty high water mark {%s} should not be smaller than low water mark {%s} bytes)", highWaterMark, lowWaterMark));
        } else {
            log.warn("[server side] bolt netty low water mark is {} bytes, high water mark is {} bytes", lowWaterMark, highWaterMark);
            this.serverBootstrap.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(lowWaterMark, highWaterMark));
        }
    }

    /*********************************************网络通信相关方法开始*********************************************/
    public void oneway(final String address, final Object request) throws RemotingException, InterruptedException {
        check();
        this.remoting.oneway(address, request, null);
    }

    public void oneway(final String address, final Object request, final InvokeContext invokeContext) throws RemotingException, InterruptedException {
        check();
        this.remoting.oneway(address, request, invokeContext);
    }

    public void oneway(final ConnectionURL connectionURL, final Object request) throws RemotingException, InterruptedException {
        check();
        this.remoting.oneway(connectionURL, request, null);
    }

    public void oneway(final ConnectionURL connectionURL, final Object request, final InvokeContext invokeContext) throws RemotingException, InterruptedException {
        check();
        this.remoting.oneway(connectionURL, request, invokeContext);
    }

    public void oneway(final Connection connection, final Object request) throws RemotingException {
        this.remoting.oneway(connection, request, null);
    }

    public void oneway(final Connection connection, final Object request, final InvokeContext invokeContext) throws RemotingException {
        this.remoting.oneway(connection, request, invokeContext);
    }

    public Object invokeSync(final String address, final Object request, final int timeoutMillis) throws RemotingException, InterruptedException {
        check();
        return this.remoting.invokeSync(address, request, null, timeoutMillis);
    }

    public Object invokeSync(final String addr, final Object request, final InvokeContext invokeContext, final int timeoutMillis) throws RemotingException, InterruptedException {
        check();
        return this.remoting.invokeSync(addr, request, invokeContext, timeoutMillis);
    }

    public Object invokeSync(ConnectionURL connectionURL, Object request, int timeoutMillis) throws RemotingException, InterruptedException {
        check();
        return this.remoting.invokeSync(connectionURL, request, null, timeoutMillis);
    }

    public Object invokeSync(final ConnectionURL connectionURL, final Object request, final InvokeContext invokeContext, final int timeoutMillis) throws RemotingException, InterruptedException {
        check();
        return this.remoting.invokeSync(connectionURL, request, invokeContext, timeoutMillis);
    }

    public Object invokeSync(final Connection connection, final Object request, final int timeoutMillis) throws RemotingException, InterruptedException {
        return this.remoting.invokeSync(connection, request, null, timeoutMillis);
    }

    public Object invokeSync(final Connection connection, final Object request, final InvokeContext invokeContext, final int timeoutMillis) throws RemotingException, InterruptedException {
        return this.remoting.invokeSync(connection, request, invokeContext, timeoutMillis);
    }

    public RPCResponseFuture invokeWithFuture(final String address, final Object request, final int timeoutMillis) throws RemotingException, InterruptedException {
        check();
        return this.remoting.invokeWithFuture(address, request, null, timeoutMillis);
    }

    public RPCResponseFuture invokeWithFuture(final String address, final Object request, final InvokeContext invokeContext, final int timeoutMillis) throws RemotingException, InterruptedException {
        check();
        return this.remoting.invokeWithFuture(address, request, invokeContext, timeoutMillis);
    }

    public RPCResponseFuture invokeWithFuture(final ConnectionURL connectionURL, final Object request, final int timeoutMillis) throws RemotingException, InterruptedException {
        check();
        return this.remoting.invokeWithFuture(connectionURL, request, null, timeoutMillis);
    }

    public RPCResponseFuture invokeWithFuture(final ConnectionURL connectionURL, final Object request, final InvokeContext invokeContext, final int timeoutMillis) throws RemotingException, InterruptedException {
        check();
        return this.remoting.invokeWithFuture(connectionURL, request, invokeContext, timeoutMillis);
    }

    public RPCResponseFuture invokeWithFuture(final Connection connection, final Object request, final int timeoutMillis) throws RemotingException {
        return this.remoting.invokeWithFuture(connection, request, null, timeoutMillis);
    }

    public RPCResponseFuture invokeWithFuture(final Connection connection, final Object request, final InvokeContext invokeContext, final int timeoutMillis) throws RemotingException {
        return this.remoting.invokeWithFuture(connection, request, invokeContext, timeoutMillis);
    }

    public void invokeWithCallback(final String address, final Object request, final InvokeCallback invokeCallback, final int timeoutMillis) throws RemotingException, InterruptedException {
        check();
        this.remoting.invokeWithCallback(address, request, null, invokeCallback, timeoutMillis);
    }

    public void invokeWithCallback(final String address, final Object request, final InvokeContext invokeContext, final InvokeCallback invokeCallback, final int timeoutMillis) throws RemotingException, InterruptedException {
        check();
        this.remoting.invokeWithCallback(address, request, invokeContext, invokeCallback, timeoutMillis);
    }

    public void invokeWithCallback(final ConnectionURL connectionURL, final Object request, final InvokeCallback invokeCallback, final int timeoutMillis) throws RemotingException, InterruptedException {
        check();
        this.remoting.invokeWithCallback(connectionURL, request, null, invokeCallback, timeoutMillis);
    }

    public void invokeWithCallback(final ConnectionURL connectionURL, final Object request, final InvokeContext invokeContext, final InvokeCallback invokeCallback, final int timeoutMillis) throws RemotingException, InterruptedException {
        check();
        this.remoting.invokeWithCallback(connectionURL, request, invokeContext, invokeCallback, timeoutMillis);
    }

    public void invokeWithCallback(final Connection connection, final Object request, final InvokeCallback invokeCallback, final int timeoutMillis) throws RemotingException {
        this.remoting.invokeWithCallback(connection, request, null, invokeCallback, timeoutMillis);
    }

    public void invokeWithCallback(final Connection connection, final Object request, final InvokeContext invokeContext, final InvokeCallback invokeCallback, final int timeoutMillis) throws RemotingException {
        this.remoting.invokeWithCallback(connection, request, invokeContext, invokeCallback, timeoutMillis);
    }
    /*********************************************网络通信相关方法结束*********************************************/

    private void check() {
        if (!this.switches().isOn(GlobalSwitch.SERVER_MANAGE_CONNECTION_SWITCH)) {
            throw new UnsupportedOperationException("Please enable connection manage feature of RPC Server before call this method! See comments in constructor RGPDefaultRemoteServer(int port, boolean manageConnection) to find how to enable!");
        }
    }
}