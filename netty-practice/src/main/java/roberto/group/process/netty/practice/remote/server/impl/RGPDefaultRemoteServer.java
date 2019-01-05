/**
 * FileName: RGPDefaultRemoteServer
 * Author:   HuangTaiHong
 * Date:     2019/1/2 10:18
 * Description: RGP RPC实现类
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.remote.server.impl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import roberto.group.process.netty.practice.command.code.RemoteCommandCode;
import roberto.group.process.netty.practice.command.processor.AuthenticationProcessor;
import roberto.group.process.netty.practice.command.processor.RemotingCommandProcessor;
import roberto.group.process.netty.practice.configuration.configs.ConfigManager;
import roberto.group.process.netty.practice.configuration.switches.impl.GlobalSwitch;
import roberto.group.process.netty.practice.connection.ConnectionEventListener;
import roberto.group.process.netty.practice.connection.DefaultConnectionManager;
import roberto.group.process.netty.practice.connection.strategy.impl.RandomSelectStrategy;
import roberto.group.process.netty.practice.handler.ConnectionEventHandler;
import roberto.group.process.netty.practice.handler.RPCConnectionEventHandler;
import roberto.group.process.netty.practice.remote.RPCRemoting;
import roberto.group.process.netty.practice.remote.parse.RemotingAddressParser;
import roberto.group.process.netty.practice.remote.parse.impl.RPCAddressParser;
import roberto.group.process.netty.practice.thread.NamedThreadFactory;
import roberto.group.process.netty.practice.utils.NettyEventLoopUtil;

import java.util.concurrent.ExecutorService;

/**
 * 〈一句话功能简述〉<br>
 * 〈RGP RPC实现类〉
 *
 * @author HuangTaiHong
 * @create 2019/1/2
 * @since 1.0.0
 */
@Slf4j
public class RGPDefaultRemoteServer extends AbstractRemotingServer {
    private ChannelFuture channelFuture;

    private RemotingAddressParser addressParser;

    /** connection manager */
    private DefaultConnectionManager connectionManager;

    /** connection event handler */
    private ConnectionEventHandler connectionEventHandler;

    /** connection event listener */
    private ConnectionEventListener connectionEventListener = new ConnectionEventListener();

    /** RPC remoting */
    protected RPCRemoting remoting;

    /** ServerBootstrap **/
    private ServerBootstrap serverBootstrap;

    /** 初始化BOSS线程 **/
    private final EventLoopGroup bossGroup = NettyEventLoopUtil.newEventLoopGroup(1, new NamedThreadFactory("rgp-netty-server-boss", false));

    /** 初始化Worker线程 **/
    private static final EventLoopGroup workerGroup = NettyEventLoopUtil.newEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2, new NamedThreadFactory("rgp-netty-server-worker", true));

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

        if (this.switches().isOn(GlobalSwitch.SERVER_MANAGE_CONNECTION_SWITCH)) {
            this.connectionEventHandler = new RPCConnectionEventHandler(switches());
            this.connectionEventHandler.setConnectionEventListener(this.connectionEventListener);
            this.connectionEventHandler.setConnectionManager(new DefaultConnectionManager(new RandomSelectStrategy()));
        } else {
            this.connectionEventHandler = new ConnectionEventHandler(switches());
            this.connectionEventHandler.setConnectionEventListener(this.connectionEventListener);
        }
    }

    protected boolean doStart() {
        return false;
    }

    protected boolean doStop() {
        return false;
    }

    public String getServerIp() {
        return null;
    }

    public int getServerPort() {
        return 0;
    }

    public void registerDefaultExecutor(byte protocolCode, ExecutorService executor) {

    }

    public void registerAuthenticationProcessor(AuthenticationProcessor<?> processor) {

    }

    public void registerProcessor(byte protocolCode, RemoteCommandCode commandCode, RemotingCommandProcessor<?> processor) {

    }

    protected void initRPCRemoting() {
        this.remoting = new RPCRemoting(new RpcCommandFactory(), this.addressParser, this.connectionManager);
    }
}