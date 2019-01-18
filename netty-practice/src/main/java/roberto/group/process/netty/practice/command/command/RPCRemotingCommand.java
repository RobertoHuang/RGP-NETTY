/**
 * FileName: RPCRemotingCommand
 * Author:   HuangTaiHong
 * Date:     2019/1/4 16:14
 * Description: Remoting command.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.command;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import roberto.group.process.netty.practice.command.code.RemoteCommandCode;
import roberto.group.process.netty.practice.configuration.manager.ConfigManager;
import roberto.group.process.netty.practice.configuration.switches.impl.ProtocolSwitch;
import roberto.group.process.netty.practice.exception.DeserializationException;
import roberto.group.process.netty.practice.exception.SerializationException;
import roberto.group.process.netty.practice.remote.invoke.context.InvokeContext;
import roberto.group.process.netty.practice.protocol.ProtocolCode;
import roberto.group.process.netty.practice.protocol.impl.RPCProtocol;
import roberto.group.process.netty.practice.serialize.serialize.manager.DeserializeLevel;

/**
 * 〈一句话功能简述〉<br>
 * 〈Remoting command.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/4
 * @since 1.0.0
 */
@NoArgsConstructor
public abstract class RPCRemotingCommand implements RemotingCommand {
    /** For serialization */
    private static final long serialVersionUID = -3570261012462596503L;

    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private byte type;

    @Getter
    @Setter
    private byte version = 0x1;

    @Getter
    @Setter
    private RemoteCommandCode remoteCommandCode;

    @Getter
    @Setter
    /** Serializer, see the Configs.SERIALIZER_DEFAULT for the default serializer. Notice: this can not be changed after initialized at runtime. **/
    private byte serializer = ConfigManager.serializer;

    @Getter
    @Setter
    private ProtocolSwitch protocolSwitch = new ProtocolSwitch();

    @Getter
    /** The class of content */
    private byte[] clazz;

    @Getter
    private short clazzLength = 0;

    @Getter
    /** Header is used for transparent transmission */
    private byte[] header;

    @Getter
    private short headerLength = 0;

    @Getter
    /** The bytes format of the content of the command */
    private byte[] content;

    @Getter
    private int contentLength = 0;

    @Getter
    @Setter
    /** invoke context of each RPC command */
    private InvokeContext invokeContext;

    public RPCRemotingCommand(byte type) {
        this();
        this.type = type;
    }

    public RPCRemotingCommand(RemoteCommandCode remoteCommandCode) {
        this();
        this.remoteCommandCode = remoteCommandCode;
    }

    public RPCRemotingCommand(byte type, RemoteCommandCode remoteCommandCode) {
        this(remoteCommandCode);
        this.type = type;
    }

    public RPCRemotingCommand(byte version, byte type, RemoteCommandCode remoteCommandCode) {
        this(type, remoteCommandCode);
        this.version = version;
    }

    public void setClazz(byte[] clazz) {
        if (clazz != null) {
            this.clazz = clazz;
            this.clazzLength = (short) clazz.length;
        }
    }

    public void setHeader(byte[] header) {
        if (header != null) {
            this.header = header;
            this.headerLength = (short) header.length;
        }
    }

    public void setContent(byte[] content) {
        if (content != null) {
            this.content = content;
            this.contentLength = content.length;
        }
    }

    @Override
    public void serialize() throws SerializationException {
        this.serializeClazz();
        this.serializeHeader(this.invokeContext);
        this.serializeContent(this.invokeContext);
    }

    @Override
    public void deserialize() throws DeserializationException {
        this.deserializeClazz();
        this.deserializeHeader(this.invokeContext);
        this.deserializeContent(this.invokeContext);
    }

    public void deserialize(long mask) throws DeserializationException {
        if (mask <= DeserializeLevel.DESERIALIZE_CLAZZ) {
            this.deserializeClazz();
        } else if (mask <= DeserializeLevel.DESERIALIZE_HEADER) {
            this.deserializeClazz();
            this.deserializeHeader(this.getInvokeContext());
        } else if (mask <= DeserializeLevel.DESERIALIZE_ALL) {
            this.deserialize();
        }
    }

    @Override
    public ProtocolCode getProtocolCode() {
        return ProtocolCode.fromBytes(RPCProtocol.PROTOCOL_CODE);
    }

    public void serializeClazz() throws SerializationException {

    }

    public void deserializeClazz() throws DeserializationException {

    }

    public void serializeHeader(InvokeContext invokeContext) throws SerializationException {

    }

    public void deserializeHeader(InvokeContext invokeContext) throws DeserializationException {

    }

    @Override
    public void serializeContent(InvokeContext invokeContext) throws SerializationException {

    }

    @Override
    public void deserializeContent(InvokeContext invokeContext) throws DeserializationException {

    }
}