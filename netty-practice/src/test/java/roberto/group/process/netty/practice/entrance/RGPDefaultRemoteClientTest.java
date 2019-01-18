/**
 * FileName: RGPDefaultRemoteClientTest
 * Author:   HuangTaiHong
 * Date:     2019/1/10 13:49
 * Description: a demo for RPC client, you can just run the main method after started RPC server of {@link RGPDefaultRemoteServerTest}
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.entrance;

import lombok.extern.slf4j.Slf4j;
import roberto.group.process.netty.practice.common.CONNECTEventProcessor;
import roberto.group.process.netty.practice.common.DISCONNECTEventProcessor;
import roberto.group.process.netty.practice.common.RequestBody;
import roberto.group.process.netty.practice.connection.enums.ConnectionEventTypeEnum;
import roberto.group.process.netty.practice.entrance.client.RGPDefaultRemoteClient;
import roberto.group.process.netty.practice.exception.RemotingException;

/**
 * 〈一句话功能简述〉<br> 
 * 〈a demo for RPC client, you can just run the main method after started RPC server of {@link RGPDefaultRemoteServerTest}〉
 *
 * @author HuangTaiHong
 * @create 2019/1/10
 * @since 1.0.0
 */
@Slf4j
public class RGPDefaultRemoteClientTest {
    /** remote address **/
    private static final String REMOTE_ADDRESS = "127.0.0.1:8999";

    /** connection event processor **/
    private static final CONNECTEventProcessor CLIENT_CONNECT_PROCESSOR = new CONNECTEventProcessor();
    private static final DISCONNECTEventProcessor CLIENT_DISCONNECT_PROCESSOR = new DISCONNECTEventProcessor();
    
    public static void main(String[] args) throws RemotingException, InterruptedException {
        // 1.create a RPC client
        RGPDefaultRemoteClient client = new RGPDefaultRemoteClient();

        // 2.add processor for connect and close event if you need
        client.addConnectionEventProcessor(ConnectionEventTypeEnum.CONNECT, CLIENT_CONNECT_PROCESSOR);
        client.addConnectionEventProcessor(ConnectionEventTypeEnum.CLOSE, CLIENT_DISCONNECT_PROCESSOR);

        // 3.do init
        client.init();

        // 4.send request
        RequestBody request = new RequestBody(1, "hello world sync");
        String result = (String) client.invokeSync(REMOTE_ADDRESS, request, 3000);
        System.out.println("invoke sync result = [" + result + "]");
        client.shutdown();
    }
}