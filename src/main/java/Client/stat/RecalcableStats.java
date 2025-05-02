package Client.stat;

import Client.MapleCharacter;
import org.slf4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

abstract class RecalcableStats {
    static final Logger log = PlayerStats.log;

    int wdef; //物理防禦力

    int speed; //移動速度
    int jump; //跳躍力
    short critRate; //爆擊概率
    short criticalDamage; //爆擊最大傷害倍率
    int percent_wdef; //物理防禦增加x%
    int incMaxHPR; //Hp增加x%
    int incMaxMPR; //Mp增加x%
    int incStrR; //力量增加x%
    int incDexR; //敏捷增加x%
    int incIntR; //智力增加x%
    int incLukR; //幸運增加x%
    int incPadR; //物理攻擊力增加x%
    int incMadR; //魔法攻擊力增加x%
    double ignoreMobpdpR; //無視怪x%防禦
    double incDamR; //傷害增加x%
    int bossDamageR; //BOSS傷害增加x%
    int localstr, localdex, localluk, localint, addmaxhp, addmaxmp;
    int indieStrFX, indieDexFX, indieLukFX, indieIntFX, indieMhpFX, indieMmpFX, indiePadFX, indieMadFX; //內在技能增加屬性 不計算潛能的百分比增加屬性
    int mad, pad;
    int recoverHP;
    int recoverMP;
    int mpconReduce;
    int incMesoProp;
    int reduceCooltime;
    int itemRecoveryUP;
    int skillRecoveryUP;
    int incBuffTime;
    int incAllskill;
    int asr;
    int terR;
    int pvpDamage;
    int incMaxDF;
    double incRewardProp;
    int weaponId;
    int incAttackCount;
    Map<Integer, List<Integer>> equipmentBonusExps;


    void resetLocalStats() {
        wdef = 0;
        addmaxhp = 0;
        addmaxmp = 0;
        localdex = 0;
        localint = 0;
        localstr = 0;
        localluk = 0;
        indieDexFX = 0;
        indieIntFX = 0;
        indieStrFX = 0;
        indieLukFX = 0;
        indieMhpFX = 0;
        indieMmpFX = 0;
        speed = 0;
        jump = 0;
        asr = 0;
        terR = 0;
        percent_wdef = 0;
        incMaxHPR = 0;
        incMaxMPR = 0;
        incStrR = 0;
        incDexR = 0;
        incIntR = 0;
        incLukR = 0;
        incPadR = 0;
        incMadR = 0;
        ignoreMobpdpR = 0;
        critRate = 0;
        criticalDamage = 0;
        incDamR = 0.0;
        bossDamageR = 0;
        mad = 0;
        pad = 0;
        pvpDamage = 0;
        recoverHP = 0;
        recoverMP = 0;
        mpconReduce = 0;
        incMesoProp = 0;
        reduceCooltime = 0;
        incRewardProp = 0.0; //潛能道具所加的裝備掉落幾率
        equipmentBonusExps = new LinkedHashMap<>();
        incBuffTime = 0;
        incMaxDF = 0;
        incAllskill = 0;
        weaponId = 0;
        incAttackCount = 0;
    }

    abstract void recalcLocalStats(boolean first_login, MapleCharacter chra);
}
