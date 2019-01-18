/**
 * FileName: ProcessorManager
 * Author:   HuangTaiHong
 * Date:     2019/1/7 11:00
 * Description: Manager of processors.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.processor.processor;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import roberto.group.process.netty.practice.command.code.RemoteCommandCode;
import roberto.group.process.netty.practice.configuration.manager.ConfigManager;
import roberto.group.process.netty.practice.thread.NamedThreadFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 〈一句话功能简述〉<br>
 * 〈Manager of processors.〉
 *
 * Maintains the relationship between command and command processor through command code.
 *
 * 主要用于管理RemoteCommandCode, RemotingProcessor的对应关系 并为RemotingProcessor提供默认ExecutorService
 *
 * @author HuangTaiHong
 * @create 2019/1/7
 * @since 1.0.0
 */
@Slf4j
public class ProcessorManager {
    @Getter
    @Setter
    private ExecutorService defaultExecutor;

    private RemotingProcessor<?> defaultProcessor;

    /** properties for defaultExecutor START **/
    private int minPoolSize = ConfigManager.default_tp_min_size();
    private int maxPoolSize = ConfigManager.default_tp_max_size();
    private int queueSize = ConfigManager.default_tp_queue_size();
    private long keepAliveTime = ConfigManager.default_tp_keepalive_time();
    /** properties for defaultExecutor END **/

    private ConcurrentHashMap<RemoteCommandCode, RemotingProcessor<?>> commandToProcessor = new ConcurrentHashMap(4);

    public ProcessorManager() {
        defaultExecutor = new ThreadPoolExecutor(minPoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new ArrayBlockingQueue<>(queueSize), new NamedThreadFactory("bolt-default-executor", true));
    }

    /**
     * 功能描述: <br>
     * 〈Register processor to process command that has the command code.〉
     *
     * @param commandCode
     * @param processor
     * @author HuangTaiHong
     * @date 2019.01.07 11:08:46
     */
    public void registerProcessor(RemoteCommandCode commandCode, RemotingProcessor<?> processor) {
        if (this.commandToProcessor.containsKey(commandCode)) {
            log.warn("Processor for cmd={} is already registered, the processor is {}, and changed to {}", commandCode, commandToProcessor.get(commandCode).getClass().getName(), processor.getClass().getName());
        }
        this.commandToProcessor.put(commandCode, processor);
    }

    /**
     * 功能描述: <br>
     * 〈Register the default processor to process command with no specific processor registered.〉
     *
     * @param processor
     * @author HuangTaiHong
     * @date 2019.01.07 11:12:35
     */
    public void registerDefaultProcessor(RemotingProcessor<?> processor) {
        if (this.defaultProcessor == null) {
            this.defaultProcessor = processor;
        } else {
            throw new IllegalStateException("The defaultProcessor has already been registered: " + this.defaultProcessor.getClass());
        }
    }

    /**
     * 功能描述: <br>
     * 〈Get the specific processor with command code of cmdCode if registered, otherwise the default processor is returned.〉
     *
     * @param commandCode
     * @return > RemotingProcessor<?>
     * @author HuangTaiHong
     * @date 2019.01.07 11:12:59
     */
    public RemotingProcessor<?> getProcessor(RemoteCommandCode commandCode) {
        RemotingProcessor<?> processor;
        return (processor = commandToProcessor.get(commandCode)) != null ? processor : this.defaultProcessor;
    }
}