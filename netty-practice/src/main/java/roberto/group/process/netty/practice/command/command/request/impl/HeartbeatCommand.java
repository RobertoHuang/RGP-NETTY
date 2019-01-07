/**
 * FileName: HeartbeatCommand
 * Author:   HuangTaiHong
 * Date:     2019/1/7 19:48
 * Description: Heart beat.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.command.request.impl;

import roberto.group.process.netty.practice.command.code.RemoteCommandCode;
import roberto.group.process.netty.practice.command.command.request.RequestCommand;
import roberto.group.process.netty.practice.utils.IDGenerator;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Heart beat.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/7
 * @since 1.0.0
 */
public class HeartbeatCommand extends RequestCommand {
    /** For serialization  */
    private static final long serialVersionUID = 4949981019109517725L;

    public HeartbeatCommand() {
        super(RemoteCommandCode.HEARTBEAT);
        this.setId(IDGenerator.nextId());
    }
}