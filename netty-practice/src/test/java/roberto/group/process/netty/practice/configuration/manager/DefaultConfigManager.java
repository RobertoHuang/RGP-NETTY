/**
 * FileName: DefaultConfigManager
 * Author:   HuangTaiHong
 * Date:     2019/2/15 14:52
 * Description: default config manager.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration.manager;


import roberto.group.process.netty.practice.configuration.component.ConfigDefine;
import roberto.group.process.netty.practice.configuration.component.ConfigType;

import java.util.List;
import java.util.Properties;

/**
 * 〈default config manager.〉
 *
 * @author HuangTaiHong
 * @create 2019/2/15
 * @since 1.0.0
 */
public class DefaultConfigManager extends AbstractConfigManager {
    private static final ConfigDefine CONFIG_DEFINE = new ConfigDefine();

    static {
        CONFIG_DEFINE.add("short_value", ConfigType.SHORT, (short) 0, "short value test");
        CONFIG_DEFINE.add("boolean_value", ConfigType.BOOLEAN, false, "boolean value test");
        CONFIG_DEFINE.add("string_value", ConfigType.STRING, "default value for string", "string value test");
        CONFIG_DEFINE.add("int_value", ConfigType.INT, 0, "int value test");
        CONFIG_DEFINE.add("unsign_int_value", ConfigType.UNSIGN_INT, 0, "unsign int value test");
        CONFIG_DEFINE.add("long_value", ConfigType.LONG, 0L, "long value test");
        CONFIG_DEFINE.add("list_value", ConfigType.LIST, ConfigDefine.NO_DEFAULT_VALUE_LIST, "list value test");
        CONFIG_DEFINE.add("enum_value", ConfigType.ENUM.with("enum_value", "latest", "earliest", "none", "custom"), "latest", "enum value test");
        CONFIG_DEFINE.add("class_value", ConfigType.CLASS, Object.class, "class value test");
    }

    public DefaultConfigManager(Properties properties) {
        super(CONFIG_DEFINE, properties);
    }

    public Short getShortValue() {
        return getShort("short_value");
    }

    public Boolean getBooleanValue() {
        return getBoolean("boolean_value");
    }

    public String getStringValue() {
        return getString("string_value");
    }

    public Integer getIntValue() {
        return getInt("int_value");
    }

    public Integer getUnsignIntValue() {
        return getInt("unsign_int_value");
    }

    public Long getLongValue() {
        return getLong("long_value");
    }

    public List getListValue() {
        return getList("list_value");
    }

    public String getEnumValue() {
        return getString("enum_value");
    }

    public Class getClassValue() {
        return getClass("class_value");
    }
}