/**
 * FileName: RPCAddressParser
 * Author:   HuangTaiHong
 * Date:     2019/1/4 9:47
 * Description: RPC地址解析器
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.remote.help.impl;

import org.apache.commons.lang3.StringUtils;
import roberto.group.process.netty.practice.configuration.configs.ConfigsSupport;
import roberto.group.process.netty.practice.configuration.configs.RPCConfigs;
import roberto.group.process.netty.practice.connection.ConnectionURL;
import roberto.group.process.netty.practice.protocol.impl.RPCProtocol;
import roberto.group.process.netty.practice.remote.help.RemotingAddressParser;

import java.lang.ref.SoftReference;
import java.util.Properties;

/**
 * 〈一句话功能简述〉<br>
 * 〈RPC地址解析器〉
 *
 * @author HuangTaiHong
 * @create 2019/1/4
 * @since 1.0.0
 */
public class RPCAddressParser implements RemotingAddressParser {
    @Override
    public ConnectionURL parse(String url) {
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("Illegal format address string [" + url + "], should not be blank! ");
        }
        // 尝试从缓存中获取
        ConnectionURL parsedUrl = this.tryGetFromCache(url);
        if (null != parsedUrl) {
            return parsedUrl;
        }

        // 缓存中不存在则尝试解析
        String ip = null;
        String port = null;
        Properties properties = null;
        int pos = 0;
        int size = url.length();

        // 解析IP地址
        for (int i = 0; i < size; ++i) {
            if (COLON == url.charAt(i)) {
                ip = url.substring(pos, i);
                pos = i;
                // should not end with COLON
                if (i == size - 1) {
                    throw new IllegalArgumentException("Illegal format address string [" + url + "], should not end with COLON[:]! ");
                }
                break;
            }
            // must have one COLON
            if (i == size - 1) {
                throw new IllegalArgumentException("Illegal format address string [" + url + "], must have one COLON[:]! ");
            }
        }

        // 解析端口号
        for (int i = pos; i < size; ++i) {
            if (QUES == url.charAt(i)) {
                port = url.substring(pos + 1, i);
                pos = i;
                if (i == size - 1) {
                    // should not end with QUES
                    throw new IllegalArgumentException("Illegal format address string [" + url + "], should not end with QUES[?]! ");
                }
                break;
            }
            // end without a QUES
            if (i == size - 1) {
                port = url.substring(pos + 1, i + 1);
                pos = size;
            }
        }

