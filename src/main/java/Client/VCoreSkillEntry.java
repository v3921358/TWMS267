package Client;

public class VCoreSkillEntry {
    private int level, exp, slot;
    private final int vcoreid, skill1, skill2, skill3;
    private long dateExpire;
    private int index;

    public VCoreSkillEntry(int vcoreid, int level, int exp, int skill1, int skill2, int skill3, long dateExpire, int slot, int index) {
        this.vcoreid = vcoreid;
        this.level = level;
        this.exp = exp;
        this.skill1 = skill1;
        this.skill2 = skill2;
        this.skill3 = skill3;
        this.dateExpire = dateExpire;
        this.slot = slot;
        this.index = index;
    }

    public int getType() {
        return vcoreid / 10000000 - 1;
    }

    public void gainExp(int gain) {
        this.exp += gain;
    }

    public void levelUP() {
        this.level++;
    }

    public int getSkill(int slot) {
        switch (slot) {
            case 1:
                return skill1;
            case 2:
                return skill2;
            case 3:
                return skill3;
            default:
                return 0;
        }
    }

    public int getVcoreid() {
        return vcoreid;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getSkill1() {
        return skill1;
    }

    public int getSkill2() {
        return skill2;
    }

    public int getSkill3() {
        return skill3;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public long getDateExpire() {
        return dateExpire;
    }

    public void setDateExpire(long dateExpire) {
        this.dateExpire = dateExpire;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}