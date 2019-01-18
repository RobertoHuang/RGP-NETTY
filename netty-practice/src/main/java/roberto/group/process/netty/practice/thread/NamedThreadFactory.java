/**
 * FileName: NamedThreadFactory
 * Author:   HuangTaiHong
 * Date:     2019/1/3 15:17
 * Description: Thread factory to name the thread purposely.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 〈一句话功能简述〉<br>
 * 〈Thread factory to name the thread purposely.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/3
 * @since 1.0.0
 */
public class NamedThreadFactory implements ThreadFactory {
    /** 所属线程组 **/
    private final ThreadGroup group;

    /** 线程名前缀 **/
    private final String namePrefix;

    /** 是否是daemon线程 **/
    private final boolean isDaemon;

    /** 线程池中的线程数 **/
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    /** 线程池个数 - 全局共享 **/
    private static final AtomicInteger poolNumber = new AtomicInteger(1);

    public NamedThreadFactory() {
        this("RGP-ThreadPool");
    }

    public NamedThreadFactory(String name) {
        this(name, false);
    }

    public NamedThreadFactory(String preffix, boolean daemon) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = preffix + "-" + poolNumber.getAndIncrement() + "-thread-";
        isDaemon = daemon;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread t = new Thread(group, runnable, namePrefix + threadNumber.getAndIncrement(), 0);
        t.setDaemon(isDaemon);
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}