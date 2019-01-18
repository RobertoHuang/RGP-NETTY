/**
 * FileName: SyncUserProcessor
 * Author:   HuangTaiHong
 * Date:     2019/1/9 16:46
 * Description: Extends this to process user defined request in SYNC way.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.processor.custom;

import roberto.group.process.netty.practice.context.AsyncContext;
import roberto.group.process.netty.practice.remote.biz.BizContext;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Extends this to process user defined request in SYNC way.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/9
 * @since 1.0.0
 */
public abstract class SyncUserProcessor<T> extends AbstractUserProcessor<T> {
    @Override
    public abstract String interest();

    @Override
    public abstract Object handleRequest(BizContext bizCtx, T request) throws Exception;

    @Override
    public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, T request) {
        throw new UnsupportedOperationException("ASYNC handle request is unsupported in SyncUserProcessor!");
    }
}