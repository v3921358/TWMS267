package Client.skills;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class ExtraSkill {
    public int SkillID, TriggerSkillID = 0, FaceLeft = 0, Delay = 0, Value = 0, TargetOID = 0;
    public Point Position;
    public List<Integer> MobOIDs = new LinkedList<>();
    public List<Integer> UnkList = new LinkedList<>();

    public ExtraSkill(int skillId, Point pos) {
        SkillID = skillId;
        Position = pos;
    }
}
