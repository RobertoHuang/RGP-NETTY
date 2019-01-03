/**
 * FileName: Protocol
 * Author:   HuangTaiHong
 * Date:     2019/1/2 14:06
 * Description: 通讯协议接口
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.protocol;

import roberto.group.process.netty.practice.protocol.codec.CommandDecoder;
import roberto.group.process.netty.practice.protocol.codec.CommandEncoder;

/**
 * 〈一句话功能简述〉<br> 
 * 〈通讯协议接口〉
 *
 * @author HuangTaiHong
 * @create 2019/1/2
 * @since 1.0.0
 */
public interface Protocol {
    /**
     * 功能描述: <br>
     * 〈获取命令编码器〉
     *
     * @return > roberto.group.process.netty.practice.protocol.codec.CommandEncoder
     * @author HuangTaiHong
     * @date 2019.01.02 19:31:51
     */
    CommandEncoder getEncoder();

    /**
     * 功能描述: <br>
     * 〈获取命令解码器〉
     *
     * @return > roberto.group.process.netty.practice.protocol.codec.CommandDecoder
     * @author HuangTaiHong
     * @date 2019.01.02 19:32:00
     */
    CommandDecoder getDecoder();
}