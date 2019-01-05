/**
 * FileName: SerializerManager
 * Author:   HuangTaiHong
 * Date:     2019/1/4 18:50
 * Description: Manage all serializers.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.serialize.serialize.manager;

import roberto.group.process.netty.practice.serialize.serialize.Serializer;
import roberto.group.process.netty.practice.serialize.serialize.impl.HessianSerializer;
import roberto.group.process.netty.practice.serialize.serialize.impl.ProtostuffSerializer;

/**
 * 〈一句话功能简述〉<br>
 * 〈Manage all serializers.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/4
 * @since 1.0.0
 */
public class SerializerManager {
    public static final byte HESSIAN2 = 1;
    public static final byte PROTOSTUFF = 2;
    private static Serializer[] serializers = new Serializer[5];

    static {
        addSerializer(HESSIAN2, new HessianSerializer());
        addSerializer(PROTOSTUFF, new ProtostuffSerializer());
    }

    public static Serializer getSerializer(int idx) {
        return serializers[idx];
    }

    public static void addSerializer(int idx, Serializer serializer) {
        if (serializers.length <= idx) {
            Serializer[] newSerializers = new Serializer[idx + 5];
            System.arraycopy(serializers, 0, newSerializers, 0, serializers.length);
            serializers = newSerializers;
        }
        serializers[idx] = serializer;
    }
}