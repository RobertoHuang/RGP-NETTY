/**
 * FileName: ConnectionUtil
 * Author:   HuangTaiHong
 * Date:     2019/1/7 18:38
 * Description: connection util
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.utils;

import io.netty.channel.Channel;
import roberto.group.process.netty.practice.connection.Connection;

/**
 * 〈一句话功能简述〉<br>
 * 〈connection util〉
 *
 * @author HuangTaiHong
 * @create 2019/1/7
 * @since 1.0.0
 */
public class ConnectionUtil {
    public static Connection getConnectionFromChannel(Channel channel) {
        if (channel == null) {
            return null;
        } else {
            return channel.attr(Connection.CONNECTION) == null ? null : channel.attr(Connection.CONNECTION).get();
        }
    }
}