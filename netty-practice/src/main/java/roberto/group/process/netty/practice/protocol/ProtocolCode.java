/**
 * FileName: ProtocolCode
 * Author:   HuangTaiHong
 * Date:     2019/1/2 19:11
 * Description: 协议码
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.protocol;

import java.util.Arrays;

/**
 * 〈一句话功能简述〉<br> 
 * 〈协议码 - 建议使用单字节〉
 *
 * @author HuangTaiHong
 * @create 2019/1/2
 * @since 1.0.0
 */
public class ProtocolCode {
    byte[] version;

    private ProtocolCode(byte... version) {
        this.version = version;
    }

    public static ProtocolCode fromBytes(byte... version) {
        return new ProtocolCode(version);
    }

    public byte getFirstByte() {
        return this.version[0];
    }

    public int length() {
        return this.version.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProtocolCode that = (ProtocolCode) o;
        return Arrays.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(version);
    }

    @Override
    public String toString() {
        return "ProtocolVersion{" + "version=" + Arrays.toString(version) + '}';
    }
}