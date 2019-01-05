/**
 * FileName: HessianSerializer
 * Author:   HuangTaiHong
 * Date:     2019/1/4 18:48
 * Description: Hessian2 serializer
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.serialize.serialize.impl;

import roberto.group.process.netty.practice.exception.CodecException;
import roberto.group.process.netty.practice.serialize.serialize.Serializer;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Hessian2 serializer〉
 *
 * @author HuangTaiHong
 * @create 2019/1/4
 * @since 1.0.0
 */
public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) throws CodecException {
        return new byte[0];
    }

    @Override
    public <T> T deserialize(byte[] data, String classOfT) throws CodecException {
        return null;
    }
}