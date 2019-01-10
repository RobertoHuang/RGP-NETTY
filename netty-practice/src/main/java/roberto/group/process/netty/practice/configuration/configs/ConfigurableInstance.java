/**
 * FileName: ConfigurableInstance
 * Author:   HuangTaiHong
 * Date:     2019/1/2 9:58
 * Description: define an interface which can be used to implement configurable apis.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration.configs;

import roberto.group.process.netty.practice.configuration.container.ConfigContainer;
import roberto.group.process.netty.practice.configuration.switches.impl.GlobalSwitch;

/**
 * 〈一句话功能简述〉<br> 
 * 〈define an interface which can be used to implement configurable apis.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/2
 * @since 1.0.0
 */
public interface ConfigurableInstance extends CustomNettyConfigure {
    /**
     * 功能描述: <br>
     * 〈get the config container for current instance〉
     *
     * @return > roberto.group.process.netty.practice.configuration.container.ConfigContainer
     * @author HuangTaiHong
     * @date 2019.01.02 09:59:30
     */
    ConfigContainer configs();

    /**
     * 功能描述: <br>
     * 〈get the global switch for current instance〉
     *
     * @return > roberto.group.process.netty.practice.configuration.switches.impl.GlobalSwitch
     * @author HuangTaiHong
     * @date 2019.01.02 09:59:39
     */
    GlobalSwitch switches();
}