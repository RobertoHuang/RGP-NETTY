/**
 * FileName: Switch
 * Author:   HuangTaiHong
 * Date:     2018/12/29 18:05
 * Description: 开关接口
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration.switches;

/**
 * 〈一句话功能简述〉<br> 
 * 〈开关接口〉
 *
 * @author HuangTaiHong
 * @create 2018/12/29
 * @since 1.0.0
 */
public interface Switch {
    /**
     * 功能描述: <br>
     * 〈打开开关〉
     *
     * @param index
     * @author HuangTaiHong
     * @date 2018.12.29 18:05:58
     */
    void turnOn(int index);

    /**
     * 功能描述: <br>
     * 〈关闭开关〉
     *
     * @param index
     * @author HuangTaiHong
     * @date 2018.12.29 18:06:10
     */
    void turnOff(int index);

    /**
     * 功能描述: <br>
     * 〈是否打开状态〉
     *
     * @param index
     * @return > boolean
     * @author HuangTaiHong
     * @date 2018.12.29 18:06:16
     */
    boolean isOn(int index);
}