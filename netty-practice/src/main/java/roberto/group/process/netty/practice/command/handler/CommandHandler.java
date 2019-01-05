/**
 * FileName: CommandHandler
 * Author:   HuangTaiHong
 * Date:     2019/1/5 19:49
 * Description: Command handler.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.handler;

import java.util.concurrent.ExecutorService;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Command handler.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/5
 * @since 1.0.0
 */
public interface CommandHandler {
    /**
     * 功能描述: <br>
     * 〈Get default executor for the handler.〉
     *
     * @return > java.util.concurrent.ExecutorService
     * @author HuangTaiHong
     * @date 2019.01.05 19:50:00
     */
    ExecutorService getDefaultExecutor();
}