/**
 * FileName: AbstractUserProcessor
 * Author:   HuangTaiHong
 * Date:     2019/1/7 18:19
 * Description: Implements common function and provide default value.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.processor.custom;

import roberto.group.process.netty.practice.remote.biz.BizContext;
import roberto.group.process.netty.practice.remote.biz.impl.DefaultBizContext;
import roberto.group.process.netty.practice.remote.remote.RemotingContext;

import java.util.concurrent.Executor;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Implements common function and provide default value.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/7
 * @since 1.0.0
 */
public abstract class AbstractUserProcessor<T> implements UserProcessor<T> {
    /** executor selector, default null */
    protected ExecutorSelector executorSelector;

    @Override
    public boolean timeoutDiscard() {
        return true;
    }

    @Override
    public boolean processInIOThread() {
        return false;
    }

    @Override
    public Executor getExecutor() {
        return null;
    }

    @Override
    public ExecutorSelector getExecutorSelector() {
        return this.executorSelector;
    }

    @Override
    public void setExecutorSelector(ExecutorSelector executorSelector) {
        this.executorSelector = executorSelector;
    }

    @Override
    public BizContext preHandleRequest(RemotingContext remotingContext, T request) {
        return new DefaultBizContext(remotingContext);
    }
}