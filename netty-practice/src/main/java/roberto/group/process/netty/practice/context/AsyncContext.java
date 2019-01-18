/**
 * FileName: AsyncContext
 * Author:   HuangTaiHong
 * Date:     2019/1/7 19:12
 * Description: Async context for biz.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.context;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Async context for biz.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/7
 * @since 1.0.0
 */
public interface AsyncContext {
    /**
     * 功能描述: <br>
     * 〈send response back.〉
     *
     * @param responseObject
     * @author HuangTaiHong
     * @date 2019.01.07 19:12:42
     */
    void sendResponse(Object responseObject);
}