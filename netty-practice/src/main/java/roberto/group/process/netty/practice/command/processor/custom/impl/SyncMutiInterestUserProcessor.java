/**
 * FileName: SyncMutiInterestUserProcessor
 * Author:   HuangTaiHong
 * Date:     2019/1/9 16:43
 * Description: Extends this to process user defined request in SYNC way.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.processor.custom.impl;

import roberto.group.process.netty.practice.command.processor.context.AsyncContext;
import roberto.group.process.netty.practice.remote.biz.BizContext;

import java.util.List;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Extends this to process user defined request in SYNC way.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/9
 * @since 1.0.0
 */
public abstract class SyncMutiInterestUserProcessor <T> extends MultiInterestUserProcessor<T>{
    @Override
    public abstract Object handleRequest(BizContext bizCtx, T request) throws Exception;

    @Override
    public void handleRequest(BizContext bizCtx, AsyncContext asyncContext, T request) {
        throw new UnsupportedOperationException("ASYNC handle request is unsupported in SyncMutiInterestUserProcessor!");
    }

    @Override
    public abstract List<String> multiInterest();
}