/**
 * FileName: ConnectionClosedException
 * Author:   HuangTaiHong
 * Date:     2019/1/5 16:11
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.exception;

import lombok.NoArgsConstructor;

/**
 * 〈一句话功能简述〉<br>
 * 〈Exception when connection is closed.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/5
 * @since 1.0.0
 */
@NoArgsConstructor
public class ConnectionClosedException extends RemotingException {
    /** For serialization */
    private static final long serialVersionUID = -2595820033346329315L;

    public ConnectionClosedException(String message) {
        super(message);
    }

    public ConnectionClosedException(String message, Throwable cause) {
        super(message, cause);
    }
}