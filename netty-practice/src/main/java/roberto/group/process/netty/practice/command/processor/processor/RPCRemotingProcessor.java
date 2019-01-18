/**
 * FileName: RPCRemotingProcessor
 * Author:   HuangTaiHong
 * Date:     2019/1/7 11:25
 * Description: Remoting processor processes remoting commands.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.processor.processor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import roberto.group.process.netty.practice.command.command.RemotingCommand;
import roberto.group.process.netty.practice.command.factory.CommandFactory;
import roberto.group.process.netty.practice.remote.remote.RemotingContext;
import roberto.group.process.netty.practice.utils.RemotingUtil;

import java.util.concurrent.ExecutorService;

/**
 * 〈一句话功能简述〉<br>
 * 〈Remoting processor processes remoting commands.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/7
 * @since 1.0.0
 */
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public abstract class RPCRemotingProcessor<T extends RemotingCommand> implements RemotingProcessor<T> {
    @Getter
    @Setter
    private ExecutorService executor;

    @Getter
    @Setter
    private CommandFactory commandFactory;

    public RPCRemotingProcessor(ExecutorService executor) {
        this.executor = executor;
    }

    public RPCRemotingProcessor(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    @Override
    public void process(RemotingContext context, T msg, ExecutorService defaultExecutor) throws Exception {
        ProcessTask task = new ProcessTask(context, msg);
        if (this.getExecutor() == null) {
            defaultExecutor.execute(task);
        } else {
            this.getExecutor().execute(task);
        }
    }

    /**
     * 功能描述: <br>
     * 〈Do the process.〉
     *
     * @param context
     * @param t
     * @throws Exception
     * @author HuangTaiHong
     * @date 2019.01.07 13:43:24
     */
    public abstract void doProcess(RemotingContext context, T t) throws Exception;

    protected class ProcessTask implements Runnable {
        private T message;
        private RemotingContext context;

        public ProcessTask(RemotingContext context, T message) {
            this.context = context;
            this.message = message;
        }

        /**
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            try {
                RPCRemotingProcessor.this.doProcess(context, message);
            } catch (Throwable e) {
                // protect the thread running this task
                String remotingAddress = RemotingUtil.parseRemoteAddress(context.getChannelContext().channel());
                log.error("Exception caught when process RPC request command in AbstractRemotingProcessor, Id=" + message.getId() + "! Invoke source address is [" + remotingAddress + "].", e);
            }
        }
    }
}