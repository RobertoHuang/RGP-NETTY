/**
 * FileName: BitSetConvertUtils
 * Author:   HuangTaiHong
 * Date:     2019/1/4 16:58
 * Description: BitSet and byte convert to each other.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.utils;

import java.util.BitSet;

/**
 * 〈一句话功能简述〉<br>
 * 〈BitSet and byte convert to each other.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/4
 * @since 1.0.0
 */
public class BitSetConvertUtils {
    /**
     * 功能描述: <br>
     * 〈from bit set to byte.〉
     *
     * @param bitSet
     * @return > byte
     * @author HuangTaiHong
     * @date 2019.01.04 17:00:43
     */
    public static byte toByte(BitSet bitSet) {
        int value = 0;
        for (int i = 0; i < bitSet.length(); ++i) {
            if (bitSet.get(i)) {
                value += 1 << i;
            }
        }
        if (bitSet.length() > 7) {
            throw new IllegalArgumentException("The byte value " + value + " generated according to bit set " + bitSet + " is out of range, should be limited between [" + Byte.MIN_VALUE + "] to [" + Byte.MAX_VALUE + "]");
        }
        return (byte) value;
    }


    /**
     * 功能描述: <br>
     * 〈from byte to bit set.〉
     *
     * @param value
     * @return > java.util.BitSet
     * @author HuangTaiHong
     * @date 2019.01.04 17:01:27
     */
    public static BitSet toBitSet(int value) {
        if (value > Byte.MAX_VALUE || value < Byte.MIN_VALUE) {
            throw new IllegalArgumentException("The value " + value + " is out of byte range, should be limited between [" + Byte.MIN_VALUE + "] to [" + Byte.MAX_VALUE + "]");
        }
        BitSet bs = new BitSet();
        int index = 0;
        while (value != 0) {
            if (value % 2 != 0) {
                bs.set(index);
            }
            ++index;
            value = (byte) (value >> 1);
        }
        return bs;
    }
}