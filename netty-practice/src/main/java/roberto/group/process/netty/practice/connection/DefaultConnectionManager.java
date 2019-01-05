/**
 * FileName: DefaultConnectionManager
 * Author:   HuangTaiHong
 * Date:     2019/1/4 15:56
 * Description: default connection manager
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.connection;

import roberto.group.process.netty.practice.connection.strategy.ConnectionSelectStrategy;
import roberto.group.process.netty.practice.exception.RemotingException;

import java.util.List;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br>
 * 〈default connection manager〉
 *
 * @author HuangTaiHong
 * @create 2019/1/4
 * @since 1.0.0
 */
public class DefaultConnectionManager implements ConnectionManager {
    protected ConnectionSelectStrategy connectionSelectStrategy;

    public DefaultConnectionManager() {
//        this.connTasks = new ConcurrentHashMap<String, RunStateRecordedFutureTask<ConnectionPool>>();
//        this.healTasks = new ConcurrentHashMap<String, FutureTask<Integer>>();
//        this.connectionSelectStrategy = new RandomSelectStrategy(globalSwitch);
    }

    public DefaultConnectionManager(ConnectionSelectStrategy connectionSelectStrategy) {
        this();
        this.connectionSelectStrategy = connectionSelectStrategy;
    }

    @Override
    public void init() {

    }

    @Override
    public void add(Connection connection) {

    }

    @Override
    public void add(Connection connection, String poolKey) {

    }

    @Override
    public Connection get(String poolKey) {
        return null;
    }

    @Override
    public List<Connection> getAll(String poolKey) {
        return null;
    }

    @Override
    public Map<String, List<Connection>> getAll() {
        return null;
    }

    @Override
    public void remove(Connection connection) {

    }

    @Override
    public void remove(Connection connection, String poolKey) {

    }

    @Override
    public void remove(String poolKey) {

    }

    @Override
    public void removeAll() {

    }

    @Override
    public void check(Connection connection) throws RemotingException {

    }

    @Override
    public int count(String poolKey) {
        return 0;
    }

    @Override
    public void createConnectionAndHealIfNeed(ConnectionURL connectionURL) throws InterruptedException, RemotingException {

    }

    @Override
    public void scan() {

    }
}