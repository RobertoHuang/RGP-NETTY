/**
 * FileName: RemotingContext
 * Author:   HuangTaiHong
 * Date:     2019/1/6 17:20
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.context;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import roberto.group.process.netty.practice.command.command.RPCCommandType;
import roberto.group.process.netty.practice.command.command.RemotingCommand;
import roberto.group.process.netty.practice.command.processor.custom.UserProcessor;
import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.utils.ConnectionUtil;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 〈一句话功能简述〉<br>
 * 〈Wrap the ChannelHandlerContext.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/6
 * @since 1.0.0
 */
public class RemotingContext {
    @Getter
    private boolean serverSide = false;

    @Setter
    /** RPC command type */
    private int commandType;

    @Setter
    /** request timeout setting by invoke side */
    private int timeout;

    @Setter
    /** request arrive time stamp */
    private long arriveTimestamp;

    @Getter
    private InvokeContext invokeContext;

    @Getter
    @Setter
    /** whether need handle request timeout, if true, request will be discarded. The default value is true **/
    private boolean timeoutDiscard = true;

    @Getter
    private ChannelHandlerContext channelContext;

    private ConcurrentHashMap<String, UserProcessor<?>> customProcessors;

    public RemotingContext(ChannelHandlerContext channelContext) {
        this.channelContext = channelContext;
    }

    public RemotingContext(ChannelHandlerContext channelContext, boolean serverSide) {
        this.serverSide = serverSide;
        this.channelContext = channelContext;
    }

    public RemotingContext(ChannelHandlerContext channelContext, boolean serverSide, ConcurrentHashMap<String, UserProcessor<?>> customProcessors) {
        this.serverSide = serverSide;
        this.channelContext = channelContext;
        this.customProcessors = customProcessors;
    }

    public RemotingContext(ChannelHandlerContext channelContext, InvokeContext invokeContext, boolean serverSide, ConcurrentHashMap<String, UserProcessor<?>> customProcessors) {
        this.serverSide = serverSide;
        this.invokeContext = invokeContext;
        this.channelContext = channelContext;
        this.customProcessors = customProcessors;
    }

    public ChannelFuture writeAndFlush(RemotingCommand message) {
        return this.channelContext.writeAndFlush(message);
    }

    public UserProcessor<?> getCustomProcessors(String className) {
        return StringUtils.isBlank(className) ? null : this.customProcessors.get(className);
    }

    public boolean isRequestTimeout() {
        return (this.timeout > 0 && (this.commandType != RPCCommandType.REQUEST_ONEWAY) && (System.currentTimeMillis() - this.arriveTimestamp) > this.timeout) ? true : false;
    }

    public Connection getConnection() {
        return ConnectionUtil.getConnectionFromChannel(channelContext.channel());
    }
}