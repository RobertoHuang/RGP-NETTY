/**
 * FileName: Serializer
 * Author:   HuangTaiHong
 * Date:     2019/1/4 18:46
 * Description: Serializer for serialize and deserialize.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.serialize.serialize;

import roberto.group.process.netty.practice.exception.CodecException;

/**
 * 〈一句话功能简述〉<br>
 * 〈Serializer for serialize and deserialize.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/4
 * @since 1.0.0
 */
public interface Serializer {
    /**
     * 功能描述: <br>
     * 〈Encode object into bytes.〉
     *
     * @param obj
     * @return > byte[]
     * @throws CodecException
     * @author HuangTaiHong
     * @date 2019.01.04 18:47:12
     */
    byte[] serialize(final Object obj) throws CodecException;

    /**
     * 功能描述: <br>
     * 〈Decode bytes into Object.〉
     *
     * @param <T>
     * @param data
     * @param classOfT
     * @return > T
     * @throws CodecException
     * @author HuangTaiHong
     * @date 2019.01.04 18:47:02
     */
    <T> T deserialize(final byte[] data, String classOfT) throws CodecException;
}