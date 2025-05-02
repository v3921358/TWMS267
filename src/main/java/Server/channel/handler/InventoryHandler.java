package Server.channel.handler;

import Client.*;
import Client.inventory.*;
import Client.skills.Skill;
import Client.skills.SkillFactory;
import Config.configs.ServerConfig;
import Config.constants.GameConstants;
import Config.constants.ItemConstants;
import Config.constants.JobConstants;
import Config.constants.SkillConstants;
import Config.constants.enums.UserChatMessageType;
import Config.constants.skills.狂狼勇士;
import Config.constants.skills.龍魔導士;
import Net.server.*;
import Net.server.buffs.MapleStatEffect;
import Net.server.cashshop.CashItemFactory;
import Net.server.life.MapleLifeFactory;
import Net.server.life.MapleMonster;
import Net.server.maps.*;
import Net.server.quest.MapleQuest;
import Net.server.shops.HiredMerchant;
import Net.server.shops.IMaplePlayerShop;
import Opcode.Headler.OutHeader;
import Packet.*;
import Server.world.WorldBroadcastService;
import SwordieX.client.party.PartyMember;
import connection.packet.OverseasPacket;
import SwordieX.overseas.extraequip.ExtraEquipResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.DateUtil;
import tools.Pair;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;

public class InventoryHandler {

    private static final Logger log = LoggerFactory.getLogger(InventoryHandler.class);

    public static final int OWL_ID = 1; //don't change. 0 = owner ID, 1 = store ID, 2 = object ID

    /*
     * 道具移動
     */
    public static void ItemMove(MaplePacketReader slea, MapleClient c) {
        if (c.getPlayer() == null || c.getPlayer().hasBlockedInventory()) { //hack
            return;
        }
        slea.readInt();
        MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
        short src = slea.readShort(); //背包位置
        short dst = slea.readShort(); //裝備欄位置
        short quantity = slea.readShort();
        if (src < 0 && dst > 0) {
            MapleInventoryManipulator.unequip(c, src, dst);
        } else if (dst < 0) {
            MapleInventoryManipulator.equip(c, type, src, dst);
        } else if (dst == 0) {
            MapleInventoryManipulator.drop(c, type, src, quantity);
        } else {
            MapleInventoryManipulator.move(c, type, src, dst);
        }
    }

    /*
     * 小背包裡面的道具移動
     */
    public static void SwitchBag(MaplePacketReader slea, MapleClient c) {
        if (c.getPlayer().hasBlockedInventory()) { //hack
            return;
        }
        c.getPlayer().setScrolledPosition((short) 0);
        c.getPlayer().updateTick(slea.readInt());
        short src = (short) slea.readInt();
        short dst = (short) slea.readInt();
        if (src < 100 || dst < 100) {
            return;
        }
        MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
        if (type.getType() < 2 || type.getType() > 4) {
            c.sendEnableActions();
            return;
        }
        MapleInventoryManipulator.move(c, type, src, dst);
    }

    /*
     * 小背包道具到道具欄
     */
    public static void MoveBag(MaplePacketReader slea, MapleClient c) {
        if (c.getPlayer().hasBlockedInventory()) { //hack
            return;
        }
        c.getPlayer().setScrolledPosition((short) 0);
        c.getPlayer().updateTick(slea.readInt());
        boolean srcFirst = slea.readInt() > 0;
        MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
        if (type.getType() < 2 || type.getType() > 4) {
            c.sendEnableActions();
            return;
        }
        short dst = (short) slea.readInt();
        short src = slea.readShort();
        MapleInventoryManipulator.move(c, type, srcFirst ? dst : src, srcFirst ? src : dst);
    }

    /*
     * 道具集合
     */
    public static void ItemGather(MaplePacketReader slea, MapleClient c) {
        c.getPlayer().updateTick(slea.readInt());
        c.getPlayer().setScrolledPosition((short) 0);
        byte mode = slea.readByte();
        MapleInventoryType pInvType = MapleInventoryType.getByType(mode);
        if (pInvType == MapleInventoryType.UNDEFINED || c.getPlayer().hasBlockedInventory()) {
            c.sendEnableActions();
            return;
        }
        final MapleInventory pInv = c.getPlayer().getInventory(pInvType); //Mode should correspond with MapleInventoryType
        boolean sorted = false;

        while (!sorted) {
            final short freeSlot = pInv.getNextFreeSlot();
            if (freeSlot != -1) {
                short itemSlot = -1;
                for (short i = freeSlot; i <= pInv.getSlotLimit(); i++) {
                    if (pInv.getItem(i) != null) {
                        itemSlot = i;
                        break;
                    }
                }
                if (itemSlot > 0) {
                    MapleInventoryManipulator.move(c, pInvType, itemSlot, freeSlot);
                } else {
                    sorted = true;
                }
            } else {
                sorted = true;
            }
        }
        c.announce(MaplePacketCreator.finishedGather(mode));
        c.dispose();
    }

    /*
     * 道具排序
     */
    public static void ItemSort(MaplePacketReader slea, MapleClient c) {
        c.getPlayer().updateTick(slea.readInt());
        c.getPlayer().setScrolledPosition((short) 0);
        if (c.getPlayer().hasBlockedInventory()) {
            c.sendEnableActions();
            return;
        }
        byte mode = slea.readByte();
        MapleInventoryType invType = MapleInventoryType.getByType(mode);
        MapleInventory inventory = c.getPlayer().getInventory(invType);

        List<Item> itemMap = new ArrayList<>();
        for (Item item : inventory.list()) {
            if (item.getPosition() <= 128) {
                itemMap.add(item);
            }
        }
        itemMap.sort(Item::compareTo);

        List<ModifyInventory> mods = new ArrayList<>();
        for (int i = 0; i < itemMap.size() - 1; ++i) {
            int n = i;
            for (int j = i + 1; j < itemMap.size(); ++j) {
                if (itemMap.get(j).getItemId() < itemMap.get(n).getItemId()) {
                    n = j;
                }
            }
            if (n != i) {
                final Item item = itemMap.get(i);
                final short position = item.getPosition();
                inventory.move(position, itemMap.get(n).getPosition(), inventory.getSlotLimit());
                mods.add(new ModifyInventory(2, item, position));
                itemMap.set(i, itemMap.get(n));
                itemMap.set(n, item);
            }
        }
        c.announce(InventoryPacket.modifyInventory(true, mods, c.getPlayer()));
        c.announce(MaplePacketCreator.finishedSort(mode));
        c.dispose();
    }

    public static boolean UseRewardBox(final short slot, final int itemId, final MapleClient c, final MapleCharacter player) {
        final Item toUse = c.getPlayer().getInventory(ItemConstants.getInventoryType(itemId)).getItem(slot);
        c.sendEnableActions();
        if (toUse != null && toUse.getQuantity() >= 1 && toUse.getItemId() == itemId && !player.hasBlockedInventory()) {
            if (player.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot() > -1 && player.getInventory(MapleInventoryType.USE).getNextFreeSlot() > -1 && player.getInventory(MapleInventoryType.SETUP).getNextFreeSlot() > -1 && player.getInventory(MapleInventoryType.ETC).getNextFreeSlot() > -1 && player.getInventory(MapleInventoryType.DECORATION).getNextFreeSlot() > -1) {
                final List<Pair<Integer, Integer>> list = CashItemFactory.getInstance().getRandomItem(itemId);
                if (list != null && list.size() > 0) {
                    final int nextInt = Randomizer.nextInt(list.size());
                    final int itemSN = list.get(nextInt).getLeft();
                    final int quantity = list.get(nextInt).getRight();
                    final int rewardItemId = CashItemFactory.getInstance().getItem(itemSN).getItemId();
                    if (player.isAdmin()) {
                        player.dropMessage(5, "打開道具獲得: " + rewardItemId);
                    }
                    MapleInventoryManipulator.addById(c, rewardItemId, quantity, "打開隨機箱子 道具ID: " + itemId + " 時間: " + DateUtil.getNowTime());
                    c.announce(MaplePacketCreator.getShowItemGain(rewardItemId, 1, true));
                    MapleInventoryManipulator.removeFromSlot(c, ItemConstants.getInventoryType(itemId), slot, (short) 1, false);
                    return true;
                }
                player.dropMessage(6, "出現未知錯誤.");
            } else {
                player.dropMessage(6, "背包空間不足。");
            }
        }
        return false;
    }

    public static boolean UseRewardItem(short slot, int itemId, MapleClient c, MapleCharacter chr) {
        Item toUse = c.getPlayer().getInventory(ItemConstants.getInventoryType(itemId)).getItem(slot);
        c.sendEnableActions();
        if (toUse != null && toUse.getQuantity() >= 1 && toUse.getItemId() == itemId && !chr.hasBlockedInventory()) {
            if (chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot() > -1 && chr.getInventory(MapleInventoryType.USE).getNextFreeSlot() > -1 && chr.getInventory(MapleInventoryType.SETUP).getNextFreeSlot() > -1 && chr.getInventory(MapleInventoryType.ETC).getNextFreeSlot() > -1 && chr.getInventory(MapleInventoryType.DECORATION).getNextFreeSlot() > -1) {
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                if (itemId == 2028048) { //未知楓幣包
                    int mesars = 5000000;
                    if (mesars > 0 && chr.getMeso() < (Integer.MAX_VALUE - mesars)) {
                        int gainmes = Randomizer.nextInt(mesars);
                        chr.gainMeso(gainmes, true, true);
                        c.announce(MTSCSPacket.sendMesobagSuccess(gainmes));
                        //MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType(itemId), itemId, 1, false, false);
                        MapleInventoryManipulator.removeFromSlot(c, ItemConstants.getInventoryType(itemId), slot, (short) 1, false);
                        return true;
                    } else {
                        chr.dropMessage(1, "楓幣已達到上限無法使用這個道具.");
                        return false;
                    }
                }
                Pair<Integer, List<Map<String, String>>> rewards = ii.getRewardItem(itemId);
                if (rewards != null && rewards.getLeft() > 0) {
                    while (true) {
                        for (Map<String, String> reward : rewards.getRight()) {
                            int rewardItemId = Integer.valueOf(reward.get("item"));
                            int prob = Integer.valueOf(reward.get("prob"));
                            int quantity = Integer.valueOf(reward.get("count"));
                            int period = Integer.valueOf(reward.get("period") != null ? reward.get("period") : "0");
                            String effect = reward.get("effect");
                            String worldmsg = reward.get("worldmsg");
                            if (prob > 0 && Randomizer.nextInt(rewards.getLeft()) < prob) { // Total prob
                                if (ItemConstants.getInventoryType(rewardItemId, false) == MapleInventoryType.EQUIP) {
                                    Item item = ii.getEquipById(rewardItemId);
                                    if (rewardItemId > 0) {
                                        item.setExpiration(System.currentTimeMillis() + (period * 60 * 1000));
                                    }
                                    item.setGMLog("Reward item: " + itemId + " on " + DateUtil.getCurrentDate());
                                    if (chr.isAdmin()) {
                                        chr.dropMessage(5, "打開道具獲得: " + item.getItemId());
                                    }
                                    if (rewardItemId / 1000 == 1182) {
                                        ii.randomize休彼德蔓徽章((Equip) item);
                                    }
                                    MapleInventoryManipulator.addbyItem(c, item);
                                    c.announce(MaplePacketCreator.getShowItemGain(item.getItemId(), item.getQuantity(), true));
                                } else {
                                    if (chr.isAdmin()) {
                                        chr.dropMessage(5, "打開道具獲得: " + rewardItemId + " - " + quantity);
                                    }
                                    MapleInventoryManipulator.addById(c, rewardItemId, quantity, "Reward item: " + itemId + " on " + DateUtil.getCurrentDate());
                                    c.announce(MaplePacketCreator.getShowItemGain(rewardItemId, quantity, true));
                                }
                                //MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType(itemId), itemId, 1, false, false);
                                MapleInventoryManipulator.removeFromSlot(c, ItemConstants.getInventoryType(itemId), slot, (short) 1, false);
                                c.announce(EffectPacket.showRewardItemAnimation(rewardItemId, effect));
                                chr.getMap().broadcastMessage(chr, EffectPacket.showRewardItemAnimation(rewardItemId, effect, chr.getId()), false);
                                return true;
                            }
                        }
                    }
                } else {
                    if (chr.getSpace(1) < 1 || chr.getSpace(2) < 1 || chr.getSpace(3) < 1 || chr.getSpace(4) < 1 || chr.getSpace(5) < 1) {
                        chr.dropMessage(1, "道具欄不足");
                        return false;
                    }
                    RaffleItem gitem = RafflePool.randomItem(itemId);
                    if (gitem == null) {
                        chr.dropMessage(1, "出現未知錯誤.");
                        return false;
                    }
                    final Item item;
                    if (MapleInventoryManipulator.removeById(c, ItemConstants.getInventoryType(itemId), itemId, 1, false, false)) {
                        item = MapleInventoryManipulator.addbyId_Gachapon(c, gitem.getItemId(), 1);
                    } else {
                        chr.dropMessage(1, "出現未知錯誤.");
                        return false;
                    }
                    if (item == null) {
                        chr.dropMessage(1, "出現未知錯誤.");
                        return false;
                    }
                    if (gitem.isSmega()) {
                        WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.gachaponMsg("恭喜" + c.getPlayer().getName() + "從" + ii.getName(itemId) + "機獲得{" + ii.getName(gitem.getItemId()) + "}", item));
                    }
                }
            } else {
                chr.dropMessage(1, "背包空間不足。");
            }
        }
        return false;
    }

    /*
     * 使用消耗道具
     */
    public static void UseItem(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || !chr.isAlive() || chr.getMapId() == 749040100 || chr.getMap() == null || chr.getBuffStatValueHolder(SecondaryStat.StopPortion) != null || chr.inPVP() || chr.getMap().isPvpMaps()) {
            c.sendEnableActions();
            return;
        }
        long time = System.currentTimeMillis();
        if (chr.getNextConsume() > time) {
            chr.dropMessage(5, "暫時無法使用這個道具，請稍後在試。");
            c.sendEnableActions();
            return;
        }
        c.getPlayer().updateTick(slea.readInt());
        short slot = slea.readShort();
        int itemId = slea.readInt();
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId) {
            c.sendEnableActions();
            return;
        }

        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (!FieldLimitType.NOMOBCAPACITYLIMIT.check(chr.getMap().getFieldLimit())) { //cwk quick hack
            if (ii.getItemEffect(toUse.getItemId()).applyTo(chr)) {
                if (ii.getItemProperty(toUse.getItemId(), "info/notConsume", 0) == 0) {
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
                }
                if (chr.getMap().getConsumeItemCoolTime() > 0) {
                    chr.setNextConsume(time + (chr.getMap().getConsumeItemCoolTime() * 1000));
                }
            }
        } else {
            c.sendEnableActions();
        }
    }

    /* 喝send null 1362 */
    public static byte[] UseItemSayNull(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.USE_ITEM_LOCK_V261_1376.getValue());
        c.announce(mplew.getPacket());
        return mplew.getPacket();
    }

    /*
     * 使用理發卷[2540000]之類的道具
     */
    public static void UseCosmetic(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || !chr.isAlive() || chr.getMap() == null || chr.hasBlockedInventory() || chr.inPVP()) {
            c.sendEnableActions();
            return;
        }
        short slot = slea.readShort();
        int itemId = slea.readInt();
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId || itemId / 10000 != 254 || (itemId / 1000) % 10 != chr.getGender()) {
            c.sendEnableActions();
            return;
        }
        if (MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId()).applyTo(chr)) {
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
        }
    }

    /*
     * 使用自定義混色染髮券[2432946]之類的道具
     */
    public static void ConsumeMixHairItemUseRequest(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || !chr.isAlive() || chr.getMap() == null || chr.hasBlockedInventory() || chr.inPVP()) {
            c.sendEnableActions();
            return;
        }
        short slot = slea.readShort();
        int itemId = slea.readInt();
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId || itemId / 10000 != 243) {
            c.sendEnableActions();
            return;
        }
        int showLookFlag = slea.readByte();
        boolean isConfirmChange = slea.readBool();
        long androidSN = -1;
        if (showLookFlag == 100)
            androidSN = slea.readLong();
        int selectID = slea.readInt();
        int select2ID = -1;
        if (showLookFlag == 101)
            select2ID = slea.readInt();
        List<Pair<Integer, Integer>> beautyResult = new LinkedList<>();
        beautyResult.add(new Pair<>(showLookFlag == 100 ? chr.getAndroid().getHair() : showLookFlag == 1 || showLookFlag == 2 ? chr.getSecondHair() : chr.getHair(), 0));
        if (showLookFlag == 101) beautyResult.add(new Pair<>(chr.getSecondHair(), 0));


        // 混色瞳孔
        int bColor = (selectID / 10000) * 1000;
        int mColor = ((selectID % 10000) / 1000) * 100;
        int prop = selectID % 100;
        int sColor = bColor + mColor + prop;

        int sColor2 = -1;
        if (showLookFlag == 101 && select2ID >= 0) {
            bColor = (select2ID / 10000) * 1000;
            mColor = ((select2ID % 10000) / 1000) * 100;
            prop = select2ID % 100;
            sColor2 = bColor + mColor + prop;
        }

        int styleID = -1;
        int style2ID = -1;
        if (sColor >= 0) styleID = ItemConstants.changeStyleID(sColor, beautyResult.getFirst().getLeft());
        if (sColor2 >= 0) style2ID = ItemConstants.changeStyleID(sColor2, beautyResult.get(1).getLeft());

        if (styleID >= 0) beautyResult.get(0).right = styleID;
        if (showLookFlag == 101 && style2ID >= 0) beautyResult.get(1).right = style2ID;

        if (styleID == -1 && (showLookFlag != 101 || style2ID != -1)) {
            c.announce(MaplePacketCreator.getChangeBeautyResult(itemId, showLookFlag, -1, null));
            return;
        }

        boolean used;
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
            int charmExp = MapleItemInformationProvider.getInstance().getIncCharmExp(itemId);
            if (charmExp > 0) {
                chr.getTrait(MapleTraitType.charm).addExp(charmExp, chr);
            }
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false, true);
        } else {
            c.announce(MaplePacketCreator.getChangeBeautyResult(itemId, showLookFlag, -1, null));
        }
    }

    /*
     * 使用還原器[2700000]之類的道具
     */
    public static void UseReducer(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || !chr.isAlive() || chr.getMap() == null || chr.hasBlockedInventory()) {
            return;
        }
        int itemId = slea.readInt();
        short slot = (short) slea.readInt();
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId || itemId < 2702000 || itemId > 2702002) {
            c.sendEnableActions();
            return;
        }
        if (chr.getInnerRank() > 2 && itemId < 2702002) {
            c.getPlayer().dropMessage(1, "這個傳播者無法對罕見等級以上的能力使用。");
            c.sendEnableActions();
            return;
        }
        if (chr.getInnerRank() >= 2 && itemId == 2702002) {
            c.getPlayer().dropMessage(1, "罕見奇幻傳播者只能在特殊、稀有階級的潛在能力上使用。");
            c.sendEnableActions();
            return;
        }
        chr.resetInnerSkill((byte) 0,itemId, Collections.emptyList(), false, false);
        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, toUse.getPosition(), (short) 1, false);
        c.announce(MaplePacketCreator.craftMessage("重新設定能力成功。"));
    }

    public static void UseReducerPrestige(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || !chr.isAlive() || chr.getMap() == null || chr.hasBlockedInventory()) {
            return;
        }
        List<Integer> lockPosition = new ArrayList<>();
        byte unk = slea.readByte();
        int lockCount = slea.readInt();
        for (int i = 0; i < lockCount; i++) {
            lockPosition.add(slea.readInt());
        }
        final int innerRank = chr.getInnerRank();
        int needHonor = ItemConstants.getNeedHonor(innerRank, lockCount);
        if (chr.getHonor() < needHonor || (lockCount > 0 && (innerRank < 2 || lockPosition.size() > 2))) {
            c.announce(MaplePacketCreator.craftMessage("重新設定能力失敗。"));
            c.sendEnableActions();
            return;
        }
        chr.gainHonor(-needHonor);
        chr.resetInnerSkill(unk, lockCount > 0 ? -2 : -1, lockPosition, false, false);
        c.announce(MaplePacketCreator.craftMessage("重新設定能力成功。"));
    }

    /*
     * 使用回城卷道具
     */
    public static void UseReturnScroll(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (!chr.isAlive() || chr.getMapId() == 749040100 || chr.hasBlockedInventory() || chr.isInBlockedMap() || chr.inPVP()) {
            c.sendEnableActions();
            return;
        }
        c.getPlayer().updateTick(slea.readInt());
        short slot = slea.readShort();
        int itemId = slea.readInt(); //物品ID
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId) {
            c.sendEnableActions();
            return;
        }
        if (!FieldLimitType.NOMOBCAPACITYLIMIT.check(chr.getMap().getFieldLimit())) {
            if (ii.getItemEffect(toUse.getItemId()).applyReturnScroll(chr)) {
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
            } else {
                c.sendEnableActions();
            }
        } else {
            c.sendEnableActions();
        }
    }

    public static void UseMiracleCube(MaplePacketReader slea, MapleCharacter chr) {
        chr.updateTick(slea.readInt());
        short scrollSlot = slea.readShort();
        Item cube = chr.getInventory(MapleInventoryType.USE).getItem(scrollSlot);
        short toScrollSlot = slea.readShort();
        Equip toScroll = (Equip) chr.getInventory(MapleInventoryType.EQUIP).getItem(toScrollSlot);
        if (toScrollSlot < 0) {
            toScroll = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(toScrollSlot);
        }
        if (cube == null || toScroll == null) {
            return;
        }
        boolean bl2 = toScroll.useCube(cube.getItemId(), chr, 0);
        if (bl2) {
            chr.forceUpdateItem(toScroll);
            if (JobConstants.is神之子(chr.getJob()) && toScrollSlot == -10) {
                toScroll = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
                toScroll.copyPotential((Equip) (chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -10)));
                chr.forceUpdateItem(toScroll);
            }
            MapleInventoryManipulator.removeFromSlot(chr.getClient(), MapleInventoryType.USE, scrollSlot, (short) 1, false, true);
        }
        chr.sendEnableActions();
    }

    /*
     * 使用鑒定放大鏡
     */
    public static void UseMagnify(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        chr.updateTick(slea.readInt());
        chr.setScrolledPosition((short) 0);
        short src = slea.readShort();
        short dst = slea.readShort();
        boolean insight = src == 20000/* && chr.getTrait(MapleTraitType.sense).getLevel() >= 30*/;
        Item magnify = chr.getInventory(MapleInventoryType.USE).getItem(src);
        Equip toScroll;
        if (dst < 0) {
            toScroll = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(dst);
        } else {
            toScroll = (Equip) chr.getInventory(MapleInventoryType.EQUIP).getItem(dst);
        }
        if (chr.isDebug()) {
            chr.dropMessage(5, "鑒定裝備: 放大鏡: " + magnify + " insight: " + insight + " toScroll: " + toScroll + " BlockedInventory: " + c.getPlayer().hasBlockedInventory());
        }
        if ((magnify == null && !insight) || toScroll == null || c.getPlayer().hasBlockedInventory()) {
            chr.dropMessage(5, "現在還不能進行操作。");
            c.announce(InventoryPacket.getInventoryFull());
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        int reqLevel = ii.getReqLevel(toScroll.getItemId()) / 10;
        final int n3 = (reqLevel >= 20) ? 19 : reqLevel;
        if (((toScroll.getState(false) < 17 && toScroll.getState(false) > 0) || (toScroll.getState(true) < 17 && toScroll.getState(true) > 0)) && (insight || magnify.getItemId() == 2460005 || magnify.getItemId() == 2460004 || magnify.getItemId() == 2460003 || (magnify.getItemId() == 2460002 && n3 <= 12) || (magnify.getItemId() == 2460001 && n3 <= 7) || (magnify.getItemId() == 2460000 && n3 <= 3))) {
            final boolean isPotAdd = toScroll.getState(false) < 17 && toScroll.getState(false) > 0;
            if (insight) {
                final long meso = ItemConstants.方塊.getCubeNeedMeso(toScroll);
                if (chr.getMeso() < meso) {
                    chr.dropMessage(5, "您沒有足夠的楓幣。");
                    c.sendEnableActions();
                    return;
                }
                chr.gainMeso(-meso, false);
            }
            toScroll.magnify();
            if (ItemConstants.isZeroWeapon(toScroll.getItemId())) {
                dst = (byte) (dst == -10 ? -11 : -10);
                chr.forceUpdateItem(((Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(dst)).copyPotential(toScroll), true);
            }
            chr.getTrait(MapleTraitType.insight).addExp((insight ? 10 : ((magnify.getItemId() + 2) - 2460000)) * 2, chr);
            chr.getMap().broadcastMessage(InventoryPacket.showMagnifyingEffect(chr.getId(), toScroll.getPosition(), !isPotAdd));
            if (!insight) {
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, magnify.getPosition(), (short) 1, false);
            }
            chr.forceUpdateItem(toScroll, true);
            if (dst < 0) { //當 dst 小於 就是鑒定裝備中的裝備 需要重新計算角色的屬性
                chr.equipChanged();
            }
            c.sendEnableActions();
        } else {
            c.announce(InventoryPacket.getInventoryFull());
        }
    }

    /*
     * 使用技能書
     */
    public static boolean UseSkillBook(short slot, int itemId, MapleClient c, MapleCharacter chr) {
        Item toUse = chr.getInventory(ItemConstants.getInventoryType(itemId)).getItem(slot);
        if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId || chr.hasBlockedInventory()) {
            return false;
        }
        Map<String, Integer> skilldata = MapleItemInformationProvider.getInstance().getItemBaseInfo(toUse.getItemId());
        Map<String, Integer> skillids = MapleItemInformationProvider.getInstance().getBookSkillID(toUse.getItemId());
        if (skilldata == null) { // Hacking or used an unknown item
            return false;
        }
        boolean canuse = false, success = false;
        int skill = 0, maxlevel = 0;

        Integer SuccessRate = skilldata.get("success");
        Integer ReqSkillLevel = skilldata.get("reqSkillLevel");
        Integer MasterLevel = skilldata.get("masterLevel");

        int i = 0;
        Integer CurrentLoopedSkillId;
        while (true) {
            CurrentLoopedSkillId = skillids.get(String.valueOf(i));
            i++;
            if (CurrentLoopedSkillId == null || MasterLevel == null) {
                break; // End of data
            }
            if (CurrentLoopedSkillId == 22171000) { // wz Bug ?
                CurrentLoopedSkillId = 龍魔導士.楓葉祝福;
            }
            Skill CurrSkillData = SkillFactory.getSkill(CurrentLoopedSkillId);
            if (CurrSkillData != null && CurrSkillData.canBeLearnedBy(chr.getJobWithSub()) && (ReqSkillLevel == null || chr.getSkillLevel(CurrSkillData) >= ReqSkillLevel) && chr.getMasterLevel(CurrSkillData) < MasterLevel) {
                canuse = true;
                if (SuccessRate == null || Randomizer.nextInt(100) <= SuccessRate) {
                    success = true;
                    chr.changeSingleSkillLevel(CurrSkillData, chr.getSkillLevel(CurrSkillData), (byte) (int) MasterLevel);
                } else {
                    success = false;
                }
                MapleInventoryManipulator.removeFromSlot(c, ItemConstants.getInventoryType(itemId), slot, (short) 1, false);
                break;
            }
        }
        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.useSkillBook(chr, skill, maxlevel, canuse, success));
        c.sendEnableActions();
        return canuse;
    }

    /*
     * 使用Sp初始化卷
     */
    public static void UseSpReset(short slot, int itemId, MapleClient c, MapleCharacter chr) {
        Item toUse = chr.getInventory(ItemConstants.getInventoryType(itemId)).getItem(slot);
        if (toUse == null || itemId / 1000 != 2500 || toUse.getItemId() != itemId || JobConstants.is零轉職業(chr.getJob())) {
            c.sendEnableActions();
            return;
        }
        chr.spReset();
        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, true);
        c.announce(MaplePacketCreator.useSPReset(chr.getId()));
    }

    /*
     * 使用Ap初始化卷
     */
    public static void UseApReset(short slot, int itemId, MapleClient c, MapleCharacter chr) {
        Item toUse = chr.getInventory(ItemConstants.getInventoryType(itemId)).getItem(slot);
        if (toUse != null && toUse.getQuantity() > 0 && toUse.getItemId() == itemId && !chr.hasBlockedInventory() && itemId / 10000 == 250) {
            chr.resetStats(4, 4, 4, 4);
            MapleInventoryManipulator.removeFromSlot(c, ItemConstants.getInventoryType(itemId), slot, (short) 1, false);
            c.announce(MaplePacketCreator.useAPReset(chr.getId()));
            c.sendEnableActions();
        } else {
            c.sendEnableActions();
        }
    }

    /*
     * 使用捕抓怪物道具
     */
    public static void UseCatchItem(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        c.getPlayer().updateTick(slea.readInt());
        c.getPlayer().setScrolledPosition((short) 0);
        short slot = slea.readShort();
        int itemid = slea.readInt();
        MapleMonster mob = chr.getMap().getMonsterByOid(slea.readInt());
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        MapleMap map = chr.getMap();
        if (toUse != null && toUse.getQuantity() > 0 && toUse.getItemId() == itemid && mob != null && !chr.hasBlockedInventory() && itemid / 10000 == 227 && MapleItemInformationProvider.getInstance().getCardMobId(itemid) == mob.getId()) {
            if (!MapleItemInformationProvider.getInstance().isMobHP(itemid) || mob.getHp() <= mob.getMobMaxHp() / 2) {
                map.broadcastMessage(MobPacket.catchMonster(mob.getObjectId(), itemid, (byte) 1));
                map.killMonster(mob, chr, true, false, (byte) 1, 0);
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false, false);
                if (MapleItemInformationProvider.getInstance().getCreateId(itemid) > 0) {
                    MapleInventoryManipulator.addById(c, MapleItemInformationProvider.getInstance().getCreateId(itemid), 1, "Catch item " + itemid + " on " + DateUtil.getCurrentDate());
                }
            } else {
                map.broadcastMessage(MobPacket.catchMonster(mob.getObjectId(), itemid, (byte) 0));
            }
        }
        c.sendEnableActions();
    }

    /*
     * 使用坐騎疲勞恢復藥水
     */
    public static void UseMountFood(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        c.getPlayer().updateTick(slea.readInt());
        short slot = slea.readShort();
        int itemid = slea.readInt(); //2260000 usually 恢復疲勞補藥
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        MapleMount mount = chr.getMount();
        if (itemid / 10000 == 226 && toUse != null && toUse.getQuantity() > 0 && toUse.getItemId() == itemid && mount != null && !c.getPlayer().hasBlockedInventory()) {
            int fatigue = mount.getFatigue();
            boolean levelup = false;
            mount.setFatigue((byte) -30);
            if (fatigue > 0) {
                mount.increaseExp();
                int level = mount.getLevel();
                if (level < 30 && mount.getExp() >= GameConstants.getMountExpNeededForLevel(level + 1)) {
                    mount.setLevel((byte) (level + 1));
                    levelup = true;
                }
            }
            chr.getMap().broadcastMessage(MaplePacketCreator.updateMount(chr, levelup));
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
        }
        c.sendEnableActions();
    }

    public static void UseScriptedNPCItem(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (c == null || chr == null) {
            return;
        }
        c.getPlayer().updateTick(slea.readInt());
        short slot = slea.readShort();
        int itemId = slea.readInt();
        Item toUse = chr.getInventory(ItemConstants.getInventoryType(itemId)).getItem(slot);
        long expiration_days = 0;
        int mountid = 0;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        ScriptedItem info = ii.getScriptedItemInfo(itemId);
        if (info == null) {
            c.sendEnableActions();
            return;
        }
        if (toUse != null && toUse.getQuantity() >= 1 && toUse.getItemId() == itemId && !chr.hasBlockedInventory() && !chr.inPVP()) {
            switch (toUse.getItemId()) {
                case 2430007: { // Blank Compass
                    MapleInventory inventory = chr.getInventory(MapleInventoryType.SETUP);
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                    if (inventory.countById(3994102) >= 20 // 羅盤用N
                            && inventory.countById(3994103) >= 20 // 羅盤用E
                            && inventory.countById(3994104) >= 20 // 羅盤用W
                            && inventory.countById(3994105) >= 20) { // 羅盤用S
                        //2430008 - 黃金羅盤 - 指向鑽石王老五寶物島位置的黃金羅盤。 雙擊後可以移動到寶物島
                        MapleInventoryManipulator.addById(c, 2430008, 1, "Scripted item: " + itemId + " on " + DateUtil.getCurrentDate()); // Gold Compass
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.SETUP, 3994102, 20, false, false);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.SETUP, 3994103, 20, false, false);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.SETUP, 3994104, 20, false, false);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.SETUP, 3994105, 20, false, false);
                    } else {
                        //2430007 - 空羅盤 - 沒有任何標誌的羅盤。
                        MapleInventoryManipulator.addById(c, 2430007, 1, "Scripted item: " + itemId + " on " + DateUtil.getCurrentDate()); // Blank Compass
                    }
                    chr.getScriptManager().startNpcScript(2084001, 0, null);
