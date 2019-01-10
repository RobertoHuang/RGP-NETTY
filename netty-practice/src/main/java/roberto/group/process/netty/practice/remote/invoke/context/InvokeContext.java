/**
 * FileName: InvokeContext
 * Author:   HuangTaiHong
 * Date:     2019/1/2 11:04
 * Description: Invoke Context
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.remote.invoke.context;

import lombok.NoArgsConstructor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 〈一句话功能简述〉<br>
 * 〈Invoke Context〉
 *
 * 将被保存在InvokeFuture中使用
 *
 * @author HuangTaiHong
 * @create 2019/1/2
 * @since 1.0.0
 */
@NoArgsConstructor
public class InvokeContext {
    public final static int INITIAL_SIZE = 8;

    /** CRC认证开关 **/
    public final static String RGP_CRC_SWITCH = "bolt.invoke.crc.switch";
    /** 用户自定义编解码器 **/
    public final static String RGP_CUSTOM_SERIALIZER = "bolt.invoke.custom.serializer";

    /** invoke context keys of bolt client and server side **/
    public final static String BOLT_INVOKE_REQUEST_ID = "bolt.invoke.request.id";

    /** invoke context keys of client side **/
    public final static String CLIENT_LOCAL_IP = "bolt.client.local.ip";
    public final static String CLIENT_LOCAL_PORT = "bolt.client.local.port";
    public final static String CLIENT_REMOTE_IP = "bolt.client.remote.ip";
    public final static String CLIENT_REMOTE_PORT = "bolt.client.remote.port";
    public final static String CLIENT_CONN_CREATETIME = "bolt.client.conn.createtime";

    /** invoke context keys of server side **/
    public final static String SERVER_LOCAL_IP = "bolt.server.local.ip";
    public final static String SERVER_LOCAL_PORT = "bolt.server.local.port";
    public final static String SERVER_REMOTE_IP = "bolt.server.remote.ip";
    public final static String SERVER_REMOTE_PORT = "bolt.server.remote.port";

    /** invoke context keys of bolt client and server side **/
    public final static String BOLT_PROCESS_WAIT_TIME = "bolt.invoke.wait.time";

    private ConcurrentHashMap<String, Object> context = new ConcurrentHashMap(INITIAL_SIZE);

    public void clear() {
        this.context.clear();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) this.context.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultIfNotFound) {
        return this.context.get(key) != null ? (T) this.context.get(key) : defaultIfNotFound;
    }

    public void put(String key, Object value) {
        this.context.put(key, value);
    }

    public void putIfAbsent(String key, Object value) {
        this.context.putIfAbsent(key, value);
    }
}