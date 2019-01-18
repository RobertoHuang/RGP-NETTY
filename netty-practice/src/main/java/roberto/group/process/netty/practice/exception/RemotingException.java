/**
 * FileName: RemotingException
 * Author:   HuangTaiHong
 * Date:     2019/1/3 9:43
 * Description: Exception for default remoting problems.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.exception;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Exception for default remoting problems.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/3
 * @since 1.0.0
 */
public class RemotingException extends Exception{
    private static final long serialVersionUID = 6183635628271812505L;

    public RemotingException() {

    }

    public RemotingException(String message) {
        super(message);
    }

    public RemotingException(String message, Throwable cause) {
        super(message, cause);
    }
}