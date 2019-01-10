/**
 * FileName: MultiInterestUserProcessor
 * Author:   HuangTaiHong
 * Date:     2019/1/9 16:36
 * Description: Support multi-interests feature based on UserProcessor
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.processor.custom.impl;

import roberto.group.process.netty.practice.command.processor.custom.UserProcessor;

import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈Support multi-interests feature based on UserProcessor〉
 *
 * @author HuangTaiHong
 * @create 2019/1/9
 * @since 1.0.0
 */
public abstract class MultiInterestUserProcessor<T> implements UserProcessor<T> {
    @Override
    public String interest() {
        return null;
    }

    /**
     * 功能描述: <br>
     * 〈A list of the class names of user request.〉
     *
     * Use String type to avoid classloader problem.
     *
     * @return > java.util.List<java.lang.String>
     * @author HuangTaiHong
     * @date 2019.01.09 16:36:40
     */
    public abstract List<String> multiInterest();
}