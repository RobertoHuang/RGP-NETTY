/**
 * FileName: ConfigContainer
 * Author:   HuangTaiHong
 * Date:     2018/12/29 17:28
 * Description: 配置容器接口
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration.configs;

/**
 * 〈一句话功能简述〉<br> 
 * 〈配置容器接口〉
 *
 * @author HuangTaiHong
 * @create 2018/12/29
 * @since 1.0.0
 */
public interface ConfigContainer {
    /**
     * 功能描述: <br>
     * 〈是否包含某配置项〉
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
     * 〈获取指定类型配置项〉
     *
     * @param <T>        the type parameter
     * @param configType
     * @param configItem
     * @return > T
     * @author HuangTaiHong
     * @date 2018.12.29 17:34:42
     */
    <T> T get(ConfigTypeEnum configType, ConfigItemEnum configItem);


    /**
     * 功能描述: <br>
     * 〈设置指定类型配置项〉
     *
     * @param configType
     * @param configItem
     * @param value
     * @author HuangTaiHong
     * @date 2018.12.29 17:35:02
     */
    void set(ConfigTypeEnum configType, ConfigItemEnum configItem, Object value);
}