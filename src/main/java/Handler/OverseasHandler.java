package Handler;

import Client.*;
import Client.force.MapleForceFactory;
import Client.inventory.*;
import Client.skills.Skill;
import Client.skills.SkillFactory;
import Config.configs.ServerConfig;
import Config.constants.GameConstants;
import Config.constants.ItemConstants;
import Config.constants.skills.墨玄;
import Net.server.MapleInventoryManipulator;
import Net.server.MapleItemInformationProvider;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import Net.server.movement.LifeMovementFragment;
import Packet.BuffPacket;
import Packet.ForcePacket;
import Server.channel.handler.MovementParse;
import connection.InPacket;
import connection.packet.OverseasPacket;
import SwordieX.overseas.extraequip.ExtraEquipMagic;
import SwordieX.overseas.extraequip.ExtraEquipResult;
import SwordieX.util.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Randomizer;
import tools.data.MaplePacketReader;

import java.awt.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static Opcode.Headler.InHeader.*;

public class OverseasHandler {

    private static final Logger log = LoggerFactory.getLogger(OverseasHandler.class);

    @Handler(op = CP_EXTRA_EQUIP_REQUEST)
    public static void extraEquipRequest(MapleClient c, InPacket inPacket) {
        MapleCharacter player = c.getPlayer();
        if (player == null || player.getMap() == null) {
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        int resultType = inPacket.decodeByte();
        int ADD_V261_UNK = inPacket.decodeByte();
        int nPacketSize = inPacket.decodeShort();
        int useType = inPacket.decodeInt();
        ExtraEquipMagic spType = ExtraEquipMagic.NONE;
        for (ExtraEquipMagic sType : ExtraEquipMagic.values()) {
            if (sType.ordinal() == useType) {
                spType = sType;
                break;
            }
        }
        switch (spType) {
            case NONE:
            case SKILL_INNER_STORM:
            default: {
                if (player.isGm()) {
                    player.dropDebugMessage(2, "[SpecialOperation] 操作錯誤" + spType.name() + "(" + useType + ")");
                }
                break;
            }
            case SKILL_INNER_GLARE: {
                short mode = inPacket.decodeShort();
                if (mode == 21) { // 內面耀光 Buff變動
                    int size = inPacket.decodeReversedVarints();
                    List<Integer> skillIds = new ArrayList();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < size; i++) {
                        int skillId = inPacket.decodeInt();
                        if (player.getSkillLevel(skillId) > 0) {
                            skillIds.add(skillId);
                            sb.append(skillId).append(",");
                        }
                    }
                    if (sb.length() > 0) {
                        player.setKeyValue("InnerGlareBuffs", sb.substring(0, sb.length() - 1));
                    }
                    c.write(OverseasPacket.extraEquipResult(ExtraEquipResult.updateInnerGlareSkills(player.getId(), skillIds)));
                }
                break;
            }
            case FAMILIAR_CARDS: {
                short mode = inPacket.decodeShort();
                switch (mode) {
                    case 17: { // 召喚萌獸
                        int familiarSN = inPacket.decodeInt();
                        MonsterFamiliar mf = player.getFamiliars().stream().filter(m -> m.getId() == familiarSN).findAny().orElse(null);
                        if (mf != null) {
                            player.spawnFamiliar(mf);
                            player.setFamiliarsChanged(true);
                        }
                        break;
                    }
                    case 18: { // 收回萌獸
                        player.removeFamiliar();
                        player.setFamiliarsChanged(true);
                        break;
                    }
                    case 19: { // 使用萌獸卡
                        short slot = inPacket.decodeShort();
                        Item item = player.getInventory(MapleInventoryType.USE).getItem(slot);
                        if (item == null) {
                            break;
                        }
                        if (item.getQuantity() < 1 || item.getItemId() / 10000 != 287) {
                            c.sendEnableActions();
                            return;
                        }
                        int familiarID = ItemConstants.getFamiliarByItemID(item.getItemId());
                        if (familiarID == 0) {
                            player.dropMessage(1, "這個萌獸卡無法使用。");
                            c.sendEnableActions();
                            return;
                        }
                        if (player.getFamiliars().size() >= 60) {
                            player.dropMessage(1, "萌獸圖鑒數量已經達到最大值!");
                            c.sendEnableActions();
                            return;
                        }
                        if (item.getFamiliarCard() == null) {
                            item.setFamiliarCard(new FamiliarCard((byte) 0));
                        }
                        if (MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false, false)) {
                            MonsterFamiliar mf = new MonsterFamiliar(c.getAccID(), player.getId(), familiarID, item.getFamiliarCard());
                            mf.addFlag(1);
                            player.addFamiliarsInfo(mf);
                            player.updateFamiliar(mf);
                        }
                        break;
                    }
                    case 22: { // 萌獸升級
                        int familiarSN = (int) inPacket.decodeLong();
                        Map<Integer, Integer> hashMap = new HashMap<>();
                        MonsterFamiliar familiar = player.getFamiliars().stream().filter(m -> m.getId() == familiarSN).findAny().orElse(null);
                        if (familiar == null) {
                            break;
                        }
                        short exp = 0;
                        int fee = 0;
                        int[] C_upgradeR = new int[]{30, 35, 40, 50, 60};
                        int[] B_upgradeR = new int[]{20, 30, 35, 40, 50};
                        int[] A_upgradeR = new int[]{5, 10, 15, 20, 30};
                        int[] S_upgradeR = new int[]{2, 5, 10, 15, 20};
                        int[] SS_upgradeR = new int[]{1, 2, 5, 10, 15};
                        int nCount = inPacket.decodeReversedVarints();
                        int[] upgradeR;
                        for (byte i = 0; i < nCount; ++i) {
                            int selectSN = (int) inPacket.decodeLong();
                            MonsterFamiliar mf = player.getFamiliars().stream().filter(m -> m.getId() == familiarSN).findAny().orElse(null);
                            if (mf == null || mf.isLock()) {
                                return;
                            }
                            switch (familiar.getGrade()) {
                                case 1:
                                    upgradeR = B_upgradeR;
                                    break;
                                case 2:
                                    upgradeR = A_upgradeR;
                                    break;
                                case 3:
                                    upgradeR = S_upgradeR;
                                    break;
                                case 4:
                                    upgradeR = SS_upgradeR;
                                    break;
                                case 0:
                                default:
                                    upgradeR = C_upgradeR;
                                    break;
                            }
                            short shortValue = (short) (ii.getFamiliarTable_rchance().get(mf.getGrade()).get(familiar.getGrade()) + 1);
                            short addExp = (short) (Randomizer.nextInt(100) < upgradeR[mf.getGrade()] ? 100 : (Randomizer.nextInt(100) < shortValue * 10) ? (shortValue * 10) : shortValue);
                            exp += addExp;
                            hashMap.put(selectSN, (int) addExp);
                            fee += 50000 * (familiar.getGrade() + 1);
                            if ((5 - familiar.getLevel()) * 100 <= exp) {
                                break;
                            }
                        }
                        if (player.getMeso() >= fee) {
                            for (Integer integer : hashMap.keySet()) {
                                player.removeFamiliarsInfo(integer);
                            }
                            familiar.gainExp(exp);
                            player.gainMeso(-fee, false);
                            c.write(OverseasPacket.extraEquipResult(ExtraEquipResult.familiarGainExp(player.getId(), hashMap)));
                            player.updateFamiliars();
                        }
                        break;
                    }
                    case 24: { // 萌獸進化
                        int base = (int) inPacket.decodeLong();
                        int material = (int) inPacket.decodeLong();
                        if (player.getFamiliars().stream().noneMatch(mf -> mf.getId() == base || mf.getId() == material)) {
                            break;
                        }
                        MonsterFamiliar baseObject = player.getFamiliars().stream().filter(mf -> mf.getId() == base).findAny().orElse(null);
                        MonsterFamiliar materialObject = player.getFamiliars().stream().filter(mf -> mf.getId() == material).findAny().orElse(null);
                        if (baseObject == null || materialObject == null || baseObject.isLock() || materialObject.isLock()) {
                            c.sendEnableActions();
                            return;
                        }
                        int cost = (50000 * (baseObject.getGrade() + 1) << 1);
                        if (baseObject.getGrade() == materialObject.getGrade() && materialObject.getLevel() == 5 && player.getMeso() >= cost) {
                            baseObject.setGrade((byte) (baseObject.getGrade() + 1));
                            baseObject.setLevel((byte) 1);
                            if (baseObject.getGrade() >= 4) {
                                baseObject.setGrade((byte) 4);
                            }
                            player.removeFamiliarsInfo(material);
                            player.gainMeso(-cost, true);
                            c.write(OverseasPacket.extraEquipResult(ExtraEquipResult.upgradeFamiliar(player.getId())));
                            player.updateFamiliars();
                        }
                        break;
                    }
                    case 27: { // 萌獸命名
                        int familiarSN = (int) inPacket.decodeLong();
                        MonsterFamiliar familiar = player.getFamiliars().stream().filter(mf -> mf.getId() == familiarSN).findAny().orElse(null);
                        if (familiar != null) {
                            int size = inPacket.decodeReversedVarints();
                            if (size < 4 || size > 13) {
                                player.dropMessage(1, "不適用於萌獸名稱的長度。");
                                return;
                            }
                            familiar.setName(inPacket.decodeString(size));
                            player.updateFamiliar(familiar);
                        }
                        break;
                    }
                    case 28: { // 放生萌獸
                        int familiarSN = (int) inPacket.decodeLong();
                        MonsterFamiliar familiar = player.getFamiliars().stream().filter(mf -> mf.getId() == familiarSN).findAny().orElse(null);
                        if (familiar != null && !familiar.isLock()) {
                            player.removeFamiliarsInfo(familiarSN);
                            player.updateFamiliars();
                        }
                        break;
                    }
                    case 29: { // 封印萌獸
                        int familiarSN = (int) inPacket.decodeLong();
                        MonsterFamiliar familiar = player.getFamiliars().stream().filter(m -> m.getId() == familiarSN).findAny().orElse(null);
                        int price = ServerConfig.FAMILIAR_SEAL_COST;
                        if (familiar == null || price <= 0) {
                            player.dropAlertNotice("未知錯誤1");
                            break;
                        }
                        if (familiar.isLock()) {
                            player.dropAlertNotice("鎖定狀態下不可使用。");
                            break;
                        }
                        if (player.getCSPoints(2) < price) {
                            player.dropAlertNotice("沒有足夠的楓點!");
                            break;
                        }
                        if (player.getSpace(2) < 1) {
                            player.dropAlertNotice("消耗欄空間不足!");
                            break;
                        }
                        player.modifyCSPoints(2, -price);
                        if (player.removeFamiliarsInfo(familiarSN) != null) {
                            int monsterCardID = ii.getFamiliar(familiar.getFamiliar()).getMonsterCardID();
                            Item card = new Item(monsterCardID, (byte) 0, (short) 1);
                            card.setFamiliarCard(familiar.createFamiliarCard());
                            MapleInventoryManipulator.addbyItem(c, card, false);
                            player.updateFamiliars();
                        }
                        break;
                    }
                    case 30: { // 使用萌獸方塊
                        short slot = inPacket.decodeShort();
                        inPacket.decodeArr(2);
                        int index = inPacket.decodeInt();
                        Item item = player.getInventory(MapleInventoryType.CASH).getItem(slot);
                        MonsterFamiliar mf = player.getFamiliars().stream().filter(m -> m.getId() == index).findAny().orElse(null);
                        if (item == null || item.getItemId() != 5743003 || mf == null || mf.isLock()) {
                            if (mf != null && mf.isLock())
                                player.dropMessage(1, "鎖定狀態下不可使用。");
                            c.sendEnableActions();
                            return;
                        }
                        player.getTempValues().put("resetOptionsFamiliar", mf);
                        mf.initOptions();
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, slot, (short) 1, false, false);
                        player.updateFamiliars();
                        break;
                    }
                    case 31: {
                        List<MonsterFamiliar> familiars = player.getFamiliars();
                        Map<Integer, MonsterFamiliar> fMap = familiars.stream().filter(fm -> fm.hasFlag(1)).collect(Collectors.toMap(familiars::indexOf, Function.identity()));
                        fMap.values().forEach(fm -> fm.removeFlag(1));
                        c.write(OverseasPacket.extraEquipResult(ExtraEquipResult.updateFamiliarInfo(player.getId(), familiars.size(), fMap)));
                        break;
                    }
                    case 32: { // 選擇萌獸組合技能
                        int selected = inPacket.decodeInt();
                        if (!player.setSelectedFamiliarTeamStat(selected)) {
                            return;
                        }
                        c.write(OverseasPacket.extraEquipResult(ExtraEquipResult.changeTeamStatSelected(player, selected)));
                        break;
                    }
                    case 33: { // 裝備萌獸組合技能
                        int optionIndex = inPacket.decodeInt();
                        if (!player.changeFamiliarTeamStat(optionIndex)) {
                            return;
                        }
                        c.write(OverseasPacket.extraEquipResult(ExtraEquipResult.changeTeamStats(player, player.getSelectedFamiliarTeamStat() - 1, player.getFamiliarTeamStats())));
                        break;
                    }
                    case 40: { // 萌獸 鎖定/解鎖 操作
                        String secondPw = inPacket.decodeString(inPacket.decodeReversedVarints());
                        int familiarSN = inPacket.decodeInt();
                        boolean lock = inPacket.decodeBoolean();
                        if (c.CheckSecondPassword(secondPw)) {
                            MonsterFamiliar mf = player.getFamiliars().stream().filter(m -> m.getId() == familiarSN).findAny().orElse(null);
                            if (mf == null) {
                                c.sendEnableActions();
                                return;
                            }
                            mf.setLock(lock);
                            player.updateFamiliars();
                            c.write(OverseasPacket.extraEquipResult(ExtraEquipResult.familiarLock(player.getId(), true, familiarSN, lock)));
                        } else
                            c.write(OverseasPacket.extraEquipResult(ExtraEquipResult.familiarLock(player.getId(), false, familiarSN, lock)));
                        break;
                    }
                }
            }
            case FAMILIAR_LIFE: {
                int mode = inPacket.readerIndex();
                switch (mode) {
                    case 18: { // 萌獸移動
                        inPacket.decodeReversedVarints(); // PacketSize
                        final int gatherDuration = inPacket.decodeInt();
                        final int nVal1 = inPacket.decodeInt();
                        Position position = inPacket.decodePosition();
                        final Point oPos = new Point(position.getX(), position.getY());
                        position = inPacket.decodePosition();
                        final Point mPos = new Point(position.getX(), position.getY());
                        try {
                            List<LifeMovementFragment> res = MovementParse.parseMovement(inPacket.toPacketReader(), 6);
                            if (res == null) {
                                log.error("ParseMovement Null - Familiar Card");
                                if (player.isDebug()) {
                                    player.dropMessage(-1, "萌獸移動包出錯 - ParseMovement Null");
                                }
                            } else if (player.getSummonedFamiliar() != null && res.size() > 0) {
                                MovementParse.updatePosition(res, player.getSummonedFamiliar(), 0);
                                byte[] packet = OverseasPacket.extraEquipResult(ExtraEquipResult.familiarMove(player.getId(), gatherDuration, nVal1, oPos, mPos, res)).getData();
                                player.getMap().objectMove(player.getId(), player.getSummonedFamiliar(), packet);
                            }
                        } catch (Exception e) {
                            log.error("ParseMovement Error - Familiar Card", e);
                            if (player.isDebug()) {
                                player.dropMessage(-1, "萌獸移動包出錯 - ParseMovement Error");
                            }
                        }
                        break;
                    }
                    case 19: { // 萌獸攻擊
                        int anInt = inPacket.decodeInt();
                        int nCount = inPacket.decodeReversedVarints();
                        MonsterFamiliar summonedFamiliar = player.getSummonedFamiliar();
                        Map<Integer, List<Integer>> hashMap = new HashMap<>();
                        if (summonedFamiliar != null) {
                            for (byte i = 0; i < nCount && i < 8; ++i) {
                                int oid = inPacket.decodeInt();
                                MapleMonster mob;
                                if ((mob = player.getMap().getMobObject(oid)) != null && mob.isAlive()) {
                                    hashMap.put(oid, Collections.singletonList((int) Math.min(player.getCalcDamage().getRandomDamage(player, true) * summonedFamiliar.getPad() * (Math.max(0.1, 100 - mob.getStats().getPDRate()) / 100.0), 9.9999999E7)));
                                }
                            }
                            inPacket.decodeByte();
                            player.getMap().broadcastMessage(player, OverseasPacket.extraEquipResult(ExtraEquipResult.familiarAttack(player.getId(), anInt, hashMap)).getData(), true);
                            for (final Map.Entry<Integer, List<Integer>> entry : hashMap.entrySet()) {
                                MapleMonster mob;
                                if ((mob = player.getMap().getMobObject(entry.getKey())) != null) {
                                    mob.damage(player, 0, (long) entry.getValue().get(0), false);
                                }
                            }
                        }
                        break;
                    }
                }
                break;
            }
        }
    }

    @Handler(op = TMSExtraSystemRequest)
    public static void tmsExtraSystemRequest(MapleClient c, InPacket inPacket) {
        int magicNumber = inPacket.decodeInt();
        short type1 = inPacket.decodeShort();
        switch (type1) {
            case 1: {
                byte type2 = inPacket.decodeByte();
                if (type2 == 7) { // 丟棄道具
                    String secondPwd = inPacket.decodeString();
                    byte iv = inPacket.decodeByte();
                    byte slot = inPacket.decodeByte();
                    if (!c.CheckSecondPassword(secondPwd)) {
                        c.getPlayer().dropMessage(1, "第二組密碼不正確");
                        c.sendEnableActions();
                        return;
                    }
                    Item item = c.getPlayer().getInventory(iv).getItem(slot);
                    if (item != null) {
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.getByType(iv), slot, item.getQuantity(), true, false);
                        c.getPlayer().dropMessage(1, "你丟掉了" + item.getName() + "。");
                    } else {
                        c.getPlayer().dropMessage(1, "發生未知錯誤");
                    }
                    c.sendEnableActions();
                }
                break;
            }
            case 2: {
                byte type2 = inPacket.decodeByte();
                if (type2 == 3) {// 交換(寵物硬幣)
                    byte iv = inPacket.decodeByte();
                    byte slot = inPacket.decodeByte();
                    Item item = c.getPlayer().getInventory(iv).getItem(slot);
                    if (item == null || !ItemConstants.類型.寵物(item.getItemId()) || !ItemAttribute.RegressScroll.check(item.getCAttribute())) {
                        c.getPlayer().dropMessage(1, "發生未知錯誤");
                    } else if (MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.getByType(iv), slot, item.getQuantity(), true, false) && MapleInventoryManipulator.addById(c, 2434162, 1, "用寵物交換")) {
                        c.getPlayer().dropMessage(1, "獲得寵物硬幣。");
                    } else {
                        c.getPlayer().dropMessage(1, "發生未知錯誤");
                    }
                    c.sendEnableActions();
                }
                break;
            }
            case 26: {
                int eqpPos = inPacket.decodeShort();
                int usePos = inPacket.decodeShort();
                byte type2 = inPacket.decodeByte();
                if (type2 == 1) {// 結合方塊隨機潛能
                    if (eqpPos <= 0) {
                        c.sendEnableActions();
                        return;
                    }
                    final Equip eq = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short) eqpPos);
                    final Item toUse = c.getPlayer().getInventory(MapleInventoryType.CASH).getItem((short) usePos);
                    if (eq == null || toUse == null || toUse.getQuantity() < 1 || eq.getState(false) < 17 || EnhanceResultType.EQUIP_MARK.check(eq.getEnchantBuff())) {
                        c.sendEnableActions();
                        return;
                    }
                    if (toUse.getItemId() != 5062026) { // 結合方塊
                        if (c.getPlayer().isAdmin()) {
                            c.getPlayer().dropDebugMessage(2, "[使用方塊] 此方塊未處理");
                        }
                        c.sendEnableActions();
                        return;
                    }
                    c.getPlayer().updateOneQuestInfo(65132, "n", String.valueOf(eq.getSN()));
                    int line = Randomizer.rand(0, eq.getPotential(3, false) > 0 ? 2 : 1);
                    c.getPlayer().updateOneQuestInfo(65132, "i", String.valueOf(line));
                    c.write(OverseasPacket.getUniCubeRes(inPacket.decodeShort(), inPacket.decodeInt(), line));
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, toUse.getPosition(), (short) 1, false);
                }
                break;
            }
            case 27: {
                byte type2 = inPacket.decodeByte();
                inPacket.decodeByte();
                switch (type2) {
                    case 1: // 進行結合方塊
                        int line = Integer.parseInt(c.getPlayer().getOneInfo(65132, "i"));
                        long sn = Long.parseLong(c.getPlayer().getOneInfo(65132, "n"));
                        Equip eq = null;
                        for (Item it : c.getPlayer().getInventory(MapleInventoryType.EQUIP).list()) {
                            if (sn == it.getSN()) {
                                eq = (Equip) it;
                                break;
                            }
                        }
                        if (eq != null) {
                            eq.useCube(5062026, c.getPlayer(), line + 1);
                        }
                    case 0: // 取消結合方塊
                        c.write(OverseasPacket.getUniCubeRes(inPacket.decodeShort(), inPacket.decodeInt(), 1));
                        c.getPlayer().removeInfoQuest(65132);
                        break;
                }
                break;
            }
            case 37: { // 墨玄 技能
                int skillId = inPacket.decodeInt();
                MapleCharacter chr = c.getPlayer();
                if (chr == null) {
                    break;
                }
                if (skillId == 墨玄.神功_移形換位_1 || skillId == 墨玄.絕技_無我之境) {
                    SecondaryStatValueHolder holder;
                    if ((holder = chr.getBuffStatValueHolder(SecondaryStat.IndieCooltimeReduce, skillId)) == null || holder.value < 1) {
                        chr.dispelBuff(skillId);
                        break;
                    }

                    if (--holder.value < 1) {
                        chr.dispelBuff(skillId);
                    } else {
                        chr.send(BuffPacket.giveBuff(chr, holder.effect, Collections.singletonMap(SecondaryStat.IndieCooltimeReduce, holder.sourceID)));
                    }

                    Skill skill = null;
                    if (skillId == 墨玄.神功_移形換位_1) {
                        skill = SkillFactory.getSkill(墨玄.神功_移形換位_2);
                    } else {
                        skill = SkillFactory.getSkill(墨玄.絕技_無我之境_1);
                    }

                    MapleStatEffect attackEffect;
                    if (skill == null || (attackEffect = chr.getSkillEffect(skill.getId())) == null) {
                        break;
                    }
                    final MapleForceFactory mff = MapleForceFactory.getInstance();
                    chr.getMap().broadcastMessage(chr, ForcePacket.forceAtomCreate(mff.getMapleForce(chr, attackEffect, 0, 0, Collections.emptyList(), chr.getPosition())), true);
                }
                break;
            }
            case 38: { // 墨玄 連擊重置/武神力量減少
                int powerType = inPacket.decodeInt();
                if (powerType == 0) {
                    // c.write(OverseasPacket.extraSystemResult(ExtraSystemResult.mukhyunPower(powerType, 0, 3000, 1, 0)));
                } else {
                    int godPower = 0;
                    int time = 30000;
                    if (c.getPlayer().getSkillLevel(墨玄.墨玄_三轉_入神) > 0) {
                        godPower = (int) c.getPlayer().getTempValues().getOrDefault("GodPower", 0);
                        godPower = Math.max(0, --godPower);
                        c.getPlayer().getTempValues().put("GodPower", godPower);
                        if (c.getPlayer().getSkillLevel(墨玄.墨玄_超技能_神力_時間持續) > 0) {
                            time += 10000;
                        }
                    }
                    //c.write(OverseasPacket.extraSystemResult(ExtraSystemResult.mukhyunPower(powerType, godPower, time, 0, 0)));
                }
                break;
            }
            case 39: { // 墨玄 神功指令鍵
                MapleCharacter chr = c.getPlayer();
                if (chr == null) {
                    break;
                }
                int godSkillMacroLine = inPacket.decodeInt();
                int godSkillMacroNumber = inPacket.decodeInt();
                int godSkillMacroSkillType = inPacket.decodeInt();
                if (godSkillMacroLine < 1 || godSkillMacroLine > 6 || godSkillMacroNumber < 1 || godSkillMacroNumber > 4 || godSkillMacroSkillType < 0 || godSkillMacroSkillType > 2) {
                    break;
                }
                chr.updateOneInfo(65899, String.format("%d-%d", godSkillMacroLine, godSkillMacroNumber), godSkillMacroSkillType == 0 ? null : String.valueOf(godSkillMacroSkillType));
                break;
            }
        }
    }
    @Handler(op = TMSEquipmentEnchantRequest)
    public static void tmsSpecialRequest(MapleClient c, InPacket inPacket) {
        int magicNumber = inPacket.decodeInt();
        short type1 = inPacket.decodeShort();
        byte type2 = inPacket.decodeByte();
        switch (type1) {
            case 0: {
                switch (type2) {
                    case 1: {
                        // 楓方塊-設置道具
                        int src = inPacket.decodeInt();
                        inPacket.decodeByte(); // [01]
                        final Equip eq = (Equip) c.getPlayer().getInventory(src > 0 ? MapleInventoryType.EQUIP : MapleInventoryType.EQUIPPED).getItem((short) src);
                        if (EnhanceResultType.EQUIP_MARK.check(eq.getEnchantBuff())) {
                            c.sendEnableActions();
                            return;
                        }
                        short opcode = inPacket.decodeShort();
                        inPacket.decodeByte(); // [00]
                        inPacket.decodeByte(); // [00]
                        inPacket.decodeByte(); // [00]
                        showAnimaCubeCost(opcode, inPacket.decodeByte(), c, eq);
                        break;
                    }
                    case 3: {
                        if (inPacket.getUnreadAmount() <= 11) { // 方塊-洗潛能
                            final short slot = inPacket.decodeShort();
                            final short dst = inPacket.decodeShort();
                            final Item toUse = c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(slot);
                            final Equip eq = (Equip) c.getPlayer().getInventory(dst > 0 ? MapleInventoryType.EQUIP : MapleInventoryType.EQUIPPED).getItem(dst);
                            if (toUse.getItemId() != 5062017 && toUse.getItemId() != 5062019 && toUse.getItemId() != 5062020 && toUse.getItemId() != 5062021 || eq == null) { // 閃耀方塊、閃耀鏡射方塊、閃炫方塊、新對等方塊
                                if (c.getPlayer().isAdmin()) {
                                    c.getPlayer().dropDebugMessage(2, "[使用方塊] 此方塊未處理");
                                }
                                c.sendEnableActions();
                                return;
                            }
                            if (EnhanceResultType.EQUIP_MARK.check(eq.getEnchantBuff()) || eq.getState(false) < 17) {
                                c.sendEnableActions();
                                return;
                            }

                            inPacket.decodeByte(); // [01]

                            if (eq.useCube(inPacket.decodeShort(), inPacket.decodeInt(), toUse.getItemId(), c.getPlayer())) {
                                MapleInventoryManipulator.removeFromSlot(c.getPlayer().getClient(), MapleInventoryType.CASH, slot, (short) 1, false, true);
                            }
                        } else { // 神秘方塊-洗潛能
                            final short dst = (short) inPacket.decodeInt();
                            inPacket.decodeInt(); // [00 00 00 00]
                            inPacket.decodeByte(); // [01]
                            final Equip eq = (Equip) c.getPlayer().getInventory(dst > 0 ? MapleInventoryType.EQUIP : MapleInventoryType.EQUIPPED).getItem(dst);
                            if (EnhanceResultType.EQUIP_MARK.check(eq.getEnchantBuff())) {
                                c.sendEnableActions();
                                return;
                            }
                            final Item cube = c.getPlayer().getInventory(MapleInventoryType.SETUP).findById(3996222);
                            if (cube == null || eq == null) {
                                c.sendEnableActions();
                                return;
                            }
                            if (!useAnimaCube(inPacket.decodeShort(), inPacket.decodeInt(), c, eq)) {
                                c.sendEnableActions();
                            }
                        }
                        break;
                    }
                    case 0xF: { // 洗方塊-女神之力
                        final Item toUse;
                        final Equip eq;
                        if (inPacket.getUnreadAmount() == 19) {
                            final short slot = inPacket.decodeShort();
                            final short dst = inPacket.decodeShort();
                            toUse = c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(slot);
                            eq = (Equip) c.getPlayer().getInventory(dst > 0 ? MapleInventoryType.EQUIP : MapleInventoryType.EQUIPPED).getItem(dst);
                        } else {
                            final short dst = (short) inPacket.decodeLong();
                            toUse = c.getPlayer().getInventory(MapleInventoryType.SETUP).findById(3994895);
                            eq = (Equip) c.getPlayer().getInventory(dst > 0 ? MapleInventoryType.EQUIP : MapleInventoryType.EQUIPPED).getItem(dst);
                        }
                        if (eq == null || toUse == null || EnhanceResultType.EQUIP_MARK.check(eq.getEnchantBuff())) {
                            c.sendEnableActions();
                            return;
                        }
                        final int code = inPacket.decodeInt();
                        final short slot = (short) inPacket.decodeInt();
                        inPacket.decodeByte();

                        final Item rock = c.getPlayer().getInventory(MapleInventoryType.ETC).getItem(slot);
                        if (rock == null || rock.getQuantity() < 1) {
                            c.sendEnableActions();
                            return;
                        }
                        int rockId = rock.getItemId();
                        int toLock = 0;
                        boolean free = false;
                        if (rockId == 4132000) {
                            free = true;
                        } else {
                            toLock = code + 1;
                        }
                        boolean used = false;
                        if (toUse.getItemId() == 3994895) { // 楓方塊
                            used = useAnimaCube(inPacket.decodeShort(), inPacket.decodeInt(), c, eq, toLock, free);
                        } else if (toUse.getItemId() == 5062017) { // 閃耀方塊
                            if (eq.getState(false) < 17) {
                                c.sendEnableActions();
                                break;
                            }
                            used = eq.useCube(inPacket.decodeShort(), inPacket.decodeInt(), toUse.getItemId(), c.getPlayer(), toLock) && !free;
                            if (used) {
                                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, toUse.getPosition(), (short) 1, false);
                            }
                        }
                        if (used) {
                            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.ETC, slot, (short) 1, false);
                        } else {
                            c.sendEnableActions();
                        }
                        break;
                    }
                }
                break;
            }
            case 1: {
                switch (type2) {
                    case 1: { // 方塊-選擇潛能
                        int cubeId = Integer.parseInt(c.getPlayer().getOneInfo(GameConstants.台方塊, "u"));
                        if (cubeId == 5062017) { // 閃耀方塊
                            int selected = inPacket.decodeInt();
                            inPacket.decodeByte(); // 01
                            short opcode = inPacket.decodeShort();
                            int action = inPacket.decodeInt();
                            if (selected == 1) {
                                Equip eq = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(Short.valueOf(c.getPlayer().getOneInfo(GameConstants.台方塊, "p")));
                                if (eq.getItemId() != Integer.valueOf(c.getPlayer().getOneInfo(GameConstants.台方塊, "i"))) {
                                    OverseasPacket.getAnimusCubeRes(opcode, action, 1, cubeId);
                                    c.sendEnableActions();
                                    return;
                                }
                                if (EnhanceResultType.EQUIP_MARK.check(eq.getEnchantBuff())) {
                                    OverseasPacket.getAnimusCubeRes(opcode, action, 1, cubeId);
                                    c.sendEnableActions();
                                    return;
                                }
                                eq.setState(Byte.valueOf(c.getPlayer().getOneInfo(GameConstants.台方塊, "s")), false);
                                String[] s = c.getPlayer().getOneInfo(GameConstants.台方塊, "o").split(",");
                                eq.setPotential(Integer.valueOf(s[0]), 1, false);
                                eq.setPotential(Integer.valueOf(s[1]), 2, false);
                                if (s.length == 3) {
                                    eq.setPotential(Integer.valueOf(s[2]), 3, false);
                                }
                                c.getPlayer().forceUpdateItem(eq);
                                c.getPlayer().removeInfoQuest(GameConstants.台方塊);
                            }
                            OverseasPacket.getAnimusCubeRes(opcode, action, 1, cubeId);
                        } else if (cubeId == 5062020) { // 閃炫方塊
                            int line = inPacket.decodeByte() / 2;
                            int pots[] = new int[line];
                            for (int i = 0; i < line; i++) {
                                pots[i] = inPacket.decodeInt();
                            }
                            inPacket.decodeByte();
                            short opcode = inPacket.decodeShort();
                            int action = inPacket.decodeInt();
                            if (Integer.valueOf(c.getPlayer().getOneInfo(GameConstants.台方塊, "c")) == line) {
                                Equip eq = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(Short.valueOf(c.getPlayer().getOneInfo(GameConstants.台方塊, "p")));
                                if (eq.getItemId() != Integer.valueOf(c.getPlayer().getOneInfo(GameConstants.台方塊, "i"))) {
                                    c.getPlayer().write(OverseasPacket.getTmsCubeRes(opcode, action, 1));
                                    c.sendEnableActions();
                                    return;
                                }
                                if (EnhanceResultType.EQUIP_MARK.check(eq.getEnchantBuff())) {
                                    c.getPlayer().write(OverseasPacket.getTmsCubeRes(opcode, action, 1));
                                    c.sendEnableActions();
                                    return;
                                }
                                List<Integer> potList = new ArrayList<>();
                                for (String p : c.getPlayer().getOneInfo(GameConstants.台方塊, "o").split(",")) {
                                    potList.add(Integer.parseInt(p));
                                }
                                for (int pot : pots) {
                                    if (!potList.contains(pot)) {
                                        c.getPlayer().write(OverseasPacket.getTmsCubeRes(opcode, action, 1));
                                        c.sendEnableActions();
                                        return;
                                    } else {
                                        potList.remove(potList.indexOf(pot));
                                    }
                                }
                                for (int i = 0; i < pots.length; i++) {
                                    eq.setPotential(pots[i], i + 1, false);
                                }
                                c.getPlayer().forceUpdateItem(eq);
                                c.getPlayer().removeInfoQuest(GameConstants.台方塊);
                            }
                            c.getPlayer().write(OverseasPacket.getTmsCubeRes(opcode, action, 0));
                        }
                        break;
                    }
                }
                break;
            }
        }
    }

    private static boolean showAnimaCubeCost(short opcode, int action, MapleClient c, Equip eq) {
        return animaCubeAction(opcode, action, c, eq, false, 0, false);
    }

    private static boolean useAnimaCube(short opcode, int action, MapleClient c, Equip eq) {
        return animaCubeAction(opcode, action, c, eq, true, 0, false);
    }

    private static boolean useAnimaCube(short opcode, int action, MapleClient c, Equip eq, int toLock, boolean free) {
        return animaCubeAction(opcode, action, c, eq, true, toLock, false);
    }

    private static boolean animaCubeAction(short opcode, int action, MapleClient c, Equip eq, boolean use, int toLock, boolean free) {
        String[] potStates = {"n", "r", "e", "u", "l"};// 無潛能,特殊,稀有,罕見,傳說
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        DateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        Date dateQuest;
        Date dateNow = new Date();
        try {
            dateQuest = c.getPlayer().getOneInfo(GameConstants.神秘方塊, "d") != null ? fmt.parse(c.getPlayer().getOneInfo(GameConstants.神秘方塊, "d")) : new Date();
        } catch (ParseException ex) {
            dateQuest = new Date();
        }
        if (Integer.parseInt(sdf.format(dateNow)) - Integer.parseInt(sdf.format(dateQuest)) > 0) {
            c.getPlayer().updateOneInfo(GameConstants.神秘方塊, "d", sdf.format(dateNow), false);
            for (String s : potStates) {
                c.getPlayer().updateOneInfo(GameConstants.神秘方塊, s, "0", false);
            }
        }

        Map<String, Integer> animaCubePotTimes = new HashMap<>();
        for (String s : potStates) {
            animaCubePotTimes.put(s, c.getPlayer().getOneInfo(GameConstants.神秘方塊, s) == null ? 0 : Integer.parseInt(c.getPlayer().getOneInfo(GameConstants.神秘方塊, s)));
        }

        int potentialState = eq.getState(false);
        if (potentialState >= 17) {
            potentialState -= 16;
        }

        if (eq.getCurrentUpgradeCount() == 0 && eq.getRestUpgradeCount() == 0 && !ItemConstants.類型.副手(eq.getItemId()) && !ItemConstants.類型.能源(eq.getItemId()) && !ItemConstants.類型.特殊潛能道具(eq.getItemId()) || MapleItemInformationProvider.getInstance().isCash(eq.getItemId()) || ItemConstants.類型.無法潛能道具(eq.getItemId())) {
            c.getPlayer().dropMessage(1, "在這道具無法使用。");
            c.write(OverseasPacket.getAnimaCubeRes(opcode, action, 3, 0));
            return false;
        }

        if (ItemConstants.類型.特殊潛能道具(eq.getItemId()) && potentialState == 0) {
            c.getPlayer().dropMessage(1, "此道具只能透過專用潛能捲軸來進行潛能設定.請設定潛能後再使用.");
            c.write(OverseasPacket.getAnimaCubeRes(opcode, action, 3, 0));
            return false;
        }

        String state = potStates[potentialState];
        int value = 0;
        if (use) {
            value = 2;

            long price = ItemConstants.方塊.getMapleCubeCost(animaCubePotTimes.get(state), potentialState);
            if (c.getPlayer().getMeso() < price && !free) {
                return false;
            }

            if (potentialState == 0) {
                eq.renewPotential(false);
                eq.magnify();
                c.getPlayer().forceUpdateItem(eq);
                eq.addAttribute(ItemAttribute.TradeBlock.getValue());
            } else if (!eq.useCube(3996222, c.getPlayer(), toLock)) {
                return false;
            }

            eq.addAttribute(ItemAttribute.AnimaCube.getValue());

            if (!free) {
                c.getPlayer().gainMeso(-price, false);
            }
            c.getPlayer().updateOneInfo(GameConstants.神秘方塊, state, String.valueOf(animaCubePotTimes.get(state) + 1), false);
            for (String s : potStates) {
                animaCubePotTimes.put(s, c.getPlayer().getOneInfo(GameConstants.神秘方塊, s) == null ? 0 : Integer.parseInt(c.getPlayer().getOneInfo(GameConstants.神秘方塊, s)));
            }
            potentialState = eq.getState(false);
            if (potentialState >= 17) {
                potentialState -= 16;
            }
            state = potStates[potentialState];
        }
        c.write(OverseasPacket.getAnimaCubeRes(opcode, action, value, ItemConstants.方塊.getMapleCubeCost(animaCubePotTimes.get(state), potentialState)));
        return true;
    }
}
