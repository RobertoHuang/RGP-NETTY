/**
 * FileName: ConfigTest
 * Author:   HuangTaiHong
 * Date:     2019/2/15 13:53
 * Description: config test.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import roberto.group.process.netty.practice.configuration.manager.DefaultConfigManager;

import java.util.Properties;

/**
 * 〈config test.〉
 *
 * @author HuangTaiHong
 * @create 2019/2/15
 * @since 1.0.0
 */
@Slf4j
public class ConfigTest {
    @Test
    public void testProperties() {
        Properties properties = new Properties();
        properties.put("short_value", (short) 3);
        properties.put("boolean_value", true);
        properties.put("string_value", "roberto growth process");
        properties.put("int_value", 940616);
        properties.put("unsign_int_value", 940616);
        properties.put("long_int_value", 201210704116L);
        properties.put("list_value", "d,r,e,a,m,T");
        properties.put("enum_value", "latest");
        properties.put("class_value", "java.lang.String");

        DefaultConfigManager configManager = new DefaultConfigManager(properties);
        log.info("short_value:" + configManager.getShortValue());
        log.info("boolean_value:" + configManager.getBooleanValue());
        log.info("string_value:" + configManager.getStringValue());
        log.info("int_value:" + configManager.getIntValue());
        log.info("unsign_int_value:" + configManager.getUnsignIntValue());
        log.info("long_int_value:" + configManager.getLongValue());
        log.info("list_value:" + configManager.getListValue());
        log.info("enum_value:" + configManager.getEnumValue());
        log.info("class_value:" + configManager.getClassValue());
    }
}