/**
 * FileName: GlobalSwitch
 * Author:   HuangTaiHong
 * Date:     2018/12/29 18:18
 * Description: Global switches used in client or server
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration.switches.impl;

import roberto.group.process.netty.practice.configuration.manager.ConfigManager;
import roberto.group.process.netty.practice.configuration.switches.Switch;

import java.util.BitSet;

/**
 * 〈一句话功能简述〉<br>
 * 〈Global switches used in client or server〉
 *
 * @author HuangTaiHong
 * @create 2018/12/29
 * @since 1.0.0
 */
public class GlobalSwitch implements Switch {
    /** 连接断线重连开关 **/
    public static final int CONN_RECONNECT_SWITCH = 0;
    /** 连接状态监测开关 **/
    public static final int CONN_MONITOR_SWITCH = 1;
    /** 连接管理进程开关 **/
    public static final int SERVER_MANAGE_CONNECTION_SWITCH = 2;
    /** 是否同步关闭SERVER开关 **/
    public static final int SERVER_SYNC_STOP = 3;

    /** user settings */
    private BitSet userSettings = new BitSet();

    public GlobalSwitch() {
        if (ConfigManager.conn_reconnect_switch()) {
            userSettings.set(CONN_RECONNECT_SWITCH);
        } else {
            userSettings.clear(CONN_RECONNECT_SWITCH);
        }

        if (ConfigManager.conn_monitor_switch()) {
            userSettings.set(CONN_MONITOR_SWITCH);
        } else {
            userSettings.clear(CONN_MONITOR_SWITCH);
        }
    }

    public void turnOn(int index) {
        this.userSettings.set(index);
    }

    public void turnOff(int index) {
        this.userSettings.clear(index);
    }

    public boolean isOn(int index) {
        return this.userSettings.get(index);
    }
}