/**
 * FileName: CodecException
 * Author:   HuangTaiHong
 * Date:     2019/1/4 16:10
 * Description: Exception when codec problems occur.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.exception;

import lombok.NoArgsConstructor;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Exception when codec problems occur.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/4
 * @since 1.0.0
 */
@NoArgsConstructor
public class CodecException extends RemotingException{
    private static final long serialVersionUID = -7513762648815278960L;

    public CodecException(String message) {
        super(message);
    }

    public CodecException(String message, Throwable cause) {
        super(message, cause);
    }
}