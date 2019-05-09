/**
 * FileName: ConfigDefine
 * Author:   HuangTaiHong
 * Date:     2019/5/9 10:50
 * Description: 配置定义解析校验类.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration.component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 〈配置定义解析校验类.〉
 *
 * @author HuangTaiHong
 * @create 2019/5/9
 * @since 1.0.0
 */
public class ConfigDefine {
    private final Map<String, ConfigKey<?>> configKeys = new HashMap<>();
    public static final List<?> NO_DEFAULT_VALUE_LIST = Collections.emptyList();
    public static final String NO_DEFAULT_VALUE_STR = new String("NO_DEFAULT_VALUE");

    public Map<String, ConfigKey<?>> getConfigKeys() {
        return Collections.unmodifiableMap(configKeys);
    }

    public <T> ConfigDefine add(String name, ConfigType<T> type, T defaultValue, String description) {
        if (configKeys.containsKey(name)) {
            throw new IllegalArgumentException("duplicate config key:" + name);
        }
        configKeys.put(name, new ConfigKey<>(name, type, defaultValue, description));
        return this;
    }

    public Map<String, Object> parse(Map<String, ?> props) {
        Map<String, Object> values = new HashMap<>();
        for (ConfigKey<?> key : configKeys.values()) {
            values.put(key.getName(), parseValue(key, props.get(key.getName()), props.containsKey(key.getName())));
        }
        return values;
    }

    private Object parseValue(ConfigKey<?> key, Object value, boolean isSetup) {
        Object parsedValue;
        if (isSetup) {
            parsedValue = key.getConfigType().convertValue(key.getName(), value);
        } else if (key.canHasNoDefaultValue()) {
            throw new IllegalArgumentException("Missing required config key\"" + key.getName() + "\" which has no default value.");
        } else {
            parsedValue = key.getDefaultValue();
        }
        return parsedValue;
    }
}