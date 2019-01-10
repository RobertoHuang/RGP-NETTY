/**
 * FileName: UserProcessorRegisterHelper
 * Author:   HuangTaiHong
 * Date:     2019/1/10 10:29
 * Description: user processor register utils.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.processor.custom;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import roberto.group.process.netty.practice.command.processor.custom.impl.MultiInterestUserProcessor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 〈一句话功能简述〉<br> 
 * 〈user processor register utils.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/10
 * @since 1.0.0
 */
public class UserProcessorRegisterHelper {
    /**
     * 功能描述: <br>
     * 〈Help register user processor.〉
     *
     * @param processor
     * @param userProcessors
     * @author HuangTaiHong
     * @date 2019.01.10 10:32:03
     */
    public static void registerUserProcessor(UserProcessor<?> processor, ConcurrentHashMap<String, UserProcessor<?>> userProcessors) {
        if (null == processor) {
            throw new RuntimeException("User processor should not be null!");
        } else {
            if (!(processor instanceof MultiInterestUserProcessor)) {
                registerSingleInterestProcessor(processor, userProcessors);
            } else {
                registerMultiInterestProcessor((MultiInterestUserProcessor) processor, userProcessors);
            }
        }
    }

    /**
     * 功能描述: <br>
     * 〈Help register single-interest user processor.〉
     *
     * @param processor
     * @param userProcessors
     * @author HuangTaiHong
     * @date 2019.01.10 10:32:39
     */
    private static void registerSingleInterestProcessor(UserProcessor<?> processor, ConcurrentHashMap<String, UserProcessor<?>> userProcessors) {
        if (StringUtils.isBlank(processor.interest())) {
            throw new RuntimeException("Processor interest should not be blank!");
        } else {
            UserProcessor<?> preProcessor = userProcessors.putIfAbsent(processor.interest(), processor);
            if (preProcessor != null) {
                throw new RuntimeException("Processor with interest key [" + processor.interest() + "] has already been registered to rpc server, can not register again!");
            }
        }
    }

    /**
     * 功能描述: <br>
     * 〈Help register multi-interest user processor.〉
     *
     * @param processor
     * @param userProcessors
     * @author HuangTaiHong
     * @date 2019.01.10 10:32:39
     */
    private static void registerMultiInterestProcessor(MultiInterestUserProcessor<?> processor, ConcurrentHashMap<String, UserProcessor<?>> userProcessors) {
        if (CollectionUtils.isEmpty(processor.multiInterest())) {
            throw new RuntimeException("Processor interest should not be blank!");
        } else {
            processor.multiInterest().forEach(interest -> {
                UserProcessor<?> preProcessor = userProcessors.putIfAbsent(interest, processor);
                if (preProcessor != null) {
                    throw new RuntimeException("Processor with interest key [" + interest + "] has already been registered to rpc server, can not register again!");
                }
            });
        }
    }
}