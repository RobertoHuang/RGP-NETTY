/**
 * FileName: Switch
 * Author:   HuangTaiHong
 * Date:     2018/12/29 18:05
 * Description: switch interface.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration.switches;

/**
 * 〈一句话功能简述〉<br> 
 * 〈switch interface.〉
 *
 * @author HuangTaiHong
 * @create 2018/12/29
 * @since 1.0.0
 */
public interface Switch {
    /**
     * 功能描述: <br>
     * 〈api for user to turn on a feature.〉
     *
     * @param index
     * @author HuangTaiHong
     * @date 2018.12.29 18:05:58
     */
    void turnOn(int index);

    /**
     * 功能描述: <br>
     * 〈api for user to turn off a feature.〉
     *
     * @param index
     * @author HuangTaiHong
     * @date 2018.12.29 18:06:10
     */
    void turnOff(int index);

    /**
     * 功能描述: <br>
     * 〈check switch whether on.〉
     *
     * @param index
     * @return > boolean
     * @author HuangTaiHong
     * @date 2018.12.29 18:06:16
     */
    boolean isOn(int index);
}