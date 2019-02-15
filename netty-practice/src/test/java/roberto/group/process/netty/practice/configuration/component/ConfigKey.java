/**
 * FileName: ConfigKey
 * Author:   HuangTaiHong
 * Date:     2019/2/15 10:16
 * Description: config key.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration.component;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 〈config key.〉
 *
 * @author HuangTaiHong
 * @create 2019/2/15
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public class ConfigKey<T> {
    private String name;

    private ConfigType<T> configType;

    private T defaultValue;

    private String description;

    public boolean canHasNoDefaultValue() {
        // 不区分类型了，直接这么简单判断就可以了
        return defaultValue == ConfigDefine.NO_DEFAULT_VALUE_LIST || defaultValue == ConfigDefine.NO_DEFAULT_VALUE_STR;
    }
}