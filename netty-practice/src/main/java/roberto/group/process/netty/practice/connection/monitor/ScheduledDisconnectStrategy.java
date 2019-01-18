/**
 * FileName: ScheduledDisconnectStrategy
 * Author:   HuangTaiHong
 * Date:     2019/1/9 17:05
 * Description: An implemented strategy to monitor connections.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.connection.monitor;

import lombok.extern.slf4j.Slf4j;
import roberto.group.process.netty.practice.configuration.manager.ConfigManager;
import roberto.group.process.netty.practice.configuration.support.ConfigsSupport;
import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.connection.ConnectionPool;
import roberto.group.process.netty.practice.utils.FutureTaskUtil;
import roberto.group.process.netty.practice.utils.RemotingAddressUtil;
import roberto.group.process.netty.practice.utils.RunStateRecordedFutureTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 〈一句话功能简述〉<br>
 * 〈An implemented strategy to monitor connections.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/9
 * @since 1.0.0
 */
@Slf4j
public class ScheduledDisconnectStrategy implements ConnectionMonitorStrategy {
    /** random */
    private Random random = new Random();

    /** Retry detect period for ScheduledDisconnectStrategy */
    private static int RETRY_DETECT_PERIOD = ConfigManager.retry_detect_period();

    /** the connections threshold of each uniqueKey */
    private static final int CONNECTION_THRESHOLD = ConfigManager.conn_threshold();

    /** fresh select connections to be closed */
    private Map<String, Connection> freshSelectConnections = new ConcurrentHashMap();

    @Override
    public Map<String, List<Connection>> filter(List<Connection> connections) {
        List<Connection> serviceOnConnections = new ArrayList();
        List<Connection> serviceOffConnections = new ArrayList();
        Map<String, List<Connection>> filteredConnections = new ConcurrentHashMap();
        for (Connection connection : connections) {
            String serviceStatus = (String) connection.getAttribute(ConfigsSupport.CONN_SERVICE_STATUS);
            if (serviceStatus != null) {
                if (connection.isInvokeFutureMapFinish() && !freshSelectConnections.containsValue(connection)) {
                    serviceOffConnections.add(connection);
                }
            } else {
                serviceOnConnections.add(connection);
            }
        }

        filteredConnections.put(ConfigsSupport.CONN_SERVICE_STATUS_ON, serviceOnConnections);
        filteredConnections.put(ConfigsSupport.CONN_SERVICE_STATUS_OFF, serviceOffConnections);
        return filteredConnections;
    }

    @Override
    public void monitor(Map<String, RunStateRecordedFutureTask<ConnectionPool>> connectionPools) {
        try {
            if (null != connectionPools && !connectionPools.isEmpty()) {
                Iterator<Map.Entry<String, RunStateRecordedFutureTask<ConnectionPool>>> iterator = connectionPools.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, RunStateRecordedFutureTask<ConnectionPool>> entry = iterator.next();
                    String poolKey = entry.getKey();
                    ConnectionPool connectionPool = FutureTaskUtil.getFutureTaskResult(entry.getValue(), log);
                    List<Connection> connections = connectionPool.getAll();
                    Map<String, List<Connection>> filteredConnections = this.filter(connections);
                    List<Connection> serviceOnConnections = filteredConnections.get(ConfigsSupport.CONN_SERVICE_STATUS_ON);
                    List<Connection> serviceOffConnections = filteredConnections.get(ConfigsSupport.CONN_SERVICE_STATUS_OFF);
                    if (serviceOnConnections.size() > CONNECTION_THRESHOLD) {
                        Connection freshSelectConnect = serviceOnConnections.get(random.nextInt(serviceOnConnections.size()));
                        freshSelectConnect.setAttribute(ConfigsSupport.CONN_SERVICE_STATUS, ConfigsSupport.CONN_SERVICE_STATUS_OFF);
                        Connection lastSelectConnect = freshSelectConnections.remove(poolKey);
                        freshSelectConnections.put(poolKey, freshSelectConnect);
                        closeFreshSelectConnections(lastSelectConnect, serviceOffConnections);
                    } else {
                        if (freshSelectConnections.containsKey(poolKey)) {
                            Connection lastSelectConnect = freshSelectConnections.remove(poolKey);
                            closeFreshSelectConnections(lastSelectConnect, serviceOffConnections);
                        }
                        log.info("the size of serviceOnConnections [{}] reached CONNECTION_THRESHOLD [{}].", serviceOnConnections.size(), CONNECTION_THRESHOLD);
                    }

                    for (Connection offConnection : serviceOffConnections) {
                        if (offConnection.isFine()) {
                            offConnection.close();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("ScheduledDisconnectStrategy monitor error", e);
        }
    }

    /**
     * 功能描述: <br>
     * 〈close the connection of the fresh select connections.〉
     *
     * @param lastSelectConnect
     * @param serviceOffConnections
     * @throws InterruptedException
     * @author HuangTaiHong
     * @date 2019.01.09 17:19:25
     */
    private void closeFreshSelectConnections(Connection lastSelectConnect, List<Connection> serviceOffConnections) throws InterruptedException {
        if (null != lastSelectConnect) {
            if (lastSelectConnect.isInvokeFutureMapFinish()) {
                serviceOffConnections.add(lastSelectConnect);
            } else {
                Thread.sleep(RETRY_DETECT_PERIOD);
                if (lastSelectConnect.isInvokeFutureMapFinish()) {
                    serviceOffConnections.add(lastSelectConnect);
                } else {
                    log.info("Address={} won't close at this schedule turn", RemotingAddressUtil.parseRemoteAddress(lastSelectConnect.getChannel()));
                }
            }
        }
    }
}