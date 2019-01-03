/**
 * FileName: ConfigurableInstance
 * Author:   HuangTaiHong
 * Date:     2019/1/2 9:58
 * Description: 定义配置接口
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration.configs;

import roberto.group.process.netty.practice.configuration.container.ConfigContainer;
import roberto.group.process.netty.practice.configuration.switches.impl.GlobalSwitch;

/**
 * 〈一句话功能简述〉<br> 
 * 〈定义配置接口〉
 *
 * @author HuangTaiHong
 * @create 2019/1/2
 * @since 1.0.0
 */
public interface ConfigurableInstance extends NettyConfigure{
    /**
     * 功能描述: <br>
     * 〈获取配置容器〉
     *
     * @return > roberto.group.process.netty.practice.configuration.container.ConfigContainer
     * @author HuangTaiHong
     * @date 2019.01.02 09:59:30
     */
    ConfigContainer configs();

    /**
     * 功能描述: <br>
     * 〈获取配置开关〉
     *
     * @return > roberto.group.process.netty.practice.configuration.switches.impl.GlobalSwitch
     * @author HuangTaiHong
     * @date 2019.01.02 09:59:39
     */
    GlobalSwitch switches();
}