/**
 * FileName: ConfigTypeTest
 * Author:   HuangTaiHong
 * Date:     2019/5/9 13:59
 * Description: config type test.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import roberto.group.process.netty.practice.configuration.component.ConfigType;
import roberto.group.process.netty.practice.configuration.exception.ConfigException;

import java.util.Arrays;
import java.util.List;

/**
 * 〈config type test.〉
 *
 * @author HuangTaiHong
 * @create 2019/5/9
 * @since 1.0.0
 */
@RunWith(JUnit4.class)
public class ConfigTypeTest {
    @Test(expected = ConfigException.class)
    public void fieldTypeShortTest() {
        Short short1 = ConfigType.SHORT.convertValue("test", null);
        Assert.assertEquals(short1, null);
        Short short2 = ConfigType.SHORT.convertValue("test", "1");
        Assert.assertEquals(short2, new Short((short) 1));
        Short short3 = ConfigType.SHORT.convertValue("test", (short) 1);
        Assert.assertEquals(short3, new Short((short) 1));
        ConfigType.SHORT.convertValue("test", "roberto-test");
    }

    @Test(expected = ConfigException.class)
    public void fieldTypeBooleanTest() {
        Boolean boolean1 = ConfigType.BOOLEAN.convertValue("test", null);
        Assert.assertEquals(boolean1, null);
        Boolean boolean2 = ConfigType.BOOLEAN.convertValue("test", "true");
        Assert.assertEquals(boolean2, true);
        Boolean boolean3 = ConfigType.BOOLEAN.convertValue("test", true);
        Assert.assertEquals(boolean3, true);
        ConfigType.BOOLEAN.convertValue("test", "roberto-test");
    }

    @Test(expected = ConfigException.class)
    public void fieldTypeStringTest() {
        String string1 = ConfigType.STRING.convertValue("test", null);
        Assert.assertEquals(string1, null);
        String string2 = ConfigType.STRING.convertValue("test", "HELLO WORLD");
        Assert.assertEquals(string2, "HELLO WORLD");
        ConfigType.STRING.convertValue("test", new Object());
    }

    @Test(expected = ConfigException.class)
    public void fieldTypeIntTest() {
        Integer integer1 = ConfigType.INT.convertValue("test", null);
        Assert.assertEquals(integer1, null);
        Integer integer2 = ConfigType.INT.convertValue("test", "1");
        Assert.assertEquals(integer2, Integer.valueOf(1));
        Integer integer3 = ConfigType.INT.convertValue("test", 1);
        Assert.assertEquals(integer3, Integer.valueOf(1));
        ConfigType.INT.convertValue("test", "roberto-test");
    }

    @Test(expected = ConfigException.class)
    public void fieldTypeUnsignIntTest() {
        Integer integer1 = ConfigType.UNSIGN_INT.convertValue("test", null);
        Assert.assertEquals(integer1, null);
        Integer integer2 = ConfigType.UNSIGN_INT.convertValue("test", "1");
        Assert.assertEquals(integer2, Integer.valueOf(1));
        Integer integer3 = ConfigType.UNSIGN_INT.convertValue("test", 1);
        Assert.assertEquals(integer3, Integer.valueOf(1));
        ConfigType.UNSIGN_INT.convertValue("test", -1);
    }

    @Test(expected = ConfigException.class)
    public void fieldTypeLongTest() {
        Long long1 = ConfigType.LONG.convertValue("test", null);
        Assert.assertEquals(long1, null);
        Long long2 = ConfigType.LONG.convertValue("test", "1");
        Assert.assertEquals(long2, Long.valueOf(1L));
        Long long3 = ConfigType.LONG.convertValue("test", 1L);
        Assert.assertEquals(long3, Long.valueOf(1L));
        ConfigType.LONG.convertValue("test", "roberto-test");
    }

    @Test(expected = ConfigException.class)
    public void fieldTypeListTest() {
        List list1 = ConfigType.LIST.convertValue("test", null);
        Assert.assertEquals(list1, null);
        List<String> list2 = (List<String>) ConfigType.LIST.convertValue("test", "a,b,c");
        Assert.assertEquals(list2, Arrays.asList("a", "b", "c"));
        List<String> list3 = (List<String>) ConfigType.LIST.convertValue("test",  Arrays.asList("a", "b", "c"));
        Assert.assertEquals(list3, Arrays.asList("a", "b", "c"));
        ConfigType.LIST.convertValue("test", new Object());
    }

    @Test(expected = ConfigException.class)
    public void fieldTypeEnumTest() {
        ConfigType.ENUM.with("test", "a", "b", "c");
        ConfigType.ENUM.convertValue("test", "roberto-test");
    }

    @Test(expected = ConfigException.class)
    public void fieldTypeClassTest() {
        Class<?> class1 = ConfigType.CLASS.convertValue("test", "com.ucarinc.loghub.core.config.ConfigType");
        Assert.assertEquals(class1, ConfigType.class);

        ConfigType.CLASS.convertValue("test", "com.ucarinc.loghub.core.roberto-test");
    }
}