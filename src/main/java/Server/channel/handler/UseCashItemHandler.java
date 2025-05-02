/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.channel.handler;

import Client.*;
import Client.inventory.*;
import Client.skills.Skill;
import Client.skills.SkillFactory;
import Client.stat.DeadDebuff;
import Client.stat.PlayerStats;
import Config.configs.ServerConfig;
import Config.constants.*;
import Config.constants.enums.BroadcastMessageType;
import Config.constants.enums.ConversationType;
import Config.constants.enums.UserChatMessageType;
import Net.server.*;
import Net.server.PredictCardFactory.PredictCard;
import Net.server.PredictCardFactory.PredictCardComment;
import Net.server.cashshop.CashItemFactory;
import Net.server.life.MapleLifeFactory;
import Net.server.maps.FieldLimitType;
import Net.server.maps.MapleAffectedArea;
import Net.server.maps.MapleLove;
import Net.server.maps.MapleMap;
import Net.server.quest.MapleQuest;
import Net.server.shop.MapleShop;
import Net.server.shop.MapleShopFactory;
import Net.server.shops.HiredMerchant;
import Packet.*;
import Server.world.WorldBroadcastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.DateUtil;
import tools.Pair;
import tools.Randomizer;
import tools.data.MaplePacketReader;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * @author PlayDK
 */
public class UseCashItemHandler {

    private static final Logger log = LoggerFactory.getLogger(UseCashItemHandler.class);

