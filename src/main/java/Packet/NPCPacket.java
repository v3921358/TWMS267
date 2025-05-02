/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Packet;

import Client.MapleClient;
import Client.inventory.Item;
import Client.inventory.MapleInventoryType;
import Config.constants.enums.NpcMessageType;
import Config.constants.enums.TrunkOptType;
import Net.server.life.MapleNPC;
import Net.server.life.PlayerNPC;
import Net.server.shop.MapleShop;
import Net.server.shop.MapleShopResponse;
import Opcode.Headler.OutHeader;
import Plugin.script.NpcScriptInfo;
import connection.packet.ScriptMan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.data.MaplePacketLittleEndianWriter;

import java.util.Collection;
import java.util.List;

/**
 * @author admin
 */
public class NPCPacket {

    // InitialQuiz
    public static final int InitialQuizRes_Request = 0x0;
    public static final int InitialQuizRes_Fail = 0x1;
    // InitialSpeedQuiz
    public static final int TypeSpeedQuizNpc = 0x0;
    public static final int TypeSpeedQuizMob = 0x1;
    public static final int TypeSpeedQuizItem = 0x2;
    // SpeakerTypeID
    public static final int NoESC = 0x1;
    public static final int NpcReplacedByUser = 0x2;
    public static final int NpcReplayedByNpc = 0x4;
    public static final int FlipImage = 0x8;
    private static final Logger log = LoggerFactory.getLogger(NPCPacket.class);

