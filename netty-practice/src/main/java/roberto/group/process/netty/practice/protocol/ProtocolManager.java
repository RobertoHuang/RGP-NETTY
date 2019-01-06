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
    private static final ConcurrentMap<ProtocolCode, Protocol> PROTOCOLS = new ConcurrentHashMap();

    public static Protocol getProtocol(ProtocolCode protocolCode) {
        return PROTOCOLS.get(protocolCode);
    }

    public static void registerProtocol(Protocol protocol, byte... protocolCodeBytes) {
        registerProtocol(ProtocolCode.fromBytes(protocolCodeBytes), protocol);
    }

    public static Protocol unRegisterProtocol(byte... protocolCode) {
        return ProtocolManager.PROTOCOLS.remove(ProtocolCode.fromBytes(protocolCode));
    }

    private static void registerProtocol(ProtocolCode protocolCode, Protocol protocol) {
        if (null == protocolCode || null == protocol) {
            throw new RuntimeException("Protocol: " + protocol + " and protocol code:" + protocolCode + " should not be null!");
        }
        Protocol exists = ProtocolManager.PROTOCOLS.putIfAbsent(protocolCode, protocol);
        if (exists != null) {
            throw new RuntimeException("Protocol for code: " + protocolCode + " already exists!");
        }
    }
}