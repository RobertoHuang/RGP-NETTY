/**
 * FileName: RPCConfigManager
 * Author:   HuangTaiHong
 * Date:     2019/1/7 14:53
 * Description: RPC framework config manager.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration.manager;

import roberto.group.process.netty.practice.configuration.support.RPCConfigsSupport;

/**
 * 〈一句话功能简述〉<br> 
 * 〈RPC framework config manager.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/7
 * @since 1.0.0
 */
public class RPCConfigManager {
    public static boolean dispatch_msg_list_in_default_executor() {
        return ConfigManager.getBool(RPCConfigsSupport.DISPATCH_MSG_LIST_IN_DEFAULT_EXECUTOR, RPCConfigsSupport.DISPATCH_MSG_LIST_IN_DEFAULT_EXECUTOR_DEFAULT);
    }
}