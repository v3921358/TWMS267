package Client.stat;

import Client.MapleCharacter;
import Client.MapleTraitType;
import Client.MonsterFamiliar;
import Client.SecondaryStat;
import Client.inventory.*;
import Client.skills.Skill;
import Client.skills.SkillEntry;
import Client.skills.SkillFactory;
import Config.constants.GameConstants;
import Config.constants.ItemConstants;
import Config.constants.JobConstants;
import Config.constants.SkillConstants;
import Config.constants.skills.惡魔復仇者;
import Config.constants.skills.陰陽師;
import Net.server.*;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.Element;
import Net.server.quest.MapleQuest;
import tools.Pair;

import java.util.*;

public class EquipRecalcableStats extends RecalcableStats {

    private final static int[] allJobs = {0, 10000000, 20000000, 20010000, 20020000, 20030000, 20040000, 20050000, 30000000, 30010000, 30020000, 40010000, 40020000, 50000000, 60000000, 60010000, 60020000, 100000000, 110000000, 130000000, 140000000, 150000000, 150010000};

    final List<Equip> durabilityHandling = new ArrayList<>();
    final List<Equip> equipLevelHandling = new ArrayList<>();
    final List<Equip> sealedEquipHandling = new ArrayList<>();
    final Map<Integer, Pair<Integer, Integer>> hpRecover_itemOption = new HashMap<>(); // 攻擊時概率回復HP
    final Map<Integer, Pair<Integer, Integer>> mpRecover_itemOption = new HashMap<>(); // 攻擊時概率回復MP
    final List<Integer> equipSummons = new ArrayList<>(); // 裝備自帶召喚獸，如天使祝福戒指
    final Map<Integer, Integer> skillsIncrement = new HashMap<>();
    final Map<Integer, Integer> equipmentSkills = new HashMap<>();
    final Map<Integer, Pair<Integer, Integer>> ignoreDAM = new HashMap<>();
    final Map<Integer, Pair<Integer, Integer>> ignoreDAMr = new HashMap<>();
    final Map<Integer, Pair<Integer, Integer>> DAMreflect = new HashMap<>();
    final EnumMap<Element, Integer> elemBoosts = new EnumMap<>(Element.class);


    int localmaxhp_, localmaxmp_;
    int recallRingId;
    int element_def, element_ice, element_fire, element_light, element_psn;
    int passivePlus;
    MapleWeapon wt;
    int harvestingTool; //採集工具在背包的坐標位置
    boolean canFish, canFishVIP;
    int starForce;
    int arc, aut;
    int levelBonus; //減少裝備的穿戴等級
    int questBonus; //任務經驗獎勵倍數
    int expCardRate;
    int dropCardRate;
    int weaponAttack;

    @Override
    void resetLocalStats() {
        super.resetLocalStats();

        durabilityHandling.clear();
        equipLevelHandling.clear();
        sealedEquipHandling.clear();
        hpRecover_itemOption.clear();
        mpRecover_itemOption.clear();
        equipSummons.clear();
        skillsIncrement.clear();
        equipmentSkills.clear();
        elemBoosts.clear();
        DAMreflect.clear();
        ignoreDAMr.clear();
        ignoreDAM.clear();

        wt = MapleWeapon.沒有武器;
        element_fire = 100;
        element_ice = 100;
        element_light = 100;
        element_psn = 100;
        element_def = 100;
        passivePlus = 0;
        harvestingTool = 0;
        canFish = false;
        canFishVIP = false;
        recallRingId = 0;
        starForce = 0;
        arc = 0;
        questBonus = 1;
        levelBonus = 0;
        dropCardRate = 100;
        expCardRate = 100;
        weaponAttack = 0;
        localmaxhp_ = 0;
        localmaxmp_ = 0;
    }

    @Override
    void recalcLocalStats(boolean firstLogin, MapleCharacter player) {

        resetLocalStats();

        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();


        final List<Integer> jokerToSetItems = new ArrayList<>(); //適配任何套裝的幸運道具，需要3個以上真實套裝才能觸發
        final Map<Integer, List<Integer>> setHandling = new HashMap<>();

        final Equip weapon = (Equip) player.getInventory(MapleInventoryType.EQUIPPED).getItem(JobConstants.is神之子(player.getJob()) && player.isBeta() ? (short) -10 : -11);
        final Equip shield = (Equip) player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -10);
        if (weapon == null) {
            wt = MapleWeapon.沒有武器;
        } else {
            weaponId = weapon.getItemId();
            wt = MapleWeapon.getByItemID(weapon.getItemId());
            if (weaponId == 1402224) {
                ++incAttackCount;
            }
            if (ItemConstants.類型.魔法武器(weaponId)) {
                weaponAttack = weapon.getTotalMad();
            } else {
                weaponAttack = weapon.getTotalPad();
            }
        }

