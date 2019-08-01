package roberto.group.process.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.concurrent.CountDownLatch;

public class BioSocketClient {
    public static void main(String[] args) throws Exception {
        Integer clientNumber = 20;
        CountDownLatch countDownLatch = new CountDownLatch(clientNumber);

        // 分别开始启动这20个客户端,并发访问
        for (int index = 0; index < clientNumber; index++, countDownLatch.countDown()) {
            ClientRequestThread client = new ClientRequestThread(index, countDownLatch);
            new Thread(client).start();
        }

        // 这个wait不涉及到具体的实验逻辑，只是为了保证守护线程在启动所有线程后，进入等待状态
        synchronized (BioSocketClient.class) {
            BioSocketClient.class.wait();
        }
    }
}

/**
 * 〈一个ClientRequestThread线程模拟一个客户端请求.〉
 *
 * @author HuangTaiHong
 * @create 2019.08.01
 * @since 1.0.0
 */
class ClientRequestThread implements Runnable {
    /** 这个线程的编号 **/
    private Integer clientIndex;

    private CountDownLatch countDownLatch;

    /**
     * countDownLatch是java提供的同步计数器。
     * 当计数器数值减为0时，所有受其影响而等待的线程将会被激活。这样保证模拟并发请求的真实性
     * @param countDownLatch
     */
    public ClientRequestThread(Integer clientIndex, CountDownLatch countDownLatch) {
        this.clientIndex = clientIndex;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        Socket socket = null;
        OutputStream clientRequest = null;
        InputStream clientResponse = null;

        try {
            socket = new Socket("localhost", 8083);
            clientRequest = socket.getOutputStream();
            clientResponse = socket.getInputStream();

            // 等待直到SocketClientDaemon完成所有线程的启动，然后所有线程一起发送请求
            this.countDownLatch.await();

            // 发送请求信息
            clientRequest.write(("这是第" + this.clientIndex + " 个客户端的请求。 over").getBytes());
            clientRequest.flush();

            // 在这里等待，直到服务器返回信息
            System.out.println("第" + this.clientIndex + "个客户端的请求发送完成，等待服务器返回信息");
            int maxLen = 1024;
            byte[] contextBytes = new byte[maxLen];
            int realLen;
            String message = "";
            // 程序执行到这里，会一直等待服务器返回信息(注意，前提是in和out都不能close，如果close了就收不到服务器的反馈了)
            while ((realLen = clientResponse.read(contextBytes, 0, maxLen)) != -1) {
                message += new String(contextBytes, 0, realLen);
            }
            // String messageEncode = new String(message , "UTF-8");
            message = URLDecoder.decode(message, "UTF-8");
            System.out.println("第" + this.clientIndex + "个客户端接收到来自服务器的信息:" + message);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (clientRequest != null) {
                    clientRequest.close();
                }
                if (clientResponse != null) {
                    clientResponse.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}