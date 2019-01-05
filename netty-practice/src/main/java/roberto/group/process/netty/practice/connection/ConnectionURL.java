/**
 * FileName: ConnectionURL
 * Author:   HuangTaiHong
 * Date:     2019/1/3 13:52
 * Description: 连接定义
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.connection;

import lombok.Getter;
import roberto.group.process.netty.practice.configuration.configs.ConfigsSupport;
import roberto.group.process.netty.practice.remote.parse.RemotingAddressParser;

import java.lang.ref.SoftReference;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 〈一句话功能简述〉<br>
 * 〈连接定义〉
 *
 * @author HuangTaiHong
 * @create 2019/1/3
 * @since 1.0.0
 */
public class ConnectionURL {
    @Getter
    /** origin url */
    private String originUrl;

    /** ip, can be number format or hostname format */
    private String ip;

    /** port, should be integer between (0, 65535] */
    private int port;

    /** unique key of this url */
    private String uniqueKey;

    /** URL args: protocol */
    private byte protocol;

    /** URL args: version */
    private byte version;

    /** URL agrs: whether need warm up connection */
    private boolean connectionWarmup;

    /** URL agrs: connection number */
    private int connectionNumber;

    /** URL args: timeout value when do connect */
    private int connectTimeout;

    /** URL agrs: all parsed args of each originUrl */
    private Properties properties;

    /** 使用软引用对地址解析结果进行缓存 **/
    public static final ConcurrentHashMap<String, SoftReference<ConnectionURL>> PARSED_URLS = new ConcurrentHashMap();

    protected ConnectionURL(String originUrl) {
        this.originUrl = originUrl;
    }

    public ConnectionURL(String originUrl, String ip, int port) {
        this(originUrl);
        this.ip = ip;
        this.port = port;
        this.uniqueKey = ip + RemotingAddressParser.COLON + port;
    }

    public ConnectionURL(String originUrl, String ip, int port, Properties properties) {
        this(originUrl, ip, port);
        this.properties = properties;
    }

    public void setProtocol(byte protocol) {
        this.protocol = protocol;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public void setConnWarmup(boolean connectionWarmup) {
        this.connectionWarmup = connectionWarmup;
    }

    public void setConnectionNumber(int connectionNumber) {
        if (connectionNumber <= 0 || connectionNumber > ConfigsSupport.MAX_CONN_NUM_PER_URL) {
            throw new IllegalArgumentException("Illegal value of connection number [" + connectionNumber + "], must be an integer between [" + ConfigsSupport.DEFAULT_CONN_NUM_PER_URL + ", " + ConfigsSupport.MAX_CONN_NUM_PER_URL + "].");
        }
        this.connectionNumber = connectionNumber;
    }

    public void setConnectTimeout(int connectTimeout) {
        if (connectTimeout <= 0) {
            throw new IllegalArgumentException("Illegal value of connectTimeout [" + connectTimeout + "], must be a positive integer].");
        }
        this.connectTimeout = connectTimeout;
    }

    public String getProperty(String key) {
        if (properties == null) {
            return null;
        }
        return properties.getProperty(key);
    }
}