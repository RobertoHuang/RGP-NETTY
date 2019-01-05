/**
 * FileName: RPCResponseCommand
 * Author:   HuangTaiHong
 * Date:     2019/1/5 16:38
 * Description: Response command for RPC.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.command.response.impl;

import lombok.Getter;
import lombok.Setter;
import roberto.group.process.netty.practice.command.code.RemoteCommandCode;
import roberto.group.process.netty.practice.command.command.response.ResponseCommand;
import roberto.group.process.netty.practice.configuration.configs.ConfigsSupport;
import roberto.group.process.netty.practice.exception.DeserializationException;
import roberto.group.process.netty.practice.exception.SerializationException;
import roberto.group.process.netty.practice.remote.invoke.context.InvokeContext;
import roberto.group.process.netty.practice.serialize.custom.CustomSerializer;
import roberto.group.process.netty.practice.serialize.custom.manager.CustomSerializerManager;
import roberto.group.process.netty.practice.serialize.serialize.manager.SerializerManager;

import java.io.UnsupportedEncodingException;

/**
 * 〈一句话功能简述〉<br>
 * 〈Response command for RPC.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/5
 * @since 1.0.0
 */
public class RPCResponseCommand extends ResponseCommand {
    /** For serialization */
    private static final long serialVersionUID = 5667111367880018776L;

    @Getter
    @Setter
    private String errorMsg;

    @Getter
    @Setter
    private String responseClass;

    @Getter
    private Object responseHeader;

    @Getter
    @Setter
    private Object responseObject;

    private CustomSerializer customSerializer;

    public RPCResponseCommand() {
        super(RemoteCommandCode.RPC_RESPONSE);
    }

    public RPCResponseCommand(Object response) {
        super(RemoteCommandCode.RPC_RESPONSE);
        this.responseObject = response;
    }

    public RPCResponseCommand(int id, Object response) {
        super(RemoteCommandCode.RPC_RESPONSE, id);
        this.responseObject = response;
    }

    public CustomSerializer getCustomSerializer() {
        if (this.customSerializer != null) {
            return customSerializer;
        }
        if (this.responseClass != null) {
            this.customSerializer = CustomSerializerManager.getCustomSerializer(this.responseClass);
        }
        if (this.customSerializer == null) {
            this.customSerializer = CustomSerializerManager.getCustomSerializer(this.getRemoteCommandCode());
        }
        return this.customSerializer;
    }

    @Override
    public void serializeClazz() throws SerializationException {
        if (this.getResponseClass() != null) {
            try {
                this.setClazz(this.getResponseClass().getBytes(ConfigsSupport.DEFAULT_CHARSET));
            } catch (UnsupportedEncodingException e) {
                throw new SerializationException("Unsupported charset: " + ConfigsSupport.DEFAULT_CHARSET, e);
            }
        }
    }

    @Override
    public void deserializeClazz() throws DeserializationException {
        if (this.getClazz() != null && this.getResponseClass() == null) {
            try {
                this.setResponseClass(new String(this.getClazz(), ConfigsSupport.DEFAULT_CHARSET));
            } catch (UnsupportedEncodingException e) {
                throw new DeserializationException("Unsupported charset: " + ConfigsSupport.DEFAULT_CHARSET, e);
            }
        }
    }

    @Override
    public void serializeHeader(InvokeContext invokeContext) throws SerializationException {
        if (this.getCustomSerializer() != null) {
            try {
                this.getCustomSerializer().serializeHeader(this);
            } catch (SerializationException e) {
                throw e;
            } catch (Exception e) {
                throw new SerializationException("Exception caught when serialize header of rpc response command!", e);
            }
        }
    }

    @Override
    public void deserializeHeader(InvokeContext invokeContext) throws DeserializationException {
        if (this.getHeader() != null && this.getResponseHeader() == null) {
            if (this.getCustomSerializer() != null) {
                try {
                    this.getCustomSerializer().deserializeHeader(this, invokeContext);
                } catch (DeserializationException e) {
                    throw e;
                } catch (Exception e) {
                    throw new DeserializationException("Exception caught when deserialize header of rpc response command!", e);
                }
            }
        }
    }

    @Override
    public void serializeContent(InvokeContext invokeContext) throws SerializationException {
        if (this.getResponseObject() != null) {
            try {
                if (this.getCustomSerializer() != null && this.getCustomSerializer().serializeContent(this)) {
                    return;
                }

                this.setContent(SerializerManager.getSerializer(this.getSerializer()).serialize(this.responseObject));
            } catch (SerializationException e) {
                throw e;
            } catch (Exception e) {
                throw new SerializationException("Exception caught when serialize content of rpc response command!", e);
            }
        }
    }

    @Override
    public void deserializeContent(InvokeContext invokeContext) throws DeserializationException {
        if (this.getResponseObject() == null) {
            try {
                if (this.getCustomSerializer() != null && this.getCustomSerializer().deserializeContent(this, invokeContext)) {
                    return;
                }
                if (this.getContent() != null) {
                    this.setResponseObject(SerializerManager.getSerializer(this.getSerializer()).deserialize(this.getContent(), this.responseClass));
                }
            } catch (DeserializationException e) {
                throw e;
            } catch (Exception e) {
                throw new DeserializationException("Exception caught when deserialize content of rpc response command!", e);
            }
        }
    }
}