/**
 * FileName: AsyncUserProcessor
 * Author:   HuangTaiHong
 * Date:     2019/1/7 19:35
 * Description: Extends this to process user defined request in ASYNC way.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.processor.custom;

import roberto.group.process.netty.practice.context.AsyncContext;
import roberto.group.process.netty.practice.remote.biz.BizContext;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Extends this to process user defined request in ASYNC way.〉
 *
 *  If you want process reqeuest in SYNC way, please extends SyncUserProcessor.
 *
 * @author HuangTaiHong
 * @create 2019/1/7
 * @since 1.0.0
 */
public abstract class AsyncUserProcessor<T> extends AbstractUserProcessor<T> {
    @Override
    public abstract String interest();

    @Override
    public Object handleRequest(BizContext bizContext, T request) throws Exception {
        throw new UnsupportedOperationException("SYNC handle request is unsupported in AsyncUserProcessor!");
    }

    @Override
    public abstract void handleRequest(BizContext bizContext, AsyncContext asyncContext, T request);
}