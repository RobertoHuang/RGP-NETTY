/**
 * FileName: NormalRequestBodyCustomSerializerWithInvokeContext
 * Author:   HuangTaiHong
 * Date:     2019/1/16 10:43
 * Description: a custom serialize demo.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.serializer;

import org.apache.commons.lang3.StringUtils;
import roberto.group.process.netty.practice.command.command.request.RequestCommand;
import roberto.group.process.netty.practice.command.command.request.impl.RPCRequestCommand;
import roberto.group.process.netty.practice.common.RequestBody;
import roberto.group.process.netty.practice.exception.DeserializationException;
import roberto.group.process.netty.practice.exception.SerializationException;
import roberto.group.process.netty.practice.context.InvokeContext;
import roberto.group.process.netty.practice.serialize.custom.DefaultCustomSerializer;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 〈一句话功能简述〉<br> 
 * 〈a custom serialize demo.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/16
 * @since 1.0.0
 */
public class NormalRequestBodyCustomSerializerWithInvokeContext extends DefaultCustomSerializer {
    private AtomicBoolean serialFlag = new AtomicBoolean();
    private AtomicBoolean deSerialFlag = new AtomicBoolean();

    public static final String SERIALTYPE1_VALUE = "SERIAL1";
    public static final String SERIALTYPE2_VALUE = "SERIAL2";
    public static final String SERIALTYPE_KEY = "serial.type";
    public static final String UNIVERSAL_REQ = "UNIVERSAL REQUEST";

    @Override
    @SuppressWarnings("all")
    public <T extends RequestCommand> boolean serializeContent(T request, InvokeContext invokeContext) throws SerializationException {
        serialFlag.set(true);
        RPCRequestCommand requestCommand = (RPCRequestCommand) request;
        if (StringUtils.equals(SERIALTYPE1_VALUE, invokeContext.get(SERIALTYPE_KEY))) {
            try {
                RequestBody requestObject = (RequestBody) requestCommand.getRequestObject();
                int id = requestObject.getId();
                byte[] msg = requestObject.getMsg().getBytes("UTF-8");
                ByteBuffer byteBuffer = ByteBuffer.allocate(4 + msg.length);
                byteBuffer.putInt(id);
                byteBuffer.put(msg);
                requestCommand.setContent(byteBuffer.array());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            try {
                requestCommand.setContent(UNIVERSAL_REQ.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    @SuppressWarnings("all")
    public <T extends RequestCommand> boolean deserializeContent(T request) throws DeserializationException {
        deSerialFlag.set(true);
        RPCRequestCommand requestCommand = (RPCRequestCommand) request;
        byte[] content = requestCommand.getContent();
        ByteBuffer byteBuffer = ByteBuffer.wrap(content);
        int id = byteBuffer.getInt();
        byte[] dst = new byte[content.length - 4];
        byteBuffer.get(dst, 0, dst.length);
        try {
            String msg = new String(dst, "UTF-8");
            RequestBody requestBody = new RequestBody(id, msg);
            requestCommand.setRequestObject(requestBody);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean isSerialized() {
        return this.serialFlag.get();
    }

    public boolean isDeserialized() {
        return this.deSerialFlag.get();
    }
}