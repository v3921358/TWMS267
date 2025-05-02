package Client.skills;

/**
 * 內在能力技能設置
 *
 * @author PlayDK
 */
public class InnerSkillEntry {

    private final int skillId;
    private final int skillLevel;
    private final byte position, rank;
    private final boolean temp;

    public InnerSkillEntry(int skillId, int skillevel, byte position, byte rank, boolean temp) {
        this.skillId = skillId;
        this.skillLevel = skillevel;
        this.position = position;
        this.rank = rank;
        this.temp = temp;
    }

    public int getSkillId() {
        return skillId;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public byte getPosition() {
        return position;
    }

    public byte getRank() {
        return rank;
    }

    public boolean isTemp() {
        return temp;
    }
}
