/**
 * FileName: DeserializationException
 * Author:   HuangTaiHong
 * Date:     2019/1/4 16:31
 * Description: Exception when deserialize failed.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.exception;

/**
 * 〈一句话功能简述〉<br>
 * 〈Exception when deserialize failed.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/4
 * @since 1.0.0
 */
public class DeserializationException extends CodecException {
    private static final long serialVersionUID = 310446237157256052L;

    private boolean serverSide = false;

    public DeserializationException() {

    }

    public DeserializationException(String message) {
        super(message);
    }

    public DeserializationException(String message, boolean serverSide) {
        this(message);
        this.serverSide = serverSide;
    }

    public DeserializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeserializationException(String message, Throwable cause, boolean serverSide) {
        this(message, cause);
        this.serverSide = serverSide;
    }

    public boolean isServerSide() {
        return serverSide;
    }
}