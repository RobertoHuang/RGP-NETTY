/**
 * FileName: InvokeServerBusyException
 * Author:   HuangTaiHong
 * Date:     2019/1/5 16:25
 * Description: Exception when thread pool busy of process server.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.exception.remote;

import lombok.NoArgsConstructor;
import roberto.group.process.netty.practice.exception.RemotingException;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Exception when thread pool busy of process server.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/5
 * @since 1.0.0
 */
@NoArgsConstructor
public class InvokeServerBusyException extends RemotingException {
    /** For serialization  */
    private static final long serialVersionUID = 4480283862377034355L;

    public InvokeServerBusyException(String message) {
        super(message);
    }

    public InvokeServerBusyException(String message, Throwable cause) {
        super(message, cause);
    }
}