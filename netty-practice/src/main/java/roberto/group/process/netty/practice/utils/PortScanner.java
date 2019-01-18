/**
 * FileName: PortScanner
 * Author:   HuangTaiHong
 * Date:     2019/1/11 9:49
 * Description: port scanner.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 〈一句话功能简述〉<br> 
 * 〈port scanner.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/11
 * @since 1.0.0
 */
@Slf4j
public class PortScanner {
    /**
     * 功能描述: <br>
     * 〈Looking for a temporary port that is available.〉
     *
     * @return  > int
     * @author HuangTaiHong
     * @date 2019.01.11 09:54:33
     */
    public static int select() {
        int port = -1;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(null);
            port = serverSocket.getLocalPort();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
                log.warn("Server socket close status: {}", serverSocket.isClosed());
            } catch (IOException e) {

            }
        }
        return port;
    }

    public static void main(String[] args) throws Exception {
        int port = PortScanner.select();
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(port));
        log.warn("listening on port：{}", port);

        Thread.sleep(1000);
        Socket socket = new Socket("localhost", port);
        log.info("socket status:{}", socket.isConnected());
        log.info("local port: {}, remote port: {}", socket.getLocalPort(), socket.getPort());
    }
}