        // 解析自定义参数
        if (pos < (size - 1)) {
            properties = new Properties();
            while (pos < (size - 1)) {
                String key = null;
                String value = null;
                for (int i = pos; i < size; ++i) {
                    if (EQUAL == url.charAt(i)) {
                        key = url.substring(pos + 1, i);
                        pos = i;
                        if (i == size - 1) {
                            // should not end with EQUAL
                            throw new IllegalArgumentException("Illegal format address string [" + url + "], should not end with EQUAL[=]! ");
                        }
                        break;
                    }
                    if (i == size - 1) {
                        // must have one EQUAL
                        throw new IllegalArgumentException("Illegal format address string [" + url + "], must have one EQUAL[=]! ");
                    }
                }
                for (int i = pos; i < size; ++i) {
                    if (AND == url.charAt(i)) {
                        value = url.substring(pos + 1, i);
                        pos = i;
                        if (i == size - 1) {
                            // should not end with AND
                            throw new IllegalArgumentException("Illegal format address string [" + url + "], should not end with AND[&]! ");
                        }
                        break;
                    }
                    // end without more AND
                    if (i == size - 1) {
                        value = url.substring(pos + 1, i + 1);
                        pos = size;
                    }
                }
                properties.put(key, value);
            }
        }
        parsedUrl = new ConnectionURL(url, ip, Integer.parseInt(port), properties);
        this.initUrlArgs(parsedUrl);
        ConnectionURL.PARSED_URLS.put(url, new SoftReference(parsedUrl));
        return parsedUrl;
    }

    @Override
    public void initUrlArgs(ConnectionURL connectionURL) {
        // 解析设置连接超时时间
        int connTimeout = ConfigsSupport.DEFAULT_CONNECT_TIMEOUT;
        String connTimeoutStr = connectionURL.getProperty(RPCConfigs.CONNECT_TIMEOUT_KEY);
        if (StringUtils.isNotBlank(connTimeoutStr)) {
            if (StringUtils.isNumeric(connTimeoutStr)) {
                connTimeout = Integer.parseInt(connTimeoutStr);
            } else {
                throw new IllegalArgumentException("Url args illegal value of key [" + RPCConfigs.CONNECT_TIMEOUT_KEY + "] must be positive integer! The origin url is [" + connectionURL.getOriginUrl() + "]");
            }
        }
        connectionURL.setConnectTimeout(connTimeout);

        // 解析设置通讯解析
        byte protocol = RPCProtocol.PROTOCOL_CODE;
        String protocolStr = connectionURL.getProperty(RPCConfigs.URL_PROTOCOL);
        if (StringUtils.isNotBlank(protocolStr)) {
            if (StringUtils.isNumeric(protocolStr)) {
                protocol = Byte.parseByte(protocolStr);
            } else {
                throw new IllegalArgumentException("Url args illegal value of key [" + RPCConfigs.URL_PROTOCOL + "] must be positive integer! The origin url is [" + connectionURL.getOriginUrl() + "]");
            }
        }
        connectionURL.setProtocol(protocol);

        // 解析设置通讯协议版本号
        byte version = RPCProtocol.PROTOCOL_VERSION_1;
        String versionStr = connectionURL.getProperty(RPCConfigs.URL_VERSION);
        if (StringUtils.isNotBlank(versionStr)) {
            if (StringUtils.isNumeric(versionStr)) {
                version = Byte.parseByte(versionStr);
            } else {
                throw new IllegalArgumentException("Url args illegal value of key [" + RPCConfigs.URL_VERSION + "] must be positive integer! The origin url is [" + connectionURL.getOriginUrl() + "]");
            }
        }
        connectionURL.setVersion(version);

        // 解析设置每个URL默认创建的连接数
        int connNum = ConfigsSupport.DEFAULT_CONN_NUM_PER_URL;
        String connNumStr = connectionURL.getProperty(RPCConfigs.CONNECTION_NUM_KEY);
        if (StringUtils.isNotBlank(connNumStr)) {
            if (StringUtils.isNumeric(connNumStr)) {
                connNum = Integer.parseInt(connNumStr);
            } else {
                throw new IllegalArgumentException("Url args illegal value of key [" + RPCConfigs.CONNECTION_NUM_KEY + "] must be positive integer! The origin url is [" + connectionURL.getOriginUrl() + "]");
            }
        }
        connectionURL.setConnectionNumber(connNum);

        // 解析并设置是否需要预热连接
        boolean connWarmup = ConfigsSupport.DEFAULT_CONNECTION_WARMUP;
        String connWarmupStr = connectionURL.getProperty(RPCConfigs.CONNECTION_WARMUP_KEY);
        if (StringUtils.isNotBlank(connWarmupStr)) {
            connWarmup = Boolean.parseBoolean(connWarmupStr);
        }
        connectionURL.setConnWarmup(connWarmup);
    }

    /**
     * 功能描述: <br>
     * 〈尝试从缓存中获取〉
     *
     * @param url
     * @return > roberto.group.process.netty.practice.connection.ConnectionURL
     * @author HuangTaiHong
     * @date 2019.01.04 09:52:07
     */
    private ConnectionURL tryGetFromCache(String url) {
        SoftReference<ConnectionURL> softReference = ConnectionURL.PARSED_URLS.get(url);

        return (softReference == null) ? null : softReference.get();
    }
}