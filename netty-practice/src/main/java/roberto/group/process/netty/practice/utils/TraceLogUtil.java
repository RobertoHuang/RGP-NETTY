/**
 * FileName: TraceLogUtil
 * Author:   HuangTaiHong
 * Date:     2019/1/15 19:35
 * Description: Trace log util.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.utils;

import org.slf4j.Logger;
import roberto.group.process.netty.practice.remote.invoke.context.InvokeContext;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Trace log util.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/15
 * @since 1.0.0
 */
public class TraceLogUtil {
    public static void printConnectionTraceLog(Logger logger, String traceId, InvokeContext invokeContext) {
        String sourceIp = invokeContext.get(InvokeContext.CLIENT_LOCAL_IP);
        Integer sourcePort = invokeContext.get(InvokeContext.CLIENT_LOCAL_PORT);

        String targetIp = invokeContext.get(InvokeContext.CLIENT_REMOTE_IP);
        Integer targetPort = invokeContext.get(InvokeContext.CLIENT_REMOTE_PORT);

        StringBuilder logMessage = new StringBuilder();
        logger.info(logMessage.append(traceId).append(",").append(sourceIp).append(":").append(sourcePort).append(",").append(targetIp).append(":").append(targetPort).toString());
    }
}