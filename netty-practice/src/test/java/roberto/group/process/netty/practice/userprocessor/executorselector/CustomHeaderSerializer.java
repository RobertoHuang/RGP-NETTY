/**
 * FileName: CustomHeaderSerializer
 * Author:   HuangTaiHong
 * Date:     2019/1/16 15:42
 * Description: custom header serializer.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.userprocessor.executorselector;

import lombok.extern.slf4j.Slf4j;
import roberto.group.process.netty.practice.command.command.request.RequestCommand;
import roberto.group.process.netty.practice.command.command.request.impl.RPCRequestCommand;
import roberto.group.process.netty.practice.command.command.response.ResponseCommand;
import roberto.group.process.netty.practice.command.command.response.impl.RPCResponseCommand;
import roberto.group.process.netty.practice.exception.DeserializationException;
import roberto.group.process.netty.practice.exception.SerializationException;
import roberto.group.process.netty.practice.remote.invoke.context.InvokeContext;
import roberto.group.process.netty.practice.serialize.custom.DefaultCustomSerializer;
import static roberto.group.process.netty.practice.userprocessor.executorselector.DefaultExecutorSelector.EXECUTOR1;

import java.io.UnsupportedEncodingException;

/**
 * 〈一句话功能简述〉<br> 
 * 〈custom header serializer.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/16
 * @since 1.0.0
 */
@Slf4j
public class CustomHeaderSerializer extends DefaultCustomSerializer {
    @Override
    public <T extends RequestCommand> boolean serializeHeader(T request, InvokeContext invokeContext) throws SerializationException {
        if (request instanceof RPCRequestCommand) {
            RPCRequestCommand requestCommand = (RPCRequestCommand) request;
            try {
                requestCommand.setHeader(EXECUTOR1.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                log.error("UnsupportedEncodingException");
            }
            return true;
        }
        return false;
    }

    @Override
    public <T extends RequestCommand> boolean deserializeHeader(T request) throws DeserializationException {
        if (request instanceof RPCRequestCommand) {
            RPCRequestCommand requestCommand = (RPCRequestCommand) request;
            byte[] header = requestCommand.getHeader();
            try {
                requestCommand.setRequestHeader(new String(header, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                log.error("UnsupportedEncodingException");
            }
            return true;
        }
        return false;
    }

    @Override
    public <T extends ResponseCommand> boolean serializeHeader(T response) throws SerializationException {
        if (response instanceof RPCResponseCommand) {
            RPCResponseCommand responseCommand = (RPCResponseCommand) response;
            try {
                responseCommand.setHeader(EXECUTOR1.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                log.error("UnsupportedEncodingException");
            }
            return true;
        }
        return false;
    }

    @Override
    public <T extends ResponseCommand> boolean deserializeHeader(T response, InvokeContext invokeContext) throws DeserializationException {
        if (response instanceof RPCResponseCommand) {
            RPCResponseCommand responseCommand = (RPCResponseCommand) response;
            byte[] header = responseCommand.getHeader();
            try {
                responseCommand.setResponseHeader(new String(header, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                log.error("UnsupportedEncodingException");
            }
            return true;
        }
        return false;
    }
}