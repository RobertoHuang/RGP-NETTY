/**
 * FileName: CommandEncoder
 * Author:   HuangTaiHong
 * Date:     2019/1/2 19:30
 * Description: Encode command.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Encode command.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/2
 * @since 1.0.0
 */
public interface CommandEncoder {
    /**
     * 功能描述: <br>
     * 〈Encode object into bytes.〉
     *
     * @param ctx
     * @param msg
     * @param out
     * @throws Exception
     * @author HuangTaiHong
     * @date 2019.01.02 19:31:13
     */
    void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out) throws Exception;
}