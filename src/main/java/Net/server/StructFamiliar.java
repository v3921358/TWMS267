package Net.server;


public class StructFamiliar {

    private int id, skillID, effectAfter, mobID, monsterCardID, FCategory, range;
    private byte grade;
    private String FAttribute;

    public StructFamiliar(int id, int skillID, int effectAfter, int mobID, int monsterCardID, byte grade, String FAttribute, int FCategory, int range) {
        this.id = id;
        this.skillID = skillID;
        this.effectAfter = effectAfter;
        this.mobID = mobID;
        this.monsterCardID = monsterCardID;
        this.grade = grade;
        this.FAttribute = FAttribute;
        this.FCategory = FCategory;
        this.range = range;
    }

    public int getId() {
        return this.id;
    }

    public int getSkillID() {
        return this.skillID;
    }

    public int getEffectAfter() {
        return this.effectAfter;
    }

    public int getMobID() {
        return this.mobID;
    }

    public int getMonsterCardID() {
        return this.monsterCardID;
    }

    public byte getGrade() {
        return this.grade;
    }

    public String getFAttribute() {
        return this.FAttribute;
    }

    public int getFCategory() {
        return this.FCategory;
    }

    public int getRange() {
        return this.range;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSkillID(int skillID) {
        this.skillID = skillID;
    }

    public void setEffectAfter(int effectAfter) {
        this.effectAfter = effectAfter;
    }

    public void setMobID(int mobID) {
        this.mobID = mobID;
    }

    public void setMonsterCardID(int monsterCardID) {
        this.monsterCardID = monsterCardID;
    }

    public void setGrade(byte grade) {
        this.grade = grade;
    }

    public void setFAttribute(String FAttribute) {
        this.FAttribute = FAttribute;
    }

    public void setFCategory(int FCategory) {
        this.FCategory = FCategory;
    }

    public void setRange(int range) {
        this.range = range;
    }
}
