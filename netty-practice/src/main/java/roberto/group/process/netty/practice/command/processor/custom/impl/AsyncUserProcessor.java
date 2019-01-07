/**
 * FileName: AsyncUserProcessor
 * Author:   HuangTaiHong
 * Date:     2019/1/7 19:35
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.processor.custom.impl;

import roberto.group.process.netty.practice.command.processor.context.AsyncContext;
import roberto.group.process.netty.practice.remote.biz.BizContext;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
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