/**
 * FileName: ConfigTypeEnum
 * Author:   HuangTaiHong
 * Date:     2018/12/29 17:29
 * Description: type of config
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration.container;

/**
 * 〈一句话功能简述〉<br>
 * 〈type of config〉
 *
 * @author HuangTaiHong
 * @create 2018/12/29
 * @since 1.0.0
 */
public enum ConfigTypeEnum {
    /** configs of this type can only be used in client side **/
    CLIENT_SIDE,

    /** configs of this type can only be used in server side **/
    SERVER_SIDE
}