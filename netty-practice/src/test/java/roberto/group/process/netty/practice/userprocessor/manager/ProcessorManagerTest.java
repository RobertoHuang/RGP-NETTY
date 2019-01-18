/**
 * FileName: ProcessorManagerTest
 * Author:   HuangTaiHong
 * Date:     2019/1/17 10:08
 * Description: test processor manager.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.userprocessor.manager;

import org.junit.Assert;
import org.junit.Test;
import roberto.group.process.netty.practice.command.code.RemoteCommandCode;
import roberto.group.process.netty.practice.command.processor.processor.ProcessorManager;
import roberto.group.process.netty.practice.command.processor.processor.impl.RPCRequestProcessor;

/**
 * 〈一句话功能简述〉<br> 
 * 〈test processor manager.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/17
 * @since 1.0.0
 */
public class ProcessorManagerTest {
    @Test
    public void testRegisterProcessor() {
        // test it should be override if register twice for the same command code
        ProcessorManager processorManager = new ProcessorManager();
        RemoteCommandCode remoteCommandCode = RemoteCommandCode.RPC_REQUEST;
        RemoteCommandCode remoteCommandCode2 = RemoteCommandCode.RPC_REQUEST;

        RPCRequestProcessor rpcRequestProcessor = new RPCRequestProcessor();
        RPCRequestProcessor rpcRequestProcessor2 = new RPCRequestProcessor();

        processorManager.registerProcessor(remoteCommandCode, rpcRequestProcessor);
        processorManager.registerProcessor(remoteCommandCode2, rpcRequestProcessor2);

        Assert.assertEquals(processorManager.getProcessor(remoteCommandCode), rpcRequestProcessor2);
        Assert.assertEquals(processorManager.getProcessor(remoteCommandCode2), rpcRequestProcessor2);
    }
}