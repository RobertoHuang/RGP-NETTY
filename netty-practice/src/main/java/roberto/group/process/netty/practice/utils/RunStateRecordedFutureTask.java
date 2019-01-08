/**
 * FileName: RunStateRecordedFutureTask
 * Author:   HuangTaiHong
 * Date:     2019/1/8 10:01
 * Description: A customized FutureTask which can record whether the run method has been called.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.utils;

import roberto.group.process.netty.practice.exception.FutureTaskNotCompleted;
import roberto.group.process.netty.practice.exception.FutureTaskNotRunYetException;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 〈一句话功能简述〉<br> 
 * 〈A customized FutureTask which can record whether the run method has been called.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/8
 * @since 1.0.0
 */
public class RunStateRecordedFutureTask<V> extends FutureTask<V> {
    private AtomicBoolean hasRun = new AtomicBoolean();

    public RunStateRecordedFutureTask(Callable<V> callable) {
        super(callable);
    }

    @Override
    public void run() {
        this.hasRun.set(true);
        super.run();
    }

    public V getAfterRun() throws InterruptedException, ExecutionException, FutureTaskNotRunYetException, FutureTaskNotCompleted {
        if (!hasRun.get()) {
            throw new FutureTaskNotRunYetException();
        }

        if (!isDone()) {
            throw new FutureTaskNotCompleted();
        }

        return super.get();
    }
}