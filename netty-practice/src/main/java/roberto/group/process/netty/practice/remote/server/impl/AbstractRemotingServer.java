/**
 * FileName: AbstractRemotingServer
 * Author:   HuangTaiHong
 * Date:     2018/12/29 15:37
 * Description: RPC Server抽象实现
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.remote.server.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import roberto.group.process.netty.practice.configuration.configs.impl.AbstractConfigurableInstance;
import roberto.group.process.netty.practice.configuration.container.ConfigTypeEnum;
import roberto.group.process.netty.practice.remote.server.RemotingServer;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 〈一句话功能简述〉<br>
 * 〈RPC Server抽象实现〉
 *
 * @author HuangTaiHong
 * @create 2018/12/29
 * @since 1.0.0
 */
public abstract class AbstractRemotingServer extends AbstractConfigurableInstance implements RemotingServer {
    private String ip;

    private int port;

    private AtomicBoolean started = new AtomicBoolean(false);

    public static final Logger LOGGER = LoggerFactory.getLogger(AbstractRemotingServer.class);

    public AbstractRemotingServer(int port) {
        this(new InetSocketAddress(port).getAddress().getHostAddress(), port);
    }

    public AbstractRemotingServer(String ip, int port) {
        super(ConfigTypeEnum.SERVER_SIDE);
        this.ip = ip;
        this.port = port;
    }

    public boolean start() {
        if (started.compareAndSet(false, true)) {
            try {
                doInit();
                LOGGER.info("Prepare to start server on port {}", port);
                if (doStart()) {
                    LOGGER.info("Server started on port {}", port);
                    return true;
                } else {
                    LOGGER.warn("Failed starting server on port {}", port);
                    return false;
                }
            } catch (Throwable throwable) {
                this.stop();
                throw new IllegalStateException("ERROR: Failed to start the Server!", throwable);
            }
        } else {
            throw new IllegalStateException("ERROR: The server has already started!");
        }
    }

    public boolean stop() {
        if (started.compareAndSet(true, false)) {
            return this.doStop();
        } else {
            throw new IllegalStateException("ERROR: The server has already stopped!");
        }
    }

    /**
     * 功能描述: <br>
     * 〈初始化〉
     *
     * @author HuangTaiHong
     * @date 2019.01.02 10:19:55
     */
    protected abstract void doInit();

    /**
     * 功能描述: <br>
     * 〈启动Server〉
     *
     * @return > boolean
     * @author HuangTaiHong
     * @date 2019.01.02 10:20:01
     */
    protected abstract boolean doStart();

    /**
     * 功能描述: <br>
     * 〈停止Server〉
     *
     * @return > boolean
     * @author HuangTaiHong
     * @date 2019.01.02 10:20:12
     */
    protected abstract boolean doStop();
}