/**
 * FileName: RemotingProcessor
 * Author:   HuangTaiHong
 * Date:     2018/12/29 15:19
 * Description: Remoting processor processes remoting commands.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.processor.processor;

import roberto.group.process.netty.practice.command.command.RemotingCommand;
import roberto.group.process.netty.practice.context.RemotingContext;

import java.util.concurrent.ExecutorService;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Remoting processor processes remoting commands.〉
 *
 * @author HuangTaiHong
 * @create 2018/12/29
 * @since 1.0.0
 */
public interface RemotingProcessor<T extends RemotingCommand> {
    /**
     * 功能描述: <br>
     * 〈Process the remoting command.〉
     *
     * @param context
     * @param msg
     * @param defaultExecutor
     * @throws Exception
     * @author HuangTaiHong
     * @date 2019.01.07 14:19:03
     */
    void process(RemotingContext context, T msg, ExecutorService defaultExecutor) throws Exception;
}