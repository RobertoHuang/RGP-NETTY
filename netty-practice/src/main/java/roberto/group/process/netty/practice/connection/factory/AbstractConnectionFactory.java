/**
 * FileName: AbstractConnectionFactory
 * Author:   HuangTaiHong
 * Date:     2019/1/8 15:54
 * Description: ConnectionFactory to create connection.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.connection.factory;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import roberto.group.process.netty.practice.codec.ProtocolCodeBasedDecoder;
import roberto.group.process.netty.practice.codec.ProtocolCodeBasedEncoder;
import roberto.group.process.netty.practice.configuration.configs.ConfigurableInstance;
import roberto.group.process.netty.practice.configuration.manager.ConfigManager;
import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.connection.ConnectionURL;
import roberto.group.process.netty.practice.connection.enums.ConnectionEventTypeEnum;
import roberto.group.process.netty.practice.handler.ConnectionEventHandler;
import roberto.group.process.netty.practice.protocol.ProtocolCode;
import roberto.group.process.netty.practice.protocol.impl.RPCProtocol;
import roberto.group.process.netty.practice.thread.NamedThreadFactory;
import roberto.group.process.netty.practice.utils.NettyEventLoopUtil;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * 〈一句话功能简述〉<br>
 * 〈ConnectionFactory to create connection.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/8
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractConnectionFactory implements ConnectionFactory {
    protected Bootstrap bootstrap;
    private final ChannelHandler encoder;
    private final ChannelHandler decoder;
    private final ChannelHandler businessHandler;
    private final ChannelHandler heartbeatHandler;
    private final ConfigurableInstance configurableInstance;
    private static final EventLoopGroup workerGroup = NettyEventLoopUtil.newEventLoopGroup(Runtime.getRuntime().availableProcessors() + 1, new NamedThreadFactory("bolt-netty-client-worker", true));

    public AbstractConnectionFactory(ChannelHandler encoder, ChannelHandler decoder, ChannelHandler businessHandler, ChannelHandler heartbeatHandler, ConfigurableInstance configurableInstance) {
        if (businessHandler == null) {
            throw new IllegalArgumentException("businessHandler must no be null.");
        }

        this.encoder = encoder;
        this.decoder = decoder;
        this.businessHandler = businessHandler;
        this.heartbeatHandler = heartbeatHandler;
        this.configurableInstance = configurableInstance;
    }

    @Override
    public void init(final ConnectionEventHandler connectionEventHandler) {
        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup).channel(NettyEventLoopUtil.getClientSocketChannelClass())
                .option(ChannelOption.TCP_NODELAY, ConfigManager.tcp_nodelay())
                .option(ChannelOption.SO_REUSEADDR, ConfigManager.tcp_so_reuseaddr())
                .option(ChannelOption.SO_KEEPALIVE, ConfigManager.tcp_so_keepalive());

        // init netty write buffer water mark
        initWriteBufferWaterMark();

        // init byte buf allocator
        if (ConfigManager.netty_buffer_pooled()) {
            this.bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        } else {
            this.bootstrap.option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT);
        }

        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel channel) {
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast("decoder", new ProtocolCodeBasedDecoder(RPCProtocol.DEFAULT_PROTOCOL_CODE_LENGTH));
                pipeline.addLast("encoder", new ProtocolCodeBasedEncoder(ProtocolCode.fromBytes(RPCProtocol.PROTOCOL_CODE)));
                boolean idleSwitch = ConfigManager.tcp_idle_switch();
                if (idleSwitch) {
                    pipeline.addLast("idleStateHandler", new IdleStateHandler(ConfigManager.tcp_client_idle(), ConfigManager.tcp_client_idle(), 0, TimeUnit.MILLISECONDS));
                    pipeline.addLast("heartbeatHandler", AbstractConnectionFactory.this.heartbeatHandler);
                }
                pipeline.addLast("connectionEventHandler", connectionEventHandler);
                pipeline.addLast("handler", AbstractConnectionFactory.this.businessHandler);
            }
        });
    }

    @Override
    public Connection createConnection(ConnectionURL connectionURL) throws Exception {
        Channel channel = doCreateConnection(connectionURL.getIp(), connectionURL.getPort(), connectionURL.getConnectTimeout());
        Connection connection = new Connection(channel, ProtocolCode.fromBytes(connectionURL.getProtocol()), connectionURL.getVersion(), connectionURL);
        channel.pipeline().fireUserEventTriggered(ConnectionEventTypeEnum.CONNECT);
        return connection;
    }

    @Override
    public Connection createConnection(String targetIP, int targetPort, int connectTimeout) throws Exception {
        Channel channel = doCreateConnection(targetIP, targetPort, connectTimeout);
        Connection connection = new Connection(channel, ProtocolCode.fromBytes(RPCProtocol.PROTOCOL_CODE), RPCProtocol.PROTOCOL_VERSION_1, new ConnectionURL(targetIP, targetPort));
        channel.pipeline().fireUserEventTriggered(ConnectionEventTypeEnum.CONNECT);
        return connection;
    }

    @Override
    public Connection createConnection(String targetIP, int targetPort, byte version, int connectTimeout) throws Exception {
        Channel channel = doCreateConnection(targetIP, targetPort, connectTimeout);
        Connection connection = new Connection(channel, ProtocolCode.fromBytes(RPCProtocol.PROTOCOL_CODE), version, new ConnectionURL(targetIP, targetPort));
        channel.pipeline().fireUserEventTriggered(ConnectionEventTypeEnum.CONNECT);
        return connection;
    }

    /**
     * 功能描述: <br>
     * 〈init netty write buffer water mark.〉
     *
     * @author HuangTaiHong
     * @date 2019.01.08 16:36:01
     */
    private void initWriteBufferWaterMark() {
        int lowWaterMark = this.configurableInstance.netty_buffer_low_watermark();
        int highWaterMark = this.configurableInstance.netty_buffer_high_watermark();
        if (lowWaterMark > highWaterMark) {
            throw new IllegalArgumentException(String.format("[client side] bolt netty high water mark {%s} should not be smaller than low water mark {%s} bytes)", highWaterMark, lowWaterMark));
        } else {
            log.warn("[client side] bolt netty low water mark is {} bytes, high water mark is {} bytes", lowWaterMark, highWaterMark);
        }
        this.bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(lowWaterMark, highWaterMark));
    }

    /**
     * 功能描述: <br>
     * 〈do create connection.〉
     *
     * @param targetIP
     * @param targetPort
     * @param connectTimeout
     * @return > io.netty.channel.Channel
     * @throws Exception
     * @author HuangTaiHong
     * @date 2019.01.08 16:37:48
     */
    protected Channel doCreateConnection(String targetIP, int targetPort, int connectTimeout) throws Exception {
        // prevent unreasonable value, at least 1000
        connectTimeout = Math.max(connectTimeout, 1000);
        String address = targetIP + ":" + targetPort;
        log.debug("connectTimeout of address [{}] is [{}].", address, connectTimeout);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(targetIP, targetPort)).awaitUninterruptibly();
        if (!future.isDone()) {
            String errorMessage = "Create connection to " + address + " timeout!";
            log.warn(errorMessage);
            throw new Exception(errorMessage);
        } else if (future.isCancelled()) {
            String errorMessage = "Create connection to " + address + " cancelled by user!";
            log.warn(errorMessage);
            throw new Exception(errorMessage);
        } else if (!future.isSuccess()) {
            String errorMessage = "Create connection to " + address + " error!";
            log.warn(errorMessage);
            throw new Exception(errorMessage, future.cause());
        }
        return future.channel();
    }
}