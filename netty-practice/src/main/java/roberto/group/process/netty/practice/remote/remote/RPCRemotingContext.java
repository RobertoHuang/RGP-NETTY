/**
 * FileName: RPCRemotingContext
 * Author:   HuangTaiHong
 * Date:     2019/1/6 17:20
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.remote.remote;

import io.netty.channel.ChannelHandlerContext;
import roberto.group.process.netty.practice.command.processor.CustomProcessor;
import roberto.group.process.netty.practice.remote.invoke.context.InvokeContext;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 〈一句话功能简述〉<br>
 * 〈Wrap the ChannelHandlerContext.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/6
 * @since 1.0.0
 */
public class RPCRemotingContext {
    private boolean serverSide = false;

    private InvokeContext invokeContext;

    private ChannelHandlerContext channelContext;

    private ConcurrentHashMap<String, CustomProcessor<?>> customProcessors;

    public RPCRemotingContext(ChannelHandlerContext channelContext, boolean serverSide, ConcurrentHashMap<String, CustomProcessor<?>> customProcessors) {
        this.serverSide = serverSide;
        this.channelContext = channelContext;
        this.customProcessors = customProcessors;
    }

    public RPCRemotingContext(ChannelHandlerContext channelContext, InvokeContext invokeContext, boolean serverSide, ConcurrentHashMap<String, CustomProcessor<?>> customProcessors) {
        this.serverSide = serverSide;
        this.invokeContext = invokeContext;
        this.channelContext = channelContext;
        this.customProcessors = customProcessors;
    }
}