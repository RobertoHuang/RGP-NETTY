/**
 * FileName: RPCResponseResolver
 * Author:   HuangTaiHong
 * Date:     2019/1/5 16:05
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.remote.remote;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import roberto.group.process.netty.practice.command.command.response.ResponseCommand;
import roberto.group.process.netty.practice.command.command.response.ResponseStatusEnum;
import roberto.group.process.netty.practice.command.command.response.impl.RPCResponseCommand;
import roberto.group.process.netty.practice.exception.CodecException;
import roberto.group.process.netty.practice.exception.ConnectionClosedException;
import roberto.group.process.netty.practice.exception.DeserializationException;
import roberto.group.process.netty.practice.exception.RemotingException;
import roberto.group.process.netty.practice.exception.SerializationException;
import roberto.group.process.netty.practice.exception.remote.InvokeException;
import roberto.group.process.netty.practice.exception.remote.InvokeSendFailedException;
import roberto.group.process.netty.practice.exception.remote.InvokeServerBusyException;
import roberto.group.process.netty.practice.exception.remote.InvokeServerException;
import roberto.group.process.netty.practice.exception.remote.InvokeTimeoutException;

/**
 * 〈一句话功能简述〉<br>
 * 〈Resolve response object from response command.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/5
 * @since 1.0.0
 */
@Slf4j
public class RPCResponseResolver {
    public static Object resolveResponseObject(ResponseCommand responseCommand, String address) throws RemotingException {
        preProcess(responseCommand, address);
        if (responseCommand.getResponseStatus() == ResponseStatusEnum.SUCCESS) {
            return toResponseObject(responseCommand);
        } else {
            String errorMsg = String.format("RPC invocation exception: %s, the address is %s, id=%s", responseCommand.getResponseStatus(), address, responseCommand.getId());
            log.warn(errorMsg);
            if (responseCommand.getCause() != null) {
                throw new InvokeException(errorMsg, responseCommand.getCause());
            } else {
                throw new InvokeException(errorMsg + ", please check the server log for more.");
            }
        }
    }

    private static void preProcess(ResponseCommand responseCommand, String address) throws RemotingException {
        String errorMsg = null;
        RemotingException exception = null;
        if (responseCommand == null) {
            errorMsg = String.format("Rpc invocation timeout[responseCommand null]! the address is %s", address);
            exception = new InvokeTimeoutException(errorMsg);
        } else {
            switch (responseCommand.getResponseStatus()) {
                case TIMEOUT:
                    errorMsg = String.format("Rpc invocation timeout[responseCommand TIMEOUT]! the address is %s", address);
                    exception = new InvokeTimeoutException(errorMsg);
                    break;
                case CLIENT_SEND_ERROR:
                    errorMsg = String.format("Rpc invocation send failed! the address is %s", address);
                    exception = new InvokeSendFailedException(errorMsg, responseCommand.getCause());
                    break;
                case CONNECTION_CLOSED:
                    errorMsg = String.format("Connection closed! the address is %s", address);
                    exception = new ConnectionClosedException(errorMsg);
                    break;
                case SERVER_THREADPOOL_BUSY:
                    errorMsg = String.format("Server thread pool busy! the address is %s, id=%s", address, responseCommand.getId());
                    exception = new InvokeServerBusyException(errorMsg);
                    break;
                case CODEC_EXCEPTION:
                    errorMsg = String.format("Codec exception! the address is %s, id=%s", address, responseCommand.getId());
                    exception = new CodecException(errorMsg);
                    break;
                case SERVER_SERIAL_EXCEPTION:
                    errorMsg = String.format("Server serialize response exception! the address is %s, id=%s, serverSide=true", address, responseCommand.getId());
                    exception = new SerializationException(detailErrMsg(errorMsg, responseCommand), toThrowable(responseCommand), true);
                    break;
                case SERVER_DESERIAL_EXCEPTION:
                    errorMsg = String.format("Server deserialize request exception! the address is %s, id=%s, serverSide=true", address, responseCommand.getId());
                    exception = new DeserializationException(detailErrMsg(errorMsg, responseCommand), toThrowable(responseCommand), true);
                    break;
                case SERVER_EXCEPTION:
                    errorMsg = String.format("Server exception! Please check the server log, the address is %s, id=%s", address, responseCommand.getId());
                    exception = new InvokeServerException(detailErrMsg(errorMsg, responseCommand), toThrowable(responseCommand));
                    break;
                default:
                    break;
            }
        }
        if (StringUtils.isNotBlank(errorMsg)) {
            log.warn(errorMsg);
        }
        if (null != exception) {
            throw exception;
        }
    }

    /**
     * 功能描述: <br>
     * 〈Convert remoting response command to application response object.〉
     *
     * @param responseCommand
     * @return > java.lang.Object
     * @throws CodecException
     * @author HuangTaiHong
     * @date 2019.01.05 16:37:03
     */
    private static Object toResponseObject(ResponseCommand responseCommand) throws CodecException {
        RPCResponseCommand response = (RPCResponseCommand) responseCommand;
        response.deserialize();
        return response.getResponseObject();
    }

    /**
     * 功能描述: <br>
     * 〈Convert remoting response command to throwable if it is a throwable, otherwise return null.〉
     *
     * @param responseCommand
     * @return > java.lang.Throwable
     * @throws CodecException
     * @author HuangTaiHong
     * @date 2019.01.05 16:54:50
     */
    private static Throwable toThrowable(ResponseCommand responseCommand) throws CodecException {
        RPCResponseCommand response = (RPCResponseCommand) responseCommand;
        response.deserialize();
        Object throwable = response.getResponseObject();
        return (throwable != null && throwable instanceof Throwable) ? (Throwable) throwable : null;
    }

    /**
     * 功能描述: <br>
     * 〈Detail your error msg with the error msg returned from response command〉
     *
     * @param clientErrMsg
     * @param responseCommand
     * @return > java.lang.String
     * @author HuangTaiHong
     * @date 2019.01.05 16:55:58
     */
    private static String detailErrMsg(String clientErrMsg, ResponseCommand responseCommand) {
        RPCResponseCommand resp = (RPCResponseCommand) responseCommand;
        if (StringUtils.isNotBlank(resp.getErrorMsg())) {
            return String.format("%s, ServerErrorMsg:%s", clientErrMsg, resp.getErrorMsg());
        } else {
            return String.format("%s, ServerErrorMsg:null", clientErrMsg);
        }
    }
}