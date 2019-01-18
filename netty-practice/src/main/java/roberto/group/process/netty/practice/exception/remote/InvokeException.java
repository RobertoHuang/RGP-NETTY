/**
 * FileName: InvokeException
 * Author:   HuangTaiHong
 * Date:     2019/1/5 16:32
 * Description: Exception when invoke failed.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.exception.remote;

import lombok.NoArgsConstructor;
import roberto.group.process.netty.practice.exception.RemotingException;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Exception when invoke failed.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/5
 * @since 1.0.0
 */
@NoArgsConstructor
public class InvokeException extends RemotingException {
    /** For serialization  */
    private static final long serialVersionUID = -3974514863386363570L;

    public InvokeException(String message) {
        super(message);
    }

    public InvokeException(String message, Throwable cause) {
        super(message, cause);
    }
}