/**
 * FileName: HeartbeatAckCommand
 * Author:   HuangTaiHong
 * Date:     2019/1/7 19:51
 * Description: Heartbeat ack.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.command.response.impl;

import roberto.group.process.netty.practice.command.code.RemoteCommandCode;
import roberto.group.process.netty.practice.command.command.response.ResponseCommand;
import roberto.group.process.netty.practice.command.command.response.ResponseStatusEnum;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Heartbeat ack.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/7
 * @since 1.0.0
 */
public class HeartbeatAckCommand extends ResponseCommand {
    /** For serialization */
    private static final long serialVersionUID = 2584912495844320855L;

    public HeartbeatAckCommand() {
        super(RemoteCommandCode.HEARTBEAT);
        this.setResponseStatus(ResponseStatusEnum.SUCCESS);
    }

    public HeartbeatAckCommand(int id) {
        super(RemoteCommandCode.HEARTBEAT);
        this.setId(id);
        this.setResponseStatus(ResponseStatusEnum.SUCCESS);
    }
}