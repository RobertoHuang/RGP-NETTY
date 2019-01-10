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
import lombok.Setter;
import roberto.group.process.netty.practice.configuration.support.ConfigsSupport;
import roberto.group.process.netty.practice.remote.help.RemotingAddressParser;

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

    @Getter
    /** ip, can be number format or hostname format */
    private String ip;

    @Getter
    /** port, should be integer between (0, 65535] */
    private int port;

    @Getter
    /** unique key of this url */
    private String uniqueKey;

    @Getter
    @Setter
    /** URL args: protocol */
    private byte protocol;

    @Getter
    @Setter
    /** URL args: version */
    private byte version;

    @Getter
    @Setter
    /** URL agrs: whether need warm up connection */
    private boolean connectionWarmup;

    @Getter
    /** URL agrs: connection number */
    private int connectionNumber;

    @Getter
    /** URL args: timeout value when do connect */
    private int connectTimeout;

    /** URL agrs: all parsed args of each originUrl */
    private Properties properties;

    /** 使用软引用对地址解析结果进行缓存 **/
    public static final ConcurrentHashMap<String, SoftReference<ConnectionURL>> PARSED_URLS = new ConcurrentHashMap();

    protected ConnectionURL(String originUrl) {
        this.originUrl = originUrl;
    }

    public ConnectionURL(String ip, int port) {
        this(ip + RemotingAddressParser.COLON + port);
        this.ip = ip;
        this.port = port;
        this.uniqueKey = this.originUrl;
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

    public ConnectionURL(String originUrl, String ip, int port, String uniqueKey, Properties properties) {
        this(originUrl, ip, port);
        this.uniqueKey = uniqueKey;
        this.properties = properties;
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

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ConnectionURL connectionURL = (ConnectionURL) obj;
        if (this.getOriginUrl().equals(connectionURL.getOriginUrl())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = 1;
        final int prime = 31;
        return prime * result + ((this.getOriginUrl() == null) ? 0 : this.getOriginUrl().hashCode());
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Origin url [" + this.originUrl + "], Unique key [" + this.uniqueKey + "].");
        return stringBuilder.toString();
    }
}