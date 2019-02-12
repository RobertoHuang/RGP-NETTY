/**
 * FileName: RemotingAddressParser
 * Author:   HuangTaiHong
 * Date:     2019/1/4 9:43
 * Description: Remoting address parser.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.remote.help;

import roberto.group.process.netty.practice.connection.ConnectionURL;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Remoting address parser.〉
 *
 *  实现此类用于解析出ConnectionURL对象
 *
 * @author HuangTaiHong
 * @create 2019/1/4
 * @since 1.0.0
 */
public interface RemotingAddressParser {
    /**
     * 功能描述: <br>
     * 〈Parse a simple string url to get ConnectionURL.〉
     *
     * @param url
     * @return > roberto.group.process.netty.practice.connection.ConnectionURL
     * @author HuangTaiHong
     * @date 2019.01.04 09:45:59
     */
    ConnectionURL parse(String url);

    /**
     * 功能描述: <br>
     * 〈Initialize ConnectionURL arguments.〉
     *
     * @param connectionURL
     * @author HuangTaiHong
     * @date 2019.01.04 10:26:10
     */
    void initUrlArgs(ConnectionURL connectionURL);

    /** symbol : */
    char COLON = ':';

    /** symbol = */
    char EQUAL = '=';

    /** symbol & */
    char AND   = '&';

    /** symbol ? */
    char QUES  = '?';
}