/**
 * FileName: RemoteCommandCode
 * Author:   HuangTaiHong
 * Date:     2018/12/29 15:09
 * Description: Remoting command code.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.code;

/**
 * 〈一句话功能简述〉<br>
 * 〈Remoting command code.〉
 *
 *  Remoting command code stands for a specific remoting command, and every kind of command has its own code.
 *
 * @author HuangTaiHong
 * @create 2018/12/29
 * @since 1.0.0
 */
public enum RemoteCommandCode {
    HEARTBEAT((short) 0), RPC_REQUEST((short) 1), RPC_RESPONSE((short) 2);

    private short value;

    RemoteCommandCode(short value) {
        this.value = value;
    }

    public short value() {
        return this.value;
    }

    public static RemoteCommandCode valueOf(short value) {
        switch (value) {
            case 0:
                return HEARTBEAT;
            case 1:
                return RPC_REQUEST;
            case 2:
                return RPC_RESPONSE;
        }
        throw new IllegalArgumentException("Unknown remote command code value ," + value);
    }
}