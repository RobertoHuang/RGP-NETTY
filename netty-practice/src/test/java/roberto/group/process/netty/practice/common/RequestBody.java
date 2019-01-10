/**
 * FileName: RequestBody
 * Author:   HuangTaiHong
 * Date:     2019/1/10 11:11
 * Description: biz request as a demo.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Random;

/**
 * 〈一句话功能简述〉<br> 
 * 〈biz request as a demo.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/10
 * @since 1.0.0
 */
@NoArgsConstructor
public class RequestBody implements Serializable {
    /** for serialization */
    private static final long serialVersionUID = -1288207208017808618L;

    public static final String DEFAULT_CLIENT_STR        = "HELLO WORLD! I'm from client";
    public static final String DEFAULT_SERVER_STR        = "HELLO WORLD! I'm from server";

    public static final String DEFAULT_SERVER_RETURN_STR = "HELLO WORLD! I'm server return";
    public static final String DEFAULT_CLIENT_RETURN_STR = "HELLO WORLD! I'm client return";

    public static final String DEFAULT_ONEWAY_STR        = "HELLO WORLD! I'm oneway request";
    public static final String DEFAULT_SYNC_STR          = "HELLO WORLD! I'm sync request";
    public static final String DEFAULT_FUTURE_STR        = "HELLO WORLD! I'm future request";
    public static final String DEFAULT_CALLBACK_STR      = "HELLO WORLD! I'm call back request";

    @Getter
    @Setter
    /** id */
    private int id;

    @Getter
    @Setter
    /** msg */
    private String msg;

    /** body */
    private byte[] body;

    private Random random = new Random();

    public RequestBody(int id, String msg) {
        this.id = id;
        this.msg = msg;
    }

    public RequestBody(int id, int size) {
        this.id = id;
        this.msg = "";
        this.body = new byte[size];
        random.nextBytes(this.body);
    }

    @Override
    public String toString() {
        return "Body[this.id = " + id + ", this.msg = " + msg + "]";
    }

    static public enum InvokeType {
        ONEWAY, SYNC, FUTURE, CALLBACK;
    }
}