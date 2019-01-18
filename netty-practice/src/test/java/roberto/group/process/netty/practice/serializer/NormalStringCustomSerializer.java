/**
 * FileName: NormalStringCustomSerializer
 * Author:   HuangTaiHong
 * Date:     2019/1/16 10:07
 * Description: normal string custom serializer.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.serializer;

import roberto.group.process.netty.practice.command.command.response.ResponseCommand;
import roberto.group.process.netty.practice.command.command.response.impl.RPCResponseCommand;
import roberto.group.process.netty.practice.exception.DeserializationException;
import roberto.group.process.netty.practice.exception.SerializationException;
import roberto.group.process.netty.practice.context.InvokeContext;
import roberto.group.process.netty.practice.serialize.custom.DefaultCustomSerializer;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 〈一句话功能简述〉<br> 
 * 〈normal string custom serializer.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/16
 * @since 1.0.0
 */
public class NormalStringCustomSerializer extends DefaultCustomSerializer {
    private byte contentSerializer = -1;
    private byte contentDeserialier = -1;

    private AtomicBoolean serialFlag = new AtomicBoolean();
    private AtomicBoolean deSerialFlag = new AtomicBoolean();

    @Override
    public <T extends ResponseCommand> boolean serializeContent(T response) throws SerializationException {
        serialFlag.set(true);
        RPCResponseCommand responseCommand = (RPCResponseCommand) response;
        String responseSTR = (String) responseCommand.getResponseObject();
        try {
            responseCommand.setContent(responseSTR.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        contentSerializer = response.getSerializer();
        return true;
    }

    @Override
    public <T extends ResponseCommand> boolean deserializeContent(T response, InvokeContext invokeContext) throws DeserializationException {
        deSerialFlag.set(true);
        RPCResponseCommand responseCommand = (RPCResponseCommand) response;
        try {
            responseCommand.setResponseObject(new String(responseCommand.getContent(), "UTF-8") + "RANDOM");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        contentDeserialier = response.getSerializer();
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

    public byte getContentDeserialier() {
        return contentDeserialier;
    }

    public void reset() {
        this.serialFlag.set(false);
        this.deSerialFlag.set(false);
        this.contentSerializer = -1;
        this.contentDeserialier = -1;
    }
}