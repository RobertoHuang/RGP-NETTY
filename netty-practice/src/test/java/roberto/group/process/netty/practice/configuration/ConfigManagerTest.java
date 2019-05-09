/**
 * FileName: ConfigManagerTest
 * Author:   HuangTaiHong
 * Date:     2019/5/9 15:23
 * Description: config manager test.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import roberto.group.process.netty.practice.configuration.component.ConfigDefine;
import roberto.group.process.netty.practice.configuration.component.ConfigType;
import roberto.group.process.netty.practice.configuration.manager.AbstractConfigManager;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * 〈config manager test.〉
 *
 * @author HuangTaiHong
 * @create 2019/5/9
 * @since 1.0.0
 */
@RunWith(JUnit4.class)
public class ConfigManagerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManagerTest.class);

    @Test
    public void testConfigManager() {
        Properties properties = new Properties();
        properties.put("short_value", (short) 3);
        properties.put("boolean_value", true);
        properties.put("string_value", "roberto-test");
        properties.put("int_value", 940616);
        properties.put("unsign_int_value", 940616);
        properties.put("long_int_value", 201210704116L);
        properties.put("list_value", "r,o,b,e,r,t,o");
        properties.put("enum_value", "latest");
        properties.put("class_value", "java.lang.String");
        properties.put("list_class_value", Arrays.asList("java.lang.StringBuffer", "java.lang.StringBuilder"));

        DefaultConfigManager configManager = new DefaultConfigManager(properties);
        LOGGER.info("short_value:" + configManager.getShortValue());
        LOGGER.info("boolean_value:" + configManager.getBooleanValue());
        LOGGER.info("string_value:" + configManager.getStringValue());
        LOGGER.info("int_value:" + configManager.getIntValue());
        LOGGER.info("unsign_int_value:" + configManager.getUnsignIntValue());
        LOGGER.info("long_int_value:" + configManager.getLongValue());
        LOGGER.info("list_value:" + configManager.getListValue());
        LOGGER.info("enum_value:" + configManager.getEnumValue());
        LOGGER.info("class_value:" + configManager.getClassValue());
        LOGGER.info("class_value_instance:" + configManager.getClassValueInstance());
        LOGGER.info("list_class_value_instance:" + configManager.getClassValueListInstance());
    }
}

class DefaultConfigManager extends AbstractConfigManager {
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
        CONFIG_DEFINE.add("list_class_value", ConfigType.LIST, ConfigDefine.NO_DEFAULT_VALUE_LIST, "list class value test");
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

    public String getClassValueInstance() {
        return getInstance("class_value", String.class);
    }

    public List<Appendable> getClassValueListInstance() {
        return getListInstance("list_class_value", Appendable.class);
    }
}