/**
 * FileName: RequestCommand
 * Author:   HuangTaiHong
 * Date:     2019/1/4 16:13
 * Description: Command of request.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.command.request;

import lombok.Getter;
import lombok.Setter;
import roberto.group.process.netty.practice.command.code.RemoteCommandCode;
import roberto.group.process.netty.practice.command.command.RPCCommandType;
import roberto.group.process.netty.practice.command.command.RPCRemotingCommand;

/**
 * 〈一句话功能简述〉<br>
 * 〈Command of request.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/4
 * @since 1.0.0
 */
public abstract class RequestCommand extends RPCRemotingCommand {
    /** For serialization */
    private static final long serialVersionUID = -3457717009326601317L;

    @Getter
    @Setter
    /** timeout, -1 stands for no timeout */
    private int timeout = -1;

    public RequestCommand() {
        super(RPCCommandType.REQUEST);
    }

    public RequestCommand(RemoteCommandCode remoteCommandCode) {
        super(RPCCommandType.REQUEST, remoteCommandCode);
    }

    public RequestCommand(byte type, RemoteCommandCode remoteCommandCode) {
        super(type, remoteCommandCode);
    }

    public RequestCommand(byte version, byte type, RemoteCommandCode remoteCommandCode) {
        super(version, type, remoteCommandCode);
    }
}