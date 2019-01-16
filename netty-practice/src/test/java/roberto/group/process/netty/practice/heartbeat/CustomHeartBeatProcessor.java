/**
 * FileName: CustomHeartBeatProcessor
 * Author:   HuangTaiHong
 * Date:     2019/1/15 17:29
 * Description: custom heartbeat processor.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.heartbeat;

import lombok.extern.slf4j.Slf4j;
import roberto.group.process.netty.practice.command.command.RemotingCommand;
import roberto.group.process.netty.practice.command.processor.processor.RPCRemotingProcessor;
import roberto.group.process.netty.practice.remote.remote.RemotingContext;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 〈一句话功能简述〉<br> 
 * 〈custom heartbeat processor.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/15
 * @since 1.0.0
 */
@Slf4j
public class CustomHeartBeatProcessor extends RPCRemotingProcessor<RemotingCommand> {
    private AtomicInteger heartBeatTimes = new AtomicInteger();

    public int getHeartBeatTimes() {
        return heartBeatTimes.get();
    }

    public void reset() {
        this.heartBeatTimes.set(0);
    }

    @Override
    public void doProcess(RemotingContext ctx, RemotingCommand msg) throws Exception {
        heartBeatTimes.incrementAndGet();
        log.warn("heart beat received: {}", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
    }
}