/**
 * FileName: RGPDefaultRemoteServerTest
 * Author:   HuangTaiHong
 * Date:     2019/1/10 11:07
 * Description: a demo for RPC server, you can just run the main method to start a server.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.entrance;

import lombok.extern.slf4j.Slf4j;
import roberto.group.process.netty.practice.common.CONNECTEventProcessor;
import roberto.group.process.netty.practice.common.DISCONNECTEventProcessor;
import roberto.group.process.netty.practice.common.SimpleServerUserProcessor;
import roberto.group.process.netty.practice.connection.enums.ConnectionEventTypeEnum;
import roberto.group.process.netty.practice.entrance.server.impl.RGPDefaultRemoteServer;

/**
 * 〈一句话功能简述〉<br> 
 * 〈a demo for RPC server, you can just run the main method to start a server.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/10
 * @since 1.0.0
 */
@Slf4j
public class RGPDefaultRemoteServerTest {
    /** server port **/
    private static final int PORT = 8999;

    /** connection event processor **/
    private static final CONNECTEventProcessor SERVER_CONNECT_PROCESSOR = new CONNECTEventProcessor();
    private static final DISCONNECTEventProcessor SERVER_DISCONNECT_PROCESSOR = new DISCONNECTEventProcessor();

    /** business envent processor **/
    private static final SimpleServerUserProcessor SERVER_USER_PROCESSOR = new SimpleServerUserProcessor();

    public static void main(String[] args) {
        // 1. create a Rpc server with port assigned
        RGPDefaultRemoteServer server = new RGPDefaultRemoteServer(PORT);
        // 2. add processor for connect and close event if you need
        server.addConnectionEventProcessor(ConnectionEventTypeEnum.CONNECT, SERVER_CONNECT_PROCESSOR);
        server.addConnectionEventProcessor(ConnectionEventTypeEnum.CLOSE, SERVER_DISCONNECT_PROCESSOR);
        // 3. register user processor for client request
        server.registerUserProcessor(SERVER_USER_PROCESSOR);
        // 4. server start
        if (server.start()) {
            System.out.println("server start ok!");
        } else {
            System.out.println("server start failed!");
        }
    }
}