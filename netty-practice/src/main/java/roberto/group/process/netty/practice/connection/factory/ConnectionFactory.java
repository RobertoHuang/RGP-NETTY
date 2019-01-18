/**
 * FileName: ConnectionFactory
 * Author:   HuangTaiHong
 * Date:     2019/1/8 10:09
 * Description: Factory that creates connections.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.connection.factory;

import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.connection.ConnectionURL;
import roberto.group.process.netty.practice.handler.ConnectionEventHandler;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Factory that creates connections.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/8
 * @since 1.0.0
 */
public interface ConnectionFactory {
    /**
     * 功能描述: <br>
     * 〈Initialize the factory.〉
     *
     * @param connectionEventHandler
     * @author HuangTaiHong
     * @date 2019.01.08 10:09:47
     */
    void init(ConnectionEventHandler connectionEventHandler);

    /**
     * 功能描述: <br>
     * 〈Create a connection use connectionURL.〉
     *
     * @param connectionURL
     * @return > roberto.group.process.netty.practice.connection.Connection
     * @throws Exception
     * @author HuangTaiHong
     * @date 2019.01.08 10:10:25
     */
    Connection createConnection(ConnectionURL connectionURL) throws Exception;

    /**
     * 功能描述: <br>
     * 〈Create a connection according to the IP and port.〉
     *
     * @param targetIP
     * @param targetPort
     * @param connectTimeout
     * @return > roberto.group.process.netty.practice.connection.Connection
     * @throws Exception
     * @author HuangTaiHong
     * @date 2019.01.08 10:10:40
     */
    Connection createConnection(String targetIP, int targetPort, int connectTimeout) throws Exception;

    /**
     * 功能描述: <br>
     * 〈Create a connection according to the IP and port.〉
     *
     * @param targetIP
     * @param targetPort
     * @param version
     * @param connectTimeout
     * @return > roberto.group.process.netty.practice.connection.Connection
     * @throws Exception
     * @author HuangTaiHong
     * @date 2019.01.08 10:10:51
     */
    Connection createConnection(String targetIP, int targetPort, byte version, int connectTimeout) throws Exception;
}