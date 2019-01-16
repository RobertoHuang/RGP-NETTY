/**
 * FileName: MultiInterestBaseRequestBody
 * Author:   HuangTaiHong
 * Date:     2019/1/16 16:39
 * Description: multi interest base request body.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.userprocessor.multiinterestprocessor;

import java.io.Serializable;

/**
 * 〈一句话功能简述〉<br> 
 * 〈multi interest base request body.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/16
 * @since 1.0.0
 */
public interface MultiInterestBaseRequestBody extends Serializable {
    int getId();

    void setId(int id);

    String getMsg();

    void setMsg(String msg);
}