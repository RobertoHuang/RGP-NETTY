/**
 * FileName: ConnectionSelectStrategy
 * Author:   HuangTaiHong
 * Date:     2019/1/5 14:00
 * Description: Select strategy from connection pool.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.connection;

import java.util.List;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Select strategy from connection pool.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/5
 * @since 1.0.0
 */
public interface ConnectionSelectStrategy {
    /**
     * 功能描述: <br>
     * 〈select strategy.〉
     *
     * @param connections
     * @return > roberto.group.process.netty.practice.connection.Connection
     * @author HuangTaiHong
     * @date 2019.01.05 14:00:26
     */
    Connection select(List<Connection> connections);
}