/**
 * FileName: BizContext
 * Author:   HuangTaiHong
 * Date:     2019/1/7 18:22
 * Description: basic info for biz.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.context;

import roberto.group.process.netty.practice.connection.Connection;

/**
 * 〈一句话功能简述〉<br> 
 * 〈basic info for biz.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/7
 * @since 1.0.0
 */
public interface BizContext {
    /**
     * 功能描述: <br>
     * 〈get remote host ip.〉
     *
     * @return > java.lang.String
     * @author HuangTaiHong
     * @date 2019.01.07 18:23:33
     */
    String getRemoteHost();


    /**
     * 功能描述: <br>
     * 〈get remote port.〉
     *
     * @return > int
     * @author HuangTaiHong
     * @date 2019.01.07 18:23:43
     */
    int getRemotePort();

    /**
     * 功能描述: <br>
     * 〈get remote address.〉
     *
     * @return > java.lang.String
     * @author HuangTaiHong
     * @date 2019.01.07 18:23:16
     */
    String getRemoteAddress();

    /**
     * 功能描述: <br>
     * 〈get the connection of this request.〉
     *
     * @return > roberto.group.process.netty.practice.connection.Connection
     * @author HuangTaiHong
     * @date 2019.01.07 18:24:00
     */
    Connection getConnection();

    /**
     * 功能描述: <br>
     * 〈get the timeout value from RPC client.〉
     *
     * @return > int
     * @author HuangTaiHong
     * @date 2019.01.07 18:24:33
     */
    int getClientTimeout();

    /**
     * 功能描述: <br>
     * 〈get the arrive time stamp.〉
     *
     * @return > long
     * @author HuangTaiHong
     * @date 2019.01.07 18:24:49
     */
    long getArriveTimestamp();

    /**
     * 功能描述: <br>
     * 〈check whether request already timeout.〉
     *
     * @return > boolean
     * @author HuangTaiHong
     * @date 2019.01.07 18:24:15
     */
    boolean isRequestTimeout();

    /**
     * 功能描述: <br>
     * 〈get value.〉
     *
     * @param key
     * @return > java.lang.String
     * @author HuangTaiHong
     * @date 2019.01.07 18:25:11
     */
    String get(String key);

    /**
     * 功能描述: <br>
     * 〈put a key and value.〉
     *
     * @param key
     * @param value
     * @author HuangTaiHong
     * @date 2019.01.07 18:25:22
     */
    void put(String key, String value);

    /**
     * 功能描述: <br>
     * 〈get invoke context.〉
     *
     * @return > roberto.group.process.netty.practice.context.InvokeContext
     * @author HuangTaiHong
     * @date 2019.01.07 18:25:30
     */
    InvokeContext getInvokeContext();
}