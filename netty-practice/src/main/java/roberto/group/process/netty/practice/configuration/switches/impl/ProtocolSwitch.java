/**
 * FileName: ProtocolSwitch
 * Author:   HuangTaiHong
 * Date:     2019/1/4 16:51
 * Description: Switches used in protocol, this is runtime switch.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration.switches.impl;

import lombok.NoArgsConstructor;
import lombok.Setter;
import roberto.group.process.netty.practice.configuration.switches.Switch;
import roberto.group.process.netty.practice.utils.BitSetConvertUtils;

import java.util.Arrays;
import java.util.BitSet;

/**
 * 〈一句话功能简述〉<br>
 * 〈Switches used in protocol, this is runtime switch.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/4
 * @since 1.0.0
 */
@NoArgsConstructor
public class ProtocolSwitch implements Switch {
    /** switche index **/
    public static final int CRC_SWITCH_INDEX = 0x000;

    /** default value **/
    public static final boolean CRC_SWITCH_DEFAULT_VALUE = true;

    @Setter
    private BitSet bitSet = new BitSet();

    public static ProtocolSwitch create(int value) {
        ProtocolSwitch status = new ProtocolSwitch();
        status.setBitSet(BitSetConvertUtils.toBitSet(value));
        return status;
    }

    public static ProtocolSwitch create(int[] index) {
        ProtocolSwitch status = new ProtocolSwitch();
        Arrays.stream(index).forEach(i -> status.turnOn(i));
        return status;
    }

    @Override
    public void turnOn(int index) {
        this.bitSet.set(index);
    }

    @Override
    public void turnOff(int index) {
        this.bitSet.clear(index);
    }

    @Override
    public boolean isOn(int index) {
        return this.bitSet.get(index);
    }

    public byte toByte() {
        return BitSetConvertUtils.toByte(this.bitSet);
    }

    public static boolean isOn(int switchIndex, int value) {
        return BitSetConvertUtils.toBitSet(value).get(switchIndex);
    }


}