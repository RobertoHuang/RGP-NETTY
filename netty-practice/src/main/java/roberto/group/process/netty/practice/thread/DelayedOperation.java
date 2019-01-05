/**
 * FileName: DelayedOperation
 * Author:   HuangTaiHong
 * Date:     2019/1/4 14:53
 * Description: A singleton holder of the timer for timeout.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.thread;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

import java.util.concurrent.TimeUnit;

/**
 * 〈一句话功能简述〉<br> 
 * 〈A singleton holder of the timer for timeout.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/4
 * @since 1.0.0
 */
public class DelayedOperation {
    private DelayedOperation() {

    }

    public static Timer getTimer() {
        return DefaultInstance.INSTANCE;
    }

    private static class DefaultInstance {
        private final static long defaultTickDuration = 10;

        /** 延迟初始化策略 **/
        private static final Timer INSTANCE = new HashedWheelTimer(new NamedThreadFactory("RGPDefaultTimer", true), defaultTickDuration, TimeUnit.MILLISECONDS);
    }
}