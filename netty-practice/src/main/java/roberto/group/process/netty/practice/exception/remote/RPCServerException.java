/**
 * FileName: RPCServerException
 * Author:   HuangTaiHong
 * Date:     2019/1/6 15:20
 * Description: RPC server exception when processing request
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.exception.remote;

import lombok.NoArgsConstructor;
import roberto.group.process.netty.practice.exception.RemotingException;

/**
 * 〈一句话功能简述〉<br> 
 * 〈RPC server exception when processing request〉
 *
 * @author HuangTaiHong
 * @create 2019/1/6
 * @since 1.0.0
 */
@NoArgsConstructor
public class RPCServerException  extends RemotingException {
    /** For serialization  */
    private static final long serialVersionUID = 4480283862377034355L;

    public RPCServerException(String errorMsg) {
        super(errorMsg);
    }

    public RPCServerException(String errorMsg, Throwable cause) {
        super(errorMsg, cause);
    }
}