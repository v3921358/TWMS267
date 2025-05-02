package Net.server;

public enum MapleStatInfo {

    MDF(0), //最大DF增加 也就是惡魔精氣
    MDamageOver(999999), //技能傷害最大值 默認: 999999
    OnActive(0), //重生成功概率 %
    PVPdamage(0),
    PVPdamageX(0), //Battle Mode ATT Increase
    abnormalDamR(0), //攻擊昏迷，黑暗，凍結的狀態異常對像時，傷害增加x%
    aranComboCon(0), //戰神使用技能消耗連擊點數
    acc(0), //命中值增加
    acc2dam(0), //命中值提升傷害 - 根據物理命中值和魔法命中值中較高數值的x%，增加格外傷害
    acc2mp(0), //命中值提升MP上限 - MP上限增加命中值的x%
    accR(0), //命中值增加 %
    accX(0), //命中值增加
    //action(0),
    actionSpeed(0), //攻擊速度提升
    ar(0), //命中率增加 %
    asrR(0), //Abnormal Status Resistance %
    attackCount(1), //攻擊怪物的次數
    bdR(0), //攻擊BOSS時，傷害增加x%
    bufftimeR(0), //增益持續時間增加 - 增益的持續時間增加x%
    bulletConsume(0), //消耗子彈/飛鏢多少發
    bulletCount(1), //攻擊怪物的數量 當為遠程職業是為 攻擊怪物的次數
    coolTimeR(0), //縮短冷卻時間 %
    cooltime(0), //冷取時間
    cr(0), //爆擊率增加 %
    criticaldamageMax(0), //爆擊最大傷害增加
    criticaldamageMin(0), //爆擊最小傷害增加
    costmpR(0), //消耗更多的MP來增加傷害
    damR(0), //技能增加傷害 % 或者總傷害增加 %
    damR_5th(0),//五轉強化技能增加傷害
    damage(100), //技能傷害 默認 100%
    damageToBoss(100), //對BOSS技能傷害 100%
    damAbsorbShieldR(0), //傷害吸收
    dex(0), //敏捷增加
    dex2str(0), //敏捷提升力量 - 投資了AP敏捷的x%追加到力量
    dexFX(0), //敏捷增加
    dexX(0), //敏捷永久增加
    dexR(0), //敏捷增加 %
    dot(0), //Damage over time %
    dotInterval(0), //Damage dealt at intervals
    dotSuperpos(1), //Damage over time stack
    dotTickDamR(0),
    dotTime(0), //DOT time length (Lasts how long)
    dropR(0), //爆率增加 %
    emad(0), //增強魔法攻擊力
    emdd(0), //增強魔法防禦
    emhp(0), //增加Hp
    emmp(0), //增強Mp
    epad(0), //增強攻擊力
    epdd(0), //增強物理防禦
    er(0), //迴避率增加x%
    eva(0), //Avoidability Increase, avoid
    eva2hp(0), //迴避值提升HP上限 - HP上限增加迴避值的x%
    evaR(0), //迴避值增加 %
    evaX(0), //迴避值增加
    expLossReduceR(0), //Reduce EXP loss at death %
    expR(0), //Additional % EXP
    extendPrice(0), //[Guild] Extend price
    finalAttackDamR(0), //Additional damage from Final Attack skills %
    fixdamage(0), //Fixed damage dealt upon using skill
    forceCon(0), //Fury Cost
    gauge(0),
    gpCon(0),
    hcCooltime(0),
    hcHp(0),
    hcProp(0),
    hcReflect(0),
    hcSubProp(0),
    hcSubTime(0),
    hcSummonHp(0), //召喚獸的Hp
    hcTime(0),
    hp(0), //Mp格外恢復
    hpFX(0),
    hpCon(0), //HP Consumed
    hpRCon(0),
    iceGageCon(0), //Ice Gauge Cost
    ignoreMobDamR(0), //受到怪物攻擊傷害減少x%
    ignoreMobpdpR(0), //Ignore Mob DEF % -> Attack higher
    indieAcc(0), //命中值增加
    indieAllStat(0), //所有屬性增加
    indieAsrR(0), //狀態異常抗性
    indieBDR(0), //對BOSS傷害增加
    indieBooster(0), //攻擊速度提升
    indieCr(0), //暴擊概率增加 %
    indieDamR(0), //攻擊力提高 %
    indieDrainHP(0),
    indieEva(0), //迴避值增加
    indieEvaR(0),
    indieExp(0), //經驗獲得
    indieForceJump(0),
    indieForceSpeed(0),
    indieIgnoreMobpdpR(0), //受到怪物攻擊的傷害減少x%
    indieJump(0), //跳躍力增加
    indieMDF(0), //DF增加 也就是惡魔精氣
    indieMad(0), //魔法攻擊力增加
    indieMadR(0),//魔法攻擊力增加 %
    indieMaxDamageOver(0), //攻擊上限增加
    indieMaxDamageOverR(0), //攻擊上限增加 %
    indieMdd(0), //魔法防禦力增加
    indieMddR(0),
    indieMhp(0), //Hp增加
    indieMhpR(0), //HP Consumed
    indieMmp(0), //Mp增加
    indieMmpR(0), //Mp增加 %
    indiePMdR(0),
    indiePad(0), //攻擊力增加
    indiePadR(0),//攻擊力增加 %
    indiePdd(0), //物理防禦力增加
    indiePddR(0), //物理防禦力增加 %
    indieSpeed(0), //移動速度增加
    indieStance(0), //什麼姿勢
    indieTerR(0), //所有屬性抗性
    indieSTR(0), //力量增加
    indieDEX(0), //敏捷增加
    indieINT(0), //智力增加
    indieLUK(0), //幸運增加
    int2luk(0), //智力提升幸運 - 投資了AP智力的x%追加到幸運
    intFX(0), //智力增加
    intX(0), //永久增加智力
    int_(0, true), //永久增加智力
    intR(0), //智力增加 %
    itemCon(0), //Consumes item upon using <itemid>
    itemConNo(0), //amount for above
    itemConsume(0), //Uses certain item to cast that attack, the itemid doesn't need to be in inventory, just the effect.
    igpCon(0),
    jump(0), //跳躍力增加
    killRecoveryR(0),
    kp(0), //Body count attack stuffs
    luk2dex(0), //幸運提升敏捷 - 投資了AP幸運的x%追加到敏捷
    lukFX(0), //幸運增加
    lukX(0), //永久增加幸運
    luk(0), //永久增加幸運
    lukR(0), //幸運增加 %
    lv2mhp(0), //升級增加血量上限
    lv2mmp(0), //升級增加Mp上限
    lv2damX(0), //Additional damage per character level
    lv2mad(0), //升級增加魔法攻擊力 - 每10級魔法攻擊力增加1
    lv2mdX(0), //Additional magic defense per character level
    lv2pad(0), //升級增加物理攻擊力 - 每10級攻擊力增加1
    lv2pdX(0), //Additional defense per character level
    mad(0), //永久增加魔法攻擊力
    madX(0), //魔法攻擊力增加
    madR(0), //魔法攻擊增加 x%
    mastery(0), //武器熟練度增加 %
    mdR(0), //魔法防禦力增加
    mdd(0), //魔法防禦力增加
    mdd2dam(0), //魔防提升傷害 - 增加魔法防禦力的x%的傷害
    mdd2pdd(0), //魔法防禦力的x%追加到物理防禦力
    mdd2pdx(0), //受到物功減少傷害 - 受到物理攻擊時，無視相當於魔法防禦力x%的傷害
    mddR(0), //魔法防禦力增加 %
    mddX(0), //魔法防禦力增加
    mesoR(0), //楓幣獲得量(怪物掉落)增加 %
    mhp2damX(0), //Max MP added as additional damage
    mhpR(0), //HP上限增加x%
    mhpX(0), //最大Hp 增加 %
    minionDeathProp(0), //攻擊解放戰補給模式的普通怪物時，有x%的概率造成一擊必殺效果
    mmp2damX(0),
    mmpR(0), //MP上限增加x%
    mmpX(0), //最大Mp 增加 %
    mobCount(1), //Max Enemies hit
    mobCountDamR(0), //
    morph(0), //MORPH ID
    mp(0), //Mp格外恢復
    mpCon(0), //使用時消耗的mp數值
    mpConEff(0), //MP Potion effect increase %
    mpConReduce(0), //技能Mp消耗減少 %
    mpRCon(0),
    nbdR(0), //攻擊普通怪物時，傷害增加x%
    nocoolProp(0), //有一定概率無冷卻時間 - 使用技能後，有x%概率無冷卻時間。使用無冷卻時間的技能時無效。
    onActive(0),
    onHitHpRecoveryR(0), //Chance to recover HP when attacking.
    onHitMpRecoveryR(0), //Chance to recover MP when attacking.
    pad(0), //攻擊力增加
    padX(0), //物理攻擊力增加
    padR(0),
    passivePlus(0), //被動技能等級加1 - 被動技能的技能等級增加1級。但對既有主動效果，又有被動效果的技能無效。
    pdd(0), //物理防禦力增加
    pdd2dam(0), //物防提升傷害 - 增加物理防禦力的x%的傷害
    pdd2mdd(0), //物理防禦力的x%追加到魔法防禦力
    pdd2mdx(0), //受到魔攻減少傷害 - 受到魔法攻擊時，無視相當於物理防禦力x%的傷害
    pddR(0), //物理防禦力增加 %
    pddX(0), //物理防禦力增加
    pdR(0), //攻擊面板加成 x%
    period(0), //[Guild/Professions] time taken
    ppCon(0),
    ppRecovery(0),
    price(0), //[Guild] price to purchase
    priceUnit(0), //[Guild] Price stuffs
    prop(100), //觸發的概率 默認100%
    psdJump(0), //跳躍力增加
    psdSpeed(0), //移動速度增加
    psdIncMaxDam(0),
    powerCon(0), //尖兵使用技能時需要的能量
    range(0), //最大射程
    reqGuildLevel(0), //[Guild] guild req level
    reduceForceR(0),
    selfDestruction(0), //Self Destruct Damage
    soulmpCon(0), //靈魂消耗點數
    speed(0), //移動速度增加
    speedMax(0), //移動速度上限增加
    str(0), //力量增加
    str2dex(0), //力量提升敏捷 - 投資了AP力量的x%追加到敏捷
    strFX(0), //力量增加
    strX(0), //力量永久增加
    strR(0), //力量增加 %
    subProp(0), //Summon Damage Prop
    subTime(-1), //Summon Damage Effect time
    suddenDeathR(0), //Instant kill on enemy %
    summonTimeR(0), //Summon Duration + %
    stanceProp(0),
    targetPlus(0), //增加群功技能對像數 - 群攻技能的攻擊對像數量增加1
    targetPlus_5th(0), //五轉強化技能增加攻擊對像
    tdR(0), //阿斯旺解放戰，攻擊塔時，傷害增加x%
    terR(0), //所有屬性抗性增加 %
    time(-1), //技能BUFF的持續時間 或者 給怪物BUFF的持續時間
    limitMin(-1),
    q(0),
    q2(0),
    s(0),
    s2(0),
    t(0), //Damage taken reduce
    u(0),
    u2(0),
    v(0),
    w(0),
    w2(0),
    x(0),
    y(0),
    z(0),
    areaDotCount(0),
    attackDelay(0),
    ballAttackCount(0),
    ballCount(0),
    ballDamage(0),
    ballDelay0(0),
    ballDelay1(0),
    ballDelay2(0),
    ballDelay3(0),
    ballDelay4(0),
    ballDelay5(0),
    ballDelay6(0),
    ballDelay7(0),
    ballDelay(0),
    ballMobCount(0),
    bulletSpeed(0),
    cooltimeMS(0),
    criticaldamage(0),
    damPlus(0),
    damageTW2(0),
    damageTW3(0),
    damageTW4(0),
    delay(0),
    disCountR(0),
    dotHealHPPerSecondR(0),
    dotHealMPPerSecondR(0),
    epCon(0),
    fixCoolTime(0),
    guardProp(0),
    incMobRateDummy(0),
    indieCD(0),
    indieCooltimeReduce(0),
    indieNotDamaged(0),
    itemCursedProtectR(0),
    itemTUCProtectR(0),
    itemUpgradeBonusR(0),
    lv2dex(0),
    lv2int(0),
    lv2luk(0),
    lv2str(0),
    maxDotTickDamR(0),
    maxLevel(0),
    mesoG(0),
    mileage(0),
    orbCount(0),
    pairskill(0),
    ppReq(0),
    pqPointR(0),
    prob(0),
    selfDestructionPlus(0),
    summonCount(0),
    time2(0),
    timeRemainEffect(0),
    trembling(0),
    v2(0),
    indieStatRBasic(0),
    expRPerM(0), //Additional % EXP
    arcX(0),
    atGauge1Con(0),
    atGauge2Con(0),
    atGauge2Inc(0),
    atSkillType(0),
    updatableTime(0),
    dropRate(0),
    plusExpRate(0),
    indiePowerGuard(0),
    ;
    private final int def;
    private final boolean special;

    MapleStatInfo(int def) {
        this.def = def;
        this.special = false;
    }

    MapleStatInfo(int def, boolean special) {
        this.def = def;
        this.special = special;
    }

    public int getDefault() {
        return def;
    }

    public boolean isSpecial() {
        return special;
    }
}
