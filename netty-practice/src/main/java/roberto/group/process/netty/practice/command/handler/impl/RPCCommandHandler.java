/**
 * FileName: RPCCommandHandler
 * Author:   HuangTaiHong
 * Date:     2019/1/7 10:57
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.handler.impl;

import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import roberto.group.process.netty.practice.command.code.RemoteCommandCode;
import roberto.group.process.netty.practice.command.command.RPCCommandType;
import roberto.group.process.netty.practice.command.command.RPCRemotingCommand;
import roberto.group.process.netty.practice.command.command.RemotingCommand;
import roberto.group.process.netty.practice.command.command.request.RequestCommand;
import roberto.group.process.netty.practice.command.command.response.ResponseCommand;
import roberto.group.process.netty.practice.command.command.response.ResponseStatusEnum;
import roberto.group.process.netty.practice.command.factory.CommandFactory;
import roberto.group.process.netty.practice.command.handler.CommandHandler;
import roberto.group.process.netty.practice.command.processor.manager.ProcessorManager;
import roberto.group.process.netty.practice.command.processor.processor.RPCRemotingProcessor;
import roberto.group.process.netty.practice.command.processor.processor.RemotingProcessor;
import roberto.group.process.netty.practice.command.processor.processor.impl.RPCHeartBeatProcessor;
import roberto.group.process.netty.practice.command.processor.processor.impl.RPCRequestProcessor;
import roberto.group.process.netty.practice.command.processor.processor.impl.RPCResponseProcessor;
import roberto.group.process.netty.practice.configuration.manager.RPCConfigManager;
import roberto.group.process.netty.practice.remote.remote.RemotingContext;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/**
 * 〈一句话功能简述〉<br>
 * 〈RPC command handler.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/7
 * @since 1.0.0
 */
@Slf4j
public class RPCCommandHandler implements CommandHandler {
    private CommandFactory commandFactory;

    private ProcessorManager processorManager;

    public RPCCommandHandler(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
        this.processorManager = new ProcessorManager();
        // process request
        this.processorManager.registerProcessor(RemoteCommandCode.RPC_REQUEST, new RPCRequestProcessor(this.commandFactory));
        // process response
        this.processorManager.registerProcessor(RemoteCommandCode.RPC_RESPONSE, new RPCResponseProcessor());
        // process heartbeat
        this.processorManager.registerProcessor(RemoteCommandCode.HEARTBEAT, new RPCHeartBeatProcessor());
        // register default processor
        this.processorManager.registerDefaultProcessor(new RPCRemotingProcessor<RemotingCommand>() {
            @Override
            public void doProcess(RemotingContext context, RemotingCommand command) {
                log.error("No processor available for command code {}, msgId {}", command.getRemoteCommandCode(), command.getId());
            }
        });
    }

    @Override
    public void registerDefaultExecutor(ExecutorService executor) {
        this.processorManager.setDefaultExecutor(executor);
    }

    @Override
    public ExecutorService getDefaultExecutor() {
        return this.processorManager.getDefaultExecutor();
    }

    @Override
    public void registerProcessor(RemoteCommandCode commandCode, RemotingProcessor<?> processor) {
        this.processorManager.registerProcessor(commandCode, processor);
    }

    @Override
    public void handleCommand(RemotingContext context, Object message) throws Exception {
        try {
            if (message instanceof List) {
                final Runnable handleTask = () -> {
                    if (log.isDebugEnabled()) {
                        log.debug("Batch message! size={}", ((List<?>) message).size());
                    }

                    for (final Object m : (List<?>) message) {
                        RPCCommandHandler.this.process(context, m);
                    }
                };

                if (!RPCConfigManager.dispatch_msg_list_in_default_executor()) {
                    handleTask.run();
                } else {
                    // If msg is list ,then the batch submission to biz threadpool can save io thread.
                    processorManager.getDefaultExecutor().execute(handleTask);
                }
            } else {
                process(context, message);
            }
        } catch (final Throwable t) {
            processException(context, message, t);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void process(RemotingContext context, Object msg) {
        try {
            final RPCRemotingCommand command = (RPCRemotingCommand) msg;
            final RemotingProcessor processor = processorManager.getProcessor(command.getRemoteCommandCode());
            processor.process(context, command, processorManager.getDefaultExecutor());
        } catch (final Throwable t) {
            processException(context, msg, t);
        }
    }

    private void processException(RemotingContext context, Object message, Throwable t) {
        if (!(message instanceof List)) {
            processExceptionForSingleCommand(context, message, t);
        } else {
            ((List) message).forEach(singleMessage -> processExceptionForSingleCommand(context, singleMessage, t));
        }
    }

    private void processExceptionForSingleCommand(RemotingContext context, Object message, Throwable t) {
        final int id = ((RPCRemotingCommand) message).getId();
        log.warn("Exception caught when processing " + ((message instanceof RequestCommand) ? "request, id={}" : "response, id={}"), id, t);
        // ensure that the request receives a response
        if (message instanceof RequestCommand) {
            final RequestCommand requestCommand = (RequestCommand) message;
            // RejectedExecutionException here assures no response has been sent back
            // Other exceptions should be processed where exception was caught, because here we don't known whether ack had been sent back.
            if ((requestCommand.getType() != RPCCommandType.REQUEST_ONEWAY) && (t instanceof RejectedExecutionException)) {
                final ResponseCommand response = this.commandFactory.createExceptionResponse(id, ResponseStatusEnum.SERVER_THREADPOOL_BUSY);
                context.getChannelContext().writeAndFlush(response).addListener((ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        log.error("Write back exception response failed, requestId={}", id, future.cause());
                    } else {
                        log.info("Write back exception response done, requestId={}, status={}", id, response.getResponseStatus());
                    }
                });
            }
        }
    }
}