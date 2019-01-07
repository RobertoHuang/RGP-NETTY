/**
 * FileName: ConnectionEventHandler
 * Author:   HuangTaiHong
 * Date:     2019/1/2 10:24
 * Description: 连接事件处理器
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.Attribute;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import roberto.group.process.netty.practice.configuration.switches.impl.GlobalSwitch;
import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.connection.ConnectionEventListener;
import roberto.group.process.netty.practice.connection.enums.ConnectionEventTypeEnum;
import roberto.group.process.netty.practice.connection.ConnectionManager;
import roberto.group.process.netty.practice.connection.ReconnectManager;
import roberto.group.process.netty.practice.thread.NamedThreadFactory;
import roberto.group.process.netty.practice.utils.RemotingUtil;

import java.net.SocketAddress;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 〈一句话功能简述〉<br>
 * 〈连接事件处理器〉
 *
 * @author HuangTaiHong
 * @create 2019/1/2
 * @since 1.0.0
 */
@ChannelHandler.Sharable
public class ConnectionEventHandler extends ChannelDuplexHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionEventHandler.class);

    private GlobalSwitch globalSwitch;

    private ReconnectManager reconnectManager;

    @Getter
    @Setter
    private ConnectionManager connectionManager;

    private ConnectionEventListener eventListener;

    private ConnectionEventExecutor eventExecutor;

    public ConnectionEventHandler() {

    }

    public ConnectionEventHandler(GlobalSwitch globalSwitch) {
        this.globalSwitch = globalSwitch;
    }

    public void setConnectionEventListener(ConnectionEventListener listener) {
        if (listener != null) {
            this.eventListener = listener;
            if (this.eventExecutor == null) {
                this.eventExecutor = new ConnectionEventExecutor();
            }
        }
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        if (LOGGER.isInfoEnabled()) {
            final String local = localAddress == null ? null : RemotingUtil.parseSocketAddressToString(localAddress);
            final String remote = remoteAddress == null ? "UNKNOWN" : RemotingUtil.parseSocketAddressToString(remoteAddress);
            if (local == null) {
                LOGGER.info("Try connect to {}", remote);
            } else {
                LOGGER.info("Try connect from {} to {}", local, remote);
            }
        }
        super.connect(ctx, remoteAddress, localAddress, promise);
    }

    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        LOGGER.info("Connection disconnect to {}", Optional.of(RemotingUtil.parseRemoteAddress(ctx.channel())).orElse("UNKNOWN-ADDR"));
        super.disconnect(ctx, promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        LOGGER.info("Connection closed: {}", Optional.of(RemotingUtil.parseRemoteAddress(ctx.channel())).orElse("UNKNOWN-ADDR"));
        final Connection connection = ctx.channel().attr(Connection.CONNECTION).get();
        if (connection != null) {
            connection.onClose();
        }
        super.close(ctx, promise);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("Connection channel registered: {}", Optional.of(RemotingUtil.parseRemoteAddress(ctx.channel())).orElse("UNKNOWN-ADDR"));
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("Connection channel unregistered: {}", Optional.of(RemotingUtil.parseRemoteAddress(ctx.channel())).orElse("UNKNOWN-ADDR"));
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("Connection channel active: {}", Optional.of(RemotingUtil.parseRemoteAddress(ctx.channel())).orElse("UNKNOWN-ADDR"));
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String remoteAddress = RemotingUtil.parseRemoteAddress(ctx.channel());
        LOGGER.info("Connection channel inactive: {}", Optional.of(remoteAddress).orElse("UNKNOWN-ADDR"));
        super.channelInactive(ctx);
        Attribute attr = ctx.channel().attr(Connection.CONNECTION);
        if (null != attr) {
            // 判断是否需要断线重连
            if (this.globalSwitch != null && this.globalSwitch.isOn(GlobalSwitch.CONN_RECONNECT_SWITCH)) {
                Connection connection = (Connection) attr.get();
                if (reconnectManager != null) {
                    reconnectManager.addReconnectTask(connection.getConnectionURL());
                }
            }
            // 触发连接不可用事件
            onEvent((Connection) attr.get(), remoteAddress, ConnectionEventTypeEnum.CLOSE);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) throws Exception {
        if (event instanceof ConnectionEventTypeEnum) {
            switch ((ConnectionEventTypeEnum) event) {
                case CONNECT:
                    Channel channel = ctx.channel();
                    if (null != channel) {
                        Connection connection = channel.attr(Connection.CONNECTION).get();
                        this.onEvent(connection, connection.getConnectionURL().getOriginUrl(), ConnectionEventTypeEnum.CONNECT);
                    } else {
                        LOGGER.warn("channel null when handle user triggered event in ConnectionEventHandler!");
                    }
                    break;
                default:
                    return;
            }
        } else {
            super.userEventTriggered(ctx, event);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        final String remoteAddress = RemotingUtil.parseRemoteAddress(ctx.channel());
        final String localAddress = RemotingUtil.parseLocalAddress(ctx.channel());
        LOGGER.warn("ExceptionCaught in connection: local[{}], remote[{}], close the connection! Cause[{}:{}]", localAddress, remoteAddress, cause.getClass().getSimpleName(), cause.getMessage());
        ctx.channel().close();
    }

    private void onEvent(final Connection conn, final String remoteAddress, final ConnectionEventTypeEnum type) {
        if (this.eventListener != null) {
            this.eventExecutor.onEvent(() -> ConnectionEventHandler.this.eventListener.onEvent(type, remoteAddress, conn));
        }
    }

    /**
     * 〈一句话功能简述〉
     * 〈连接事件执行器〉
     *
     * @author HuangTaiHong
     * @create 2019.01.03
     * @since 1.0.0
     */
    public class ConnectionEventExecutor {
        private final Logger LOGGER = LoggerFactory.getLogger(ConnectionEventExecutor.class);

        ExecutorService executor = new ThreadPoolExecutor(1, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(10000), new NamedThreadFactory("rgp-conn-event-executor", true));

        public void onEvent(Runnable event) {
            try {
                executor.execute(event);
            } catch (Throwable t) {
                LOGGER.error("Exception caught when execute connection event!", t);
            }
        }
    }
}