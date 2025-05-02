package Client.status;

import Client.MapleCharacter;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MobSkill;

import java.lang.ref.WeakReference;

public class MonsterStatusEffect {

    private int skill;
    private MobSkill mobskill;
    private boolean monsterSkill;
    private int level;
    private MonsterStatus stati;
    private WeakReference<MapleCharacter> weakChr = null;
    private Integer x;
    private int poisonSchedule = 0;
    private int moboid = 0;
    private final boolean reflect = false;
    private long cancelTime = 0, dotTime = 0;
    private final int DOTStack = 0;//疊加
    private long startTime = System.currentTimeMillis();
    private boolean firstUse = true;
    private int angle = 0;
    private int count = 1;
    private int skilevel;
    //該字段可用於結冰(減速的等級)
    private int OngoingSize = 0;
    private int chrid;
    private MapleStatEffect effect;

    //每X秒進行傷害
    private int DOTInterval = 0;
    private int DOTCount = 0;

    /**
     * *
     * 玩家給予怪物debuff
     *
     * @param effect
     * @param x
     * @param skillid
     * @param skillevel
     */
    public MonsterStatusEffect(MapleStatEffect effect, MonsterStatus stati, Integer x, int skillid, int skillevel, int Stack) {
        this.effect = effect;
        this.x = x;
        this.skilevel = skillevel;
        this.skill = skillid;
        this.mobskill = null;
        this.monsterSkill = false;
        this.stati = stati;
        if (Stack > 1000000) {
            Stack /= 1000;
        }
        setCancelTask(Stack);
    }

    /**
     * *
     * 怪物給予怪物的Debuff
     *
     * @param monSkill
     * @param stati
     * @param x
     * @param Stack
     */
    public MonsterStatusEffect(MobSkill monSkill, MonsterStatus stati, Integer x, int Stack) {
        this.effect = null;
        this.x = x;
        this.monsterSkill = true;
        this.mobskill = monSkill;
        this.stati = stati;
        setCancelTask(Stack);
    }

    public MonsterStatusEffect(int i, int i1, long l) {
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public MonsterStatus getStati() {
        return stati;
    }

    public Integer getX() {
        return x * count;
    }

    public void setValue(MonsterStatus status, Integer newVal) {
        stati = status;
        x = newVal;
    }

    public int getSkill() {
        return skill;
    }

    public MobSkill getMobSkill() {
        return mobskill;
    }

    public boolean isMonsterSkill() {
        return monsterSkill;
    }

    /*
     * 獲取怪物取消BUFF的時間
     */
    public long getCancelTask() {
        return this.cancelTime;
    }

    /*
     * 設置取消怪物BUFF的時間
     */
    public void setCancelTask(long cancelTask) {
        this.cancelTime = System.currentTimeMillis() + cancelTask;
    }

    public long getDotTime() {
        return this.dotTime;
    }

    public void setDotTime(long duration) {
        this.dotTime = duration;
    }

    public void setPoisonSchedule(int poisonSchedule, MapleCharacter chrr) {
        this.poisonSchedule = poisonSchedule;
        this.weakChr = new WeakReference<>(chrr);
    }

    public int getPoisonSchedule() {
        return this.poisonSchedule;
    }

    public boolean shouldCancel(long now) {
        return (cancelTime > 0 && cancelTime <= now);
    }

    public void cancelTask() {
        cancelTime = 0;
    }

    public boolean isReflect() {
        return reflect;
    }

    public int getFromID() {
        return weakChr == null || weakChr.get() == null ? 0 : weakChr.get().getId();
    }

    public int GetStack() {
        return this.DOTStack;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public boolean isFirstUse() {
        return firstUse;
    }

    public void setFirstUse(boolean firstUse) {
        this.firstUse = firstUse;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count, boolean gain) {
        this.count = gain ? count : (this.count += count);
    }

    public int getMoboid() {
        return moboid;
    }

    public void setMoboid(int moboid) {
        this.moboid = moboid;
    }

    /**
     * @return the OngoingSize
     */
    public int getOngoingSize() {
        return OngoingSize;
    }

    /**
     * @param OngoingSize the OngoingSize to set
     */
    public void setOngoingSize(int OngoingSize) {
        this.OngoingSize = OngoingSize;
    }

    public void SetDOTInterval(int Interval) {
        DOTInterval = Interval;
    }

    public int getDOTInterval() {
        return DOTInterval;
    }

    /**
     * *
     * 該作用是每X秒進行傷害
     *
     * @return
     */
    public boolean HanlderDOTCount() {
        DOTCount++;
        if (DOTCount >= DOTInterval / 1000) {
            DOTCount = 0;
            return true;
        }

        return false;
    }

    public void setValue(Integer newVal) {
        x = newVal;
    }

    public MapleStatEffect getEffect() {
        return this.effect;
    }

    public int getChrid() {
        return chrid;
    }

    public void setChrid(int id) {
        chrid = id;
    }

    public int getSkillevel() {
        return this.skilevel;
    }
}