//                    NPCScriptManager.getInstance().start(c, 2084001);
                    break;
                }
                case 2430008: { // 黃金羅盤
                    chr.saveLocation(SavedLocationType.RICHIE);
                    MapleMap map;
                    boolean warped = false;
                    for (int i = 390001000; i <= 390001004; i++) {
                        map = c.getChannelServer().getMapFactory().getMap(i);
                        if (map.getCharactersSize() == 0) {
                            chr.changeMap(map, map.getPortal(0));
                            warped = true;
                            break;
                        }
                    }
                    if (warped) { // Removal of gold compass
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                    } else { // Or mabe some other message.
                        c.getPlayer().dropMessage(5, "All maps are currently in use, please try again later.");
                    }
                    break;
                }
                case 2430112: //神奇方塊碎片
                    if (c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                        if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430112) >= 25) {
                            if (MapleInventoryManipulator.checkSpace(c, 2049400, 1, "") && MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 25, true, false)) {
                                MapleInventoryManipulator.addById(c, 2049400, 1, "Scripted item: " + toUse.getItemId() + " on " + DateUtil.getCurrentDate());
                            } else {
                                c.getPlayer().dropMessage(5, "消耗欄空間位置不足.");
                            }
                        } else if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430112) >= 10) {
                            if (MapleInventoryManipulator.checkSpace(c, 2049401, 1, "") && MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 10, true, false)) {
                                MapleInventoryManipulator.addById(c, 2049401, 1, "Scripted item: " + toUse.getItemId() + " on " + DateUtil.getCurrentDate());
                            } else {
                                c.getPlayer().dropMessage(5, "消耗欄空間位置不足.");
                            }
                        } else {
                            c.getPlayer().getScriptManager().startItemScript(toUse, info.getNpc(), null);
//                            NPCScriptManager.getInstance().startItem(c, info.getNpc(), toUse);
                            //c.getPlayer().dropMessage(5, "There needs to be 10 Fragments for a Potential Scroll, 25 for Advanced Potential Scroll.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "消耗欄空間位置不足.");
                    }
                    break;
                case 2430481: //高級神奇方塊碎片
                    if (c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                        if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430481) >= 100) {
                            if (MapleInventoryManipulator.checkSpace(c, 2049701, 1, "") && MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 100, true, false)) {
                                MapleInventoryManipulator.addById(c, 2049701, 1, "Scripted item: " + toUse.getItemId() + " on " + DateUtil.getCurrentDate());
                            } else {
                                c.getPlayer().dropMessage(5, "消耗欄空間位置不足.");
                            }
                        } else if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430481) >= 30) {
                            if (MapleInventoryManipulator.checkSpace(c, 2049400, 1, "") && MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 30, true, false)) {
                                MapleInventoryManipulator.addById(c, 2049400, 1, "Scripted item: " + toUse.getItemId() + " on " + DateUtil.getCurrentDate());
                            } else {
                                c.getPlayer().dropMessage(5, "消耗欄空間位置不足.");
                            }
                        } else {
                            c.getPlayer().getScriptManager().startItemScript(toUse, info.getNpc(), null);
//                            NPCScriptManager.getInstance().startItem(c, info.getNpc(), toUse);
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "消耗欄空間位置不足.");
                    }
                    break;
                case 2430760: // 星岩方塊碎片
                    if (c.getPlayer().getInventory(MapleInventoryType.CASH).getNumFreeSlot() >= 1) {
                        if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430760) >= 10) {
                            if (MapleInventoryManipulator.checkSpace(c, 5750000, 1, "") && MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 10, true, false)) {
                                MapleInventoryManipulator.addById(c, 5750000, 1, "Scripted item: " + toUse.getItemId() + " on " + DateUtil.getCurrentDate());
                            } else {
                                c.getPlayer().dropMessage(5, "請檢測背包空間是否足夠.");
                            }
                        } else {
                            c.getPlayer().dropMessage(5, "10個星岩方塊碎片才可以兌換1個星岩方塊.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "請檢測背包空間是否足夠.");
                    }
                    break;
                case 5680019: {//starling hair
                    //if (c.getPlayer().getGender() == 1) {
                    int hair = 32150 + (c.getPlayer().getHair() % 10);
                    c.getPlayer().setHair(hair);
                    c.getPlayer().updateSingleStat(MapleStat.髮型, hair);
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, slot, (byte) 1, false);
                    //}
                    break;
                }
                case 5680020: {//starling hair
                    //if (c.getPlayer().getGender() == 0) {
                    int hair = 32160 + (c.getPlayer().getHair() % 10);
                    c.getPlayer().setHair(hair);
                    c.getPlayer().updateSingleStat(MapleStat.髮型, hair);
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, slot, (byte) 1, false);
                    //}
                    break;
                }
                case 3994225:
                    c.getPlayer().dropMessage(5, "Please bring this item to the NPC.");
                    break;
                case 2430212: //疲勞恢復藥
                    MapleQuestStatus marr = c.getPlayer().getQuestNAdd(MapleQuest.getInstance(GameConstants.ENERGY_DRINK));
                    if (marr.getCustomData() == null) {
                        marr.setCustomData("0");
                    }
                    long lastTime = Long.parseLong(marr.getCustomData());
                    if (lastTime + (600000) > System.currentTimeMillis()) {
                        c.getPlayer().dropMessage(5, "疲勞恢復藥 10分鐘內只能使用1次，請稍後在試。");
                    } else if (c.getPlayer().getFatigue() > 0) {
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                        c.getPlayer().setFatigue(c.getPlayer().getFatigue() - 5);
                    }
                    break;
                case 2430213: //疲勞恢復藥
                    marr = c.getPlayer().getQuestNAdd(MapleQuest.getInstance(GameConstants.ENERGY_DRINK));
                    if (marr.getCustomData() == null) {
                        marr.setCustomData("0");
                    }
                    lastTime = Long.parseLong(marr.getCustomData());
                    if (lastTime + (600000) > System.currentTimeMillis()) {
                        c.getPlayer().dropMessage(5, "疲勞恢復藥 10分鐘內只能使用1次，請稍後在試。");
                    } else if (c.getPlayer().getFatigue() > 0) {
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                        c.getPlayer().setFatigue(c.getPlayer().getFatigue() - 10);
                    }
                    break;
                case 2430220: //疲勞恢復藥
                case 2430214: //疲勞恢復藥
                    if (c.getPlayer().getFatigue() > 0) {
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                        c.getPlayer().setFatigue(c.getPlayer().getFatigue() - 30);
                    }
                    break;
                case 2430227: //疲勞恢復藥
                    if (c.getPlayer().getFatigue() > 0) {
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                        c.getPlayer().setFatigue(c.getPlayer().getFatigue() - 50);
                    }
                    break;
                case 2430231: //疲勞恢復藥
                    marr = c.getPlayer().getQuestNAdd(MapleQuest.getInstance(GameConstants.ENERGY_DRINK));
                    if (marr.getCustomData() == null) {
                        marr.setCustomData("0");
                    }
                    lastTime = Long.parseLong(marr.getCustomData());
                    if (lastTime + (600000) > System.currentTimeMillis()) {
                        c.getPlayer().dropMessage(5, "疲勞恢復藥 10分鐘內只能使用1次，請稍後在試。");
                    } else if (c.getPlayer().getFatigue() > 0) {
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                        c.getPlayer().setFatigue(c.getPlayer().getFatigue() - 40);
                    }
                    break;
                case 2430144: //秘密能手冊
                    int itemid = Randomizer.nextInt(373) + 2290000;
                    if (MapleItemInformationProvider.getInstance().itemExists(itemid) && !MapleItemInformationProvider.getInstance().getName(itemid).contains("Special") && !MapleItemInformationProvider.getInstance().getName(itemid).contains("Event")) {
                        MapleInventoryManipulator.addById(c, itemid, 1, "Reward item: " + toUse.getItemId() + " on " + DateUtil.getCurrentDate());
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                    }
                    break;
                case 2430370: //秘密配方
                    if (MapleInventoryManipulator.checkSpace(c, 2028062, 1, "")) {
                        MapleInventoryManipulator.addById(c, 2028062, 1, "Reward item: " + toUse.getItemId() + " on " + DateUtil.getCurrentDate());
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                    }
                    break;
                case 2430158: //獅子王的勳章
                    if (c.getPlayer().getInventory(MapleInventoryType.ETC).getNumFreeSlot() >= 1) {
                        if (c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000630) >= 100) {
                            if (MapleInventoryManipulator.checkSpace(c, 4310010, 1, "") && MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 1, true, false)) {
                                MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4000630, 100, true, false);
                                MapleInventoryManipulator.addById(c, 4310010, 1, "Scripted item: " + toUse.getItemId() + " on " + DateUtil.getCurrentDate());
                            } else {
                                c.getPlayer().dropMessage(5, "其他欄空間位置不足.");
                            }
                        } else if (c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000630) >= 50) {
                            if (MapleInventoryManipulator.checkSpace(c, 4310009, 1, "") && MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 1, true, false)) {
                                MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4000630, 50, true, false);
                                MapleInventoryManipulator.addById(c, 4310009, 1, "Scripted item: " + toUse.getItemId() + " on " + DateUtil.getCurrentDate());
                            } else {
                                c.getPlayer().dropMessage(5, "其他欄空間位置不足.");
                            }
                        } else {
                            c.getPlayer().dropMessage(5, "需要50個淨化圖騰才能兌換出獅子王的貴族勳章，100個淨化圖騰才能兌換獅子王的皇家勳章。");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "其他欄空間位置不足.");
                    }
                    break;
                case 2430159: //阿爾卡斯特的水晶
                    MapleQuest.getInstance(3182).forceComplete(c.getPlayer(), 2161004);
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                    break;
                case 2430200: //閃電之石
                    if (c.getPlayer().getQuestStatus(31152) != 2) {
                        c.getPlayer().dropMessage(5, "You have no idea how to use it.");
                    } else if (c.getPlayer().getInventory(MapleInventoryType.ETC).getNumFreeSlot() >= 1) {
                        if (c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000660) >= 1 && c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000661) >= 1 && c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000662) >= 1 && c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000663) >= 1) {
                            if (MapleInventoryManipulator.checkSpace(c, 4032923, 1, "") && MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 1, true, false) && MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4000660, 1, true, false) && MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4000661, 1, true, false) && MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4000662, 1, true, false) && MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4000663, 1, true, false)) {
                                MapleInventoryManipulator.addById(c, 4032923, 1, "Scripted item: " + toUse.getItemId() + " on " + DateUtil.getCurrentDate());
                            } else {
                                c.getPlayer().dropMessage(5, "其他欄空間位置不足.");
                            }
                        } else {
                            c.getPlayer().dropMessage(5, "There needs to be 1 of each Stone for a Dream Key.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "其他欄空間位置不足.");
                    }
                    break;
                case 2430130: //反抗者能量膠囊
                case 2430131: //為所有人準備的能量膠囊
                    if (JobConstants.is末日反抗軍(c.getPlayer().getJob())) {
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                        c.getPlayer().gainExp(20000 + (c.getPlayer().getLevel() * 50 * c.getChannelServer().getExpRate()), true, true, false);
                    } else {
                        c.getPlayer().dropMessage(5, "您無法使用這個道具。");
                    }
                    break;
                case 2430132: //反抗者武器箱
                case 2430133: //卡珊德拉的補給品箱
                case 2430134: //反抗者秘密箱子
                case 2430142: //卡珊德拉的專屬補給品箱
                    if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() >= 1 && c.getPlayer().getInventory(MapleInventoryType.DECORATION).getNumFreeSlot() >= 1) {
                        if (c.getPlayer().getJob() == 3200 || c.getPlayer().getJob() == 3210 || c.getPlayer().getJob() == 3211 || c.getPlayer().getJob() == 3212) {
                            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                            MapleInventoryManipulator.addById(c, 1382101, 1, "Scripted item: " + itemId + " on " + DateUtil.getCurrentDate());
                        } else if (c.getPlayer().getJob() == 3300 || c.getPlayer().getJob() == 3310 || c.getPlayer().getJob() == 3311 || c.getPlayer().getJob() == 3312) {
                            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                            MapleInventoryManipulator.addById(c, 1462093, 1, "Scripted item: " + itemId + " on " + DateUtil.getCurrentDate());
                        } else if (c.getPlayer().getJob() == 3500 || c.getPlayer().getJob() == 3510 || c.getPlayer().getJob() == 3511 || c.getPlayer().getJob() == 3512) {
                            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                            MapleInventoryManipulator.addById(c, 1492080, 1, "Scripted item: " + itemId + " on " + DateUtil.getCurrentDate());
                        } else {
                            c.getPlayer().dropMessage(5, "您無法使用這個道具。");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "背包空間不足。");
                    }
                    break;
                case 2430455: //傳說時空石
                    c.getPlayer().getScriptManager().startItemScript(toUse, 9010000, null);
