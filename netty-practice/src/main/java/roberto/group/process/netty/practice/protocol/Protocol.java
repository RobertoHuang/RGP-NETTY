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

import roberto.group.process.netty.practice.command.codec.CommandDecoder;
import roberto.group.process.netty.practice.command.codec.CommandEncoder;
import roberto.group.process.netty.practice.command.factory.CommandFactory;
import roberto.group.process.netty.practice.command.handler.CommandHandler;
import roberto.group.process.netty.practice.heartbeat.HeartbeatTrigger;

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
     * 〈Get the newEncoder for the protocol.〉
     *
     * @return > roberto.group.process.netty.practice.command.codec.CommandEncoder
     * @author HuangTaiHong
     * @date 2019.01.02 19:31:51
     */
    CommandEncoder getEncoder();

    /**
     * 功能描述: <br>
     * 〈Get the decoder for the protocol.〉
     *
     * @return > roberto.group.process.netty.practice.command.codec.CommandDecoder
     * @author HuangTaiHong
     * @date 2019.01.02 19:32:00
     */
    CommandDecoder getDecoder();

    /**
     * 功能描述: <br>
     * 〈Get the heartbeat trigger for the protocol.〉
     *
     * @return > roberto.group.process.netty.practice.heartbeat.HeartbeatTrigger
     * @author HuangTaiHong
     * @date 2019.01.08 17:31:14
     */
    HeartbeatTrigger getHeartbeatTrigger();

    /**
     * 功能描述: <br>
     * 〈Get the command handler for the protocol.〉
     *
     * @return > roberto.group.process.netty.practice.command.handler.CommandHandler
     * @author HuangTaiHong
     * @date 2019.01.05 19:51:08
     */
    CommandHandler getCommandHandler();

    /**
     * 功能描述: <br>
     * 〈Get the command factory for the protocol.〉
     *
     * @return > roberto.group.process.netty.practice.command.factory.CommandFactory
     * @author HuangTaiHong
     * @date 2019.01.08 17:31:20
     */
    CommandFactory getCommandFactory();
}