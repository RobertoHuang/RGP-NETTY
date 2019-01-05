/**
 * FileName: IDGenerator
 * Author:   HuangTaiHong
 * Date:     2019/1/5 10:05
 * Description: IDGenerator is used for generating request id in integer form.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 〈一句话功能简述〉<br> 
 * 〈IDGenerator is used for generating request id in integer form.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/5
 * @since 1.0.0
 */
public class IDGenerator {
    private static final AtomicInteger id = new AtomicInteger(0);

    public static int nextId() {
        return id.incrementAndGet();
    }

    public static void resetId() {
        id.set(0);
    }
}