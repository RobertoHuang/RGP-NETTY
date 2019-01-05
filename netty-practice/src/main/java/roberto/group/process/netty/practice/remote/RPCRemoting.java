/**
 * FileName: RPCRemoting
 * Author:   HuangTaiHong
 * Date:     2019/1/4 15:50
 * Description: RPC remoting capability.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.remote;

import lombok.extern.slf4j.Slf4j;
import roberto.group.process.netty.practice.command.command.RPCCommandType;
import roberto.group.process.netty.practice.command.command.RemotingCommand;
import roberto.group.process.netty.practice.command.command.request.RequestCommand;
import roberto.group.process.netty.practice.command.command.request.impl.RPCRequestCommand;
import roberto.group.process.netty.practice.command.command.response.ResponseCommand;
import roberto.group.process.netty.practice.command.factory.CommandFactory;
import roberto.group.process.netty.practice.configuration.switches.impl.ProtocolSwitch;
import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.connection.ConnectionURL;
import roberto.group.process.netty.practice.connection.DefaultConnectionManager;
import roberto.group.process.netty.practice.exception.RemotingException;
import roberto.group.process.netty.practice.exception.SerializationException;
import roberto.group.process.netty.practice.remote.invoke.callback.InvokeCallback;
import roberto.group.process.netty.practice.remote.invoke.context.InvokeContext;
import roberto.group.process.netty.practice.remote.invoke.future.InvokeFuture;
import roberto.group.process.netty.practice.remote.parse.RemotingAddressParser;
import roberto.group.process.netty.practice.utils.RemotingUtil;

/**
 * 〈一句话功能简述〉<br>
 * 〈RPC remoting capability.〉
 *
 * 将客户端的请求地址 -> Connection对象
 * -> 客户端根据请求地址获取Connection对象
 * -> 服务端根据请求地址获取Connection对象
 *
 * 将客户端的请求实体 -> RemotingCommand
 *
 * @author HuangTaiHong
 * @create 2019/1/4
 * @since 1.0.0
 */
@Slf4j
public abstract class RPCRemoting extends Remoting {
    /** address parser to get custom args */
    protected RemotingAddressParser addressParser;

    /** default connection manager */
    protected DefaultConnectionManager connectionManager;

