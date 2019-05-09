/**
 * FileName: ConfigDefineTest
 * Author:   HuangTaiHong
 * Date:     2019/5/9 15:12
 * Description: config define test.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import roberto.group.process.netty.practice.configuration.component.ConfigDefine;
import roberto.group.process.netty.practice.configuration.component.ConfigType;

import java.util.HashMap;
import java.util.Map;

/**
 * 〈config define test.〉
 *
 * @author HuangTaiHong
 * @create 2019/5/9
 * @since 1.0.0
 */
@RunWith(JUnit4.class)
public class ConfigDefineTest {
    @Test(expected = IllegalArgumentException.class)
    public void configDefineTest() {
        ConfigDefine configDefine = new ConfigDefine();
        configDefine.add("test", ConfigType.STRING, "roberto-test", "测试ConfigDefine.");
        Map<String, Object> hashMap = new HashMap<>();
        Map<String, Object> parse = configDefine.parse(hashMap);
        Assert.assertEquals(parse.size(), 1);
        Assert.assertEquals(parse.get("test"),"roberto-test");

        ConfigDefine configDefine2 = new ConfigDefine();
        configDefine2.add("test", ConfigType.STRING, ConfigDefine.NO_DEFAULT_VALUE_STR, "测试无默认值");
        configDefine2.parse( new HashMap<>());
    }
}