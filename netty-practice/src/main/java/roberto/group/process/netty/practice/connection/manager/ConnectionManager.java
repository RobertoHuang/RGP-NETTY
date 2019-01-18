/**
 * FileName: ConnectionManager
 * Author:   HuangTaiHong
 * Date:     2019/1/2 10:35
 * Description: Connection manager of connection pool.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.connection.manager;

import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.connection.ConnectionURL;
import roberto.group.process.netty.practice.exception.RemotingException;
import roberto.group.process.netty.practice.scanner.Scannable;

import java.util.List;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br>
 * 〈Connection manager of connection pool.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/2
 * @since 1.0.0
 */
public interface ConnectionManager extends Scannable {
    /**
     * 功能描述: <br>
     * 〈init.〉
     *
     * @author HuangTaiHong
     * @date 2019.01.02 20:04:14
     */
    void init();

    /**
     * 功能描述: <br>
     * 〈Get the number of Connection in ConnectionPool with the specified pool key.〉
     *
     * @param poolKey
     * @return > int
     * @author HuangTaiHong
     * @date 2019.01.03 09:56:04
     */
    int count(String poolKey);

    /**
     * 功能描述: <br>
     * 〈Add a connection to ConnectionPool.〉
     *
     *  If it contains multiple pool keys, this connection will be added to multiple ConnectionPool too.
     *
     * @param connection
     * @author HuangTaiHong
     * @date 2019.01.02 20:04:27
     */
    void add(Connection connection);

    /**
     * 功能描述: <br>
     * 〈Add a connection to ConnectionPool with the specified poolKey.〉
     *
     * @param poolKey
     * @param connection
     * @author HuangTaiHong
     * @date 2019.01.02 20:11:28
     */
    void add(String poolKey, Connection connection);

    /**
     * 功能描述: <br>
     * 〈Get a connection from ConnectionPool with the specified poolKey.〉
     *
     * @param poolKey
     * @return > roberto.group.process.netty.practice.connection.Connection
     * @author HuangTaiHong
     * @date 2019.01.02 20:11:49
     */
    Connection get(String poolKey);

    /**
     * 功能描述: <br>
     * 〈Get all connections from ConnectionPool with the specified poolKey.〉
     *
     * @param poolKey
     * @return > java.util.List<roberto.group.process.netty.practice.connection.Connection>
     * @author HuangTaiHong
     * @date 2019.01.02 20:12:35
     */
    List<Connection> getAll(String poolKey);

    /**
     * 功能描述: <br>
     * 〈Get all connections of all poolKey.〉
     *
     * @return > java.util.Map<java.lang.String,java.util.List<roberto.group.process.netty.practice.connection.Connection>>
     * @author HuangTaiHong
     * @date 2019.01.02 20:13:01
     */
    Map<String, List<Connection>> getAll();

    /**
     * 功能描述: <br>
     * 〈Remove a Connection from all ConnectionPool with the poolKeys in Connection, and close it.〉
     *
     * @param connection
     * @author HuangTaiHong
     * @date 2019.01.02 20:13:29
     */
    void remove(Connection connection);

    /**
     * 功能描述: <br>
     * 〈Remove and close a Connection from ConnectionPool with the specified poolKey.〉
     *
     * @param poolKey
     * @param connection
     * @author HuangTaiHong
     * @date 2019.01.02 20:13:48
     */
    void remove(String poolKey, Connection connection);

    /**
     * 功能描述: <br>
     * 〈Remove and close all connections from ConnectionPool with the specified poolKey.〉
     *
     * @param poolKey
     * @author HuangTaiHong
     * @date 2019.01.02 20:14:12
     */
    void remove(String poolKey);

    /**
     * 功能描述: <br>
     * 〈Remove and close all connections from all ConnectionPool.〉
     *
     * @author HuangTaiHong
     * @date 2019.01.03 09:42:54
     */
    void removeAll();

    /**
     * 功能描述: <br>
     * 〈check a connection whether available, if not, throw RemotingException.〉
     *
     * @param connection
     * @throws RemotingException
     * @author HuangTaiHong
     * @date 2019.01.03 09:55:34
     */
    void check(Connection connection) throws RemotingException;

    /**
     * 功能描述: <br>
     * 〈Create a connection using specified connectionURL.〉
     *
     * @param connectionURL
     * @return > roberto.group.process.netty.practice.connection.Connection
     * @throws RemotingException
     * @author HuangTaiHong
     * @date 2019.01.08 11:37:42
     */
    Connection create(ConnectionURL connectionURL) throws RemotingException;

    /**
     * 功能描述: <br>
     * 〈Create a connection using specified address.〉
     *
     * @param address
     * @param connectTimeout
     * @return > roberto.group.process.netty.practice.connection.Connection
     * @throws RemotingException
     * @author HuangTaiHong
     * @date 2019.01.08 11:38:20
     */
    Connection create(String address, int connectTimeout) throws RemotingException;

    /**
     * 功能描述: <br>
     * 〈Create a connection using specified ip and port.〉
     *
     * @param ip
     * @param port
     * @param connectTimeout
     * @return > roberto.group.process.netty.practice.connection.Connection
     * @throws RemotingException
     * @author HuangTaiHong
     * @date 2019.01.08 11:38:42
     */
    Connection create(String ip, int port, int connectTimeout) throws RemotingException;

    /**
     * 功能描述: <br>
     * 〈Get a connection using connectionURL, if null then create and add into ConnectionPool.〉
     *
     * @param connectionURL
     * @return > roberto.group.process.netty.practice.connection.Connection
     * @throws InterruptedException
     * @throws RemotingException
     * @author HuangTaiHong
     * @date 2019.01.08 14:53:22
     */
    Connection getAndCreateIfAbsent(ConnectionURL connectionURL) throws InterruptedException, RemotingException;

    /**
     * 功能描述: <br>
     * 〈This method can create connection pool with connections initialized and check the number of connections.〉
     *
     * @param connectionURL
     * @throws InterruptedException
     * @throws RemotingException
     * @author HuangTaiHong
     * @date 2019.01.03 14:08:28
     */
    void createConnectionAndHealIfNeed(ConnectionURL connectionURL) throws InterruptedException, RemotingException;
}