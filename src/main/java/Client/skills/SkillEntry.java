package Client.skills;

public class SkillEntry {

    public byte position;
    public long expiration;
    public int skillevel, masterlevel, teachId, teachTimes;
    public byte rank;

    /**
     * 普通技能設置
     */
    public SkillEntry(int skillevel, int masterlevel, long expiration) {
        this.skillevel = skillevel;
        this.masterlevel = masterlevel;
        this.expiration = expiration;
        this.teachId = 0;
        this.teachTimes = 0;
        this.position = -1;
    }

    /**
     * 傳授技能設置
     */
    public SkillEntry(int skillevel, int masterlevel, long expiration, int teachId, int teachTimes) {
        this.skillevel = skillevel;
        this.masterlevel = masterlevel;
        this.expiration = expiration;
        this.teachId = teachId;
        this.teachTimes = teachTimes;
        this.position = -1;
    }

    /**
     * 複製技能設置
     */
    public SkillEntry(int skillevel, int masterlevel, long expiration, int teachId, int teachTimes, byte position) {
        this.skillevel = skillevel;
        this.masterlevel = masterlevel;
        this.expiration = expiration;
        this.teachId = teachId;
        this.teachTimes = teachTimes;
        this.position = position;
    }

    @Override
    public String toString() {
        return skillevel + ":" + masterlevel;
    }
}
