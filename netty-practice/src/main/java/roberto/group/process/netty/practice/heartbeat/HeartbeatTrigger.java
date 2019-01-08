/**
 * FileName: HeartbeatTrigger
 * Author:   HuangTaiHong
 * Date:     2019/1/8 16:54
 * Description: Heartbeat triggers here.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.heartbeat;

import io.netty.channel.ChannelHandlerContext;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Heartbeat triggers here.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/8
 * @since 1.0.0
 */
public interface HeartbeatTrigger {
    /**
     * 功能描述: <br>
     * 〈heart beat triggered.〉
     *
     * @param ctx
     * @throws Exception
     * @author HuangTaiHong
     * @date 2019.01.08 16:55:04
     */
    void heartbeatTriggered(final ChannelHandlerContext ctx) throws Exception;
}