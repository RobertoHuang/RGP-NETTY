/**
 * FileName: ConfigManager
 * Author:   HuangTaiHong
 * Date:     2018/12/29 18:29
 * Description: 配置管理器
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration.manager;

import roberto.group.process.netty.practice.configuration.support.ConfigsSupport;

/**
 * 〈一句话功能简述〉<br>
 * 〈配置管理器〉
 *
 * @author HuangTaiHong
 * @create 2018/12/29
 * @since 1.0.0
 */
public class ConfigManager {
    /************************************************************
     *                 properties for bootstrap                 *
     ***********************************************************/
    public static boolean tcp_nodelay() {
        return getBool(ConfigsSupport.TCP_NODELAY, ConfigsSupport.TCP_NODELAY_DEFAULT);
    }

    public static boolean tcp_so_reuseaddr() {
        return getBool(ConfigsSupport.TCP_SO_REUSEADDR, ConfigsSupport.TCP_SO_REUSEADDR_DEFAULT);
    }

    public static int tcp_so_backlog() {
        return getInt(ConfigsSupport.TCP_SO_BACKLOG, ConfigsSupport.TCP_SO_BACKLOG_DEFAULT);
    }

    public static boolean tcp_so_keepalive() {
        return getBool(ConfigsSupport.TCP_SO_KEEPALIVE, ConfigsSupport.TCP_SO_KEEPALIVE_DEFAULT);
    }

    public static int netty_io_ratio() {
        return getInt(ConfigsSupport.NETTY_IO_RATIO, ConfigsSupport.NETTY_IO_RATIO_DEFAULT);
    }

    public static boolean netty_buffer_pooled() {
        return getBool(ConfigsSupport.NETTY_BUFFER_POOLED, ConfigsSupport.NETTY_BUFFER_POOLED_DEFAULT);
    }

    public static int netty_buffer_low_watermark() {
        return getInt(ConfigsSupport.NETTY_BUFFER_LOW_WATERMARK, ConfigsSupport.NETTY_BUFFER_LOW_WATERMARK_DEFAULT);
    }

    public static int netty_buffer_high_watermark() {
        return getInt(ConfigsSupport.NETTY_BUFFER_HIGH_WATERMARK, ConfigsSupport.NETTY_BUFFER_HIGH_WATERMARK_DEFAULT);
    }

    public static boolean netty_epoll() {
        return getBool(ConfigsSupport.NETTY_EPOLL_SWITCH, ConfigsSupport.NETTY_EPOLL_SWITCH_DEFAULT);
    }

    public static boolean netty_epoll_lt_enabled() {
        return getBool(ConfigsSupport.NETTY_EPOLL_LT, ConfigsSupport.NETTY_EPOLL_LT_DEFAULT);
    }

    /************************************************************
     *                 properties for idle                 *
     ***********************************************************/
    public static boolean tcp_idle_switch() {
        return getBool(ConfigsSupport.TCP_IDLE_SWITCH, ConfigsSupport.TCP_IDLE_SWITCH_DEFAULT);
    }

    public static int tcp_client_idle() {
        return getInt(ConfigsSupport.TCP_CLIENT_IDLE, ConfigsSupport.TCP_CLIENT_IDLE_DEFAULT);
    }

    public static int tcp_server_idle() {
        return getInt(ConfigsSupport.TCP_SERVER_IDLE, ConfigsSupport.TCP_SERVER_IDLE_DEFAULT);
    }

    public static int tcp_idle_maxtimes() {
        return getInt(ConfigsSupport.TCP_IDLE_MAXTIMES, ConfigsSupport.TCP_IDLE_MAXTIMES_DEFAULT);
    }

    /************************************************************
     *            properties for connection manager             *
     ***********************************************************/
    public static int conn_create_tp_min_size() {
        return getInt(ConfigsSupport.CONN_CREATE_TP_MIN_SIZE, ConfigsSupport.CONN_CREATE_TP_MIN_SIZE_DEFAULT);
    }

