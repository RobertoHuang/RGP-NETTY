/**
 * FileName: RPCRequestCommand
 * Author:   HuangTaiHong
 * Date:     2019/1/5 9:57
 * Description: Request command for RPC.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.command.request.impl;

import lombok.Getter;
import lombok.Setter;
import roberto.group.process.netty.practice.command.code.RemoteCommandCode;
import roberto.group.process.netty.practice.command.command.request.RequestCommand;
import roberto.group.process.netty.practice.configuration.support.ConfigsSupport;
import roberto.group.process.netty.practice.exception.DeserializationException;
import roberto.group.process.netty.practice.exception.SerializationException;
import roberto.group.process.netty.practice.remote.invoke.context.InvokeContext;
import roberto.group.process.netty.practice.serialize.custom.CustomSerializer;
import roberto.group.process.netty.practice.serialize.custom.manager.CustomSerializerManager;
import roberto.group.process.netty.practice.serialize.serialize.manager.SerializerManager;
import roberto.group.process.netty.practice.utils.IDGenerator;

import java.io.UnsupportedEncodingException;

/**
 * 〈一句话功能简述〉<br>
 * 〈Request command for RPC.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/5
 * @since 1.0.0
 */
public class RPCRequestCommand extends RequestCommand {
    /** For serialization */
    private static final long serialVersionUID = -4602613826188210946L;

    @Getter
    @Setter
    private String requestClass;

    @Getter
    @Setter
    private Object requestHeader;

    @Getter
    @Setter
    private Object requestObject;

    @Getter
    @Setter
    private transient long arriveTime = -1;

    private CustomSerializer customSerializer;

    public RPCRequestCommand() {
        super(RemoteCommandCode.RPC_REQUEST);
    }

    public RPCRequestCommand(Object requestObject) {
        super(RemoteCommandCode.RPC_REQUEST);
        this.requestObject = requestObject;
        this.setId(IDGenerator.nextId());
    }

    public CustomSerializer getCustomSerializer() {
        if (this.customSerializer != null) {
            return customSerializer;
        }
        if (this.requestClass != null) {
            this.customSerializer = CustomSerializerManager.getCustomSerializer(this.requestClass);
        }
        if (this.customSerializer == null) {
            this.customSerializer = CustomSerializerManager.getCustomSerializer(this.getRemoteCommandCode());
        }
        return this.customSerializer;
    }

    @Override
    public void serializeClazz() throws SerializationException {
        if (this.requestClass != null) {
            try {
                this.setClazz(this.requestClass.getBytes(ConfigsSupport.DEFAULT_CHARSET));
            } catch (UnsupportedEncodingException e) {
                throw new SerializationException("Unsupported charset: " + ConfigsSupport.DEFAULT_CHARSET, e);
            }
        }
    }

    @Override
    public void deserializeClazz() throws DeserializationException {
        if (this.getClazz() != null && this.getRequestClass() == null) {
            try {
                this.setRequestClass(new String(this.getClazz(), ConfigsSupport.DEFAULT_CHARSET));
            } catch (UnsupportedEncodingException e) {
                throw new DeserializationException("Unsupported charset: " + ConfigsSupport.DEFAULT_CHARSET, e);
            }
        }
    }

    @Override
    public void serializeHeader(InvokeContext invokeContext) throws SerializationException {
        if (this.getCustomSerializer() != null) {
            try {
                this.getCustomSerializer().serializeHeader(this, invokeContext);
            } catch (SerializationException e) {
                throw e;
            } catch (Exception e) {
                throw new SerializationException("Exception caught when serialize header of RPC request command!", e);
            }
        }
    }

    @Override
    public void deserializeHeader(InvokeContext invokeContext) throws DeserializationException {
        if (this.getHeader() != null && this.getRequestHeader() == null) {
            if (this.getCustomSerializer() != null) {
                try {
                    this.getCustomSerializer().deserializeHeader(this);
                } catch (DeserializationException e) {
                    throw e;
                } catch (Exception e) {
                    throw new DeserializationException("Exception caught when deserialize header of RPC request command!", e);
                }
            }
        }
    }

    @Override
    public void serializeContent(InvokeContext invokeContext) throws SerializationException {
        if (this.requestObject != null) {
            try {
                if (this.getCustomSerializer() != null && this.getCustomSerializer().serializeContent(this, invokeContext)) {
                    return;
                }
                this.setContent(SerializerManager.getSerializer(this.getSerializer()).serialize(this.requestObject));
            } catch (SerializationException e) {
                throw e;
            } catch (Exception e) {
                throw new SerializationException("Exception caught when serialize content of RPC request command!", e);
            }
        }
    }

    @Override
    public void deserializeContent(InvokeContext invokeContext) throws DeserializationException {
        if (this.getRequestObject() == null) {
            try {
                if (this.getCustomSerializer() != null && this.getCustomSerializer().deserializeContent(this)) {
                    return;
                }
                if (this.getContent() != null) {
                    this.setRequestObject(SerializerManager.getSerializer(this.getSerializer()).deserialize(this.getContent(), this.requestClass));
                }
            } catch (DeserializationException e) {
                throw e;
            } catch (Exception e) {
                throw new DeserializationException("Exception caught when deserialize content of RPC request command!", e);
            }
        }
    }
}