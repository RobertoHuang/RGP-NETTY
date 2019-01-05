/**
 * FileName: InvokeCallbackListener
 * Author:   HuangTaiHong
 * Date:     2019/1/5 18:23
 * Description: Listener to listen response and invoke callback.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.remote.invoke.callback;

import roberto.group.process.netty.practice.remote.invoke.future.InvokeFuture;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Listener to listen response and invoke callback.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/5
 * @since 1.0.0
 */
public interface InvokeCallbackListener {
    /**
     * 功能描述: <br>
     * 〈Get the remote address.〉
     *
     * @return > java.lang.String
     * @author HuangTaiHong
     * @date 2019.01.05 18:23:38
     */
    String getRemoteAddress();


    /**
     * 功能描述: <br>
     * 〈Response arrived.〉
     *
     * @param future
     * @author HuangTaiHong
     * @date 2019.01.05 18:23:44
     */
    void onResponse(final InvokeFuture future);
}