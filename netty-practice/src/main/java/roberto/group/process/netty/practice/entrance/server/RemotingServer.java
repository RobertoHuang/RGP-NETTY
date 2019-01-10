/**
 * FileName: RemotingServer
 * Author:   HuangTaiHong
 * Date:     2018/12/29 14:56
 * Description: RPC Server接口
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
 * 〈RPC Server接口〉
 *
 * @author HuangTaiHong
 * @create 2018/12/29
 * @since 1.0.0
 */
public interface RemotingServer {
    /**
     * 功能描述: <br>
     * 〈启动服务端〉
     *
     * @author HuangTaiHong
     * @date 2018.12.29 14:57:24
     */
    boolean start();

    /**
     * 功能描述: <br>
     * 〈停止服务端〉
     *
     * @return > boolean
     * @author HuangTaiHong
     * @date 2018.12.29 14:57:47
     */
    boolean stop();


    /**
     * 功能描述: <br>
     * 〈获取服务端IP地址〉
     *
     * @return > java.lang.String
     * @author HuangTaiHong
     * @date 2018.12.29 14:59:50
     */
    String getIp();

    /**
     * 功能描述: <br>
     * 〈获取服务端端口号〉
     *
     * @return > int
     * @author HuangTaiHong
     * @date 2018.12.29 15:00:10
     */
    int getPort();

    /**
     * 功能描述: <br>
     * 〈注册默认线程执行器〉
     *
     * @param protocolCode
     * @param executor
     * @author HuangTaiHong
     * @date 2018.12.29 15:34:29
     */
    void registerDefaultExecutor(byte protocolCode, ExecutorService executor);

    /**
     * 功能描述: <br>
     * 〈注册认证处理器〉
     *
     * @param processor
     * @author HuangTaiHong
     * @date 2018.12.29 15:29:25
     */
    void registerUserProcessor(UserProcessor<?> processor);

    /**
     * 功能描述: <br>
     * 〈注册指令处理器〉
     *
     * @param protocolCode
     * @param commandCode
     * @param processor
     * @author HuangTaiHong
     * @date 2018.12.29 15:21:37
     */
    void registerProcessor(byte protocolCode, RemoteCommandCode commandCode, RemotingProcessor<?> processor);
}