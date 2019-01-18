/**
 * FileName: InvokeServerException
 * Author:   HuangTaiHong
 * Date:     2019/1/5 16:29
 * Description: Server exception caught when invoking.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.exception.remote;

import lombok.NoArgsConstructor;
import roberto.group.process.netty.practice.exception.RemotingException;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Server exception caught when invoking.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/5
 * @since 1.0.0
 */
@NoArgsConstructor
public class InvokeServerException extends RemotingException {
    /** For serialization  */
    private static final long serialVersionUID = 4480283862377034355L;

    public InvokeServerException(String message) {
        super(message);
    }

    public InvokeServerException(String message, Throwable cause) {
        super(message, cause);
    }
}