/**
 * FileName: RemotingUtil
 * Author:   HuangTaiHong
 * Date:     2019/1/3 10:12
 * Description: RemotingUtil
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.utils;

import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * 〈一句话功能简述〉<br>
 * 〈RemotingUtil〉
 *
 * @author HuangTaiHong
 * @create 2019/1/3
 * @since 1.0.0
 */
public class RemotingUtil {
    public static String parseRemoteAddress(final Channel channel) {
        if (null == channel) {
            return StringUtils.EMPTY;
        }
        final SocketAddress remote = channel.remoteAddress();
        return doParse(remote != null ? remote.toString().trim() : StringUtils.EMPTY);
    }

    public static String parseLocalAddress(final Channel channel) {
        if (null == channel) {
            return StringUtils.EMPTY;
        }
        final SocketAddress local = channel.localAddress();
        return doParse(local != null ? local.toString().trim() : StringUtils.EMPTY);
    }

    public static String parseRemoteIP(final Channel channel) {
        if (null == channel) {
            return StringUtils.EMPTY;
        }
        final InetSocketAddress remote = (InetSocketAddress) channel.remoteAddress();
        if (remote != null) {
            return remote.getAddress().getHostAddress();
        }
        return StringUtils.EMPTY;
    }

    public static int parseRemotePort(final Channel channel) {
        if (null == channel) {
            return -1;
        }
        final InetSocketAddress remote = (InetSocketAddress) channel.remoteAddress();
        if (remote != null) {
            return remote.getPort();
        }
        return -1;
    }

    public static String parseLocalIP(final Channel channel) {
        if (null == channel) {
            return StringUtils.EMPTY;
        }
        final InetSocketAddress local = (InetSocketAddress) channel.localAddress();
        if (local != null) {
            return local.getAddress().getHostAddress();
        }
        return StringUtils.EMPTY;
    }

    public static int parseLocalPort(final Channel channel) {
        if (null == channel) {
            return -1;
        }
        final InetSocketAddress local = (InetSocketAddress) channel.localAddress();
        if (local != null) {
            return local.getPort();
        }
        return -1;
    }

    public static String parseSocketAddressToString(SocketAddress socketAddress) {
        if (socketAddress != null) {
            return doParse(socketAddress.toString().trim());
        }
        return StringUtils.EMPTY;
    }

    private static String doParse(String address) {
        if (StringUtils.isBlank(address)) {
            return StringUtils.EMPTY;
        }

        if (address.charAt(0) == '/') {
            return address.substring(1);
        } else {
            int len = address.length();
            for (int i = 1; i < len; ++i) {
                if (address.charAt(i) == '/') {
                    return address.substring(i + 1);
                }
            }
            return address;
        }
    }
}