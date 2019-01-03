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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import roberto.group.process.netty.practice.command.code.RemoteCommandCode;
import roberto.group.process.netty.practice.command.processor.AuthenticationProcessor;
import roberto.group.process.netty.practice.command.processor.RemotingCommandProcessor;
import roberto.group.process.netty.practice.configuration.configs.ConfigManager;
import roberto.group.process.netty.practice.configuration.switches.impl.GlobalSwitch;
import roberto.group.process.netty.practice.connection.ConnectionEventListener;
import roberto.group.process.netty.practice.handler.ConnectionEventHandler;
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
public class RGPDefaultRemoteServer extends AbstractRemotingServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RGPDefaultRemoteServer.class);

    private ChannelFuture channelFuture;

    private ConnectionEventHandler connectionEventHandler;

    private ConnectionEventListener connectionEventListener = new ConnectionEventListener();

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
        // 是否启用连接管理
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


    protected void doInit() {

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
}