        final boolean wuxing = JobConstants.is陰陽師(player.getJob()) && player.getSkillLevel(陰陽師.五行的陰陽師) > 0;
        final boolean blood = JobConstants.is惡魔復仇者(player.getJob()) && player.getSkillLevel(惡魔復仇者.血之限界) > 0;
        StructItemOption soc;
        Map<Integer, SkillEntry> sData = new HashMap<>();
        for (Item item1 : player.getInventory(MapleInventoryType.EQUIPPED).newList()) {
            Equip equip = (Equip) item1;
            int itemId = equip.getItemId();
            if (equip.getPosition() == -5200 || equip.getPosition() == -32 && player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -33) == null || equip.getPosition() == -33 && player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -32) == null) {
                continue;
            }
            if (player.getLevel() < equip.getTotalReqLevel()) {
                continue;
            }
            if (EnhanceResultType.EQUIP_MARK.check(equip.getEnchantBuff())) {
                continue;
            }
            if (player.getBuffStatValueHolder(SecondaryStat.DispelItemOption) == null) {
                Pair<Integer, Integer> ix = handleEquipAdditions(ii, player, firstLogin, sData, itemId);
                if (ix != null) {
                    localmaxhp_ += ix.getLeft();
                    localmaxmp_ += ix.getRight();
                }
                int[] potentials = {0, 0, 0, 0, 0, 0, equip.getSoulOption()};
                if (equip.getState(false) >= 17) {
                    potentials[0] = equip.getPotential(1, false);
                    potentials[1] = equip.getPotential(2, false);
                    potentials[2] = equip.getPotential(3, false);
                }
                if (equip.getState(true) >= 17) {
                    potentials[3] = equip.getPotential(1, true);
                    potentials[4] = equip.getPotential(2, true);
                    potentials[5] = equip.getPotential(3, true);
                }
                for (int i : potentials) {
                    if (i > 0) {
                        int itemReqLevel = ii.getReqLevel(itemId);
                        //System.err.println("潛能ID: " + i + " 裝備等級: " + itemReqLevel + " 潛能等級: " + (itemReqLevel - 1) / 10);
                        List<StructItemOption> potentialInfo = ii.getPotentialInfo(i);
                        soc = potentialInfo.get(Math.max(0, Math.min(potentialInfo.size() - 1, (itemReqLevel - 1) / 10)));
                        if (soc != null) {
                            localmaxhp_ += soc.get("incMHP");
                            if (wuxing) {
                                localmaxhp_ += soc.get("incMHP");
                            } else {
                                localmaxmp_ += soc.get("incMMP");
                            }
                            handleItemOption(itemId, soc, player, firstLogin, sData);
                        }
                    }
                }
                if (equip.getSocketState() >= 0x13) {
                    int[] sockets = {equip.getSocket1(), equip.getSocket2(), equip.getSocket3()};
                    for (int i : sockets) {
                        if (i > 0) {
                            soc = ii.getSocketInfo(i);
                            if (soc != null) {
                                localmaxhp_ += soc.get("incMHP");
                                if (wuxing) {
                                    localmaxhp_ += soc.get("incMHP");
                                } else {
                                    localmaxmp_ += soc.get("incMMP");
                                }
                                handleItemOption(itemId, soc, player, firstLogin, sData);
                            }
                        }
                    }
                }
                if (equip.getDurability() > 0) {
                    durabilityHandling.add(equip);
                }
            }
            // 神之子過濾未使用的武器
            if (JobConstants.is神之子(player.getJob()) && ((player.isBeta() && equip.getPosition() == -11) || (!player.isBeta() && equip.getPosition() == -10))) {
                continue;
            }
            if (itemId / 10000 == 171) { //秘法符文加的屬性是最終屬性
                arc += equip.getARC();
                aut += equip.getAut();
                indieStrFX += equip.getTotalStr();
                indieDexFX += equip.getTotalDex();
                indieIntFX += equip.getTotalInt();
                indieLukFX += equip.getTotalLuk();
                indieMhpFX += equip.getTotalHp();
                pad += equip.getTotalPad();
                mad += equip.getTotalMad();
                wdef += equip.getTotalPdd();
            } else {
                if (equip.getPosition() == -11 && ItemConstants.類型.魔法武器(itemId)) {
                    Map<String, Integer> eqstat = ii.getItemBaseInfo(itemId);
                    if (eqstat != null) { //slow, poison, darkness, seal, freeze
                        if (eqstat.containsKey("incRMAF")) {
                            element_fire = eqstat.get("incRMAF");
                        }
                        if (eqstat.containsKey("incRMAI")) {
                            element_ice = eqstat.get("incRMAI");
                        }
                        if (eqstat.containsKey("incRMAL")) {
                            element_light = eqstat.get("incRMAL");
                        }
                        if (eqstat.containsKey("incRMAS")) {
                            element_psn = eqstat.get("incRMAS");
                        }
                        if (eqstat.containsKey("elemDefault")) {
                            element_def = eqstat.get("elemDefault");
                        }
                    }
                }
                if (blood) {
                    localmaxhp_ += equip.getTotalHp() / 2;
                } else {
                    localmaxhp_ += equip.getTotalHp();
                }
                if (wuxing) {
                    localmaxhp_ += equip.getTotalMp();
                } else {
                    localmaxmp_ += equip.getTotalMp();
                }
                //--
                if (ItemConstants.類型.機器人(itemId) && equip.getAndroid() != null && player.getAndroid() == null) {
                    player.setAndroid(equip.getAndroid());
                }
                player.getTrait(MapleTraitType.craft).addLocalExp(equip.getTotalHands());
                //--
                starForce += equip.getStarForceLevel();
                localdex += equip.getTotalDex();
                localint += equip.getTotalInt();
                localstr += equip.getTotalStr();
                localluk += equip.getTotalLuk();
                mad += equip.getTotalMad();
                pad += equip.getTotalPad();
                wdef += equip.getTotalPdd();
                speed += equip.getTotalSpeed();
                jump += equip.getTotalJump();
                pvpDamage += equip.getPVPDamage();
                bossDamageR += equip.getTotalBossDamage();
                addIgnoreMobpdpR(equip.getTotalIgnorePDR());
                incDamR += equip.getTotalTotalDamage();
                int allstat = equip.getTotalAllStat();
                if (allstat > 0) {
                    incStrR += allstat;
                    incDexR += allstat;
                    incIntR += allstat;
                    incLukR += allstat;
                }
//                if (equip.getFtExpireDate() > 0L && ii.isCash(itemId)) {
//                    final int[] array3 = {equip.getAnOption(0), equip.getAnOption(1), equip.getAnOption(2)};
//                    for (int n5 = 0; n5 < 3; ++n5) {
//                        final int n7;
//                        final int n6 = (n7 = array3[n5]) % 1000;
//                        switch (n7 / 1000) {
//                            case 11: {
//                                this.localstr += n6;
//                                break;
//                            }
//                            case 12: {
//                                this.localdex += n6;
//                                break;
//                            }
//                            case 13: {
//                                this.localint += n6;
//                                break;
//                            }
//                            case 14: {
//                                this.localluk += n6;
//                                break;
//                            }
//                            case 21: {
//                                this.pad += n6;
//                                break;
//                            }
//                            case 22: {
//                                this.mad += n6;
//                                break;
//                            }
//                        }
//                    }
//                }
                //惡魔獵手的盾牌加的Mp單獨計算
                if (itemId / 1000 == 1099) {
                    incMaxDF += equip.getTotalMp();
                }
                switch (itemId) {
                    case 1112127: // Welcome Back
                    case 1112917: // 超級新人王戒指
                    case 1112918: // I'm Back 戒指
                        recallRingId = itemId;
                        break;
                    default:
                        List<Integer> bonusExps = ii.getBonusExps(itemId);
                        if (bonusExps != null && !bonusExps.isEmpty()) {
                            equipmentBonusExps.put(Math.abs(equip.getPosition()), bonusExps);
                        }
                        break;
                }
                incMaxHPR += ii.getItemIncMHPr(itemId); //增加百分比的HP
                if (wuxing) {
                    incMaxHPR += ii.getItemIncMMPr(itemId); //增加百分比的MP
                } else {
                    incMaxMPR += ii.getItemIncMMPr(itemId); //增加百分比的MP
                }
                int summonid = ItemConstants.getEquipSummon(itemId);
                if (summonid > 0) {
                    equipSummons.add(summonid);
                }
                //套裝屬性
                if (ii.isEpicItem(itemId) || ii.isJokerToSetItem(itemId)) {
                    jokerToSetItems.add(itemId);
                }
                Integer setId = ii.getSetItemID(itemId);
                if (setId != null && setId > 0) {
                    setHandling.computeIfAbsent(setId, k -> new ArrayList<>()).add(itemId);
                }
                if (equip.getIncSkill() > 0 && ii.getEquipSkills(itemId) != null) {
                    for (int skillId : ii.getEquipSkills(itemId)) {
                        if (equip.getIncSkill() != skillId) {
                            continue;
                        }
                        Skill skil = SkillFactory.getSkill(skillId);
                        if (skil != null) { //dont go over masterlevel :D
                            skillsIncrement.merge(skil.getId(), 1, (a, b) -> a + b);
                        }
                    }
                }
                for (Pair<Integer, Integer> skillEntry : ii.getEquipmentSkills(itemId)) {
                    Skill skil = SkillFactory.getSkill(skillEntry.getLeft());
                    int value = ii.getEquipmentSkillsFixLevel(itemId);
                    if (skil != null && value > 0) {
                        equipmentSkills.merge(skil.getId(), value, (a, b) -> Math.min(a + b, skillEntry.getRight()));
                    }
                }
                //--
                if (ItemConstants.getMaxLevel(itemId) > 0 && (GameConstants.getStatFromWeapon(itemId) == null ? (equip.getEquipLevel() <= ItemConstants.getMaxLevel(itemId)) : (equip.getEquipLevel() < ItemConstants.getMaxLevel(itemId)))) {
                    equipLevelHandling.add(equip);
                }
                if (equip.isSealedEquip()) {
                    sealedEquipHandling.add(equip);
                }
                //--
            }
        } // 裝備處理結束

        // 處理投擲物品增加攻擊力
        switch (wt) {
            case 弓: {
                final Item arrowSlot;
                if ((arrowSlot = player.getInventory(MapleInventoryType.USE).getArrowSlot(player.getLevel())) == null) {
                    break;
                }
                if (player.getBuffedValue(SecondaryStat.SoulArrow) == null) {
                    pad += ii.getWatkForProjectile(arrowSlot.getItemId());
                }
                break;
            }
            case 弩: {
                final Item crossbowSlot;
                if ((crossbowSlot = player.getInventory(MapleInventoryType.USE).getCrossbowSlot(player.getLevel())) == null) {
                    break;
                }
                if (player.getBuffedValue(SecondaryStat.SoulArrow) == null) {
                    pad += ii.getWatkForProjectile(crossbowSlot.getItemId());
                    break;
                }
                break;
            }
            case 拳套: {
                final Item dartsSlot;
                if ((dartsSlot = player.getInventory(MapleInventoryType.USE).getDartsSlot(player.getLevel())) != null) {
                    pad += ii.getWatkForProjectile(dartsSlot.getItemId());
                    break;
                }
                break;
            }
            case 火槍: {
                final Item bulletSlot;
                if ((bulletSlot = player.getInventory(MapleInventoryType.USE).getBulletSlot(player.getLevel())) != null) {
                    pad += ii.getWatkForProjectile(bulletSlot.getItemId());
                    break;
                }
                break;
            }
        }

        if (player.getSummonedFamiliar() != null) {
            final MonsterFamiliar summonedFamiliar = player.getSummonedFamiliar();
            for (int i = 0; i < 3; ++i) {
                final int option = summonedFamiliar.getOption(i);
                if (option > 0) {
                    soc = ii.getFamiliar_option().get(option).get(Math.max(summonedFamiliar.getGrade(), 0));
                    if (soc != null) {
                        localmaxhp_ += soc.get("incMHP");
                        if (wuxing) {
                            localmaxhp_ += soc.get("incMMP");
                        } else {
                            localmaxmp_ += soc.get("incMMP");
                        }
                        handleItemOption(summonedFamiliar.getFamiliar(), soc, player, firstLogin, sData);
                    }
                }
            }
        }

        //todo 重構MaplePet和PetItem，處理寵物訓練