//                    NPCScriptManager.getInstance().startItem(c, 9010000, toUse);
                    break;
                case 2430036: //黑鱷魚1天使用券
                    mountid = 1027;
                    expiration_days = 1;
                    break;
                case 2430170: //croco 7 day
                    mountid = 1027;
                    expiration_days = 7;
                    break;
                case 2430037: //男男機車1天使用券
                    mountid = 1028;
                    expiration_days = 1;
                    break;
                case 2430038: //女女機車1天使用券
                    mountid = 1029;
                    expiration_days = 1;
                    break;
                case 2430039: //觔斗雲1天使用券
                    mountid = 1030;
                    expiration_days = 1;
                    break;
                case 2430040: //巴洛古1天使用券
                    mountid = 1031;
                    expiration_days = 1;
                    break;
                case 2430223: //balrog 1 day
                    mountid = 1031;
                    expiration_days = 15;
                    break;
                case 2430259: //蝙蝠魔騎寵卷
                    mountid = 1031;
                    expiration_days = 3;
                    break;
                case 2430242: //摩托車使用券
                    mountid = 80001018;
                    expiration_days = 10;
                    break;
                case 2430243: //超能套裝使用券
                    mountid = 80001019;
                    expiration_days = 10;
                    break;
                case 2430261: //超能套裝騎寵卷
                    mountid = 80001019;
                    expiration_days = 3;
                    break;
                case 2430249: //木飛機3天使用券
                    mountid = 80001027;
                    expiration_days = 3;
                    break;
                case 2430225: //巴洛古騎寵使用券
                    mountid = 1031;
                    expiration_days = 10;
                    break;
                case 2430053: //鱷魚30天使用券
                    mountid = 1027;
                    expiration_days = 1;
                    break;
                case 2430054: //男男機車30天使用券
                    mountid = 1028;
                    expiration_days = 30;
                    break;
                case 2430055: //女女機車30天使用券
                    mountid = 1029;
                    expiration_days = 30;
                    break;
                case 2430257: //女女機車7天使用券
                    mountid = 1029;
                    expiration_days = 7;
                    break;
                case 2430056: //蝙蝠魔先生30天使用券
                    mountid = 1035;
                    expiration_days = 30;
                    break;
                case 2430057: //賽車30天使用券
                    mountid = 1033;
                    expiration_days = 30;
                    break;
                case 2430072: //老虎傳說7天使用券
                    mountid = 1034;
                    expiration_days = 7;
                    break;
                case 2430073: //獅子王 (有效期15天)
                    mountid = 1036;
                    expiration_days = 15;
                    break;
                case 2430074: //獨角獸騎寵券（7天）
                    mountid = 1037;
                    expiration_days = 15;
                    break;
                case 2430272: //跑車騎寵卷 - 跑車3天使用券
                    mountid = 1038;
                    expiration_days = 3;
                    break;
                case 2430275: //休彼德蔓的熱氣球7天使用券
                    mountid = 80001033;
                    expiration_days = 7;
                    break;
                case 2430075: //low rider 15 day
                    mountid = 1038;
                    expiration_days = 15;
                    break;
                case 2430076: //田園紅卡車 (有效期15天)
                    mountid = 1039;
                    expiration_days = 15;
                    break;
                case 2430077: //惡魔石像 (有效期15天)
                    mountid = 1040;
                    expiration_days = 15;
                    break;
                case 2430080: //聖獸提拉奧斯20天使用券
                    mountid = 1042;
                    expiration_days = 20;
                    break;
                case 2430082: //花蘑菇7天使用券
                    mountid = 1044;
                    expiration_days = 7;
                    break;
                case 2430260: //花蘑菇騎寵卷 - 花蘑菇3天使用券
                    mountid = 1044;
                    expiration_days = 3;
                    break;
                case 2430091: //夢魘使用券 - 雙擊後可以在10天內使用騎乘技能[夢魘]
                    mountid = 1049;
                    expiration_days = 10;
                    break;
                case 2430092: //白雪人騎寵使用券 - 雙擊後可以在7天內使用騎乘技能[白雪人騎寵]
                    mountid = 1050;
                    expiration_days = 10;
                    break;
                case 2430263: //白雪人騎寵卷 - 白雪人騎寵3天使用券
                    mountid = 1050;
                    expiration_days = 3;
                    break;
                case 2430093: //鴕鳥騎寵使用券 - 雙擊後可以在10天內使用騎乘技能[鴕鳥騎寵]
                    mountid = 1051;
                    expiration_days = 10;
                    break;
                case 2430101: //粉紅熊熱氣球使用券 - 雙擊後可以在10天內使用騎乘技能[粉紅熊熱氣球]
                    mountid = 1052;
                    expiration_days = 10;
                    break;
                case 2430102: //變形金剛使用券 - 雙擊後可以在10天內使用騎乘技能[變形金剛]
                    mountid = 1053;
                    expiration_days = 10;
                    break;
                case 2430103: //chicken 30 day
                    mountid = 1054;
                    expiration_days = 30;
                    break;
                case 2430266: //走路雞騎寵卷 - 走路雞騎寵3天使用券
                    mountid = 1054;
                    expiration_days = 3;
                    break;
                case 2430265: //騎士團戰車騎寵卷 - 騎士團戰車騎寵3天使用券
                    mountid = 1151;
                    expiration_days = 3;
                    break;
                case 2430258: //警車1年使用券
                    mountid = 1115;
                    expiration_days = 365;
                    break;
                case 2430117: //獅子王(有效期1年)
                    mountid = 1036;
                    expiration_days = 365;
                    break;
                case 2430118: //田園紅卡車  (有效期1年)
                    mountid = 1039;
                    expiration_days = 365;
                    break;
                case 2430119: //惡魔石像  (有效期1年)
                    mountid = 1040;
                    expiration_days = 365;
                    break;
                case 2430120: //unicorn 1 year
                    mountid = 1037;
                    expiration_days = 365;
                    break;
                case 2430271: //貓頭鷹騎寵卷 - 貓頭鷹騎寵3天使用券
                    mountid = 1069;
                    expiration_days = 3;
                    break;
                case 2430136: //貓頭鷹騎寵15天權
                    mountid = 1069;
                    expiration_days = 15;
                    break;
                case 2430137: //貓頭鷹騎寵30天權
                    mountid = 1069;
                    expiration_days = 30;
                    break;
                case 2430138: //貓頭鷹騎寵1年權
                    mountid = 1069;
                    expiration_days = 365;
                    break;
                case 2430145: //mothership
                    mountid = 1070;
                    expiration_days = 30;
                    break;
                case 2430146: //mothership
                    mountid = 1070;
                    expiration_days = 365;
                    break;
                case 2430147: //mothership
                    mountid = 1071;
                    expiration_days = 30;
                    break;
                case 2430148: //mothership
                    mountid = 1071;
                    expiration_days = 365;
                    break;
                case 2430135: //os4
                    mountid = 1065;
                    expiration_days = 15;
                    break;
                case 2430149: //雄獅騎寵30日使用權
                    mountid = 1072;
                    expiration_days = 30;
                    break;
                case 2430262: //雄獅騎寵卷 - 雄獅騎寵3天使用券
                    mountid = 1072;
                    expiration_days = 3;
                    break;
                case 2430179: //魔女的掃把15日使用權
                    mountid = 1081;
                    expiration_days = 15;
                    break;
                case 2430264: //魔女的掃把騎寵卷 - 魔女的掃把3天使用券
                    mountid = 1081;
                    expiration_days = 3;
                    break;
                case 2430201: //兔子騎寵3日券
                    mountid = 1096;
                    expiration_days = 3;
                    break;
                case 2430228: //兔兔加油騎寵（15天權）
                    mountid = 1101;
                    expiration_days = 15;
                    break;
                case 2430276: //tiny bunny 60 day
                    mountid = 1101;
                    expiration_days = 15;
                    break;
                case 2430277: //tiny bunny 60 day
                    mountid = 1101;
                    expiration_days = 365;
                    break;
                case 2430283: //突擊！木馬10天使用券
                    mountid = 1025;
                    expiration_days = 10;
                    break;
                case 2430291: //熱氣球交換券
                    mountid = 1145;
                    expiration_days = -1;
                    break;
                case 2430293: //飛船交換券
                    mountid = 1146;
                    expiration_days = -1;
                    break;
                case 2430295: //天馬交換券
                    mountid = 1147;
                    expiration_days = -1;
                    break;
                case 2430297: //暗光龍交換券
                    mountid = 1148;
                    expiration_days = -1;
                    break;
                case 2430299: //魔法掃帚交換券
                    mountid = 1149;
                    expiration_days = -1;
                    break;
                case 2430301: //觔斗雲交換券
                    mountid = 1150;
                    expiration_days = -1;
                    break;
                case 2430303: //騎士團戰車交換券
                    mountid = 1151;
                    expiration_days = -1;
                    break;
                case 2430305: //夢魘交換券
                    mountid = 1152;
                    expiration_days = -1;
                    break;
                case 2430307: //巴洛古交換券
                    mountid = 1153;
                    expiration_days = -1;
                    break;
                case 2430309: //透明巴洛古交換券
                    mountid = 1154;
                    expiration_days = -1;
                    break;
                case 2430311: //貓頭鷹交換券
                    mountid = 1156;
                    expiration_days = -1;
                    break;
                case 2430313: //直升機交換券
                    mountid = 1156;
                    expiration_days = -1;
                    break;
                case 2430315: //妮娜的魔法陣交換券
                    mountid = 1118;
                    expiration_days = -1;
                    break;
                case 2430317: //青蛙交換券
                    mountid = 1121;
                    expiration_days = -1;
                    break;
                case 2430319: //小龜龜交換券
                    mountid = 1122;
                    expiration_days = -1;
                    break;
                case 2430321: //無辜水牛交換券
                    mountid = 1123;
                    expiration_days = -1;
                    break;
                case 2430323: //玩具坦克交換券
                    mountid = 1124;
                    expiration_days = -1;
                    break;
                case 2430325: //維京戰車交換券
                    mountid = 1129;
                    expiration_days = -1;
                    break;
                case 2430327: //打豆豆機器人交換券
                    mountid = 1130;
                    expiration_days = -1;
                    break;
                case 2430329: //暴風摩托交換券
                    mountid = 1063;
                    expiration_days = -1;
                    break;
                case 2430331: //玩具木馬交換券
                    mountid = 1025;
                    expiration_days = -1;
                    break;
                case 2430333: //老虎只是傳說交換券
                    mountid = 1034;
                    expiration_days = -1;
                    break;
                case 2430335: //萊格斯的豺犬交換券
                    mountid = 1136;
                    expiration_days = -1;
                    break;
                case 2430337: //鴕鳥交換券
                    mountid = 1051;
                    expiration_days = -1;
                    break;
                case 2430339: //跑車交換券
                    mountid = 1138;
                    expiration_days = -1;
                    break;
                case 2430341: //拿破侖的白馬交換券
                    mountid = 1139;
                    expiration_days = -1;
                    break;
                case 2430343: //鱷魚王交換券
                    mountid = 1027;
                    expiration_days = -1;
                    break;
                case 2430346: //女女機車交換券
                    mountid = 1029;
                    expiration_days = -1;
                    break;
                case 2430348: //男男機車交換券
                    mountid = 1028;
                    expiration_days = -1;
                    break;
                case 2430350: //賽車交換券
                    mountid = 1033;
                    expiration_days = -1;
                    break;
                case 2430352: //機械套裝交換券
                    mountid = 1064;
                    expiration_days = -1;
                    break;
                case 2430354: //巨無霸兔子交換券
                    mountid = 1096;
                    expiration_days = -1;
                    break;
                case 2430356: //兔兔加油交換券
                    mountid = 1101;
                    expiration_days = -1;
                    break;
                case 2430358: //兔子車伕交換券
                    mountid = 1102;
                    expiration_days = -1;
                    break;
                case 2430360: //走路雞交換券
                    mountid = 1054;
                    expiration_days = -1;
                    break;
                case 2430362: //鋼鐵變形俠交換券
                    mountid = 1053;
                    expiration_days = -1;
                    break;
                case 2430292: //熱氣球90天交換券
                    mountid = 1145;
                    expiration_days = 90;
                    break;
                case 2430294: //飛船90天交換券
                    mountid = 1146;
                    expiration_days = 90;
                    break;
                case 2430296: //天馬90天交換券
                    mountid = 1147;
                    expiration_days = 90;
                    break;
                case 2430298: //暗光龍90天交換券
                    mountid = 1148;
                    expiration_days = 90;
                    break;
                case 2430300: //魔法掃帚90天交換券
                    mountid = 1149;
                    expiration_days = 90;
                    break;
                case 2430302: //觔斗雲90天交換券
                    mountid = 1150;
                    expiration_days = 90;
                    break;
                case 2430304: //騎士團戰車90天交換券
                    mountid = 1151;
                    expiration_days = 90;
                    break;
                case 2430306: //夢魘90天交換券
                    mountid = 1152;
                    expiration_days = 90;
                    break;
                case 2430308: //巴洛古90天交換券
                    mountid = 1153;
                    expiration_days = 90;
                    break;
                case 2430310: //透明巴洛古90天交換券
                    mountid = 1154;
                    expiration_days = 90;
                    break;
                case 2430312: //貓頭鷹90天交換券
                    mountid = 1156;
                    expiration_days = 90;
                    break;
                case 2430314: //直升機90天交換券
                    mountid = 1156;
                    expiration_days = 90;
                    break;
                case 2430316: //妮娜的魔法90天交換券
                    mountid = 1118;
                    expiration_days = 90;
                    break;
                case 2430318: //青蛙90天交換券
                    mountid = 1121;
                    expiration_days = 90;
                    break;
                case 2430320: //小龜龜90天交換券
                    mountid = 1122;
                    expiration_days = 90;
                    break;
                case 2430322: //無辜水牛90天交換券
                    mountid = 1123;
                    expiration_days = 90;
                    break;
                case 2430326: //維京戰車90天交換券
                    mountid = 1129;
                    expiration_days = 90;
                    break;
                case 2430328: //打豆豆機器人90天交換券
                    mountid = 1130;
                    expiration_days = 90;
                    break;
                case 2430330: //暴風摩托90天交換券
                    mountid = 1063;
                    expiration_days = 90;
                    break;
                case 2430332: //玩具木馬90天交換券
                    mountid = 1025;
                    expiration_days = 90;
                    break;
                case 2430334: //老虎只是傳說90天交換券
                    mountid = 1034;
                    expiration_days = 90;
                    break;
                case 2430336: //萊格斯的豺犬90天交換券
                    mountid = 1136;
                    expiration_days = 90;
                    break;
                case 2430338: //鴕鳥90天交換券
                    mountid = 1051;
                    expiration_days = 90;
                    break;
                case 2430340: //跑車90天交換券
                    mountid = 1138;
                    expiration_days = 90;
                    break;
                case 2430342: //拿破侖的白馬90天交換券
                    mountid = 1139;
                    expiration_days = 90;
                    break;
                case 2430344: //鱷魚王90天交換券
                    mountid = 1027;
                    expiration_days = 90;
                    break;
                case 2430347: //女女機車90天交換券
                    mountid = 1029;
                    expiration_days = 90;
                    break;
                case 2430349: //男男機車90天交換券
                    mountid = 1028;
                    expiration_days = 90;
                    break;
                case 2430351: //賽車90天交換券
                    mountid = 1033;
                    expiration_days = 90;
                    break;
                case 2430353: //機械套裝90天交換券
                    mountid = 1064;
                    expiration_days = 90;
                    break;
                case 2430355: //巨無霸兔子90天交換券
                    mountid = 1096;
                    expiration_days = 90;
                    break;
                case 2430357: //兔兔加油90天交換券
                    mountid = 1101;
                    expiration_days = 90;
                    break;
                case 2430359: //兔子車伕90天交換券
                    mountid = 1102;
                    expiration_days = 90;
                    break;
                case 2430361: //走路雞90天交換券
                    mountid = 1054;
                    expiration_days = 90;
                    break;
                case 2430363: //鋼鐵變形俠90天交換券
                    mountid = 1053;
                    expiration_days = 90;
                    break;
                case 2430324: //機動巡邏車(准乘4人)交換券
                    mountid = 1158;
                    expiration_days = -1;
                    break;
                case 2430345: //機動巡邏車(准乘4人)90天交換券
                    mountid = 1158;
                    expiration_days = 90;
                    break;
                case 2430367: //警車3天使用券
                    mountid = 1115;
                    expiration_days = 3;
                    break;
                case 2430365: //pony
                    mountid = 1025;
                    expiration_days = 365;
                    break;
                case 2430366: //pony
                    mountid = 1025;
                    expiration_days = 15;
                    break;
                case 2430369: //夢魘騎寵10天使用券
                    mountid = 1049;
                    expiration_days = 10;
                    break;
                case 2430392: //冒險騎士團高速電車90天使用券
                    mountid = 80001038;
                    expiration_days = 90;
                    break;
                case 2430476: //red truck? but name is pegasus?
                    mountid = 1039;
                    expiration_days = 15;
                    break;
                case 2430477: //red truck? but name is pegasus?
                    mountid = 1039;
                    expiration_days = 365;
                    break;
                case 2430232: //福袋10天使用券
                    mountid = 1106;
                    expiration_days = 10;
                    break;
                case 2430511: //spiegel
                    mountid = 80001033;
                    expiration_days = 15;
                    break;
                case 2430512: //rspiegel
                    mountid = 80001033;
                    expiration_days = 365;
                    break;
                case 2430536: //GO兔冒險永久權交換券
                    mountid = 80001114;
                    expiration_days = -1;
                    break;
                case 2430537: //GO兔冒險90天權交換券
                    mountid = 80001114;
                    expiration_days = 90;
                    break;
                case 2430229: //bunny rickshaw 60 day
                    mountid = 1102;
                    expiration_days = 60;
                    break;
                case 2430199: //聖誕雪橇12小時使用券
                    mountid = 1089;
                    expiration_days = 1;
                    break;
                case 2432311: //聖誕雪橇騎寵永久使用券
                    mountid = 1089;
                    expiration_days = -1;
                    break;
                case 2430211: //賽車30天使用券
                    mountid = 80001009;
                    expiration_days = 30;
                    break;
                case 2430521: //小兔子騎寵30天使用券
                    mountid = 80001326;
                    expiration_days = 30;
                    break;
                case 2432497: //赤兔馬騎寵永久使用券
                    mountid = 80011029;
                    expiration_days = -1;
                    break;
                case 2430707: //好朋友坐騎7天使用券
                    mountid = 80001348;
                    expiration_days = -1;
                    break;
                case 2430464: //國慶紀念版熱氣球永久權
                    mountid = 80001120;
                    expiration_days = -1;
                    break;
                case 2432735: //熊貓騎寵永久券
                    mountid = 80001112;
                    expiration_days = -1;
                    break;
                case 2432733: //雄鷹！騎寵永久券
                    mountid = 80001552;
                    expiration_days = -1;
                    break;
                case 2432487: //LV騎寵永久券 -os
                    mountid = 80001531;
                    expiration_days = -1;
                    break;
                case 2432496: //舞獅騎寵永久使用權 -os
                    mountid = 80011028;
                    expiration_days = -1;
                    break;
                case 2432518: //幽靈馬車騎寵永久使用券 -os
                    mountid = 80011030;
                    expiration_days = -1;
                    break;
                case 2430534: //企鵝永久權使用券
                    mountid = 80001113;
                    expiration_days = -1;
                    break;
