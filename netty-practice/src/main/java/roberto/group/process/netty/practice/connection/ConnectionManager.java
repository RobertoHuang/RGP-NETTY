/**
 * FileName: ConnectionManager
 * Author:   HuangTaiHong
 * Date:     2019/1/2 10:35
 * Description: 连接管理器
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.connection;

import roberto.group.process.netty.practice.exception.RemotingException;
import roberto.group.process.netty.practice.scanner.Scannable;

import java.util.List;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br> 
 * 〈连接管理器〉
 *
 * @author HuangTaiHong
 * @create 2019/1/2
 * @since 1.0.0
 */
public interface ConnectionManager extends Scannable {
    /**
     * 功能描述: <br>
     * 〈初始化〉
     *
     * @author HuangTaiHong
     * @date 2019.01.02 20:04:14
     */
    void init();

    /**
     * 功能描述: <br>
     * 〈添加连接〉
     *
     * @param connection
     * @author HuangTaiHong
     * @date 2019.01.02 20:04:27
     */
    void add(Connection connection);

    /**
     * 功能描述: <br>
     * 〈添加链接-使用指定的poolKey〉
     *
     * @param connection
     * @param poolKey
     * @author HuangTaiHong
     * @date 2019.01.02 20:11:28
     */
    void add(Connection connection, String poolKey);

    /**
     * 功能描述: <br>
     * 〈根据poolKey获取连接〉
     *
     * @param poolKey
     * @return > roberto.group.process.netty.practice.connection.Connection
     * @author HuangTaiHong
     * @date 2019.01.02 20:11:49
     */
    Connection get(String poolKey);

    /**
     * 功能描述: <br>
     * 〈根据poolKey获取所有连接〉
     *
     * @param poolKey
     * @return > java.util.List<roberto.group.process.netty.practice.connection.Connection>
     * @author HuangTaiHong
     * @date 2019.01.02 20:12:35
     */
    List<Connection> getAll(String poolKey);


    /**
     * 功能描述: <br>
     * 〈获取poolKey对应的所有连接MAP组合〉
     *
     * @return > java.util.Map<java.lang.String,java.util.List<roberto.group.process.netty.practice.connection.Connection>>
     * @author HuangTaiHong
     * @date 2019.01.02 20:13:01
     */
    Map<String, List<Connection>> getAll();

    /**
     * 功能描述: <br>
     * 〈删除连接〉
     *
     * @param connection
     * @author HuangTaiHong
     * @date 2019.01.02 20:13:29
     */
    void remove(Connection connection);

    /**
     * 功能描述: <br>
     * 〈根据poolKey删除连接〉
     *
     * @param connection
     * @param poolKey
     * @author HuangTaiHong
     * @date 2019.01.02 20:13:48
     */
    void remove(Connection connection, String poolKey);

    /**
     * 功能描述: <br>
     * 〈根据poolKey删除所有连接〉
     *
     * @param poolKey
     * @author HuangTaiHong
     * @date 2019.01.02 20:14:12
     */
    void remove(String poolKey);

    /**
     * 功能描述: <br>
     * 〈删除并关闭所有连接〉
     *
     * @author HuangTaiHong
     * @date 2019.01.03 09:42:54
     */
    void removeAll();

    /**
     * 功能描述: <br>
     * 〈检查连接是否可用，如果不可用则抛出RemotingException〉
     *
     * @param connection
     * @throws RemotingException
     * @author HuangTaiHong
     * @date 2019.01.03 09:55:34
     */
    void check(Connection connection) throws RemotingException;

    /**
     * 功能描述: <br>
     * 〈TODO〉
     *
     * @param poolKey
     * @return > int
     * @author HuangTaiHong
     * @date 2019.01.03 09:56:04
     */
    int count(String poolKey);

    /**
     * 功能描述: <br>
     * 〈创建连接池并初始化连接〉
     *
     *  检查连接数 如果还不够则执行修复逻辑【取决于Url.getConnNum()】
     *
     * @param connectionURL
     * @throws InterruptedException
     * @throws RemotingException
     * @author HuangTaiHong
     * @date 2019.01.03 14:08:28
     */
    void createConnectionAndHealIfNeed(ConnectionURL connectionURL) throws InterruptedException, RemotingException;
}