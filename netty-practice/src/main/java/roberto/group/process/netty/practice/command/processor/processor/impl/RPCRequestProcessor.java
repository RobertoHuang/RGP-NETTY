/**
 * FileName: RPCRequestProcessor
 * Author:   HuangTaiHong
 * Date:     2019/1/7 11:28
 * Description: Process RPC request.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.processor.processor.impl;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import roberto.group.process.netty.practice.command.command.RPCCommandType;
import roberto.group.process.netty.practice.command.command.RemotingCommand;
import roberto.group.process.netty.practice.command.command.request.impl.RPCRequestCommand;
import roberto.group.process.netty.practice.command.command.response.ResponseStatusEnum;
import roberto.group.process.netty.practice.command.factory.CommandFactory;
import roberto.group.process.netty.practice.context.RPCAsyncContext;
import roberto.group.process.netty.practice.command.processor.custom.UserProcessor;
import roberto.group.process.netty.practice.command.processor.custom.AsyncUserProcessor;
import roberto.group.process.netty.practice.command.processor.processor.RPCRemotingProcessor;
import roberto.group.process.netty.practice.exception.DeserializationException;
import roberto.group.process.netty.practice.exception.SerializationException;
import roberto.group.process.netty.practice.context.InvokeContext;
import roberto.group.process.netty.practice.context.RemotingContext;
import roberto.group.process.netty.practice.serialize.serialize.manager.DeserializeLevel;
import roberto.group.process.netty.practice.utils.RemotingAddressUtil;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/**
 * 〈一句话功能简述〉<br>
 * 〈Process RPC request.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/7
 * @since 1.0.0
 */
@Slf4j
@NoArgsConstructor
public class RPCRequestProcessor extends RPCRemotingProcessor<RPCRequestCommand> {
    public RPCRequestProcessor(ExecutorService executor) {
        super(executor);
    }

    public RPCRequestProcessor(CommandFactory commandFactory) {
        super(commandFactory);
    }