//                case 2430620: //紅火恐龍90天使用券
//                    mountid = 80001127;
//                    expiration_days = 30;
//                    break;
                case 2430992: //藏獒騎寵7天使用券
                    mountid = 80001181;
                    expiration_days = 7;
                    break;
                case 2430993: //藏獒騎寵30天使用券
                    mountid = 80001181;
                    expiration_days = 30;
                    break;
                case 2430994: //藏獒騎寵90天使用券
                    mountid = 80001181;
                    expiration_days = 90;
                    break;
//                case 2430794: //宇宙船騎寵 -os
//                    mountid = 80001163;
//                    expiration_days = 7;
//                    break;
//                case 2430726: //雙跳銀狼戰車騎寵30天使用券
//                    mountid = 80001144;
//                    expiration_days = 30;
//                    break;
//                case 2430727: //雙跳紅卡車騎寵30天使用券
//                    mountid = 80001148;
//                    expiration_days = 30;
//                    break;
//                case 2430728: //雙跳強力機甲騎寵30天使用券
//                    mountid = 80001149;
//                    expiration_days = 30;
//                    break;
//                case 2431364: //雙變形金剛騎寵30天使用券
//                    mountid = 80001183;
//                    expiration_days = 30;
//                    break;
//                case 2430934: //雙神獸騎寵永久使用券
//                    mountid = 80001185;
//                    expiration_days = -1;
//                    break;
//                case 2430936: //雙熊熱氣球騎寵永久使用券
//                    mountid = 80001187;
//                    expiration_days = -1;
//                    break;
//                case 2430933: //雙夢魘騎寵永久使用券
//                    mountid = 80001184;
//                    expiration_days = -1;
//                    break;
//                case 2430935: //雙貓頭鷹騎寵永久使用券
//                    mountid = 80001186;
//                    expiration_days = -1;
//                    break;
//                case 2431369: //雙兔車騎寵30天使用券
//                    mountid = 80001173;
//                    expiration_days = 30;
//                    break;
//                case 2431370: //雙花蘑菇騎寵30天使用券
//                    mountid = 80001174;
//                    expiration_days = 30;
//                    break;
//                case 2431371: //雙超級兔子騎寵30天使用券
//                    mountid = 80001175;
//                    expiration_days = 30;
//                    break;
//                case 2430937: //雙馬克西姆斯騎寵永久使用券
//                    mountid = 80001193;
//                    expiration_days = -1;
//                    break;
                case 2430938: //紅色皮卡永久使用券 -os
                    mountid = 80001194;
                    expiration_days = -1;
                    break;
                case 2430939: //雙強力滑車騎寵永久使用券
                    mountid = 80001195;
                    expiration_days = -1;
                    break;
                case 2430968: //雙粉紅飛馬永久使用券
                    mountid = 80001196;
                    expiration_days = -1;
                    break;
                case 2431137: //幻龍騎寵永久使用券
                    mountid = 80001198;
                    expiration_days = -1;
                    break;
                case 2431073: //二連跳青蛙永久使用券
                    mountid = 80001199;
                    expiration_days = -1;
                    break;
                case 2431135: //與你相伴幻影騎寵永久使用券
                    mountid = 80001220;
                    expiration_days = -1;
                    break;
                case 2431136: //與你相伴阿莉亞騎寵永久使用券
                    mountid = 80001221;
                    expiration_days = -1;
                    break;
                case 2431268: //瑪瑙美洲豹永久使用券
                    mountid = 80001228;
                    expiration_days = -1;
                    break;
                case 2431353: //黑飛龍騎寵永久使用券
                    mountid = 80001237;
                    expiration_days = -1;
                    break;
                case 2431362: //禮物雪球永久使用券
                    mountid = 80001240;
                    expiration_days = -1;
                    break;
//                case 2431415: //蝴蝶鞦韆騎寵永久使用券
//                    mountid = 80001241;
//                    expiration_days = -1;
//                    break;
                case 2431423: //天空自行車騎寵永久使用券
                    mountid = 80001243;
                    expiration_days = -1;
                    break;
                case 2431424: //雪花騎寵永久使用券
                    mountid = 80011175;
                    expiration_days = -1;
                    break;
                case 2431425: //烏雲騎寵永久使用券
                    mountid = 80001245;
                    expiration_days = -1;
                    break;
                case 2431426: //月亮騎寵永久使用券
                    mountid = 80001645;
                    expiration_days = -1;
                    break;
                case 2431473: //和皮卡啾一起旅行騎寵永久使用券
                    mountid = 80001257;
                    expiration_days = -1;
                    break;
                case 2431474: //和布萊克繽一起旅行騎寵永久使用券
                    mountid = 80001258;
                    expiration_days = -1;
                    break;
                case 2434377: //不想長大！騎寵永久使用券
                    mountid = 80001792;
                    expiration_days = -1;
                    break;
                case 2434379: //騎士團花轎騎寵永久使用券
                    mountid = 80001790;
                    expiration_days = -1;
                    break;
                case 2434277: //極光鹿騎寵永久使用券
                    mountid = 80001786;
                    expiration_days = -1;
                    break;
//                case 2432820: //可愛貓咪人力車騎寵永久使用券
//                    mountid = 80011093;
//                    expiration_days = -1;
//                    break;
//                case 2432821: //摩托艇騎寵永久使用券
//                    mountid = 80011094;
//                    expiration_days = -1;
//                    break;
                case 2432172: //兔兔賞月騎寵永久使用券
                    mountid = 80001410;
                    expiration_days = -1;
                    break;
                case 2432992: //蜜蝴蝶騎寵永久使用券
                    mountid = 80011109;
                    expiration_days = -1;
                    break;
                case 2433069: //迷你太空船永久使用券
                    mountid = 80011110;
                    expiration_days = -1;
                    break;
                case 2432806: //官轎騎寵永久券
                    mountid = 80001557;
                    expiration_days = -1;
                    break;
                case 2432994: //希望敞篷車騎寵
                    mountid = 80001561;
                    expiration_days = -1;
                    break;
                case 2432995: //後起跑車騎寵
                    mountid = 80001562;
                    expiration_days = -1;
                    break;
                case 2432996: //夢幻跑車騎寵
                    mountid = 80001563;
                    expiration_days = -1;
                    break;
                case 2432997: //新手滑板騎寵
                    mountid = 80001564;
                    expiration_days = -1;
                    break;
                case 2432998: //初級滑板騎寵
                    mountid = 80001565;
                    expiration_days = -1;
                    break;
                case 2432999: //普通滑板騎寵
                    mountid = 80001566;
                    expiration_days = -1;
                    break;
                case 2433000: //高級滑板騎寵
                    mountid = 80001567;
                    expiration_days = -1;
                    break;
                case 2433001: //精英滑板騎寵
                    mountid = 80001568;
                    expiration_days = -1;
                    break;
                case 2433002: //傳說滑板騎寵
                    mountid = 80001569;
                    expiration_days = -1;
                    break;
                case 2433003: //閃耀氣球騎寵
                    mountid = 80001570;
                    expiration_days = -1;
                    break;
                case 2433051: //貓咪手推車騎寵永久券
                    mountid = 80001582;
                    expiration_days = -1;
                    break;
                case 2433053: //噴氣式滑艇騎寵永久券
                    mountid = 80001584;
                    expiration_days = -1;
                    break;
                case 2431898: //彈跳車騎寵永久券
                    mountid = 80001324;
                    expiration_days = -1;
                    break;
                case 2431914: //小兔子騎寵30天使用券
                    mountid = 80001326;
                    expiration_days = 30;
                    break;
                case 2431915: //鳥叔郵遞員永久使用券
                    mountid = 80001327;
                    expiration_days = -1;
                    break;
                case 2432003: //戰鬥飛艇騎寵使用券
                    mountid = 80001331;
                    expiration_days = 10;
                    break;
                case 2432007: //海加頓之拳騎寵使用券
                    mountid = 80001345;
                    expiration_days = 10;
                    break;
                case 2432029: //老式戰船騎寵90天券
                    mountid = 80001346;
                    expiration_days = 90;
                    break;
                case 2432030: //石像鬼騎寵永久券
                    mountid = 80001347;
                    expiration_days = -1;
                    break;
                case 2432031: //好友騎寵永久券
                    mountid = 80001348;
                    expiration_days = -1;
                    break;
                case 2432078: //地獄犬騎寵永久券
                    mountid = 80001353;
                    expiration_days = -1;
                    break;
                case 2432085: //海豚騎寵永久券
                    mountid = 80001355;
                    expiration_days = -1;
                    break;
                case 2431883: //沙雲騎寵永久使用券
                    mountid = 80001330;
                    expiration_days = -1;
                    break;
                case 2431765: //搖搖木馬騎寵券
                    mountid = 80001290;
                    expiration_days = -1;
                    break;
                case 2432015: //赤紅沙雲騎寵永久使用券
                    mountid = 80001333;
                    expiration_days = -1;
                    break;
                case 2432099: //巨大公雞騎寵30天券
                    mountid = 80001336;
                    expiration_days = 30;
                    break;
                case 2431950: //巨大公雞騎寵90天券
                    mountid = 80001337;
                    expiration_days = 90;
                    break;
                case 2432149: //稻香四溢騎寵永久使用券
                    mountid = 80001398;
                    expiration_days = -1;
                    break;
                case 2432151: //飛行床騎寵永久券
                    mountid = 80001400;
                    expiration_days = -1;
                    break;
                case 2432309: //吉尼騎寵永久使用券
                    mountid = 80001404;
                    expiration_days = -1;
                    break;
                case 2432328: //Naver帽子騎寵30天使用券
                    mountid = 80001435;
                    expiration_days = 30;
                    break;
                case 2432216: //殭屍卡車永久券
                    mountid = 80001411;
                    expiration_days = -1;
                    break;
                case 2432218: //妮娜的魔法陣騎寵永久券
                    mountid = 80001413;
                    expiration_days = -1;
                    break;
                case 2432291: //滑板永久使用券
                    mountid = 80001419;
                    expiration_days = -1;
                    break;
                case 2432293: //南瓜馬車永久使用券
                    mountid = 80001421;
                    expiration_days = -1;
                    break;
                case 2432295: //貝倫騎寵永久使用券
                    mountid = 80001423;
                    expiration_days = -1;
                    break;
                case 2432347: //南哈特和雪原永久騎寵交換券
                    mountid = 80001440;
                    expiration_days = -1;
                    break;
                case 2432348: //西格諾斯和雪原永久騎寵交換券
                    mountid = 80001441;
                    expiration_days = -1;
                    break;
                case 2432349: //殺人鯨和雪原永久騎寵交換券
                    mountid = 80001442;
                    expiration_days = -1;
                    break;
                case 2432350: //白魔法師和雪原騎寵1天交換券
                    mountid = 80001443;
                    expiration_days = -1;
                    break;
                case 2432351: //希拉和雪原永久騎寵交換券
                    mountid = 80001444;
                    expiration_days = -1;
                    break;
                case 2432431: //龍騎士騎寵永久使用券
                    mountid = 80001480;
                    expiration_days = -1;
                    break;
                case 2432433: //魔法掃帚騎寵永久使用券
                    mountid = 80001482;
                    expiration_days = -1;
                    break;
                case 2432449: //飛鞋騎寵永久使用券
                    mountid = 80001484;
                    expiration_days = -1;
                    break;
                case 2432582: //遲到了！騎寵永久使用券
                    mountid = 80001505;
                    expiration_days = -1;
                    break;
                case 2432498: //藍焰夢魘騎寵永久使用券
                    mountid = 80001508;
                    expiration_days = -1;
                    break;
                case 2432500: //新年快樂騎寵永久使用券
                    mountid = 80001510;
                    expiration_days = -1;
                    break;
                case 2432645: //毛驢騎寵永久券
                    mountid = 80001531;
                    expiration_days = -1;
                    break;
                case 2432653: //舞動的向日葵騎寵
                    mountid = 80001533;
                    expiration_days = -1;
                    break;
