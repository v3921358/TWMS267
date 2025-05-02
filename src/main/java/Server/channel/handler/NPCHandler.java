package Server.channel.handler;

import Client.*;
import Client.inventory.Equip;
import Client.inventory.Item;
import Client.inventory.ItemAttribute;
import Client.inventory.MapleInventoryType;
import Config.configs.ServerConfig;
import Config.constants.GameConstants;
import Config.constants.ItemConstants;
import Config.constants.enums.*;
import Database.DatabaseLoader.DatabaseConnection;
import Net.server.AutobanManager;
import Net.server.MapleInventoryManipulator;
import Net.server.MapleItemInformationProvider;
import Net.server.MapleTrunk;
import Net.server.life.MapleLifeFactory;
import Net.server.life.MapleNPC;
import Net.server.maps.MapleMap;
import Net.server.maps.MapleQuickMove;
import Net.server.movement.LifeMovementFragment;
import Net.server.quest.MapleQuest;
import Net.server.shop.MapleShop;
import Net.server.shop.MapleShopFactory;
import Opcode.Headler.OutHeader;
import Opcode.Opcode.EffectOpcode;
import Packet.EffectPacket;
import Packet.MaplePacketCreator;
import Packet.NPCPacket;
import Packet.PacketHelper;
import Plugin.provider.loaders.StringData;
import Plugin.script.ScriptManager;
import Server.world.WorldBroadcastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Pair;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static Config.constants.enums.TrunkOptType.*;

public class NPCHandler {

    /**
     * Logger for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(NPCHandler.class);

    /*
     * NPC自己說話和移動效果
     */
    public static void NPCAnimation(MaplePacketReader slea, MapleClient c) {
        if (c.getPlayer() == null || c.getPlayer().getMap() == null) {
            return;
        } else if (c.getPlayer().getLastChangeMapTime() > 0 && (System.currentTimeMillis() - c.getPlayer().getLastChangeMapTime()) < 1000) { // 換地圖後1秒內不會有此封包回傳，除非客戶端卡頓，進一步造成不同地圖相同的OID封包觸發並斷線
            return;
        }
        MapleMap map = c.getPlayer().getMap();
        int npcOid = slea.readInt();
        MapleNPC npc = map.getNPCByOid(npcOid);
        if (npc == null) {
            return;
        }
        byte type1 = slea.readByte();
        byte type2 = slea.readByte();
        int n1 = slea.readInt();

        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_NpcMove.getValue());
        mplew.writeInt(npcOid);
        mplew.write(type1);
        mplew.write(type2);
        mplew.writeInt(n1);