    public RPCRemoting(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public RPCRemoting(CommandFactory commandFactory, RemotingAddressParser addressParser, DefaultConnectionManager connectionManager) {
        this(commandFactory);
        this.addressParser = addressParser;
        this.connectionManager = connectionManager;
    }

    public void oneway(final String address, final Object request, final InvokeContext invokeContext) throws RemotingException, InterruptedException {
        this.oneway(addressParser.parse(address), request, invokeContext);
    }

    public void oneway(final Connection connection, final Object request, final InvokeContext invokeContext) throws RemotingException {
        RequestCommand requestCommand = (RequestCommand) buildRemotingCommand(request, invokeContext, -1);
        requestCommand.setType(RPCCommandType.REQUEST_ONEWAY);
        preProcessInvokeContext(connection, requestCommand, invokeContext);
        super.oneway(connection, requestCommand);
    }

    public Object invokeSync(final String address, final Object request, final InvokeContext invokeContext, final int timeoutMillis) throws RemotingException, InterruptedException {
        return this.invokeSync(addressParser.parse(address), request, invokeContext, timeoutMillis);
    }

    public Object invokeSync(final Connection connection, final Object request, final InvokeContext invokeContext, final int timeoutMillis) throws RemotingException, InterruptedException {
        RemotingCommand requestCommand = buildRemotingCommand(request, invokeContext, timeoutMillis);
        preProcessInvokeContext(connection, requestCommand, invokeContext);
        ResponseCommand responseCommand = (ResponseCommand) super.invokeSync(connection, requestCommand, timeoutMillis);
        return RPCResponseResolver.resolveResponseObject(responseCommand, RemotingUtil.parseRemoteAddress(connection.getChannel()));
    }

    public RPCResponseFuture invokeWithFuture(final String address, final Object request, final InvokeContext invokeContext, int timeoutMillis) throws RemotingException, InterruptedException {
        ConnectionURL connectionURL = this.addressParser.parse(address);
        return this.invokeWithFuture(connectionURL, request, invokeContext, timeoutMillis);
    }

    public RPCResponseFuture invokeWithFuture(final Connection connection, final Object request, final InvokeContext invokeContext, final int timeoutMillis) throws RemotingException {
        RemotingCommand requestCommand = buildRemotingCommand(request, invokeContext, timeoutMillis);
        preProcessInvokeContext(connection, requestCommand, invokeContext);
        InvokeFuture future = super.invokeWithFuture(connection, requestCommand, timeoutMillis);
        return new RPCResponseFuture(RemotingUtil.parseRemoteAddress(connection.getChannel()), future);
    }

    public void invokeWithCallback(String address, Object request, final InvokeContext invokeContext, InvokeCallback invokeCallback, int timeoutMillis) throws RemotingException, InterruptedException {
        ConnectionURL connectionURL = this.addressParser.parse(address);
        this.invokeWithCallback(connectionURL, request, invokeContext, invokeCallback, timeoutMillis);
    }

    public void invokeWithCallback(final Connection connection, final Object request, final InvokeContext invokeContext, final InvokeCallback invokeCallback, final int timeoutMillis) throws RemotingException {
        RemotingCommand requestCommand = buildRemotingCommand(request, invokeContext, timeoutMillis);
        preProcessInvokeContext(connection, requestCommand, invokeContext);
        super.invokeWithCallback(connection, requestCommand, invokeCallback, timeoutMillis);
    }

    protected RemotingCommand buildRemotingCommand(Object request, InvokeContext invokeContext, int timeoutMillis) throws SerializationException {
        RPCRequestCommand command = this.getCommandFactory().createRequestCommand(request);
        if (null != invokeContext) {
            // set client custom serializer for request command if not null
            Object clientCustomSerializer = invokeContext.get(InvokeContext.RGP_CUSTOM_SERIALIZER);
            if (null != clientCustomSerializer) {
                try {
                    command.setSerializer((Byte) clientCustomSerializer);
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException("Illegal custom serializer [" + clientCustomSerializer + "], the type of value should be [byte], but now is [" + clientCustomSerializer.getClass().getName() + "].");
                }
            }

            // enable crc by default, user can disable by set invoke context `false` for key `InvokeContext.BOLT_CRC_SWITCH`
            Boolean crcSwitch = invokeContext.get(InvokeContext.RGP_CRC_SWITCH, ProtocolSwitch.CRC_SWITCH_DEFAULT_VALUE);
            if (null != crcSwitch && crcSwitch) {
                command.setProtocolSwitch(ProtocolSwitch.create(new int[]{ProtocolSwitch.CRC_SWITCH_INDEX}));
            }
        } else {
            // enable crc by default, if there is no invoke context.
            command.setProtocolSwitch(ProtocolSwitch.create(new int[]{ProtocolSwitch.CRC_SWITCH_INDEX}));
        }
        command.setTimeout(timeoutMillis);
        command.setRequestClass(request.getClass().getName());
        command.setInvokeContext(invokeContext);
        command.serialize();
        return command;
    }

    /**
     * 功能描述: <br>
     * 〈Oneway RPC invocation.〉
     *
     * @param connectionURL
     * @param request
     * @param invokeContext
     * @throws RemotingException
     * @throws InterruptedException
     * @author HuangTaiHong
     * @date 2019.01.04 16:04:07
     */
    public abstract void oneway(final ConnectionURL connectionURL, final Object request, final InvokeContext invokeContext) throws RemotingException, InterruptedException;

    /**
     * 功能描述: <br>
     * 〈Synchronous rpc invocation.〉
     *
     * @param connectionURL
     * @param request
     * @param invokeContext
     * @param timeoutMillis
     * @return > java.lang.Object
     * @throws RemotingException
     * @throws InterruptedException
     * @author HuangTaiHong
     * @date 2019.01.05 15:55:07
     */
    public abstract Object invokeSync(final ConnectionURL connectionURL, final Object request, final InvokeContext invokeContext, final int timeoutMillis) throws RemotingException, InterruptedException;

    /**
     * 功能描述: <br>
     * 〈RPC invocation with future returned.〉
     *
     * @param connectionURL
     * @param request
     * @param invokeContext
     * @param timeoutMillis
     * @return > roberto.group.process.netty.practice.remote.RPCResponseFuture
     * @throws RemotingException
     * @throws InterruptedException
     * @author HuangTaiHong
     * @date 2019.01.05 17:02:58
     */
    public abstract RPCResponseFuture invokeWithFuture(final ConnectionURL connectionURL, final Object request, final InvokeContext invokeContext, final int timeoutMillis) throws RemotingException, InterruptedException;

    /**
     * 功能描述: <br>
     * 〈RPC invocation with callback.〉
     *
     * @param connectionURL
     * @param request
     * @param invokeContext
     * @param invokeCallback
     * @param timeoutMillis
     * @throws RemotingException
     * @throws InterruptedException
     * @author HuangTaiHong
     * @date 2019.01.05 17:11:35
     */
    public abstract void invokeWithCallback(final ConnectionURL connectionURL, final Object request, final InvokeContext invokeContext, final InvokeCallback invokeCallback, final int timeoutMillis) throws RemotingException, InterruptedException;

    /**
     * 功能描述: <br>
     * 〈Process InvokeContext and add the necessary information〉
     *
     * @param connection
     * @param remotingCommand
     * @param invokeContext
     * @author HuangTaiHong
     * @date 2019.01.05 15:16:55
     */
    protected abstract void preProcessInvokeContext(Connection connection, RemotingCommand remotingCommand, InvokeContext invokeContext);
}