/**
 * FileName: CustomSerializer
 * Author:   HuangTaiHong
 * Date:     2019/1/5 10:17
 * Description: Define custom serializers for command header and content.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.serialize.custom;

import roberto.group.process.netty.practice.command.command.request.RequestCommand;
import roberto.group.process.netty.practice.command.command.response.ResponseCommand;
import roberto.group.process.netty.practice.exception.DeserializationException;
import roberto.group.process.netty.practice.exception.SerializationException;
import roberto.group.process.netty.practice.context.InvokeContext;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Define custom serializers for command header and content.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/5
 * @since 1.0.0
 */
public interface CustomSerializer {
    /**
     * 功能描述: <br>
     * 〈Serialize the header of RequestCommand.〉
     *
     * @param <T>
     * @param request
     * @param invokeContext
     * @return > boolean
     * @throws SerializationException
     * @author HuangTaiHong
     * @date 2019.01.05 10:18:56
     */
    <T extends RequestCommand> boolean serializeHeader(T request, InvokeContext invokeContext) throws SerializationException;

    /**
     * 功能描述: <br>
     * 〈Deserialize the header of RequestCommand.〉
     *
     * @param <T>
     * @param request
     * @return > boolean
     * @throws DeserializationException
     * @author HuangTaiHong
     * @date 2019.01.05 10:20:49
     */
    <T extends RequestCommand> boolean deserializeHeader(T request) throws DeserializationException;

    /**
     * 功能描述: <br>
     * 〈Serialize the header of ResponseCommand.〉
     *
     * @param <T>
     * @param response
     * @return > boolean
     * @throws SerializationException
     * @author HuangTaiHong
     * @date 2019.01.05 10:20:12
     */
    <T extends ResponseCommand> boolean serializeHeader(T response) throws SerializationException;


    /**
     * 功能描述: <br>
     * 〈Deserialize the header of ResponseCommand.〉
     *
     * @param <T>
     * @param response
     * @param invokeContext
     * @return > boolean
     * @throws DeserializationException
     * @author HuangTaiHong
     * @date 2019.01.05 10:21:26
     */
    <T extends ResponseCommand> boolean deserializeHeader(T response, InvokeContext invokeContext) throws DeserializationException;


    /**
     * 功能描述: <br>
     * 〈Serialize the content of RequestCommand.〉
     *
     * @param <T>
     * @param request
     * @param invokeContext
     * @return > boolean
     * @throws SerializationException
     * @author HuangTaiHong
     * @date 2019.01.05 10:22:24
     */
    <T extends RequestCommand> boolean serializeContent(T request, InvokeContext invokeContext) throws SerializationException;

    /**
     * 功能描述: <br>
     * 〈Deserialize the content of RequestCommand.〉
     *
     * @param <T>     the type parameter
     * @param request
     * @return > boolean
     * @throws DeserializationException
     * @author HuangTaiHong
     * @date 2019.01.05 10:22:56
     */
    <T extends RequestCommand> boolean deserializeContent(T request) throws DeserializationException;

    /**
     * 功能描述: <br>
     * 〈Serialize the content of ResponseCommand.〉
     *
     * @param <T>      the type parameter
     * @param response
     * @return > boolean
     * @throws SerializationException
     * @author HuangTaiHong
     * @date 2019.01.05 10:22:35
     */
    <T extends ResponseCommand> boolean serializeContent(T response) throws SerializationException;

    /**
     * 功能描述: <br>
     * 〈Deserialize the content of ResponseCommand.〉
     *
     * @param <T>
     * @param response
     * @param invokeContext
     * @return > boolean
     * @throws DeserializationException
     * @author HuangTaiHong
     * @date 2019.01.05 10:23:09
     */
    <T extends ResponseCommand> boolean deserializeContent(T response, InvokeContext invokeContext) throws DeserializationException;
}