/**
 * FileName: RPCCommandFactory
 * Author:   HuangTaiHong
 * Date:     2019/1/6 15:11
 * Description: command factory for rpc protocol.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.factory.impl;

import roberto.group.process.netty.practice.command.command.RemotingCommand;
import roberto.group.process.netty.practice.command.command.request.impl.RPCRequestCommand;
import roberto.group.process.netty.practice.command.command.response.ResponseCommand;
import roberto.group.process.netty.practice.command.command.response.ResponseStatusEnum;
import roberto.group.process.netty.practice.command.command.response.impl.RPCResponseCommand;
import roberto.group.process.netty.practice.command.factory.CommandFactory;
import roberto.group.process.netty.practice.exception.remote.RPCServerException;

import java.net.InetSocketAddress;

/**
 * 〈一句话功能简述〉<br>
 * 〈command factory for rpc protocol.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/6
 * @since 1.0.0
 */
public class RPCCommandFactory implements CommandFactory {
    @Override
    public RPCRequestCommand createRequestCommand(Object requestObject) {
        return new RPCRequestCommand(requestObject);
    }

    @Override
    public RPCResponseCommand createResponse(Object responseObject, RemotingCommand requestCommand) {
        RPCResponseCommand response = new RPCResponseCommand(requestCommand.getId(), responseObject);
        if (responseObject == null) {
            response.setResponseClass(null);
        } else {
            response.setResponseClass(responseObject.getClass().getName());
        }
        response.setSerializer(requestCommand.getSerializer());
        response.setProtocolSwitch(requestCommand.getProtocolSwitch());
        response.setResponseStatus(ResponseStatusEnum.SUCCESS);
        return response;
    }

    @Override
    public RPCResponseCommand createExceptionResponse(int id, String errorMsg) {
        return createExceptionResponse(id, null, errorMsg);
    }

    @Override
    public RPCResponseCommand createExceptionResponse(int id, Throwable t, String errorMsg) {
        RPCResponseCommand response;
        if (t == null) {
            response = new RPCResponseCommand(id, new RPCServerException(errorMsg));
        } else {
            response = new RPCResponseCommand(id, new RPCServerException(errorMsg, t));
        }
        response.setResponseClass(RPCServerException.class.getName());
        response.setResponseStatus(ResponseStatusEnum.SERVER_EXCEPTION);
        return response;
    }

    @Override
    public ResponseCommand createTimeoutResponse(InetSocketAddress address) {
        ResponseCommand responseCommand = new ResponseCommand();
        responseCommand.setResponseStatus(ResponseStatusEnum.TIMEOUT);
        responseCommand.setResponseTimeMillis(System.currentTimeMillis());
        responseCommand.setResponseHost(address);
        return responseCommand;
    }

    @Override
    public ResponseCommand createSendFailedResponse(InetSocketAddress address, Throwable throwable) {
        ResponseCommand responseCommand = new ResponseCommand();
        responseCommand.setResponseStatus(ResponseStatusEnum.CLIENT_SEND_ERROR);
        responseCommand.setResponseTimeMillis(System.currentTimeMillis());
        responseCommand.setResponseHost(address);
        responseCommand.setCause(throwable);
        return responseCommand;
    }

    @Override
    public ResponseCommand createConnectionClosedResponse(InetSocketAddress address, String message) {
        ResponseCommand responseCommand = new ResponseCommand();
        responseCommand.setResponseStatus(ResponseStatusEnum.CONNECTION_CLOSED);
        responseCommand.setResponseTimeMillis(System.currentTimeMillis());
        responseCommand.setResponseHost(address);
        return responseCommand;
    }
}