/**
 * FileName: AsynMultiInterestUserProcessor
 * Author:   HuangTaiHong
 * Date:     2019/1/9 16:38
 * Description: Extends this to process user defined request in ASYNC way.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.processor.custom;

import roberto.group.process.netty.practice.context.AsyncContext;
import roberto.group.process.netty.practice.context.BizContext;

import java.util.List;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Extends this to process user defined request in ASYNC way.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/9
 * @since 1.0.0
 */
public abstract class AsynMultiInterestUserProcessor<T> extends MultiInterestUserProcessor<T> {
    @Override
    public Object handleRequest(BizContext bizCtx, T request) throws Exception {
        throw new UnsupportedOperationException("SYNC handle request is unsupported in AsynMultiInterestUserProcessor!");
    }

    @Override
    public abstract void handleRequest(BizContext bizCtx, AsyncContext asyncContext, T request);

    @Override
    public abstract List<String> multiInterest();
}