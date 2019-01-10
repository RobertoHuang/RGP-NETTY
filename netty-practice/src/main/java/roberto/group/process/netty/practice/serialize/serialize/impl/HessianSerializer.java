/**
 * FileName: HessianSerializer
 * Author:   HuangTaiHong
 * Date:     2019/1/4 18:48
 * Description: Hessian2 serializer.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.serialize.serialize.impl;

import com.alipay.hessian.ClassNameResolver;
import com.alipay.hessian.internal.InternalNameBlackListFilter;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import roberto.group.process.netty.practice.exception.CodecException;
import roberto.group.process.netty.practice.serialize.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 〈一句话功能简述〉<br> 
 * 〈Hessian2 serializer.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/4
 * @since 1.0.0
 */
public class HessianSerializer implements Serializer {
    private SerializerFactory serializerFactory = new SerializerFactory();

    public HessianSerializer() {
        //initialize with default black list in hessian
        ClassNameResolver resolver = new ClassNameResolver();
        resolver.addFilter(new InternalNameBlackListFilter(8192));
        serializerFactory.setClassNameResolver(resolver);
    }

    @Override
    public byte[] serialize(Object obj) throws CodecException {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        Hessian2Output output = new Hessian2Output(byteArray);
        output.setSerializerFactory(serializerFactory);
        try {
            output.writeObject(obj);
            output.close();
            return byteArray.toByteArray();
        } catch (IOException e) {
            throw new CodecException("IOException occurred when Hessian serializer encode!", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, String clazz) throws CodecException {
        Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(data));
        input.setSerializerFactory(serializerFactory);
        try {
            Object resultObject = input.readObject();
            input.close();
            return (T) resultObject;
        } catch (IOException e) {
            throw new CodecException("IOException occurred when Hessian serializer decode!", e);
        }
    }
}