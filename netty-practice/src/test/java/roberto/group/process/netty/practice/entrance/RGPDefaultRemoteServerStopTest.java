/**
 * FileName: RGPDefaultRemoteServerStopTest
 * Author:   HuangTaiHong
 * Date:     2019/1/17 10:00
 * Description: test RPC server and stop logic.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.entrance;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import roberto.group.process.netty.practice.entrance.server.impl.RGPDefaultRemoteServer;

/**
 * 〈一句话功能简述〉<br> 
 * 〈test RPC server and stop logic.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/17
 * @since 1.0.0
 */
@Slf4j
public class RGPDefaultRemoteServerStopTest {
    @Before
    public void init() {

    }

    @After
    public void stop() {
        
    }

    @Test
    public void doTestStartAndStop() {
        doTestStartAndStop(true);
        doTestStartAndStop(false);
    }

    private void doTestStartAndStop(boolean syncStop) {
        // 1. start a remote server successfully
        RGPDefaultRemoteServer remoteServer = new RGPDefaultRemoteServer(1111, false, syncStop);
        try {
            remoteServer.start();
        } catch (Exception e) {
            log.warn("start fail");
            Assert.fail("Should not reach here");
        }
        log.warn("start success");

        // 2. start a remote server with the same port number failed
        RGPDefaultRemoteServer remoteServer2 = new RGPDefaultRemoteServer(1111, false, syncStop);
        try {
            remoteServer2.start();
            Assert.fail("Should not reach here");
            log.warn("start success");
        } catch (Exception e) {
            log.warn("start fail");
        }

        // 3. stop the first remote server successfully
        try {
            remoteServer.stop();
        } catch (IllegalStateException e) {
            Assert.fail("Should not reach here");
        }

        // 4. stop the second remote server failed, for if start failed, stop method will be called automatically
        try {
            remoteServer2.stop();
            Assert.fail("Should not reach here");
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }
}