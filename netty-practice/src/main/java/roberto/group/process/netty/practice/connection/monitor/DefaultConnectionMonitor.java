/**
 * FileName: DefaultConnectionMonitor
 * Author:   HuangTaiHong
 * Date:     2019/1/9 17:02
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.connection.monitor;

import lombok.extern.slf4j.Slf4j;
import roberto.group.process.netty.practice.configuration.manager.ConfigManager;
import roberto.group.process.netty.practice.connection.manager.impl.DefaultConnectionManager;
import roberto.group.process.netty.practice.connection.strategy.ConnectionMonitorStrategy;
import roberto.group.process.netty.practice.thread.NamedThreadFactory;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 〈一句话功能简述〉<br>
 * 〈A default connection monitor that handle connections with strategies〉
 *
 * @author HuangTaiHong
 * @create 2019/1/9
 * @since 1.0.0
 */
@Slf4j
public class DefaultConnectionMonitor {
    private ScheduledThreadPoolExecutor executor;

    /** Monitor strategy */
    private ConnectionMonitorStrategy strategy;

    /** Connection pools to monitor */
    private DefaultConnectionManager connectionManager;

    public DefaultConnectionMonitor(ConnectionMonitorStrategy strategy, DefaultConnectionManager connectionManager) {
        this.strategy = strategy;
        this.connectionManager = connectionManager;
    }

    public void start() {
        /** initial delay to execute schedule task, unit: ms */
        long initialDelay = ConfigManager.conn_monitor_initial_delay();

        /** period of schedule task, unit: ms*/
        long period = ConfigManager.conn_monitor_period();

        /** init thread pool **/
        this.executor = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("Connection-Monitor-Thread", true), new ThreadPoolExecutor.AbortPolicy());

        /** execute task **/
        this.executor.scheduleAtFixedRate(() -> {
            try {
                if (strategy != null) {
                    strategy.monitor(connectionManager.getConnectionTasks());
                }
            } catch (Exception e) {
                log.warn("MonitorTask error", e);
            }
        }, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    public void destroy() {
        this.executor.purge();

        this.executor.shutdown();
    }
}