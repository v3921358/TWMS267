package Client.hexa;

import lombok.Getter;

@Getter
public class HexaSkillCoreEntry {
    private final int id;
    private final int skillId;
    private final int type;
    private final int maxLevel;

    public HexaSkillCoreEntry(int id, int skillid, int type, int maxLevel) {
        this.id = id;
        this.skillId = skillid;
        this.type = type;
        this.maxLevel = maxLevel;
    }
}
