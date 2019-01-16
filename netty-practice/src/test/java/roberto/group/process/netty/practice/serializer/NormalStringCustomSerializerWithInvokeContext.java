/**
 * FileName: NormalStringCustomSerializerWithInvokeContext
 * Author:   HuangTaiHong
 * Date:     2019/1/16 10:45
 * Description: 
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.serializer;

import org.apache.commons.lang3.StringUtils;
import roberto.group.process.netty.practice.command.command.response.ResponseCommand;
import roberto.group.process.netty.practice.command.command.response.impl.RPCResponseCommand;
import roberto.group.process.netty.practice.exception.DeserializationException;
import roberto.group.process.netty.practice.exception.SerializationException;
import roberto.group.process.netty.practice.remote.invoke.context.InvokeContext;
import roberto.group.process.netty.practice.serialize.custom.DefaultCustomSerializer;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 〈一句话功能简述〉<br> 
 * 〈a custom serialize demo using invoke context.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/16
 * @since 1.0.0
 */
public class NormalStringCustomSerializerWithInvokeContext extends DefaultCustomSerializer {
    private AtomicBoolean serialFlag = new AtomicBoolean();
    private AtomicBoolean deserialFlag = new AtomicBoolean();

    public static final String SERIALTYPE1_VALUE = "SERIAL1";
    public static final String SERIALTYPE_KEY = "serial.type";
    public static final String UNIVERSAL_RESP = "UNIVERSAL RESPONSE";

    @Override
    public <T extends ResponseCommand> boolean serializeContent(T response) throws SerializationException {
        serialFlag.set(true);
        RPCResponseCommand responseCommand = (RPCResponseCommand) response;
        String responSTR = (String) responseCommand.getResponseObject();
        try {
            responseCommand.setContent(responSTR.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public <T extends ResponseCommand> boolean deserializeContent(T response, InvokeContext invokeContext) throws DeserializationException {
        deserialFlag.set(true);
        RPCResponseCommand responseCommand = (RPCResponseCommand) response;
        if (StringUtils.equals(SERIALTYPE1_VALUE, invokeContext.get(SERIALTYPE_KEY))) {
            try {
                responseCommand.setResponseObject(new String(responseCommand.getContent(), "UTF-8") + "RANDOM");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            responseCommand.setResponseObject(UNIVERSAL_RESP);
        }
        return true;
    }

    public boolean isSerialized() {
        return this.serialFlag.get();
    }

    public boolean isDeserialized() {
        return this.deserialFlag.get();
    }
}