    @Override
    public void process(RemotingContext context, RPCRequestCommand command, ExecutorService defaultExecutor) throws Exception {
        // deserialize class
        if (!deserializeRequestCommand(context, command, DeserializeLevel.DESERIALIZE_CLAZZ)) {
            return;
        } else {
            // get userProcessor according to deserialize class
            UserProcessor userProcessor = context.getCustomProcessors(command.getRequestClass());
            if (userProcessor == null) {
                String errorMsg = "No custom processor found for request: " + command.getRequestClass();
                log.error(errorMsg);
                sendResponseIfNecessary(context, command.getType(), this.getCommandFactory().createExceptionResponse(command.getId(), errorMsg));
                return;
            } else {
                // set timeout check state from user's processor
                context.setTimeoutDiscard(userProcessor.timeoutDiscard());
                // to check whether to process in io thread
                if (userProcessor.processInIOThread()) {
                    if (!deserializeRequestCommand(context, command, DeserializeLevel.DESERIALIZE_ALL)) {
                        return;
                    } else {
                        // process in io thread
                        new ProcessTask(context, command).run();
                        return;
                    }
                } else {
                    Executor executor;
                    // to check whether get executor using executor selector
                    if (userProcessor.getExecutorSelector() == null) {
                        executor = userProcessor.getExecutor();
                    } else {
                        // in case haven't deserialized in io thread
                        // it need to deserialize clazz and header before using executor dispath strategy
                        if (!deserializeRequestCommand(context, command, DeserializeLevel.DESERIALIZE_HEADER)) {
                            return;
                        } else {
                            //try get executor with strategy
                            executor = userProcessor.getExecutorSelector().select(command.getRequestClass(), command.getRequestHeader());
                        }
                    }

                    // Till now, if executor still null, then try default
                    if (executor == null) {
                        executor = (this.getExecutor() == null ? defaultExecutor : this.getExecutor());
                    }

                    // use the final executor dispatch process task
                    executor.execute(new ProcessTask(context, command));
                }
            }
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void doProcess(final RemotingContext context, RPCRequestCommand command) throws Exception {
        long currentTimestamp = System.currentTimeMillis();
        preProcessRemotingContext(context, command, currentTimestamp);
        if (context.isTimeoutDiscard() && context.isRequestTimeout()) {
            timeoutLog(context, command, currentTimestamp);
            return;
        } else {
            debugLog(context, command, currentTimestamp);
            // decode request all
            if (!deserializeRequestCommand(context, command, DeserializeLevel.DESERIALIZE_ALL)) {
                return;
            } else {
                dispatchToUserProcessor(context, command);
            }
        }
    }

    /**
     * 功能描述: <br>
     * 〈dispatch request command to user processor.〉
     *
     * @param context
     * @param command
     * @author HuangTaiHong
     * @date 2019.01.07 17:39:17
     */
    private void dispatchToUserProcessor(RemotingContext context, RPCRequestCommand command) {
        final int id = command.getId();
        final byte type = command.getType();
        // processor here must not be null, for it have been checked before
        UserProcessor processor = context.getCustomProcessors(command.getRequestClass());
        if (processor instanceof AsyncUserProcessor) {
            try {
                processor.handleRequest(processor.preHandleRequest(context, command.getRequestObject()), new RPCAsyncContext(context, command, this), command.getRequestObject());
            } catch (RejectedExecutionException e) {
                log.warn("RejectedExecutionException occurred when do ASYNC process in RpcRequestProcessor");
                sendResponseIfNecessary(context, type, this.getCommandFactory().createExceptionResponse(id, ResponseStatusEnum.SERVER_THREAD_POOL_BUSY));
            } catch (Throwable t) {
                String errorMsg = "AYSNC process RPC request failed in RpcRequestProcessor, id=" + id;
                log.error(errorMsg, t);
                sendResponseIfNecessary(context, type, this.getCommandFactory().createExceptionResponse(id, t, errorMsg));
            }
        } else {
            try {
                Object responseObject = processor.handleRequest(processor.preHandleRequest(context, command.getRequestObject()), command.getRequestObject());
                sendResponseIfNecessary(context, type, this.getCommandFactory().createResponse(responseObject, command));
            } catch (RejectedExecutionException e) {
                log.warn("RejectedExecutionException occurred when do SYNC process in RPCRequestProcessor");
                sendResponseIfNecessary(context, type, this.getCommandFactory().createExceptionResponse(id, ResponseStatusEnum.SERVER_THREAD_POOL_BUSY));
            } catch (Throwable t) {
                String errorMessage = "SYNC process RPC request failed in RPCRequestProcessor, id=" + id;
                log.error(errorMessage, t);
                sendResponseIfNecessary(context, type, this.getCommandFactory().createExceptionResponse(id, t, errorMessage));
            }
        }
    }

    /**
     * 功能描述: <br>
     * 〈deserialize request command.〉
     *
     * @param context
     * @param requestCommand
     * @param level
     * @return > boolean
     * @author HuangTaiHong
     * @date 2019.01.07 17:18:23
     */
    private boolean deserializeRequestCommand(RemotingContext context, RPCRequestCommand requestCommand, int level) {
        boolean result;
        try {
            requestCommand.deserialize(level);
            result = true;
        } catch (DeserializationException e) {
            log.error("DeserializationException occurred when process in RPCRequestProcessor, id={}, deserializeLevel={}", requestCommand.getId(), DeserializeLevel.valueOf(level), e);
            sendResponseIfNecessary(context, requestCommand.getType(), this.getCommandFactory().createExceptionResponse(requestCommand.getId(), ResponseStatusEnum.SERVER_DESERIAL_EXCEPTION, e));
            result = false;
        } catch (Throwable t) {
            String errorMsg = "Deserialize RpcRequestCommand failed in RPCRequestProcessor, id=" + requestCommand.getId() + ", deserializeLevel=" + level;
            log.error(errorMsg, t);
            sendResponseIfNecessary(context, requestCommand.getType(), this.getCommandFactory().createExceptionResponse(requestCommand.getId(), t, errorMsg));
            result = false;
        }
        return result;
    }

    /**
     * 功能描述: <br>
     * 〈Send response using remoting context if necessary.〉
     *
     * If request type is oneway, no need to send any response nor exception.
     *
     * @param context
     * @param type
     * @param response
     * @author HuangTaiHong
     * @date 2019.01.07 16:04:13
     */
    public void sendResponseIfNecessary(final RemotingContext context, byte type, final RemotingCommand response) {
        final int id = response.getId();
        if (type == RPCCommandType.REQUEST_ONEWAY) {
            log.debug("Oneway RPC request received, do not send response, id=" + id + ", the address is " + RemotingAddressUtil.parseRemoteAddress(context.getChannelContext().channel()));
        } else {
            RemotingCommand serializedResponse = response;
            try {
                response.serialize();
            } catch (SerializationException e) {
                log.error("SerializationException occurred when sendResponseIfNecessary in RpcRequestProcessor, id={}", id, e);
                serializedResponse = this.getCommandFactory().createExceptionResponse(id, ResponseStatusEnum.SERVER_SERIAL_EXCEPTION, e);
                try {
                    // serialize again for exception response
                    serializedResponse.serialize();
                } catch (SerializationException e1) {
                    // should not happen
                    log.error("serialize SerializationException response failed!");
                }
            } catch (Throwable t) {
                String errorMessage = "Serialize RpcResponseCommand failed when sendResponseIfNecessary in RpcRequestProcessor, id=" + id;
                log.error(errorMessage, t);
                serializedResponse = this.getCommandFactory().createExceptionResponse(id, t, errorMessage);
            }

            context.writeAndFlush(serializedResponse).addListener((ChannelFutureListener) future -> {
                if (log.isDebugEnabled()) {
                    log.debug("Rpc response sent! requestId=" + id + ". The address is " + RemotingAddressUtil.parseRemoteAddress(context.getChannelContext().channel()));
                }
                if (!future.isSuccess()) {
                    log.error("Rpc response send failed! id=" + id + ". The address is " + RemotingAddressUtil.parseRemoteAddress(context.getChannelContext().channel()), future.cause());
                }
            });
        }
    }


    /**
     * 功能描述: <br>
     * 〈pre process remoting context, initial some useful infos and pass to biz.〉
     *
     * @param context
     * @param command
     * @param currentTimestamp
     * @author HuangTaiHong
     * @date 2019.01.07 17:35:06
     */
    private void preProcessRemotingContext(RemotingContext context, RPCRequestCommand command, long currentTimestamp) {
        context.setArriveTimestamp(command.getArriveTime());
        context.setTimeout(command.getTimeout());
        context.setCommandType(command.getType());
        context.getInvokeContext().putIfAbsent(InvokeContext.BOLT_PROCESS_WAIT_TIME, currentTimestamp - command.getArriveTime());
    }


    /**
     * 功能描述: <br>
     * 〈print some debug log when receive request.〉
     *
     * @param context
     * @param command
     * @param currentTimestamp
     * @author HuangTaiHong
     * @date 2019.01.07 17:37:29
     */
    private void debugLog(RemotingContext context, RPCRequestCommand command, long currentTimestamp) {
        if (log.isDebugEnabled()) {
            log.debug("RPC request received! requestId={}, from {}", command.getId(), RemotingAddressUtil.parseRemoteAddress(context.getChannelContext().channel()));
            log.debug("request id {} currenTimestamp {} - arriveTime {} = server cost {} < timeout {}.", command.getId(), currentTimestamp, command.getArriveTime(), (currentTimestamp - command.getArriveTime()), command.getTimeout());
        }
    }

    /**
     * 功能描述: <br>
     * 〈print some log when request timeout and discarded in io thread.〉
     *
     * @param context
     * @param command
     * @param currentTimestamp
     * @author HuangTaiHong
     * @date 2019.01.07 17:35:10
     */
    private void timeoutLog(RemotingContext context, final RPCRequestCommand command, long currentTimestamp) {
        if (log.isDebugEnabled()) {
            log.debug("request id [{}] currenTimestamp [{}] - arriveTime [{}] = server cost [{}] >= timeout value [{}].", command.getId(), currentTimestamp, command.getArriveTime(), (currentTimestamp - command.getArriveTime()), command.getTimeout());
        }

        String remoteAddr = "UNKNOWN";
        if (null != context) {
            Channel channel = context.getChannelContext().channel();
            if (null != channel) {
                remoteAddr = RemotingAddressUtil.parseRemoteAddress(channel);
            }
        }
        log.warn("RPC request id[{}], from remoteAddr[{}] stop process, total wait time in queue is [{}], client timeout setting is [{}].", command.getId(), remoteAddr, (currentTimestamp - command.getArriveTime()), command.getTimeout());
    }
}