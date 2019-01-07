/**
 * FileName: RPCConfigsSupport
 * Author:   HuangTaiHong
 * Date:     2019/1/4 10:35
 * Description: Constants for rpc.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration.configs;

/**
 * 〈一句话功能简述〉<br>
 * 〈Constants for rpc.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/4
 * @since 1.0.0
 */
public class RPCConfigsSupport {
    /** Protocol key in url. **/
    public static final String URL_PROTOCOL = "_PROTOCOL";

    /** Version key in url. **/
    public static final String URL_VERSION = "_VERSION";

    /** Connection number key of each address. **/
    public static final String CONNECTION_NUM_KEY = "_CONNECTIONNUM";

    /** Connection timeout key in url. **/
    public static final String CONNECT_TIMEOUT_KEY = "_CONNECTTIMEOUT";

    /** whether need to warm up connections. **/
    public static final String CONNECTION_WARMUP_KEY = "_CONNECTIONWARMUP";

    /** whether to dispatch message list in default executor. **/
    public static final String DISPATCH_MSG_LIST_IN_DEFAULT_EXECUTOR = "bolt.rpc.dispatch-msg-list-in-default-executor";
    public static final String DISPATCH_MSG_LIST_IN_DEFAULT_EXECUTOR_DEFAULT = "true";
}