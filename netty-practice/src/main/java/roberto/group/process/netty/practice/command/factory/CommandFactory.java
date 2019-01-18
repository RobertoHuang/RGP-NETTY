/**
 * FileName: CommandFactory
 * Author:   HuangTaiHong
 * Date:     2019/1/4 14:02
 * Description: command factory.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.factory;

import roberto.group.process.netty.practice.command.command.RemotingCommand;
import roberto.group.process.netty.practice.command.command.response.ResponseStatusEnum;

import java.net.InetSocketAddress;

/**
 * 〈一句话功能简述〉<br>
 * 〈command factory.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/4
 * @since 1.0.0
 */
public interface CommandFactory {
    /**
     * 功能描述: <br>
     * 〈create request command.〉
     *
     * @param <T>
     * @param requestObject
     * @return > T
     * @author HuangTaiHong
     * @date 2019.01.04 14:03:04
     */
    <T extends RemotingCommand> T createRequestCommand(final Object requestObject);

    /**
     * 功能描述: <br>
     * 〈create response command.〉
     *
     * @param <T>
     * @param responseObject
     * @param requestCommand
     * @return > T
     * @author HuangTaiHong
     * @date 2019.01.04 14:04:13
     */
    <T extends RemotingCommand> T createResponse(final Object responseObject, RemotingCommand requestCommand);

    /**
     * 功能描述: <br>
     * 〈create exception response command.〉
     *
     * @param <T>
     * @param id
     * @param errorMsg
     * @return > T
     * @author HuangTaiHong
     * @date 2019.01.04 14:04:42
     */
    <T extends RemotingCommand> T createExceptionResponse(int id, String errorMsg);

    /**
     * 功能描述: <br>
     * 〈create exception response command.〉
     *
     * @param <T>
     * @param id
     * @param status
     * @return > T
     * @author HuangTaiHong
     * @date 2019.01.07 14:27:55
     */
    <T extends RemotingCommand> T createExceptionResponse(int id, ResponseStatusEnum status);

    /**
     * 功能描述: <br>
     * 〈create exception response command.〉
     *
     * @param <T>    the type parameter
     * @param id
     * @param status
     * @param t
     * @return > T
     * @author HuangTaiHong
     * @date 2019.01.07 15:40:15
     */
    <T extends RemotingCommand> T createExceptionResponse(int id, ResponseStatusEnum status, final Throwable t);

    /**
     * 功能描述: <br>
     * 〈create exception response command.〉
     *
     * @param <T>
     * @param id
     * @param t
     * @param errorMsg
     * @return > T
     * @author HuangTaiHong
     * @date 2019.01.04 14:05:34
     */
    <T extends RemotingCommand> T createExceptionResponse(int id, final Throwable t, String errorMsg);

    /**
     * 功能描述: <br>
     * 〈create timeout response command.〉
     *
     * @param <T>
     * @param address
     * @return > T
     * @author HuangTaiHong
     * @date 2019.01.04 14:06:31
     */
    <T extends RemotingCommand> T createTimeoutResponse(final InetSocketAddress address);

    /**
     * 功能描述: <br>
     * 〈create send failed response command.〉
     *
     * @param <T>
     * @param address
     * @param throwable
     * @return > T
     * @author HuangTaiHong
     * @date 2019.01.04 14:06:54
     */
    <T extends RemotingCommand> T createSendFailedResponse(final InetSocketAddress address, Throwable throwable);

    /**
     * 功能描述: <br>
     * 〈create connection closed response command.〉
     *
     * @param <T>
     * @param address
     * @param message
     * @return > T
     * @author HuangTaiHong
     * @date 2019.01.04 14:07:42
     */
    <T extends RemotingCommand> T createConnectionClosedResponse(final InetSocketAddress address, String message);
}