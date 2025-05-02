/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import Client.force.MapleForceAtom;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author PlayDK
 */
public class PlayerSpecialStats implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private final HashMap<Integer, AtomicInteger> fieldSkillCounters = new HashMap<>();
    private final AtomicInteger forceCounter = new AtomicInteger(); //惡魔相關
    private int cardStack; //幻影卡片
    private int moonCycle;
    private int hayatoPoint;
    private int pp; //超能力者MP
    private int aranCombo; //戰神連擊點數
    private int mindBreakCount;
    private transient int cylinder;
    private transient int bullet;
    private transient int maxbullet;
    private transient int hurtHP;
    private int jaguarSkillID;
    private int angelReborn;
    private int maelstromMoboid;
    private MapleForceAtom guidedArrow;
    private int remoteDice;
    private long lastShadowBiteTime;
    private int shadowBite;
    private int adeleCharge;
    private int maliceCharge;
    private int shadowHP;
    private int poolMakerCount;
    /**
     * 虎影 符文道力
     */
    private int hoYoungRune;
    /**
     * 虎影 卷軸道力
     */
    private int hoYoungScroll;
    /**
     * 虎影 天属性状态
     */
    private int hoYoungState1;
    /**
     * 虎影 地属性状态
     */
    private int hoYoungState2;
    /**
     * 虎影 人属性状态
     */
    private int hoYoungState3;
    private int flameBeads;
    private int pureBeads;
    private int galeBeads;
    private int abyssBeads;
    private int erosions;

    public void resetSpecialStats() {
        forceCounter.set(0);
        cardStack = 0;
        moonCycle = 0;
        hayatoPoint = 0;
        pp = 0;
        aranCombo = 0;
        cylinder = 0;
        bullet = 0;
        maxbullet = 0;
        remoteDice = -1;
        shadowHP = 0;
        flameBeads = 0;
        pureBeads = 0;
        galeBeads = 0;
        abyssBeads = 0;
        erosions = 0;
        lastShadowBiteTime = -1;
        shadowBite = 0;
        adeleCharge = 0;
        mindBreakCount = 0;
        maliceCharge = 0;
    }

    public int getMindBreakCount() {
        return mindBreakCount;
    }

    public void setMindBreakCount(final int count) {
        mindBreakCount = count;
    }

    public void addMindBreakCount(int val) {
        mindBreakCount += val;
    }

    public int getErosions() {
        return erosions;
    }

    public void setErosions(final int erosions) {
        this.erosions = erosions;
    }

    public void addErosions(int n) {
        this.erosions += n;
        this.erosions = Math.min(1000, Math.max(0, this.erosions));
    }

    public int getFlameBeads() {
        return this.flameBeads;
    }

    public int getPureBeads() {
        return this.pureBeads;
    }

    public void addPureBeads(int val) {
        this.pureBeads += val;
        this.pureBeads = Math.max(0, this.pureBeads);
    }

    public void setPureBeads(final int aAc) {
        this.pureBeads = aAc;
    }

    public void setFlameBeads(final int aDb) {
        this.flameBeads = aDb;
    }

    public void addFlameBeads(int n) {
        this.flameBeads += n;
        this.flameBeads = Math.max(0, this.flameBeads);
    }

    public int getGaleBeads() {
        return this.galeBeads;
    }

    public void setGaleBeads(final int aDc) {
        this.galeBeads = aDc;
    }

    public void addGaleBeads(int n) {
        this.galeBeads += n;
        this.galeBeads = Math.max(0, this.galeBeads);
    }

    public int getAbyssBeads() {
        return this.abyssBeads;
    }

    public void setAbyssBeads(final int aDd) {
        this.abyssBeads = aDd;
    }

    public void addAbyssBeads(int n) {
        this.abyssBeads += n;
        this.abyssBeads = Math.max(0, this.abyssBeads);
    }

    public void addShadowBite(long expiration, int count) {
        if (System.currentTimeMillis() - lastShadowBiteTime >= expiration) {
            shadowBite = 0;
            lastShadowBiteTime = System.currentTimeMillis();
        }
        shadowBite += count;
    }

    public int getShadowBite() {
        return shadowBite;
    }

    public void setAdeleCharge(int val) {
        adeleCharge = val;
    }

    public int getAdeleCharge() {
        return adeleCharge;
    }

    public void setMaliceCharge(int val) {
        maliceCharge = val;
    }

    public int getMaliceCharge() {
        return maliceCharge;
    }

    public int getFieldSkillCounter(int skillID) {
        return fieldSkillCounters.computeIfAbsent(skillID, k -> new AtomicInteger(0)).get();
    }

    public void setFieldSkillCounter(int skillID, int amount) {
        if (amount < 0) {
            amount = 0;
        }
        fieldSkillCounters.computeIfAbsent(skillID, k -> new AtomicInteger(0)).set(amount);
    }

    public int gainFieldSkillCounter(int skillID) {
        AtomicInteger ai = fieldSkillCounters.computeIfAbsent(skillID, k -> new AtomicInteger(0));
        if (ai.get() == Integer.MAX_VALUE) {
            ai.set(0);
        }
        return ai.incrementAndGet();
    }

    public int gainFieldSkillCounter(int skillID, int amount) {
        AtomicInteger ai = fieldSkillCounters.computeIfAbsent(skillID, k -> new AtomicInteger(0));
        if (ai.get() + amount == Integer.MAX_VALUE || ai.get() + amount < 0) {
            ai.set(0);
        }
        return ai.addAndGet(amount);
    }

    public void resetFieldSkillCounters() {
        fieldSkillCounters.clear();
    }

    /**
     * 惡魔相關
     */
    public int getForceCounter() {
        return forceCounter.get();
    }

    public void setForceCounter(int amount) {
        if (amount < 0) {
            amount = 0;
        }
        this.forceCounter.set(amount);
    }

    public int gainForceCounter() {
        if (forceCounter.get() == Integer.MAX_VALUE) {
            forceCounter.set(0);
        }
        return forceCounter.incrementAndGet();
    }

    public void gainForceCounter(int amount) {
        if (forceCounter.get() + amount == Integer.MAX_VALUE || forceCounter.get() + amount < 0) {
            forceCounter.set(0);
        }
        this.forceCounter.addAndGet(amount);
    }

    /**
     * 幻影卡片系統
     */
    public int getCardStack() {
        if (cardStack < 0) {
            cardStack = 0;
        }
        return cardStack;
    }

    public void setCardStack(int amount) {
        this.cardStack = amount;
    }

    public void gainCardStack() {
        this.cardStack++;
    }

    public int getMoonCycle() {
        moonCycle++;
        if (moonCycle > 1) {
            moonCycle = 0;
        }
        return moonCycle;
    }

    public void addHayatoPoint(int n) {
        hayatoPoint = Math.max(0, Math.min(1000, hayatoPoint + n));
    }

    public void gainHayatoPoint(int mode) {
        this.hayatoPoint = Math.min(1000, hayatoPoint + (mode == 1 ? 5 : 2));
    }

    public int getHayatoPoint() {
        return hayatoPoint;
    }

    public void setHayatoPoint(int jianqi) {
        this.hayatoPoint = Math.min(1000, jianqi);
    }

    public void gainPP(int pp) {
        this.pp = Math.min(30, Math.max(0, this.pp + pp));
    }

    public int getPP() {
        return pp;
    }

    public void setPP(int pp) {
        this.pp = Math.min(30, pp);
    }

    public int getAranCombo() {
        return aranCombo;
    }

    public void setAranCombo(int aranCombo) {
        this.aranCombo = aranCombo;
    }

    public int getCylinder() {
        return cylinder;
    }

    public void setCylinder(int cylinder) {
        this.cylinder = cylinder;
    }

    public int getBullet() {
        return bullet;
    }

    public void setBullet(int bullet) {
        this.bullet = bullet;
    }

    public int getMaxBullet() {
        return maxbullet;
    }

    public void setMaxBullet(int maxBullet) {
        this.maxbullet = maxBullet;
    }

    public int getHurtHP() {
        return hurtHP;
    }

    public void setHurtHP(int hurtHP) {
        this.hurtHP = hurtHP;
    }

    public void setJaguarSkillID(int jaguarSkillID) {
        this.jaguarSkillID = jaguarSkillID;
    }

    public int getJaguarSkillID() {
        return jaguarSkillID;
    }

    public int getAngelReborn() {
        return angelReborn;
    }

    public void resetAngelReborn() {
        angelReborn = 0;
    }

    public void gainAngelReborn() {
        angelReborn++;
    }

    public void setMaelstromMoboid(int maelstromMoboid) {
        this.maelstromMoboid = maelstromMoboid;
    }

    public int getMaelstromMoboid() {
        return maelstromMoboid;
    }

    public void setGuidedArrow(MapleForceAtom guidedArrow) {
        this.guidedArrow = guidedArrow;
    }

    public MapleForceAtom getGuidedArrow() {
        return guidedArrow;
    }

    public int getRemoteDice() {
        return remoteDice;
    }

    public void setRemoteDice(int remoteDice) {
        this.remoteDice = remoteDice;
    }

    public void setShadowHP(int shadowHP) {
        this.shadowHP = shadowHP;
    }

    public int getShadowHP() {
        return shadowHP;
    }

    public int getPoolMakerCount() {
        return poolMakerCount;
    }

    public void setPoolMakerCount(int poolMakerCount) {
        this.poolMakerCount = poolMakerCount;
    }


    public int getHoYoungRune() {
        return hoYoungRune;
    }

    public void setHoYoungRune(int hoYoungRune) {
        this.hoYoungRune = hoYoungRune;
    }

    public int getHoYoungScroll() {
        return hoYoungScroll;
    }

    public void setHoYoungScroll(int hoYoungScroll) {
        this.hoYoungScroll = hoYoungScroll;
    }

    public int getHoYoungState1() {
        return hoYoungState1;
    }

    public void setHoYoungState1(int hoYoungState1) {
        this.hoYoungState1 = hoYoungState1;
    }

    public int getHoYoungState2() {
        return hoYoungState2;
    }

    public void setHoYoungState2(int hoYoungState2) {
        this.hoYoungState2 = hoYoungState2;
    }

    public int getHoYoungState3() {
        return hoYoungState3;
    }

    public void setHoYoungState3(int hoYoungState3) {
        this.hoYoungState3 = hoYoungState3;
    }

    public void gainHoYoungRune(int diff) {
        this.hoYoungRune = Math.max(0, Math.min(100, hoYoungRune + diff));
    }

    public void gainHoYoungScroll(int diff) {
        this.hoYoungScroll = Math.max(0, Math.min(900, hoYoungScroll + diff));
    }

}
