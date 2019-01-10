/**
 * FileName: ConnectionMonitorStrategy
 * Author:   HuangTaiHong
 * Date:     2019/1/9 17:03
 * Description: The strategy of connection monitor
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.connection.strategy;

import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.connection.ConnectionPool;
import roberto.group.process.netty.practice.utils.RunStateRecordedFutureTask;

import java.util.List;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br> 
 * 〈The strategy of connection monitor.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/9
 * @since 1.0.0
 */
public interface ConnectionMonitorStrategy {
    /**
     * 功能描述: <br>
     * 〈Filter connections to monitor.〉
     *
     * @param connections
     * @return > java.util.Map<java.lang.String,java.util.List<roberto.group.process.netty.practice.connection.Connection>>
     * @author HuangTaiHong
     * @date 2019.01.09 17:04:20
     */
    Map<String, List<Connection>> filter(List<Connection> connections);

    /**
     * 功能描述: <br>
     * 〈Add a set of connections to monitor.〉
     *
     * @param connectionPools
     * @author HuangTaiHong
     * @date 2019.01.09 17:04:28
     */
    void monitor(Map<String, RunStateRecordedFutureTask<ConnectionPool>> connectionPools);
}