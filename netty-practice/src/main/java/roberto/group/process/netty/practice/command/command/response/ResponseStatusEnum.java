/**
 * FileName: ResponseStatusEnum
 * Author:   HuangTaiHong
 * Date:     2019/1/5 13:30
 * Description: Status of the response.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.command.command.response;

/**
 * 〈一句话功能简述〉<br>
 * 〈Status of the response.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/5
 * @since 1.0.0
 */
public enum ResponseStatusEnum {
    SUCCESS,
    ERROR,
    SERVER_EXCEPTION,
    UNKNOWN,
    SERVER_THREADPOOL_BUSY,
    ERROR_COMM,
    NO_PROCESSOR,
    TIMEOUT,
    CLIENT_SEND_ERROR,
    CODEC_EXCEPTION,
    CONNECTION_CLOSED,
    SERVER_SERIAL_EXCEPTION,
    SERVER_DESERIAL_EXCEPTION;

    public short getValue() {
        switch (this) {
            case SUCCESS:
                return 0x0000;
            case ERROR:
                return 0x0001;
            case SERVER_EXCEPTION:
                return 0x0002;
            case UNKNOWN:
                return 0x0003;
            case SERVER_THREADPOOL_BUSY:
                return 0x0004;
            case ERROR_COMM:
                return 0x0005;
            case NO_PROCESSOR:
                return 0x0006;
            case TIMEOUT:
                return 0x0007;
            case CLIENT_SEND_ERROR:
                return 0x0008;
            case CODEC_EXCEPTION:
                return 0x0009;
            case CONNECTION_CLOSED:
                return 0x0010;
            case SERVER_SERIAL_EXCEPTION:
                return 0x0011;
            case SERVER_DESERIAL_EXCEPTION:
                return 0x0012;

        }
        throw new IllegalArgumentException("Unknown status," + this);
    }

    public static ResponseStatusEnum valueOf(short value) {
        switch (value) {
            case 0x0000:
                return SUCCESS;
            case 0x0001:
                return ERROR;
            case 0x0002:
                return SERVER_EXCEPTION;
            case 0x0003:
                return UNKNOWN;
            case 0x0004:
                return SERVER_THREADPOOL_BUSY;
            case 0x0005:
                return ERROR_COMM;
            case 0x0006:
                return NO_PROCESSOR;
            case 0x0007:
                return TIMEOUT;
            case 0x0008:
                return CLIENT_SEND_ERROR;
            case 0x0009:
                return CODEC_EXCEPTION;
            case 0x0010:
                return CONNECTION_CLOSED;
            case 0x0011:
                return SERVER_SERIAL_EXCEPTION;
            case 0x0012:
                return SERVER_DESERIAL_EXCEPTION;
        }
        throw new IllegalArgumentException("Unknown status value ," + value);
    }
}