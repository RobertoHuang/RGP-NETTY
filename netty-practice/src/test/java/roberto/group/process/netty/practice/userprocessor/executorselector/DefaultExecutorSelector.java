/**
 * FileName: DefaultExecutorSelector
 * Author:   HuangTaiHong
 * Date:     2019/1/16 15:43
 * Description: Default Executor Selector.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.userprocessor.executorselector;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import roberto.group.process.netty.practice.command.processor.custom.UserProcessor;
import roberto.group.process.netty.practice.thread.NamedThreadFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Default Executor Selector.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/16
 * @since 1.0.0
 */
public class DefaultExecutorSelector implements UserProcessor.ExecutorSelector {
    public static final String EXECUTOR0 = "executor0";
    public static final String EXECUTOR1 = "executor1";

    private String chooseExecutorStr;
    /** executor */
    private ThreadPoolExecutor executor0;
    private ThreadPoolExecutor executor1;

    public DefaultExecutorSelector(String chooseExecutorStr) {
        this.chooseExecutorStr = chooseExecutorStr;
        this.executor0 = new ThreadPoolExecutor(1, 3, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(4), new NamedThreadFactory("rpc-specific0-executor"));
        this.executor1 = new ThreadPoolExecutor(1, 3, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(4), new NamedThreadFactory("rpc-specific1-executor"));
    }

    @Override
    public Executor select(String requestClass, Object requestHeader) {
        Assert.assertNotNull(requestClass);
        Assert.assertNotNull(requestHeader);
        return (StringUtils.equals(chooseExecutorStr, (String) requestHeader)) ? executor1 : executor0;
    }
}