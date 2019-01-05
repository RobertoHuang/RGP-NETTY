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
    /** 连接状态 **/
    public static final String CONN_SERVICE_STATUS = "rgp.conn.service.status";
    public static final String CONN_SERVICE_STATUS_OFF = "off";
    public static final String CONN_SERVICE_STATUS_ON = "on";

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

    /** 序列化类型配置 **/
    public static final String SERIALIZER = "rgp.serializer";
    public static final String SERIALIZER_DEFAULT = String.valueOf("");

    /** Netty Buffer低水位 **/
    public static final String NETTY_BUFFER_LOW_WATERMARK = "rgp.netty.buffer.low.watermark";
    public static final String NETTY_BUFFER_LOW_WATERMARK_DEFAULT = Integer.toString(32 * 1024);

    /** Netty Buffer高水位 **/
    public static final String NETTY_BUFFER_HIGH_WATERMARK = "rgp.netty.buffer.high.watermark";
    public static final String NETTY_BUFFER_HIGH_WATERMARK_DEFAULT = Integer.toString(64 * 1024);

    /** 默认字符编码 */
    public static final String DEFAULT_CHARSET = "UTF-8";

    /** 默认连接超时时间 单位(ms) */
    public static final int DEFAULT_CONNECT_TIMEOUT = 1000;

    /** 每个URL默认创建的连接数 */
    public static final int DEFAULT_CONN_NUM_PER_URL = 1;

    /** 每个URL默认创建的最大连接数 */
    public static final int MAX_CONN_NUM_PER_URL = 100 * 10000;

    /** 是否需要预热链接【默认值false】 **/
    public static final boolean DEFAULT_CONNECTION_WARMUP = false;
}