        if (npc.isMove()) {
            if (slea.available() >= 17) {
                final int gatherDuration = slea.readInt();
                final int nVal1 = slea.readInt();
                final Point mPos = slea.readPos();
                final Point oPos = slea.readPos();
                List<LifeMovementFragment> res = MovementParse.parseMovement(slea, 10);
                MovementParse.updatePosition(res, npc, 0);

                PacketHelper.serializeMovementList(mplew, gatherDuration, nVal1, mPos, oPos, res, null);
            }
        }
        mplew.write(0);
        map.objectMove(-1, npc, mplew.getPacket());
    }

    /*
     * NPC商店操作
     */
    public static void NPCShop(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || chr.getMap() == null) {
            return;
        }
        MapleShop shop;
        if (chr.getConversation() != ConversationType.ON_NPC_SHOP || (shop = chr.getShop()) == null) {
            c.sendEnableActions();
            return;
        }
        byte bmode = slea.readByte();
        switch (bmode) {
            case 0: { //購買道具
                short position = slea.readShort(); //道具在商店的位置
                int itemId = slea.readInt();
                short quantity = slea.readShort();
                slea.skip(10);
                shop.buy(c, itemId, quantity, position);
                break;
            }
            case 1: { //出售道具
                short slot = slea.readShort();
                int itemId = slea.readInt();
                short quantity = slea.readShort();
                shop.sell(c, ItemConstants.getInventoryType(itemId), slot, quantity);
                break;
            }
            case 2: { //沖飛鏢和子彈數量
                short slot = slea.readShort();
                shop.recharge(c, slot);
                break;
            }
            case 3: { //關閉商店
                chr.setConversation(ConversationType.NONE);
                chr.setShop(null);
                break;
            }
            default:
                c.sendEnableActions();
                break;
        }
    }

    /*
     * NPC對話操作
     */
    public static void NPCTalk(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || chr.getMap() == null || !c.canClickNPC()) {
            c.sendEnableActions();
            return;
        }
        int npcID = slea.readInt();
        MapleNPC npc = c.getPlayer().getMap().getNPCByOid(npcID);
        if (npc == null) {
            return;
        }
        if (chr.hasBlockedInventory()) {
            chr.dropMessage(5, "已經與NPC對話當中,可以使用@ea解除卡對話狀態。");
            c.sendEnableActions();
            return;
        }
        if (MapleAntiMacro.isAntiNow(chr.getName())) {
            chr.dropMessage(5, "被使用測謊機時無法操作。");
            c.sendEnableActions();
            return;
        }
        chr.setCurrenttime(System.currentTimeMillis());
        if (chr.getCurrenttime() - chr.getLasttime() < chr.getDeadtime()) {
            if (chr.isGm()) {
                chr.dropMessage(5, "對話或按鈕過快。");
            }

            c.sendEnableActions();
            return;
        }
        chr.setLasttime(System.currentTimeMillis());
        c.getPlayer().updateTick(slea.readInt()); //暫時不檢測點NPC的速度
        MapleShop shop = MapleShopFactory.getInstance().getShopForNPC(npc.getId());
        if (shop != null) {
            shop.sendShop(c);
        } else if (MapleLifeFactory.isNpcTrunk(npc.getId())) {
            String npcScriptInfo = StringData.getNpcStringById(npc.getId());
            if ("MISSINGNO".equalsIgnoreCase(npcScriptInfo)) {
                npcScriptInfo = "";
            }
            if (npcScriptInfo.isEmpty()) {
                npcScriptInfo = String.valueOf(npc.getId());
            } else {
                npcScriptInfo += "(" + npc.getId() + ")";
            }
            if (chr.isAdmin()) {
                chr.dropSpouseMessage(UserChatMessageType.青, "[" + npcScriptInfo + "] 使用倉庫。");
            }
            chr.setConversation(ConversationType.ON_TRUNK);
            chr.getTrunk().secondPwdRequest(c, npc.getId());
        } else if (chr.getScriptManager().startNpcScript(npc.getId(), npcID, null) != null){
            chr.getScriptManager().startScript(npcID, MapleLifeFactory.getNpcScriptName(npcID), null);
        } else if(MapleLifeFactory.getNpcScriptName(npcID)!= null){
            chr.getScriptManager().startScript(npcID, "npcs/"+MapleLifeFactory.getNpcScriptName(npcID), null);
        }
    }

    /*
     * 任務操作
     */
    public static void QuestAction(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || chr.getMap() == null || chr.hasBlockedInventory()) {
            return;
        }
        byte action = slea.readByte();
        int questId = slea.readInt();
        if (MapleAntiMacro.isAntiNow(chr.getName())) {
            chr.dropMessage(5, "被使用測謊機時無法操作。");
            c.sendEnableActions();
            return;
        }
        QuestRequestType op = QuestRequestType.getQTFromByte(action);
        if (op == null) {
            if (chr.isDebug()) {
                chr.dropMessage(5, "Unknown QuestRequestType: " + action);
            }
            return;
        }
        MapleQuest quest = MapleQuest.getInstance(questId);
        switch (op) {
            case QuestReq_LostItem: {
                slea.readInt();
                quest.restoreLostItem(chr, slea.readInt());
                break;
            }
            case QuestReq_AcceptQuest: {
                int npc = slea.readInt();
                if (quest.getStartScript().isEmpty()) {
                    quest.start(chr, npc);
                }
                break;
            }
            case QuestReq_CompleteQuest: {
                int npc = slea.readInt();
                int selection = slea.readInt();
                if (!quest.getEndScript().isEmpty()) {
                    return;
                }
                if (slea.available() >= 4) {
                    quest.complete(chr, npc, slea.readInt());
                } else if (selection >= 0) {
                    quest.complete(chr, npc, selection);
                } else {
                    quest.complete(chr, npc);
                }
                break;
            }
            case QuestReq_ResignQuest: {
                if (GameConstants.canForfeit(quest.getId())) {
                    quest.forfeit(chr);
                    if (chr.isDebug()) {
                        chr.dropMessage(6, "[任務系統] 放棄系統任務 " + quest);
                    }
                } else {
                    chr.dropMessage(1, "無法放棄這個任務.");
                }
                break;
            }
            case QuestReq_OpeningScript: {
                chr.getScriptManager().startQuestSScript(slea.readInt(), questId);
//                NPCScriptManager.getInstance().startQuest(c, slea.readInt(), questId);
                break;
            }
            case QuestReq_CompleteScript: { // 腳本任務結束
                chr.getScriptManager().startQuestEScript(slea.readInt(), questId);
//                NPCScriptManager.getInstance().endQuest(c, slea.readInt(), questId, false);
                break;
            }
            case QuestReq_LaterStep: { // 任務完成效果
                if (chr.getQuestStatus(questId) == 2) {
                    chr.send(EffectPacket.showSpecialEffect(EffectOpcode.UserEffect_QuestComplete)); // 任務完成
                    chr.getMap().broadcastMessage(chr, EffectPacket.showForeignEffect(chr.getId(), EffectOpcode.UserEffect_QuestComplete), false);
                }
            }
        }
    }

    /*
     * 倉庫操作
     */
    public static void Storage(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || chr.getTrunk() == null) {
            return;
        }
        if (MapleAntiMacro.isAntiNow(chr.getName())) {
            chr.dropMessage(5, "被使用測謊機時無法操作。");
            c.sendEnableActions();
            return;
        }
        if (chr.getConversation() != ConversationType.ON_TRUNK) {
            c.sendEnableActions();
            return;
        }
        byte mode = slea.readByte();
        MapleTrunk storage = chr.getTrunk();
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (storage == null || (!storage.isPwdChecked() && mode != TrunkReq_CheckSSN2)) {
            chr.setConversation(ConversationType.NONE);
            c.sendEnableActions();
            return;
        }
        switch (mode) {
            case TrunkReq_CheckSSN2: {
                String secondPwd = slea.readMapleAsciiString();
                if (c.CheckSecondPassword(secondPwd)) {
                    storage.setPwdChecked(true);
                    storage.sendStorage(c);
                } else {
                    storage.secondPwdRequest(c, -1);
                }
                break;
            }
            case TrunkReq_GetItem: { // 取出
                byte type = slea.readByte();
                short slot = slea.readByte();
                slot = storage.getSlot(MapleInventoryType.getByType(type), slot);
                Item item = storage.getItem(slot); //獲取道具在倉庫的信息
                if (item != null) {
                    //檢測是否是唯一道具
                    if (ii.isPickupRestricted(item.getItemId()) && chr.getItemQuantity(item.getItemId(), true) > 0) {
                        c.announce(NPCPacket.getStorageError((byte) TrunkOptType.TruncRes_GetHavingOnlyItem));
                        return;
                    }
                    //檢測取回道具楓幣是否足夠
                    int meso = storage.getNpcId() == 9030100 || storage.getNpcId() == 9031016 ? 1000 : 0;
                    if (chr.getMeso() < meso) {
                        c.announce(NPCPacket.getStorageError((byte) TrunkOptType.TrunkRes_GetNoMoney)); //取回道具楓幣不足
                        return;
                    }
                    //檢測角色背包是否有位置
                    if (MapleInventoryManipulator.checkSpace(c, item.getItemId(), item.getQuantity(), item.getOwner())) {
                        item = storage.takeOut(slot); //從倉庫中取出這個道具
                        int flag = item.getAttribute();
                        int accShareItem = ItemAttribute.AccountSharable.getValue() | ItemAttribute.CutUsed.getValue();
                        if ((flag & accShareItem) != accShareItem) {
                            flag &= ~ItemAttribute.TradeOnce.getValue();
                            flag &= ~ItemAttribute.CutUsed.getValue();
                            flag &= ~ItemAttribute.AccountSharable.getValue();
                        }
                        if (ii.isSharableOnce(item.getItemId())) {
                            flag |= ItemAttribute.TradeBlock.getValue();
                        }
                        item.setAttribute(flag);
                        MapleInventoryManipulator.addFromDrop(c, item, false); //給角色道具
                        if (meso > 0) { //扣取角色的楓幣
                            chr.gainMeso(-meso, false);
                        }
                        storage.sendTakenOut(c, ItemConstants.getInventoryType(item.getItemId()));
                    } else {
                        c.announce(NPCPacket.getStorageError((byte) TrunkOptType.TrunkRes_GetUnknown)); //發送背包是滿的封包
                    }
                } else {
                    //AutobanManager.getInstance().autoban(c, "試圖從倉庫取出不存在的道具.");
                    log.info("[作弊] " + chr.getName() + " (等級 " + chr.getLevel() + ") 試圖從倉庫取出不存在的道具.");
                    WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.spouseMessage(UserChatMessageType.淺紫, "[GM消息] 玩家: " + chr.getName() + " (等級 " + chr.getLevel() + ") 試圖從倉庫取出不存在的道具."));
                    c.sendEnableActions();
                }
                break;
            }
            case TrunkReq_PutItem: { // 存入
                short slot = slea.readShort();
                int itemId = slea.readInt();
                short quantity = slea.readShort();
                //檢測保存道具的數量是否小於 1
                if (quantity < 1) {
                    AutobanManager.getInstance().autoban(c, "試圖存入到倉庫的道具數量: " + quantity + " 道具ID: " + itemId);
                    return;
                }
                //檢測倉庫的道具是否已滿
                if (storage.isFull()) {
                    c.announce(NPCPacket.getStorageError((byte) TrunkRes_PutNoSpace));
                    return;
                }
                //檢測角色背包當前道具是否有道具
                MapleInventoryType type = ItemConstants.getInventoryType(itemId);
                if (chr.getInventory(type).getItem(slot) == null) {
                    c.sendEnableActions();
                    return;
                }
                //檢測楓幣是否足夠
                int meso = storage.getNpcId() == 9030100 || storage.getNpcId() == 9031016 ? 500 : 100;
                if (chr.getMeso() < meso) {
                    c.announce(NPCPacket.getStorageError((byte) TrunkRes_PutNoMoney));
                    return;
                }
                //開始操作保存道具到倉庫
                Item item = chr.getInventory(type).getItem(slot).copy();
                int flag = item.getCAttribute();
                //檢測道具是否為寵物道具
                if (ItemConstants.類型.寵物(item.getItemId())) {
                    c.announce(NPCPacket.getStorageError((byte) TrunkRes_PutUnknown));
                    return;
                }
                //被封印的道具不能存入倉庫
                if (ItemAttribute.Seal.check(flag)) {
                    c.sendEnableActions();
                    return;
                }
                if ((ItemAttribute.TradeBlock.check(flag) || ii.isTradeBlock(item.getItemId())) && !ItemAttribute.AccountSharable.check(flag) && !ItemAttribute.TradeOnce.check(flag) && !ItemAttribute.CutUsed.check(flag)) {
                    c.sendEnableActions();
                    return;
                }
                //檢測道具是否為唯一道具 且角色倉庫已經有這個道具
                if (ii.isPickupRestricted(item.getItemId()) && storage.findById(item.getItemId()) != null) {
                    c.sendEnableActions();
                    return;
                }
                if (item.getItemId() == itemId && (item.getQuantity() >= quantity || ItemConstants.類型.可充值道具(itemId))) {
                    //如果是飛鏢子彈道具就設置保存的數量為道具當前的數量
                    if (ItemConstants.類型.可充值道具(itemId)) {
                        quantity = item.getQuantity();
                    }
                    MapleInventoryManipulator.removeFromSlot(c, type, slot, quantity, false); //刪除角色背包中的道具
                    chr.gainMeso(-meso, false, false); //收取保存到倉庫的費用
                    item.setQuantity(quantity); //設置道具的數量
                    storage.store(item); //存入道具到倉庫
                    storage.sendStored(c, ItemConstants.getInventoryType(itemId)); //發送當前倉庫的道具封包
                } else {
                    AutobanManager.getInstance().addPoints(c, 1000, 0, "試圖存入到倉庫的道具: " + itemId + " 數量: " + quantity + " 當前玩家用道具: " + item.getItemId() + " 數量: " + item.getQuantity());
                }
                break;
            }
            case TrunkReq_SortItem: { // 倉庫物品排序
                storage.arrange();
                storage.update(c);
                break;
            }
            case TrunkReq_Money: { // 楓幣
                DatabaseConnection.domain(con -> {
                    long meso = slea.readLong();
                    long storageMesos = storage.getMesoForUpdate(con);
                    long playerMesos = chr.getMeso();
                    if ((meso > 0 && storageMesos >= meso) || (meso < 0 && playerMesos >= -meso)) {
                        if (meso < 0 && (storageMesos - meso) < 0) {
                            meso = -(ServerConfig.CHANNEL_PLAYER_MAXMESO - storageMesos);
                            if ((-meso) > playerMesos) {
                                return null;
                            }
                        } else if (meso > 0 && (playerMesos + meso) < 0) {
                            meso = ServerConfig.CHANNEL_PLAYER_MAXMESO - playerMesos;
                            if ((meso) > storageMesos) {
                                return null;
                            }
                        } else if (meso + playerMesos > ServerConfig.CHANNEL_PLAYER_MAXMESO) {
                            chr.dropMessage(1, "楓幣將達到系統上限，不能取出。");
                            c.announce(NPCPacket.getStorageError((byte) TrunkRes_TradeBlocked));
                            return null;
                        } else {
                            storage.setMeso(con, storageMesos - meso);
                            chr.gainMeso(meso, false, false);
                        }
                    } else {
                        AutobanManager.getInstance().addPoints(c, 1000, 0, "Trying to store or take out unavailable amount of mesos (" + meso + "/" + storage.getMeso() + "/" + c.getPlayer().getMeso() + ")");
                    }
                    return null;
                });
                storage.sendMeso(c);
                break;
            }
            case TrunkReq_CloseDialog: { // 退出倉庫
                storage.close();
                chr.setConversation(ConversationType.NONE);
                break;
            }
            default:
                System.out.println("未處理的倉庫操作，模式: " + mode);
                break;
        }
    }

    /**
     * 和NPC交談也就是對話操作
     */
    public static void userScriptMessageAnswer(MaplePacketReader slea, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            return;
        }
        if (player.getConversation() != ConversationType.TALK_TO_NPC) {
            return;
        }
        ScriptManager sm = player.getScriptManager();
        int npcid = slea.readInt();
        byte lastType = slea.readByte(); // 00 last message type (2 = yesno, 0F = acceptdecline)
        NpcMessageType nmt = NpcMessageType.getByVal(lastType);
        if (nmt == null) {
            c.getPlayer().dropMessage(1, "Unknown NpcMessageType:" + lastType);
            return;
        }
