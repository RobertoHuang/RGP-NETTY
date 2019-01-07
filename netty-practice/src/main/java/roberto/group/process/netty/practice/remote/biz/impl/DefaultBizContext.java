/**
 * FileName: DefaultBizContext
 * Author:   HuangTaiHong
 * Date:     2019/1/7 18:26
 * Description: default biz context
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.remote.biz.impl;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import roberto.group.process.netty.practice.remote.biz.BizContext;
import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.remote.invoke.context.InvokeContext;
import roberto.group.process.netty.practice.remote.remote.RemotingContext;
import roberto.group.process.netty.practice.utils.RemotingUtil;

/**
 * 〈一句话功能简述〉<br>
 * 〈default biz context〉
 *
 * @author HuangTaiHong
 * @create 2019/1/7
 * @since 1.0.0
 */
@AllArgsConstructor
public class DefaultBizContext implements BizContext {
    @Getter
    private RemotingContext remotingContext;

    @Override
    public String getRemoteHost() {
        if (this.remotingContext != null) {
            Channel channel = this.remotingContext.getChannelContext().channel();
            if (null != channel) {
                return RemotingUtil.parseRemoteIP(channel);
            }
        }
        return "UNKNOWN_HOST";
    }

    @Override
    public int getRemotePort() {
        if (this.remotingContext != null) {
            Channel channel = this.remotingContext.getChannelContext().channel();
            if (null != channel) {
                return RemotingUtil.parseRemotePort(channel);
            }
        }
        return -1;
    }

    @Override
    public String getRemoteAddress() {
        if (this.remotingContext != null) {
            Channel channel = this.remotingContext.getChannelContext().channel();
            if (null != channel) {
                return RemotingUtil.parseRemoteAddress(channel);
            }
        }
        return "UNKNOWN_ADDRESS";
    }

    @Override
    public Connection getConnection() {
        if (this.remotingContext != null) {
            return this.remotingContext.getConnection();
        }
        return null;
    }

    @Override
    public int getClientTimeout() {
        return 0;
    }

    @Override
    public long getArriveTimestamp() {
        return 0;
    }

    @Override
    public boolean isRequestTimeout() {
        return false;
    }

    @Override
    public String get(String key) {
        return null;
    }

    @Override
    public void put(String key, String value) {

    }

    @Override
    public InvokeContext getInvokeContext() {
        return null;
    }
}