/**
 * FileName: ConnectionSelectStrategy
 * Author:   HuangTaiHong
 * Date:     2019/1/3 9:57
 * Description: 连接选择策略
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.connection;

import java.util.List;

/**
 * 〈一句话功能简述〉<br> 
 * 〈连接选择策略〉
 *
 * @author HuangTaiHong
 * @create 2019/1/3
 * @since 1.0.0
 */
public interface ConnectionSelectStrategy {
    /**
     * 功能描述: <br>
     * 〈连接选择策略〉
     *
     * @param connections
     * @return > roberto.group.process.netty.practice.connection.Connection
     * @author HuangTaiHong
     * @date 2019.01.03 09:57:55
     */
    Connection select(List<Connection> connections);
}