/**
 * FileName: InvokeCallback
 * Author:   HuangTaiHong
 * Date:     2019/1/2 11:01
 * Description: Invoke callback.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.remote.invoke.callback;

import java.util.concurrent.Executor;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Invoke callback.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/2
 * @since 1.0.0
 */
public interface InvokeCallback {
    /**
     * 功能描述: <br>
     * 〈User defined executor.〉
     *
     * @return > java.util.concurrent.Executor
     * @author HuangTaiHong
     * @date 2019.01.05 18:28:22
     */
    Executor getExecutor();

    /**
     * 功能描述: <br>
     * 〈Response received.〉
     *
     * @param result
     * @author HuangTaiHong
     * @date 2019.01.05 19:00:55
     */
    void onResponse(final Object result);

    /**
     * 功能描述: <br>
     * 〈Exception caught.〉
     *
     * @param e
     * @author HuangTaiHong
     * @date 2019.01.05 18:37:53
     */
    void onException(final Throwable e);
}