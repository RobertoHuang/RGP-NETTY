/**
 * FileName: ReconnectManager
 * Author:   HuangTaiHong
 * Date:     2019/1/3 13:48
 * Description: Reconnect manager.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.connection.manager;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import roberto.group.process.netty.practice.connection.ConnectionURL;
import roberto.group.process.netty.practice.exception.RemotingException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 〈一句话功能简述〉<br>
 * 〈Reconnect manager.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/3
 * @since 1.0.0
 */
@Slf4j
public class ReconnectManager {
    private volatile boolean started;

    private int healConnectionInterval = 1000;

    private final Thread healConnectionThreads;

    private ConnectionManager connectionManager;

    private final List<ConnectionURL> canceledTasks = new CopyOnWriteArrayList<>();

    private final LinkedBlockingQueue<ReconnectTask> reconnectTasks = new LinkedBlockingQueue<>();

    public ReconnectManager(ConnectionManager connectionManager) {
        this.started = true;
        this.connectionManager = connectionManager;
        (this.healConnectionThreads = new Thread(new HealConnectionRunner())).start();
    }

    private boolean isValidTask(ReconnectTask task) {
        return !canceledTasks.contains(task.connectionURL);
    }

    public void addReconnectTask(ConnectionURL connectionURL) {
        reconnectTasks.add(new ReconnectTask(connectionURL));
    }

    private void addReconnectTask(ReconnectTask task) {
        reconnectTasks.add(task);
    }

    public void addCancelUrl(ConnectionURL connectionURL) {
        canceledTasks.add(connectionURL);
    }

    public void removeCancelUrl(ConnectionURL connectionURL) {
        canceledTasks.remove(connectionURL);
    }

    private void doReconnectTask(ReconnectTask task) throws InterruptedException, RemotingException {
        connectionManager.createConnectionAndHealIfNeed(task.connectionURL);
    }

    public void stop() {
        if (!this.started) {
            return;
        }
        this.started = false;
        healConnectionThreads.interrupt();
        this.canceledTasks.clear();
        this.reconnectTasks.clear();
    }

    @AllArgsConstructor
    class ReconnectTask {
        private ConnectionURL connectionURL;
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
                        log.warn("Invalid reconnect request task {}, cancel list size {}", task.connectionURL, canceledTasks.size());
                    }
                    this.lastConnectTime = System.currentTimeMillis() - start;
                } catch (Exception e) {
                    retryWhenException(start, task, e);
                }
            }
        }

        private void retryWhenException(long start, ReconnectTask task, Exception e) {
            if (start != -1) {
                this.lastConnectTime = System.currentTimeMillis() - start;
            }

            if (task != null) {
                log.warn("reconnect target: {} failed.", task.connectionURL, e);
                ReconnectManager.this.addReconnectTask(task);
            }
        }
    }
}