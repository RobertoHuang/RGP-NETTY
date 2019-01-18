/**
 * FileName: CustomNettyConfigure
 * Author:   HuangTaiHong
 * Date:     2019/1/2 9:56
 * Description: netty related configuration items.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration.configs;

/**
 * 〈一句话功能简述〉<br> 
 * 〈netty related configuration items.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/2
 * @since 1.0.0
 */
public interface CustomNettyConfigure {
    /**
     * 功能描述: <br>
     * 〈Initialize netty write buffer water mark for remoting instance.〉
     *
     * @param low
     * @param high
     * @author HuangTaiHong
     * @date 2019.01.02 09:56:53
     */
    void initWriteBufferWaterMark(int low, int high);

    /**
     * 功能描述: <br>
     * 〈get the low water mark for netty write buffer.〉
     *
     * @return > int
     * @author HuangTaiHong
     * @date 2019.01.02 09:57:01
     */
    int netty_buffer_low_watermark();

    /**
     * 功能描述: <br>
     * 〈get the high water mark for netty write buffer.〉
     *
     * @return > int
     * @author HuangTaiHong
     * @date 2019.01.02 09:57:11
     */
    int netty_buffer_high_watermark();
}