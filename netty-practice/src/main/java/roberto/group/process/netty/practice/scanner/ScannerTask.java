/**
 * FileName: ScannerTask
 * Author:   HuangTaiHong
 * Date:     2019/1/9 16:59
 * Description: Scanner is used to do scan task.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.scanner;

import lombok.extern.slf4j.Slf4j;
import roberto.group.process.netty.practice.thread.NamedThreadFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 〈一句话功能简述〉<br>
 * 〈Scanner is used to do scan task.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/9
 * @since 1.0.0
 */
@Slf4j
public class ScannerTask {
    private List<Scannable> scanList = new LinkedList();

    private ScheduledExecutorService scheduledService = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("RPC-Task-Scanner-Thread", true));

    public void start() {
        scheduledService.scheduleWithFixedDelay(() -> {
            for (Scannable scanned : scanList) {
                try {
                    scanned.scan();
                } catch (Throwable t) {
                    log.error("Exception caught when scannings.", t);
                }
            }
        }, 10000, 10000, TimeUnit.MILLISECONDS);
    }

    public void add(Scannable target) {
        scanList.add(target);
    }

    public void shutdown() {
        scheduledService.shutdown();
    }
}