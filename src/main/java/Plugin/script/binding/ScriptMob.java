package Plugin.script.binding;

import Net.server.life.ForcedMobStat;
import Net.server.life.MapleMonster;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;

@Slf4j
public class ScriptMob {
    @Setter
    @Getter
    private MapleMonster monster;


    public ScriptMob(MapleMonster monster) {
        this.monster = monster;
    }

    public MapleMonster getMob() {
        return getMonster();
    }

    public void changeBaseHp(long maxHp) {
        getMonster().changeBaseHp(maxHp);
    }

    public int getDataId() {
        return getMonster().getId();
    }

    public int getEntityId() {
        return getMonster().getShowMobId();
    }

    public long getHp() {
        return getMonster().getHp();
    }

    public void setHp(long maxHp) {
        getMonster().setHp(maxHp);
    }

    public int getMapId() {
        return getMonster().getMap().getId();
    }

    public long getMaxHp() {
        return getMonster().getMaxHP();
    }

    public void setMaxHp(long maxHp) {
        getMonster().setMaxHP(maxHp);
    }

    public int getLevel() {
        return getMonster().getMobLevel();
    }

    public void setForcedMobStat(int level, int rate) {
        getMonster().changeLevelmod(level, rate);
    }

    public void setHpLimitPercent(double hpLimitPercent) {
        getMonster().setHpLimitPercent(hpLimitPercent);
    }

    public ForcedMobStat getForcedMobStat() {
        return getMonster().getForcedMobStat();
    }

    public void setZoneDataType(int nCurZoneDataType) {
        getMonster().setZoneDataType(nCurZoneDataType);
    }

    public void heal(long heal) {
        getMonster().healHPMP(heal, 10000);
    }

    public void disableSpawnRevives() {
        getMonster().disableSpawnRevives();
    }

    public int getHPPercent() {
        return getMonster().getHPPercent();
    }

    public void setReduceDamageR(double reduceDamageR) {
        getMonster().setReduceDamageR(reduceDamageR);
    }

    public Point getPosition() {
        return getMonster().getPosition();
    }

    public void disableDrops() {
        getMonster().disableDrops();
    }

    public void setInvincible(boolean invincible) {
        getMonster().getStats().setInvincible(invincible);
    }

    public void setRemoveAfter(int var1) {
        getMonster().getStats().setRemoveAfter(var1);
    }

    public void setAppearType(short var1) {
        getMonster().setAppearType(var1);
    }


}