    public static void handlePacket(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || chr.getMap() == null || chr.inPVP()) {
            c.sendEnableActions();
            return;
        }
        chr.updateTick(slea.readInt());
        chr.setScrolledPosition((short) 0);
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        short slot = slea.readShort();
        int itemId = slea.readInt(); //物品ID
        int itemType = itemId / 10000; //物品類型,取余
        MapleInventoryType iType = MapleInventoryType.USE;
        Item toUse = chr.getInventory(iType).getItem(slot);
        if (toUse == null || toUse.getItemId() != itemId) {
            iType = MapleInventoryType.CASH;
            toUse = chr.getInventory(iType).getItem(slot);
        }
        if (toUse == null || toUse.getItemId() != itemId || toUse.getQuantity() < 1 || chr.hasBlockedInventory()) {
            c.sendEnableActions();
            return;
        }
        boolean used = false, cc = false;
        switch (itemType) {
            case 285: { // 萌獸卡牌包
                if (chr.getSpace(2) < 1) {
                    chr.dropMessage(1, "請先空出消耗欄1格，再試一次");
                    break;
                }
                int nMinGrade = 0;
                int nMaxGrade;
                switch (itemId) {
                    case 2854000: // 低等級萌獸卡牌包
                        nMaxGrade = 0;
                        break;
                    case 2854001: // 中等級萌獸卡牌包
                        nMaxGrade = 1;
                        break;
                    case 2854002: // 高等級萌獸卡牌包
                        nMaxGrade = 2;
                        break;
                    case 2854004: // 那堤怪物萌獸卡
                        nMinGrade = 2;
                        nMaxGrade = 2;
                        break;
                    default:
                        nMaxGrade = 0;
                        break;
                }
                InventoryHandler.UseFamiliarCard(chr, nMinGrade, nMaxGrade, false, itemId == 2854004);
                used = true;
                break;
            }
            case 504: { //縮地石之類
                if (itemId == 5043000 || itemId == 5043001) {
                    short questid = slea.readShort();
                    int npcid = slea.readInt();
                    MapleQuest quest = MapleQuest.getInstance(questid);
                    if (chr.getQuest(quest).getStatus() == 1 && quest.canComplete(chr, npcid)) {
                        int mapId = MapleLifeFactory.getNPCLocation(npcid);
                        if (mapId != -1) {
                            MapleMap map = c.getChannelServer().getMapFactory().getMap(mapId);
                            if (map.containsNPC(npcid) && !FieldLimitType.TELEPORTITEMLIMIT.check(chr.getMap().getFieldLimit()) && !FieldLimitType.TELEPORTITEMLIMIT.check(map.getFieldLimit()) && !chr.isInBlockedMap()) {
                                chr.changeMap(map, map.getPortal(0));
                            }
                            used = true;
                        } else {
                            chr.dropMessage(1, "使用道具出現未知的錯誤.");
                        }
                    }
                } else if (itemId == 5042000) { //豫園高級瞬移之石
                    MapleMap map = c.getChannelServer().getMapFactory().getMap(701000200); //東方神州 - 上海豫園
                    chr.changeMap(map, map.getPortal(0));
                    used = true;
                    break;
                } else if (itemId == 5040005) { //超時空券
                    chr.dropMessage(5, "無法使用這個道具.");
                    break;
                } else if (itemId / 1000 == 5044) { // 超級瞬移之石
                    long itemNextTime = 0;
                    String keyValue = chr.getKeyValue("MapTransferItemNextTime");
                    String newKeyValue = "";
                    if (keyValue != null) {
                        final String[] split = keyValue.split(",");
                        for (String nextTime : split) {
                            if (nextTime == null || !nextTime.contains("=")) {
                                continue;
                            }
                            final String[] split_2 = nextTime.split("=");
                            if (split_2[0].equals(String.valueOf(toUse.getSN()))) {
                                itemNextTime = Long.parseLong(split_2[1]);
                                continue;
                            }
                            newKeyValue += nextTime + ",";
                        }
                    }
                    if (itemNextTime != 0 && System.currentTimeMillis() < itemNextTime) {
                        chr.dropMessage(5, "現在還不能使用。");
                        break;
                    }
                    slea.skip(1);
                    int toMapId = slea.readInt();
                    final MapleMap moveTo = c.getChannelServer().getMapFactory().getMap(toMapId);
                    if (MapConstants.isBossMap(toMapId)) {
                        c.announce(MTSCSPacket.getTrockMessage((byte) 11));
                        c.sendEnableActions();
                        return;
                    }
                    if (moveTo == null || FieldLimitType.TELEPORTITEMLIMIT.check(moveTo.getFieldLimit())) {
                        c.announce(MTSCSPacket.getTrockMessage((byte) 11));
                        c.sendEnableActions();
                        return;
                    }
                    if (moveTo == chr.getMap()) {
                        c.announce(MTSCSPacket.getTrockMessage((byte) 9));
                        c.sendEnableActions();
                        return;
                    }
                    if (chr.getLevel() < moveTo.getLevelLimit()) {
                        chr.dropMessage(1, "只有" + moveTo.getLevelLimit() + "等級可以移動的地區。");
                        c.sendEnableActions();
                        return;
                    }
                    if (moveTo.getQuestLimit() > 0 && chr.getQuestStatus(moveTo.getQuestLimit()) != 2) {
                        if (moveTo.getBarrierArc() > 0 || moveTo.getBarrierAut() > 0) {
                            c.announce(MTSCSPacket.getTrockMessage((byte) 11));
                            c.sendEnableActions();
                            chr.dropMessage(5, "目標地區需要完成任務「" + MapleQuest.getInstance(moveTo.getQuestLimit()).getName() + "」才能傳送。");
                            if (!chr.isIntern()) {
                                return;
                            }
                        }
                    }
                    int limitMin = ii.getItemProperty(itemId, "info/limitMin", 0);
                    if (limitMin > 0) {
                        itemNextTime = System.currentTimeMillis() + limitMin * 60000;
                        newKeyValue += toUse.getSN() + "=" + itemNextTime;
                        chr.setKeyValue("MapTransferItemNextTime", newKeyValue);
                        //chr.write(OverseasPacket.extraSystemResult(ExtraSystemResult.extraTimerSystem(toUse.getSN(), itemNextTime)));
                    }
                    if (5044016 == itemId || 5044017 == itemId) {
                        used = true;
                    }
                    chr.changeMap(moveTo);
                    break;
                } else if (itemId < 5042000) {
                    used = InventoryHandler.UseTeleRock(slea, c, itemId);
                }
                break;
            }
            case 505: { //洗能力點
                if (itemId == 5050000) {
                    final Map<MapleStat, Long> statupdate = new EnumMap<MapleStat, Long>(MapleStat.class);
                    int apto = (int) slea.readLong();
                    int apfrom = (int) slea.readLong();
                    int statLimit = ServerConfig.CHANNEL_PLAYER_MAXAP;
                    if (chr.isAdmin()) {
                        chr.dropMessage(5, "洗能力點 apto: " + apto + " apfrom: " + apfrom);
                    }
                    if (apto == apfrom) {
                        break; // Hack
                    }
                    int job = chr.getJob();
                    PlayerStats playerst = chr.getStat();
                    used = true;
                    switch (apto) { // AP to
                        case 64: // 力量
                            if (playerst.getStr() >= statLimit) {
                                used = false;
                            }
                            break;
                        case 128: // 敏捷
                            if (playerst.getDex() >= statLimit) {
                                used = false;
                            }
                            break;
                        case 256: // 智力
                            if (playerst.getInt() >= statLimit) {
                                used = false;
                            }
                            break;
                        case 512: // 幸運
                            if (playerst.getLuk() >= statLimit) {
                                used = false;
                            }
                            break;
                        case 2048: // 血
                            if (playerst.getMaxHp() >= ServerConfig.CHANNEL_PLAYER_MAXHP) {
                                used = false;
                            }
                            break;
                        case 8192: // 藍
                            if (playerst.getMaxMp() >= ServerConfig.CHANNEL_PLAYER_MAXMP) {
                                used = false;
                            }
                            break;
                    }
                    switch (apfrom) { // AP to
                        case 64: // 力量
                            if (playerst.getStr() <= 4) {
                                used = false;
                            }
                            break;
                        case 128: // 敏捷
                            if (playerst.getDex() <= 4) {
                                used = false;
                            }
                            break;
                        case 256: // 智力
                            if (playerst.getInt() <= 4) {
                                used = false;
                            }
                            break;
                        case 512: // 幸運
                            if (playerst.getLuk() <= 4) {
                                used = false;
                            }
                            break;
                        case 2048: // 血
                            if (chr.getHpApUsed() <= 0 || chr.getHpApUsed() >= 10000) {
                                used = false;
                            }
                            break;
                        case 8192: // 藍
                            if (chr.getMpApUsed() <= 0 || chr.getMpApUsed() >= 10000 || JobConstants.isNotMpJob(job)) {
                                used = false;
                            }
                            break;
                    }
                    if (used) {
                        switch (apto) { // AP to
                            case 64: { // 力量
                                long toSet = playerst.getStr() + 1;
                                playerst.setStr((short) toSet, chr);
                                statupdate.put(MapleStat.力量, toSet);
                                break;
                            }
                            case 128: { // 敏捷
                                long toSet = playerst.getDex() + 1;
                                playerst.setDex((short) toSet, chr);
                                statupdate.put(MapleStat.敏捷, toSet);
                                break;
                            }
                            case 256: { // 智力
                                long toSet = playerst.getInt() + 1;
                                playerst.setInt((short) toSet, chr);
                                statupdate.put(MapleStat.智力, toSet);
                                break;
                            }
                            case 512: { // 幸運
                                long toSet = playerst.getLuk() + 1;
                                playerst.setLuk((short) toSet, chr);
                                statupdate.put(MapleStat.幸運, toSet);
                                break;
                            }
                            case 2048: // 血
                                chr.useHpAp(1);
                                break;
                            case 8192: // 藍
                                if (JobConstants.isNotMpJob(job)) {  //惡魔和天使不能洗
                                    chr.dropMessage(1, "該職業無法使用.");
                                } else {
                                    chr.useMpAp(1);
                                }
                                break;
                        }
                        switch (apfrom) { // AP from
                            case 64: { // 力量
                                long toSet = playerst.getStr() - 1;
                                playerst.setStr((short) toSet, chr);
                                statupdate.put(MapleStat.力量, toSet);
                                break;
                            }
                            case 128: { // 敏捷
                                long toSet = playerst.getDex() - 1;
                                playerst.setDex((short) toSet, chr);
                                statupdate.put(MapleStat.敏捷, toSet);
                                break;
                            }
                            case 256: { // 智力
                                long toSet = playerst.getInt() - 1;
                                playerst.setInt((short) toSet, chr);
                                statupdate.put(MapleStat.智力, toSet);
                                break;
                            }
                            case 512: { // 幸運
                                long toSet = playerst.getLuk() - 1;
                                playerst.setLuk((short) toSet, chr);
                                statupdate.put(MapleStat.幸運, toSet);
                                break;
                            }
                            case 2048: // 血
                                chr.useHpAp(-1);
                                break;
                            case 8192: // 藍
                                if (JobConstants.isNotMpJob(job)) {  //惡魔和天使不能洗
                                    chr.dropMessage(1, "該職業無法使用.");
                                } else {
                                    chr.useMpAp(-1);
                                }
                                break;
                        }
                        c.announce(MaplePacketCreator.updatePlayerStats(statupdate, true, chr));
                    }
                } else if (itemId < 5050010) { // 重配1點技能點卷軸
                    if (itemId >= 5050005 && itemId <= 5050009) { // 龍魔重配技能券(舊版龍魔, 有10轉的時候)
                        chr.dropMessage(1, "這個道具無法使用");
                        break;
                    }
                    if (itemId < 5050005 && (JobConstants.is神之子(chr.getJob()) || JobConstants.is皮卡啾(chr.getJob()) || JobConstants.is雪吉拉(chr.getJob()))) {
                        chr.dropMessage(1, "這個職業無法使用這個道具");
                        break;
                    } //well i dont really care other than this o.o
                    int skill1 = slea.readInt();
                    int skill2 = slea.readInt();
                    for (int i : GameConstants.blockedSkills) {
                        if (skill1 == i) {
                            chr.dropMessage(1, "該技能未修復，無法增加此技能.");
                            return;
                        }
                    }
                    Skill skillSPTo = SkillFactory.getSkill(skill1);
                    Skill skillSPFrom = SkillFactory.getSkill(skill2);
                    if (skillSPTo.isBeginnerSkill() || skillSPFrom.isBeginnerSkill()) {
                        chr.dropMessage(1, "無法對新手技能使用.");
                        break;
                    }
                    if (JobConstants.getSkillBookBySkill(skill1) != JobConstants.getSkillBookBySkill(skill2)) { //resistance evan
                        chr.dropMessage(1, "You may not add different job skills.");
                        break;
                    }
                    if ((chr.getSkillLevel(skillSPTo) + 1 <= skillSPTo.getMaxLevel()) && chr.getSkillLevel(skillSPFrom) > 0 && skillSPTo.canBeLearnedBy(chr.getJobWithSub())) {
                        if (skillSPTo.isFourthJob() && (chr.getSkillLevel(skillSPTo) + 1 > chr.getMasterLevel(skillSPTo))) {
                            chr.dropMessage(1, "You will exceed the master level.");
                            break;
                        }
                        if (itemId >= 5050005) {
                            if (JobConstants.getSkillBookBySkill(skill1) != (itemId - 5050005) * 2 && JobConstants.getSkillBookBySkill(skill1) != (itemId - 5050005) * 2 + 1) {
                                chr.dropMessage(1, "You may not add this job SP using this reset.");
                                break;
                            }
                        } else {
                            int theJob = JobConstants.getJobNumber(skill2 / 10000);
                            if (theJob != itemId - 5050000) { //you may only subtract from the skill if the ID matches Sp reset
                                chr.dropMessage(1, "You may not subtract from this skill. Use the appropriate SP reset.");
                                break;
                            }
                        }
                        chr.changeSingleSkillLevel(skillSPFrom, (byte) (chr.getSkillLevel(skillSPFrom) - 1), chr.getMasterLevel(skillSPFrom));
                        chr.changeSingleSkillLevel(skillSPTo, (byte) (chr.getSkillLevel(skillSPTo) + 1), chr.getMasterLevel(skillSPTo));
                        used = true;
                    }
                } else if (itemId == 5050100) { // AP點數初始化券
                    chr.resetStats(4, 4, 4, 4);
                    chr.dropMessage(1, "AP點數初始化完成");
                    used = true;
                } else if (itemId == 5051001) { // 技能點數初始化卷軸
                    chr.spReset();
                    chr.dropMessage(1, "技能點數初始化完成");
                    used = true;
                } else {
                    chr.dropMessage(1, "這個道具還未修復，請聯繫管理員");
                }
                break;
            }
            case 506: {
                if (itemId == 5060000) { //裝備刻名
                    short slot2 = slea.readShort();
                    MapleInventoryType inventoryType = slot2 > 0 ? MapleInventoryType.EQUIP : MapleInventoryType.EQUIPPED;
                    Item item = chr.getInventory(inventoryType).getItem(slot2);
                    if (item != null && item.getOwner().equals("") && !((Equip) item).isMvpEquip()) {
                        boolean change = true;
                        for (String z : GameConstants.RESERVED) {
                            if (chr.getName().contains(z)) {
                                change = false;
                                break;
                            }
                        }
                        if (change) {
                            item.setOwner(chr.getName());
                            chr.forceUpdateItem(item);
                            used = true;
                            break;
                        }
                    } else {
                        chr.dropMessage(1, "請將道具直接點在你需要刻名的裝備上.");
                        break;
                    }
                } else if (itemId == 5060010) { // 去除名字
                    short slot2 = slea.readShort();
                    MapleInventoryType inventoryType = slot2 > 0 ? MapleInventoryType.EQUIP : MapleInventoryType.EQUIPPED;
                    Item item = chr.getInventory(inventoryType).getItem(slot2);
                    if (item != null && !item.getOwner().equals("") && !((Equip) item).isMvpEquip()) {
                        boolean change = true;
                        for (String z : GameConstants.RESERVED) {
                            if (chr.getName().contains(z)) {
                                change = false;
                                break;
                            }
                        }
                        if (change) {
                            item.setOwner("");
                            chr.forceUpdateItem(item);
                            used = true;
                            break;
                        }
                    } else {
                        chr.dropMessage(1, "請將道具直接點在你需要刻名的裝備上.");
                        break;
                    }
                } else if (itemId == 5060001 || itemId == 5061000 || itemId == 5061001 || itemId == 5061002 || itemId == 5061003) { //封印之鎖
                    MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
                    Item item = chr.getInventory(type).getItem((short) slea.readInt());
                    if (item != null && item.getExpiration() == -1) {
                        item.addAttribute(ItemAttribute.Seal.getValue());
                        long days = 0;
                        if (itemId == 5061000) { //封印之鎖（7天）
                            days = 7;
                        } else if (itemId == 5061001) { //封印之鎖：30天
                            days = 30;
                        } else if (itemId == 5061002) { //封印之鎖（90天）
                            days = 90;
                        } else if (itemId == 5061003) { //封印之鎖（365天）
                            days = 365;
                        }
                        if (chr.isAdmin()) {
                            chr.dropMessage(5, "使用封印之鎖 物品ID: " + itemId + " 天數: " + days);
                        }
                        if (days > 0) {
                            item.setExpiration(System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000));
                        }
                        chr.forceUpdateItem(item);
                        used = true;
                        break;
                    } else {
                        chr.dropMessage(1, "使用道具出現錯誤.");
                        break;
                    }
                } else if (itemId == 5061100) {
                    MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
                    Item item = chr.getInventory(type).getItem((short) slea.readInt());
                    String secondPwd = slea.readMapleAsciiString();
                    if (!c.CheckSecondPassword(secondPwd)) {
                        chr.dropMessage(1, "第二組密碼不正確");
                        break;
                    }
                    if (item != null && ItemAttribute.Seal.check(item.getAttribute())) {
                        item.setExpiration(System.currentTimeMillis() + 86400000);
                        // 立即解除封印
                        // item.removeAttribute((short) ItemAttribute.Seal.getValue());
                        // item.setExpiration(-1);
                        chr.forceUpdateItem(item);
                        used = true;
                        break;
                    } else {
                        chr.dropMessage(1, "使用道具出現錯誤.");
                    }
                    break;
                } else if (itemId == 5060003) { //花生機器
                    if (c.getPlayer().getSpace(1) < 1 || c.getPlayer().getSpace(2) < 1 || c.getPlayer().getSpace(3) < 1) {
                        c.announce(MaplePacketCreator.getPeanutResult(itemId));
                        c.sendEnableActions();
                        break;
                    }
                    int etcItemId = 4170023;
                    Item etcItem = chr.getInventory(MapleInventoryType.ETC).findById(etcItemId);
                    if (etcItem == null || etcItem.getQuantity() <= 0 || !MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.ETC, etcItem.getPosition(), (short) 1, false)) {
                        chr.dropMessage(1, "你沒有" + ii.getName(etcItemId) + "，無法使用。");
                        c.sendEnableActions();
                        return;
                    }
                    int reward = getIncubatedItem(chr, itemId, etcItemId);
                    if (reward < 0) {
                        chr.dropMessage(1, "出現未知錯誤。");
                        c.sendEnableActions();
                        break;
                    }
                    c.announce(MaplePacketCreator.getPeanutResult(reward, (short) 1, itemId, slot));
                    used = true;
                    break;
                } else if (itemId == 5060025) { // 魔法豎琴
                    if (c.getPlayer().getSpace(1) < 2 || c.getPlayer().getSpace(2) < 2 || c.getPlayer().getSpace(3) < 2 || c.getPlayer().getSpace(4) < 2) {
                        c.announce(MaplePacketCreator.getPeanutResult(itemId));
                        c.sendEnableActions();
                        break;
                    }
                    int etcItemId = 4170043;
                    Item etcItem = chr.getInventory(MapleInventoryType.ETC).findById(etcItemId);
                    if (etcItem == null || etcItem.getQuantity() <= 0 || !MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.ETC, etcItem.getPosition(), (short) 1, false)) {
                        chr.dropMessage(1, "你沒有" + ii.getName(etcItemId) + "，無法使用。");
                        c.sendEnableActions();
                        return;
                    }
                    int reward = getIncubatedItem(chr, itemId, etcItemId);
                    if (reward < 0) {
                        chr.dropMessage(1, "出現未知錯誤。");
                        c.sendEnableActions();
                        break;
                    }
                    short sQuantity = (short) Randomizer.rand(1, 5);
                    c.announce(MaplePacketCreator.getPeanutResult(reward, (short) 1, itemId, slot, 2432488, sQuantity));
                    MapleInventoryManipulator.addById(c, 2432488, sQuantity, null, null, 0, 0, ii.getName(itemId) + " 在 " + DateUtil.getCurrentDate());
                    used = true;
                } else if (itemId == 5060028) { // 潘朵拉箱子
                    if (c.getPlayer().getSpace(1) < 2 || c.getPlayer().getSpace(2) < 2 || c.getPlayer().getSpace(3) < 2 || c.getPlayer().getSpace(4) < 2) {
                        c.announce(MaplePacketCreator.getPeanutResult(itemId));
                        c.sendEnableActions();
                        break;
                    }
                    int etcItemId = 4170049;
                    Item etcItem = chr.getInventory(MapleInventoryType.ETC).findById(etcItemId);
                    if (etcItem == null || etcItem.getQuantity() <= 0 || !MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.ETC, etcItem.getPosition(), (short) 1, false)) {
                        chr.dropMessage(1, "你沒有" + ii.getName(etcItemId) + "，無法使用。");
                        c.sendEnableActions();
                        return;
                    }
                    int reward = getIncubatedItem(chr, itemId, etcItemId);
                    if (reward < 0) {
                        chr.dropMessage(1, "出現未知錯誤。");
                        c.sendEnableActions();
                        break;
                    }
                    c.announce(MaplePacketCreator.getPeanutResult(reward, (short) 1, itemId, slot));
                    used = true;
                } else if (itemId == 5060029) { // 艾比米修斯箱子
                    if (c.getPlayer().getSpace(1) < 2 || c.getPlayer().getSpace(2) < 2 || c.getPlayer().getSpace(3) < 2 || c.getPlayer().getSpace(4) < 2) {
                        c.announce(MaplePacketCreator.getPeanutResult(itemId));
                        c.sendEnableActions();
                        break;
                    }
                    int etcItemId = 4170050;
                    Item etcItem = chr.getInventory(MapleInventoryType.ETC).findById(etcItemId);
                    if (etcItem == null || etcItem.getQuantity() <= 0 || !MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.ETC, etcItem.getPosition(), (short) 1, false)) {
                        chr.dropMessage(1, "你沒有" + ii.getName(etcItemId) + "，無法使用。");
                        c.sendEnableActions();
                        return;
                    }
                    int reward = getIncubatedItem(chr, itemId, etcItemId);
                    if (reward < 0) {
                        chr.dropMessage(1, "出現未知錯誤。");
                        c.sendEnableActions();
                        break;
                    }
                    c.announce(MaplePacketCreator.getPeanutResult(reward, (short) 1, itemId, slot));
                    used = true;
                } else if (itemId == 5060048) { // 黃金蘋果
                    if (c.getPlayer().getSpace(1) < 1 || c.getPlayer().getSpace(2) < 2 || c.getPlayer().getSpace(3) < 1 || c.getPlayer().getSpace(4) < 1) {
                        c.announce(MaplePacketCreator.getPeanutResult(itemId));
                        c.sendEnableActions();
                        break;
                    }
                    int reward = getIncubatedItem(chr, itemId, 4170062);
                    if (reward < 0) {
                        chr.dropMessage(1, "出現未知錯誤。");
                        c.sendEnableActions();
                        break;
                    }
                    c.announce(MaplePacketCreator.getPeanutResult(reward, (short) 1, itemId, slot, 2630612, (short) 1));
                    MapleInventoryManipulator.addById(c, 2630612, 1, null, null, ServerConfig.goldenAppleFragmentNoTimeLimit ? 0 : (RafflePool.getNextPeriodDate(5060048) - System.currentTimeMillis()), 0, ii.getName(itemId) + " 在 " + DateUtil.getCurrentDate());
                    used = true;
                } else if (itemId == 5060086) { // 魔法畫框
                    if (c.getPlayer().getSpace(1) < 1 || c.getPlayer().getSpace(2) < 2 || c.getPlayer().getSpace(3) < 1 || c.getPlayer().getSpace(4) < 1) {
                        c.announce(MaplePacketCreator.getPeanutResult(itemId));
                        c.sendEnableActions();
                        break;
                    }
                    int reward = getIncubatedItem(chr, itemId, 4170086);
                    if (reward < 0) {
                        chr.dropMessage(1, "出現未知錯誤。");
                        c.sendEnableActions();
                        break;
                    }
                    c.announce(MaplePacketCreator.getPeanutResult(reward, (short) 1, itemId, slot, 2634934, (short) 1));
                    MapleInventoryManipulator.addById(c, 2634934, 1, null, null, ServerConfig.goldenAppleFragmentNoTimeLimit ? 0 : (RafflePool.getNextPeriodDate(5060048) - System.currentTimeMillis()), 0, ii.getName(itemId) + " 在 " + DateUtil.getCurrentDate());
                    used = true;
                } else if (itemId == 5060049) { // 傷害字型卡包
                    if (chr.getSpace(2) < 1) {
                        c.announce(MaplePacketCreator.getPeanutResult(itemId));
                        c.sendEnableActions();
                        break;
                    }
                    int reward = getIncubatedItem(chr, itemId, 4170063);
                    if (reward < 0) {
                        chr.dropMessage(1, "出現未知錯誤。");
                        c.sendEnableActions();
                        break;
                    }
                    c.announce(MaplePacketCreator.getPeanutResult(reward, (short) 1, itemId, slot));
                    used = true;
                    break;
                } else if (itemId == 5060057) { // 幸運箱子
                    if (c.getPlayer().getSpace(1) < 2 || c.getPlayer().getSpace(2) < 2 || c.getPlayer().getSpace(3) < 2 || c.getPlayer().getSpace(4) < 2) {
                        c.announce(MaplePacketCreator.getPeanutResult(itemId));
                        c.sendEnableActions();
                        break;
                    }
                    int reward = getIncubatedItem(chr, itemId, 4170069);
                    if (reward < 0) {
                        chr.dropMessage(1, "出現未知錯誤。");
                        c.sendEnableActions();
                        break;
                    }
                    c.announce(MaplePacketCreator.getPeanutResult(reward, (short) 1, itemId, slot, 2435719, (short) 1));
                    MapleInventoryManipulator.addById(c, 2435719, 1, null, null, 0, 0, ii.getName(itemId) + " 在 " + DateUtil.getCurrentDate());
                    used = true;
                } else if (itemId == 5064000 || itemId == 5064003 || itemId == 5064100 || itemId == 5064300 || itemId == 5068100 || itemId == 5068200 || itemId == 5064400) { //裝備保護卷軸 or 安全盾牌卷軸 or 寵物專用打擊盾牌 or 寵物專用恢復盾牌 or 恢復卡
                    MapleInventoryType ivType = MapleInventoryType.getByType((byte) slea.readShort());
                    short dst = slea.readShort();
                    ItemScrollHandler.UseUpgradeScroll(slot, dst, ivType, (short) 0, c, chr, 0, slea.readBool(), iType == MapleInventoryType.CASH);
                    return;
                } else if (itemId == 5064200) { //完美還原卷軸 - 可以使除潛能之外的所有屬性#c初始化#為標準能力值。只能用於裝備道具。成長型道具可以恢復到成長之前的狀態。\n此外，無法用於使用了#c宿命剪刀、物品共享牌、封印之鎖#的道具。附加屬性不會初始化。
                    chr.updateTick(slea.readInt());
                    short ws = slea.readShort();
                    short dst = slea.readShort();
                    ItemScrollHandler.UseUpgradeScroll(slot, dst, null, ws, c, chr, 0, slea.readBool(), iType == MapleInventoryType.CASH);
                    return;
                } else if (itemId >= 5062000 && itemId < 5062200 || itemId >= 5062502 && itemId <= 5062503) { //方塊
                    short equipSlot = (short) slea.readInt();
                    Equip equip = (Equip) chr.getInventory(MapleInventoryType.EQUIP).getItem(equipSlot);
                    if (equipSlot < 0) {
                        equip = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(equipSlot);
                    }
                    int lockslot = 0;
                    short lockid = 0;
                    if (itemId == 5062000) {
                        lockslot = slea.readInt();
                        lockid = slea.readShort();
                    }

                    if (equip != null) {
                        if (lockslot > 0) {
                            Item item = chr.getInventory(MapleInventoryType.CASH).findById(5067000);
                            if (item == null) {
                                break;
                            }
                            MapleInventoryManipulator.removeById(chr.getClient(), MapleInventoryType.CASH, item.getPosition(), 1, false, true);
                        }
                        used = equip.useCube(itemId, chr, lockslot);
                        if (JobConstants.is神之子(chr.getJob()) && equipSlot == -10) {
                            equip = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
                            equip.copyPotential((Equip) (chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -10)));
                            chr.forceUpdateItem(equip);
                        }
                        if (used) {
                            chr.equipChanged();
                        }
                    }
                    if (equip == null || !used) {
                        chr.getMap().broadcastMessage(InventoryPacket.showPotentialReset(chr.getId(), false, itemId, ItemConstants.方塊.getCubeDebris(itemId), 0));
                    }
                    break;
                } else if (itemId == 5062400 || itemId == 5062401 || itemId == 5062402 || itemId == 5062403 || itemId == 5062404 || itemId == 5062405) {
                    //5062400 - 神奇鐵砧 - 可合成#c同一種類的遊戲裝備#，並#c改變裝備外觀#的道具、裝備屬性由功能道具決定，可以用相同外觀的道具進行合成。合成後，#c外觀道具會消失#。\n注意：可使用的裝備: 帽子, 眼飾, 臉飾, 上衣, 褲裙, 全身甲, 鞋子, 手套, 披風, 武器, 盾牌以及刀等總共12種。
                    //5062402 - 勳章神秘鐵砧 - 可合成#c勳章道具#，並#c改變外觀#的鐵砧。道具效果將依照功能道具，擁有同樣外觀的道具也可以合成，請注意：合成後，#c外觀道具會消失#。\n可使用的裝備：勳章
                    MapleInventoryType typ = MapleInventoryType.getByType((byte) slea.readInt());
                    short skinSlot = (short) slea.readInt();
                    short itemSlot = (short) slea.readInt();
                    Equip toItem = (Equip) chr.getInventory(typ).getItem(itemSlot);
                    Equip skin = (Equip) chr.getInventory(typ).getItem(skinSlot);
                    if (!ItemConstants.類型.裝備(skin.getItemId()) || !ItemConstants.類型.裝備(toItem.getItemId()) || skin.getItemId() / 10000 != toItem.getItemId() / 10000) {
                        if (c.getPlayer().isAdmin()) {
                            c.getPlayer().dropDebugMessage(2, "[影像合成] 外型道具不是裝備:" + !ItemConstants.類型.裝備(skin.getItemId()) + " 功能道具不是裝備:" + !ItemConstants.類型.裝備(toItem.getItemId()) + " 外型道具和功能道具不是同一類型:" + (skin.getItemId() / 10000 != toItem.getItemId() / 10000));
                        }
                        c.announce(InventoryPacket.showSynthesizingMsg(itemId, 2028093, false));
                        return;
                    }
                    if (ItemConstants.類型.getGender(skin.getItemId()) < 2 && ItemConstants.類型.getGender(skin.getItemId()) != ItemConstants.類型.getGender(toItem.getItemId())) {
                        if (c.getPlayer().isAdmin()) {
                            c.getPlayer().dropDebugMessage(2, "[影像合成] 外型道具和功能道具不符合");
                        }
                        c.announce(InventoryPacket.showSynthesizingMsg(itemId, 2028093, false));
                        return;
                    }
                    if (ItemConstants.類型.勳章(toItem.getItemId())) {
                        if (itemId != 5062402) {
                            if (c.getPlayer().isAdmin()) {
                                c.getPlayer().dropDebugMessage(2, "[影像合成] 影像合成的勳章使用的道具不是 勳章的神秘鐵鉆");
                            }
                            c.announce(InventoryPacket.showSynthesizingMsg(itemId, 2028093, false));
                            return;
                        }
                    } else if (ItemConstants.類型.防具(toItem.getItemId())
                            || ItemConstants.類型.臉飾(toItem.getItemId())
                            || ItemConstants.類型.眼飾(toItem.getItemId())
                            || ItemConstants.類型.耳環(toItem.getItemId())
                            || ItemConstants.類型.盾牌(toItem.getItemId())
                            || ItemConstants.類型.武器(toItem.getItemId())) {
                        if (itemId == 5062404) {
                            if (!ii.isCash(toItem.getItemId()) || ItemConstants.類型.武器(toItem.getItemId())) {
                                if (c.getPlayer().isAdmin()) {
                                    c.getPlayer().dropDebugMessage(2, "[影像合成] 影像合成的現金道具使用的不是 時裝神秘鐵砧");
                                }
                                c.announce(InventoryPacket.showSynthesizingMsg(itemId, 2028093, false));
                                return;
                            }
                        } else if (ii.isCash(toItem.getItemId())) {
                            if (c.getPlayer().isAdmin()) {
                                c.getPlayer().dropDebugMessage(2, "[影像合成] 使用勳章的神秘鐵鉆的道具不是非現金道具");
                            }
                            c.announce(InventoryPacket.showSynthesizingMsg(itemId, 2028093, false));
                            return;
                        }
                    } else {
                        if (c.getPlayer().isAdmin()) {
                            c.getPlayer().dropDebugMessage(2, "[影像合成] 此道具類型無法使用影像合成");
                        }
                        c.announce(InventoryPacket.showSynthesizingMsg(itemId, 2028093, false));
                        return;
                    }
                    if (toItem.getItemId() / 10000 == skin.getItemId() / 10000 && toItem != null && skin != null && chr.getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                        MapleInventoryManipulator.addById(c, 2028093, 1, "使用神奇鐵砧 時間: " + DateUtil.getCurrentDate());
                        //MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.EQUIP, skinSlot, (short) 1, false, true);
                        toItem.setItemSkin(skin.getItemSkin() > 0 ? skin.getItemSkin() : skin.getItemId()); //如果外形道具有其他的外形 就用其他的外形 如果沒有就用外形道具
                        chr.forceUpdateItem(toItem);
                        c.announce(InventoryPacket.showSynthesizingMsg(itemId, 2028093, true));
                        used = true;
                        break;
                    } else {
                        c.announce(InventoryPacket.showSynthesizingMsg(itemId, 2028093, false));
                        break;
                    }
                } else if (itemId == 5062500 || itemId == 5062501) { //5062500 - 潛能變化方塊 - 可對附加潛能進行修改的神秘方塊。對原有的潛能不起影響。\n#c附加潛能僅限對B級以上，SS級以下的道具使用#\n#c創造物最高等級: SS級#
                    Item item = chr.getInventory(MapleInventoryType.EQUIP).getItem((short) slea.readInt());
                    if (item != null) {
                        Equip eq = (Equip) item;
                        used = eq.useCube(itemId, chr, 0);
                        if (used) {
                            chr.equipChanged();
                        }
                    }
                    if (item == null || !used) {
                        chr.getMap().broadcastMessage(InventoryPacket.showPotentialReset(chr.getId(), false, itemId, ItemConstants.方塊.getCubeDebris(itemId), 0));
                    }
                    break;
                } else if (itemId == 5060002) { //孵化器 - 為孵化 #c飛天豬的蛋# 的必要裝備.雙擊孵化器可孵化蛋.
                    byte inventory2 = (byte) slea.readInt();
                    short slot2 = (short) slea.readInt();
                    Item item2 = chr.getInventory(MapleInventoryType.getByType(inventory2)).getItem(slot2);
                    if (item2 == null) {
                        return;
                    }
                    chr.dropMessage(1, "暫時無法使用這個道具.");
                } else if (itemId == 5062800 || itemId == 5062801) { // 奇幻傳播者 || 奇幻循環者
                    if (c.getPlayer().getInnerRank() > 0) {
                        chr.resetInnerSkill((byte) 0, itemId, Collections.emptyList(), true, true);
                        c.announce(InventoryPacket.CharacterPotentialResult(chr.getTempInnerSkills(), itemId));
                        used = true;
                    } else {
                        c.getPlayer().dropMessage(1, ii.getName(itemId) + "稀有等級以上才能使用。");
                    }
                } else if (itemId == 5060052) {
                    int chairid;
                    do {
                        chairid = Randomizer.nextInt(5000) + 3010000;
                    } while (ii.getName(chairid) == null);

                    if (chr.getSpace(3) >= 1) {
                        used = true;
                        MapleInventoryManipulator.addById(c, chairid, 1, "通過<椅子工具箱> ItemID: " + itemId + " 獲得時間：" + DateUtil.getNowTime());
                        chr.dropMessage(1, "恭喜你，\n\r你獲得了一個<" + ii.getName(chairid) + ">.");
                    } else {
                        c.getPlayer().dropMessage(5, "背包空間不足。");
                    }
                    break;
                } else if (itemId / 100 == 50645) {
                    short dst = slea.readShort();
                    ItemScrollHandler.UseUpgradeScroll(slot, dst, null, (short) 0, c, chr, 0, slea.readBool(), iType == MapleInventoryType.CASH);
                    return;
                } else {
                    chr.dropMessage(1, "暫時無法使用這個道具.");
                }
                break;
            }
            case 507: {
                if (chr.isAdmin()) {
                    chr.dropMessage(5, "使用商城喇叭 道具類型: " + itemId / 1000 % 10);
                }
                if (chr.getLevel() < 10) {
                    chr.dropMessage(5, "需要等級10級才能使用這個道具.");
                    break;
                }
                if (chr.getMapId() == GameConstants.JAIL) {
                    chr.dropMessage(5, "在這個地方無法使用這個道具.");
                    break;
                }
                if (!chr.getCheatTracker().canSmega() && !chr.isGm()) {
                    chr.dropMessage(5, "你需要等待3秒之後才能使用這個道具.");
                    break;
                }
                if (!c.getChannelServer().getMegaphoneMuteState()) {
                    int msgType = itemId / 1000 % 10;
                    used = true;
                    switch (msgType) {
                        case 0: //藍喇叭
                            chr.getMap().broadcastMessage(MaplePacketCreator.serverNotice(BroadcastMessageType.SPEAKER_CHANNEL, chr.getMedalText() + chr.getName() + " : " + slea.readMapleAsciiString()));
                            break;
                        case 1: //紅喇叭
                            c.getChannelServer().broadcastSmegaPacket(MaplePacketCreator.serverNotice(BroadcastMessageType.SPEAKER_CHANNEL, chr.getMedalText() + chr.getName() + " : " + slea.readMapleAsciiString()));
                            break;
                        case 2: //高質量喇叭
                            WorldBroadcastService.getInstance().broadcastSmega(MaplePacketCreator.serverNotice(BroadcastMessageType.SPEAKER_WORLD, c.getChannel(), chr.getMedalText() + chr.getName() + " : " + slea.readMapleAsciiString(), slea.readByte() != 0));
                            break;
                        case 3: //心臟高級喇叭
                            WorldBroadcastService.getInstance().broadcastSmega(MaplePacketCreator.serverNotice(BroadcastMessageType.HEART_TYPE, c.getChannel(), chr.getMedalText() + chr.getName() + " : " + slea.readMapleAsciiString(), slea.readByte() != 0));
                            break;
                        case 4: //白骨高級喇叭
                            WorldBroadcastService.getInstance().broadcastSmega(MaplePacketCreator.serverNotice(BroadcastMessageType.BONE_TYPE, c.getChannel(), chr.getMedalText() + chr.getName() + " : " + slea.readMapleAsciiString(), slea.readByte() != 0));
                            break;
                        case 6: //道具喇叭
                            String djmsg = chr.getMedalText() + chr.getName() + " : " + slea.readMapleAsciiString();
                            boolean sjEar = slea.readByte() > 0;
                            int showAddObjType = slea.readInt();
                            Item item = null;
                            if (showAddObjType == 1) { //item
                                byte invType = (byte) slea.readInt();
                                short pos = (short) slea.readInt();
                                item = chr.getInventory(MapleInventoryType.getByType(invType)).getItem(pos);
                            } else if (showAddObjType != 0) {
                                chr.dropMessage(1, "暫時不支援展示這個訊息");
                                return;
                            }
                            WorldBroadcastService.getInstance().broadcastSmega(MaplePacketCreator.itemMegaphone(djmsg, sjEar, c.getChannel(), item));
                            break;
                        case 7: //繽紛喇叭
                            byte numLines = slea.readByte();
                            if (numLines < 1 || numLines > 3) {
                                return;
                            }
                            List<String> bfMessages = new LinkedList<>();
                            String bfMsg;
                            for (int i = 0; i < numLines; i++) {
                                bfMsg = slea.readMapleAsciiString();
                                if (bfMsg.length() > 65) {
                                    break;
                                }
                                bfMessages.add(chr.getName() + " : " + bfMsg);
                            }
                            boolean bfEar = slea.readByte() > 0;
                            WorldBroadcastService.getInstance().broadcastSmega(MaplePacketCreator.tripleSmega(bfMessages, bfEar, c.getChannel()));
                            break;
                        case 9: //5079001 - 蛋糕高級喇叭  5079002 - 餡餅高級喇叭
                            WorldBroadcastService.getInstance().broadcastSmega(MaplePacketCreator.serverNotice(itemId == 5079001 ? BroadcastMessageType.CAKE_TYPE : BroadcastMessageType.PIE_TYPE, c.getChannel(), chr.getMedalText() + chr.getName() + " : " + slea.readMapleAsciiString(), slea.readByte() != 0));
                            break;
                    }
                } else {
                    chr.dropMessage(5, "當前頻道禁止使用道具喇叭.");
                }
                break;
            }
            case 508: { //消息 可以點開那種 懸浮在空中
                MapleLove love = new MapleLove(chr, chr.getPosition(), chr.getMap().getFootholds().findBelow(chr.getPosition()).getId(), slea.readMapleAsciiString(), itemId);
                chr.getMap().spawnLove(love);
                used = true;
                break;
            }
            case 509: { //請柬和消息
//                String sendTo = slea.readMapleAsciiString();
//                String msg = slea.readMapleAsciiString();
//                chr.sendNote(sendTo, msg);
//                used = true;
                break;
            }
            case 510: { //音樂盒
                //chr.getMap().broadcastMessage(MaplePacketCreator.musicChange("Jukebox/Congratulation"));
                chr.getMap().startJukebox(chr.getName(), itemId);
                used = true;
                break;
            }
            case 512: {  // v265 天氣
                String ourMsg = slea.readMapleAsciiString();
                chr.getMap().startMapEffect(ourMsg, itemId);
                int buff = ii.getStateChangeItem(itemId);
                if (buff != 0) {
                    for (MapleCharacter mChar : chr.getMap().getCharacters()) {
                        ii.getItemEffect(buff).applyTo(mChar);
                    }
                }
                used = true;
                break;
            }
            case 513: {
                if (itemId / 1000 >= 5130 && itemId / 1000 <= 5131) {
                    DeadDebuff deadDebuff = DeadDebuff.getDebuff(chr, -1);
                    if (deadDebuff == null) {
                        chr.dropMessage(-9, "只有套用角色死亡導致的經驗值獲得、掉落率減少效果時才能使用。");
                        break;
                    }
                    DeadDebuff.cancelDebuff(chr, true);
                    used = true;
                    if (itemId == 5131000) { // VIP護身符
                        MapleItemInformationProvider.getInstance().getItemEffect(2022569).applyTo(chr);
                    }
                }
                break;
            }
            case 515: {
                if (itemId / 1000 >= 5150 && itemId / 1000 <= 5154) {
                    int showLookFlag = slea.readByte();
                    boolean isConfirmChange = slea.readBool();
                    long androidSN = -1;
                    if (showLookFlag == 100)
                        androidSN = slea.readLong();
                    int selectID = -1;
                    int select2ID = -1;
                    if (itemId == 5152300 || ii.isChoice(itemId)) {
                        selectID = slea.readInt();
                        if (showLookFlag == 101)
                            select2ID = slea.readInt();
                    }
                    if (showLookFlag == 100 && (chr.getAndroid() == null || chr.getAndroid().getUniqueId() != androidSN)) {
                        c.announce(MaplePacketCreator.getChangeBeautyResult(itemId, showLookFlag, -1, null));
                        break;
                    }
                    List<Pair<Integer, Integer>> beautyResult = new LinkedList<>();
                    switch (itemId / 1000) {
                        case 5150: // 美髮會員卡
                        case 5154: // 美髮會員卡
                        case 5151: // 染髮會員卡
                            beautyResult.add(new Pair<>(showLookFlag == 100 ? chr.getAndroid().getHair() : showLookFlag == 1 || showLookFlag == 2 ? chr.getSecondHair() : chr.getHair(), 0));
                            if (showLookFlag != 101) break;
                            beautyResult.add(new Pair<>(chr.getSecondHair(), 0));
                            break;
                        case 5152: // 整形券
                            beautyResult.add(new Pair<>(showLookFlag == 100 ? chr.getAndroid().getFace() : showLookFlag == 1 || showLookFlag == 2 ? chr.getSecondFace() : chr.getFace(), 0));
                            if (showLookFlag != 101) break;
                            beautyResult.add(new Pair<>(chr.getSecondFace(), 0));
                            break;
                        case 5153: // 護膚券
                            beautyResult.add(new Pair<>((showLookFlag == 100 ? chr.getAndroid().getSkin() : (int) ((showLookFlag == 1 || showLookFlag == 2 ? chr.getSecondSkinColor() : chr.getSkinColor())) % 1000), 0));
                            if (showLookFlag != 101) break;
                            beautyResult.add(new Pair<>(chr.getSecondSkinColor() % 1000, 0));
                            break;
                    }
                    if (beautyResult.size() <= 0) {
                        c.announce(MaplePacketCreator.getChangeBeautyResult(itemId, showLookFlag, -1, null));
                        break;
                    }

                    int styleID = -1;
                    int style2ID = -1;
                    if (itemId / 1000 == 5151) {
                        // 染髮會員卡
                        if (ii.isChoice(itemId)) {
                            if (ItemConstants.getStyleBaseID(beautyResult.getFirst().getLeft())
                                    == ItemConstants.getStyleBaseID(selectID) && ii.isHairExist(selectID)) {
                                styleID = selectID;
                            }
                            if (showLookFlag == 101) {
                                if (ItemConstants.getStyleBaseID(chr.getSecondHair())
                                        == ItemConstants.getStyleBaseID(select2ID) && ii.isHairExist(select2ID)) {
                                    style2ID = select2ID;
                                }
                            }
                        } else {
                            int hair = beautyResult.getFirst().getLeft();
                            int baseCol = ItemConstants.getStyleColorID(hair);
                            hair = ItemConstants.getStyleBaseID(hair);
                            int randCol = Randomizer.nextInt(8);
                            if (baseCol == randCol) randCol = randCol == 7 ? 0 : (randCol + 1);
                            if (!ii.isHairExist(hair + randCol)) {
                                randCol = -1;
                                for (int i = 0; i < 10; i++) {
                                    if (baseCol != i + i && ii.isHairExist(hair + i)) {
                                        randCol = i;
                                        break;
                                    }
                                }
                            }
                            if (randCol < 0) {
                                c.announce(MaplePacketCreator.getChangeBeautyResult(itemId, showLookFlag, -1, null));
                                break;
                            }
                            styleID = hair + randCol;
                            if (showLookFlag == 101) {
                                hair = chr.getSecondHair();
                                baseCol = ItemConstants.getStyleColorID(hair);
                                hair = ItemConstants.getStyleBaseID(hair);
                                randCol = Randomizer.nextInt(8);
                                if (baseCol == randCol) randCol = randCol == 7 ? 0 : (randCol + 1);
                                if (!ii.isHairExist(hair + randCol)) {
                                    randCol = -1;
                                    for (int i = 0; i < 10; i++) {
                                        if (baseCol != i + i && ii.isHairExist(hair + i)) {
                                            randCol = i;
                                            break;
                                        }
                                    }
                                }
                                if (randCol < 0) {
                                    c.announce(MaplePacketCreator.getChangeBeautyResult(itemId, showLookFlag, -1, null));
                                    break;
                                }
                                style2ID = hair + randCol;
                            }
                        }
                    } else if (itemId / 100 == 51521) {
                        // 隱形眼鏡
                        if (selectID >= 0) {
                            styleID = ItemConstants.getStyleBaseID(beautyResult.getFirst().getLeft()) + (selectID * 100);
                            if (showLookFlag == 101 && select2ID >= 0)
                                style2ID = ItemConstants.getStyleBaseID(beautyResult.get(1).getLeft()) + (select2ID * 100);
                        }
                    } else if (itemId / 100 == 51523) {
                        // 混色瞳孔
                        int sColor = -1;
                        int sColor2 = -1;
                        if (itemId == 5152300) {
                            int bColor = (selectID / 10000) * 100000;
                            int mColor = ((selectID % 10000) / 1000) * 100;
                            int prop = selectID % 100;
                            sColor = bColor + mColor + prop;
                            if (showLookFlag == 101 && select2ID >= 0) {
                                bColor = (select2ID / 10000) * 100000;
                                mColor = ((select2ID % 10000) / 1000) * 100;
                                prop = select2ID % 100;
                                sColor2 = bColor + mColor + prop;
                            }
                        } else {
                            int randCol = Randomizer.nextInt(8);
                            int randMix = Randomizer.nextInt(8);
                            if (randCol == randMix) randMix = randMix == 7 ? 0 : (randMix + 1);
                            sColor = (randCol * 100000) + (randMix * 100) + 50;
                            if (showLookFlag == 101) {
                                randCol = Randomizer.nextInt(8);
                                randMix = Randomizer.nextInt(8);
                                if (randCol == randMix) randMix = randMix == 7 ? 0 : (randMix + 1);
                                sColor2 = (randCol * 100000) + (randMix * 100) + 50;
                            }
                        }
                        if (sColor >= 0)
                            styleID = ItemConstants.changeStyleID(sColor, beautyResult.getFirst().getLeft());
                        if (sColor2 >= 0)
                            style2ID = ItemConstants.changeStyleID(sColor2, beautyResult.get(1).getLeft());
                    } else {
                        if (ii.isChoice(itemId)) {
                            int baseStyle = beautyResult.getFirst().getLeft();
                            int selectBase = ItemConstants.getStyleBaseID(selectID);
                            int baseStyle2 = -1;
                            int select2Base = -1;
                            if (showLookFlag == 101) {
                                baseStyle2 = beautyResult.get(1).getLeft();
                                select2Base = ItemConstants.getStyleBaseID(select2ID);
                            }
                            List<RaffleItem> itemList = RafflePool.getItems(itemId);
                            for (RaffleItem rItem : itemList) {
                                int baseR = ItemConstants.getStyleBaseID(rItem.getItemId());
                                if (baseR == selectBase || (select2Base > 0 && baseR == select2Base)) {
                                    if (styleID == -1 && baseR == selectBase) {
                                        styleID = ItemConstants.changeStyleID(baseStyle, selectID);
                                        RafflePool.gainRaffle(rItem);
                                    }
                                    if (select2Base >= 0 && style2ID == -1 && baseR == select2Base) {
                                        style2ID = ItemConstants.changeStyleID(baseStyle2, select2ID);
                                        RafflePool.gainRaffle(rItem);
                                    }
                                    if (select2Base == -1 || (styleID != -1 && style2ID != -1)) {
                                        break;
                                    }
                                }
                            }
                        } else {
                            RaffleItem gitem = RafflePool.randomItem(itemId, showLookFlag == 100 ? chr.getAndroid().getGender() : showLookFlag == 2 ? 1 : chr.getGender());
                            if (gitem != null) {
                                int rewardID = gitem.getItemId();
                                styleID = ItemConstants.changeStyleID(beautyResult.getFirst().getLeft(), rewardID);
                                if (gitem.isSmega())
                                    WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.gachaponMsg("恭喜" + chr.getName() + "從" + ii.getName(rewardID) + "獲得[" + ii.getName(rewardID) + "]", toUse));
                            }
                            if (showLookFlag == 101) {
                                gitem = RafflePool.randomItem(itemId, 1);
                                if (gitem != null) {
                                    int rewardID = gitem.getItemId();
                                    style2ID = ItemConstants.changeStyleID(beautyResult.get(1).getLeft(), rewardID);
                                    if (gitem.isSmega())
                                        WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.gachaponMsg("恭喜" + chr.getName() + "從" + ii.getName(rewardID) + "獲得[" + ii.getName(rewardID) + "]", toUse));
                                }
                            }
                        }
                    }
                    if (styleID >= 0) {
                        if (ItemConstants.類型.getGender(styleID) == (showLookFlag == 100 ? chr.getAndroid().getGender() : showLookFlag == 2 ? 1 : chr.getGender()) || ItemConstants.類型.getGender(styleID) >= 2) {
                            beautyResult.get(0).right = styleID;
                        } else {
                            styleID = -1;
                        }
                    }
                    if (showLookFlag == 101 && style2ID >= 0) {
                        if (ItemConstants.類型.getGender(style2ID) == 1 || ItemConstants.類型.getGender(style2ID) >= 2) {
                            beautyResult.get(1).right = style2ID;
                        } else {
                            style2ID = -1;
                        }
                    }

                    if (styleID == -1 && (showLookFlag != 101 || style2ID != -1)) {
                        c.announce(MaplePacketCreator.getChangeBeautyResult(itemId, showLookFlag, -1, null));
                        break;
                    }

                    if (showLookFlag == 100) {
                        used = chr.changeAndroidBeauty(styleID);
                    } else {
                        used = chr.changeBeauty(styleID, showLookFlag == 1 || showLookFlag == 2);
                        if (showLookFlag == 101) {
                            used = chr.changeBeauty(style2ID, true) || used;
                        }
                    }

                    if (used) {
                        c.announce(MaplePacketCreator.getChangeBeautyResult(itemId, showLookFlag, androidSN, beautyResult));
                        int charmExp = ii.getIncCharmExp(itemId);
                        if (charmExp > 0) {
                            chr.getTrait(MapleTraitType.charm).addExp(charmExp, chr);
                        }
                    } else {
                        c.announce(MaplePacketCreator.getChangeBeautyResult(itemId, showLookFlag, -1, null));
                    }
                } else if (itemId == 5155000 || itemId == 5155004 || itemId == 5155005) { //卡勒塔的許願珍珠 - 卡勒塔煉製的願望珍珠。\n可以讓尖耳朵#c精靈#變成#c人類面孔#，也可以讓想體驗精靈的#c人類#擁有#c精靈耳朵#。\n想變化新樣子時需要對#c新珍珠#重新許願。\n註：精靈耳朵很帥氣，但#c有時過於突出#，需謹慎。
                    String questInfo = chr.getQuestInfo(GameConstants.精靈耳朵, "sw");
                    questInfo = questInfo == null ? "0" : questInfo;
                    int value = Integer.valueOf(questInfo);
                    used = true;
                    chr.updateOneQuestInfo(GameConstants.精靈耳朵, "sw", GameConstants.getEarValue(itemId, chr.getJob(), value));
                    c.announce(EffectPacket.showJobChanged(-1, chr.getJob()));
                    chr.getMap().broadcastMessage(chr, EffectPacket.showJobChanged(chr.getId(), chr.getJob()), false);
                    chr.modifiedAvatar();
                } else if (itemId == 5156000) { //5156000 - 偉大的變性秘藥 - 使用後角色性別將改變。使用後將變成基礎臉型和基礎髮型。#c但，已婚角色或只有一個性別的職業無法使用。#
                    if (chr.getMarriageId() > 0 || chr.getMarriageRing() != null) {
                        chr.dropSpouseMessage(UserChatMessageType.系統, "已婚人士無法使用。");
                    } else if (JobConstants.is米哈逸(chr.getJob()) || JobConstants.is天使破壞者(chr.getJob())) {
                        chr.dropSpouseMessage(UserChatMessageType.系統, "該職業群無法使用的物品。");
                    } else {
                        chr.setGender(chr.getGender() == 0 ? (byte) 0x01 : 0x00);
                        Pair<Integer, Integer> ret = JobConstants.getDefaultFaceAndHair(chr.getJob(), chr.getGender());
                        Map<MapleStat, Long> statupdate = new EnumMap<MapleStat, Long>(MapleStat.class);
                        chr.setFace(ret.getLeft());
                        chr.setHair(ret.getRight());
                        statupdate.put(MapleStat.臉型, (long) chr.getFace());
                        statupdate.put(MapleStat.髮型, (long) chr.getHair());
                        statupdate.put(MapleStat.性別, (long) chr.getGender());
                        c.announce(MaplePacketCreator.updatePlayerStats(statupdate, chr));
                        c.announce(EffectPacket.showOwnEffectUOL("Effect/BasicEff.img/TransGender", 0, 0));
                        chr.getMap().broadcastMessage(chr, EffectPacket.showEffectUOL(chr.getId(), "Effect/BasicEff.img/TransGender", 0, 0), false);
                        c.announce(MaplePacketCreator.spawnPlayerMapobject(chr));
                        chr.equipChanged();
                        used = true;
                    }
                } else if (itemId == 5155001) {
                    if (JobConstants.is凱撒(chr.getJob())) {
                        used = true;
                        final String questInfo2 = chr.getQuestInfo(1544, "wing");
                        chr.updateOneQuestInfo(1544, "wing", (questInfo2 == null || "0".equals(questInfo2)) ? "1" : "0");
                        chr.modifiedAvatar();
                    } else {
                        chr.dropAlertNotice("當前職業無法使用此道具");
                    }
                } else if (itemId == 5155002) {
                    if (chr.getDecorate() == 1012361) {
                        chr.dropAlertNotice("創角時沒有選擇傑諾標識而無法使用道具。");
                    } else if (JobConstants.is傑諾(chr.getJob())) {
                        used = true;
                        final String keyValue = chr.getKeyValue("FaceMark");
                        chr.setKeyValue("FaceMark", (keyValue == null || "0".equals(keyValue)) ? "1" : "0");
                        chr.modifiedAvatar();
                        c.announce(MaplePacketCreator.CharacterModified(chr, 1L));
                    } else {
                        chr.dropAlertNotice("當前職業無法使用此道具");
                    }
                } else if (itemId == 5155003) {
                    if (chr.getDecorate() == 1012361) {
                        chr.dropAlertNotice("創角色時沒有選擇魔族標識而無法使用道具。");
                    } else if (JobConstants.is惡魔(chr.getJob())) {
                        used = true;
                        final String keyValue2 = chr.getKeyValue("FaceMark");
                        chr.setKeyValue("FaceMark", (keyValue2 == null || "0".equals(keyValue2)) ? "1" : "0");
                        chr.modifiedAvatar();
                        c.announce(MaplePacketCreator.CharacterModified(chr, 1L));
                    } else {
                        chr.dropAlertNotice("當前職業無法使用此道具");
                    }
                } else if (itemId == 5155006) {
                    if (chr.getDecorate() == 1012361) {
                        chr.dropAlertNotice("創建角色時，沒有選擇的亞克的標誌，無法使用道具。");
                    } else if (JobConstants.is亞克(chr.getJob())) {
                        used = true;
                        final String keyValue3 = chr.getKeyValue("FaceMark");
                        chr.setKeyValue("FaceMark", (keyValue3 == null || "0".equals(keyValue3)) ? "1" : "0");
                        chr.modifiedAvatar();
                        c.announce(MaplePacketCreator.CharacterModified(chr, 1L));
                    } else {
                        chr.dropAlertNotice("當前職業無法使用此道具");
                    }
                } else {
                    chr.dropMessage(1, "暫不支持這個道具的使用.");
                }
                break;
            }
            case 517: { //寵物取名
                int uniqueid = (int) slea.readLong();
                MaplePet pet = null;
                Item item = chr.getInventory(MapleInventoryType.CASH).findByLiSN(uniqueid);
                if (item == null) {
                    break;
                }
                if (!ItemConstants.類型.寵物(item.getItemId())) {
                    break;
                }
                pet = item.getPet();
                if (pet == null) {
                    chr.dropMessage(1, "寵物改名錯誤，找不到寵物的信息.");
                    break;
                }
                String nName = slea.readMapleAsciiString();
                for (String z : GameConstants.RESERVED) {
                    if (pet.getName().contains(z) || nName.contains(z)) {
                        break;
                    }
                }
                if (MapleCharacterUtil.canChangePetName(nName)) {
                    pet.setName(nName);
                    pet.saveToDb();
                    chr.petUpdateStats(pet, true);
                    c.sendEnableActions();
                    chr.getMap().broadcastMessage(PetPacket.changePetName(chr, nName, pet.getInventoryPosition()));
                    used = true;
                }
                break;
            }
            case 519: { //寵物取消增加功能
                int uniqueid = (int) slea.readLong();
                if (itemId >= 5190000 && itemId <= 5190013 && itemId != 5190014) { //增加寵物的一些功能
                    Item item = chr.getInventory(MapleInventoryType.CASH).findByLiSN(uniqueid);
                    if (item == null) {
                        break;
                    }
                    if (!ItemConstants.類型.寵物(item.getItemId())) {
                        break;
                    }
                    MaplePet pet = item.getPet();
                    if (pet == null) {
                        chr.dropMessage(1, "找不到寵物的信息.");
                        break;
                    }
                    PetFlag petFlag = PetFlag.getByAddId(itemId);
                    if (petFlag != null && !petFlag.check(pet.getFlags())) {
                        pet.setFlags(pet.getFlags() | petFlag.getValue());
                        pet.saveToDb();
                        chr.petUpdateStats(pet, true);
                        c.sendEnableActions();
                        c.announce(MTSCSPacket.changePetFlag(uniqueid, true, petFlag.getValue()));
                        used = true;
                    }
                    break;
                } else if ((itemId >= 5191000 && itemId <= 5191004) || itemId == 5190014) { //取消寵物的一些功能
                    Item item = chr.getInventory(MapleInventoryType.CASH).findByLiSN(uniqueid);
                    if (item == null) {
                        break;
                    }
                    if (!ItemConstants.類型.寵物(item.getItemId())) {
                        break;
                    }
                    MaplePet pet = item.getPet();
                    if (pet == null) {
                        chr.dropMessage(1, "找不到寵物的信息.");
                        break;
                    }
                    PetFlag petFlag = PetFlag.getByDelId(itemId);
                    if (petFlag != null && petFlag.check(pet.getFlags())) {
                        pet.setFlags(pet.getFlags() - petFlag.getValue());
                        pet.saveToDb();
                        chr.petUpdateStats(pet, true);
                        c.sendEnableActions();
                        c.announce(MTSCSPacket.changePetFlag(uniqueid, false, petFlag.getValue()));
                        used = true;
                    }
                } else if (itemId == 5192000) {
                    Item item = chr.getInventory(MapleInventoryType.CASH).findByLiSN(uniqueid);
                    if (item == null) {
                        break;
                    }
                    if (!ItemConstants.類型.寵物(item.getItemId())) {
                        break;
                    }
                    MaplePet pet = item.getPet();
                    if (pet == null) {
                        chr.dropMessage(1, "將要接受訓練的寵物放上去。");
                        break;
                    }
                    if (pet.getSummoned()) {
                        chr.dropMessage(1, "安裝中的寵物無法訓練。");
                        break;
                    }
                    pet.setAddSkill(Randomizer.nextInt(6) + 1);
                    pet.saveToDb();
                    c.announce(InventoryPacket.updatePet(pet, chr.getInventory(MapleInventoryType.CASH).getItem(pet.getInventoryPosition()), pet.getSummoned()));
                    c.announce(InventoryPacket.petAddSkillEffect(chr, pet));
                    c.getPlayer().dropMessage(-11, "成功強化寵物:" + pet.getName() + ", 寵物等級:" + pet.getLevel() + ".");
                    c.sendEnableActions();
                    used = true;
                    break;
                }
                break;
            }
            case 520: { //楓幣包和豆豆箱子
                if (itemId >= 5200000 && itemId <= 5200008) {
                    if (chr.isIntern()) {
                        int mesars = ii.getMeso(itemId);
                        if (mesars > 0 && chr.getMeso() < (Integer.MAX_VALUE - mesars)) {
                            used = true;
                            if (Math.random() > 0.1) {
                                int gainmes = Randomizer.nextInt(mesars);
                                chr.gainMeso(gainmes, false);
                                c.announce(MTSCSPacket.sendMesobagSuccess(gainmes));
                            } else {
                                c.announce(MTSCSPacket.sendMesobagFailed());
                            }
                        } else {
                            chr.dropMessage(1, "楓幣已達到上限無法使用這個道具.");
                        }
                    } else {
                        AutobanManager.getInstance().autoban(chr.getClient(), "使用非法道具.");
                    }
                    break;
                } else {
                    chr.dropMessage(5, "暫時無法使用這個道具.");
                }
                break;
            }
            case 523: { //商店搜索器
                int itemSearch = slea.readInt();
                List<HiredMerchant> hms = c.getChannelServer().searchMerchant(itemSearch);
                if (hms.size() > 0) {
                    c.announce(MaplePacketCreator.getOwlSearched(itemSearch, hms));
                    used = true;
                } else {
                    chr.dropMessage(1, "沒有找到這個道具.");
                }
                MapleCharacterUtil.addToItemSearch(itemSearch);
                break;
            }
            case 524: { //寵物食品
                MaplePet pet = null;
                MaplePet[] pets = chr.getSpawnPets();
                for (int i = 0; i < 3; i++) {
                    if (pets[i] != null && (pets[i].canConsume(itemId) || itemId == 5249000)) {
                        pet = pets[i];
                        break;
                    }
                }
                if (pet == null) {
                    chr.dropMessage(1, "沒有可以餵食的寵物。\r\n請重新確認。");
                    break;
                }
                byte petIndex = chr.getPetIndex(pet);
                pet.setFullness(100);
                if (pet.getCloseness() < 30000) {
                    pet.setCloseness(Math.min(itemId == 5249000 ? pet.getCloseness() + 100 : pet.getCloseness() + (100 * ServerConfig.CHANNEL_RATE_TRAIT), 30000));
                    while (pet.getCloseness() >= GameConstants.getClosenessNeededForLevel(pet.getLevel() + 1)) {
                        pet.setLevel(pet.getLevel() + 1);
                        c.announce(EffectPacket.showOwnPetLevelUp(chr.getPetIndex(pet)));
                        chr.getMap().broadcastMessage(PetPacket.showPetLevelUp(chr, petIndex));
                    }
                }
                chr.petUpdateStats(pet, true);
                chr.getMap().broadcastMessage(chr, PetPacket.commandResponse(chr.getId(), (byte) 1, petIndex, true, true), true);
                used = true;
                break;
            }
            case 528: { //傳說中的臭屁
                Rectangle bounds = new Rectangle((int) chr.getPosition().getX(), (int) chr.getPosition().getY(), 1, 1);
                MapleAffectedArea mist = new MapleAffectedArea(bounds, chr);
                chr.getMap().spawnAffectedArea(mist, 10000, true);
                c.sendEnableActions();
                used = true;
                break;
            }
            case 532: {
                String name = slea.readMapleAsciiString();
                String otherName = slea.readMapleAsciiString();
                slea.readInt();
                slea.readInt();
                int cardId = slea.readByte();
                PredictCardFactory pcf = PredictCardFactory.getInstance();
                PredictCard Card = pcf.getPredictCard(cardId);
                int commentId = Randomizer.nextInt(pcf.getCardCommentSize());
                PredictCardComment Comment = pcf.getPredictCardComment(commentId);
                if (Card != null && Comment != null) {
                    chr.dropMessage(5, "占卜只是隨便寫的，占卜結果就當個玩笑看看。");
                    int love = Randomizer.rand(1, Comment.score) + 5;
                    c.announce(MaplePacketCreator.showPredictCard(name, otherName, love, cardId, commentId));
                    used = true;
                }
                break;
            }
            case 537: { //黑板
                chr.setChalkboard(slea.readMapleAsciiString());
                break;
            }
            case 539: { //情景喇叭
                if (chr.getLevel() < 10) {
                    chr.dropMessage(5, "需要等級10級才能使用這個道具.");
                    break;
                }
                if (chr.getMapId() == GameConstants.JAIL) {
                    chr.dropMessage(5, "當前地圖無法使用這個道具.");
                    break;
                }
                if (!chr.getCheatTracker().canAvatarSmega()) {
                    chr.dropMessage(5, "你需要等待6秒之後才能使用這個道具.");
                    break;
                }
                if (!c.getChannelServer().getMegaphoneMuteState()) {
                    List<String> messages = new LinkedList<>();
                    for (int i = 0; i < 4; i++) {
                        messages.add(slea.readMapleAsciiString());
                    }
                    boolean ear = slea.readByte() != 0;
                    WorldBroadcastService.getInstance().broadcastSmega(MaplePacketCreator.getAvatarMega(chr, c.getChannel(), itemId, messages, ear));
                    used = true;
                } else {
                    chr.dropMessage(5, "當前頻道禁止使用情景喇叭.");
                }
                break;
            }
            case 545: {

                if (ServerConstants.isBlockedMapFM(chr.getMapId())) {
                    chr.dropMessage(5, "當前地圖無法使用此道具.");
                    c.sendEnableActions();
                    return;
                }
                if (chr.getLevel() < 10) {
                    chr.dropMessage(5, "只有等級達到10級才可以使用此道具.");
                } else if (chr.hasBlockedInventory() || chr.checkEvent() || chr.getMap().getEMByMap(chr) || chr.getMapId() >= 990000000) {
                    chr.dropMessage(5, "當前地圖無法使用此道具.");
                } else if ((chr.getMapId() >= 680000210 && chr.getMapId() <= 680000502) || (chr.getMapId() / 1000 == 980000 && chr.getMapId() != 980000000) || (chr.getMapId() / 100 == 1030008) || (chr.getMapId() / 100 == 922010) || (chr.getMapId() / 10 == 13003000)) {
                    chr.dropMessage(5, "當前地圖無法使用此道具.");
                } else /*
                 * 編號:　5450000 名稱:　包裹商人妙妙
                 * 編號:　5450001 名稱:　金先生直通電話
                 * 編號:　5450003 名稱:　新手包裹商人妙妙
                 * 編號:　5450004 名稱:　包裹商人妙妙（30天）
                 * 編號:　5450005 名稱:　移動倉庫（30天）
                 * 編號:　5450006 名稱:　[1天]包裹商人妙妙
                 * 編號:　5450007 名稱:　[7天]包裹商人妙妙
                 * 編號:　5450008 名稱:　[1天]移動倉庫王先生
                 * 編號:　5450009 名稱:　[7天]移動倉庫王先生
                 * 編號:　5450010 名稱:　貓咪商人奈落
                 */ if (itemId == 5451001) { //編號:　5451001  名稱:　新手轉蛋券
                    chr.dropMessage(1, "暫時無法使用這個道具.");
                } else if (itemId == 5450001 || itemId == 5450005 || itemId == 5450008 || itemId == 5450009) { //移動倉庫
                    chr.setConversation(ConversationType.ON_TRUNK);
                    chr.getTrunk().secondPwdRequest(c, itemId == 5450001 ? 1002005 : 1022005);
                } else if (itemId == 5450010) { //貓咪商人奈落 高級雜貨商店
                    MapleShop shop = MapleShopFactory.getInstance().getShop(9090100);
                    if (shop == null) {
                        chr.dropMessage(1, "這個商店暫時無法使用。(code:9090100)");
                    } else {
                        shop.sendItemShop(c, itemId);
                        used = true;
                    }
                } else {
                    MapleShop shop = MapleShopFactory.getInstance().getShop(9090000);
                    if (shop == null) {
                        chr.dropMessage(1, "這個商店暫時無法使用。(code:9090000)");
                    } else {
                        shop.sendItemShop(c, itemId);
                        used = true;
                    }
                }
                break;
            }
            case 550: {
                if (itemId == 5500003) { //佳佳變身藥水
                    chr.dropMessage(1, "暫時無法使用這個道具.");
                    break;
                } else if (itemId == 5501001 || itemId == 5501002) { //魔法絲線
                    Skill skil = SkillFactory.getSkill(slea.readInt());
                    if (skil == null || skil.getId() / 10000 != 8000 || chr.getSkillLevel(skil) <= 0 || !skil.isTimeLimited() || GameConstants.getMountItem(skil.getId(), chr) <= 0) {
                        break;
                    }
                    long toAdd = (itemId == 5501001 ? 30 : 60) * 24 * 60 * 60 * 1000L;
                    long expire = chr.getSkillExpiry(skil);
                    if (expire < System.currentTimeMillis() || expire + toAdd >= System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000L)) {
                        break;
                    }
                    chr.changeSingleSkillLevel(skil, chr.getSkillLevel(skil), chr.getMasterLevel(skil), expire + toAdd);
                    used = true;
                    break;
                } else if (itemId >= 5500000 && itemId <= 5500006) { //魔法沙漏
                    Short slots = slea.readShort();
                    if (slots == 0) {
                        chr.dropMessage(1, "請該道具點在你需要延長時間的道具上.");
                        break;
                    }
                    Item item = chr.getInventory(MapleInventoryType.EQUIPPED).getItem(slots);
                    long days = 0;
                    if (itemId == 5500000) { //魔法沙漏
                        days = 1;
                    } else if (itemId == 5500001) { //魔法沙漏（7天）
                        days = 7;
                    } else if (itemId == 5500002) { //魔法沙漏（20天）
                        days = 20;
                    } else if (itemId == 5500004) { //魔法沙漏（30天）
                        days = 30;
                    } else if (itemId == 5500005) { //魔法沙漏（50天）
                        days = 50;
                    } else if (itemId == 5500006) { //魔法沙漏（99天）
                        days = 99;
                    }
                    if (item != null && !ItemConstants.類型.飾品(item.getItemId()) && item.getExpiration() > -1 && !ii.isCash(item.getItemId()) && System.currentTimeMillis() + (100 * 24 * 60 * 60 * 1000L) > item.getExpiration() + (days * 24 * 60 * 60 * 1000L)) {
                        boolean change = true;
                        for (String z : GameConstants.RESERVED) {
                            if (chr.getName().contains(z) || item.getOwner().contains(z)) {
                                change = false;
                                break;
                            }
                        }
                        if (change && days > 0) {
                            item.setExpiration(item.getTrueExpiration() + (days * 24 * 60 * 60 * 1000));
                            chr.forceUpdateItem(item);
                            used = true;
                            break;
                        } else {
                            chr.dropMessage(1, "無法使用在這個道具上.");
                        }
                    } else {
                        chr.dropMessage(1, "使用道具出現錯誤.");
                    }
                } else {
                    chr.dropMessage(1, "暫時無法使用這個道具.");
                }
                break;
            }
            case 552: {
                if (itemId == 5521000) { //轉存吊牌
                    MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
                    Item item = chr.getInventory(type).getItem((short) slea.readInt());
                    if (item != null && !ItemAttribute.Seal.check(item.getAttribute()) && !ItemAttribute.AccountSharable.check(item.getCAttribute()) && !ItemAttribute.CutUsed.check(item.getAttribute())) {
                        if (ii.isShareTagEnabled(item.getItemId())) {
                            int flag = item.getAttribute();
                            flag |= ItemAttribute.TradeBlock.getValue();
                            flag |= ItemAttribute.TradeOnce.getValue();

                            if (type == MapleInventoryType.EQUIP || type == MapleInventoryType.DECORATION) {
                                flag |= ItemAttribute.AccountSharable.getValue();
                            }

                            item.setAttribute(flag);
                            chr.forceUpdateItem(item);
                            used = true;
                            break;
                        }
                    }
                } else if (itemId == 5520000 || itemId == 5520001 || itemId == 5520002 || itemId == 5520003) { //宿命剪刀
                    MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
                    Item item = chr.getInventory(type).getItem((short) slea.readInt());
                    if (item == null || ItemAttribute.TradeOnce.check(item.getCAttribute()) || ItemAttribute.AccountSharable.check(item.getCAttribute()) || ItemAttribute.CutUsed.check(item.getAttribute())) {
                        break;
                    }

                    boolean allowKarma = false;
                    switch (itemId) {
                        case 5520000:
                            allowKarma = ii.isTradeAvailable(item.getItemId()) && !ItemAttribute.AnimaCube.check(item.getCAttribute());
                            break;
                        case 5520001:
                            allowKarma = ii.isPKarmaEnabled(item.getItemId()) || ItemAttribute.AnimaCube.check(item.getCAttribute());
                            break;
                        case 5520002:
                            allowKarma = ii.isCash(item.getItemId());
                            break;
                        case 5520003:
                            allowKarma = ItemConstants.類型.寵物(item.getItemId());
                            break;
                    }
                    if (allowKarma) {
                        int flag = item.getAttribute();
                        if (item instanceof Equip) {
                            short karmaCount = ((Equip) item).getKarmaCount();
                            if (karmaCount == -1) {
                                karmaCount = ServerConfig.DEFAULT_CUTTABLE;
                            }
                            if (karmaCount == 0) {
                                chr.dropAlertNotice("該道具使用剪刀次數已達上限");
                                break;
                            } else if (karmaCount > 0) {
                                ((Equip) item).setKarmaCount((short) (karmaCount - 1));
                                chr.dropSpouseMessage(UserChatMessageType.系統, "[" + item.getName() + "] 剩餘可剪刀次數:" + karmaCount);
                            }
                        }
                        flag |= ItemAttribute.TradeBlock.getValue();
                        flag |= ItemAttribute.TradeOnce.getValue();
                        if (ItemAttribute.AnimaCube.check(flag)) {
                            flag &= ~ItemAttribute.AnimaCube.getValue();
                        }

                        if (type == MapleInventoryType.EQUIP || type == MapleInventoryType.DECORATION) {
                            flag |= ItemAttribute.CutUsed.getValue();
                        }
                        item.setAttribute(flag);
                        chr.forceUpdateItem(item);
                        used = true;
                        break;
                    }
                }
                break;
            }
            case 553: {
                if (CashItemFactory.getInstance().hasRandomItem(itemId)) {
                    InventoryHandler.UseRewardBox(slot, itemId, c, chr);
                } else if (itemId == 5537000) { // 萌獸卡牌包
                    if (chr.getSpace(2) < 3) {
//                        chr.send(FamiliarPacket.errorMessage(chr.getId(), (byte) 0));
                        chr.dropMessage(1, "請先空出消耗欄3格，再試一次");
                        break;
                    }
                    InventoryHandler.UseFamiliarCard(chr, 1, 4, true);
                    used = true;
                } else {
                    if (!InventoryHandler.UseRewardItem(slot, itemId, c, chr)) {
//                        chr.dropMessage(1, "該道具無法使用.道具ID: " + itemId);
                    }
                }
                break;
            }
            case 555: {
                if (itemId == 5551000) {
                    String count = chr.getOneInfo(56829, "count");
                    final int damskinslot = count == null ? ServerConfig.defaultDamageSkinSlot : Integer.valueOf(count);
                    if (damskinslot < GameConstants.DamageSkinSlotMax) {
                        chr.updateOneInfo(56829, "count", String.valueOf(damskinslot + 1));
                        chr.send(InventoryPacket.UserDamageSkinSaveResult(2, 4, chr));
                        chr.dropMessage(1, "傷害皮膚擴充成功，當前有：" + (damskinslot + 1) + " 格");
                        used = true;
                    } else {
                        chr.dropMessage(1, "傷害皮膚擴充失敗，欄位已超過上限。");
                    }
                } else {
                    chr.dropMessage(1, "該道具無法使用.道具ID: " + itemId);
                }
                break;
            }
            /*
            case 557: { //金錘子
                used = true;
                slea.readInt(); // Inventory type, Hammered eq is always EQ.
                short pos = slea.readShort();
                slea.readByte();
                boolean suc = Randomizer.isSuccess(70); // 商城的金錘子成功率70%
                if (JobConstants.is神之子(chr.getJob()) && pos == -10) {
                    Equip item = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(pos);
                    if (suc && item != null) {
                        if (ii.getSlots(item.getItemId()) > 0 && item.getViciousHammer() < 2) {
                            item.setViciousHammer((byte) (item.getViciousHammer() + 1));
                            item.setRestUpgradeCount((byte) (item.getRestUpgradeCount() + 1));
                            chr.forceUpdateItem(item);
                        } else {
                            chr.dropMessage(1, "無法使用在這個道具上。");
                        }
                    }
                    c.announce(MTSCSPacket.sendGoldHammerResult(0, suc ? 0 : 1));
                    break;
                }
                c.announce(MTSCSPacket.sendPlatinumHammerResult(true, suc, pos));
                break;
            }
             */
            case 561: {
                MapleInventoryType eqpType = MapleInventoryType.getByType((byte) slea.readInt());
                short dst = (short) slea.readInt();
                MapleInventoryType useType = MapleInventoryType.getByType((byte) slea.readInt());
                short toSlot = (short) slea.readInt();
                short ws = (short) slea.readInt();
                chr.updateTick(slea.readInt());
                ItemScrollHandler.UseUpgradeScroll(toSlot, dst, eqpType, ws, c, chr, itemId, false, useType == MapleInventoryType.CASH);
                used = true;
                break;
            }
            case 562: { //技能書
                if (InventoryHandler.UseSkillBook(slot, itemId, c, chr)) {
                    chr.gainSP(1);
                }
                break;
            }
            case 570: { //智能機器人取名
                slea.skip(8);
                if (chr.getAndroid() == null) {
                    break;
                }
                String nName = slea.readMapleAsciiString();
                for (String z : GameConstants.RESERVED) {
                    if (chr.getAndroid().getName().contains(z) || nName.contains(z)) {
                        break;
                    }
                }
                if (MapleCharacterUtil.canChangePetName(nName)) {
                    chr.getAndroid().setName(nName);
                    chr.getAndroid().saveToDb(); //保存下機器人的數據
                    chr.setAndroid(chr.getAndroid()); //重新召喚機器人
                    used = true;
                }
                break;
            }
            case 579: {
                if (itemId == 5790000) {
                    int slots = c.getAccCardSlots();
                    if (c.gainAccCardSlot()) {
                        chr.dropMessage(1, "卡牌擴充成功，當前欄位: " + (slots + 1));
                        used = true;
                    } else {
                        chr.dropMessage(1, "卡牌擴充失敗，欄位已超過上限。");
                    }
                } else {
                    chr.dropMessage(1, "該道具無法使用.");
                }
                break;
            }
            case 580: {
                if (JobConstants.is天使破壞者(chr.getJob())) {
                    int newLongcoat = 1051294 + itemId % 10;
                    used = true;
                    chr.setKeyValue("Longcoat", String.valueOf(newLongcoat));
                    chr.send(MaplePacketCreator.DressUpInfoModified(chr));
                    break;
                }
                chr.dropMessage(1, "該道具只有天使破壞者可以使用。");
                break;
            }
            case 584: {
                int rewardItemId = 0;
                int needItemId = 0;
                int[] needItemIds = new int[]{4310233};
                switch (itemId) {
                    case 5840007: {  // 椅子專用萬花筒鑰匙
                        int randItemId = Randomizer.nextInt(5000) + 3010000;
                        while (ii.getName(randItemId) == null) {
                            randItemId = Randomizer.nextInt(5000) + 3010000;
                        }
                        needItemId = needItemIds[Randomizer.nextInt(needItemIds.length)];
                        if (chr.getInventory(MapleInventoryType.SETUP).getNumFreeSlot() >= 2 && chr.canHold(needItemId)) {
                            used = true;
                            rewardItemId = randItemId;
                            chr.removeItem(4009440, 1);
                            break;
                        }
                        needItemId = 0;
                        c.getPlayer().dropMessage(5, "背包空間不足。");
                        break;
                    }
                    case 5840006: { // 傷害皮膚專用萬花筒鑰匙
                        List<Integer> arrayList = new ArrayList<>();
                        arrayList.addAll(ii.getDamageSkinBox().keySet());
                        needItemId = needItemIds[Randomizer.nextInt(needItemIds.length)];
                        int randItemId = arrayList.get(Randomizer.nextInt(arrayList.size()));
                        if (chr.getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 2 && chr.canHold(needItemId)) {
                            used = true;
                            rewardItemId = randItemId;
                            chr.removeItem(4009440, 1);
                            break;
                        }
                        needItemId = 0;
                        c.getPlayer().dropMessage(5, "背包空間不足。");
                        break;
                    }
                    case 5840004: { // 萌獸卡牌包鑰匙
                        if (chr.getSpace(2) >= 3) {
                            InventoryHandler.UseFamiliarCard(chr, 1, 4, true);
                            used = true;
                            break;
                        }
                        chr.dropMessage(1, "請確認消耗窗口有3個空欄!");
                    }
                }
                if (rewardItemId > 0) {
                    c.announce(MaplePacketCreator.getPeanutResult(rewardItemId, (short) 1, itemId, slot, needItemId, (short) 1));
                    MapleInventoryManipulator.addById(c, rewardItemId, 1, ii.getName(itemId) + " 在 " + DateUtil.getCurrentDate());
                    if (needItemId > 0) {
                        MapleInventoryManipulator.addById(c, needItemId, 1, ii.getName(itemId) + " 在 " + DateUtil.getCurrentDate());
                    } else {
                        break;
                    }
                }
                break;
            }
            default:
                log.warn("使用未處理的商城道具 : " + itemId + " 封包：" + slea.toString(true));
                break;
        }
        if (itemType == 506) {
            //c.announce(MaplePacketCreator.showScrollTip(used));
        }
        if (used) {
            MapleInventoryManipulator.removeFromSlot(c, iType, slot, (short) 1, false, true);
        }
        c.sendEnableActions();
        if (cc) {
            if (!chr.isAlive() || chr.checkEvent() || FieldLimitType.MIGRATELIMIT.check(chr.getMap().getFieldLimit())) {
                chr.dropMessage(1, "刷新人物數據失敗.");
                return;
            }
            chr.dropMessage(5, "正在刷新人數據.請等待...");
            chr.fakeRelog();
            if (chr.getScrolledPosition() != 0) {
                c.announce(MaplePacketCreator.pamSongUI());
            }
        }
    }

    private static int getIncubatedItem(MapleCharacter chr, int itemId, int effectId) {
        RaffleItem gitem = RafflePool.randomItem(itemId);
        if (gitem == null) {
            return -1;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final Item item = MapleInventoryManipulator.addbyId_Gachapon(chr.getClient(), gitem.getItemId(), 1, "從" + ii.getName(itemId) + "抽取 時間 " + DateUtil.getCurrentDate());
        if (item == null) {
            return -1;
        }

        if (gitem.isSmega()) {
            WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.gachaponMsg("恭喜" + chr.getName() + "從" + ii.getName(itemId) + "獲得{" + (item != null ? ii.getName(item.getItemId()) : "") + "}", item));
            chr.send(EffectPacket.showIncubatorEffect(-1, effectId, "Effect/BasicEff/Event1/Best"));
            chr.getMap().broadcastMessage(chr, EffectPacket.showIncubatorEffect(chr.getId(), effectId, "Effect/BasicEff/Event1/Best"), false);
        }
        return item.getItemId();
    }

    public static void TapJoyResponse(final MaplePacketReader lea, final MapleCharacter player) {
        final int slot = lea.readInt();
        final int itemId = lea.readInt();

        if (player.getItemQuantity(itemId) > 0) {
            player.send(InventoryPacket.showTapJoyInfo(slot, itemId));
        } else {
            player.sendEnableActions();
        }
    }

    public static void TapJoyDone(final MaplePacketReader lea, final MapleCharacter player) {
        final int rank = lea.readInt();
        final int slot = lea.readInt();
        final int itemid = lea.readInt();
        final int hammerid = rank < 2 ? 5840000 + rank + 1 : 0;
        final List<Integer> mods = new ArrayList<>(ItemConstants.TapJoyReward.getRewardList(rank));
        Collections.shuffle(mods);
        final int rewardItemId = mods.get(Randomizer.nextInt(mods.size()));
        final int intValue3 = rank < 2 ? itemid + 1 : 0;
        final Item item = player.getInventory(MapleInventoryType.ETC).getItem((short) slot);
        int mode = 3;
        int nextBoxSlot = 0;
        if (!player.haveItem(5840000 + rank, 1) || player.getInventory(MapleInventoryType.EQUIP).isFull() || player.getInventory(MapleInventoryType.USE).isFull() || player.getInventory(MapleInventoryType.SETUP).isFull() || player.getInventory(MapleInventoryType.ETC).isFull() || player.getInventory(MapleInventoryType.CASH).isFull() || player.getInventory(MapleInventoryType.DECORATION).isFull()) {
            mode = 4;
        } else if (item != null) {
            final short amount = 1;
            player.removeItem(itemid, 1);
            player.removeItem(5840000 + rank, 1);
            final ArrayList<ModifyInventory> list = new ArrayList<>();
            if (intValue3 <= 0) {
                mode = 0;
            } else {
                mode = 1;
                nextBoxSlot = MapleInventoryManipulator.addId(player.getClient(), itemid + 1, 1, "", "敲敲樂下一階段獲得！");
            }
            player.gainItem(rewardItemId, amount, "敲敲樂第" + rank + 1 + "階段獲得");
            player.send(InventoryPacket.showTapJoy(rewardItemId));
            player.send(EffectPacket.getShowItemGain(rewardItemId, amount, true));
            player.send(InventoryPacket.modifyInventory(true, list, player));
        }
        player.send(InventoryPacket.showTapJoyDone(mode, itemid, intValue3, nextBoxSlot, hammerid));
    }

    public static void TapJoyNextStage(final MaplePacketReader lea, final MapleCharacter player) {
        final int int1 = lea.readInt();
        final int int2 = lea.readInt();
        final int itemid = lea.readInt();
        final byte byte1 = lea.readByte();
        final int intValue = ItemConstants.TapJoyReward.getItemIdAndSN(ItemConstants.TapJoyReward.getSN(itemid) + 1).getLeft();
        int n = 8;
        final Item item = player.getInventory(MapleInventoryType.CASH).getItem((short) int2);
        final ArrayList<ModifyInventory> mods = new ArrayList<>();
        if (item != null && player.modifyCSPoints(byte1, -(int1 + 1) * 350, false)) {
            if (intValue <= 0) {
                player.getInventory(ItemConstants.getInventoryType(item.getItemId())).removeItem(item.getPosition(), (short) 1, false);
                mods.add(new ModifyInventory(1, item));
                mods.add(new ModifyInventory(3, item));
            } else {
                n = 7;
                item.setItemId(intValue);
                mods.add(new ModifyInventory(0, item));
            }
            player.send(InventoryPacket.modifyInventory(true, mods, player));
            player.send(InventoryPacket.showTapJoy(0));
        }
        player.send(InventoryPacket.showTapJoyNextStage(player, n, int2, intValue, byte1));
    }
}
