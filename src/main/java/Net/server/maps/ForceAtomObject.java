package Net.server.maps;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ForceAtomObject {
    public final int Idx;
    public final int DataIndex;
    public final int Index;
    public final int OwnerId;
    public final int Rotate;
    public final int SkillId;
    public final List<Integer> ValueList = new ArrayList();
    public int EnableDelay;
    public Point Position;
    public Point ObjPosition;
    public int Expire;
    public int CreateDelay;
    public int Target;
    public int Idk1, Idk2, Idk3, Idk4, Idk5 = 0;
    public boolean B1, B2, B3 = false;

    public ForceAtomObject(int idx, int dataIndex, int index, int ownerId, int rotate, int skillId) {
        this.Idx = idx;
        this.DataIndex = dataIndex;
        this.Index = index;
        this.OwnerId = ownerId;
        this.Rotate = rotate;
        this.SkillId = skillId;
    }

    public final void addX(int var1) {
        this.ValueList.add(var1);
    }
}
