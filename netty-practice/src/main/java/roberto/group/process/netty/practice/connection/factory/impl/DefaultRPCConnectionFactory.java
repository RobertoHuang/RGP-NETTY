/**
 * FileName: DefaultRPCConnectionFactory
 * Author:   HuangTaiHong
 * Date:     2019/1/8 16:49
 * Description: Default RPC connection factory impl.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.connection.factory.impl;

import roberto.group.process.netty.practice.command.processor.custom.UserProcessor;
import roberto.group.process.netty.practice.configuration.configs.ConfigurableInstance;
import roberto.group.process.netty.practice.handler.HeartbeatHandler;
import roberto.group.process.netty.practice.handler.RPCBusinessEventHandler;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Default RPC connection factory impl.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/8
 * @since 1.0.0
 */
public class DefaultRPCConnectionFactory extends AbstractConnectionFactory{
    public DefaultRPCConnectionFactory(ConcurrentHashMap<String, UserProcessor<?>> userProcessors, ConfigurableInstance configurableInstance) {
        super(new HeartbeatHandler(), new RPCBusinessEventHandler(userProcessors), configurableInstance);
    }
}