//                case 2432654: //花瓣螺旋槳騎寵90天券
//                    mountid = 80001534;
//                    expiration_days = 90;
//                    break;
                case 2434127: //菇菇有點腫騎寵永久券
                    mountid = 80001549;
                    expiration_days = -1;
                    break;
                case 2433499: //殺人鯨之攙扶永久券
                    mountid = 80001671;
                    expiration_days = -1;
                    break;
                case 2433501: //赫麗娜之攙扶永久券
                    mountid = 80001673;
                    expiration_days = -1;
                    break;
                case 2433735: //飛翔青羊騎寵永久券
                    mountid = 80001707;
                    expiration_days = -1;
                    break;
                case 2433736: //飛翔粉羊騎寵永久券
                    mountid = 80001708;
                    expiration_days = -1;
                    break;
                case 2433809: //拉拉喵騎寵永久券
                    mountid = 80001711;
                    expiration_days = -1;
                    break;
                case 2433811: //草莓蛋糕騎寵永久券
                    mountid = 80001713;
                    expiration_days = -1;
                    break;
                case 2433292: //蛋糕騎寵永久使用券
                    mountid = 80011139;
                    expiration_days = -1;
                    break;
                case 2433293: //馴鹿雪橇騎寵永久使用券
                    mountid = 80011140;
                    expiration_days = -1;
                    break;
                case 2433497: //美羊羊騎寵永久交換券
                    mountid = 80011147;
                    expiration_days = -1;
                    break;
                case 2433511: //喜羊羊騎寵永久交換券
                    mountid = 80011148;
                    expiration_days = -1;
                    break;
                case 2434084: //黑色天使騎寵永久交換券
                    mountid = 80001701;
                    expiration_days = -1;
                    break;
                case 2434142: //蓋奧勒克騎寵使用券
                    mountid = 80011205;
                    expiration_days = -1;
                    break;
                case 2434143: //粉碎機騎寵使用券
                    mountid = 80011206;
                    expiration_days = -1;
                    break;
                case 2434235: //變身雲朵騎寵
                    mountid = 80011236;
                    expiration_days = -1;
                    break;
                case 2434236: //呆萌鯨魚騎寵
                    mountid = 80011237;
                    expiration_days = -1;
                    break;
                case 2434037: //迷你黑色天堂騎寵券
                    mountid = 80011157;
                    expiration_days = -1;
                    break;
                case 2433836: //拯救騎寵
                    mountid = 80011179;
                    expiration_days = -1;
                    break;
                case 2433058: //永久性噴氣式滑艇載具優惠券
                    mountid = 80011180;
                    expiration_days = -1;
                    break;
                case 2433059: //永久性潛水艇載具優惠券
                    mountid = 80011181;
                    expiration_days = -1;
                    break;
                case 2433060: //永久性直升機載具優惠券
                    mountid = 80011182;
                    expiration_days = -1;
                    break;
                case 2433168: //永久性熱氣球載具優惠券
                    mountid = 80011183;
                    expiration_days = -1;
                    break;
                case 2433169: //永久性獨木舟載具優惠券
                    mountid = 80011184;
                    expiration_days = -1;
                    break;
                case 2433170: //永久性迷你直升機載具優惠券
                    mountid = 80011185;
                    expiration_days = -1;
                    break;
                case 2433198: //永久性貓咪海盜船載具優惠券
                    mountid = 80011186;
                    expiration_days = -1;
                    break;
                case 2433881: //橄欖球兔騎寵
                    mountid = 80011190;
                    expiration_days = -1;
                    break;
                case 2433876: //王權馬車騎寵使用券
                    mountid = 80011189;
                    expiration_days = -1;
                    break;
                case 2434082: //金魚出租車騎寵永久交換券
                    mountid = 80011199;
                    expiration_days = -1;
                    break;
                case 2434083: //金龍魚騎寵永久使用券
                    mountid = 80011200;
                    expiration_days = -1;
                    break;
                case 2435116: {
                    mountid = 80011303;
                    break;
                }
                case 2435133: {
                    mountid = 80011289;
                    expiration_days = 30;
                    break;
                }
                case 2435036: {
                    mountid = 80011289;
                    expiration_days = -1;
                    break;
                }
                case 2434965: {
                    mountid = 80011279;
                    expiration_days = 30;
                    break;
                }
                case 2434867: {
                    mountid = 80011279;
                    expiration_days = 90;
                    break;
                }
                case 2434360: {
                    mountid = 80011279;
                    expiration_days = -1;
                    break;
                }
                case 2434690: {
                    mountid = 80011272;
                    expiration_days = -1;
                    break;
                }
                case 2434618: {
                    mountid = 80011263;
                    expiration_days = -1;
                    break;
                }
                case 2434603: {
                    mountid = 80011262;
                    expiration_days = -1;
                    break;
                }
                case 2433742: {
                    mountid = 80011148;
                    expiration_days = 30;
                    break;
                }
                case 2433743: {
                    mountid = 80011147;
                    expiration_days = 30;
                    break;
                }
                case 2434163: {
                    mountid = 80011027;
                    expiration_days = -1;
                    break;
                }
                case 2432483: {
                    mountid = 80011027;
                    expiration_days = 90;
                    break;
                }
                case 2434737: {
                    mountid = 80001923;
                    expiration_days = -1;
                    break;
                }
                case 2434649: {
                    mountid = 80001918;
                    expiration_days = -1;
                    break;
                }
                case 2435103: {
                    mountid = 80001814;
                    expiration_days = 90;
                    break;
                }
                case 2434518: {
                    mountid = 80001814;
                    expiration_days = 90;
                    break;
                }
                case 2434517: {
                    mountid = 80001814;
                    expiration_days = -1;
                    break;
                }
                case 2434516: {
                    mountid = 80001811;
                    expiration_days = 90;
                    break;
                }
                case 2434515: {
                    mountid = 80001811;
                    expiration_days = -1;
                    break;
                }
                case 2434378: {
                    mountid = 80001792;
                    expiration_days = 90;
                    break;
                }
                case 2434380: {
                    mountid = 80001790;
                    expiration_days = 90;
                    break;
                }
                case 2434278: {
                    mountid = 80001787;
                    expiration_days = 90;
                    break;
                }
                case 2434276: {
                    mountid = 80001785;
                    expiration_days = 90;
                    break;
                }
                case 2434275: {
                    mountid = 80001784;
                    expiration_days = -1;
                    break;
                }
                case 2434079: {
                    mountid = 80001779;
                    expiration_days = -1;
                    break;
                }
                case 2434080: {
                    mountid = 80001778;
                    expiration_days = 90;
                    break;
                }
                case 2434078: {
                    mountid = 80001777;
                    expiration_days = 90;
                    break;
                }
                case 2434077: {
                    mountid = 80001776;
                    expiration_days = -1;
                    break;
                }
                case 2434013: {
                    mountid = 80001775;
                    expiration_days = 30;
                    break;
                }
                case 2434025: {
                    mountid = 80001774;
                    expiration_days = 30;
                    break;
                }
                case 2433949: {
                    mountid = 80001767;
                    expiration_days = 90;
                    break;
                }
                case 2433948: {
                    mountid = 80001766;
                    expiration_days = -1;
                    break;
                }
                case 2433947: {
                    mountid = 80001765;
                    expiration_days = 90;
                    break;
                }
                case 2433946: {
                    mountid = 80001764;
                    expiration_days = -1;
                    break;
                }
                case 2433932: {
                    mountid = 80001763;
                    expiration_days = 30;
                    break;
                }
                case 2433812: {
                    mountid = 80001714;
                    expiration_days = 90;
                    break;
                }
                case 2433810: {
                    mountid = 80001712;
                    expiration_days = 90;
                    break;
                }
                case 2433734: {
                    mountid = 80001708;
                    expiration_days = 90;
                    break;
                }
                case 2433500: {
                    mountid = 80001673;
                    expiration_days = 90;
                    break;
                }
                case 2433498: {
                    mountid = 80001671;
                    expiration_days = 90;
                    break;
                }
                case 2431542: {
                    mountid = 80001645;
                    expiration_days = 90;
                    break;
                }
                case 2431530: {
                    mountid = 80001645;
                    expiration_days = 30;
                    break;
                }
                case 2433350: {
                    mountid = 80001628;
                    expiration_days = 90;
                    break;
                }
                case 2433349: {
                    mountid = 80001627;
                    expiration_days = -1;
                    break;
                }
                case 2433348: {
                    mountid = 80001626;
                    expiration_days = 90;
                    break;
                }
                case 2433347: {
                    mountid = 80001625;
                    expiration_days = -1;
                    break;
                }
                case 2433346: {
                    mountid = 80001624;
                    expiration_days = 90;
                    break;
                }
                case 2433345: {
                    mountid = 80001623;
                    expiration_days = -1;
                    break;
                }
                case 2433277: {
                    mountid = 80001622;
                    expiration_days = 90;
                    break;
                }
                case 2433276: {
                    mountid = 80001621;
                    expiration_days = -1;
                    break;
                }
                case 2433275: {
                    mountid = 80001620;
                    expiration_days = 90;
                    break;
                }
                case 2433274: {
                    mountid = 80001619;
                    expiration_days = -1;
                    break;
                }
                case 2433273: {
                    mountid = 80001618;
                    expiration_days = 90;
                    break;
                }
                case 2433272: {
                    mountid = 80001617;
                    expiration_days = -1;
                    break;
                }
                case 2433054: {
                    mountid = 80001585;
                    expiration_days = 90;
                    break;
                }
                case 2433052: {
                    mountid = 80001583;
                    expiration_days = 90;
                    break;
                }
                case 2432807: {
                    mountid = 80001558;
                    expiration_days = 90;
                    break;
                }
                case 2432752: {
                    mountid = 80001555;
                    expiration_days = 90;
                    break;
                }
                case 2432751: {
                    mountid = 80001554;
                    expiration_days = -1;
                    break;
                }
                case 2432734: {
                    mountid = 80001553;
                    expiration_days = 90;
                    break;
                }
                case 2432501: {
                    mountid = 80001511;
                    expiration_days = 90;
                    break;
                }
                case 2432499: {
                    mountid = 80001509;
                    expiration_days = 90;
                    break;
                }
                case 2432583: {
                    mountid = 80001506;
                    expiration_days = 90;
                    break;
                }
                case 2432581: {
                    mountid = 80001504;
                    expiration_days = 90;
                    break;
                }
                case 2432580: {
                    mountid = 80001503;
                    expiration_days = -1;
                    break;
                }
                case 2432552: {
                    mountid = 80001492;
                    expiration_days = -1;
                    break;
                }
                case 2432528: {
                    mountid = 80001491;
                    expiration_days = 90;
                    break;
                }
                case 2432527: {
                    mountid = 80001490;
                    expiration_days = 90;
                    break;
                }
                case 2432450: {
                    mountid = 80001485;
                    expiration_days = 90;
                    break;
                }
                case 2432434: {
                    mountid = 80001483;
                    expiration_days = 90;
                    break;
                }
                case 2432432: {
                    mountid = 80001481;
                    expiration_days = 90;
                    break;
                }
                case 2432362: {
                    mountid = 80001448;
                    expiration_days = 90;
                    break;
                }
                case 2432361: {
                    mountid = 80001447;
                    expiration_days = 30;
                    break;
                }
                case 2432296: {
                    mountid = 80001424;
                    expiration_days = 90;
                    break;
                }
                case 2432294: {
                    mountid = 80001422;
                    expiration_days = 90;
                    break;
                }
                case 2432292: {
                    mountid = 80001420;
                    expiration_days = 90;
                    break;
                }
                case 2432219: {
                    mountid = 80001414;
                    expiration_days = 90;
                    break;
                }
                case 2432217: {
                    mountid = 80001412;
                    expiration_days = 90;
                    break;
                }
                case 2434567: {
                    mountid = 80001410;
                    expiration_days = 90;
                    break;
                }
                case 2432167: {
                    mountid = 80001403;
                    expiration_days = -1;
                    break;
                }
                case 2432152: {
                    mountid = 80001401;
                    expiration_days = 90;
                    break;
                }
                case 2432135: {
                    mountid = 80001397;
                    expiration_days = 30;
                    break;
                }
                case 2432079: {
                    mountid = 80001354;
                    expiration_days = 90;
                    break;
                }
                case 2432006: {
                    mountid = 80001345;
                    expiration_days = 1;
                    break;
                }
                case 2431949: {
                    mountid = 80001336;
                    expiration_days = -1;
                    break;
                }
                case 2431916: {
                    mountid = 80001328;
                    expiration_days = 90;
                    break;
                }
                case 2431899: {
                    mountid = 80001325;
                    expiration_days = 90;
                    break;
                }
                case 2430079: {
                    mountid = 80001293;
                    expiration_days = 172800000;
                    break;
                }
                case 2431758: {
                    mountid = 80001288;
                    expiration_days = 1440000;
                    break;
                }
                case 2431757: {
                    mountid = 80001287;
                    expiration_days = 7;
                    break;
                }
                case 2431756: {
                    mountid = 80001285;
                    expiration_days = 3;
                    break;
                }
                case 2431755: {
                    mountid = 80001285;
                    expiration_days = 1;
                    break;
                }
                case 2431745: {
                    mountid = 80001278;
                    expiration_days = -1;
                    break;
                }
                case 2431733: {
                    mountid = 80001278;
                    expiration_days = -1;
                    break;
                }
                case 2431722: {
                    mountid = 80001261;
                    expiration_days = 90;
                    break;
                }
                case 2431700: {
                    mountid = 80001261;
                    expiration_days = 30;
                    break;
                }
                case 2431573: {
                    mountid = 80001261;
                    expiration_days = -1;
                    break;
                }
                case 2431464: {
                    mountid = 80001246;
                    expiration_days = -1;
                    break;
                }
                case 2431529: {
                    mountid = 80001245;
                    expiration_days = 30;
                    break;
                }
                case 2431462: {
                    mountid = 80001245;
                    expiration_days = -1;
                    break;
                }
                case 2431541: {
                    mountid = 80001243;
                    expiration_days = 90;
                    break;
                }
                case 2434477: {
                    mountid = 80001196;
                    expiration_days = -1;
                    break;
                }
                case 2431697: {
                    mountid = 80001166;
                    expiration_days = -1;
                    break;
                }
                case 2431833: {
                    mountid = 80001114;
                    expiration_days = 50;
                    break;
                }
                case 2430203: {
                    mountid = 80001084;
                    expiration_days = 30;
                    break;
                }
                case 2430081: {
                    mountid = 80001024;
                    expiration_days = 7;
                    break;
                }
                case 2431698: {
                    mountid = 80001013;
                    expiration_days = -1;
                    break;
                }
                case 2430050: {
                    mountid = 80001504;
                    expiration_days = 5;
                    break;
                }
                case 2434191: {
                    mountid = 80001148;
                    expiration_days = -1;
                    break;
                }
                case 2434161: {
                    mountid = 80001240;
                    expiration_days = 90;
                    break;
                }
                case 2433889: {
                    mountid = 80011194;
                    expiration_days = 90;
                    break;
                }
                case 2433888: {
                    mountid = 80011199;
                    expiration_days = 15;
                    break;
                }
                case 2433884: {
                    mountid = 80001057;
                    expiration_days = 14;
                    break;
                }
                case 2433866: {
                    mountid = 80011186;
                    expiration_days = 90;
                    break;
                }
                case 2433865: {
                    mountid = 80011136;
                    expiration_days = 90;
                    break;
                }
                case 2433864: {
                    mountid = 80011184;
                    expiration_days = 90;
                    break;
                }
                case 2433863: {
                    mountid = 80011183;
                    expiration_days = 90;
                    break;
                }
                case 2433862: {
                    mountid = 80011182;
                    expiration_days = 90;
                    break;
                }
                case 2433861: {
                    mountid = 80011181;
                    expiration_days = 90;
                    break;
                }
                case 2433860: {
                    mountid = 80011180;
                    expiration_days = 90;
                    break;
                }
                case 2433805: {
                    mountid = 80011109;
                    expiration_days = -1;
                    break;
                }
                case 2433729: {
                    mountid = 80011025;
                    expiration_days = -1;
                    break;
                }
                case 2433718: {
                    mountid = 80001019;
                    expiration_days = -1;
                    break;
                }
                case 2433707: {
                    mountid = 80001244;
                    expiration_days = -1;
                    break;
                }
                case 2433659: {
                    mountid = 80001703;
                    expiration_days = 30;
                    break;
                }
                case 2433658: {
                    mountid = 80001703;
                    expiration_days = 30;
                    break;
                }
                case 2433603: {
                    mountid = 80001244;
                    expiration_days = 30;
                    break;
                }
                case 2433567: {
                    mountid = 80001191;
                    expiration_days = 30;
                    break;
                }
                case 2433566: {
                    mountid = 80001190;
                    expiration_days = 30;
                    break;
                }
                case 2433565: {
                    mountid = 80001189;
                    expiration_days = 30;
                    break;
                }
                case 2433564: {
                    mountid = 80001188;
                    expiration_days = 30;
                    break;
                }
                case 2433513: {
                    mountid = 80001025;
                    expiration_days = 7;
                    break;
                }
                case 2433461: {
                    mountid = 80001645;
                    expiration_days = -1;
                    break;
                }
                case 2433460: {
                    mountid = 80001644;
                    expiration_days = -1;
                    break;
                }
                case 2433459: {
                    mountid = 80001504;
                    expiration_days = -1;
                    break;
                }
                case 2433458: {
                    mountid = 80001029;
                    expiration_days = -1;
                    break;
                }
                case 2433454: {
                    mountid = 80001023;
                    expiration_days = 7;
                    break;
                }
                case 2433406: {
                    mountid = 80001640;
                    expiration_days = 30;
                    break;
                }
                case 2433405: {
                    mountid = 80001639;
                    expiration_days = 30;
                    break;
                }
                case 2433325: {
                    mountid = 80011139;
                    expiration_days = 90;
                    break;
                }
                case 2433324: {
                    mountid = 80001022;
                    expiration_days = 30;
                    break;
                }
                case 2433006: {
                    mountid = 80011062;
                    expiration_days = 30;
                    break;
                }
                case 2432989: {
                    mountid = 80001410;
                    expiration_days = 30;
                    break;
                }
                case 2432835: {
                    mountid = 80011095;
                    expiration_days = 30;
                    break;
                }
                case 2432821: {
                    mountid = 80011094;
                    expiration_days = -1;
                    break;
                }
                case 2432820: {
                    mountid = 80011093;
                    expiration_days = -1;
                    break;
                }
                case 2432736: {
                    mountid = 80001551;
                    expiration_days = 90;
                    break;
                }
                case 2432724: {
                    mountid = 80001549;
                    expiration_days = 90;
                    break;
                }
                case 2432654: {
                    mountid = 80001782;
                    expiration_days = 90;
                    break;
                }
                case 2432646: {
                    mountid = 80001532;
                    expiration_days = 90;
                    break;
                }
                case 2432635: {
                    mountid = 80001517;
                    expiration_days = 90;
                    break;
                }
                case 2432437: {
                    mountid = 80011025;
                    expiration_days = -1;
                    break;
                }
                case 2432243: {
                    mountid = 80001026;
                    expiration_days = 30;
                    break;
                }
                case 2432191: {
                    mountid = 80001196;
                    expiration_days = -1;
                    break;
                }
                case 2432190: {
                    mountid = 80001166;
                    expiration_days = -1;
                    break;
                }
                case 2432189: {
                    mountid = 80001329;
                    expiration_days = -1;
                    break;
                }
                case 2432170: {
                    mountid = 80001261;
                    expiration_days = 90;
                    break;
                }
                case 2432110: {
                    mountid = 80001222;
                    expiration_days = -1;
                    break;
                }
                case 2432106: {
                    mountid = 80001221;
                    expiration_days = 365;
                    break;
                }
                case 2432105: {
                    mountid = 80001220;
                    expiration_days = 365;
                    break;
                }
                case 2432104: {
                    mountid = 80001290;
                    expiration_days = 90;
                    break;
                }
                case 2432100: {
                    mountid = 80001335;
                    expiration_days = -1;
                    break;
                }
                case 2432086: {
                    mountid = 80001356;
                    expiration_days = 90;
                    break;
                }
                case 2432008: {
                    mountid = 80001345;
                    expiration_days = 1;
                    break;
                }
                case 2431951: {
                    mountid = 80001293;
                    expiration_days = 172800000;
                    break;
                }
                case 2431856: {
                    mountid = 80001304;
                    expiration_days = -1;
                    break;
                }
                case 2431800: {
                    mountid = 80001303;
                    expiration_days = 90;
                    break;
                }
                case 2431799: {
                    mountid = 80001302;
                    expiration_days = -1;
                    break;
                }
                case 2431798: {
                    mountid = 80001301;
                    expiration_days = 90;
                    break;
                }
                case 2431797: {
                    mountid = 80001300;
                    expiration_days = -1;
                    break;
                }
                case 2431779: {
                    mountid = 80001290;
                    expiration_days = 90;
                    break;
                }
                case 2431778: {
                    mountid = 80001294;
                    expiration_days = 90;
                    break;
                }
                case 2431777: {
                    mountid = 80011000;
                    expiration_days = 90;
                    break;
                }
                case 2431764: {
                    mountid = 80001294;
                    expiration_days = -1;
                    break;
                }
                case 2431760: {
                    mountid = 80001291;
                    expiration_days = 30;
                    break;
                }
                case 2431528: {
                    mountid = 80011175;
                    expiration_days = 30;
                    break;
                }
                case 2431527: {
                    mountid = 80001243;
                    expiration_days = 30;
                    break;
                }
                case 2431506: {
                    mountid = 80001020;
                    expiration_days = 30;
                    break;
                }
                case 2431505: {
                    mountid = 80001119;
                    expiration_days = 30;
                    break;
                }
                case 2431504: {
                    mountid = 80001111;
                    expiration_days = 30;
                    break;
                }
                case 2431503: {
                    mountid = 80001030;
                    expiration_days = 30;
                    break;
                }
                case 2431502: {
                    mountid = 80001005;
                    expiration_days = 30;
                    break;
                }
                case 2431501: {
                    mountid = 80001003;
                    expiration_days = 30;
                    break;
                }
                case 2431500: {
                    mountid = 80001018;
                    expiration_days = 30;
                    break;
                }
                case 2431499: {
                    mountid = 80001009;
                    expiration_days = 30;
                    break;
                }
                case 2431498: {
                    mountid = 80011289;
                    expiration_days = 30;
                    break;
                }
                case 2431497: {
                    mountid = 80001004;
                    expiration_days = 30;
                    break;
                }
                case 2431496: {
                    mountid = 80001026;
                    expiration_days = 30;
                    break;
                }
                case 2431495: {
                    mountid = 80001025;
                    expiration_days = 30;
                    break;
                }
                case 2431494: {
                    mountid = 80001015;
                    expiration_days = 30;
                    break;
                }
                case 2431493: {
                    mountid = 80001013;
                    expiration_days = 30;
                    break;
                }
                case 2431492: {
                    mountid = 80001006;
                    expiration_days = 30;
                    break;
                }
                case 2431491: {
                    mountid = 80001021;
                    expiration_days = 30;
                    break;
                }
                case 2431490: {
                    mountid = 80001199;
                    expiration_days = 30;
                    break;
                }
                case 2431458: {
                    mountid = 80001243;
                    expiration_days = -1;
                    break;
                }
                case 2431454: {
                    mountid = 80001241;
                    expiration_days = -1;
                    break;
                }
                case 2431452: {
                    mountid = 80001250;
                    expiration_days = -1;
                    break;
                }
                case 2431422: {
                    mountid = 80001237;
                    expiration_days = -1;
                    break;
                }
                case 2431415: {
                    mountid = 80001241;
                    expiration_days = -1;
                    break;
                }
                case 2431393: {
                    mountid = 80011028;
                    expiration_days = -1;
                    break;
                }
                case 2431392: {
                    mountid = 80011028;
                    expiration_days = 365;
                    break;
                }
                case 2431391: {
                    mountid = 80011028;
                    expiration_days = 90;
                    break;
                }
                case 2431372: {
                    mountid = 80011028;
                    expiration_days = 30;
                    break;
                }
                case 2431371: {
                    mountid = 80001175;
                    expiration_days = 30;
                    break;
                }
                case 2431370: {
                    mountid = 80001174;
                    expiration_days = 30;
                    break;
                }
                case 2431369: {
                    mountid = 80001173;
                    expiration_days = 30;
                    break;
                }
                case 2431368: {
                    mountid = 80001191;
                    expiration_days = 30;
                    break;
                }
                case 2431367: {
                    mountid = 80001189;
                    expiration_days = 30;
                    break;
                }
                case 2431366: {
                    mountid = 80001187;
                    expiration_days = 30;
                    break;
                }
                case 2431365: {
                    mountid = 80001190;
                    expiration_days = 30;
                    break;
                }
                case 2431364: {
                    mountid = 80001188;
                    expiration_days = 30;
                    break;
                }
                case 2431267: {
                    mountid = 80001228;
                    expiration_days = -1;
                    break;
                }
                case 2431134: {
                    mountid = 80001221;
                    expiration_days = 7;
                    break;
                }
                case 2431133: {
                    mountid = 80001220;
                    expiration_days = 7;
                    break;
                }
                case 2431044: {
                    mountid = 80001198;
                    expiration_days = 30;
                    break;
                }
                case 2430991: {
                    mountid = 80001174;
                    expiration_days = 30;
                    break;
                }
                case 2430948: {
                    mountid = 80001190;
                    expiration_days = -1;
                    break;
                }
                case 2430937: {
                    mountid = 80001193;
                    expiration_days = -1;
                    break;
                }
                case 2430936: {
                    mountid = 80001192;
                    expiration_days = -1;
                    break;
                }
                case 2430935: {
                    mountid = 80001191;
                    expiration_days = -1;
                    break;
                }
                case 2430934: {
                    mountid = 80001190;
                    expiration_days = -1;
                    break;
                }
                case 2430933: {
                    mountid = 80001189;
                    expiration_days = -1;
                    break;
                }
                case 2430932: {
                    mountid = 80001188;
                    expiration_days = -1;
                    break;
                }
                case 2430931: {
                    mountid = 80001187;
                    expiration_days = 30;
                    break;
                }
                case 2430930: {
                    mountid = 80001186;
                    expiration_days = 30;
                    break;
                }
                case 2430929: {
                    mountid = 80001185;
                    expiration_days = 30;
                    break;
                }
                case 2430928: {
                    mountid = 80001184;
                    expiration_days = 30;
                    break;
                }
                case 2430927: {
                    mountid = 80001183;
                    expiration_days = 30;
                    break;
                }
                case 2430918: {
                    mountid = 80001181;
                    expiration_days = 172800000;
                    break;
                }
                case 2430908: {
                    mountid = 80001175;
                    expiration_days = 30;
                    break;
                }
                case 2430907: {
                    mountid = 80001174;
                    expiration_days = 30;
                    break;
                }
                case 2430906: {
                    mountid = 80001173;
                    expiration_days = 30;
                    break;
                }
                case 2430871: {
                    mountid = 80001006;
                    expiration_days = 7;
                    break;
                }
                case 2430794: {
                    mountid = 80001163;
                    expiration_days = 7;
                    break;
                }
                case 2430728: {
                    mountid = 80001149;
                    expiration_days = 30;
                    break;
                }
                case 2430727: {
                    mountid = 80001148;
                    expiration_days = 30;
                    break;
                }
                case 2430726: {
                    mountid = 80001144;
                    expiration_days = 30;
                    break;
                }
                case 2430719: {
                    mountid = 80001025;
                    expiration_days = 30;
                    break;
                }
                case 2430718: {
                    mountid = 80001013;
                    expiration_days = 30;
                    break;
                }
                case 2430717: {
                    mountid = 80001504;
                    expiration_days = 30;
                    break;
                }
                case 2430654: {
                    mountid = 80001113;
                    expiration_days = 30;
                    break;
                }
                case 2430634: {
                    mountid = 80001006;
                    expiration_days = 30;
                    break;
                }
                case 2430633: {
                    mountid = 80001024;
                    expiration_days = 30;
                    break;
                }
                case 2430619: {
                    mountid = 80001113;
                    expiration_days = 15;
                    break;
                }
                case 2430617: {
                    mountid = 80001112;
                    expiration_days = 15;
                    break;
                }
                case 2430616: {
                    mountid = 80001114;
                    expiration_days = 15;
                    break;
                }
                case 2430615: {
                    mountid = 80001113;
                    expiration_days = 7;
                    break;
                }
                case 2430614: {
                    mountid = 80001112;
                    expiration_days = 7;
                    break;
                }
                case 2430613: {
                    mountid = 80001114;
                    expiration_days = 7;
                    break;
                }
                case 2430610: {
                    mountid = 80001022;
                    expiration_days = 7;
                    break;
                }
                case 2430598: {
                    mountid = 80001019;
                    expiration_days = 3;
                    break;
                }
                case 2430593: {
                    mountid = 80001057;
                    expiration_days = 3;
                    break;
                }
                case 2430585: {
                    mountid = 80001113;
                    expiration_days = 3;
                    break;
                }
                case 2430580: {
                    mountid = 80001112;
                    expiration_days = 3;
                    break;
                }
                case 2430579: {
                    mountid = 80001114;
                    expiration_days = 3;
                    break;
                }
                case 2430566: {
                    mountid = 80001071;
                    expiration_days = 30;
                    break;
                }
                case 2430544: {
                    mountid = 80001002;
                    expiration_days = 7;
                    break;
                }
                case 2430535: {
                    mountid = 80001113;
                    expiration_days = 90;
                    break;
                }
                case 2430533: {
                    mountid = 80001112;
                    expiration_days = 90;
                    break;
                }
                case 2430532: {
                    mountid = 80001112;
                    expiration_days = -1;
                    break;
                }
                case 2430518: {
                    mountid = 80001090;
                    expiration_days = 30;
                    break;
                }
                case 2430508: {
                    mountid = 80001084;
                    expiration_days = 30;
                    break;
                }
                case 2430507: {
                    mountid = 80001083;
                    expiration_days = 30;
                    break;
                }
                case 2430506: {
                    mountid = 80001082;
                    expiration_days = 30;
                    break;
                }
                case 2430480: {
                    mountid = 80001239;
                    expiration_days = 30;
                    break;
                }
                case 2430475: {
                    mountid = 80001121;
                    expiration_days = 30;
                    break;
                }
                case 2430458: {
                    mountid = 80001326;
                    expiration_days = 7;
                    break;
                }
                case 2430206: {
                    mountid = 80001009;
                    expiration_days = 7;
                    break;
                }
                case 2430202: {
                    mountid = 80001326;
                    expiration_days = 15;
                    break;
                }
                case 2430198: {
                    mountid = 80001015;
                    expiration_days = 365;
                    break;
                }
                case 2430196: {
                    mountid = 80001024;
                    expiration_days = 365;
                    break;
                }
                case 2430195: {
                    mountid = 80001017;
                    expiration_days = 365;
                    break;
                }
                case 2430194: {
                    mountid = 80001072;
                    expiration_days = 365;
                    break;
                }
                case 2430578: {
                    mountid = 80001077;
                    expiration_days = 3;
                    break;
                }
                default:
                    if (ItemConstants.isDamageSkinItem(toUse.getItemId())) {
//                        NPCScriptManager.getInstance().startItem(c, info.getNpc(), "DamageSkin", toUse);
                        c.getPlayer().getScriptManager().startItemScript(toUse, info.getNpc(), "DamageSkin");

                    } else {
                        c.getPlayer().getScriptManager().startItemScript(toUse, info.getNpc(), null);

//                        NPCScriptManager.getInstance().startItem(c, info.getNpc(), toUse);
                    }
                    break;
            }
        }
        if (mountid > 0) {
            //System.err.println("騎寵技能 - 1 " + mountid);
            mountid = mountid > 80001000 ? mountid : SkillConstants.getSkillByJob(mountid, c.getPlayer().getJob());
            int fk = GameConstants.getMountItem(mountid, c.getPlayer());
            //System.err.println("騎寵技能 - 2 " + mountid + " fk: " + fk);
            if (fk > 0 && mountid < 80001000) {
                for (int i = 80001001; i < 80001999; i++) {
                    Skill skill = SkillFactory.getSkill(i);
                    if (skill != null && GameConstants.getMountItem(skill.getId(), c.getPlayer()) == fk) {
                        mountid = i;
                        break;
                    }
                }
            }
            //System.err.println("騎寵技能 - 3 " + mountid + " 是否有技能: " + (SkillFactory.getSkill(mountid) == null) + " 騎寵: " + (GameConstants.getMountItem(mountid, c.getPlayer()) == 0));
            if (c.getPlayer().getSkillLevel(mountid) > 0) {
                c.getPlayer().dropMessage(1, "您已經擁有了[" + SkillFactory.getSkill(mountid).getName() + "]這個騎寵的技能，無法使用該道具。");
            } else if (SkillFactory.getSkill(mountid) == null || GameConstants.getMountItem(mountid, c.getPlayer()) == 0) {
                c.getPlayer().dropMessage(1, "您無法使用這個騎寵的技能.");
            } else if (expiration_days > 0) {
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(mountid), (byte) 1, (byte) 1, System.currentTimeMillis() + expiration_days * 24 * 60 * 60 * 1000);
                c.getPlayer().dropMessage(1, "恭喜您獲得[" + SkillFactory.getSkill(mountid).getName() + "]騎寵技能 " + expiration_days + " 權。");
            } else if (expiration_days == -1) {
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(mountid), (byte) 1, (byte) 1, -1);
                c.getPlayer().dropMessage(1, "恭喜您獲得[" + SkillFactory.getSkill(mountid).getName() + "]騎寵技能永久權。");
            }
        }
        c.sendEnableActions();
    }

    /*
     * 使用怪物召喚包
     */
    public static void UseSummonBag(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (!chr.isAlive() || chr.hasBlockedInventory() || chr.inPVP()) {
            c.sendEnableActions();
            return;
        }
        c.getPlayer().updateTick(slea.readInt());
        short slot = slea.readShort();
        int itemId = slea.readInt();
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        if (toUse != null && toUse.getQuantity() >= 1 && toUse.getItemId() == itemId && (c.getPlayer().getMapId() < 910000000 || c.getPlayer().getMapId() > 910000022)) {
            Map<String, Integer> toSpawn = MapleItemInformationProvider.getInstance().getItemBaseInfo(itemId);
            if (toSpawn == null) {
                c.sendEnableActions();
                return;
            }
            MapleMonster ht = null;
            int type = 0;
            for (Entry<String, Integer> i : toSpawn.entrySet()) {
                if (i.getKey().startsWith("mob") && Randomizer.nextInt(99) <= i.getValue()) {
                    ht = MapleLifeFactory.getMonster(Integer.parseInt(i.getKey().substring(3)));
                    chr.getMap().spawnMonster(ht, chr.getPosition(), type);
                }
            }
            if (ht == null) {
                c.sendEnableActions();
                return;
            }
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
        }
        c.sendEnableActions();
    }

    /*
     * 玩家撿取道具
     */
    public static void Pickup_Player(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || chr.getMap() == null) {
            return;
        }
        if (c.getPlayer().hasBlockedInventory()) { //hack
            c.sendEnableActions();
            return;
        }
        byte type = slea.readByte();
        chr.updateTick(slea.readInt());
        slea.readInt();
        c.getPlayer().setScrolledPosition((short) 0);
        Point Client_Reportedpos = slea.readPos();
        MapleMapObject ob = chr.getMap().getMapObject(slea.readInt(), MapleMapObjectType.ITEM);
        if (ob == null) {
            c.sendEnableActions();
            return;
        }
        MapleMapItem mapitem = (MapleMapItem) ob;
        Lock lock = mapitem.getLock();
        lock.lock();
        try {
            if (mapitem.isPickedUp()) {
                c.sendEnableActions();
                return;
            }
            if (mapitem.getOnlySelfID() >= 0 && mapitem.getOnlySelfID() != chr.getId()) {
                c.sendEnableActions();
                return;
            }
            if (mapitem.getQuest() > 0 && chr.getQuestStatus(mapitem.getQuest()) != 1) {
                c.sendEnableActions();
                return;
            }
            if (mapitem.getOwnerID() != chr.getId() && ((!mapitem.isPlayerDrop() && mapitem.getOwnType() == 0) || (mapitem.isPlayerDrop() && chr.getMap().getEverlast()))) {
                c.sendEnableActions();
                return;
            }
            if (!mapitem.isPlayerDrop() && mapitem.getOwnType() == 1 && mapitem.getOwnerID() != chr.getId() && (chr.getParty() == null || chr.getParty().getPartyMemberByID(mapitem.getOwnerID()) == null)) {
                c.sendEnableActions();
                return;
            }
            double Distance = Client_Reportedpos.distance(mapitem.getPosition());
            if (mapitem.getDropMotionType() == 0 && Distance > 70 && (mapitem.getMeso() > 0 || mapitem.getItemId() != 4001025)) {
                chr.getCheatTracker().checkPickup(20, false);
                //chr.getCheatTracker().registerOffense(CheatingOffense.ITEMVAC_CLIENT, String.valueOf(Distance));
                //WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.spouseMessage(UserChatMessageType.淺紫, "[GM消息] " + chr.getName() + " ID: " + chr.getId() + " (等級 " + chr.getLevel() + ") 全屏撿物。地圖ID: " + chr.getMapId() + " 範圍: " + Distance));
            } else if (mapitem.getDropMotionType() == 0 && chr.getPosition().distance(mapitem.getPosition()) > 800) {
                chr.getCheatTracker().checkPickup(10, false);
                //chr.getCheatTracker().registerOffense(CheatingOffense.ITEMVAC_SERVER);
                //WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.spouseMessage(UserChatMessageType.淺紫, "[GM消息] " + chr.getName() + " ID: " + chr.getId() + " (等級 " + chr.getLevel() + ") 全屏撿物。地圖ID: " + chr.getMapId() + " 範圍: " + Distance));
            }
            if (mapitem.getMeso() > 0) {
                if (chr.getParty() != null && mapitem.getOwnerID() != chr.getId()) {
                    List<MapleCharacter> toGive = new LinkedList<>();
                    int splitMeso = mapitem.getMeso() * 40 / 100;
                    for (PartyMember z : chr.getParty().getMembers()) {
                        MapleCharacter m = chr.getMap().getPlayerObject(z.getCharID());
                        if (m != null && m.getId() != chr.getId()) {
                            toGive.add(m);
                        }
                    }
                    for (MapleCharacter m : toGive) {
                        if (m.getMeso() >= ServerConfig.CHANNEL_PLAYER_MAXMESO) {
                            m.getClient().sendEnableActions();
                            return;
                        }
                        long totalGainMeso = splitMeso / toGive.size();
                        if (m.getStat().hasPartyBonus) {
                            totalGainMeso += (int) (mapitem.getMeso() / 20.0);
                        }
                        if (mapitem.getDropper() instanceof MapleMonster && m.getStat().incMesoProp > 0) {
                            totalGainMeso += Math.floor(((m.getStat().incMesoProp / 100.0) * totalGainMeso));
                        }
                        m.gainMeso(totalGainMeso, true);
                    }
                    if (chr.getMeso() >= ServerConfig.CHANNEL_PLAYER_MAXMESO) {
                        c.sendEnableActions();
                        return;
                    }
                    chr.gainMeso(mapitem.getMeso() - splitMeso, true);
                } else {
                    if (chr.getMeso() >= ServerConfig.CHANNEL_PLAYER_MAXMESO) {
                        c.sendEnableActions();
                        return;
                    }
                    long totalGainMeso = mapitem.getMeso();
                    if (mapitem.getDropper() instanceof MapleMonster && chr.getStat().incMesoProp > 0) {
                        totalGainMeso += Math.floor(((chr.getStat().incMesoProp / 100.0) * totalGainMeso));
                    }
                    chr.gainMeso(totalGainMeso, true);
                }
                removeItem(chr, mapitem, ob);
            } else if (MapleItemInformationProvider.getInstance().isPickupBlocked(mapitem.getItemId())) {
                c.sendEnableActions();
                chr.dropMessage(5, "這個道具無法撿取.");
            } else if (chr.inPVP() && Integer.parseInt(chr.getEventInstance().getVariable("ice").toString()) == chr.getId()) {
                c.announce(InventoryPacket.getInventoryFull());
                c.announce(InventoryPacket.getShowInventoryFull());
                c.sendEnableActions();
            } else if (useItem(c, mapitem)) {
                c.sendEnableActions();
                removeItem(chr, mapitem, ob);
            } else if (MapleItemInformationProvider.getInstance().isOnly(mapitem.getItemId()) && chr.haveItem(mapitem.getItemId())) {
                c.announce(InventoryPacket.getInventoryFull());
                c.announce(InventoryPacket.showItemUnavailable());
            } else if (mapitem.getQuest() > 0 && (chr.getQuestStatus(mapitem.getQuest()) != 1 || !chr.needQuestItem(mapitem.getQuest(), mapitem.getItemId()))) {
                c.announce(InventoryPacket.getInventoryFull());
                c.announce(InventoryPacket.showItemUnavailable());
            } else if (mapitem.getItemId() / 10000 != 291 && MapleInventoryManipulator.checkSpace(c, mapitem.getItemId(), mapitem.getItem().getQuantity(), mapitem.getItem().getOwner())) {
                if (mapitem.getItem().getQuantity() >= 50 && mapitem.getItemId() == 2340000) {
                    c.setMonitored(true); //hack check
                }
                MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true, mapitem.getDropper() instanceof MapleMonster);
                removeItem(chr, mapitem, ob);
            } else {
                c.announce(InventoryPacket.getInventoryFull());
                c.announce(InventoryPacket.getShowInventoryFull());
                c.sendEnableActions();
            }
        } finally {
            lock.unlock();
        }
    }

    /*
     * 寵物撿取道具
     */
    public static void Pickup_Pet(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null) {
            return;
        }
        if (c.getPlayer().hasBlockedInventory() || c.getPlayer().inPVP()) { //hack
            return;
        }
        byte petz = (byte)slea.readInt();
        MaplePet pet = chr.getSpawnPet(petz);
        slea.skip(1);
        slea.readInt();
        slea.readInt(); // v264
        Point Client_Reportedpos = slea.readPos();
        MapleMapObject ob = chr.getMap().getMapObject(slea.readInt(), MapleMapObjectType.ITEM);;
        if (ob == null || pet == null) {
            return;
        }
        MapleMapItem mapitem = (MapleMapItem) ob;
        Lock lock = mapitem.getLock();
        lock.lock();
        try {
            if (mapitem.isPickedUp()) {
                c.announce(InventoryPacket.getInventoryStatus());
                return;
            }
            if (mapitem.getOnlySelfID() >= 0 && mapitem.getOnlySelfID() != chr.getId()) {
                return;
            }
            if (mapitem.getOwnerID() != chr.getId() && mapitem.isPlayerDrop()) {
                return;
            }
            if (mapitem.getOwnerID() != chr.getId() && ((!mapitem.isPlayerDrop() && mapitem.getOwnType() == 0) || (mapitem.isPlayerDrop() && chr.getMap().getEverlast()))) {
                return;
            }
            if (!mapitem.isPlayerDrop() && mapitem.getOwnType() == 1 && mapitem.getOwnerID() != chr.getId() && (chr.getParty() == null || chr.getParty().getPartyMemberByID(mapitem.getOwnerID()) == null)) {
                return;
            }
            double Distance = Client_Reportedpos.distance(mapitem.getPosition());
            if (Distance > 100 && (mapitem.getMeso() > 0 || mapitem.getItemId() != 4001025)) {
                chr.getCheatTracker().checkPickup(12, true);
                //chr.getCheatTracker().registerOffense(CheatingOffense.PET_ITEMVAC_CLIENT, String.valueOf(Distance));
//                WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.spouseMessage(UserChatMessageType.淺紫, "[GM消息] " + chr.getName() + " ID: " + chr.getId() + " (等級 " + chr.getLevel() + ") 全屏寵吸。地圖ID: " + chr.getMapId() + " 範圍: " + Distance));
            } else if (pet.getPos().distance(mapitem.getPosition()) > 800) {
                chr.getCheatTracker().checkPickup(6, true);
                //chr.getCheatTracker().registerOffense(CheatingOffense.PET_ITEMVAC_SERVER);
//                WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.spouseMessage(UserChatMessageType.淺紫, "[GM消息] " + chr.getName() + " ID: " + chr.getId() + " (等級 " + chr.getLevel() + ") 全屏寵吸。地圖ID: " + chr.getMapId() + " 範圍: " + Distance));
            }
            if (mapitem.getMeso() > 0) {
                if (chr.getParty() != null && mapitem.getOwnerID() != chr.getId()) {
                    List<MapleCharacter> toGive = new LinkedList<>();
                    int splitMeso = mapitem.getMeso() * 40 / 100;
                    for (PartyMember z : chr.getParty().getMembers()) {
                        MapleCharacter m = chr.getMap().getPlayerObject(z.getCharID());
                        if (m != null && m.getId() != chr.getId()) {
                            toGive.add(m);
                        }
                    }
                    for (MapleCharacter m : toGive) {
                        m.gainMeso(splitMeso / toGive.size() + (m.getStat().hasPartyBonus ? (int) (mapitem.getMeso() / 20.0) : 0), true, true, false);
                    }
                    chr.gainMeso(mapitem.getMeso() - splitMeso, true, false, false);
                } else {
                    chr.gainMeso(mapitem.getMeso(), true, false, false);
                }
                removeItem_Pet(chr, mapitem, petz);
            } else if (MapleItemInformationProvider.getInstance().isPickupBlocked(mapitem.getItemId()) || mapitem.getItemId() / 10000 == 291) {
                c.sendEnableActions();
            } else if (useItem(c, mapitem)) {
                removeItem_Pet(chr, mapitem, petz);
            } else if (MapleItemInformationProvider.getInstance().isOnly(mapitem.getItemId()) && chr.haveItem(mapitem.getItemId())) {
                c.announce(InventoryPacket.showItemUnavailable());
            } else if (mapitem.getQuest() > 0 && (chr.getQuestStatus(mapitem.getQuest()) != 1 || !chr.needQuestItem(mapitem.getQuest(), mapitem.getItemId()))) {
                c.announce(InventoryPacket.showItemUnavailable());
            } else if (MapleInventoryManipulator.checkSpace(c, mapitem.getItemId(), mapitem.getItem().getQuantity(), mapitem.getItem().getOwner())) {
                if (mapitem.getItem().getQuantity() >= 50 && mapitem.getItemId() == 2340000) {
                    c.setMonitored(true); //hack check
                }
                MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true, mapitem.getDropper() instanceof MapleMonster, false);
                removeItem_Pet(chr, mapitem, petz);
            }
        } finally {
            lock.unlock();
        }
    }

    /*
     * 使用物品道具
     */
    public static boolean useItem(MapleClient c, MapleMapItem mapItem) {
        int id = mapItem.getItemId();
        if (mapItem.getPointType() > 0) {
            int toCharge;
            switch (id) {
                case 2435892:
                    toCharge = 1;
                    break;
                case 2432107:
                    toCharge = 2;
                    break;
                case 2431872:
                    toCharge = 3;
                    break;
                default:
                    return true;
            }
            if (c.getPlayer() != null && c.getPlayer().getMap() != null && mapItem.getItem() != null) {
                c.getPlayer().getMap().pickupPoint(toCharge, mapItem.getItem().getQuantity(), c.getPlayer());
            }
            return true;
        } else if (ItemConstants.類型.消耗(id)) { // TO prevent caching of everything, waste of mem
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            MapleStatEffect eff = ii.getItemEffect(id);
            if (eff == null) {
                return false;
            }
            //must hack here for ctf
            if (id / 10000 == 291) {
                boolean area = false;
                for (Rectangle rect : c.getPlayer().getMap().getAreas()) {
                    if (rect.contains(c.getPlayer().getPosition())) {
                        area = true;
                        break;
                    }
                }
                if (!c.getPlayer().inPVP() || (c.getPlayer().getTeam() == (id - 2910000) && area)) {
                    return false; //dont apply the consume
                }
            }
            int consumeval = eff.getConsume();
            if (consumeval > 0) {
                // 連續擊殺
                if (id == 2023650 || id == 2023651 || id == 2023652 || id == 2023484 || id == 2023494 || id == 2023495 || id == 2023669) {
                    long exp = 0;
                    if (c.getPlayer() != null && c.getPlayer().getMap() != null) {
                        int level = c.getPlayer().getMap().getFieldLevel();
                        if (c.getPlayer().getLevel() < level - 20) {
                            c.getPlayer().dropSpecialTopMsg("從超過合適等級的怪物身上無法獲得Combo kill經驗值。", 3, 20, 20, 0);
                        } else if (level <= 0 || level > GameConstants.lvMobExp.length || GameConstants.lvMobExp[level - 1] == 0) {
                            c.getPlayer().dropSpecialTopMsg("因未知錯誤，無法從連續擊殺之珠中獲得經驗值。", 3, 20, 20, 0);
                        } else {
                            int rate = 0;
                            switch (id) {
                                case 2023650:
                                case 2023484:
                                    rate = 500;
                                    break;
                                case 2023651:
                                case 2023494:
                                    rate = 700;
                                    break;
                                case 2023652:
                                case 2023495:
                                    rate = 1000;
                                    break;
                                case 2023669:
                                    rate = 1100;
                                    break;
                            }

                            MapleStatEffect effect = c.getPlayer().getSkillEffect(狂狼勇士.連續擊殺優勢);
                            if (effect == null || !JobConstants.is狂狼勇士(c.getPlayer().getJob())) {
                                effect = c.getPlayer().getSkillEffect(狂狼勇士.連續擊殺優勢_傳授);
                            }
                            if (effect != null) {
                                rate = (int) Math.floor(rate * effect.getX() / 100.0D);
                            }

                            exp = (int) Math.floor(GameConstants.lvMobExp[level - 1] * rate / 100.0D);

                            exp = (int) Math.floor(exp * (100 - c.getPlayer().getMap().getRuneCurseRate()) / 100.0f);

                            if (exp > 0) {
                                c.getPlayer().gainFieldExp(exp, true);
                            }
                        }
                    }
                    if (exp <= 0) {
                        c.sendEnableActions();
                    }
                }
                consumeItem(c, eff);
                consumeItem(c, ii.getItemEffectEX(id));
                c.announce(MaplePacketCreator.getShowItemGain(id, 1));
                return true;
            } else if (GameConstants.isDoJangConsume(id)) {
                ii.getItemEffect(id).applyTo(c.getPlayer());
                c.announce(MaplePacketCreator.getShowItemGain(id, 1));
                return true;
            } else if (id == 2431174) {
                int num = 0;
                for (int i = 0; i < mapItem.getItem().getQuantity(); i++) {
                    num += Randomizer.rand(5, 20);
                }
                c.getPlayer().gainHonor(num);
                return true;
            } else if (ii.isRunOnPickup(id)) {
                MapleInventoryManipulator.addFromDrop(c, mapItem.getItem(), true, mapItem.getDropper() instanceof MapleMonster);
                Item item = c.getPlayer().getInventory(ItemConstants.getInventoryType(mapItem.getItem().getItemId())).findById(mapItem.getItem().getItemId());
                if (item != null) {
                    c.getPlayer().getScriptManager().startItemScript(item, 0, null);
                }
                return true;
            }
        }
        return false;
    }

    /*
     * 消耗物品道具 也就是使用成功吧
     */
    public static void consumeItem(MapleClient c, MapleStatEffect eff) {
        if (eff == null) {
            return;
        }
        if (eff.getConsume() == 2) {
            if (c.getPlayer().getParty() != null && c.getPlayer().isAlive()) {
                for (PartyMember pc : c.getPlayer().getParty().getMembers()) {
                    MapleCharacter chr = c.getPlayer().getMap().getPlayerObject(pc.getCharID());
                    if (chr != null && chr.isAlive()) {
                        eff.applyTo(chr);
                    }
                }
            } else {
                eff.applyTo(c.getPlayer());
            }
        } else if (c.getPlayer().isAlive()) {
            eff.applyTo(c.getPlayer());
        }
    }

    /*
     * 寵物撿取道具後移除地圖道具信息
     */
    public static void removeItem_Pet(MapleCharacter chr, MapleMapItem mapitem, int pet) {
        if (chr.getEventInstance() != null) {
            chr.getEventInstance().getHooks().playerPickupItem(chr, mapitem.getItemId());
        }
        mapitem.setPickedUp(true);
        chr.getMap().broadcastMessage(InventoryPacket.removeItemFromMap(mapitem.getObjectId(), 5, chr.getId(), pet));
        chr.getMap().removeMapObject(mapitem);
    }

    /*
     * 玩家撿取道具後移除地圖道具信息
     */
    private static void removeItem(MapleCharacter chr, MapleMapItem mapitem, MapleMapObject ob) {
        if (chr.getEventInstance() != null) {
            chr.getEventInstance().getHooks().playerPickupItem(chr, mapitem.getItemId());
        }
        mapitem.setPickedUp(true);
        chr.getMap().broadcastMessage(InventoryPacket.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
        chr.getMap().removeMapObject(ob);
    }

    /*
     * 使用商店搜索器開始搜索道具
     */
    public static void OwlMinerva(MaplePacketReader slea, MapleClient c) {
        short slot = slea.readShort();
        int itemid = slea.readInt();
        Item toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
        if (toUse != null && toUse.getQuantity() > 0 && toUse.getItemId() == itemid && itemid == 2310000 && !c.getPlayer().hasBlockedInventory()) {
            int itemSearch = slea.readInt();
            List<HiredMerchant> hms = c.getChannelServer().searchMerchant(itemSearch);
            if (hms.size() > 0) {
                c.announce(MaplePacketCreator.getOwlSearched(itemSearch, hms));
                MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemid, 1, true, false);
            } else {
                c.getPlayer().dropMessage(1, "沒有找到這個道具.");
            }
            MapleCharacterUtil.addToItemSearch(itemSearch);
        }
        c.sendEnableActions();
    }

    /*
     * 打開商店搜索器
     */
    public static void Owl(MaplePacketReader slea, MapleClient c) {
        //5230000 - 商店搜索器 5230001 - 新手商店搜索器 2310000 -雅典娜的貓頭鷹
        if (c.getPlayer().getMapId() >= 910000000 && c.getPlayer().getMapId() <= 910000022) {
            c.announce(MaplePacketCreator.getOwlOpen());
        } else {
            c.getPlayer().dropMessage(5, "商店搜索器只能在自由市場使用.");
            c.sendEnableActions();
        }
    }

    /*
     * 使用商店搜索器後選擇道具點擊移動
     */
    public static void OwlWarp(MaplePacketReader slea, MapleClient c) {
        c.sendEnableActions();
        if (c.getPlayer().getMapId() >= 910000000 && c.getPlayer().getMapId() <= 910000022 && !c.getPlayer().hasBlockedInventory()) {
            int id = slea.readInt();
//            int type = slea.readByte(); //未知 應該是搜索類型
            slea.skip(1);
            int map = slea.readInt();
            if (map >= 910000001 && map <= 910000022) {
                MapleMap mapp = c.getChannelServer().getMapFactory().getMap(map);
                c.getPlayer().changeMap(mapp, mapp.getPortal(0));
                HiredMerchant merchant = null;
                List<MapleMapObject> objects;
                switch (OWL_ID) {
                    case 0:
                        objects = mapp.getAllHiredMerchantsThreadsafe();
                        for (MapleMapObject ob : objects) {
                            if (ob instanceof IMaplePlayerShop) {
                                IMaplePlayerShop ips = (IMaplePlayerShop) ob;
                                if (ips instanceof HiredMerchant) {
                                    HiredMerchant merch = (HiredMerchant) ips;
                                    if (merch.getOwnerId() == id) {
                                        merchant = merch;
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                    case 1:
                        objects = mapp.getAllHiredMerchantsThreadsafe();
                        for (MapleMapObject ob : objects) {
                            if (ob instanceof IMaplePlayerShop) {
                                IMaplePlayerShop ips = (IMaplePlayerShop) ob;
                                if (ips instanceof HiredMerchant) {
                                    HiredMerchant merch = (HiredMerchant) ips;
                                    if (merch.getStoreId() == id) {
                                        merchant = merch;
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        MapleMapObject ob = mapp.getMapObject(id, MapleMapObjectType.HIRED_MERCHANT);
                        if (ob instanceof IMaplePlayerShop) {
                            IMaplePlayerShop ips = (IMaplePlayerShop) ob;
                            if (ips instanceof HiredMerchant) {
                                merchant = (HiredMerchant) ips;
                            }
                        }
                        break;
                }
                if (merchant != null) {
                    if (merchant.isOwner(c.getPlayer())) {
                        merchant.setOpen(false);
                        merchant.removeAllVisitors(0x14, (byte) 1);
                        c.getPlayer().setPlayerShop(merchant);
                        c.announce(PlayerShopPacket.getHiredMerch(c.getPlayer(), merchant, false));
                    } else if (!merchant.isOpen() || !merchant.isAvailable()) {
                        c.getPlayer().dropMessage(1, "主人正在整理商店物品\r\n請稍後再度光臨！");
                    } else if (merchant.getFreeSlot() == -1) {
                        c.getPlayer().dropMessage(1, "店舖已達到最大人數\r\n請稍後再度光臨！");
                    } else if (merchant.isInBlackList(c.getPlayer().getName())) {
                        c.getPlayer().dropMessage(1, "你被禁止進入該店舖.");
                    } else {
                        c.getPlayer().setPlayerShop(merchant);
                        merchant.addVisitor(c.getPlayer());
                        c.announce(PlayerShopPacket.getHiredMerch(c.getPlayer(), merchant, false));
                    }
                } else {
                    c.getPlayer().dropMessage(1, "主人正在整理商店物品\r\n請稍後再度光臨！");
                }
            }
        }
    }

    /*
     * 還原裝備升級失敗減少的次數
     */
//    public static void PamSong(MaplePacketReader slea, MapleClient c) {
//        Item pam = c.getPlayer().getInventory(MapleInventoryType.CASH).findById(5640000);
//        if (slea.readByte() > 0 && c.getPlayer().getScrolledPosition() != 0 && pam != null && pam.getQuantity() > 0) {
//            MapleInventoryType inv = c.getPlayer().getScrolledPosition() < 0 ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP;
//            Item item = c.getPlayer().getInventory(inv).getItem(c.getPlayer().getScrolledPosition());
//            c.getPlayer().setScrolledPosition((short) 0);
//            if (item != null) {
//                Equip eq = (Equip) item;
//                eq.setRestUpgradeCount((byte) (eq.getRestUpgradeCount() + 1));
//                c.getPlayer().forceUpdateItem(eq);
//                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, pam.getPosition(), (short) 1, true, false);
//                c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.pamsSongEffect(c.getPlayer().getId()));
//            }
//        } else {
//            c.getPlayer().setScrolledPosition((short) 0);
//        }
//    }

    /*
     * 使用瞬移之石移動
     */
    public static void TeleRock(MaplePacketReader slea, MapleClient c) {
        short slot = slea.readShort();
        int itemId = slea.readInt();
        Item toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
        if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId || itemId / 10000 != 232 || c.getPlayer().hasBlockedInventory()) {
            c.sendEnableActions();
            return;
        }
        boolean used = UseTeleRock(slea, c, itemId);
        if (used) {
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
        }
        c.sendEnableActions();
    }

    public static boolean UseTeleRock(MaplePacketReader slea, MapleClient c, int itemId) {
        boolean used = false;
        if (slea.readByte() == 0) { // Rocktype
            MapleMap target = c.getChannelServer().getMapFactory().getMap(slea.readInt());
            if ((itemId == 5041000 && c.getPlayer().isRockMap(target.getId())) || (itemId != 5041000 && c.getPlayer().isRegRockMap(target.getId())) || ((itemId == 5040004 || itemId == 5041001) && (c.getPlayer().isHyperRockMap(target.getId()) || GameConstants.isHyperTeleMap(target.getId())))) {
                if (!FieldLimitType.TELEPORTITEMLIMIT.check(c.getPlayer().getMap().getFieldLimit()) && !FieldLimitType.TELEPORTITEMLIMIT.check(target.getFieldLimit()) && !c.getPlayer().isInBlockedMap()) { //Makes sure this map doesn't have a forced return map
                    c.getPlayer().changeMap(target, target.getPortal(0));
                    used = true;
                }
            }
        } else {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString());
            if (victim != null && !victim.isIntern() && !c.getPlayer().checkEvent() && !victim.checkEvent()) {
                if (!FieldLimitType.TELEPORTITEMLIMIT.check(c.getPlayer().getMap().getFieldLimit()) && !FieldLimitType.TELEPORTITEMLIMIT.check(c.getChannelServer().getMapFactory().getMap(victim.getMapId()).getFieldLimit()) && !victim.isInBlockedMap() && !c.getPlayer().isInBlockedMap()) {
                    if (itemId == 5041000 || itemId == 5040004 || itemId == 5041001 || (victim.getMapId() / 100000000) == (c.getPlayer().getMapId() / 100000000)) { // Viprock or same continent
                        c.getPlayer().changeMap(victim.getMap(), victim.getMap().findClosestPortal(victim.getPosition()));
                        used = true;
                    }
                }
            } else {
                c.getPlayer().dropMessage(1, "在此頻道未找到該玩家.");
            }
        }
        return used;
    }


    /*
     * 使用潛能附加印章
     */
    public static void UseAdditionalAddItem(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || !chr.isAlive() || chr.getMap() == null || chr.hasBlockedInventory() || chr.inPVP()) {
            c.sendEnableActions();
            return;
        }
        chr.updateTick(slea.readInt());
        short slot = slea.readShort();
        short toSlot = slea.readShort();
        Item scroll = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        Equip toScroll = (Equip) chr.getInventory(MapleInventoryType.EQUIP).getItem(toSlot);
        if (scroll == null || scroll.getQuantity() < 0 || !ItemConstants.類型.附加潛能印章(scroll.getItemId()) || toScroll == null || toScroll.getQuantity() != 1) {
            c.sendEnableActions();
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        int successRate = ii.getScrollSuccess(scroll.getItemId()); //卷軸成功的幾率
        if (successRate <= 0) {
            c.getPlayer().dropMessage(1, "卷軸道具: " + scroll.getItemId() + " - " + ii.getName(scroll.getItemId()) + " 成功幾率為: " + successRate + " 該卷軸可能還未修復.");
            c.sendEnableActions();
            return;
        }
        if (chr.isAdmin()) {
            chr.dropSpouseMessage(UserChatMessageType.系統, "卷軸道具: " + scroll.getItemId() + " - " + ii.getName(scroll.getItemId()) + " 成功幾率為: " + successRate + "%");
        }
        if (toScroll.getPotential(1, true) == 0 || toScroll.getPotential(2, true) == 0 || toScroll.getPotential(3, true) == 0) {
            boolean success = false;
            int lines = toScroll.getPotential(1, true) == 0 ? 4 : toScroll.getPotential(2, true) == 0 ? 5 : 6;
            int reqLevel = ii.getReqLevel(toScroll.getItemId()) / 10;
            final int rank = (reqLevel >= 20) ? 19 : reqLevel;
            if (Randomizer.nextInt(100) <= successRate) {
                List<List<StructItemOption>> pots = new LinkedList<>(ii.getAllPotentialInfo().values());
                boolean rewarded = false;
                while (!rewarded) {
                    final StructItemOption option = pots.get(Randomizer.nextInt(pots.size())).get(rank);
                    if (option != null && !GameConstants.isAboveA(option.opID) && option.reqLevel / 10 <= rank && GameConstants.optionTypeFits(option.optionType, toScroll.getItemId()) && GameConstants.potentialIDFits(option.opID, 17, 1) && GameConstants.isBlockedPotential(toScroll, option.opID, true, false)) {
                        toScroll.setPotential(option.opID, lines - 3, true);
                        if (chr.isDebug()) {
                            chr.dropMessage(5, "附加潛能" + lines + " 獲得ID： " + option.opID);
                        }
                        rewarded = true;
                    }
                }
                success = true;
            }
            /*
             * 如果沒有成功
             * 2048200 - 低級潛能附加印章 - 成功幾率 5 % 失敗 100 %消失道具
             * 2048201 - 中級潛能附加印章 - 成功幾率 5 % 失敗 100 %消失道具
             */
            toScroll.initAllState();
            List<ModifyInventory> mods = new ArrayList<>();
            mods.add(new ModifyInventory(3, toScroll));
            mods.add(new ModifyInventory(0, toScroll));
            c.announce(InventoryPacket.modifyInventory(true, mods, chr));
            chr.getMap().broadcastMessage(InventoryPacket.showPotentialEx(chr.getId(), success, scroll.getItemId()));
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, scroll.getPosition(), (short) 1, false);
        } else {
            c.sendEnableActions();
        }
    }

    public static void UseAdditionalItem(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || !chr.isAlive() || chr.getMap() == null || chr.hasBlockedInventory() || chr.inPVP()) {
            c.sendEnableActions();
            return;
        }
        chr.updateTick(slea.readInt());
        final short slot = slea.readShort();
        final short toSlot = slea.readShort();
        final Item scroll = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        final Equip toScroll = (Equip) chr.getInventory((toSlot < 0) ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP).getItem(toSlot);
        if (scroll == null || scroll.getQuantity() < 0 || !ItemConstants.類型.潛能印章(scroll.getItemId()) || toScroll == null || toScroll.getQuantity() != 1) {
            c.sendEnableActions();
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final int successRate = ii.getScrollSuccess(scroll.getItemId());
        if (successRate <= 0) {
            c.getPlayer().dropMessage(1, "卷軸道具: " + scroll.getItemId() + " - " + ii.getName(scroll.getItemId()) + " 成功幾率為: " + successRate + " 該卷軸可能還未修復.");
            c.sendEnableActions();
            return;
        }
        if (chr.isAdmin()) {
            chr.dropSpouseMessage(UserChatMessageType.系統, "卷軸道具: " + scroll.getItemId() + " - " + ii.getName(scroll.getItemId()) + " 成功幾率為: " + successRate + "%");
        }
        if (toScroll.getPotential(1, false) == 0 || toScroll.getPotential(2, false) == 0 || toScroll.getPotential(3, false) == 0) {
            boolean success = false;
            final int lines = (toScroll.getPotential(2, false) == 0) ? 2 : ((toScroll.getPotential(1, false) == 0) ? 1 : 3);
            final int reqLevel = ii.getReqLevel(toScroll.getItemId()) / 10;
            final int rank = (reqLevel >= 20) ? 19 : reqLevel;
            if (Randomizer.nextInt(100) <= successRate) {
                List<List<StructItemOption>> pots = new LinkedList<>(ii.getAllPotentialInfo().values());
                boolean rewarded = false;
                while (!rewarded) {
                    final StructItemOption option = pots.get(Randomizer.nextInt(pots.size())).get(rank);
                    if (option != null && !GameConstants.isAboveA(option.opID) && option.reqLevel / 10 <= rank && GameConstants.optionTypeFits(option.optionType, toScroll.getItemId()) && GameConstants.potentialIDFits(option.opID, 17, 1) && GameConstants.isBlockedPotential(toScroll, option.opID, false, false)) {
                        toScroll.setPotential(option.opID, lines, false);
                        if (chr.isAdmin()) {
                            chr.dropMessage(5, "印章潛能" + lines + " 獲得ID： " + option.opID);
                        }
                        rewarded = true;
                    }
                }
                if (ItemConstants.isZeroWeapon(toScroll.getItemId())) {
                    chr.forceUpdateItem(((Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) ((toSlot == -10) ? -11 : -10))).copyPotential(toScroll), true);
                }
                success = true;
            }
            toScroll.initAllState();
            final List<ModifyInventory> mods = new ArrayList<>();
            mods.add(new ModifyInventory(3, toScroll));
            mods.add(new ModifyInventory(0, toScroll));
            c.announce(InventoryPacket.modifyInventory(true, mods, chr));
            chr.getMap().broadcastMessage(InventoryPacket.showPotentialEx(chr.getId(), success, scroll.getItemId()));
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, scroll.getPosition(), (short) 1, false);
        } else {
            c.sendEnableActions();
        }
    }

    /*
     * 購買十字獵人商店道具
     */
    public static void BuyCrossHunterItem(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || !chr.isAlive() || chr.getMap() == null || chr.hasBlockedInventory() || chr.inPVP()) {
            c.sendEnableActions();
            return;
        }
        int key = slea.readShort();
        int itemId = slea.readInt();
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        StructCrossHunterShop shop = ii.getCrossHunterShop(key);
        if (shop != null && itemId == shop.getItemId() && shop.getTokenPrice() > 0) {
            if (chr.getInventory(MapleInventoryType.ETC).countById(4310029) >= shop.getTokenPrice()) {
                if (MapleInventoryManipulator.checkSpace(c, shop.getItemId(), 1, "")) {
                    MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4310029, shop.getTokenPrice(), true, false);
                    MapleInventoryManipulator.addById(c, shop.getItemId(), 1, shop.getPotentialGrade(), "十字商店購買: " + DateUtil.getCurrentDate());
                    c.announce(MaplePacketCreator.confirmCrossHunter((byte) 0x00)); //物品購買完成。
                } else {
                    c.announce(MaplePacketCreator.confirmCrossHunter((byte) 0x02)); //背包空間不足。
                }
            } else {
                c.announce(MaplePacketCreator.confirmCrossHunter((byte) 0x01)); //道具不夠.
            }
        } else {
            c.announce(MaplePacketCreator.confirmCrossHunter((byte) 0x04)); //現在無法購買物品。
        }
    }

    public static void UserItemSkillOptionUpgradeItemUseRequest(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        c.getPlayer().updateTick(slea.readInt());
        short slot = slea.readShort();
        short toSlot = slea.readShort();
        boolean legendarySpirit = slea.available() == 1 && slea.readByte() == 1;
        Item scroll = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        Equip equip = (Equip) chr.getInventory((legendarySpirit ? MapleInventoryType.EQUIP : MapleInventoryType.EQUIPPED)).getItem(toSlot);

        if (equip == null || scroll == null) {
            c.sendEnableActions();
            return;
        }

        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        int soulid = scroll.getItemId();
        int skillid = ii.getSoulSkill(soulid);
        if (!ItemConstants.類型.靈魂寶珠(soulid) || !ItemConstants.類型.武器(equip.getItemId()) || skillid == 0 || equip.getSoulSocketID() == 0) {
            c.sendEnableActions();
            return;
        }

        ArrayList<Integer> tempOption = ii.getTempOption(soulid);
        int pot;
        if (tempOption.isEmpty()) {
            c.sendEnableActions();
            return;
        } else if (tempOption.size() == 1) {
            pot = tempOption.get(0);
        } else {
            pot = tempOption.get(Randomizer.nextInt(tempOption.size()));
        }

        int success = ii.getScrollSuccess(scroll.getItemId(), 100);

        chr.getInventory(ItemConstants.getInventoryType(scroll.getItemId())).removeItem(scroll.getPosition(), (short) 1, false);
        List<ModifyInventory> mods = new ArrayList<>();
        mods.add(new ModifyInventory(scroll.getQuantity() > 0 ? 1 : 3, scroll));
        boolean result = Randomizer.nextInt(100) < success;
        if (result) {
            equip.setSoulOptionID((short) (soulid % 1000 + 1));
            equip.setSoulSocketID((short) 1);
            equip.setSoulOption((short) pot);
            equip.setSoulSkill(skillid);
            mods.add(new ModifyInventory(3, equip));
            mods.add(new ModifyInventory(0, equip));
            if (!legendarySpirit) {
                int skid = chr.getSoulSkillID();
                if (skid > 0) {
                    chr.changeSkillLevel(new Skill(chr.getSoulSkillID()), (byte) 0, (byte) 0);
                }
                chr.changeSkillLevel(new Skill(skillid), (byte) 1, (byte) 1);
                chr.setSoulMP(chr.getSoulMP());
            }
        }
        if (legendarySpirit) {
            chr.getMap().broadcastMessage(chr, InventoryPacket.getScrollEffect(chr.getId(), result ? Equip.ScrollResult.成功 : Equip.ScrollResult.失敗, legendarySpirit, false, scroll.getItemId(), equip.getItemId()), true);
        } else {
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.showItemSkillOptionUpgradeEffect(chr.getId(), result, false, equip.getItemId(), equip.getSoulOption()), true);
        }
        c.announce(InventoryPacket.modifyInventory(true, mods, chr));
    }

    public static void UserItemSkillSocketUpgradeItemUseRequest(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || !chr.isAlive() || chr.getMap() == null || chr.hasBlockedInventory() || chr.inPVP()) {
            return;
        }
        chr.updateTick(slea.readInt());
        short slot = slea.readShort();
        short toSlot = slea.readShort();
        boolean legendarySpirit = slea.available() == 1 && slea.readByte() == 1;
        Item scroll = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        Equip nEquip = (Equip) chr.getInventory((legendarySpirit ? MapleInventoryType.EQUIP : MapleInventoryType.EQUIPPED)).getItem(toSlot);
        if (nEquip == null || scroll == null) {
            c.sendEnableActions();
            return;
        }
        if (!ItemConstants.類型.靈魂卷軸_附魔器(scroll.getItemId()) || nEquip.getSoulSocketID() != 0 || !ItemConstants.類型.武器(nEquip.getItemId())) {
            c.sendEnableActions();
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

        int reqLevel = ii.getReqLevel(nEquip.getItemId());
        Pair<Integer, Integer> socketReqLevel = ii.getSocketReqLevel(scroll.getItemId());
        if (reqLevel > socketReqLevel.getLeft() || reqLevel < socketReqLevel.getRight() || nEquip.getRestUpgradeCount() > 0) {
            chr.dropMessage(-1, "無法使用魂之珠的道具。");
            c.sendEnableActions();
            return;
        }

        int success = ii.getScrollSuccess(scroll.getItemId(), 100);
        boolean result = Randomizer.nextInt(100) <= success;
        List<ModifyInventory> mods = new ArrayList<>();
        if (result) {
            nEquip.setSoulOptionID((short) 0);
            nEquip.setSoulSocketID((short) 3);
            nEquip.setSoulOption((short) 0);
            nEquip.setSoulSkill(0);
            if (!legendarySpirit) {
                chr.setSoulMP(0);
            }
        }
        if (legendarySpirit) {
            chr.getMap().broadcastMessage(chr, InventoryPacket.getScrollEffect(chr.getId(), result ? Equip.ScrollResult.成功 : Equip.ScrollResult.失敗, legendarySpirit, false, scroll.getItemId(), nEquip.getItemId()), true);
        } else {
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.showItemSkillSocketUpgradeEffect(chr.getId(), result), true);
        }
        chr.getInventory(ItemConstants.getInventoryType(scroll.getItemId())).removeItem(scroll.getPosition(), (short) 1, false);
        mods.add(new ModifyInventory(scroll.getQuantity() > 0 ? 1 : 3, scroll));
        mods.add(new ModifyInventory(3, nEquip));
        mods.add(new ModifyInventory(0, nEquip));
        c.announce(InventoryPacket.modifyInventory(true, mods, chr));

        c.sendEnableActions();
    }

    public static void applyHyunCube(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        chr.updateTick(slea.readInt());
        final int size = slea.readInt();
        if ("5062024".equals(chr.getOneInfo(GameConstants.台方塊, "u")) && String.valueOf(size).equals(chr.getOneInfo(GameConstants.台方塊, "c")) && chr.getOneInfo(GameConstants.台方塊, "o") != null && chr.getOneInfo(GameConstants.台方塊, "p") != null) {
            final int intValue = Integer.valueOf(chr.getOneInfo(GameConstants.台方塊, "p"));
            final Equip equip = (Equip) chr.getInventory((intValue > 0) ? MapleInventoryType.EQUIP : MapleInventoryType.EQUIPPED).getItem((short) intValue);
            if (equip != null && String.valueOf(equip.getItemId()).equals(chr.getOneInfo(GameConstants.台方塊, "i"))) {
                int[] pots = new int[size];
                List<Integer> potList = new ArrayList<>();
                for (String p : chr.getOneInfo(GameConstants.台方塊, "o").split(",")) {
                    potList.add(Integer.parseInt(p));
                }
                for (int i = 0; i < pots.length; i++) {
                    pots[i] = slea.readInt();
                    if (!potList.contains(pots[i])) {
                        chr.send(InventoryPacket.showHyunPotentialResult(null));
                        c.sendEnableActions();
                        return;
                    } else {
                        potList.remove(potList.indexOf(pots[i]));
                    }
                }
                for (int i = 0; i < pots.length; i++) {
                    equip.setPotential(pots[i], i + 1, false);
                }
                chr.forceUpdateItem(equip, true);
                chr.send(InventoryPacket.showHyunPotentialResult(null));
                chr.removeInfoQuest(GameConstants.台方塊);
                chr.equipChanged();
            } else {
                chr.send(InventoryPacket.showHyunPotentialResult(null));
            }
        } else {
            chr.send(InventoryPacket.showHyunPotentialResult(null));
        }
        c.sendEnableActions();
    }

    public static void applyBlackCube(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || !chr.isAlive() || chr.getMap() == null || chr.hasBlockedInventory() || chr.inPVP()) {
            c.sendEnableActions();
            return;
        }
        chr.updateTick(slea.readInt());
        int mode = slea.readShort();

        if (mode == 7 && chr.getOneInfo(GameConstants.台方塊, "p") != null && "5062010".equals(chr.getOneInfo(GameConstants.台方塊, "u"))) {
            final int intValue = Integer.valueOf(chr.getOneInfo(GameConstants.台方塊, "p"));
            final Equip nEquip = (Equip) chr.getInventory((intValue > 0) ? MapleInventoryType.EQUIP : MapleInventoryType.EQUIPPED).getItem((short) intValue);
            if (chr.getOneInfo(GameConstants.台方塊, "o") != null && nEquip != null && String.valueOf(nEquip.getItemId()).equals(chr.getOneInfo(GameConstants.台方塊, "i"))) {
                int i = 1;
                for (String p : chr.getOneInfo(GameConstants.台方塊, "o").split(",")) {
                    nEquip.setPotential(Integer.parseInt(p), i++, false);
                }
                chr.equipChanged();
                chr.forceUpdateItem(nEquip);
                chr.removeInfoQuest(GameConstants.台方塊);
            }
        }

