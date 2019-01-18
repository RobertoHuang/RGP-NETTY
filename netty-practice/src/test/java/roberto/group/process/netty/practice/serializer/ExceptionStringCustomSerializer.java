/**
 * FileName: ExceptionStringCustomSerializer
 * Author:   HuangTaiHong
 * Date:     2019/1/16 10:14
 * Description: a String serializer throw exception.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.serializer;

import roberto.group.process.netty.practice.command.command.response.ResponseCommand;
import roberto.group.process.netty.practice.exception.DeserializationException;
import roberto.group.process.netty.practice.exception.SerializationException;
import roberto.group.process.netty.practice.context.InvokeContext;
import roberto.group.process.netty.practice.serialize.custom.DefaultCustomSerializer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 〈一句话功能简述〉<br> 
 * 〈a String serializer throw exception.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/16
 * @since 1.0.0
 */
public class ExceptionStringCustomSerializer extends DefaultCustomSerializer {
    private AtomicBoolean serialFlag               = new AtomicBoolean();
    private AtomicBoolean deSerialFlag             = new AtomicBoolean();

    private boolean       serialException          = false;
    private boolean       serialRuntimeException   = true;

    private boolean       deserialException        = false;
    private boolean       deserialRuntimeException = true;

    public ExceptionStringCustomSerializer(boolean serialException, boolean deserialException) {
        this.serialException = serialException;
        this.deserialException = deserialException;
    }

    public ExceptionStringCustomSerializer(boolean serialException, boolean serialRuntimeException, boolean deserialException, boolean deserialRuntimeException) {
        this.serialException = serialException;
        this.serialRuntimeException = serialRuntimeException;
        this.deserialException = deserialException;
        this.deserialRuntimeException = deserialRuntimeException;
    }

    @Override
    public <T extends ResponseCommand> boolean serializeContent(T response) throws SerializationException {
        serialFlag.set(true);
        if (serialRuntimeException) {
            throw new RuntimeException("serialRuntimeException in ExceptionStringCustomSerializer!");
        } else if (serialException) {
            throw new SerializationException("serialException in ExceptionStringCustomSerializer!");
        } else {
            // use default codec
            return false;
        }
    }

    @Override
    public <T extends ResponseCommand> boolean deserializeContent(T response, InvokeContext invokeContext) throws DeserializationException {
        deSerialFlag.set(true);
        if (deserialRuntimeException) {
            throw new RuntimeException("deserialRuntimeException in ExceptionStringCustomSerializer!");
        } else if (deserialException) {
            throw new DeserializationException("deserialException in ExceptionStringCustomSerializer!");
        } else {
            // use default codec
            return false;
        }
    }

    public boolean isSerialized() {
        return this.serialFlag.get();
    }

    public boolean isDeserialized() {
        return this.deSerialFlag.get();
    }
}