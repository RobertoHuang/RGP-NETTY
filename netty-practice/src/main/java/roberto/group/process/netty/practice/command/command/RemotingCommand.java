/**
 * FileName: Remoting command
 * Author:   HuangTaiHong
 * Date:     2018/12/29 15:18
 * Description: Remoting command.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.command;

import roberto.group.process.netty.practice.command.code.RemoteCommandCode;
import roberto.group.process.netty.practice.configuration.switches.impl.ProtocolSwitch;
import roberto.group.process.netty.practice.exception.DeserializationException;
import roberto.group.process.netty.practice.exception.SerializationException;
import roberto.group.process.netty.practice.context.InvokeContext;
import roberto.group.process.netty.practice.protocol.ProtocolCode;

import java.io.Serializable;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Remoting command.〉
 *
 * @author HuangTaiHong
 * @create 2018/12/29
 * @since 1.0.0
 */
public interface RemotingCommand extends Serializable {
    /**
     * 功能描述: <br>
     * 〈Get the id of the command.〉
     *
     * @return > int
     * @author HuangTaiHong
     * @date 2019.01.04 14:43:14
     */
    int getId();

    /**
     * 功能描述: <br>
     * 〈Get the code of the protocol that this command belongs to.〉
     *
     * @return > roberto.group.process.netty.practice.protocol.ProtocolCode
     * @author HuangTaiHong
     *  2019.01.04 16:20:35
     */
    ProtocolCode getProtocolCode();

    /**
     * 功能描述: <br>
     * 〈Get the protocol switch status for this command.〉
     *
     * @return > roberto.group.process.netty.practice.configuration.switches.impl.ProtocolSwitch
     * @author HuangTaiHong
     * @date 2019.01.04 17:09:53
     */
    ProtocolSwitch getProtocolSwitch();

    /**
     * 功能描述: <br>
     * 〈Get the command code for this command.〉
     *
     * @return > roberto.group.process.netty.practice.command.code.RemoteCommandCode
     * @author HuangTaiHong
     * @date 2019.01.04 16:21:30
     */
    RemoteCommandCode getRemoteCommandCode();

    /**
     * 功能描述: <br>
     * 〈Get invoke context for this command.〉
     *
     * @return > roberto.group.process.netty.practice.context.InvokeContext
     * @author HuangTaiHong
     * @date 2019.01.04 14:23:51
     */
    InvokeContext getInvokeContext();

    /**
     * 功能描述: <br>
     * 〈Get serializer type for this command.〉
     *
     * @return > byte
     * @author HuangTaiHong
     * @date 2019.01.04 16:29:35
     */
    byte getSerializer();

    /**
     * 功能描述: <br>
     * 〈Serialize all parts of remoting command.〉
     *
     * @throws SerializationException
     * @author HuangTaiHong
     * @date 2019.01.04 16:30:29
     */
    void serialize() throws SerializationException;

    /**
     * 功能描述: <br>
     * 〈Deserialize all parts of remoting command.〉
     *
     * @throws DeserializationException
     * @author HuangTaiHong
     * @date 2019.01.04 16:32:26
     */
    void deserialize() throws DeserializationException;

    /**
     * 功能描述: <br>
     * 〈Serialize content of remoting command.〉
     *
     * @param invokeContext
     * @throws SerializationException
     * @author HuangTaiHong
     * @date 2019.01.04 16:32:57
     */
    void serializeContent(InvokeContext invokeContext) throws SerializationException;

    /**
     * 功能描述: <br>
     * 〈Deserialize content of remoting command.〉
     *
     * @param invokeContext
     * @throws DeserializationException
     * @author HuangTaiHong
     * @date 2019.01.04 16:33:14
     */
    void deserializeContent(InvokeContext invokeContext) throws DeserializationException;
}