/**
 * FileName: RPCProtocol
 * Author:   HuangTaiHong
 * Date:     2019/1/4 10:54
 * Description: RPC协议
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.protocol.impl;

import roberto.group.process.netty.practice.command.codec.CommandDecoder;
import roberto.group.process.netty.practice.command.codec.CommandEncoder;
import roberto.group.process.netty.practice.protocol.Protocol;

/**
 * 〈一句话功能简述〉<br>
 * 〈RPC协议〉
 *
 * @author HuangTaiHong
 * @create 2019/1/4
 * @since 1.0.0
 */
public class RPCProtocol implements Protocol {
    public static final byte PROTOCOL_CODE = (byte) 1;
    public static final int DEFAULT_PROTOCOL_CODE_LENGTH = 1;

    public static final byte PROTOCOL_VERSION_1 = (byte) 1;


    @Override
    public CommandEncoder getEncoder() {
        return null;
    }

    @Override
    public CommandDecoder getDecoder() {
        return null;
    }
}