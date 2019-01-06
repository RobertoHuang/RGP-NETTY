/**
 * FileName: RPCServerRemoting
 * Author:   HuangTaiHong
 * Date:     2019/1/6 16:02
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.remote.remote.server;

import roberto.group.process.netty.practice.command.command.RemotingCommand;
import roberto.group.process.netty.practice.command.factory.CommandFactory;
import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.connection.ConnectionURL;
import roberto.group.process.netty.practice.connection.DefaultConnectionManager;
import roberto.group.process.netty.practice.exception.RemotingException;
import roberto.group.process.netty.practice.remote.help.RemotingAddressParser;
import roberto.group.process.netty.practice.remote.invoke.callback.InvokeCallback;
import roberto.group.process.netty.practice.remote.invoke.context.InvokeContext;
import roberto.group.process.netty.practice.remote.remote.RPCRemoting;
import roberto.group.process.netty.practice.remote.remote.RPCResponseFuture;
import roberto.group.process.netty.practice.utils.RemotingUtil;

/**
 * 〈一句话功能简述〉<br>
 * 〈RPC server remoting〉
 *
 * @author HuangTaiHong
 * @create 2019/1/6
 * @since 1.0.0
 */
public class RPCServerRemoting extends RPCRemoting {
    public RPCServerRemoting(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public RPCServerRemoting(CommandFactory commandFactory, RemotingAddressParser addressParser, DefaultConnectionManager connectionManager) {
        super(commandFactory, addressParser, connectionManager);
    }

    @Override
    public void oneway(ConnectionURL connectionURL, Object request, InvokeContext invokeContext) throws RemotingException {
        Connection connection = this.connectionManager.get(connectionURL.getUniqueKey());
        if (null == connection) {
            throw new RemotingException("Client address [" + connectionURL.getOriginUrl() + "] not connected yet!");
        } else {
            this.connectionManager.check(connection);
            this.oneway(connection, request, invokeContext);
        }
    }

    @Override
    public Object invokeSync(ConnectionURL connectionURL, Object request, InvokeContext invokeContext, int timeoutMillis) throws RemotingException, InterruptedException {
        Connection connection = this.connectionManager.get(connectionURL.getUniqueKey());
        if (null == connection) {
            throw new RemotingException("Client address [" + connectionURL.getUniqueKey() + "] not connected yet!");
        } else {
            this.connectionManager.check(connection);
            return this.invokeSync(connection, request, invokeContext, timeoutMillis);
        }
    }

    @Override
    public RPCResponseFuture invokeWithFuture(ConnectionURL connectionURL, Object request, InvokeContext invokeContext, int timeoutMillis) throws RemotingException {
        Connection connection = this.connectionManager.get(connectionURL.getUniqueKey());
        if (null == connection) {
            throw new RemotingException("Client address [" + connectionURL.getUniqueKey() + "] not connected yet!");
        }
        this.connectionManager.check(connection);
        return this.invokeWithFuture(connection, request, invokeContext, timeoutMillis);
    }

    @Override
    public void invokeWithCallback(ConnectionURL connectionURL, Object request, InvokeContext invokeContext, InvokeCallback invokeCallback, int timeoutMillis) throws RemotingException {
        Connection connection = this.connectionManager.get(connectionURL.getUniqueKey());
        if (null == connection) {
            throw new RemotingException("Client address [" + connectionURL.getUniqueKey() + "] not connected yet!");
        }
        this.connectionManager.check(connection);
        this.invokeWithCallback(connection, request, invokeContext, invokeCallback, timeoutMillis);
    }

    @Override
    protected void preProcessInvokeContext(Connection connection, RemotingCommand remotingCommand, InvokeContext invokeContext) {
        if (null != invokeContext) {
            invokeContext.putIfAbsent(InvokeContext.BOLT_INVOKE_REQUEST_ID, remotingCommand.getId());
            invokeContext.putIfAbsent(InvokeContext.SERVER_REMOTE_IP, RemotingUtil.parseRemoteIP(connection.getChannel()));
            invokeContext.putIfAbsent(InvokeContext.SERVER_REMOTE_PORT, RemotingUtil.parseRemotePort(connection.getChannel()));
            invokeContext.putIfAbsent(InvokeContext.SERVER_LOCAL_IP, RemotingUtil.parseLocalIP(connection.getChannel()));
            invokeContext.putIfAbsent(InvokeContext.SERVER_LOCAL_PORT, RemotingUtil.parseLocalPort(connection.getChannel()));
        }
    }
}