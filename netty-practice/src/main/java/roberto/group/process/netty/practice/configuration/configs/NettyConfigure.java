/**
 * FileName: NettyConfigure
 * Author:   HuangTaiHong
 * Date:     2019/1/2 9:56
 * Description: Netty相关配置
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration.configs;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Netty相关配置〉
 *
 * @author HuangTaiHong
 * @create 2019/1/2
 * @since 1.0.0
 */
public interface NettyConfigure {
    /**
     * 功能描述: <br>
     * 〈初始化高低水位〉
     *
     * @param low
     * @param high
     * @author HuangTaiHong
     * @date 2019.01.02 09:56:53
     */
    void initWriteBufferWaterMark(int low, int high);

    /**
     * 功能描述: <br>
     * 〈获取低水位〉
     *
     * @return > int
     * @author HuangTaiHong
     * @date 2019.01.02 09:57:01
     */
    int netty_buffer_low_watermark();

    /**
     * 功能描述: <br>
     * 〈获取高水位〉
     *
     * @return > int
     * @author HuangTaiHong
     * @date 2019.01.02 09:57:11
     */
    int netty_buffer_high_watermark();
}