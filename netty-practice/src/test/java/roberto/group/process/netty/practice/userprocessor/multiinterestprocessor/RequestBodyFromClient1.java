/**
 * FileName: RequestBodyFromClient1
 * Author:   HuangTaiHong
 * Date:     2019/1/16 16:43
 * Description: request body from client 1.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.userprocessor.multiinterestprocessor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Random;

/**
 * 〈一句话功能简述〉<br> 
 * 〈request body from client 1.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/16
 * @since 1.0.0
 */
@NoArgsConstructor
public class RequestBodyFromClient1 implements MultiInterestBaseRequestBody {
    private static final long serialVersionUID = -103461930947826245L;

    public static final String DEFAULT_CLIENT_STR = "HELLO WORLD! I'm from client--C1";
    public static final String DEFAULT_SERVER_STR = "HELLO WORLD! I'm from server--C1";
    public static final String DEFAULT_SERVER_RETURN_STR = "HELLO WORLD! I'm server return--C1";
    public static final String DEFAULT_CLIENT_RETURN_STR = "HELLO WORLD! I'm client return--C1";

    public static final String DEFAULT_ONEWAY_STR = "HELLO WORLD! I'm oneway req--C1";
    public static final String DEFAULT_SYNC_STR = "HELLO WORLD! I'm sync req--C1";
    public static final String DEFAULT_FUTURE_STR = "HELLO WORLD! I'm future req--C1";
    public static final String DEFAULT_CALLBACK_STR = "HELLO WORLD! I'm call back req--C1";

    /** id */
    @Getter
    @Setter
    private int id;

    /** msg */
    @Getter
    @Setter
    private String msg;

    /** body */
    private byte[] body;

    private Random random = new Random();

    public RequestBodyFromClient1(int id, String msg) {
        this.id = id;
        this.msg = msg;
    }

    public RequestBodyFromClient1(int id, int size) {
        this.id = id;
        this.msg = "";
        this.body = new byte[size];
        random.nextBytes(this.body);
    }

    @Override
    public String toString() {
        return "Body[this.id = " + this.id + ", this.msg = " + this.msg + "]";
    }
}