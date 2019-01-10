/**
 * FileName: AbstractConfigurableInstance
 * Author:   HuangTaiHong
 * Date:     2019/1/2 10:04
 * Description: common implementation for a configurable instance
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration.configs.impl;

import roberto.group.process.netty.practice.configuration.manager.ConfigManager;
import roberto.group.process.netty.practice.configuration.configs.ConfigurableInstance;
import roberto.group.process.netty.practice.configuration.container.ConfigContainer;
import roberto.group.process.netty.practice.configuration.container.ConfigItemEnum;
import roberto.group.process.netty.practice.configuration.container.ConfigTypeEnum;
import roberto.group.process.netty.practice.configuration.container.impl.DefaultConfigContainer;
import roberto.group.process.netty.practice.configuration.switches.impl.GlobalSwitch;

/**
 * 〈一句话功能简述〉<br>
 * 〈common implementation for a configurable instance〉
 *
 * @author HuangTaiHong
 * @create 2019/1/2
 * @since 1.0.0
 */
public abstract class AbstractConfigurableInstance implements ConfigurableInstance {
    private ConfigTypeEnum configType;
    private GlobalSwitch globalSwitch = new GlobalSwitch();
    private ConfigContainer configContainer = new DefaultConfigContainer();

    protected AbstractConfigurableInstance(ConfigTypeEnum configType) {
        this.configType = configType;
    }

    public GlobalSwitch switches() {
        return this.globalSwitch;
    }

    public ConfigContainer configs() {
        return this.configContainer;
    }

    public void initWriteBufferWaterMark(int low, int high) {
        this.configContainer.set(configType, ConfigItemEnum.NETTY_BUFFER_LOW_WATER_MARK, low);
        this.configContainer.set(configType, ConfigItemEnum.NETTY_BUFFER_HIGH_WATER_MARK, high);
    }

    public int netty_buffer_low_watermark() {
        if (null != configContainer && configContainer.contains(configType, ConfigItemEnum.NETTY_BUFFER_LOW_WATER_MARK)) {
            return (Integer) configContainer.get(configType, ConfigItemEnum.NETTY_BUFFER_LOW_WATER_MARK);
        } else {
            return ConfigManager.netty_buffer_low_watermark();
        }
    }

    public int netty_buffer_high_watermark() {
        if (null != configContainer && configContainer.contains(configType, ConfigItemEnum.NETTY_BUFFER_HIGH_WATER_MARK)) {
            return (Integer) configContainer.get(configType, ConfigItemEnum.NETTY_BUFFER_HIGH_WATER_MARK);
        } else {
            return ConfigManager.netty_buffer_high_watermark();
        }
    }
}