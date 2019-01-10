/**
 * FileName: DefaultConfigContainer
 * Author:   HuangTaiHong
 * Date:     2018/12/29 17:31
 * Description: default implementation for config container
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration.container.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import roberto.group.process.netty.practice.configuration.container.ConfigContainer;
import roberto.group.process.netty.practice.configuration.container.ConfigItemEnum;
import roberto.group.process.netty.practice.configuration.container.ConfigTypeEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br>
 * 〈default implementation for config container〉
 *
 * @author HuangTaiHong
 * @create 2018/12/29
 * @since 1.0.0
 */
public class DefaultConfigContainer implements ConfigContainer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConfigContainer.class);

    /** use a hash map to store the user configs with different config types and config items. **/
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

    private void validate(ConfigTypeEnum configType, ConfigItemEnum configItem) {
        if (null == configType || null == configItem) {
            throw new IllegalArgumentException(String.format("ConfigType {%s}, ConfigItem {%s} should not be null!", configType, configItem));
        }
    }

    private void validate(ConfigTypeEnum configType, ConfigItemEnum configItem, Object value) {
        if (null == configType || null == configItem || null == value) {
            throw new IllegalArgumentException(String.format("ConfigType {%s}, ConfigItem {%s}, value {%s} should not be null!", configType, configItem, value == null ? null : value.toString()));
        }
    }
}