//        ScriptNpc npc = null;
//        NPCConversationManager npc = null;
//        if (!sm.isActive(sm.getLastActiveScriptType())) {
//            npc = NPCScriptManager.getInstance().getCM(c);
//            if (npc == null) {
//                return;
//            } else if (npc.getLastSMType() != lastType) {
//                if (player.isShowPacket()) {
//                    player.dropMessage(6, "[ScriptMessage] LastSMType Error! Packet Type:" + nmt);
//                    npc.dispose();
//                }
//                return;
//            }
//        }
        int action; // 00 = end chat/no/decline, 01 == next/yes/accept
        int selection = -1;
        long answer = 0;
        short unk;
        String ans = null;
        switch (nmt) {
            case Say:
                slea.readInt();
                slea.readMapleAsciiString();
                action = slea.readByte();
                break;
            case AskAccept:
            case AskYesNo:
                action = slea.readByte();
                selection = action;
                break;
            case AskMenu:
            case AskSlideMenu:
                action = slea.readByte() > 0 ? 1 : -1;
                if (action > 0) {
                    answer = Math.max(slea.readInt(), 0);
                    selection = (int) answer;
                }
                break;
            case AskText:
            case AskBoxtext:
            case OnAskNumberUseKeyPad:
                action = slea.readByte() > 0 ? 1 : -1;
                if (action > 0) {
                    String returnText = slea.readMapleAsciiString();
                    ans = returnText;
//                    if (npc != null) npc.setText(returnText);
                }
                break;
            case AskNumber:
                action = slea.readByte() > 0 ? 1 : -1;
                if (action > 0) {
                    answer = Math.max(slea.readLong(), 0L);
                    selection = (int) answer;
//                    if (npc != null) npc.setNumber(answer);
                }
                break;
            case AskAvatar:
                action = slea.readByte() > 0 ? 1 : -1;
                if (action > 0) {
                    slea.readByte(); // second
                    slea.readByte(); // 確認變更
                    answer = slea.readByte();
                    selection = (int) answer;
                }
                break;
            case AskAndroid:
                action = slea.readByte() > 0 ? 1 : -1;
                if (action > 0) {
                    answer = slea.readByte();
                    selection = (int) answer;
                }
                break;
            case AskPet:
            case AskPetAll:
                action = slea.readByte() > 0 ? 1 : -1;
                if (action > 0) {
                    answer = slea.readLong();
//                    if (npc != null) {
//                        npc.setPetSN(answer);
//                        selection = 1;
//                    }
                }
                break;
            case AskAngelicBuster: {
                action = slea.readByte();
                answer = action;
                selection = (int) answer;
                break;
            }
            case AskAvatarZero:
                slea.readByte(); // 確認變更
                action = slea.readByte() > 0 ? 1 : -1;
                if (action > 0) {
                    answer = slea.readByte();
//                    npc.setNumber(slea.readByte());
                    selection = (int) answer;
                }
                break;
            case AskAvatarMixColor:
                action = 1;
                break;
            case AskConfirmAvatarChange:
            case AskAvatarRandomMixColor:
                action = 1;
                answer = slea.readByte();
                break;
            case Monologue:
            case OnAskScreenShinningStarMsg:
                action = 1;
                break;
            case PlayMovieClip:
                answer = slea.readByte();
                action = answer == -1 ? -1 : 1;
                selection = (int) answer;
                break;
            case AskSelectMenu:
                action = slea.readByte();
                answer = slea.readByte();
                selection = (int) answer;
                break;
            case AskIngameDirection:
                slea.readByte();
                action = slea.readByte();
                break;
            default:
                action = slea.readByte();
                break;
        }
        if (sm.isActive(sm.getLastActiveScriptType())) {
            sm.handleAction(sm.getLastActiveScriptType(), nmt, action, answer, ans);
            return;
        }
        if (action == -1) {
            sm.dispose();
            return;
        }
        switch (sm.getLastActiveScriptType()) {
            case Npc:
            case Item:
            case onUserEnter:
            case onFirstUserEnter:
            case Command: {
//                NPCScriptManager.getInstance().action(c, (byte) action, lastType, selection);
                break;
            }
            case QuestStart:
//                sm.startQuestSScript()
//                NPCScriptManager.getInstance().startAction(c, (byte) action, lastType, selection);
                break;
            case QuestEnd:
//                NPCScriptManager.getInstance().endAction(c, (byte) action, lastType, selection);
                break;
        }
    }

    /*
     * 全部裝備持久修復
     */
    public static void repairAll(MapleClient c) {
        Equip eq;
        double rPercentage;
        int price = 0;
        Map<String, Integer> eqStats;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Map<Equip, Integer> eqs = new HashMap<>();
        MapleInventoryType[] types = {MapleInventoryType.EQUIP, MapleInventoryType.EQUIPPED};
        for (MapleInventoryType type : types) {
            for (Item item : c.getPlayer().getInventory(type).newList()) {
                if (item instanceof Equip) { //redundant
                    eq = (Equip) item;
                    if (eq.getDurability() >= 0) {
                        eqStats = ii.getItemBaseInfo(eq.getItemId());
                        if (eqStats.containsKey("durability") && eqStats.get("durability") > 0 && eq.getDurability() < eqStats.get("durability")) {
                            rPercentage = (100.0 - Math.ceil((eq.getDurability() * 1000.0) / (eqStats.get("durability") * 10.0)));
                            eqs.put(eq, eqStats.get("durability"));
                            price += (int) Math.ceil(rPercentage * ii.getUnitPrice(eq.getItemId()) / (ii.getReqLevel(eq.getItemId()) < 70 ? 100.0 : 1.0));
                        }
                    }
                }
            }
        }
        if (eqs.size() <= 0 || c.getPlayer().getMeso() < price) {
            return;
        }
        c.getPlayer().gainMeso(-price, true);
        Equip ez;
        for (Entry<Equip, Integer> eqqz : eqs.entrySet()) {
            ez = eqqz.getKey();
            ez.setDurability(eqqz.getValue());
            c.getPlayer().forceUpdateItem(ez.copy());
        }
    }

    /*
     * 當個裝備持久修復
     */
    public static void repair(MaplePacketReader slea, MapleClient c) {
        if (slea.available() < 4) {
            return;
        }
        int position = slea.readInt(); //who knows why this is a int
        MapleInventoryType type = position < 0 ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP;
        Item item = c.getPlayer().getInventory(type).getItem((byte) position);
        if (item == null) {
            return;
        }
        Equip eq = (Equip) item;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Map<String, Integer> eqStats = ii.getItemBaseInfo(item.getItemId());
        if (eq.getDurability() < 0 || !eqStats.containsKey("durability") || eqStats.get("durability") <= 0 || eq.getDurability() >= eqStats.get("durability")) {
            return;
        }
        double rPercentage = (100.0 - Math.ceil((eq.getDurability() * 1000.0) / (eqStats.get("durability") * 10.0)));
        //drpq level 105 weapons - ~420k per %; 2k per durability point
        //explorer level 30 weapons - ~10 mesos per %
        int price = (int) Math.ceil(rPercentage * ii.getUnitPrice(eq.getItemId()) / (ii.getReqLevel(eq.getItemId()) < 70 ? 100.0 : 1.0)); // / 100 for level 30?
        if (c.getPlayer().getMeso() < price) {
            return;
        }
        c.getPlayer().gainMeso(-price, false);
        eq.setDurability(eqStats.get("durability"));
        c.getPlayer().forceUpdateItem(eq.copy());
    }

    /*
     * 更新任務信息
     */
    public static void UpdateQuest(MaplePacketReader slea, MapleClient c) {
        MapleQuest quest = MapleQuest.getInstance(slea.readShort());
        if (quest != null) {
            c.getPlayer().updateQuest(c.getPlayer().getQuest(quest), true);
        }
    }

    public static void UseItemQuest(MaplePacketReader slea, MapleClient c) {
        short slot = slea.readShort();
        int itemId = slea.readInt();
        Item item = c.getPlayer().getInventory(MapleInventoryType.ETC).getItem(slot);
        int qid = slea.readInt();
        MapleQuest quest = MapleQuest.getInstance(qid);
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Pair<Integer, Map<Integer, Integer>> questItemInfo = null;
        boolean found = false;
        for (Item i : c.getPlayer().getInventory(MapleInventoryType.ETC)) {
            if (i.getItemId() / 10000 == 422) {
                questItemInfo = ii.questItemInfo(i.getItemId());
                if (questItemInfo != null && questItemInfo.getLeft() == qid && questItemInfo.getRight() != null && questItemInfo.getRight().containsKey(itemId)) {
                    found = true;
                    break; //i believe it's any order
                }
            }
        }
        if (quest != null && found && item != null && item.getQuantity() > 0 && item.getItemId() == itemId) {
            int newData = slea.readInt();
            MapleQuestStatus stats = c.getPlayer().getQuestNoAdd(quest);
            if (stats != null && stats.getStatus() == 1) {
                stats.setCustomData(String.valueOf(newData));
                c.getPlayer().updateQuest(stats, true);
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.ETC, slot, questItemInfo.getRight().get(item.getItemId()).shortValue(), false);
            }
        }
    }

    public static void RPSGame(MaplePacketReader slea, MapleClient c) {
        if (slea.available() == 0 || c.getPlayer() == null || c.getPlayer().getMap() == null || !c.getPlayer().getMap().containsNPC(9000019)) {
            if (c.getPlayer() != null && c.getPlayer().getRPS() != null) {
                c.getPlayer().getRPS().dispose(c);
            }
            return;
        }
        byte mode = slea.readByte();
        switch (mode) {
            case 0: //start game
            case 5: //retry
                if (c.getPlayer().getRPS() != null) {
                    c.getPlayer().getRPS().reward(c);
                }
                if (c.getPlayer().getMeso() >= 1000) {
                    c.getPlayer().setRPS(new RockPaperScissors(c, mode));
                } else {
                    c.announce(MaplePacketCreator.getRPSMode((byte) 0x08, -1, -1, -1));
                }
                break;
            case 1: //answer
                if (c.getPlayer().getRPS() == null || !c.getPlayer().getRPS().answer(c, slea.readByte())) {
                    c.announce(MaplePacketCreator.getRPSMode((byte) 0x0D, -1, -1, -1));
                }
                break;
            case 2: //time over
                if (c.getPlayer().getRPS() == null || !c.getPlayer().getRPS().timeOut(c)) {
                    c.announce(MaplePacketCreator.getRPSMode((byte) 0x0D, -1, -1, -1));
                }
                break;
            case 3: //continue
                if (c.getPlayer().getRPS() == null || !c.getPlayer().getRPS().nextRound(c)) {
                    c.announce(MaplePacketCreator.getRPSMode((byte) 0x0D, -1, -1, -1));
                }
                break;
            case 4: //leave
                if (c.getPlayer().getRPS() != null) {
                    c.getPlayer().getRPS().dispose(c);
                } else {
                    c.announce(MaplePacketCreator.getRPSMode((byte) 0x0D, -1, -1, -1));
                }
                break;
        }

    }

    /*
     * 使用小地圖下面的快速移動
     */
    public static void OpenQuickMoveNpc(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        int npcid = slea.readInt();
        if (c == null || chr == null || chr.hasBlockedInventory() || chr.isInBlockedMap() || chr.inEvent() || 875999999 == chr.getMapId() || npcid == 0) {
            chr.dropMessage(5, "觸發定身需要解卡請輸入@EA");
            return;
        }
        for (MapleQuickMove mqm : chr.getMap().QUICK_MOVE) {
            if ((mqm.TESTPIA && !ServerConfig.TESPIA) || chr.getGmLevel() < mqm.GM_LEVEL) {
                continue;
            }
            if (mqm.NPC == npcid) {
                if (chr.getLevel() < mqm.MIN_LEVEL) {
                    chr.dropMessage(-1, "未達到可使用等級。");
                    return;
                }
                chr.getScriptManager().startNpcScript(mqm.NPC, 0, mqm.SCRIPT);
                break;
            }
        }
    }

    public static void OpenQuickMoveNpcScript(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        final int selection = slea.readInt();
        int qmSize = 0;
        if (chr.getMap().QUICK_MOVE != null) {
            for (MapleQuickMove qm : chr.getMap().QUICK_MOVE) {
                if ((qm.TESTPIA && !ServerConfig.TESPIA) || chr.getGmLevel() < qm.GM_LEVEL) {
                    continue;
                }
                qmSize++;
            }
        }
        if (c == null || chr == null || chr.hasBlockedInventory() || chr.isInBlockedMap() || chr.inEvent() || 875999999 == chr.getMapId() || qmSize < selection + 1) {
            chr.dropMessage(5, "觸發定身需要解卡請輸入@EA");
            return;
        }
        MapleQuickMove mqm = chr.getMap().QUICK_MOVE.get(selection);
        if ((mqm.TESTPIA && !ServerConfig.TESPIA) || chr.getGmLevel() < mqm.GM_LEVEL) {
            return;
        }
        if (chr.getLevel() < mqm.MIN_LEVEL) {
            chr.dropMessage(-1, "未達到可使用等級。");
            return;
        }
        if (mqm.NPC == 0 && (mqm.SCRIPT == null || mqm.SCRIPT.isEmpty())) {
            chr.dropMessage(-1, "這個選項無法使用，請回報給管理員。");
            return;
        }
        chr.getScriptManager().startNpcScript(mqm.NPC, 0, mqm.SCRIPT);
    }

    public static void ExitGaintBoss(MapleClient c, MapleCharacter player) {
        player.getScriptManager().startNpcScript(9390124, 0, null);
    }
}
