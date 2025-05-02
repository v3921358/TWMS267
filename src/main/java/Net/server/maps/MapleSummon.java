package Net.server.maps;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.anticheat.CheatingOffense;
import Client.skills.SkillFactory;
import Config.constants.SkillConstants;
import Config.constants.enums.SummonAttackType;
import Config.constants.skills.*;
import Config.constants.skills.冒險家_技能群組.type_劍士.英雄;
import Config.constants.skills.冒險家_技能群組.type_劍士.黑騎士;
import Config.constants.skills.冒險家_技能群組.type_法師.主教;
import Config.constants.skills.冒險家_技能群組.type_法師.火毒;
import Config.constants.skills.冒險家_技能群組.*;
import Config.constants.skills.皇家騎士團_技能群組.暗夜行者;
import Config.constants.skills.皇家騎士團_技能群組.烈焰巫師;
import Config.constants.skills.皇家騎士團_技能群組.破風使者;
import Config.constants.skills.皇家騎士團_技能群組.聖魂劍士;
import Net.server.buffs.MapleStatEffect;
import Packet.SummonPacket;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public final class MapleSummon extends AnimatedMapleMapObject {

    private final int ownerid;
    private final int skillLevel;
    private final int ownerLevel;
    private final MapleCharacter owner;
    private long createTime;
    private final SummonMovementType movementType; //召喚獸的移動類型
    private int skillId;
    private int duration;
    private int moveRange;
    private MapleMap map; //required for instanceMaps
    private int hp = 1;
    private boolean changedMap = false;
    //下面是能夠攻擊怪物的召喚獸處理
    private int lastSummonTickCount;
    private byte Summon_tickResetCount;
    private long Server_ClientSummonTickDiff;
    private long lastAttackTime;
    private int mobid;

    private boolean soul1 = false;
    private boolean soul2 = false;

    private MapleStatEffect effect;
    private int acState1, acState2;
    private final int[] state = new int[3];
    private int shadow;
    private int animated;
    private boolean resist = false;

    public MapleSummon(MapleCharacter owner, MapleStatEffect effect, Point pos, SummonMovementType movementType, int duration, int range, int mobid, long startTime) {
        this(owner, effect.getSourceId(), effect.getLevel(), pos, movementType, duration, range, mobid, startTime);
        this.effect = effect;
    }

    public MapleSummon(MapleCharacter owner, int sourceid, int level, Point pos, SummonMovementType movementType, int duration, int range, int mobid, long startTime) {
        this.owner = owner;
        this.map = owner.getMap();
        this.ownerid = owner.getId();
        this.ownerLevel = owner.getLevel();
        this.skillId = sourceid;
        this.skillLevel = level;
        this.movementType = movementType;
        this.createTime = startTime;
        this.duration = duration;
        switch (sourceid) {
            case 陰陽師.雙天狗_左:
            case 陰陽師.雙天狗_右:
                range = 0;
                break;
        }
        this.moveRange = range;
        this.mobid = mobid;

        if (!is替身()) {
            lastSummonTickCount = 0;
            Summon_tickResetCount = 0;
            Server_ClientSummonTickDiff = 0;
            lastAttackTime = 0;
        }
        setPosition(pos);

        MapleFoothold fh;
        setCurrentFh(movementType != SummonMovementType.FLY && ((fh = owner.getMap().getFootholds().findBelow(pos)) != null) ? fh.getId() : 0);
        setStance(owner.isFacingLeft() ? 1 : 0);
        resetAncientCrystal();
    }

    public void resetAncientCrystal() {
        this.acState1 = 0;
        this.acState2 = 0;
        for (int i = 0; i < this.state.length; ++i) {
            this.state[i] = 1;
        }
    }

    @Override
    public int getRange() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if (client.getPlayer() != null) {
            client.announce(SummonPacket.spawnSummon(this));
        }
        if (resist) {
            client.announce(SummonPacket.summonedSetAbleResist(ownerid, getObjectId(), (byte) 0));
        }
        if (skillId == 伊利恩.古代水晶 && client.getPlayer().getId() == getOwnerId()) {
            client.announce(SummonPacket.SummonedStateChange(this, 2, this.acState1, this.acState2));
        }
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(SummonPacket.removeSummon(this, false));
    }

    public void setMap(MapleMap map) {
        this.map = map;
    }

    public MapleCharacter getOwner() {
        return map.getPlayerObject(ownerid);
    }

    public int getOwnerId() {
        return ownerid;
    }

    public int getOwnerLevel() {
        return ownerLevel;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public int getSummonHp() {
        return hp;
    }

    public void setSummonHp(int hp) {
        this.hp = hp;
    }

    public void addSummonHp(int delta) {
        this.hp = Math.max(0, hp + delta);
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getDuration() {
        if (duration == 2100000000) {
            return duration;
        }
        if (this.skillId == 聖魂劍士.ATTACK_極樂之境_II) {
            return this.duration - 50;
        }
        return this.duration - 1000;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getMoveRange() {
        return moveRange;
    }

    public void setMoveRange(int value) {
        moveRange = value;
    }

    public boolean is替身() {
        switch (skillId) {
            case 神射手.幻像箭影:
            case 影武者.幻影替身:
            case 破風使者.SUMMON_翡翠花園:
            case 破風使者.強化翡翠花園:
            case 33111003:
                return true;
        }
        return is天使召喚獸();
    }

    public boolean is天使召喚獸() {
        return SkillConstants.is天使祝福戒指(skillId);
    }

    /**
     * 是否在召喚時顯示當前角色的外觀信息
     *
     * @return
     */
    public boolean showCharLook() {
        if (getOwner() == null) {
            return false;
        }
        switch (skillId) {
            case 聖魂劍士.日月星爆:
            case 精靈遊俠.元素精靈:
            case 精靈遊俠.元素精靈_1:
            case 精靈遊俠.元素精靈_2:
            case 影武者.幻影替身:
            case 暗夜行者.Skill_暗影僕從:
            case 暗夜行者.影幻_40:
            case 暗夜行者.影幻_20:
            case 暗夜行者.暗影侍從:
            case 皮卡啾.皮卡啾暗影:
            case 皮卡啾.皮卡啾暗影_1:
            case 皮卡啾.皮卡啾暗影_2:
                return true;
            default:
                return false;
        }
    }

    public boolean is神箭幻影() {
        return skillId == 神射手.幻像箭影;
    }

    public boolean is靈魂助力() {
        return skillId == 黑騎士.追隨者;
    }

    public boolean is大漩渦() {
        return skillId == 烈焰巫師.漩渦;
    }

    public boolean is黑暗雜耍() {
        return skillId == 夜使者.絕殺領域 || skillId == 暗影神偷.絕殺領域;
    }

    public boolean isSummon() {
        return is天使召喚獸() || SkillFactory.getSkill(skillId).isSummonSkill();
    }

    public SummonMovementType getMovementType() {
        return movementType;
    }

    public byte getAttackType() {
        switch (skillId) {
            case 夜使者.達克魯的秘傳:
                return SummonAttackType.ASSIST_ATTACK_REPET.value;
            case 火毒.劇毒領域:
            case 箭神.回歸箭筒:
            case 神射手.回歸之箭:
            case 開拓者.遺跡解放_爆破:
            case 開拓者.遺跡解放_轉移:
            case 影武者.幻影替身:
            case 槍神.砲艇標記:
            case 破風使者.SUMMON_翡翠花園:
            case 破風使者.強化翡翠花園:
            case 暗夜行者.SUMMON_暗影蝙蝠_召喚獸:
            case 暗夜行者.Skill_暗影僕從:
            case 暗夜行者.影幻_40:
            case 暗夜行者.影幻_20:
            case 機甲戰神.磁場:
            case 35121010:
            case 聖魂劍士.日月星爆:
            case 精靈遊俠.元素精靈:
            case 精靈遊俠.元素精靈_1:
            case 精靈遊俠.元素精靈_2:
            case 神射手.分裂之矢_1:
            case 暗夜行者.暗影侍從:
            case 烈焰巫師.SUMMON_元素火焰:
            case 烈焰巫師.SUMMON_元素火焰II:
            case 烈焰巫師.SUMMON_元素火焰III:
            case 烈焰巫師.SUMMON_元素火焰IV:
            case 烈焰巫師.火焰之魂_獅子:
            case 烈焰巫師.火焰之魂_狐狸:
            case 暗夜行者.Switch_type_Magic_暗影蝙蝠:
            case 虎影.歪曲縮地符:
            case 虎影.歪曲縮地符_向門傳送:
                return SummonAttackType.ASSIST_NONE.value;
            case 主教.天秤之使:
            case 1085:
            case 1087:
            case 黑騎士.追隨者:
            case 傑諾.能量領域_支援:
            case 80000052:
            case 80000053:
            case 80000054:
            case 80000155:
            case 80001154:
            case 80001262:
            case 80010067:
            case 80010068:
            case 80010069:
            case 80010070:
            case 80010071:
            case 80010072:
            case 80010075:
            case 80010076:
            case 80010077:
            case 80010078:
            case 80010079:
            case 80010080:
            case 80011103:
            case 80011104:
            case 80011105:
            case 80011106:
            case 80011107:
            case 80011108:
                return SummonAttackType.ASSIST_HEAL.value;
            case 精靈遊俠.元素騎士:
            case 精靈遊俠.元素騎士1:
            case 精靈遊俠.元素騎士2:
            case 35111001:
            case 35111009:
            case 35111010:
            case 英雄.燃燒靈魂之劍:
                return SummonAttackType.ASSIST_ATTACK_EX.value;
            case 機甲戰神.機器人工廠_RM1:
            case 伊利恩.水晶技能_德烏斯:
            case 隱月.鬼武陣:
                return SummonAttackType.ASSIST_SUMMON.value;
            case 主教.天秤之使_1:
            case 煉獄巫師.死神:
            case 煉獄巫師.死神契約I:
            case 煉獄巫師.死神契約II:
            case 煉獄巫師.死神契約III:
            case 機甲戰神.戰鬥機器_巨人錘:
            case 陰陽師.雙天狗_隱藏:
            case 伊利恩.里幽:
            case 伊利恩.古代水晶:
            case 伊利恩.水晶技能_德烏斯_1:
            case 惡魔殺手.奧爾特羅斯:
            case 惡魔殺手.奧爾特羅斯_1:
            case 伊利恩.靈魂水晶:
                return SummonAttackType.ASSIST_ATTACK_MANUAL.value;
            case 14111010:
                return SummonAttackType.ASSIST_ATTACK_COUNTER.value;
            case 狂豹獵人.召喚美洲豹_銀灰:
            case 狂豹獵人.召喚美洲豹_暗黃:
            case 狂豹獵人.召喚美洲豹_血紅:
            case 狂豹獵人.召喚美洲豹_紫光:
            case 狂豹獵人.召喚美洲豹_深藍:
            case 狂豹獵人.召喚美洲豹_傑拉:
            case 狂豹獵人.召喚美洲豹_白雪:
            case 狂豹獵人.召喚美洲豹_歐尼斯:
            case 狂豹獵人.召喚美洲豹_地獄裝甲:
                return SummonAttackType.ASSIST_ATTACK_JAGUAR.value;
            case 槍神.海盜砲擊艇:
            case 陰陽師.式神炎舞_1:
            case 槍神.船員強化:
            case 槍神.海盜砲擊艇_1:
                return SummonAttackType.ASSIST_UNKNOWN_12.value;
            case 凱撒.超新星守護者:
            case 凱撒.超新星守護者_1:
            case 凱撒.超新星守護者_2:
                return SummonAttackType.ASSIST_UNKNOWN_14.value;
            case 機甲戰神.多重屬性_M_FL:
            case 機甲戰神.微型導彈箱:
                return SummonAttackType.ASSIST_UNKNOWN_15.value;
            case 煉獄巫師.黑魔祭壇:
            case 伊利恩.神怒寶劍:
                return SummonAttackType.ASSIST_UNKNOWN_16.value;
            case 槍神.召喚船員_2轉:
            case 槍神.召喚船員_3轉:
                return SummonAttackType.ASSIST_UNKNOWN_17.value;
            case 開拓者.遺跡解放_釋放:
            case 重砲指揮官.特種猴子部隊:
            case 重砲指揮官.特種猴子部隊_2:
            case 重砲指揮官.特種猴子部隊_3:
            case 夜光.光與暗的洗禮:
            case 龍魔導士.星宮射線:
            case 傑諾.能量領域_融合:
            case 天使破壞者.小萌新吉祥物:
            case 機甲戰神.巨型航母:
            case 菈菈.釋放_波瀾之江_1:
            case 菈菈.釋放_旋風_1:
            case 菈菈.釋放_波瀾之江_3:
            case 菈菈.釋放_旋風_3:
                return SummonAttackType.ASSIST_UNKNOWN_18.value;
            case 虎影.追擊鬼火符:
                return SummonAttackType.ASSIST_UNKNOWN_5.value;
            default:
                return SummonAttackType.ASSIST_ATTACK.value;
        }
    }

    /*
     * 召喚獸移除時的提示
     */
    public byte getRemoveStatus() {
        if (is天使召喚獸()) {
            return 0x0A;
        }
        switch (skillId) {
            case 35111011:
            case 35121010:
            case 機甲戰神.機器人工廠_機器人:
            case 陰陽師.式神炎舞_1:
            case 陰陽師.式神炎舞:
            case 聖魂劍士.ATTACK_極樂之境_II:
            case 重砲指揮官.磁錨:
            case 機甲戰神.磁場:
            case 機甲戰神.輔助機器_H_EX:
            case 機甲戰神.機器人工廠_RM1:
                return 0x05;
            case 35111001:
            case 35111009:
            case 35111010:
            case 機甲戰神.微型導彈箱:
            case 精靈遊俠.元素騎士:
            case 精靈遊俠.元素騎士1:
            case 精靈遊俠.元素騎士2:
            case 機甲戰神.戰鬥機器_巨人錘:
                return 0x0A;
        }
        return (byte) this.animated;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.SUMMON;
    }

    public void CheckSummonAttackFrequency(MapleCharacter chr, int tickcount) {
        int tickdifference = (tickcount - lastSummonTickCount);
        if (tickdifference < SkillFactory.getSummonData(skillId).delay) {
            chr.getCheatTracker().registerOffense(CheatingOffense.FAST_SUMMON_ATTACK);
        }
        long STime_TC = System.currentTimeMillis() - tickcount;
        long S_C_Difference = Server_ClientSummonTickDiff - STime_TC;
        if (S_C_Difference > 500) {
            chr.getCheatTracker().registerOffense(CheatingOffense.FAST_SUMMON_ATTACK);
        }
        Summon_tickResetCount++;
        if (Summon_tickResetCount > 4) {
            Summon_tickResetCount = 0;
            Server_ClientSummonTickDiff = STime_TC;
        }
        lastSummonTickCount = tickcount;
    }

    public void CheckPVPSummonAttackFrequency(MapleCharacter chr) {
        long tickdifference = (System.currentTimeMillis() - lastAttackTime);
        if (tickdifference < SkillFactory.getSummonData(skillId).delay) {
            chr.getCheatTracker().registerOffense(CheatingOffense.FAST_SUMMON_ATTACK);
        }
        lastAttackTime = System.currentTimeMillis();
    }

    public boolean checkLastAttackTime() {
        if (System.currentTimeMillis() - getSkillCoolTime(skillId) * 1000L < lastAttackTime) {
            return false;
        }
        lastAttackTime = System.currentTimeMillis();
        return true;
    }

    /**
     * 召喚獸是否更換地圖
     *
     * @return true = 更換 : false = 沒有
     */
    public boolean isChangedMap() {
        return changedMap;
    }

    /**
     * 寫入更換地圖數據
     *
     * @param npc
     */
    public void setChangedMap(boolean npc) {
        this.changedMap = npc;
    }

    /**
     * 更換地圖的話就取消
     *
     * @return true = 更換地圖就取消 ： false = 更換地圖不取消
     */
    public boolean isChangeMapCanceled() {
        return this.getMovementType() == SummonMovementType.STOP || this.getMovementType() == SummonMovementType.SMART || this.getMovementType() == SummonMovementType.WALK_RANDOM;
    }

    public int getMobid() {
        return mobid;
    }

    public void setMobid(int mobid) {
        this.mobid = mobid;
    }

    public int getSkillCoolTime(int skillId) {
        switch (skillId) {
            case 煉獄巫師.死神:
                return 9;
            case 煉獄巫師.死神契約I:
                return 8;
            case 煉獄巫師.死神契約II:
                return 6;
            case 煉獄巫師.死神契約III:
                return 5;
        }
        return 0;
    }

    public int getSummonMaxCount() {
        return getSummonMaxCount(skillId);
    }

    public static int getSummonMaxCount(int skillID) {
        switch (skillID) {
            case 機甲戰神.機器人工廠_機器人:
            case 煉獄巫師.黑魔祭壇:
                return -1;
            case 暗夜行者.SUMMON_暗影蝙蝠_召喚獸:
                return 2;
            case 機甲戰神.磁場:
            case 開拓者.遺跡解放_轉移:
                return 3;
            case 菈菈.山之種子:
                return 4;
            case 隱月.鬼武陣_1:
                return 10;
            case 英雄.劍士意念_1:
                return 12;
        }
        return 1;
    }

    public boolean isSoul1() {
        return soul1;
    }

    public boolean isSoul2() {
        return soul2;
    }

    public boolean setSoul1(boolean soul1) {
        return this.soul1 = soul1;
    }

    public boolean setSoul2(boolean soul2) {
        return this.soul2 = soul2;
    }

    public int mp(int n) {
        return n - (int) (System.currentTimeMillis() - createTime);
    }

    public int getAcState1() {
        return acState1;
    }

    public void setAcState1(int acState1) {
        this.acState1 = acState1;
    }

    public int getAcState2() {
        return acState2;
    }

    public void setAcState2(int acState2) {
        this.acState2 = acState2;
    }

    public int getState(final int n) {
        return this.state[n];
    }

    public void setState(final int n, final int n2) {
        this.state[n] = 0;
    }

    public int getShadow() {
        return shadow;
    }

    public void setShadow(int shadow) {
        this.shadow = shadow;
    }

    public MapleStatEffect getEffect() {
        return effect;
    }

    public void setAnimated(int animated) {
        this.animated = animated;
    }

    public int getAnimated() {
        return animated;
    }

    public boolean isResist() {
        return resist;
    }

    public void setResist(boolean b) {
        resist = b;
    }

    public static List<Integer> getLinkSummons(int skillID) {
        List<Integer> summons = new LinkedList<>();
        switch (skillID) {
            case 槍神.召喚船員_3轉:
                summons.add(槍神.船員強化);
                break;
            case 重砲指揮官.雙胞胎猴子:
                summons.add(重砲指揮官.雙胞胎猴子_1);
                break;
            case 重砲指揮官.特種猴子部隊:
                summons.add(重砲指揮官.特種猴子部隊_2);
                summons.add(重砲指揮官.特種猴子部隊_2);
                break;
        }
        return summons;
    }

    public int getParentSummon() {
        switch (skillId) {
            case 槍神.船員強化:
                return 槍神.召喚船員_3轉;
            case 重砲指揮官.雙胞胎猴子_1:
                return 重砲指揮官.雙胞胎猴子;
            case 重砲指揮官.特種猴子部隊_2:
            case 重砲指揮官.特種猴子部隊_3:
                return 重砲指揮官.特種猴子部隊;
        }
        return 0;
    }
}
