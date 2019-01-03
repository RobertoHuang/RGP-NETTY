/**
 * FileName: ConfigsSupport
 * Author:   HuangTaiHong
 * Date:     2019/1/2 9:47
 * Description: 可支持配置项
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration.configs;

/**
 * 〈一句话功能简述〉<br>
 * 〈可支持配置项〉
 *
 * @author HuangTaiHong
 * @create 2019/1/2
 * @since 1.0.0
 */
public class ConfigsSupport {
    /** I/O操作在EventLoop中占的时间比(0-100) **/
    public static final String NETTY_IO_RATIO = "rgp.netty.io.ratio";
    /** Netty默认为50(即执行I/O的时间与非I/O的时间相同) **/
    public static final String NETTY_IO_RATIO_DEFAULT = "70";

    /** 是否自动重连 **/
    public static final String CONN_RECONNECT_SWITCH = "rgp.conn.reconnect.switch";
    public static final String CONN_RECONNECT_SWITCH_DEFAULT = "false";

    /** 是否打开连接监视器 **/
    public static final String CONN_MONITOR_SWITCH = "rgp.conn.monitor.switch";
    public static final String CONN_MONITOR_SWITCH_DEFAULT = "false";

    /** 是否采用epoll线程模型 **/
    public static final String NETTY_EPOLL_SWITCH = "rgp.netty.epoll.switch";
    public static final String NETTY_EPOLL_SWITCH_DEFAULT = "true";

    /** Netty Buffer低水位 **/
    public static final String NETTY_BUFFER_LOW_WATERMARK = "rgp.netty.buffer.low.watermark";
    public static final String NETTY_BUFFER_LOW_WATERMARK_DEFAULT = Integer.toString(32 * 1024);

    /** Netty Buffer高水位 **/
    public static final String NETTY_BUFFER_HIGH_WATERMARK = "rgp.netty.buffer.high.watermark";
    public static final String NETTY_BUFFER_HIGH_WATERMARK_DEFAULT = Integer.toString(64 * 1024);
}