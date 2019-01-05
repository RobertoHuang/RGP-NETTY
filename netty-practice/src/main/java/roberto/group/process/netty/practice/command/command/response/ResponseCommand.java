/**
 * FileName: ResponseCommand
 * Author:   HuangTaiHong
 * Date:     2019/1/5 10:19
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.command.response;

import lombok.Getter;
import lombok.Setter;
import roberto.group.process.netty.practice.command.code.RemoteCommandCode;
import roberto.group.process.netty.practice.command.command.RPCCommandType;
import roberto.group.process.netty.practice.command.command.RPCRemotingCommand;

import java.net.InetSocketAddress;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author HuangTaiHong
 * @create 2019/1/5
 * @since 1.0.0
 */
public class ResponseCommand extends RPCRemotingCommand {
    /** For serialization */
    private static final long serialVersionUID = -5194754228565292441L;

    @Getter
    @Setter
    private Throwable cause;

    @Getter
    @Setter
    private long responseTimeMillis;

    @Getter
    @Setter
    private ResponseCommandStatus responseCommandStatus;

    @Getter
    @Setter
    private InetSocketAddress responseHost;

    public ResponseCommand() {
        super(RPCCommandType.RESPONSE);
    }

    public ResponseCommand(int id) {
        super(RPCCommandType.RESPONSE);
        this.setId(id);
    }

    public ResponseCommand(RemoteCommandCode remoteCommandCode) {
        super(RPCCommandType.RESPONSE, remoteCommandCode);
    }

    public ResponseCommand(RemoteCommandCode remoteCommandCode, int id) {
        super(RPCCommandType.RESPONSE, remoteCommandCode);
        this.setId(id);
    }

    public ResponseCommand(byte version, byte type, RemoteCommandCode remoteCommandCode, int id) {
        super(version, type, remoteCommandCode);
        this.setId(id);
    }
}