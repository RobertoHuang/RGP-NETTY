/**
 * FileName: ConfigContainer
 * Author:   HuangTaiHong
 * Date:     2018/12/29 17:28
 * Description: the interface of a config container
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration.container;

/**
 * 〈一句话功能简述〉<br> 
 * 〈the interface of a config container〉
 *
 *  Mainly used to manage config by user api, this is instance related, not globally.
 *  That is to say, different remoting instance client or server hold different ConfigContainer.
 *
 * @author HuangTaiHong
 * @create 2018/12/29
 * @since 1.0.0
 */
public interface ConfigContainer {
    /**
     * 功能描述: <br>
     * 〈check whether a config item of a certain config type exist.〉
     *
     * @param configType
     * @param configItem
     * @return > boolean
     * @author HuangTaiHong
     * @date 2018.12.29 17:34:14
     */
    boolean contains(ConfigTypeEnum configType, ConfigItemEnum configItem);

    /**
     * 功能描述: <br>
     * 〈try to get config value using config type and config item.〉
     *
     * @param <T>
     * @param configType
     * @param configItem
     * @return > T
     * @author HuangTaiHong
     * @date 2018.12.29 17:34:42
     */
    <T> T get(ConfigTypeEnum configType, ConfigItemEnum configItem);

    /**
     * 功能描述: <br>
     * 〈init a config item with certain config type, and the value can be any type.〉
     *
     *  Notice: the value can be overwrite if you set more than once.
     *
     * @param configType
     * @param configItem
     * @param value
     * @author HuangTaiHong
     * @date 2018.12.29 17:35:02
     */
    void set(ConfigTypeEnum configType, ConfigItemEnum configItem, Object value);
}