/**
 * FileName: CRCUtil
 * Author:   HuangTaiHong
 * Date:     2019/1/8 19:21
 * Description: CRC32 utility.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.utils;

import java.util.zip.CRC32;

/**
 * 〈一句话功能简述〉<br>
 * 〈CRC32 utility.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/8
 * @since 1.0.0
 */
public class CRCUtil {
    private static final ThreadLocal<CRC32> CRC_32_THREAD_LOCAL = ThreadLocal.withInitial(() -> new CRC32());

    /**
     * 功能描述: <br>
     * 〈Compute CRC32 code for byte[].〉
     *
     * @param array
     * @return > int
     * @author HuangTaiHong
     * @date 2019.01.08 19:22:44
     */
    public static final int crc32(byte[] array) {
        return array != null ? crc32(array, 0, array.length) : 0;
    }

    /**
     * 功能描述: <br>
     * 〈Compute CRC32 code for byte[].〉
     *
     * @param array
     * @param offset
     * @param length
     * @return > int
     * @author HuangTaiHong
     * @date 2019.01.08 19:25:49
     */
    public static final int crc32(byte[] array, int offset, int length) {
        CRC32 crc32 = CRC_32_THREAD_LOCAL.get();
        crc32.update(array, offset, length);
        int result = (int) crc32.getValue();
        crc32.reset();
        return result;
    }
}