    public static byte[] sendNpcHide(List<Integer> hide) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_LimitedNPCDisableInfo.getValue());
        mplew.write(hide.size());
        for (Integer h : hide) {
            mplew.writeInt(h.intValue());
        }
        return mplew.getPacket();
    }

    public static byte[] spawnNPC(MapleNPC life) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.CTX_Npc_Enter_Field.getValue());
        mplew.writeInt(life.getObjectId());
        writeNpcData(mplew, life, false);
        return mplew.getPacket();
    }

    public static byte[] removeNPC(int objectid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_NpcLeaveField.getValue());
        mplew.writeInt(objectid);

        return mplew.getPacket();
    }

    public static byte[] removeNPCController(int objectid, boolean miniMap) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.CTX_Npc_Change_Controller.getValue());
        mplew.writeBool(miniMap);
        mplew.writeInt(objectid);

        return mplew.getPacket();
    }

    private static void writeNpcData(MaplePacketLittleEndianWriter mplew, MapleNPC npc, boolean MiniMap) {
        mplew.writeInt(npc.getId());
        mplew.writeShort(npc.getPosition().x);
        mplew.writeShort(npc.getCy());
        mplew.writeInt(-1);
        mplew.writeInt(-1);
        mplew.writeBool(npc.isMove());
        mplew.write(npc.getF() == 1 ? 0 : 1);
        mplew.writeShort(npc.getCurrentFH());
        mplew.writeShort(npc.getRx0());
        mplew.writeShort(npc.getRx1());
        mplew.writeShort(npc.getCy()); // v267 官方改變封包y軸
        mplew.writeShort(npc.getCy()); // v267 官方改變封包y軸
        mplew.write(MiniMap || npc.isHidden() ? 0 : 1);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeInt(-1);
        mplew.write(0);
        mplew.writeHexString("40 E0 FD 3B 37 4F 01");
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeMapleAsciiString("");
        mplew.write(0);
    }

    public static byte[] spawnNPCRequestController(MapleNPC life, boolean MiniMap) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.CTX_Npc_Change_Controller.getValue());
        mplew.write(1);
        mplew.writeInt(life.getObjectId());
        writeNpcData(mplew, life, MiniMap);

        return mplew.getPacket();
    }

    public static byte[] spawnPlayerNPC(PlayerNPC npc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.PLAYER_NPC.getValue());
        mplew.write(npc.getF() == 1 ? 0 : 1);
        mplew.writeInt(npc.getId());
        mplew.writeMapleAsciiString(npc.getName());
        npc.getPlayer().getAvatarLook().encode(mplew, false);

        return mplew.getPacket();
    }

    public static byte[] OnSay(byte type, int npcId, boolean b, int u1, int n3, short u2, boolean bPrev, boolean bNext, String sText, int n5) {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(npcId);
        nsi.setSpeakerType(type);
        nsi.setOverrideSpeakerTemplateID(u1);
        nsi.setParam(u2);
        nsi.setInnerOverrideSpeakerTemplateID(n3);
        nsi.setText(sText);
        nsi.setPrevPossible(bPrev);
        nsi.setNextPossible(bNext);
        nsi.setDelay(n5);

        return ScriptMan.scriptMessage(nsi, NpcMessageType.Say).getData();
    }

    public static byte[] OnSayImage(byte b, int n, short n2, String[] array) {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(n2);
        nsi.setImages(array);

        return ScriptMan.scriptMessage(nsi, NpcMessageType.SayImage).getData();
    }

    public static byte[] OnSayIllu(byte b, int n, boolean b2, int n2, int n3, short n4, boolean b3, boolean b4, String s, int n5, int n6, boolean b5) {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(n4);
        nsi.setInnerOverrideSpeakerTemplateID(n3);
        nsi.setText(s);
        nsi.setPrevPossible(b3);
        nsi.setNextPossible(b4);
        nsi.setDelay(n5);
        nsi.setUnk(n6);
        nsi.setBUnk(b5);

        return ScriptMan.scriptMessage(nsi, NpcMessageType.SayIllustration).getData();
    }

    public static byte[] OnSayDualIllu(byte b, int n, boolean b2, int n2, int n3, short n4, boolean b3, boolean b4, String s, int n5, int n6) {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(n4);
        nsi.setInnerOverrideSpeakerTemplateID(n3);
        nsi.setText(s);
        nsi.setPrevPossible(b3);
        nsi.setNextPossible(b4);
        nsi.setDelay(n5);
        nsi.setUnk(n6);

        return ScriptMan.scriptMessage(nsi, NpcMessageType.SayDualIllustration).getData();
    }

    public static byte[] OnAskYesNo(byte b, int n, int n2, short n3, String s) {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(n3);
        nsi.setInnerOverrideSpeakerTemplateID(n2);
        nsi.setText(s);

        return ScriptMan.scriptMessage(nsi, NpcMessageType.AskYesNo).getData();
    }

    public static byte[] OnAskAccept(byte b, int n, int diffnpc, short n3, String s) {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(n3);
        nsi.setInnerOverrideSpeakerTemplateID(diffnpc);
        nsi.setText(s);

        return ScriptMan.scriptMessage(nsi, NpcMessageType.AskAccept).getData();
    }

    public static byte[] OnAskAcceptNoESC(byte b, int n, int diffnpc, short n3, String s) {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(n3);
        nsi.setInnerOverrideSpeakerTemplateID(diffnpc);
        nsi.setText(s);

        return ScriptMan.scriptMessage(nsi, NpcMessageType.AskAcceptNoEsc).getData();
    }

    public static byte[] OnAskText(byte b, int n, int n2, short sParam, short nLenMin, short nLenMax, String sMsg, String sMsgDefault) {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(sParam);
        nsi.setInnerOverrideSpeakerTemplateID(n2);
        nsi.setText(sMsg);
        nsi.setDefaultText(sMsgDefault);
        nsi.setMin(nLenMin);
        nsi.setMax(nLenMax);

        return ScriptMan.scriptMessage(nsi, NpcMessageType.AskText).getData();
    }

    public static byte[] OnAskBoxText(byte b, int n, int n2, short n3, short nCol, short nLine, String sMsg, String sMsgDefault) {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(n3);
        nsi.setInnerOverrideSpeakerTemplateID(n2);
        nsi.setText(sMsg);
        nsi.setDefaultText(sMsgDefault);
        nsi.setCol(nCol);
        nsi.setLine(nLine);

        return ScriptMan.scriptMessage(nsi, NpcMessageType.AskBoxtext).getData();
    }

    public static byte[] OnAskNumber(byte b, int n, short n2, long nDef, long nMin, long nMax, String sMsg) {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(n2);
        nsi.setText(sMsg);
        nsi.setDefaultNumber(nDef);
        nsi.setMin(nMin);
        nsi.setMax(nMax);

        return ScriptMan.scriptMessage(nsi, NpcMessageType.AskNumber).getData();
    }

    public static byte[] OnAskMenu(byte b, int n, int diffnpc, short n3, String s) {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(n3);
        nsi.setInnerOverrideSpeakerTemplateID(diffnpc);
        nsi.setText(s);
        return ScriptMan.scriptMessage(nsi, NpcMessageType.AskMenu).getData();
    }

    public static byte[] OnAskAvatar(byte b, int n, int n2, boolean b2, boolean b3, int[] array, String s) {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(0);
        nsi.setColor(0);
        nsi.setSecondLookValue(b2 ? 1 : b3 ? 2 : 0);
        nsi.setText(s);
        nsi.setOptions(array);
        nsi.setItemID(n2);
        nsi.setSrcBeauty(0);

        return ScriptMan.scriptMessage(nsi, NpcMessageType.AskAvatar).getData();
    }

    public static byte[] OnAskAndroid(byte nSpeakerTypeID, int nSpeakerTemplateID, int cardID, int[] array, String s) {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.setTemplateID(nSpeakerTemplateID);
        nsi.setSpeakerType(nSpeakerTypeID);
        nsi.setParam(0);
        nsi.setColor(0);
        nsi.setText(s);
        nsi.setOptions(array);
        nsi.setItemID(cardID);
        nsi.setSrcBeauty(0);

        return ScriptMan.scriptMessage(nsi, NpcMessageType.AskAndroid).getData();
    }

    public static byte[] OnAskAngelicBuster(byte b, int n) {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(0);
        nsi.setColor(0);

        return ScriptMan.scriptMessage(nsi, NpcMessageType.AskAngelicBuster).getData();
    }

    public static byte[] OnAskZeroAvatar(byte nSpeakerTypeID, int nSpeakerTemplateID, int cardID, int[] array, int[] array2, String s) {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.setTemplateID(nSpeakerTemplateID);
        nsi.setSpeakerType(nSpeakerTypeID);
        nsi.setParam(0);
        nsi.setColor(0);
        nsi.setText(s);
        nsi.setItemID(cardID);
        nsi.setOptions(array);
        nsi.setOptions2(array2);
        nsi.setSrcBeauty(0);
        nsi.setSrcBeauty2(0);

        return ScriptMan.scriptMessage(nsi, NpcMessageType.AskAvatarZero).getData();
    }

    public static byte[] OnAskPet(byte b, int n, List<Item> items, String sMsg) {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(0);
        nsi.setColor(0);
        nsi.setText(sMsg);
        nsi.setItems(items);

        return ScriptMan.scriptMessage(nsi, NpcMessageType.AskPet).getData();
    }

    public static byte[] OnAskPetAll(byte b, int n, List<Item> list, String s) {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(0);
        nsi.setColor(0);
        nsi.setText(s);
        nsi.setItems(list);

        return ScriptMan.scriptMessage(nsi, NpcMessageType.AskPetAll).getData();
    }

    public static byte[] OnAskQuiz(byte b, int n, short n2, boolean request, int nMinInput, int nMaxInput, String sTitle, String sProblemText, String sHintText) {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(n2);
        nsi.setType(request ? 0 : 1);
        nsi.setTitle(sTitle);
        nsi.setProblemText(sProblemText);
        nsi.setHintText(sHintText);
        nsi.setMin(nMinInput);
        nsi.setMax(nMaxInput);
        nsi.setTime(0);

        return ScriptMan.scriptMessage(nsi, NpcMessageType.InitialQuiz).getData();
    }

    public static byte[] OnAskSpeedQuiz(byte b, int n, short n2, boolean b2, int nType, int dwAnswer, int nCorrect, int nRemain, int tRemainInitialQuiz) {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(n2);
        nsi.setType(b2 ? 0 : 1);
        nsi.setQuizType(nType);
        nsi.setAnswer(dwAnswer);
        nsi.setCorrectAnswers(nCorrect);
        nsi.setRemaining(nRemain);
        nsi.setTime(tRemainInitialQuiz);

        return ScriptMan.scriptMessage(nsi, NpcMessageType.InitialSpeedQuiz).getData();
    }

    public static byte[] OnAskICQuiz(byte b, int n, short n2, boolean b2, String s, String s2, int n3) {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(n2);
        nsi.setType(b2 ? 0 : 1);
        nsi.setText(s);
        nsi.setHintText(s2);
        nsi.setTime(n3);

        return ScriptMan.scriptMessage(nsi, NpcMessageType.ICQuiz).getData();
    }

    public static byte[] OnAskOlympicQuiz(byte b, int n, short n2, boolean b2, int nType, int nQuestion, int nCorrect, int nRemain, int tRemainInitialQuiz) {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(n2);
        nsi.setType(b2 ? 0 : 1);
        nsi.setQuizType(nType);
        nsi.setAnswer(nQuestion);
        nsi.setCorrectAnswers(nCorrect);
        nsi.setRemaining(nRemain);
        nsi.setTime(tRemainInitialQuiz);

        return ScriptMan.scriptMessage(nsi, NpcMessageType.AskOlympicQuiz).getData();
    }

    public static byte[] OnAskNumberKeypad(int n, short n2, int n3) {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.setTemplateID(n);
        nsi.setSpeakerType(3);
        nsi.setParam(n2);
        nsi.setColor(0);
        nsi.setDefaultNumber(n3);

        return ScriptMan.scriptMessage(nsi, NpcMessageType.OnAskNumberUseKeyPad).getData();
    }

    public static byte[] OnAskUserSurvey(int n, short n2, int n3, String s) {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.setTemplateID(n);
        nsi.setSpeakerType(3);
        nsi.setParam(n2);
        nsi.setColor(0);
        nsi.setDefaultNumber(n3);
        nsi.setText(s);

        return ScriptMan.scriptMessage(nsi, NpcMessageType.AskUserSurvey).getData();
    }

    public static byte[] OnAskSlideMenu(byte b, int n, int bSlideDlgEX, int nIndex, String sMsg) {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(0);
        nsi.setColor(0);
        nsi.setDlgType(bSlideDlgEX);
        nsi.setDefaultSelect(nIndex);
        nsi.setText(sMsg);

        return ScriptMan.scriptMessage(nsi, NpcMessageType.AskSlideMenu).getData();
    }

    public static byte[] OnAskSelectMenu(byte b, int n, int n2, String[] array) {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(0);
        nsi.setColor(0);
        nsi.setOverrideSpeakerTemplateID(n);
        nsi.setDlgType(n2);
        nsi.setDefaultSelect(0);
        nsi.setSelectText(array);

        return ScriptMan.scriptMessage(nsi, NpcMessageType.AskSelectMenu).getData();
    }

    public static byte[] OnAskPetEvolution(byte b, int n, List<Item> items, String s) {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(0);
        nsi.setColor(0);
        nsi.setText(s);
        nsi.setItems(items);

        return ScriptMan.scriptMessage(nsi, NpcMessageType.AskActionPetEvolution).getData();
    }

    /*
     * 打開1個商店
     */
    public static byte[] getNPCShop(int shopId, MapleShop shop, MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_OpenShopDlg.getValue());
        mplew.writeInt(shopId);//V.160 new
        mplew.write(0);
        PacketHelper.addShopInfo(mplew, shop, c);
        return mplew.getPacket();
    }

    /*
     * 商店操作提示
     */
    public static byte[] confirmShopTransaction(MapleShopResponse code, MapleShop shop, MapleClient c, int indexBought, int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ShopResult.getValue());
        mplew.write(code.getValue());
        switch (code) {
            case ShopRes_BuySuccess: //購買道具 [9B 02] [00] [00 00] 購買回購欄裡面的道具 [9B 02] [00] [01] [00 00 00 00]
            case ShopRes_NotEnoughSpace: //請確認是不是你的背包的空間不夠。[9B 02] [04] [00 00]
            case ShopRes_RechargeSuccess: //充值飛鏢和子彈 V.112修改 以前 0x0A
            case ShopRes_RechargeNoMoney: //充值飛鏢和子彈提示楓幣不足 V.112修改 以前0x0C
            case ShopRes_TradeBlockedNotActiveAccount: //販賣價格比購買價格高.無法購買。
                mplew.writeBool(indexBought >= 0); //是否回購欄的道具
                if (indexBought >= 0) {
                    mplew.writeInt(indexBought); //道具在回購欄的位置 默認從 0 開始 V.160 short=>int
                } else {
                    //V.160:
                    mplew.writeInt(itemId);//new
                    mplew.writeInt(1000000);//new
                    mplew.writeInt(0);//V.161 new
                    mplew.writeInt(0);
                    //mplew.write(0);//del
                    //end
                }
//                mplew.writeInt(0);//V.160 delete
                break;
            case ShopRes_SellSuccess: //賣出道具
//                mplew.writeInt(shop.getId());//V.160 new
                PacketHelper.addShopInfo(mplew, shop, c);
                break;
            case ShopRes_BuyNoStock:
                mplew.writeInt(0);
                break;
            case ShopRes_NpcRandomShopReset:
                mplew.writeInt(0);
                mplew.writeInt(0);
                break;
            case ShopRes_BuyNoMoney:
            case ShopRes_RechargeNoStock:
                break;
            case ShopRes_BuyNoQuest:
                break;
            //10 break
            case ShopRes_BuyNoFloor:
                mplew.writeInt(0);
                break;
            case ShopRes_LimitLevel_Less: // #{-indexBought}級以下才可以購買
                mplew.writeInt(-indexBought);
                break;
            case ShopRes_LimitLevel_More: // #{indexBought}級以上才可以購買
                mplew.writeInt(indexBought);
                break;
            case ShopRes_FailedByBuyLimit:
                mplew.writeInt(0);
                break;
            case ShopRes_BuyStockOver:
                mplew.writeInt(0);
                break;
            case ShopRes_TradeBlockedSnapShot:
                mplew.writeInt(0);
                break;
            case ShopRes_UnableShopVersion:
                mplew.write(0);
                if (false) {
                    PacketHelper.addShopInfo(mplew, shop, c);
                }
                break;
        }

        return mplew.getPacket();
    }

    /*
     * 倉庫取出
     */
    public static byte[] takeOutStorage(short slots, MapleInventoryType type, Collection<Item> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_TrunkResult.getValue());
        mplew.write(TrunkOptType.TrunkRes_GetSuccess);
        mplew.write(slots & 0xFF);
        mplew.write(0);
        mplew.write(0);
        if(type != MapleInventoryType.EQUIP) {
            mplew.write(0);
            mplew.write(0);
            mplew.write(0);
            mplew.write(1);
            mplew.writeZeroBytes(94);
        } else {
            mplew.write(1);
            mplew.writeZeroBytes(97);
        }
        mplew.write(items.size());
        for (Item item : items) {
            PacketHelper.GW_ItemSlotBase_Encode(mplew, item);
        }

        return mplew.getPacket();
    }

    /*
     * 取回道具
     * 0x0A = 請確認是不是你的背包空間不夠。
     * 0x0B = 楓幣不足
     * 保存道具
     * 0x10 = 楓幣不足
     * 0x11 = 倉庫已滿
     */
    public static byte[] getStorageError(byte op) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_TrunkResult.getValue());
        mplew.write(op);

        return mplew.getPacket();
    }

    /*
     * 倉庫存入道具 V267
     */
    public static byte[] storeStorage(short slots, MapleInventoryType type, Collection<Item> items) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_TrunkResult.getValue());
        mplew.write(TrunkOptType.TrunkRes_PutSuccess);
        mplew.write(slots & 0xFF);
        mplew.write(0);
        mplew.write(0);
        if(type != MapleInventoryType.EQUIP) {
            mplew.write(0);
            mplew.write(0);
            mplew.write(0);
            mplew.write(1);
            mplew.writeZeroBytes(94);
        } else {
        mplew.write(1);
        mplew.writeZeroBytes(97);
        }
        mplew.write(items.size()); //仔裡面的道具總數輛
        for (Item item : items) {
            PacketHelper.GW_ItemSlotBase_Encode(mplew, item);
        }
        return mplew.getPacket();
    }

    /*
     * 倉庫道具排序
     */
    public static byte[] arrangeStorage(short slots, Collection<Item> items, boolean changed) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_TrunkResult.getValue());
        mplew.write(TrunkOptType.TrunkRes_SortItem);
        mplew.write(slots & 0xFF);
        mplew.write(0);
        mplew.write(0);
        mplew.write(1);
        mplew.write(1);
        mplew.write(1);
        mplew.write(1);
        mplew.write(1);
        mplew.writeZeroBytes(96);

        mplew.write(items.size());
        for (Item item : items) {
            PacketHelper.GW_ItemSlotBase_Encode(mplew, item);
        }
        mplew.writeInt(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    /*
     * 倉庫保存楓幣
     */
    public static byte[] mesoStorage(short slots, long meso) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_TrunkResult.getValue());
        mplew.write(TrunkOptType.TrunkRes_MoneySuccess);
        mplew.write(slots & 0xFF);
        mplew.write(0);
        mplew.write(1);
        mplew.writeZeroBytes(98);
        mplew.writeLong(meso);
        return mplew.getPacket();
    }

    public static byte[] getStoragePwd(boolean wrong) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_TrunkResult.getValue());
        mplew.write(TrunkOptType.TrunkRes_TrunkCheckSSN2);
        mplew.write(wrong);
        return mplew.getPacket();
    }

    /*
     * 打開倉庫
     */
    public static byte[] getStorage(int npcId, short slots, Collection<Item> items, long meso) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_TrunkResult.getValue());
        mplew.write(TrunkOptType.TrunkRes_OpenTrunkDlg);
        mplew.writeInt(npcId);
        mplew.write(slots & 0xFF); // 倉庫欄位格數
        mplew.write(0);
        mplew.write(1);
        mplew.write(1);
        mplew.write(1);
        mplew.write(1);
        mplew.write(1);
        mplew.write(1);
        mplew.writeZeroBytes(40);
        mplew.write(1);
        mplew.writeZeroBytes(52);
        mplew.writeLong(meso);
        mplew.write(items.size()); //裡面的道具總數
        for (Item item : items) {
            PacketHelper.GW_ItemSlotBase_Encode(mplew, item);
        }
        mplew.writeLong(0);
        return mplew.getPacket();
    }

    public static byte[] setNPCSpecialAction(int npcoid, String string) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_NpcSpecialAction.getValue());
        mplew.writeInt(npcoid);
        mplew.writeMapleAsciiString(string);
        mplew.writeInt(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] updateNPCSpecialAction(int npcoid, int value, int x, int y) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_ForceMoveByScript.getValue());
        mplew.writeInt(npcoid);
        mplew.writeInt(value);
        mplew.writeInt(x);
        mplew.writeInt(y);

        return mplew.getPacket();
    }

    public static byte[] SetBuyLimitCount(int shopId, short position, int itemId, int buyLimit, long l) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SetBuyLimitCount.getValue());
        mplew.writeInt(shopId);
        mplew.writeShort(position);
        mplew.writeInt(itemId);
        mplew.writeShort(buyLimit);
        PacketHelper.addExpirationTime(mplew, l);
        return mplew.getPacket();
    }

    public static byte[] ResetBuyLimitCount(int shopId, List<Integer> list) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ResetBuyLimitcount.getValue());

        mplew.writeInt(shopId);
        mplew.writeInt(list.size());
        for (Integer integer : list) {
            mplew.writeShort(integer);
        }
        return mplew.getPacket();
    }


}
