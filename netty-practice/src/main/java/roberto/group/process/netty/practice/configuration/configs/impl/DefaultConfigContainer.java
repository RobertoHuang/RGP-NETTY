/**
 * FileName: DefaultConfigContainer
 * Author:   HuangTaiHong
 * Date:     2018/12/29 17:31
 * Description: 默认配置容器类
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration.configs.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import roberto.group.process.netty.practice.configuration.configs.ConfigContainer;
import roberto.group.process.netty.practice.configuration.configs.ConfigItemEnum;
import roberto.group.process.netty.practice.configuration.configs.ConfigTypeEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br>
 * 〈默认配置容器类〉
 *
 * @author HuangTaiHong
 * @create 2018/12/29
 * @since 1.0.0
 */
public class DefaultConfigContainer implements ConfigContainer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConfigContainer.class);

    /** 用户保存用户自定义配置 **/
    private Map<ConfigTypeEnum, Map<ConfigItemEnum, Object>> userConfigs = new HashMap<ConfigTypeEnum, Map<ConfigItemEnum, Object>>();

    public boolean contains(ConfigTypeEnum configType, ConfigItemEnum configItem) {
        validate(configType, configItem);
        return null != userConfigs.get(configType) && userConfigs.get(configType).containsKey(configItem);
    }

    public <T> T get(ConfigTypeEnum configType, ConfigItemEnum configItem) {
        validate(configType, configItem);
        if (userConfigs.containsKey(configType)) {
            return (T) userConfigs.get(configType).get(configItem);
        }
        return null;
    }

    public void set(ConfigTypeEnum configType, ConfigItemEnum configItem, Object value) {
        validate(configType, configItem, value);
        Map<ConfigItemEnum, Object> items = userConfigs.get(configType);
        if (null == items) {
            items = new HashMap<ConfigItemEnum, Object>();
            userConfigs.put(configType, items);
        }

        Object prev = items.put(configItem, value);
        if (null != prev) {
            LOGGER.warn("the value of ConfigType {}, ConfigItem {} changed from {} to {}", configType, configItem, prev.toString(), value.toString());
        }
    }

    /**
     * 功能描述: <br>
     * 〈校验配置项是否合法〉
     *
     * @param configType
     * @param configItem
     * @author HuangTaiHong
     * @date 2018.12.29 17:36:23
     */
    private void validate(ConfigTypeEnum configType, ConfigItemEnum configItem) {
        if (null == configType || null == configItem) {
            throw new IllegalArgumentException(String.format("ConfigType {%s}, ConfigItem {%s} should not be null!", configType, configItem));
        }
    }

    /**
     * 功能描述: <br>
     * 〈校验配置项是否合法〉
     *
     * @param configType
     * @param configItem
     * @param value
     * @author HuangTaiHong
     * @date 2018.12.29 17:36:23
     */
    private void validate(ConfigTypeEnum configType, ConfigItemEnum configItem, Object value) {
        if (null == configType || null == configItem || null == value) {
            throw new IllegalArgumentException(String.format("ConfigType {%s}, ConfigItem {%s}, value {%s} should not be null!", configType, configItem, value == null ? null : value.toString()));
        }
    }
}