/**
 * FileName: FutureTaskNotCompleted
 * Author:   HuangTaiHong
 * Date:     2019/1/8 10:03
 * Description: Exception to represent the run method of a future task has not been completed.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.exception;

import lombok.NoArgsConstructor;

/**
 * 〈一句话功能简述〉<br>
 * 〈Exception to represent the run method of a future task has not been completed.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/8
 * @since 1.0.0
 */
@NoArgsConstructor
public class FutureTaskNotCompleted extends Exception {
    /** For serialization */
    private static final long serialVersionUID = -3635466558774380138L;

    public FutureTaskNotCompleted(String message) {
        super(message);
    }

    public FutureTaskNotCompleted(String message, Throwable cause) {
        super(message, cause);
    }
}