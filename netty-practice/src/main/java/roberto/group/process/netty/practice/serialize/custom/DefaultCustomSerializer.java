/**
 * FileName: DefaultCustomSerializer
 * Author:   HuangTaiHong
 * Date:     2019/1/16 9:57
 * Description: The default custom serializer, which does nothing.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.serialize.custom;

import roberto.group.process.netty.practice.command.command.request.RequestCommand;
import roberto.group.process.netty.practice.command.command.response.ResponseCommand;
import roberto.group.process.netty.practice.exception.DeserializationException;
import roberto.group.process.netty.practice.exception.SerializationException;
import roberto.group.process.netty.practice.remote.invoke.context.InvokeContext;

/**
 * 〈一句话功能简述〉<br> 
 * 〈The default custom serializer, which does nothing.〉
 *
 *  Extend this class and override the methods you want to custom.
 *
 * @author HuangTaiHong
 * @create 2019/1/16
 * @since 1.0.0
 */
public class DefaultCustomSerializer implements CustomSerializer{
    @Override
    public <T extends RequestCommand> boolean serializeHeader(T request, InvokeContext invokeContext) throws SerializationException {
        return false;
    }

    @Override
    public <T extends RequestCommand> boolean deserializeHeader(T request) throws DeserializationException {
        return false;
    }

    @Override
    public <T extends ResponseCommand> boolean serializeHeader(T response) throws SerializationException {
        return false;
    }

    @Override
    public <T extends ResponseCommand> boolean deserializeHeader(T response, InvokeContext invokeContext) throws DeserializationException {
        return false;
    }

    @Override
    public <T extends RequestCommand> boolean serializeContent(T request, InvokeContext invokeContext) throws SerializationException {
        return false;
    }

    @Override
    public <T extends RequestCommand> boolean deserializeContent(T request) throws DeserializationException {
        return false;
    }

    @Override
    public <T extends ResponseCommand> boolean serializeContent(T response) throws SerializationException {
        return false;
    }

    @Override
    public <T extends ResponseCommand> boolean deserializeContent(T response, InvokeContext invokeContext) throws DeserializationException {
        return false;
    }
}