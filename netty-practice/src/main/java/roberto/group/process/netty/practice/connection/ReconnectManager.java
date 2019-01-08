/**
 * FileName: ReconnectManager
 * Author:   HuangTaiHong
 * Date:     2019/1/3 13:48
 * Description: 重新连接管理器
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import roberto.group.process.netty.practice.connection.manager.ConnectionManager;
import roberto.group.process.netty.practice.exception.RemotingException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 〈一句话功能简述〉<br>
 * 〈重新连接管理器〉
 *
 * @author HuangTaiHong
 * @create 2019/1/3
 * @since 1.0.0
 */
public class ReconnectManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReconnectManager.class);

    /** 重连线程状态 **/
    private volatile boolean started;

    /** 重连时间间隔 **/
    private int healConnectionInterval = 1000;

    /** 重新连接线程 **/
    private final Thread healConnectionThreads;

    /** 连接管理器 - 用于真正执行重新连接任务 **/
    private ConnectionManager connectionManager;

    /** 用于保存取消重新连接的任务 **/
    protected final List<ConnectionURL> canceledTasks = new CopyOnWriteArrayList<>();

    /** 用于保存需要重新连接的任务 **/
    private final LinkedBlockingQueue<ReconnectTask> reconnectTasks = new LinkedBlockingQueue<>();

    public ReconnectManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        // 启动消费重新连接任务线程
        this.healConnectionThreads = new Thread(new HealConnectionRunner());
        this.healConnectionThreads.start();
        this.started = true;
    }

    public void addReconnectTask(ConnectionURL url) {
        reconnectTasks.add(new ReconnectTask(url));
    }

    private void addReconnectTask(ReconnectTask task) {
        reconnectTasks.add(task);
    }

    public void addCancelUrl(ConnectionURL url) {
        canceledTasks.add(url);
    }

    public void removeCancelUrl(ConnectionURL url) {
        canceledTasks.remove(url);
    }

    private void doReconnectTask(ReconnectTask task) throws InterruptedException, RemotingException {
        connectionManager.createConnectionAndHealIfNeed(task.connectionURL);
    }

    /**
     * 功能描述: <br>
     * 〈检查任务是否有效、如果被取消则无效〉
     *
     * @param task
     * @return > boolean
     * @author HuangTaiHong
     * @date 2019.01.03 13:58:06
     */
    private boolean isValidTask(ReconnectTask task) {
        return !canceledTasks.contains(task.connectionURL);
    }

    /**
     * 功能描述: <br>
     * 〈停止消费重新连接任务线程〉
     *
     * @author HuangTaiHong
     * @date 2019.01.03 14:34:25
     */
    public void stop() {
        if (!this.started) {
            return;
        }
        this.started = false;
        healConnectionThreads.interrupt();
        this.reconnectTasks.clear();
        this.canceledTasks.clear();
    }

    /**
     * 〈一句话功能简述〉
     * 〈重新连接任务〉
     *
     * @author HuangTaiHong
     * @create 2019.01.03
     * @since 1.0.0
     */
    class ReconnectTask {
        private ConnectionURL connectionURL;

        public ReconnectTask(ConnectionURL connectionURL) {
            this.connectionURL = connectionURL;
        }
    }

    private final class HealConnectionRunner implements Runnable {
        /** 最后一次尝试连接耗时 **/
        private long lastConnectTime = -1;

        @Override
        public void run() {
            while (ReconnectManager.this.started) {
                long start = -1;
                ReconnectTask task = null;
                try {
                    // 退避策略【重连时间间隔 未到时间间隔则SLEEP】
                    if (this.lastConnectTime > 0 && this.lastConnectTime < ReconnectManager.this.healConnectionInterval || this.lastConnectTime < 0) {
                        Thread.sleep(ReconnectManager.this.healConnectionInterval);
                    }

                    try {
                        // 阻塞获取重连任务
                        task = ReconnectManager.this.reconnectTasks.take();
                    } catch (InterruptedException e) {

                    }

                    start = System.currentTimeMillis();
                    if (ReconnectManager.this.isValidTask(task)) {
                        try {
                            // 尝试进行重新连接
                            ReconnectManager.this.doReconnectTask(task);
                        } catch (InterruptedException e) {
                            throw e;
                        }
                    } else {
                        LOGGER.warn("Invalid reconnect request task {}, cancel list size {}", task.connectionURL, canceledTasks.size());
                    }
                    this.lastConnectTime = System.currentTimeMillis() - start;
                } catch (Exception e) {
                    retryWhenException(start, task, e);
                }
            }
        }

        /**
         * 功能描述: <br>
         * 〈出现异常则重新加回任务队列〉
         *
         * @param start
         * @param task
         * @param e
         * @author HuangTaiHong
         * @date 2019.01.03 14:26:23
         */
        private void retryWhenException(long start, ReconnectTask task, Exception e) {
            if (start != -1) {
                this.lastConnectTime = System.currentTimeMillis() - start;
            }

            if (task != null) {
                LOGGER.warn("reconnect target: {} failed.", task.connectionURL, e);
                ReconnectManager.this.addReconnectTask(task);
            }
        }
    }
}