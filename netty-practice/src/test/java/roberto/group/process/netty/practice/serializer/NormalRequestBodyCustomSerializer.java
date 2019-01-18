/**
 * FileName: NormalRequestBodyCustomSerializer
 * Author:   HuangTaiHong
 * Date:     2019/1/16 9:56
 * Description: a custom serialize demo.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.serializer;

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
public class NormalRequestBodyCustomSerializer extends DefaultCustomSerializer {
    private byte contentSerializer = -1;
    private byte contentDeserializer = -1;

    private AtomicBoolean serialFlag = new AtomicBoolean();
    private AtomicBoolean deSerialFlag = new AtomicBoolean();

    @Override
    public <T extends RequestCommand> boolean serializeContent(T request, InvokeContext invokeContext) throws SerializationException {
        serialFlag.set(true);
        RPCRequestCommand requestCommand = (RPCRequestCommand) request;
        RequestBody requestObject = (RequestBody) requestCommand.getRequestObject();
        int id = requestObject.getId();
        try {
            byte[] message = requestObject.getMsg().getBytes("UTF-8");
            ByteBuffer byteBuffer = ByteBuffer.allocate(4 + message.length);
            byteBuffer.putInt(id);
            byteBuffer.put(message);
            requestCommand.setContent(byteBuffer.array());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        contentSerializer = requestCommand.getSerializer();
        return true;
    }

    @Override
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

        contentDeserializer = requestCommand.getSerializer();
        return true;
    }

    public boolean isSerialized() {
        return this.serialFlag.get();
    }

    public boolean isDeserialized() {
        return this.deSerialFlag.get();
    }

    public byte getContentSerializer() {
        return contentSerializer;
    }

    public byte getContentDeserializer() {
        return contentDeserializer;
    }

    public void reset() {
        this.serialFlag.set(false);
        this.deSerialFlag.set(false);
        this.contentSerializer = -1;
        this.contentDeserializer = -1;
    }
}