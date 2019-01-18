/**
 * FileName: InvokeTimeoutException
 * Author:   HuangTaiHong
 * Date:     2019/1/5 16:07
 * Description: Exception when invoke timeout.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.exception.remote;

import lombok.NoArgsConstructor;
import roberto.group.process.netty.practice.exception.RemotingException;

/**
 * 〈一句话功能简述〉<br>
 * 〈Exception when invoke timeout.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/5
 * @since 1.0.0
 */
@NoArgsConstructor
public class InvokeTimeoutException extends RemotingException {
    /** For serialization  */
    private static final long serialVersionUID = -7772633244795043476L;

    public InvokeTimeoutException(String message) {
        super(message);
    }

    public InvokeTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}