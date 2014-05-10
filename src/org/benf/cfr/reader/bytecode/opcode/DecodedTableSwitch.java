/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.opcode;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.benf.cfr.reader.bytecode.opcode.DecodedSwitch;
import org.benf.cfr.reader.bytecode.opcode.DecodedSwitchEntry;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.bytestream.BaseByteData;
import org.benf.cfr.reader.util.functors.UnaryFunction;

public class DecodedTableSwitch
implements DecodedSwitch {
    private static final int OFFSET_OF_DEFAULT = 0;
    private static final int OFFSET_OF_LOWBYTE = 4;
    private static final int OFFSET_OF_HIGHBYTE = 8;
    private static final int OFFSET_OF_OFFSETS = 12;
    private final int startValue;
    private final int endValue;
    private final int defaultTarget;
    private final List<DecodedSwitchEntry> jumpTargets;

    public DecodedTableSwitch(byte[] data, int offsetOfOriginalInstruction) {
        int curoffset = offsetOfOriginalInstruction + 1;
        int overflow = curoffset % 4;
        int offset = overflow > 0 ? 4 - overflow : 0;
        BaseByteData bd = new BaseByteData(data);
        int defaultvalue = bd.getS4At(offset + 0);
        int lowvalue = bd.getS4At(offset + 4);
        int highvalue = bd.getS4At(offset + 8);
        int numoffsets = highvalue - lowvalue + 1;
        this.defaultTarget = defaultvalue;
        this.startValue = lowvalue;
        this.endValue = highvalue;
        Map uniqueTargets = MapFactory.newLazyMap(new TreeMap(), new UnaryFunction<Integer, List<Integer>>(){

            @Override
            public List<Integer> invoke(Integer arg) {
                return ListFactory.newList();
            }
        });
        for (int x = 0; x < numoffsets; ++x) {
            int target;
            if ((target = bd.getS4At(offset + 12 + x * 4)) == this.defaultTarget) continue;
            ((List)uniqueTargets.get(target)).add(this.startValue + x);
        }
        this.jumpTargets = ListFactory.newList();
        for (Map.Entry entry : uniqueTargets.entrySet()) {
            this.jumpTargets.add(new DecodedSwitchEntry((List)entry.getValue(), (Integer)entry.getKey()));
        }
    }

    @Override
    public int getDefaultTarget() {
        return this.defaultTarget;
    }

    @Override
    public List<DecodedSwitchEntry> getJumpTargets() {
        return this.jumpTargets;
    }

}