    public static int conn_create_tp_max_size() {
        return getInt(ConfigsSupport.CONN_CREATE_TP_MAX_SIZE, ConfigsSupport.CONN_CREATE_TP_MAX_SIZE_DEFAULT);
    }

    public static int conn_create_tp_queue_size() {
        return getInt(ConfigsSupport.CONN_CREATE_TP_QUEUE_SIZE, ConfigsSupport.CONN_CREATE_TP_QUEUE_SIZE_DEFAULT);
    }

    public static int conn_create_tp_keepalive() {
        return getInt(ConfigsSupport.CONN_CREATE_TP_KEEPALIVE_TIME, ConfigsSupport.CONN_CREATE_TP_KEEPALIVE_TIME_DEFAULT);
    }

    /************************************************************
     *            properties for processor manager              *
     ***********************************************************/
    public static int default_tp_min_size() {
        return getInt(ConfigsSupport.TP_MIN_SIZE, ConfigsSupport.TP_MIN_SIZE_DEFAULT);
    }

    public static int default_tp_max_size() {
        return getInt(ConfigsSupport.TP_MAX_SIZE, ConfigsSupport.TP_MAX_SIZE_DEFAULT);
    }

    public static int default_tp_queue_size() {
        return getInt(ConfigsSupport.TP_QUEUE_SIZE, ConfigsSupport.TP_QUEUE_SIZE_DEFAULT);
    }

    public static int default_tp_keepalive_time() {
        return getInt(ConfigsSupport.TP_KEEPALIVE_TIME, ConfigsSupport.TP_KEEPALIVE_TIME_DEFAULT);
    }

    /************************************************************
     *            properties for reconnect manager              *
     ***********************************************************/
    public static boolean conn_reconnect_switch() {
        return getBool(ConfigsSupport.CONN_RECONNECT_SWITCH, ConfigsSupport.CONN_RECONNECT_SWITCH_DEFAULT);
    }

    /************************************************************
     *            properties for connection monitor             *
     ***********************************************************/
    public static boolean conn_monitor_switch() {
        return getBool(ConfigsSupport.CONN_MONITOR_SWITCH, ConfigsSupport.CONN_MONITOR_SWITCH_DEFAULT);
    }

    public static long conn_monitor_initial_delay() {
        return getLong(ConfigsSupport.CONN_MONITOR_INITIAL_DELAY, ConfigsSupport.CONN_MONITOR_INITIAL_DELAY_DEFAULT);
    }

    public static long conn_monitor_period() {
        return getLong(ConfigsSupport.CONN_MONITOR_PERIOD, ConfigsSupport.CONN_MONITOR_PERIOD_DEFAULT);
    }

    public static int conn_threshold() {
        return getInt(ConfigsSupport.CONN_THRESHOLD, ConfigsSupport.CONN_THRESHOLD_DEFAULT);
    }

    public static int retry_detect_period() {
        return getInt(ConfigsSupport.RETRY_DETECT_PERIOD, ConfigsSupport.RETRY_DETECT_PERIOD_DEFAULT);
    }

    /************************************************************
     *                properties for serializer                 *
     ***********************************************************/
    public static final byte serializer = serializer();

    public static byte serializer() {
        return getByte(ConfigsSupport.SERIALIZER, ConfigsSupport.SERIALIZER_DEFAULT);
    }

    /** 获取系统属性相关 **/
    public static byte getByte(String key, String defaultValue) {
        return Byte.parseByte(System.getProperty(key, defaultValue));
    }

    public static boolean getBool(String key, String defaultValue) {
        return Boolean.parseBoolean(System.getProperty(key, defaultValue));
    }

    public static int getInt(String key, String defaultValue) {
        return Integer.parseInt(System.getProperty(key, defaultValue));
    }

    public static long getLong(String key, String defaultValue) {
        return Long.parseLong(System.getProperty(key, defaultValue));
    }
}