/**
 * FileName: RemotingServer
 * Author:   HuangTaiHong
 * Date:     2018/12/29 14:56
 * Description: RPC Server.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.entrance.server;

import roberto.group.process.netty.practice.command.code.RemoteCommandCode;
import roberto.group.process.netty.practice.command.processor.custom.UserProcessor;
import roberto.group.process.netty.practice.command.processor.processor.RemotingProcessor;

import java.util.concurrent.ExecutorService;

/**
 * 〈一句话功能简述〉<br> 
 * 〈RPC Server.〉
 *
 * @author HuangTaiHong
 * @create 2018/12/29
 * @since 1.0.0
 */
public interface RemotingServer {
    /**
     * 功能描述: <br>
     * 〈Start the server.〉
     *
     * @author HuangTaiHong
     * @date 2018.12.29 14:57:24
     */
    boolean start();

    /**
     * 功能描述: <br>
     * 〈Stop the server.〉
     *
     * @return > boolean
     * @author HuangTaiHong
     * @date 2018.12.29 14:57:47
     */
    boolean stop();


    /**
     * 功能描述: <br>
     * 〈Get the ip of the server.〉
     *
     * @return > java.lang.String
     * @author HuangTaiHong
     * @date 2018.12.29 14:59:50
     */
    String getIp();

    /**
     * 功能描述: <br>
     * 〈Get the port of the server.〉
     *
     * @return > int
     * @author HuangTaiHong
     * @date 2018.12.29 15:00:10
     */
    int getPort();

    /**
     * 功能描述: <br>
     * 〈Register default executor service for server.〉
     *
     * @param protocolCode
     * @param executor
     * @author HuangTaiHong
     * @date 2018.12.29 15:34:29
     */
    void registerDefaultExecutor(byte protocolCode, ExecutorService executor);

    /**
     * 功能描述: <br>
     * 〈Register user processor.〉
     *
     * @param processor
     * @author HuangTaiHong
     * @date 2018.12.29 15:29:25
     */
    void registerUserProcessor(UserProcessor<?> processor);

    /**
     * 功能描述: <br>
     * 〈Register processor for command with the command code.〉
     *
     * @param protocolCode
     * @param commandCode
     * @param processor
     * @author HuangTaiHong
     * @date 2018.12.29 15:21:37
     */
    void registerProcessor(byte protocolCode, RemoteCommandCode commandCode, RemotingProcessor<?> processor);
}