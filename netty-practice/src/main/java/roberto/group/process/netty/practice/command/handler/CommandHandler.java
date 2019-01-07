/**
 * FileName: CommandHandler
 * Author:   HuangTaiHong
 * Date:     2019/1/5 19:49
 * Description: Command handler.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.handler;

import roberto.group.process.netty.practice.command.code.RemoteCommandCode;
import roberto.group.process.netty.practice.command.processor.processor.RemotingProcessor;
import roberto.group.process.netty.practice.remote.remote.RemotingContext;

import java.util.concurrent.ExecutorService;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Command handler.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/5
 * @since 1.0.0
 */
public interface CommandHandler {
    /**
     * 功能描述: <br>
     * 〈Register default executor for the handler.〉
     *
     * @param executor
     * @author HuangTaiHong
     * @date 2019.01.07 10:38:00
     */
    void registerDefaultExecutor(ExecutorService executor);

    /**
     * 功能描述: <br>
     * 〈Get default executor for the handler.〉
     *
     * @return > java.util.concurrent.ExecutorService
     * @author HuangTaiHong
     * @date 2019.01.05 19:50:00
     */
    ExecutorService getDefaultExecutor();

    /**
     * 功能描述: <br>
     * 〈Register processor for command with specified code.〉
     *
     * @param commandCode
     * @param processor
     * @author HuangTaiHong
     * @date 2019.01.07 10:38:52
     */
    void registerProcessor(RemoteCommandCode commandCode, RemotingProcessor<?> processor);

    /**
     * 功能描述: <br>
     * 〈Handle the command.〉
     *
     * @param remotingContext
     * @param message
     * @throws Exception
     * @author HuangTaiHong
     * @date 2019.01.06 17:26:13
     */
    void handleCommand(RemotingContext remotingContext, Object message) throws Exception;
}