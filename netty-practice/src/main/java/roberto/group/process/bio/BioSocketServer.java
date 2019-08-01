package roberto.group.process.bio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class BioSocketServer {
    // 默认的端口号
    private static int DEFAULT_PORT = 8083;

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            log.info("监听来自于" + DEFAULT_PORT + "的端口信息");
            serverSocket = new ServerSocket(DEFAULT_PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                SocketServerThread socketServerThread = new SocketServerThread(socket);
                new Thread(socketServerThread).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {

                }
            }
        }

        // 这个wait不涉及到具体的实验逻辑
        // 只是为了保证守护线程在启动所有线程后，进入等待状态
        synchronized (BioSocketServer.class) {
            try {
                BioSocketServer.class.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

class SocketServerThread implements Runnable {
    private Socket socket;

    public SocketServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        InputStream in = null;
        OutputStream out = null;
        try {
            // 下面我们收取信息
            in = socket.getInputStream();
            out = socket.getOutputStream();
            Integer sourcePort = socket.getPort();
            int maxLen = 1024;
            byte[] contextBytes = new byte[maxLen];
            // 使用线程，同样无法解决read方法的阻塞问题
            // 也就是说read方法处同样会被阻塞，直到操作系统有数据准备好
            int realLen = in.read(contextBytes, 0, maxLen);
            // 读取信息
            String message = new String(contextBytes, 0, realLen);
            // 下面打印信息
            System.out.println("服务器收到来自于端口：" + sourcePort + "的信息：" + message);
            // 下面开始发送信息
            out.write("回发响应信息！".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //试图关闭
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (this.socket != null) {
                    this.socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}