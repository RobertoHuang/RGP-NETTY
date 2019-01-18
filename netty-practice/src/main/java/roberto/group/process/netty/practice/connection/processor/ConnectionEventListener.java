/**
 * FileName: ConnectionEventListener
 * Author:   HuangTaiHong
 * Date:     2019/1/3 14:40
 * Description: Listen and dispatch connection events.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.connection.processor;

import roberto.group.process.netty.practice.connection.Connection;
import roberto.group.process.netty.practice.connection.enums.ConnectionEventTypeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Listen and dispatch connection events.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/3
 * @since 1.0.0
 */
public class ConnectionEventListener {
    /** KEY:连接事件类型  VALUE:连接事件类型对应的处理器集合 **/
    private ConcurrentHashMap<ConnectionEventTypeEnum, List<ConnectionEventProcessor>> processors = new ConcurrentHashMap(3);

    /**
     * 功能描述: <br>
     * 〈Dispatch events.〉
     *
     *  获取对应事件类型对应的处理器
     *  遍历调用处理器的onEvent方法对连接事件进行处理
     *
     * @param type
     * @param remoteAddr
     * @param conn
     * @author HuangTaiHong
     * @date 2019.01.03 14:47:12
     */
    public void onEvent(ConnectionEventTypeEnum type, String remoteAddr, Connection conn) {
        List<ConnectionEventProcessor> processorList = this.processors.get(type);
        if (processorList != null) {
            for (ConnectionEventProcessor processor : processorList) {
                processor.onEvent(remoteAddr, conn);
            }
        }
    }

    /**
     * 功能描述: <br>
     * 〈Add event processor.〉
     *
     * @param type
     * @param processor
     * @author HuangTaiHong
     * @date 2019.01.03 14:46:35
     */
    public void addConnectionEventProcessor(ConnectionEventTypeEnum type, ConnectionEventProcessor processor) {
        List<ConnectionEventProcessor> processorList = this.processors.get(type);
        if (processorList == null) {
            this.processors.putIfAbsent(type, new ArrayList<>(1));
            processorList = this.processors.get(type);
        }
        processorList.add(processor);
    }
}