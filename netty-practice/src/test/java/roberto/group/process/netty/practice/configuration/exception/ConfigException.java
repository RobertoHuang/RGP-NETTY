/**
 * FileName: ConfigException
 * Author:   HuangTaiHong
 * Date:     2019/5/9 16:20
 * Description: config exception.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration.exception;

/**
 * 〈config exception.〉
 *
 * @author HuangTaiHong
 * @create 2019/5/9
 * @since 1.0.0
 */
public class ConfigException extends RuntimeException {
    private static final long serialVersionUID = -3491021984338790284L;

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String name, Object value) {
        this(name, value, null);
    }

    public ConfigException(String name, Object value, String message) {
        super("Invalid value " + value + " for configuration " + name + (message == null ? "" : ": " + message));
    }
}