//            final HashMap<Integer, Integer> options = new HashMap<>();
//            for (MaplePet pet : player.getSpawnPets()) {
//                if (pet != null && pet.getAddSkill() > 0) {
//                    final int n10 = pet.getAddSkill() - 1;
//                    if (!options.containsKey(n10)) {
//                        options.put(n10, 1);
//                    } else {
//                        options.replace(n10, options.get(n10) + 1);
//                    }
//                }
//            }
//            for (final Map.Entry<Integer, Integer> entry3 : options.entrySet()) {
//                for (int n11 = 0; n11 < entry3.getValue(); ++n11) {
//                    final M1054 m1054;
//                    if ((m1054 = ii.lt((int)entry3.getKey()).get(n11)) != null) {
//                        incMaxHPR += m1054.get("incMHPr");
//                        if (wuxing) {
//                            incMaxHPR += m1054.get("incMMPr");
//                        }
//                        else {
//                            incMaxMPR += m1054.get("incMMPr");
//                        }
//                        pad += m1054.get("incPAD");
//                        mad += m1054.get("incMAD");
//                        critRate += m1054.get("incCr");
//                        expMod += m1054.get("incEXPr");
//                        incDamR += m1054.get("incDAMr");
//                        addIgnoreMobpdpR(m1054.get("ignoreTargetDEF"));
//                    }
//                }
//            }

        int activeNickItemID = player.getActiveNickItemID();
        if (activeNickItemID > 0) {
            MapleInventory iv = player.getInventory(MapleInventoryType.SETUP);
            if (iv != null && iv.findById(activeNickItemID) != null) {
                if (!ii.isNickSkillTimeLimited(activeNickItemID) || iv.findById(activeNickItemID).getExpiration() > System.currentTimeMillis()) {
                    MapleStatEffect itemEffect = ii.getNickItemEffect(player.getActiveNickItemID());
                    if (itemEffect != null && itemEffect.getInfo() != null) {
                        addmaxhp += itemEffect.getInfo().getOrDefault(MapleStatInfo.mhpX, 0);
                        addmaxmp += itemEffect.getInfo().getOrDefault(MapleStatInfo.mmpX, 0);
                    }
                }
            }
        }
        player.getStat().handleProfessionTool(player);

        // 將神之子使用過幸運卷軸的武器添加到套裝
        if (JobConstants.is神之子(player.getJob())) {
            String data = player.getQuestNAdd(MapleQuest.getInstance(41907)).getCustomData();
            if (wt != MapleWeapon.沒有武器 && data != null && !data.equals("0")) {
                setHandling.computeIfAbsent(Integer.valueOf(data), k -> new ArrayList<>()).add(weaponId);
            }
        }
        // 將幸運道具添加到套裝
        Map<Integer, Integer> treeMap = new TreeMap<>((n1, n2) -> n2.compareTo(n1));
        for (Map.Entry<Integer, List<Integer>> entry : setHandling.entrySet()) {
            StructSetItem ssi = ii.getSetItem(entry.getKey());
            if (ssi != null) {
                int reqlevel = 0;
                for (int id : ssi.itemIDs) {
                    reqlevel = Math.max(ii.getReqLevel(id), reqlevel);
                }
                treeMap.put(entry.getKey(), reqlevel);
            }
        }
        List<Map.Entry<Integer, Integer>> list1 = new ArrayList<>(treeMap.entrySet());
        list1.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        for (int id : jokerToSetItems) {
            for (Map.Entry<Integer, Integer> entry : list1) {
                int setId = entry.getKey();
                List<Integer> list2 = new ArrayList<>(setHandling.get(setId));
                boolean b = false;
                if (list2.size() >= 3) {
                    for (int id3 : setHandling.get(setId)) {
                        StructSetItem ssi = ii.getSetItem(setId);
                        if (ssi != null && !b) {
                            for (int id4 : ssi.itemIDs) {
                                if (id3 != id4 && id3 != id && id4 / 10000 == id / 10000) {
                                    list2.add(id);
                                    b = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                setHandling.put(setId, list2);
            }
        }
        list1.clear();
        // 處理套裝屬性
        List<Integer> list2 = new ArrayList<>();
        for (Map.Entry<Integer, List<Integer>> setHandlingEntry : setHandling.entrySet()) {
            int setId = setHandlingEntry.getKey();
            List<Integer> itemids = setHandlingEntry.getValue();
            StructSetItem ssi = ii.getSetItem(setId);
            if (ssi == null) continue;
            if (itemids.size() >= ssi.completeCount) {
                list2.add(setId);
            }
            for (Map.Entry<Integer, StructSetItemStat> entry : new LinkedHashMap<>(ssi.setItemStat).entrySet()) {
                StructSetItemStat stat = entry.getValue();
                if (entry.getKey() <= itemids.size()) {
                    localstr += stat.incSTR + stat.incAllStat;
                    localdex += stat.incDEX + stat.incAllStat;
                    localint += stat.incINT + stat.incAllStat;
                    localluk += stat.incLUK + stat.incAllStat;
                    pad += stat.incPAD;
                    mad += stat.incMAD;
                    speed += stat.incSpeed;
                    localmaxhp_ += stat.incMHP;
                    incMaxHPR += stat.incMHPr;
                    if (wuxing) {
                        localmaxhp_ += stat.incMMP;
                        incMaxHPR += stat.incMMPr;
                    } else {
                        localmaxmp_ += stat.incMMP;
                        incMaxMPR += stat.incMMPr;
                    }
                    wdef += stat.incPDD;
                    if (stat.skillId > 0 && stat.skillLevel > 0) {
                        sData.put(stat.skillId, new SkillEntry(stat.skillLevel, (byte) 0, -1, 0, 0, (byte) -1));
                    }
                    if (stat.option1 > 0 && stat.option1Level > 0) {
                        soc = ii.getPotentialInfo(stat.option1).get(stat.option1Level);
                        if (soc != null) {
                            localmaxhp_ += soc.get("incMHP");
                            if (wuxing) {
                                localmaxhp_ += soc.get("incMMP");
                            } else {
                                localmaxmp_ += soc.get("incMMP");
                            }
                            handleItemOption(entry.getKey(), soc, player, firstLogin, sData);
                        }
                    }
                    if (stat.option2 > 0 && stat.option2Level > 0) {
                        soc = ii.getPotentialInfo(stat.option2).get(stat.option2Level);
                        if (soc != null) {
                            localmaxhp_ += soc.get("incMHP");
                            if (wuxing) {
                                localmaxhp_ += soc.get("incMMP");
                            } else {
                                localmaxmp_ += soc.get("incMMP");
                            }
                            handleItemOption(entry.getKey(), soc, player, firstLogin, sData);
                        }
                    }
                }
            }
        }
        list2.clear();

        int extraExpRate = 100;
        for (Item item : player.getInventory(MapleInventoryType.CASH).newList()) {
            if (item.getItemId() / 10000 == 521) {
                int rate = ii.getExpCardRate(item.getItemId());
                if (item.getItemId() != 5210009 && rate > 0) {
                    if ((item.getItemId() / 1000 != 5212 && !ii.isExpOrDropCardTime(item.getItemId())) || player.getLevel() < ii.getExpCardMinLevel(item.getItemId()) || player.getLevel() > ii.getExpCardMaxLevel(item.getItemId()) || item.getExpiration() <= System.currentTimeMillis()) {
                        if (item.getExpiration() == -1L) {
                            player.dropMessage(5, ii.getName(item.getItemId()) + "屬性錯誤，經驗值加成無效。");
                            if (!player.isIntern()) {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    }
                    switch (item.getItemId()) {
                        case 5212000: // 199等級以下 4hr 1回
                        case 5212003: // 199等級以下 1hr 1回
                        case 5212006: // 199等級以下 30min 1回
                            //--------------------------------
                        case 5212001: // 200~224等級 4hr 1回
                        case 5212004: // 200~224等級 1hr 1回
                        case 5212007: // 200~224等級 30min 1回
                            //--------------------------------
                        case 5212002: // 225~249等級 4hr 1回
                        case 5212005: // 225~249等級 1hr 1回
                        case 5212008: // 225~249等級 30min 1回
                            extraExpRate *= rate / 100.0;
                            break;
                        default:
                            if (expCardRate < rate) {
                                expCardRate = rate;
                            }
                    }
                }
            } else if (dropCardRate == 100 && item.getItemId() / 10000 == 536) {
                if (item.getItemId() >= 5360000 && item.getItemId() < 5360100) {
                    if (!ii.isExpOrDropCardTime(item.getItemId()) || item.getExpiration() <= System.currentTimeMillis()) {
                        if (item.getExpiration() == -1L) {
                            player.dropMessage(5, ii.getName(item.getItemId()) + "屬性錯誤，掉寶機率加成無效。");
                            if (!player.isIntern()) {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    }
                    dropCardRate = 200;
                }
            } else if (item.getItemId() == 5590001) { //高級裝備特許證 - 擁有裝備特許證後可以裝備比自己等級高#c10級#的裝備
                levelBonus = 10;
            } else if (levelBonus == 0 && item.getItemId() == 5590000) { //裝備特許證 - 擁有裝備特許證後可以裝備比自己等級高#c5級#的裝備
                levelBonus = 5;
            } else if (item.getItemId() == 5710000) {
                questBonus = 2;
            } else if (item.getItemId() == 5340000) { //釣竿 - 用來捕魚的重要裝備
                canFish = true;
            } else if (item.getItemId() == 5340001) { //高級魚竿 - 用更堅韌的材料製成的釣魚竿，可以加快釣魚的速度
                canFish = true;
                canFishVIP = true;
            }
        }
        expCardRate = Math.max(extraExpRate, expCardRate);
        for (Item item : player.getInventory(MapleInventoryType.ETC).list()) { //omfg;
            switch (item.getItemId()) { //暫時不開放以下功能
                case 4030003: //俄羅斯方塊
                    //pickupRange = Double.POSITIVE_INFINITY;
                    break;
                case 4030004: //俄羅斯方塊
                    //hasClone = true;
                    break;
                case 4030005: //俄羅斯方塊
                    //cashMod = 2;
                    break;
            }
        }
        if (firstLogin && player.getLevel() >= 30) {
            //大天使,黑天使,白色天使
            int[] skills = {1085, 1087, 1179};
            for (int skillId : skills) {
                for (int allJob : allJobs) {
                    if (JobConstants.getBeginner(player.getJob()) != allJob && player.getSkillEntry(skillId + allJob) != null) {
                        sData.put(skillId + allJob, new SkillEntry(0, 0, -1));
                    }
                }
                sData.put(SkillConstants.getSkillByJob(skillId, player.getJob()), new SkillEntry(-1, 0, -1));
            }
        }
        player.changeSkillLevel_Skip(sData, false);
    }

    @SuppressWarnings("unchecked")
    private Pair<Integer, Integer> handleEquipAdditions(MapleItemInformationProvider ii, MapleCharacter chra, boolean first_login, Map<Integer, SkillEntry> sData, int itemId) {
        Map<String, ?> additions = ii.getEquipAdditions(itemId);
        if (additions == null) {
            return null;
        }
        int localmaxhp_x = 0, localmaxmp_x = 0;
        int skillid = 0, skilllevel = 0;
        for (Map.Entry<String, ?> add : additions.entrySet()) {
//            int right = Integer.parseInt(ii.getEquipAdditionInfo(itemId, "elemboost/elemVol"));
            switch (add.getKey()) {
                case "elemboost": {
                    String craft = null, elemVol = null;
                    try {
                        Object craftObj = ((Map<?, ?>) ((Map<?, ?>) add.getValue()).get("con")).get("craft");
                        if (craftObj instanceof String) {
                            craft = (String) craftObj;
                        } else {
                            craft = (craftObj == null) ? null : craftObj.toString();
                        }
                        elemVol = (String) ((Map<?, ?>) add.getValue()).get("elemVol");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (elemVol != null && (craft == null || chra.getTrait(MapleTraitType.craft).getLocalTotalExp() >= Integer.parseInt(craft))) {
                        int value = Integer.parseInt(elemVol.substring(1));
                        Element key = Element.getFromChar(elemVol.charAt(0));
                        if (elemBoosts.get(key) != null) {
                            value += elemBoosts.get(key);
                        }
                        elemBoosts.put(key, value);
                    }
                    break;
                }
                case "mobcategory": {
                    Integer damage = (Integer) ((Map<?, ?>) add.getValue()).get("damage");
                    if (damage != null) {
                        incDamR += damage;
                    }
                    break;
                }
                case "critical":
                    boolean canJob = true, canLevel = true;
                    int prob = 0, damage = 0;
                    if (add.getValue() instanceof Map<?, ?>) {
                        for (Map.Entry<String, ?> entry : ((Map<String, ?>) add.getValue()).entrySet()) {
                            switch (entry.getKey()) {
                                case "con":
                                    Map<?, ?> subentry = (Map<?, ?>) entry.getValue();
                                    if (subentry.containsKey("job")) {
                                        canJob = subentry.containsValue(chra.getJob());
                                    } else if (subentry.containsKey("lv")) {
                                        canLevel = chra.getLevel() >= (Integer) subentry.get("lv");
                                    }
                                    break;
                                case "prob":
                                    prob = (Integer) entry.getValue();
                                    break;
                                case "damage":
                                    try {
                                        damage = Integer.parseInt(entry.getValue().toString());
                                    } catch (ClassCastException e) {
                                        log.error("讀取damage錯誤, Itemid: " + itemId, e);
                                    }
                            }
                        }
                    }
                    if (canJob && canLevel) {
                        critRate += prob;
                        criticalDamage += damage;
                    }
                    break;
                case "boss":
                    // ignore prob, just add
//                    String craft = (String) ((Map<?, ?>) ((Map<?, ?>) add.getValue()).get("con")).get("craft");
//                    if (add.getMid().equals("damage") && (craft == null || chra.getTrait(MapleTraitType.craft).getLocalTotalExp() >= Integer.parseInt(craft))) {
//                        bossDamageR += right;
//                    }
                    break;
                case "mobdie":
                    // lv, hpIncRatioOnMobDie, hpRatioProp, mpIncRatioOnMobDie, mpRatioProp, modify =D, don't need mob to die
//                    craft = ii.getEquipAdditionInfo(itemId, add.getLeft(), "craft");
//                    if ((craft == null || chra.getTrait(MapleTraitType.craft).getLocalTotalExp() >= Integer.parseInt(craft))) {
//                        switch (add.getMid()) {
//                            case "hpIncOnMobDie":
//                                hpRecover += right;
//                                hpRecoverProp += 5;
//                                break;
//                            case "mpIncOnMobDie":
//                                mpRecover += right;
//                                mpRecoverProp += 5;
//                                break;
//                        }
//                    }
//                    break;
                case "skill":
                    // all these are additional skills
                    if (add.getValue() instanceof Map<?, ?>) {
                        try {
                            Map<String, ?> v = (Map<String, ?>) add.getValue();
                            if (((Map<?, ?>) add.getValue()).containsKey("con")) {
                                String craft = (String) ((Map<?, ?>) ((Map<?, ?>) add.getValue()).get("con")).get("craft");
                                if (chra.getTrait(MapleTraitType.craft).getLocalTotalExp() < Integer.parseInt(craft)) {
                                    continue;
                                }
                            }
                            if (((Map<?, ?>) add.getValue()).containsKey("id")) {
                                String id = (String) ((Map<?, ?>) add.getValue()).get("id");
                                skillid = Integer.parseInt(id);
                            }
                            if (((Map<?, ?>) add.getValue()).containsKey("level")) {
                                String level = (String) ((Map<?, ?>) add.getValue()).get("level");
                                skilllevel = Integer.parseInt(level);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    break;
                case "hpmpchange":
//                    switch (add.getMid()) {
//                        case "hpChangerPerTime":
//                            recoverHP += right;
//                            break;
//                        case "mpChangerPerTime":
//                            recoverMP += right;
//                            break;
//                    }
//                    break;
                case "statinc":
//                    boolean canJobx = false,
//                            canLevelx = false;
//                    job = ii.getEquipAdditionInfo(itemId, add.getLeft(), "job");
//                    if (job != null) {
//                        if (job.contains(",")) {
//                            String[] jobs = job.split(",");
//                            for (String x : jobs) {
//                                if (chra.getJob() == Integer.parseInt(x)) {
//                                    canJobx = true;
//                                }
//                            }
//                        } else if (chra.getJob() == Integer.parseInt(job)) {
//                            canJobx = true;
//                        }
//                    }
//                    level = ii.getEquipAdditionInfo(itemId, add.getLeft(), "level");
//                    if (level != null && chra.getLevel() >= Integer.parseInt(level)) {
//                        canLevelx = true;
//                    }
//                    if ((!canJobx && job != null) || (!canLevelx && level != null)) {
//                        continue;
//                    }
//                    if (itemId == 1142367) { //1142367 - 巧克力棒週末特別勳章 - (無描述)
//                        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
//                        if (day != 1 && day != 7) {
//                            continue;
//                        }
//                    }
//                    switch (add.getMid()) {
//                        case "incPAD":
//                            watk += right;
//                            break;
//                        case "incMAD":
//                            magic += right;
//                            break;
//                        case "incSTR":
//                            localstr += right;
//                            break;
//                        case "incDEX":
//                            localdex += right;
//                            break;
//                        case "incINT":
//                            localint_ += right;
//                            break;
//                        case "incLUK":
//                            localluk += right;
//                            break;
//                        case "incJump":
//                            jump += right;
//                            break;
//                        case "incMHP":
//                            localmaxhp_x += right;
//                            break;
//                        case "incMMP":
//                            localmaxmp_x += right;
//                            break;
//                        case "incPDD":
//                            wdef += right;
//                            break;
//                        case "incMDD":
//                            mdef += right;
//                            break;
//                        case "incACC":
//                            accuracy += right;
//                            break;
//                        case "incEVA":
//                            break;
//                        case "incSpeed":
//                            speed += right;
//                            break;
//                        case "incMMPr":
//                            percent_mp += right;
//                            break;
//                    }
                    break;
            }
        }
        if (skillid != 0 && skilllevel != 0) {
            sData.put(skillid, new SkillEntry((byte) skilllevel, (byte) 0, -1));
        }
        return new Pair<>(localmaxhp_x, localmaxmp_x);
    }

    /* twms By Hertz -[ Horus.荷魯斯 Code AI GPT - 4.0] 2024/3/30 02:40 */
    private void handleItemOption(int sourceid, StructItemOption soc, MapleCharacter chra, boolean first_login, Map<Integer, SkillEntry> sData) {
        localstr += soc.get("incSTR");
        localdex += soc.get("incDEX");
        localint += soc.get("incINT");
        localluk += soc.get("incLUK");
        if (soc.get("incSTRlv") > 0) { /* 259.5 Ida8.3 [Horus.荷魯斯插件.plugin] sub_145B5A3C0 */
            localstr += (chra.getLevel() / 10) * soc.get("incSTRlv");
        }
        if (soc.get("incDEXlv") > 0) {
            localdex += (chra.getLevel() / 10) * soc.get("incDEXlv");
        }
        if (soc.get("incINTlv") > 0) {
            localint += (chra.getLevel() / 10) * soc.get("incINTlv");
        }
        if (soc.get("incLUKlv") > 0) {
            localluk += (chra.getLevel() / 10) * soc.get("incLUKlv");
        }
        speed += soc.get("incSpeed");
        jump += soc.get("incJump");
        pad += soc.get("incPAD");
        if (soc.get("incPADlv") > 0) {
            pad += (chra.getLevel() / 10) * soc.get("incPADlv");
        }
        mad += soc.get("incMAD");
        if (soc.get("incMADlv") > 0) {
            mad += (chra.getLevel() / 10) * soc.get("incMADlv");
        }
        wdef += soc.get("incPDD");
        incStrR += soc.get("incSTRr");
        incDexR += soc.get("incDEXr");
        incIntR += soc.get("incINTr");
        incLukR += soc.get("incLUKr");
        incMaxHPR += soc.get("incMHPr");
        incMaxMPR += soc.get("incMMPr");
        incPadR += soc.get("incPADr");
        incMadR += soc.get("incMADr");
        percent_wdef += soc.get("incPDDr");
        critRate += soc.get("incCr");
        if (soc.get("boss") > 0) {
            bossDamageR += soc.get("incDAMr");
        } else {
            incDamR += soc.get("incDAMr");
        }
        recoverHP += soc.get("RecoveryHP"); // This shouldn't be here, set 4 seconds.
        recoverMP += soc.get("RecoveryMP"); // This shouldn't be here, set 4 seconds.
        if (soc.get("HP") > 0) { // Should be heal upon attacking
            hpRecover_itemOption.put(sourceid, new Pair<>(soc.get("HP"), soc.get("prop")));
        }
        if (soc.get("MP") > 0 && !JobConstants.isNotMpJob(chra.getJob())) {
            mpRecover_itemOption.put(sourceid, new Pair<>(soc.get("MP"), soc.get("prop")));
        }
        addIgnoreMobpdpR(soc.get("ignoreTargetDEF"));
        if (soc.get("ignoreDAM") > 0) {
            ignoreDAM.put(sourceid, new Pair<>(soc.get("ignoreDAM"), soc.get("prop")));
        }
        incAllskill += soc.get("incAllskill");
        if (soc.get("ignoreDAMr") > 0) {
            ignoreDAMr.put(sourceid, new Pair<>(soc.get("ignoreDAMr"), soc.get("prop")));
        }
        skillRecoveryUP += soc.get("RecoveryUP"); // only for hp items and skills
        itemRecoveryUP += soc.get("RecoveryUP");
        criticalDamage += soc.get("incCriticaldamageMin");
        criticalDamage += soc.get("incCriticaldamageMax");
        terR += soc.get("incTerR"); // elemental resistance = avoid element damage from monster
        asr += soc.get("incAsrR"); // abnormal status = disease
        if (soc.get("DAMreflect") > 0) {
            DAMreflect.put(sourceid, new Pair<>(soc.get("DAMreflect"), soc.get("prop")));
        }
        mpconReduce += soc.get("mpconReduce");
        reduceCooltime += soc.get("reduceCooltime"); // in seconds
        incMesoProp += soc.get("incMesoProp"); // 楓幣掉率 + %
        incRewardProp += soc.get("incRewardProp") / 100.0; // 道具掉落 + %先不計算百分百的掉落幾率 最後計算
        passivePlus += soc.get("passivePlus");
        incBuffTime += soc.get("bufftimeR");
        if (soc.get("skillID") > 0) {
            sData.put(SkillConstants.getSkillByJob(soc.get("skillID"), chra.getJob()), new SkillEntry((byte) 1, (byte) 0, -1));
        }
    }

    private void addIgnoreMobpdpR(final int val) {
        ignoreMobpdpR += (100.0 - ignoreMobpdpR) * (val / 100.0);
    }
}
