/**
 * FileName: SerializationException
 * Author:   HuangTaiHong
 * Date:     2019/1/4 16:09
 * Description: Exception when serialize failed
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.exception;

/**
 * 〈一句话功能简述〉<br>
 * 〈Exception when serialize failed〉
 *
 * @author HuangTaiHong
 * @create 2019/1/4
 * @since 1.0.0
 */
public class SerializationException extends CodecException {
    private static final long serialVersionUID = 5668965722686668067L;

    private boolean serverSide = false;

    public SerializationException() {

    }

    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String message, boolean serverSide) {
        this(message);
        this.serverSide = serverSide;
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializationException(String message, Throwable cause, boolean serverSide) {
        this(message, cause);
        this.serverSide = serverSide;
    }

    public boolean isServerSide() {
        return serverSide;
    }
}