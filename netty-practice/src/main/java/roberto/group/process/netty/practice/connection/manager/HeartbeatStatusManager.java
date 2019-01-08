/**
 * FileName: HeartbeatStatusManager
 * Author:   HuangTaiHong
 * Date:     2019/1/8 9:48
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.connection.manager;

import roberto.group.process.netty.practice.connection.Connection;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Connection heart beat manager, operate heart beat whether enabled for a certain connection at runtime〉
 *
 * @author HuangTaiHong
 * @create 2019/1/8
 * @since 1.0.0
 */
public interface HeartbeatStatusManager {
    /**
     * 功能描述: <br>
     * 〈enable heart beat for a certain connection〉
     *
     * @param connection
     * @author HuangTaiHong
     * @date 2019.01.08 09:49:23
     */
    void enableHeartbeat(Connection connection);

    /**
     * 功能描述: <br>
     * 〈disable heart beat for a certain connection〉
     *
     * @param connection
     * @author HuangTaiHong
     * @date 2019.01.08 09:49:29
     */
    void disableHeartbeat(Connection connection);
}