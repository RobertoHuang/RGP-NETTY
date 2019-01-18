/**
 * FileName: ConnectionEventProcessor
 * Author:   HuangTaiHong
 * Date:     2019/1/3 14:42
 * Description: Process connection events.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.connection.processor;

import roberto.group.process.netty.practice.connection.Connection;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Process connection events.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/3
 * @since 1.0.0
 */
public interface ConnectionEventProcessor {
    /**
     * 功能描述: <br>
     * 〈处理连接事件〉
     *
     * @param remoteAddress
     * @param connection
     * @author HuangTaiHong
     * @date 2019.01.03 14:42:48
     */
    void onEvent(String remoteAddress, Connection connection);
}