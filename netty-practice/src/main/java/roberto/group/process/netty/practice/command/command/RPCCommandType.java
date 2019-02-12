/**
 * FileName: RPCCommandType
 * Author:   HuangTaiHong
 * Date:     2019/1/5 9:53
 * Description: The type of command in the request/response model.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.command;

/**
 * 〈一句话功能简述〉<br> 
 * 〈The type of command in the request/response model.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/5
 * @since 1.0.0
 */
public class RPCCommandType {
    /** RPC response */
    public static final byte RESPONSE = (byte) 0x00;
    /** RPC request */
    public static final byte REQUEST = (byte) 0x01;
    /** RPC oneway request */
    public static final byte REQUEST_ONEWAY = (byte) 0x02;
}