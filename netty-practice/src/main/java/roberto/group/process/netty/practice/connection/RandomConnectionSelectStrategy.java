/**
 * FileName: RandomConnectionSelectStrategy
 * Author:   HuangTaiHong
 * Date:     2019/1/5 14:01
 * Description: Select a connection randomly.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.connection;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import roberto.group.process.netty.practice.configuration.support.ConfigsSupport;
import roberto.group.process.netty.practice.configuration.switches.impl.GlobalSwitch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 〈一句话功能简述〉<br>
 * 〈Select a connection randomly.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/5
 * @since 1.0.0
 */
@Slf4j
@NoArgsConstructor
public class RandomConnectionSelectStrategy implements ConnectionSelectStrategy {
    private GlobalSwitch globalSwitch;

    /** max retry times */
    private static final int MAX_TIMES = 5;

    private final Random random = new Random();

    public RandomConnectionSelectStrategy(GlobalSwitch globalSwitch) {
        this.globalSwitch = globalSwitch;
    }

    @Override
    public Connection select(List<Connection> connections) {
        try {
            if (CollectionUtils.isEmpty(connections)) {
                return null;
            } else {
                Connection result;
                // 判断连接状态监测开关是否打开
                if (null != this.globalSwitch && this.globalSwitch.isOn(GlobalSwitch.CONN_MONITOR_SWITCH)) {
                    List<Connection> serviceStatusOnConns = new ArrayList<>();
                    for (Connection connection : connections) {
                        String serviceStatus = (String) connection.getAttribute(ConfigsSupport.CONN_SERVICE_STATUS);
                        if (!StringUtils.equals(serviceStatus, ConfigsSupport.CONN_SERVICE_STATUS_OFF)) {
                            serviceStatusOnConns.add(connection);
                        }
                    }
                    if (serviceStatusOnConns.size() == 0) {
                        throw new Exception("No available connection when select in RandomConnectionSelectStrategy.");
                    }
                    result = randomGetConnection(serviceStatusOnConns);
                } else {
                    result = randomGetConnection(connections);
                }
                return result;
            }
        } catch (Throwable e) {
            log.error("Choose connection failed using RandomConnectionSelectStrategy!", e);
            return null;
        }
    }

    private Connection randomGetConnection(List<Connection> connections) {
        if (CollectionUtils.isEmpty(connections)) {
            return null;
        } else {
            int tries = 0;
            Connection result = null;
            int size = connections.size();
            while ((result == null || !result.isFine()) && tries++ < MAX_TIMES) {
                result = connections.get(this.random.nextInt(size));
            }
            // 如果最后一次轮询仍然未获取到可用Connection则返回null
            return (result != null && !result.isFine()) ? null : result;
        }
    }
}