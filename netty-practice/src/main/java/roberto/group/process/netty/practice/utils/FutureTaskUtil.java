/**
 * FileName: FutureTaskUtil
 * Author:   HuangTaiHong
 * Date:     2019/1/8 10:58
 * Description: Utils for future task.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.utils;

import org.slf4j.Logger;
import roberto.group.process.netty.practice.exception.FutureTaskNotCompleted;
import roberto.group.process.netty.practice.exception.FutureTaskNotRunYetException;

import java.util.concurrent.ExecutionException;

/**
 * 〈一句话功能简述〉<br>
 * 〈Utils for future task.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/8
 * @since 1.0.0
 */
public class FutureTaskUtil {
    /**
     * 功能描述: <br>
     * 〈get the result of a future task.〉
     *
     *  Notice: the run method of this task should have been called at first.
     *
     * @param <T>    the type parameter
     * @param task
     * @param logger
     * @return > T
     * @author HuangTaiHong
     * @date 2019.01.08 13:46:58
     */
    public static <T> T getFutureTaskResult(RunStateRecordedFutureTask<T> task, Logger logger) {
        T t = null;
        if (task != null) {
            try {
                t = task.getAfterRun();
            } catch (InterruptedException e) {
                logger.error("Future task interrupted!", e);
            } catch (ExecutionException e) {
                logger.error("Future task execute failed!", e);
            } catch (FutureTaskNotRunYetException e) {
                logger.error("Future task has not run yet!", e);
            } catch (FutureTaskNotCompleted e) {
                logger.error("Future task has not completed!", e);
            }
        }
        return t;
    }

    /**
     * 功能描述: <br>
     * 〈launder the throwable.〉
     *
     * @param t
     * @author HuangTaiHong
     * @date 2019.01.08 11:00:26
     */
    public static void launderThrowable(Throwable t) {
        if (t instanceof Error) {
            throw (Error) t;
        } else if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        } else {
            throw new IllegalStateException("Not unchecked!", t);
        }
    }
}