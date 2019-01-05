/**
 * FileName: RPCConnectionEventHandler
 * Author:   HuangTaiHong
 * Date:     2019/1/5 13:53
 * Description: ConnectionEventHandler for RPC.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.handler;

import io.netty.channel.ChannelHandlerContext;
import roberto.group.process.netty.practice.configuration.switches.impl.GlobalSwitch;
import roberto.group.process.netty.practice.connection.Connection;

/**
 * 〈一句话功能简述〉<br> 
 * 〈ConnectionEventHandler for RPC.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/5
 * @since 1.0.0
 */
public class RPCConnectionEventHandler extends ConnectionEventHandler {
    public RPCConnectionEventHandler() {
        super();
    }

    public RPCConnectionEventHandler(GlobalSwitch globalSwitch) {
        super(globalSwitch);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Connection connection = ctx.channel().attr(Connection.CONNECTION).get();
        if (connection != null) {
            this.getConnectionManager().remove(connection);
        }
        super.channelInactive(ctx);
    }
}