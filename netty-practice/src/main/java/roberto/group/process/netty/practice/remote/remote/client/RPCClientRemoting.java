/**
 * FileName: RPCClientRemoting
 * Author:   HuangTaiHong
 * Date:     2019/1/9 17:30
 * Description: RPC client remoting.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.remote.remote.client;

import roberto.group.process.netty.practice.command.command.RemotingCommand;
import roberto.group.process.netty.practice.command.factory.CommandFactory;
import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.connection.ConnectionURL;
import roberto.group.process.netty.practice.connection.manager.impl.DefaultConnectionManager;
import roberto.group.process.netty.practice.exception.RemotingException;
import roberto.group.process.netty.practice.remote.help.RemotingAddressParser;
import roberto.group.process.netty.practice.remote.invoke.callback.InvokeCallback;
import roberto.group.process.netty.practice.remote.invoke.context.InvokeContext;
import roberto.group.process.netty.practice.remote.remote.RPCRemoting;
import roberto.group.process.netty.practice.remote.remote.RPCResponseFuture;
import roberto.group.process.netty.practice.utils.RemotingUtil;

/**
 * 〈一句话功能简述〉<br>
 * 〈RPC client remoting.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/9
 * @since 1.0.0
 */
public class RPCClientRemoting extends RPCRemoting {
    public RPCClientRemoting(CommandFactory commandFactory, RemotingAddressParser addressParser, DefaultConnectionManager connectionManager) {
        super(commandFactory, addressParser, connectionManager);
    }

    @Override
    public void oneway(ConnectionURL connectionURL, Object request, InvokeContext invokeContext) throws RemotingException, InterruptedException {
        final Connection conn = getConnectionAndInitInvokeContext(connectionURL, invokeContext);
        this.connectionManager.check(conn);
        this.oneway(conn, request, invokeContext);
    }

    @Override
    public Object invokeSync(ConnectionURL connectionURL, Object request, InvokeContext invokeContext, int timeoutMillis) throws RemotingException, InterruptedException {
        final Connection connection = getConnectionAndInitInvokeContext(connectionURL, invokeContext);
        this.connectionManager.check(connection);
        return this.invokeSync(connection, request, invokeContext, timeoutMillis);
    }

    @Override
    public RPCResponseFuture invokeWithFuture(ConnectionURL connectionURL, Object request, InvokeContext invokeContext, int timeoutMillis) throws RemotingException, InterruptedException {
        final Connection conn = getConnectionAndInitInvokeContext(connectionURL, invokeContext);
        this.connectionManager.check(conn);
        return this.invokeWithFuture(conn, request, invokeContext, timeoutMillis);
    }

    @Override
    public void invokeWithCallback(ConnectionURL connectionURL, Object request, InvokeContext invokeContext, InvokeCallback invokeCallback, int timeoutMillis) throws RemotingException, InterruptedException {
        final Connection conn = getConnectionAndInitInvokeContext(connectionURL, invokeContext);
        this.connectionManager.check(conn);
        this.invokeWithCallback(conn, request, invokeContext, invokeCallback, timeoutMillis);
    }

    @Override
    protected void preProcessInvokeContext(Connection connection, RemotingCommand remotingCommand, InvokeContext invokeContext) {
        if (null != invokeContext) {
            invokeContext.putIfAbsent(InvokeContext.BOLT_INVOKE_REQUEST_ID, remotingCommand.getId());
            invokeContext.putIfAbsent(InvokeContext.CLIENT_LOCAL_IP, RemotingUtil.parseLocalIP(connection.getChannel()));
            invokeContext.putIfAbsent(InvokeContext.CLIENT_LOCAL_PORT, RemotingUtil.parseLocalPort(connection.getChannel()));
            invokeContext.putIfAbsent(InvokeContext.CLIENT_REMOTE_IP, RemotingUtil.parseRemoteIP(connection.getChannel()));
            invokeContext.putIfAbsent(InvokeContext.CLIENT_REMOTE_PORT, RemotingUtil.parseRemotePort(connection.getChannel()));
        }
    }

    protected Connection getConnectionAndInitInvokeContext(ConnectionURL connectionURL, InvokeContext invokeContext) throws RemotingException, InterruptedException {
        Connection connection;
        long start = System.currentTimeMillis();
        try {
            connection = this.connectionManager.getAndCreateIfAbsent(connectionURL);
        } finally {
            if (null != invokeContext) {
                invokeContext.putIfAbsent(InvokeContext.CLIENT_CONN_CREATETIME, (System.currentTimeMillis() - start));
            }
        }
        return connection;
    }
}