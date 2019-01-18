/**
 * FileName: DeserializeLevel
 * Author:   HuangTaiHong
 * Date:     2019/1/4 19:02
 * Description: RPC Deserializelevel
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.serialize.serialize.manager;

/**
 * 〈一句话功能简述〉<br>
 * 〈RPC Deserializelevel〉
 *
 * @author HuangTaiHong
 * @create 2019/1/4
 * @since 1.0.0
 */
public class DeserializeLevel {
    /** deserialize only the clazz part of RPC command */
    public final static int DESERIALIZE_CLAZZ = 0x00;

    /** deserialize both header and clazz parts of RPC command */
    public final static int DESERIALIZE_HEADER = 0x01;

    /** deserialize clazz, header, contents all three parts of RPC command */
    public final static int DESERIALIZE_ALL = 0x02;

    public static String valueOf(int value) {
        switch (value) {
            case 0x00:
                return "DESERIALIZE_CLAZZ";
            case 0x01:
                return "DESERIALIZE_HEADER";
            case 0x02:
                return "DESERIALIZE_ALL";
        }
        throw new IllegalArgumentException("Unknown deserialize level value ," + value);
    }
}