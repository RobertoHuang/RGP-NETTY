/**
 * FileName: ProtocolManager
 * Author:   HuangTaiHong
 * Date:     2019/1/2 19:30
 * Description: 协议管理器
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.protocol;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 〈一句话功能简述〉<br>
 * 〈协议管理器〉
 *
 * @author HuangTaiHong
 * @create 2019/1/2
 * @since 1.0.0
 */
public class ProtocolManager {
    private static final ConcurrentMap<ProtocolCode, Protocol> protocols = new ConcurrentHashMap<ProtocolCode, Protocol>();

    public static Protocol getProtocol(ProtocolCode protocolCode) {
        return protocols.get(protocolCode);
    }

    /**
     * 功能描述: <br>
     * 〈注册协议〉
     *
     * @param protocol
     * @param protocolCodeBytes
     * @author HuangTaiHong
     * @date 2019.01.02 19:39:56
     */
    public static void registerProtocol(Protocol protocol, byte... protocolCodeBytes) {
        registerProtocol(protocol, ProtocolCode.fromBytes(protocolCodeBytes));
    }

    /**
     * 功能描述: <br>
     * 〈注销协议〉
     *
     * @param protocolCode
     * @return > roberto.group.process.netty.practice.protocol.Protocol
     * @author HuangTaiHong
     * @date 2019.01.02 19:40:12
     */
    public static Protocol unRegisterProtocol(byte... protocolCode) {
        return ProtocolManager.protocols.remove(ProtocolCode.fromBytes(protocolCode));
    }

    public static void registerProtocol(Protocol protocol, ProtocolCode protocolCode) {
        if (null == protocolCode || null == protocol) {
            throw new RuntimeException("Protocol: " + protocol + " and protocol code:" + protocolCode + " should not be null!");
        }
        Protocol exists = ProtocolManager.protocols.putIfAbsent(protocolCode, protocol);
        if (exists != null) {
            throw new RuntimeException("Protocol for code: " + protocolCode + " already exists!");
        }
    }
}