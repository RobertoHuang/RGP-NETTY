/**
 * FileName: InvokeSendFailedException
 * Author:   HuangTaiHong
 * Date:     2019/1/5 16:10
 * Description: Exception when invoke send failed.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.exception.remote;

import lombok.NoArgsConstructor;
import roberto.group.process.netty.practice.exception.RemotingException;

/**
 * 〈一句话功能简述〉<br>
 * 〈Exception when invoke send failed.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/5
 * @since 1.0.0
 */
@NoArgsConstructor
public class InvokeSendFailedException extends RemotingException {
    /** For serialization */
    private static final long serialVersionUID = 4832257777758730796L;

    public InvokeSendFailedException(String message) {
        super(message);
    }

    public InvokeSendFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}