/**
 * FileName: AuthenticationProcessor
 * Author:   HuangTaiHong
 * Date:     2018/12/29 15:26
 * Description: Defined all functions for biz to process user defined request.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.processor.custom;

import roberto.group.process.netty.practice.context.AsyncContext;
import roberto.group.process.netty.practice.context.BizContext;
import roberto.group.process.netty.practice.context.RemotingContext;

import java.util.concurrent.Executor;

/**
 * 〈一句话功能简述〉<br>
 * 〈Defined all functions for biz to process user defined request.〉
 *
 * @author HuangTaiHong
 * @create 2018/12/29
 * @since 1.0.0
 */
public interface UserProcessor<T> {
    /**
     * 功能描述: <br>
     * 〈The class name of user request.〉
     *
     *  Use String type to avoid classloader problem.
     *
     * @return > java.lang.String
     * @author HuangTaiHong
     * @date 2019.01.07 19:36:56
     */
    String interest();

    /**
     * 功能描述: <br>
     * 〈Whether handle request timeout automatically, we call this fail fast processing when detect timeout.〉
     *
     * @return > boolean
     * @author HuangTaiHong
     * @date 2019.01.07 16:29:18
     */
    boolean timeoutDiscard();

    /**
     * 功能描述: <br>
     * 〈Whether deserialize and process biz logic in io thread.〉
     *
     * Notice: If return true, this will have a strong impact on performance.
     *
     * @return > boolean
     * @author HuangTaiHong
     * @date 2019.01.07 16:33:06
     */
    boolean processInIOThread();

    /**
     * 功能描述: <br>
     * 〈Get user executor.〉
     *
     * @return > java.util.concurrent.Executor
     * @author HuangTaiHong
     * @date 2019.01.07 16:47:19
     */
    Executor getExecutor();

    /**
     * 功能描述: <br>
     * 〈Use this method to get the executor selector.〉
     *
     * @return > roberto.group.process.netty.practice.command.processor.custom.UserProcessor.ExecutorSelector
     * @author HuangTaiHong
     * @date 2019.01.07 16:45:58
     */
    ExecutorSelector getExecutorSelector();

    /**
     * 功能描述: <br>
     * 〈Use this method to set executor selector.〉
     *
     * @param executorSelector
     * @author HuangTaiHong
     * @date 2019.01.07 19:21:25
     */
    void setExecutorSelector(ExecutorSelector executorSelector);

    /**
     * 功能描述: <br>
     * 〈pre handle request to avoid expose remotingContext directly to biz handle request logic.〉
     *
     * @param remotingCtx
     * @param request
     * @return > roberto.group.process.netty.practice.context.BizContext
     * @author HuangTaiHong
     * @date 2019.01.07 18:44:08
     */
    BizContext preHandleRequest(RemotingContext remotingCtx, T request);


    /**
     * 功能描述: <br>
     * 〈Handle request with {@link AsyncContext}.〉
     *
     * @param bizContext
     * @param asyncContext
     * @param request
     * @author HuangTaiHong
     * @date 2019.01.07 19:32:58
     */
    void handleRequest(BizContext bizContext, AsyncContext asyncContext, T request);

    /**
     * 功能描述: <br>
     * 〈Handle request in sync way.〉
     *
     * @param bizContext
     * @param request
     * @return > java.lang.Object
     * @throws Exception
     * @author HuangTaiHong
     * @date 2019.01.07 19:33:26
     */
    Object handleRequest(BizContext bizContext, T request) throws Exception;

    interface ExecutorSelector {
        Executor select(String requestClass, Object requestHeader);
    }
}