//        if (mode == 7 && chr.getOneInfo(GameConstants.神秘方塊, "dst") != null) {
//            final int intValue = Integer.valueOf(chr.getOneInfo(GameConstants.神秘方塊, "dst"));
//            final Equip nEquip = (Equip) chr.getInventory((intValue > 0) ? MapleInventoryType.EQUIP : MapleInventoryType.EQUIPPED).getItem((short) intValue);
//            for (int i = 0; i < 3; ++i) {
//                if (chr.getOneInfo(GameConstants.神秘方塊, "Pot" + i) != null) {
//                    nEquip.setPotential(Integer.valueOf(chr.getOneInfo(GameConstants.神秘方塊, "Pot" + i)), i + 1, false);
//                }
//            }
//            chr.equipChanged();
//            chr.forceUpdateItem(nEquip);
//            chr.removeInfoQuest(GameConstants.神秘方塊);
//        }
        c.announce(InventoryPacket.showBlackCubeResults());
    }

    public static void UseFamiliarCard(MapleCharacter chr, int minGrade, int maxGrade, boolean isCash) {
        UseFamiliarCard(chr, minGrade, maxGrade, isCash, false);
    }

    public static void UseFamiliarCard(MapleCharacter chr, int minGrade, int maxGrade, boolean isCash, boolean isNadi) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        int count = isCash ? 3 : 1;
        List<Integer> cards = new ArrayList<>();
        List<Boolean> mega = new ArrayList<>();
        if (isNadi) {
            List<Integer> natiCards = new ArrayList<Integer>() {
                {
                    add(2870849); // 圓滾那堤卡
                    add(2870850); // 丹丹那堤卡
                    add(2870851); // 咚咚那堤卡
                    add(2870852); // 黑咚那堤卡
                }
            };
            for (int i = 0; i < count; i++) {
                cards.add(natiCards.get(Randomizer.nextInt(natiCards.size())));
                mega.add(false);
            }
        } else {
            RaffleItem gitem = null;
            for (int i = 0; i < count; i++) {
                int j = 0;
                while (gitem == null) {
                    if (1000 <= j++) {
                        throw new Error("抽取萌獸卡包出錯");
                    }
                    gitem = RafflePool.randomItem(5537000);
                    if (gitem == null || (isCash && gitem.getPeriod() > 0)) {
                        gitem = null;
                        continue;
                    }
                    if (ii.getFamiliarID(gitem.getItemId()) == 0) {
                        if (chr.isAdmin()) {
                            chr.dropMessage(1, "抽取萌獸卡包出錯, 萌獸卡ID:" + gitem.getItemId() + " 沒有對應萌獸。");
                        }
                        gitem = null;
                        continue;
                    }
                    cards.add(gitem.getItemId());
                    mega.add(gitem.isSmega());
                    gitem = null;
                    break;
                }
            }
        }
        List<Pair<Integer, Integer>> familiarids = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            int random = Randomizer.nextInt(1000);
            int grade;
            if (random < 150 && maxGrade >= 4) {
                grade = 4;
            } else if (random < 300 && maxGrade >= 3) {
                grade = 3;
            } else if (random < 450 && maxGrade >= 2) {
                grade = 2;
            } else if (random < 600 && maxGrade >= 1) {
                grade = 1;
            } else {
                grade = 0;
            }
            if (isCash && familiarids.size() == 2) {
                boolean found = false;
                for (Pair<Integer, Integer> fid : familiarids) {
                    if (fid.getRight() >= 2) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    grade = 2;
                }
            }
            grade = Math.max(minGrade, grade);
            if (chr.isAdmin() && chr.isInvincible()) {
                grade = 4;
                chr.dropMessage(-6, "伺服器管理員無敵狀態抽取最高潛能滿等級萌獸卡");
            }
            int familiarid = ii.getFamiliarID(cards.get(i));
            familiarids.add(new Pair<>(familiarid, grade));
            Item item = new Item(cards.get(i), (byte) 0, (short) 1);
            item.setFamiliarCard(new FamiliarCard((byte) grade));
            if (chr.isAdmin() && chr.isInvincible()) {
                item.getFamiliarCard().setLevel((byte) 5);
            }
            item.setGMLog("使用萌獸卡牌包獲得");
            if (!isCash) {
                item.addAttribute(ItemAttribute.ColdProof.getValue());
                item.addAttribute(ItemAttribute.TradeBlock.getValue());
            }
            MapleInventoryManipulator.addbyItem(chr.getClient(), item);
            if (mega.get(i)) {
                WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.gachaponMsg("恭喜" + chr.getName() + "從萌獸卡牌包獲得{" + (item != null ? ii.getName(item.getItemId()) : "") + "}", item));
            }
        }
        chr.write(OverseasPacket.extraEquipResult(ExtraEquipResult.openCardPack(chr.getId(), familiarids)));
    }

    //
    public static void UseToadsHammer(MaplePacketReader slea, MapleCharacter chr) {
        short mode = slea.readShort();
        switch (mode) {
            case 0: {
                chr.send(InventoryPacket.UserToadsHammerResult(mode, applyToadsHammer(chr, slea.readShort(), slea.readShort(), slea.readShort(), false), (short) 0, null));
                break;
            }
            case 1: {
                final short slot1 = slea.readShort();
                final short slot2 = slea.readShort();
                final short short4 = slea.readShort();
                final Equip equip1 = (Equip) chr.getInventory((slot1 < 0) ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP).getItem(slot1);
                final Equip equip2 = (Equip) chr.getInventory((slot2 < 0) ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP).getItem(slot2);
                if (equip2.getReqLevel() - equip1.getReqLevel() > 0 && equip2.getReqLevel() - equip1.getReqLevel() <= 10) {
                    final Equip copy = (Equip) equip1.copy();
                    final Equip newEquip = applyToadsHammer(chr, slot1, slot2, short4, true);
                    final List<ModifyInventory> inventoryList = new ArrayList<>();
                    inventoryList.add(new ModifyInventory(3, equip1));
                    inventoryList.add(new ModifyInventory(3, newEquip));
                    inventoryList.add(new ModifyInventory(0, newEquip));
                    chr.getInventory((slot1 < 0) ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP).removeItem(slot1);
                    chr.send(InventoryPacket.modifyInventory(true, inventoryList, chr));
                    chr.send(InventoryPacket.UserToadsHammerResult(mode, copy, slot2, null));
                    if (slot2 < 0 || slot1 < 0) {
                        chr.equipChanged();
                    }
                    break;
                }
                break;
            }
            case 2: {
                final short slot = slea.readShort();
                final Equip equip = (Equip) chr.getInventory((slot < 0) ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP).getItem(slot);
                chr.send(InventoryPacket.UserToadsHammerResult(mode, equip, (short) 0, EnchantHandler.getScrollList(equip)));
                break;
            }
        }
    }

    public static Equip applyToadsHammer(final MapleCharacter chr, final short slot1, final short slot2, final int n3, final boolean b) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final Equip equip1 = (Equip) chr.getInventory((slot1 < 0) ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP).getItem(slot1);
        final Equip equip2 = (Equip) chr.getInventory((slot2 < 0) ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP).getItem(slot2);
        final Equip nEquip = (Equip) (b ? equip2 : equip2.copy());
        final List<EnchantScrollEntry> scrollList = EnchantHandler.getScrollList(equip2);
        if (nEquip.getRestUpgradeCount() > 0 && n3 > -1) {
            final EnchantScrollEntry scrollEntry = scrollList.get(n3);
            for (byte upgradeSlots = nEquip.getRestUpgradeCount(), b5 = 0; b5 < upgradeSlots; ++b5) {
                ItemScrollHandler.enchantScrollEquip(scrollEntry, nEquip, 1, chr.getClient(), true);
            }
        }
        nEquip.setStarForceLevel((byte) (nEquip.getStarForceLevel() - 1));
        //對s、ss級潛能降級處理
        if (equip1.getState(false) > 18) {
            for (int j = 1; j <= 3; ++j) {
                final int potential = equip1.getPotential(j, false);
                if (potential > 0) {
                    final int n4 = potential % 10000 + 20000;
                    if (ii.getPotentialInfo(n4) != null) {
                        nEquip.setPotential(n4, j, false);
                    } else {
                        final List<StructItemOption> optionList = nEquip.getFitOptionList(3);
                        if (optionList.size() >= 1) {
                            nEquip.setPotential(optionList.get(j - 1).opID, j, false);
                        }
                    }
                } else {
                    nEquip.setPotential(0, j, false);
                }
            }
            nEquip.setState((byte) 18, false);
        } else {
            nEquip.setPotential(equip1.getPotential(1, false), 1, false);
            nEquip.setPotential(equip1.getPotential(2, false), 2, false);
            nEquip.setPotential(equip1.getPotential(3, false), 3, false);
            nEquip.setState(equip1.getState(false), false);
        }

        nEquip.initAllState();
        return nEquip;
    }

    public static void arcaneForceRequest(MaplePacketReader slea, MapleCharacter chr) {
        if (chr == null || chr.getMap() == null) {
            return;
        }
        final int mode = slea.readInt();
        switch (mode) {
            case 0: { // 合併經驗，不會升級
                final int slot = slea.readInt();
                final Equip e = (Equip) chr.getInventory(MapleInventoryType.EQUIP).getItem((short) slot);
                if (e == null || !ItemConstants.類型.秘法符文(e.getItemId())) {
                    return;
                }
                int maxExpNeededForLevel = 0;
                Equip to = null;
                for (short i = -1605; i <= -1600; ++i) {
                    Equip equipped = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(i);
                    if (equipped != null && e.getItemId() == equipped.getItemId() && equipped.getARCLevel() < 20) {
                        for (short j = equipped.getARCLevel(); j < 20; j++) {
                            maxExpNeededForLevel += ItemConstants.getArcExpNeededForLevel(j, equipped.getItemId() % 10);
                        }
                        if (equipped.getArcExp() < maxExpNeededForLevel) {
                            to = equipped;
                            break;
                        }
                    }
                }
                if (to == null) {
                    return;
                }
                int type = to.getItemId() % 10;
                short eLevel = e.getARCLevel();
                int eExp = e.getArcExp();
                int exp = 0;
                for (short i = 1; i < eLevel; ++i) {
                    exp += ItemConstants.getArcExpNeededForLevel(i, type);
                }
                exp += eExp;
                exp += to.getArcExp();
                to.setARCExp(Math.min(exp, maxExpNeededForLevel));
                MapleInventoryManipulator.removeFromSlot(chr.getClient(), MapleInventoryType.EQUIP, (short) slot, (short) 1, false, false);
                chr.forceUpdateItem(to);
                return;
            }

            case 1: { // 升級，需要消耗楓幣
                final int slot = -slea.readInt();
                Equip e = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) slot);
                if (e == null || e.getItemId() / 10000 != 171) {
                    return;
                }
                short arcLevel = e.getARCLevel();
                int type = e.getItemId() % 10;
                int exp = ItemConstants.getArcExpNeededForLevel(arcLevel, type);
                long meso = ItemConstants.getArcMesoNeededForLevel(arcLevel, type);
                if (chr.getMeso() < meso) {
                    chr.dropMessage(1, "楓幣不足！需要" + meso);
                    return;
                }
                if (e.getArcExp() >= exp) {
                    e.setARCLevel((short) (arcLevel + 1));
                    e.setARCExp(e.getArcExp() - exp);
                    int job = chr.getJob();
                    e.recalcArcStat(job);
                    chr.gainMeso(-meso, false);
                    chr.forceUpdateItem(e);
                    int Type = e.getItemId();
                    if (Type == 1712001 || Type == 1712002 || Type == 1712003 || Type == 1712004 || Type == 1712005 || Type == 1712006) {
                        chr.send(VCorePacket.ArcAutLevelUpEffect(slot));
                    }
                    chr.dropMessage(1, e.getName() + " 已經強化成功！");
                }
                return;
            }
            case 2: {
                int itemId = slea.readInt();
                if (!ItemConstants.類型.秘法符文(itemId)) {
                    return;
                }
                slea.readInt();
                int count = slea.readInt();
                int maxExpNeededForLevel = 0;
                Equip to = null;
                for (short i = -1605; i <= -1600; ++i) {
                    Equip e = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(i);
                    if (e != null && itemId == e.getItemId() && e.getARCLevel() < 20) {
                        for (short j = e.getARCLevel(); j < 20; j++) {
                            maxExpNeededForLevel += ItemConstants.getArcExpNeededForLevel(j, e.getItemId() % 10);
                        }
                        if (e.getArcExp() < maxExpNeededForLevel) {
                            to = e;
                            break;
                        }
                    }
                }
                if (to != null) {
                    if (chr.getItemQuantity(itemId) > 0) {
                        int n = 0;
                        for (Item item : chr.getInventory(MapleInventoryType.EQUIP).listById(itemId)) {
                            final Equip e = (Equip) item;
                            if (e.getARCLevel() == 1 && e.getArcExp() == 1) {
                                if (n >= count) {
                                    break;
                                }
                                ++n;
                                int type = to.getItemId() % 10;
                                int exp = 1;
                                exp += to.getArcExp();
                                to.setARCExp(Math.min(exp, maxExpNeededForLevel));
                                MapleInventoryManipulator.removeFromSlot(chr.getClient(), MapleInventoryType.EQUIP, e.getPosition(), (short) 1, false, false);
                                chr.forceUpdateItem(to);
                                if (exp >= maxExpNeededForLevel) {
                                    break;
                                }
                            }
                        }
                    }
                }
                break;
            }
            default: {
                log.warn("Unhandled arc force mode:" + mode);
                break;
            }
        }
    }

    public static void authenticForceRequest(MaplePacketReader slea, MapleCharacter chr) {
        if (chr == null || chr.getMap() == null) {
            return;
        }
        final int mode = slea.readInt();
        switch (mode) {
            case 0: { // 合併經驗，不會升級
                final int slot = slea.readInt();
                slea.readInt();
                final Equip e = (Equip) chr.getInventory(MapleInventoryType.EQUIP).getItem((short) slot);
                if (e == null || !ItemConstants.類型.真實符文(e.getItemId())) {
                    return;
                }
                int maxExpNeededForLevel = 0;
                Equip to = null;
                for (short i = -1705; i <= -1700; ++i) {
                    Equip equipped = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(i);
                    if (equipped != null && e.getItemId() == equipped.getItemId() && equipped.getAutLevel() < 11) {
                        for (short j = equipped.getAutLevel(); j < 11; j++) {
                            maxExpNeededForLevel += ItemConstants.getAutExpNeededForLevel(j, equipped.getItemId() % 10);
                        }
                        if (equipped.getAutExp() < maxExpNeededForLevel) {
                            to = equipped;
                            break;
                        }
                    }
                }
                if (to == null) {
                    return;
                }
                int type = to.getItemId() % 10;
                short eLevel = e.getAutLevel();
                int eExp = e.getAutExp();
                int exp = 0;
                for (short i = 1; i < eLevel; ++i) {
                    exp += ItemConstants.getAutExpNeededForLevel(i, type);
                }
                exp += eExp;
                exp += to.getAutExp();
                to.setAutExp(Math.min(exp, maxExpNeededForLevel));
                MapleInventoryManipulator.removeFromSlot(chr.getClient(), MapleInventoryType.EQUIP, (short) slot, (short) 1, false, false);
                chr.forceUpdateItem(to);
                return;
            }
            case 1: { // 升級，需要消耗楓幣
                final int slot = -slea.readInt();
                Equip e = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) slot);
                if (e == null || e.getItemId() / 10000 != 171) {
                    return;
                }
                short autLevel = e.getAutLevel();
                int type = e.getItemId() % 10;
                int exp = ItemConstants.getAutExpNeededForLevel(autLevel, type);
                long meso = ItemConstants.getAutMesoNeededForLevel(autLevel, type);
                if (chr.getMeso() < meso) {
                    chr.dropMessage(1, "楓幣不足！需要" + meso);
                    return;
                }
                if (e.getAutExp() >= exp) {
                    e.setAutLevel((short) (autLevel + 1));
                    e.setAutExp(e.getAutExp() - exp);
                    int job = chr.getJob();
                    e.recalcAutStat(job);
                    chr.gainMeso(-meso, false);
                    chr.forceUpdateItem(e);
                    chr.dropMessage(1, "真實符文已經強化成功！");
                }
                return;
            }
            case 2: {
                int itemId = slea.readInt();
                if (!ItemConstants.類型.真實符文(itemId)) {
                    return;
                }
                slea.readInt();
                int count = slea.readInt();
                int maxExpNeededForLevel = 0;
                Equip to = null;
                for (short i = -1705; i <= -1700; ++i) {
                    Equip e = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(i);
                    if (e != null && ItemConstants.類型.真實符文(itemId) && itemId == e.getItemId() && e.getAutLevel() < 11) {
                        for (short j = e.getAutLevel(); j < 11; j++) {
                            maxExpNeededForLevel += ItemConstants.getAutExpNeededForLevel(j, e.getItemId() % 10);
                        }
                        if (e.getAutExp() < maxExpNeededForLevel) {
                            to = e;
                            break;
                        }
                    }
                }
                if (to != null) {
                    if (chr.getItemQuantity(itemId) > 0) {
                        int n = 0;
                        for (Item item : chr.getInventory(MapleInventoryType.EQUIP).listById(itemId)) {
                            final Equip e = (Equip) item;
                            if (e.getAutLevel() == 1 && e.getAutExp() == 1) {
                                if (n >= count) {
                                    break;
                                }
                                ++n;
                                int type = to.getItemId() % 10;
                                int exp = 1;
                                exp += to.getAutExp();
                                to.setAutExp(Math.min(exp, maxExpNeededForLevel));
                                MapleInventoryManipulator.removeFromSlot(chr.getClient(), MapleInventoryType.EQUIP, e.getPosition(), (short) 1, false, false);
                                chr.forceUpdateItem(to);
                                if (exp >= maxExpNeededForLevel) {
                                    break;
                                }
                            }
                        }
                    }
                }
                break;
            }
            default: {
                log.warn("Unhandled aut force mode:" + mode);
                break;
            }
        }
    }

    public static void UserWeaponTempItemOptionRequest(MaplePacketReader slea, MapleClient c, MapleCharacter player) {
        if (player == null || player.getMap() == null) {
            return;
        }
        player.updateTick(slea.readInt());
        slea.readByte();
        player.setSoulMP(player.getSoulMP());
        player.checkSoulState(false);
        c.dispose();
    }
}
