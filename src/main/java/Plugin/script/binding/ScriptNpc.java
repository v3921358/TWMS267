package Plugin.script.binding;

import Client.MapleClient;
import Client.inventory.Item;
import Client.inventory.MapleInventoryType;
import Config.constants.ItemConstants;
import Config.constants.enums.NpcMessageType;
import Config.constants.enums.ScriptParam;
import Config.constants.enums.ScriptType;
import Net.server.MapleInventoryManipulator;
import Net.server.MapleItemInformationProvider;
import Net.server.maps.MapleSlideMenu;
import Net.server.quest.MapleQuest;
import Net.server.quest.MapleQuestRequirement;
import Net.server.quest.MapleQuestRequirementType;
import Opcode.Headler.OutHeader;
import Packet.MaplePacketCreator;
import Packet.UIPacket;
import Plugin.script.EventManager;
import Plugin.script.NpcScriptInfo;
import Plugin.script.ScriptManager;
import SwordieX.client.party.Party;
import SwordieX.client.party.PartyMember;
import SwordieX.client.party.PartyResult;
import connection.packet.ScriptMan;
import connection.packet.WvsContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tools.SearchGenerator;
import tools.data.MaplePacketLittleEndianWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static Config.constants.enums.NpcMessageType.*;

@Slf4j
public class ScriptNpc extends PlayerScriptInteraction {
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    @Getter
    private final String scriptPath;
    private final int npcId;
    @Getter
    private final MapleClient client;
    private final Object obj;
    private ScriptType scriptType = ScriptType.None;
    @Getter
    @Setter
    private int lastSMType;

    public ScriptNpc(MapleClient client, int npcId, String scriptPath, ScriptType scriptType, Object obj) {
        super(client.getPlayer());
        this.client = client;
        this.npcId = npcId;
        this.obj = obj;
        this.scriptPath = scriptPath;
        this.scriptType = scriptType;
        if (scriptType == ScriptType.Command) {
            setVariable("commands", obj);
        }
    }

    public int getNpcId() {
        return npcId;
    }

    public int getItemId() {
        if (scriptType == ScriptType.Item) {
            return ((Item) obj).getItemId();
        }
        return 0;
    }

    private Object sendScriptMessage(String text, NpcMessageType nmt) throws NullPointerException {
        String checkText = text.replaceAll("[\r\n]", "");
        if (checkText.matches("(.)*#[lL][0-9]+#(.)*")) {
            nmt = AskMenu;
        }

        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo().deepCopy();
        nsi.setText(text);
        nsi.setMessageType(nmt);
        if (nmt != Say) {
            nsi.setNextPossible(false);
            nsi.setPrevPossible(false);
        }
        getPlayer().getScriptManager().getMemory().add(nsi);
        getPlayer().write(ScriptMan.scriptMessage(nsi, nmt));
        Object response = null;
        if (getPlayer().getScriptManager().isActive(getPlayer().getScriptManager().getLastActiveScriptType())) {
            response = getPlayer().getScriptManager().getScriptInfoByType(getPlayer().getScriptManager().getLastActiveScriptType()).awaitResponse();
        }
        if (response == null) {
//            response = -1;
            throw new NullPointerException(ScriptManager.INTENDED_NPE_MSG);
        }
        return response;
    }

    private int sendGeneralSay(String text, NpcMessageType nmt, boolean hasNext) throws NullPointerException {
        String checkText = text.replaceAll("[\r\n]", "");
        if (checkText.matches("(.)*#[lL][0-9]+#(.)*")) {
            nmt = AskMenu;
        }
        if (nmt == Say) {
            NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
            nsi.setNextPossible(hasNext);
            nsi.setPrevPossible(getPlayer().getScriptManager().getMemory().hasBack());
        }
        return (int) (long) sendScriptMessage(text, nmt);
    }

    private int sendGeneralSay(String message, NpcMessageType nmt) throws NullPointerException {
        return sendGeneralSay(message, nmt, false);
    }

//    public int say(String text) {
//        return sendGeneralSay(text, Say);
//    }

//    public int sayNext(String text) {
//        return sendGeneralSay(text, Say, true);
//    }

//    public int askMenu(String message) {
//        return sendGeneralSay(message, AskMenu);
//    }
//
//    public boolean askYesNo(String message) {
//        return sendGeneralSay(message, AskYesNo) != 0;
//    }
//
//    public boolean askAccept(String message) {
//        return sendGeneralSay(message, AskAccept) != 0;
//    }
//
//    public boolean askAcceptNoESC(String message) {
//        return sendGeneralSay(message, AskAcceptNoEsc) != 0;
//    }

    public String askQuiz(byte type, String title, String problemText, String hintText, int min, int max, int time) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setType(type);
        nsi.setTitle(title);
        nsi.setProblemText(problemText);
        nsi.setHintText(hintText);
        nsi.setMin(min);
        nsi.setMax(max);
        nsi.setTime(time);
        return (String) sendScriptMessage("", InitialQuiz);
    }

    public String askSpeedQuiz(byte type, int quizType, int answer, int correctAnswers, int remaining, int time) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setType(type);
        nsi.setQuizType(quizType);
        nsi.setAnswer(answer);
        nsi.setCorrectAnswers(correctAnswers);
        nsi.setRemaining(remaining);
        nsi.setTime(time);
        return (String) sendScriptMessage("", InitialSpeedQuiz);
    }

    public String askText(String message, String defaultText, short minLength, short maxLength) throws NullPointerException {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setMin(minLength);
        nsi.setMax(maxLength);
        nsi.setDefaultText(defaultText);
        return (String) sendScriptMessage(message, AskText);
    }

    public String askBoxText(String def, short columns, short rows) throws NullPointerException {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setDefaultText(def);
        nsi.setCol(columns);
        nsi.setLine(rows);
        return (String) sendScriptMessage("", AskBoxtext);
    }

    public long askNumber(String message, long def, long min, long max) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setDefaultNumber(def);
        nsi.setMin(min);
        nsi.setMax(max);
        return (long) sendScriptMessage(message, AskNumber);
    }

    public int askPet(String message, List<Item> list) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setItems(list);
        return (int)(long) sendScriptMessage(message, AskPet);
    }

    public int askAvatar(String message, int itemId, int secondLookValue, int srcBeauty, int[] styles) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setItemID(itemId);
        nsi.setSecondLookValue(secondLookValue);
        nsi.setOptions(styles);
        nsi.setSrcBeauty(srcBeauty);
        return (int) (long) sendScriptMessage(message, AskAvatar);
    }

    public int askAvatarZero(String message, int itemId, int srcBeauty, int srcBeauty2, int[] styles, int[] styles2) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setItemID(itemId);
        nsi.setOptions(styles);
        nsi.setOptions2(styles2);
        nsi.setSrcBeauty(srcBeauty);
        nsi.setSrcBeauty2(srcBeauty2);
        return (int) (long) sendScriptMessage(message, AskAvatarZero);
    }

    public boolean askAngelicBuster() {
        return ((long) sendScriptMessage("", AskAngelicBuster)) != 0;
    }

    public int askAvatarMixColor(int cardID, String msg, int secondLookValue, int srcBeauty) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setItemID(cardID);
        nsi.setSecondLookValue(secondLookValue);
        nsi.setSrcBeauty(srcBeauty);
        return (int) (long) sendScriptMessage(msg, AskAvatarMixColor);
    }

    public boolean askAvatarRandomMixColor(String msg) {
        return askAvatarRandomMixColor(null, null, msg);
    }

    public boolean askAvatarRandomMixColor(Integer itemID, Integer secondLookValue, String msg) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        if (itemID != null) nsi.setItemID(itemID);
        if (secondLookValue != null) nsi.setSecondLookValue(secondLookValue);
        return ((long) sendScriptMessage(msg, AskAvatarRandomMixColor)) != 0;
    }

    public int sayAvatarMixColorChanged(String msg, int srcBeauty, int drtBeauty, int srcBeauty2, int drtBeauty2) {
        return sayAvatarMixColorChanged(msg, null, srcBeauty, drtBeauty, srcBeauty2, drtBeauty2);
    }

    public int sayAvatarMixColorChanged(String msg, Integer itemID, int srcBeauty, int drtBeauty, int srcBeauty2, int drtBeauty2) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        if (itemID != null) nsi.setItemID(itemID);
        nsi.setSrcBeauty(srcBeauty);
        nsi.setDrtBeauty(drtBeauty);
        nsi.setSrcBeauty2(srcBeauty2);
        nsi.setDrtBeauty2(drtBeauty2);
        return (int) (long) sendScriptMessage(msg, SayAvatarMixColorChanged);
    }

    public boolean askConfirmAvatarChange(int srcBeauty, int srcBeauty2) {
        return askConfirmAvatarChange(null, null, srcBeauty, srcBeauty2);
    }

    public boolean askConfirmAvatarChange(Integer itemID, Integer secondLookValue, int srcBeauty, int srcBeauty2) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        if (itemID != null) nsi.setItemID(itemID);
        if (secondLookValue != null) nsi.setSecondLookValue(secondLookValue);
        nsi.setSrcBeauty(srcBeauty);
        nsi.setSrcBeauty2(srcBeauty2);
        return ((long) sendScriptMessage("", AskConfirmAvatarChange)) != 0;
    }

    //--------------------------------------------------
    // public npc talk Net.api start
    //--------------------------------------------------

    public int sendOkN(String text) {
        return sendOkE(text, npcId);
    }

    public int sayN(String text) {
        return sendOkE(text, npcId);
    }

    public int sendOkN(String text, int idd) {
        return sendOkE(text, idd);
    }

    public int sayN(String text, int idd) {
        return sendOkE(text, idd);
    }

    public int sendOkS(String text, byte type) {
        return sendOkS(text, type, npcId);
    }

    public int sayS(String text, byte type) {
        return sendOkS(text, type, npcId);
    }

    public int sendOkS(String text, byte type, int idd) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(idd);
        nsi.setSpeakerType(3);
        nsi.setOverrideSpeakerTemplateID(0);
        nsi.setParam(type);
        // nsi.setInnerOverrideSpeakerTemplateID(idd);
        nsi.setText(text);
        nsi.setPrevPossible(false);
        nsi.setNextPossible(false);
        nsi.setDelay(0);
        return (int) (long) sendScriptMessage(text, Say);
    }

    public int sendPlayerToNpc(String text) {
        return sendNextS(text, (byte) 3, npcId);
    }

    public int sayPlayerToNpc(String text) {
        return sendNextS(text, (byte) 3, npcId);
    }

    public int sendNextS(String text, byte type) {
        return sendNextS(text, type, npcId);
    }

    public int sayNextS(String text, byte type) {
        return sendNextS(text, type, npcId);
    }

    public int sendNextS(String text, byte type, int idd) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(idd);
        nsi.setSpeakerType(3);
        nsi.setOverrideSpeakerTemplateID(0);
        nsi.setParam(type);
        // nsi.setInnerOverrideSpeakerTemplateID(idd);
        nsi.setText(text);
        nsi.setPrevPossible(false);
        nsi.setNextPossible(true);
        nsi.setDelay(0);
        return (int) (long) sendScriptMessage(text, Say);
//        getClient().announce(NPCPacket.OnSay((byte) 3, idd, false, 0, idd, type, false, true, text, 0));
    }

    public int sendNextN(String text) {
        return this.sendNextN(text, NpcMessageType.Say.getVal(), this.getNpcId());
    }

    public int sayNextN(String text) {
        return this.sendNextN(text, NpcMessageType.Say.getVal(), this.getNpcId());
    }

    public int sendNextN(String text, byte type, int idd) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(idd);
        nsi.setSpeakerType(4);
        nsi.setOverrideSpeakerTemplateID(0);
        nsi.setParam(type);
        // nsi.setInnerOverrideSpeakerTemplateID(idd);
        nsi.setText(text);
        nsi.setPrevPossible(false);
        nsi.setNextPossible(true);
        nsi.setDelay(0);
        return (int) (long) sendScriptMessage(text, Say);
//        getClient().announce(NPCPacket.OnSay((byte) 4, idd, false, 0, idd, type, false, true, text, 0));
    }

    public int sendPrevS(String text, byte type) {
        return sendPrevS(text, type, npcId);
    }

    public int sayPrevS(String text, byte type) {
        return sendPrevS(text, type, npcId);
    }

    public int sendPrevS(String text, byte type, int idd) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(idd);
        nsi.setSpeakerType(3);
        nsi.setOverrideSpeakerTemplateID(0);
        nsi.setParam(type);
        // nsi.setInnerOverrideSpeakerTemplateID(idd);
        nsi.setText(text);
        nsi.setPrevPossible(true);
        nsi.setNextPossible(false);
        nsi.setDelay(0);
        return (int) (long) sendScriptMessage(text, Say);
//        getClient().announce(NPCPacket.OnSay((byte) 3, idd, false, 0, idd, type, true, false, text, 0));
    }

    public int sendPrevN(String text) {
        return this.sendPrevN(text, NpcMessageType.Say.getVal(), npcId);
    }

    public int sayPrevN(String text) {
        return this.sendPrevN(text, NpcMessageType.Say.getVal(), npcId);
    }

    public int sendPrevN(String text, byte type) {
        return this.sendPrevN(text, type, this.getNpcId());
    }

    public int sayPrevN(String text, byte type) {
        return this.sendPrevN(text, type, this.getNpcId());
    }

    public int sendPrevN(String text, byte type, int idd) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(idd);
        nsi.setSpeakerType(4);
        nsi.setOverrideSpeakerTemplateID(0);
        nsi.setParam(type);
        // nsi.setInnerOverrideSpeakerTemplateID(idd);
        nsi.setText(text);
        nsi.setPrevPossible(false);
        nsi.setNextPossible(true);
        nsi.setDelay(0);
        return (int) (long) sendScriptMessage(text, Say);
//        getClient().announce(NPCPacket.OnSay((byte) 4, idd, false, 0, idd, type, true, false, text, 0));
    }

    public int PlayerToNpc(String text) {
        return sendNextPrevS(text, (byte) 3);
    }

    public int sendNextPrevS(String text, byte type) {
        return sendNextPrevS(text, type, npcId);
    }

    public int sayNextPrevS(String text, byte type) {
        return sendNextPrevS(text, type, npcId);
    }

    public int sendNextPrevS(String text, byte type, int idd) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(idd);
        nsi.setSpeakerType(3);
        nsi.setOverrideSpeakerTemplateID(0);
        nsi.setParam(type);
        // nsi.setInnerOverrideSpeakerTemplateID(idd);
        nsi.setText(text);
        nsi.setPrevPossible(true);
        nsi.setNextPossible(true);
        nsi.setDelay(0);
        return (int) (long) sendScriptMessage(text, Say);
//        getClient().announce(NPCPacket.OnSay((byte) 4, idd, false, 0, idd, type, true, true, text, 0));
    }

    public int sendNextPrevN(String text) {
        return this.sendNextPrevS(text, NpcMessageType.Say.getVal());
    }

    public int sayNextPrevN(String text) {
        return this.sendNextPrevS(text, NpcMessageType.Say.getVal());
    }

    public int sendNextPrevN(String text, byte type) {
        return this.sendNextPrevN(text, type, this.getNpcId());
    }

    public int sayNextPrevN(String text, byte type) {
        return this.sendNextPrevN(text, type, this.getNpcId());
    }

    public int sendNextPrevN(String text, byte type, int idd) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(idd);
        nsi.setSpeakerType(4);
        nsi.setOverrideSpeakerTemplateID(0);
        nsi.setParam(type);
        // nsi.setInnerOverrideSpeakerTemplateID(idd);
        nsi.setText(text);
        nsi.setPrevPossible(true);
        nsi.setNextPossible(true);
        nsi.setDelay(0);
        return (int) (long) sendScriptMessage(text, Say);
//        getClient().announce(NPCPacket.OnSay((byte) 4, idd, false, 0, idd, type, true, true, text, 0));
    }

    public int sendAcceptDecline(String text) {
        return askAccept(text);
    }

    public int sayAcceptDecline(String text) {
        return askAccept(text);
    }

    public int sendAcceptDeclineNoESC(String text) {
        return askAcceptNoESC(text);
    }

    public int sayAcceptDeclineNoESC(String text) {
        return askAcceptNoESC(text);
    }

    public int askAcceptDecline(String text) {
        return askAcceptDecline(text, npcId);
    }

    public int askAcceptDecline(String text, int id) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(id);
        nsi.setSpeakerType(4);
        nsi.setParam(0);
        // nsi.setInnerOverrideSpeakerTemplateID(0);
        nsi.setText(text);

        return (int) (long) sendScriptMessage(text, AskAccept);

//        getClient().announce(NPCPacket.OnAskAccept((byte) 4, id, 0, (short) 0, text));
    }

    public int askAcceptDeclineNoESC(String text) {
        return askAcceptDeclineNoESC(text, npcId);
    }

    public int askAcceptDeclineNoESC(String text, int id) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(id);
        nsi.setSpeakerType(4);
        nsi.setParam(0x01);
        // nsi.setInnerOverrideSpeakerTemplateID(0);
        nsi.setText(text);

        return (int) (long) sendScriptMessage(text, AskAccept);
//        getClient().announce(NPCPacket.OnAskAccept((byte) 4, id, 0, (short) 0x01, text));
    }

    public int askMapSelection(String sel) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setTemplateID(npcId);
        nsi.setSpeakerType(4);
        nsi.setParam(0);
        // nsi.setInnerOverrideSpeakerTemplateID(0);
        nsi.setText(sel);
        nsi.setDefaultText("");
        nsi.setCol((short) (npcId == 3000012 ? 5 : npcId == 9010000 ? 3 : npcId == 2083006 ? 1 : 0));
        nsi.setLine((short) (npcId == 9010022 ? 1 : 0));
        return (int) (long) sendScriptMessage(sel, AskBoxtext);
//        getClient().announce(NPCPacket.OnAskBoxText((byte) 4, npcId, 0, (short) 0, (short) (npcId == 3000012 ? 5 : npcId == 9010000 ? 3 : npcId == 2083006 ? 1 : 0), (short) (npcId == 9010022 ? 1 : 0), sel, ""));
    }

    public int sendSimple(String text) {
        return askMenu(text);
    }

    public int sendSimple(String text, int id) {
        return askMenu(text, id);
    }

    public int sendSimpleS(String text, byte type) {
        return sendSimpleS(text, type, npcId);
    }

    public int sendSimpleS(String text, byte type, int idd) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(idd);
        nsi.setSpeakerType(3);
        nsi.setParam(type);
//        // nsi.setInnerOverrideSpeakerTemplateID(diffnpc);
        nsi.setText(text);
        return (int) (long) sendScriptMessage(text, AskMenu);
    }

    public int sendSimpleN(String text) {
        return this.sendSimpleN(text, NpcMessageType.AskMenuDualIllustration.getVal(), this.getNpcId());
    }

    public int sendSimpleN(String text, byte type, int idd) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(idd);
        nsi.setSpeakerType(4);
        nsi.setParam(type);
//        // nsi.setInnerOverrideSpeakerTemplateID(diffnpc);
        nsi.setText(text);
        return (int) (long) sendScriptMessage(text, AskMenu);
    }

    public int askAvatar(String text, int[] styles, int card, boolean isSecond) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setTemplateID(npcId);
        nsi.setSpeakerType(4);
        nsi.setParam(0);
        nsi.setColor(0);
        nsi.setSecondLookValue(isSecond ? 1 : 0);
        nsi.setText(text);
        nsi.setOptions(styles);
        nsi.setItemID(card);
        nsi.setSrcBeauty(0);
        return (int) (long) sendScriptMessage(text, AskAvatar);
//        getClient().announce(NPCPacket.OnAskAvatar((byte) 4, npcId, card, isSecond, false, styles, text));
    }

    public int sendStyle(String text, int[] styles, int card, boolean isSecond) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setTemplateID(npcId);
        nsi.setSpeakerType(4);
        nsi.setParam(0);
        nsi.setColor(0);
        nsi.setText(text);
        nsi.setOptions(styles);
        nsi.setItemID(card);
        nsi.setSrcBeauty(0);
        return (int) (long) sendScriptMessage(text, AskAndroid);

//        getClient().announce(NPCPacket.OnAskAndroid((byte) 4, npcId, card, styles, text));
    }

    public int sendAStyle(String text, int[] styles, int card) {
        return askAndroid(text, styles, card);
//        getClient().announce(NPCPacket.getNPCTalkStyle(npcId, text, styles, card, true, false));
    }

    public int sendGetNumber(String text, long def, long min, long max) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(npcId);
        nsi.setSpeakerType(4);
        nsi.setParam(0);
        nsi.setText(text);
        nsi.setDefaultNumber(def);
        nsi.setMin(min);
        nsi.setMax(max);
        return (int) (long) sendScriptMessage(text, AskNumber);
//        getClient().announce(NPCPacket.OnAskNumber((byte) 4, npcId, (short) 0, def, min, max, text));
    }

    public String sendGetText(String text) {
        return sendGetText(text, npcId);
    }

    public String sendGetText(String text, int id) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(id);
        nsi.setSpeakerType(4);
        nsi.setParam(0);
        // nsi.setInnerOverrideSpeakerTemplateID(0);
        nsi.setText(text);
        nsi.setDefaultText("");
        nsi.setMin(0);
        nsi.setMax(0);
        return (String) sendScriptMessage(text, AskText);
//        getClient().announce(NPCPacket.OnAskText((byte) 4, id, 0, (short) 0, (short) 0, (short) 0, text, ""));
    }

    public int sendPlayerOk(String text) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(npcId);
        nsi.setSpeakerType(3);
        nsi.setOverrideSpeakerTemplateID(0);
        nsi.setParam(ScriptParam.PlayerAsSpeakerFlip.getValue());
        // nsi.setInnerOverrideSpeakerTemplateID(0);
        nsi.setText(text);
        nsi.setPrevPossible(false);
        nsi.setNextPossible(false);
        nsi.setDelay(0);
        return (int) (long) sendScriptMessage(text, Say);
//        getClient().announce(NPCPacket.OnSay((byte) 3, npcId, false, 0, 0, ScriptParam.PlayerAsSpeakerFlip.getValue(), false, false, text, 0));
    }

    public int sendPlayerOk(String text, byte type, int npcId) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(npcId);
        nsi.setSpeakerType(3);
        nsi.setOverrideSpeakerTemplateID(0);
        nsi.setParam(type);
        // nsi.setInnerOverrideSpeakerTemplateID(npcId);
        nsi.setText(text);
        nsi.setPrevPossible(false);
        nsi.setNextPossible(false);
        nsi.setDelay(0);
        return (int) (long) sendScriptMessage(text, Say);
//        getClient().announce(NPCPacket.OnSay((byte) 3, npcId, false, 0, npcId, type, false, false, text, 0));
    }

    public int sendPlayerPrev(String text, byte type, int npcId) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(npcId);
        nsi.setSpeakerType(3);
        nsi.setOverrideSpeakerTemplateID(0);
        nsi.setParam(type);
        // nsi.setInnerOverrideSpeakerTemplateID(npcId);
        nsi.setText(text);
        nsi.setPrevPossible(true);
        nsi.setNextPossible(false);
        nsi.setDelay(0);
        return (int) (long) sendScriptMessage(text, Say);
//        getClient().announce(NPCPacket.OnSay((byte) 3, npcId, false, 0, npcId, type, true, false, text, 0));
    }

    public int sendPlayerNext(String text) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(npcId);
        nsi.setSpeakerType(3);
        nsi.setOverrideSpeakerTemplateID(0);
        nsi.setParam((short) (ScriptParam.PlayerAsSpeaker.getValue() | ScriptParam.PlayerAsSpeakerFlip.getValue()));
        // nsi.setInnerOverrideSpeakerTemplateID(0);
        nsi.setText(text);
        nsi.setPrevPossible(false);
        nsi.setNextPossible(true);
        nsi.setDelay(0);
        return (int) (long) sendScriptMessage(text, Say);
//        getClient().announce(NPCPacket.OnSay((byte) 3, npcId, false, 0, 0, (short) (ScriptParam.PlayerAsSpeaker.getValue() | ScriptParam.PlayerAsSpeakerFlip.getValue()), false, true, text, 0));
    }

    public int sendPlayerNext(String text, byte type, int npcId) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(npcId);
        nsi.setSpeakerType(3);
        nsi.setOverrideSpeakerTemplateID(0);
        nsi.setParam(type);
        // nsi.setInnerOverrideSpeakerTemplateID(npcId);
        nsi.setText(text);
        nsi.setPrevPossible(false);
        nsi.setNextPossible(true);
        nsi.setDelay(0);
        return (int) (long) sendScriptMessage(text, Say);
//        getClient().announce(NPCPacket.OnSay((byte) 3, npcId, false, 0, npcId, type, false, true, text, 0));
    }

    public int sendPlayerNextPrev(String text) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(npcId);
        nsi.setSpeakerType(3);
        nsi.setOverrideSpeakerTemplateID(0);
        nsi.setParam((short) (ScriptParam.PlayerAsSpeaker.getValue() | ScriptParam.PlayerAsSpeakerFlip.getValue()));
        // nsi.setInnerOverrideSpeakerTemplateID(0);
        nsi.setText(text);
        nsi.setPrevPossible(true);
        nsi.setNextPossible(true);
        nsi.setDelay(0);
        return (int) (long) sendScriptMessage(text, Say);
//        getClient().announce(NPCPacket.OnSay((byte) 3, npcId, false, 0, 0, (short) (ScriptParam.PlayerAsSpeaker.getValue() | ScriptParam.PlayerAsSpeakerFlip.getValue()), true, true, text, 0));
    }

    public int sendPlayerNextPrev(String text, byte type, int npcId) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(npcId);
        nsi.setSpeakerType(3);
        nsi.setOverrideSpeakerTemplateID(0);
        nsi.setParam(type);
        // nsi.setInnerOverrideSpeakerTemplateID(npcId);
        nsi.setText(text);
        nsi.setPrevPossible(true);
        nsi.setNextPossible(true);
        nsi.setDelay(0);
        return (int) (long) sendScriptMessage(text, Say);
//        getClient().announce(NPCPacket.OnSay((byte) 3, npcId, false, 0, npcId, type, true, true, text, 0));
    }

    /*
     * 復活寵物選擇對話框
     * 未知正式啟用
     * 1個寵物 寵物位置 9
     * Recv NPC_TALK [02B9] (65)
     * B9 02
     * 04
     * A6 BF 0F 00
     * 0C 00
     * 2C 00 C4 E3 CF EB C8 C3 C4 C4 D2 BB B8 F6 B3 E8 CE EF B8 B4 BB EE C4 D8 A3 BF C7 EB D1 A1 D4 F1 CF EB B8 B4 BB EE B5 C4 B3 E8 CE EF A1 AD
     * 01
     * 11 6E 3B 00 00 00 00 00 - 寵物的唯一ID
     * 09 寵物在背包的位置
     * ?.....,.你想讓哪一個寵物復活呢？請選擇想復活的寵物…..n;......
     */
    public int sendRevivePet(String text) {
        return askPetRevive(text);
    }

    public int sendPlayerStart(String text) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(npcId);
        nsi.setSpeakerType(3);
        nsi.setParam(ScriptParam.PlayerAsSpeakerFlip.getValue());
        // nsi.setInnerOverrideSpeakerTemplateID(0);
        nsi.setText(text);
        return (int) (long) sendScriptMessage(text, AskAccept);

//        getClient().announce(NPCPacket.OnAskAccept((byte) 3, npcId, 0, ScriptParam.PlayerAsSpeakerFlip.getValue(), text));
    }

    public int sendSlideMenu(final int type, final String sel) {
        String[] arrstring = sel.split("#");
        if (arrstring.length < 3) {
            return -1;
        }
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setTemplateID(npcId);
        nsi.setSpeakerType(NpcMessageType.AskText.getVal());
        nsi.setParam(0);
        nsi.setColor(0);
        nsi.setDlgType(type);
        nsi.setDefaultSelect(Integer.valueOf(arrstring[arrstring.length - 2]));
        nsi.setText(sel);
        return (int) (long) sendScriptMessage(sel, AskSlideMenu);

//        getClient().announce(NPCPacket.OnAskSlideMenu(NpcMessageType.AskText.getVal(), npcId, type, Integer.valueOf(arrstring[arrstring.length - 2]), sel));
    }

    public String getSlideMenuSelection(int type) {
        switch (type) {
            case 1:
                return MapleSlideMenu.SlideMenu1.getSelectionInfo(getPlayer(), npcId);
            case 2:
                return MapleSlideMenu.SlideMenu2.getSelectionInfo(getPlayer(), npcId);
            case 3:
                return MapleSlideMenu.SlideMenu3.getSelectionInfo(getPlayer(), npcId);
            case 4:
                return MapleSlideMenu.SlideMenu4.getSelectionInfo(getPlayer(), npcId);
            case 5:
                return MapleSlideMenu.SlideMenu5.getSelectionInfo(getPlayer(), npcId);
            case 6:
                return MapleSlideMenu.SlideMenu6.getSelectionInfo(getPlayer(), npcId);
            case 0:
            default:
                return MapleSlideMenu.SlideMenu0.getSelectionInfo(getPlayer(), npcId);
        }
    }

    public int[] getSlideMenuDataIntegers(int type, int selection) {
        switch (type) {
            case 1:
                return MapleSlideMenu.SlideMenu1.getDataIntegers(selection);
            case 2:
                return MapleSlideMenu.SlideMenu2.getDataIntegers(selection);
            case 3:
                return MapleSlideMenu.SlideMenu3.getDataIntegers(selection);
            case 4:
                return MapleSlideMenu.SlideMenu4.getDataIntegers(selection);
            case 5:
                return MapleSlideMenu.SlideMenu5.getDataIntegers(selection);
            case 6:
                return MapleSlideMenu.SlideMenu6.getDataIntegers(selection);
            case 0:
            default:
                return MapleSlideMenu.SlideMenu0.getDataIntegers(selection);
        }
    }

    public int sendOk(final String s) {
        return this.sendOk(s, npcId, ScriptParam.Normal, true);
    }

    public int say(final String s) {
        return this.sendOk(s, npcId, ScriptParam.Normal, true);
    }

    public int sendOk(final String s, final int n) {
        return this.sendOk(s, n, ScriptParam.Normal, true);
    }

    public int say(final String s, final int n) {
        return this.sendOk(s, n, ScriptParam.Normal, true);
    }

    public int sendOk(final String message, final boolean bLeft) {
        return this.sendOk(message, this.getNpcId(), ScriptParam.Normal, bLeft);
    }

    public int say(final String message, final boolean bLeft) {
        return this.sendOk(message, this.getNpcId(), ScriptParam.Normal, bLeft);
    }

    public int sendOkNoESC(final String message) {
        return sendOkNoESC(message, true);
    }

    public int sayNoESC(final String message) {
        return sendOkNoESC(message, true);
    }

    public int sendOkNoESC(final String message, final boolean bLeft) {
        return this.sendOk(message, this.getNpcId(), ScriptParam.NoEsc, bLeft);
    }

    public int sayNoESC(final String message, final boolean bLeft) {
        return this.sendOk(message, this.getNpcId(), ScriptParam.NoEsc, bLeft);
    }

    public int sendOkS(final String s) {
        return this.sendOkS(s, ScriptParam.Normal, true);
    }

    public int sayS(final String s) {
        return this.sendOkS(s, ScriptParam.Normal, true);
    }

    public int sendOkS(final String s, final boolean b) {
        return this.sendOkS(s, ScriptParam.Normal, b);
    }

    public int sayS(final String s, final boolean b) {
        return this.sendOkS(s, ScriptParam.Normal, b);
    }

    public int sendOkE(final String s) {
        return this.sendOkE(s, 0, ScriptParam.Normal, false, 0);
    }

    public int sayE(final String s) {
        return this.sendOkE(s, 0, ScriptParam.Normal, false, 0);
    }

    public int sendOkE(final String s, final int n) {
        return this.sendOkE(s, n, ScriptParam.Normal, n < 0, 0);
    }

    public int sayE(final String s, final int n) {
        return this.sendOkE(s, n, ScriptParam.Normal, n < 0, 0);
    }

    public int sendOkENoESC(final String s) {
        return this.sendOkE(s, 0, ScriptParam.NoEsc, false, 0);
    }

    public int sayENoESC(final String s) {
        return this.sendOkE(s, 0, ScriptParam.NoEsc, false, 0);
    }

    public int sendOkENoESC(final String s, final int n) {
        return this.sendOkE(s, n, ScriptParam.NoEsc, n < 0, 0);
    }

    public int sayENoESC(final String s, final int n) {
        return this.sendOkE(s, n, ScriptParam.NoEsc, n < 0, 0);
    }

    public int sayENoESC(final String s, final int n, final int n2) {
        return this.sendOkE(s, n, ScriptParam.NoEsc, n < 0, n2);
    }

    public int sendNext(final String s) {
        return this.sendNext(s, 0, ScriptParam.Normal, true);
    }

    public int sayNext(final String s) {
        return this.sendNext(s, 0, ScriptParam.Normal, true);
    }

    public int sendNext(final String s, final int n) {
        return this.sendNext(s, n, ScriptParam.Normal, false);
    }

    public int sayNext(final String s, final int n) {
        return this.sendNext(s, n, ScriptParam.Normal, false);
    }

    public int sendNext(final String s, final boolean b) {
        return this.sendNext(s, this.getNpcId(), ScriptParam.Normal, b);
    }

    public int sayNext(final String s, final boolean b) {
        return this.sendNext(s, this.getNpcId(), ScriptParam.Normal, b);
    }

    public int sendNextNoESC(final String s) {
        return this.sendNext(s, this.getNpcId(), ScriptParam.NoEsc, true);
    }

    public int sayNextNoESC(final String s) {
        return this.sendNext(s, this.getNpcId(), ScriptParam.NoEsc, true);
    }

    public int sendNextNoESC(final String s, final boolean b) {
        return this.sendNext(s, this.getNpcId(), ScriptParam.NoEsc, b);
    }

    public int sayNextNoESC(final String s, final boolean b) {
        return this.sendNext(s, this.getNpcId(), ScriptParam.NoEsc, b);
    }

    public int sendNextNoESC(final String s, final int n) {
        return this.sendNext(s, n, ScriptParam.NoEsc, false);
    }

    public int sayNextNoESC(final String s, final int n) {
        return this.sendNext(s, n, ScriptParam.NoEsc, false);
    }

    public int sendNextS(final String s) {
        return this.sendNextS(s, ScriptParam.Normal, true);
    }

    public int sayNextS(final String s) {
        return this.sendNextS(s, ScriptParam.Normal, true);
    }

    public int sendNextS(final String s, final boolean b) {
        return this.sendNextS(s, ScriptParam.Normal, b);
    }

    public int sayNextS(final String s, final boolean b) {
        return this.sendNextS(s, ScriptParam.Normal, b);
    }

    public int sendNextSNoESC(final String s) {
        return this.sendNextS(s, ScriptParam.NoEsc, true);
    }

    public int sayNextSNoESC(final String s) {
        return this.sendNextS(s, ScriptParam.NoEsc, true);
    }

    public int sendNextE(final String s) {
        return this.sendNextE(s, 0, ScriptParam.Normal, false, 0);
    }

    public int sayNextE(final String s) {
        return this.sendNextE(s, 0, ScriptParam.Normal, false, 0);
    }

    public int sendNextE(final String s, final int n) {
        return this.sendNextE(s, n, ScriptParam.Normal, n < 0, 0);
    }

    public int sayNextE(final String s, final int n) {
        return this.sendNextE(s, n, ScriptParam.Normal, n < 0, 0);
    }

    public int sendNextENoESC(final String s) {
        return this.sendNextE(s, 0, ScriptParam.NoEsc, false, 0);
    }

    public int sayNextENoESC(final String s) {
        return this.sendNextE(s, 0, ScriptParam.NoEsc, false, 0);
    }

    public int sendNextENoESC(final String s, final int n) {
        return this.sendNextE(s, n, ScriptParam.NoEsc, n < 0, 0);
    }

    public int sayNextENoESC(final String s, final int n) {
        return this.sendNextE(s, n, ScriptParam.NoEsc, n < 0, 0);
    }

    public int sendNextENoESC(final String s, final int n, final int n2) {
        return this.sendNextE(s, n, ScriptParam.NoEsc, n < 0, n2);
    }

    public int sayNextENoESC(final String s, final int n, final int n2) {
        return this.sendNextE(s, n, ScriptParam.NoEsc, n < 0, n2);
    }

    public int sendPrev(final String s) {
        return this.sendPrev(s, 0, ScriptParam.Normal, true);
    }

    public int sayPrev(final String s) {
        return this.sendPrev(s, 0, ScriptParam.Normal, true);
    }

    public int sendPrevS(final String s) {
        return this.sendPrevS(s, ScriptParam.Normal, true);
    }

    public int sayPrevS(final String s) {
        return this.sendPrevS(s, ScriptParam.Normal, true);
    }

    public int sendPrevE(final String s) {
        return this.sendPrevE(s, 0, ScriptParam.Normal, false, 0);
    }

    public int sayPrevE(final String s) {
        return this.sendPrevE(s, 0, ScriptParam.Normal, false, 0);
    }

    public int sendPrevE(final String s, final int n) {
        return this.sendPrevE(s, n, ScriptParam.Normal, n < 0, 0);
    }

    public int sayPrevE(final String s, final int n) {
        return this.sendPrevE(s, n, ScriptParam.Normal, n < 0, 0);
    }

    public int sendPrevENoESC(final String s) {
        return this.sendPrevE(s, 0, ScriptParam.NoEsc, false, 0);
    }

    public int sayPrevENoESC(final String s) {
        return this.sendPrevE(s, 0, ScriptParam.NoEsc, false, 0);
    }

    public int sendPrevENoESC(final String s, final int n) {
        return this.sendPrevE(s, n, ScriptParam.NoEsc, n < 0, 0);
    }

    public int sayPrevENoESC(final String s, final int n) {
        return this.sendPrevE(s, n, ScriptParam.NoEsc, n < 0, 0);
    }

    public int sendPrevENoESC(final String s, final int n, final int n2) {
        return this.sendPrevE(s, n, ScriptParam.NoEsc, n < 0, n2);
    }

    public int sayPrevENoESC(final String s, final int n, final int n2) {
        return this.sendPrevE(s, n, ScriptParam.NoEsc, n < 0, n2);
    }

    public int sendNextPrev(final String s) {
        return this.sendNextPrev(s, 0, ScriptParam.Normal, true);
    }

    public int sayNextPrev(final String s) {
        return this.sendNextPrev(s, 0, ScriptParam.Normal, true);
    }

    public int sendNextPrev(final String s, final boolean b) {
        return this.sendNextPrev(s, this.getNpcId(), ScriptParam.Normal, b);
    }

    public int sayNextPrev(final String s, final boolean b) {
        return this.sendNextPrev(s, this.getNpcId(), ScriptParam.Normal, b);
    }

    public int sendNextPrev(final String s, final int n) {
        return this.sendNextPrev(s, n, ScriptParam.Normal, false);
    }

    public int sayNextPrev(final String s, final int n) {
        return this.sendNextPrev(s, n, ScriptParam.Normal, false);
    }

    public int sendNextPrevNoESC(final String s) {
        return this.sendNextPrevNoESC(s, true);
    }

    public int sayNextPrevNoESC(final String s) {
        return this.sendNextPrevNoESC(s, true);
    }

    public int sendNextPrevNoESC(final String s, final boolean b) {
        return this.sendNextPrev(s, this.getNpcId(), ScriptParam.NoEsc, b);
    }

    public int sayNextPrevNoESC(final String s, final boolean b) {
        return this.sendNextPrev(s, this.getNpcId(), ScriptParam.NoEsc, b);
    }

    public int sendNextPrevNoESC(final String s, final int n) {
        return this.sendNextPrev(s, n, ScriptParam.NoEsc, false);
    }

    public int sayNextPrevNoESC(final String s, final int n) {
        return this.sendNextPrev(s, n, ScriptParam.NoEsc, false);
    }

    public int sendNextPrevS(final String s) {
        return this.sendNextPrevS(s, ScriptParam.Normal, true);
    }

    public int sayNextPrevS(final String s) {
        return this.sendNextPrevS(s, ScriptParam.Normal, true);
    }

    public int sendNextPrevS(final String s, final boolean b) {
        return this.sendNextPrevS(s, ScriptParam.Normal, b);
    }

    public int sayNextPrevS(final String s, final boolean b) {
        return this.sendNextPrevS(s, ScriptParam.Normal, b);
    }

    public int sendNextPrevSNoESC(final String s) {
        return this.sendNextPrevS(s, ScriptParam.NoEsc, true);
    }

    public int sayNextPrevSNoESC(final String s) {
        return this.sendNextPrevS(s, ScriptParam.NoEsc, true);
    }

    public int sendNextPrevSNoESC(final String s, final boolean b) {
        return this.sendNextPrevS(s, ScriptParam.NoEsc, b);
    }

    public int sayNextPrevSNoESC(final String s, final boolean b) {
        return this.sendNextPrevS(s, ScriptParam.NoEsc, b);
    }

    public int sendNextPrevE(final String s) {
        return this.sendNextPrevE(s, 0, ScriptParam.Normal, false, 0);
    }

    public int sayNextPrevE(final String s) {
        return this.sendNextPrevE(s, 0, ScriptParam.Normal, false, 0);
    }

    public int sendNextPrevE(final String s, final int n) {
        return this.sendNextPrevE(s, n, ScriptParam.Normal, n < 0, 0);
    }

    public int sayNextPrevE(final String s, final int n) {
        return this.sendNextPrevE(s, n, ScriptParam.Normal, n < 0, 0);
    }

    public int sendNextPrevENoESC(final String s) {
        return this.sendNextPrevE(s, 0, ScriptParam.NoEsc, false, 0);
    }

    public int sayNextPrevENoESC(final String s) {
        return this.sendNextPrevE(s, 0, ScriptParam.NoEsc, false, 0);
    }

    public int sendNextPrevENoESC(final String s, final int n) {
        return this.sendNextPrevE(s, n, ScriptParam.NoEsc, n < 0, 0);
    }

    public int sayNextPrevENoESC(final String s, final int n) {
        return this.sendNextPrevE(s, n, ScriptParam.NoEsc, n < 0, 0);
    }

    public int sendNextPrevENoESC(final String s, final int n, final int n2) {
        return this.sendNextPrevE(s, n, ScriptParam.NoEsc, n < 0, n2);
    }

    public int sayNextPrevENoESC(final String s, final int n, final int n2) {
        return this.sendNextPrevE(s, n, ScriptParam.NoEsc, n < 0, n2);
    }

    public int askReplace(final String s) {
        return this.askYesNo(s, this.getNpcId(), ScriptParam.Replace, true);
    }

    public int askYesNo(final String s) {
        return this.askYesNo(s, true);
    }

    public int askYesNo(final String s, final int n) {
        return this.askYesNo(s, n, ScriptParam.Normal, false);
    }

    public int askYesNo(final String s, final boolean b) {
        return this.askYesNo(s, this.getNpcId(), ScriptParam.Normal, b);
    }

    public int askYesNoNoESC(final String s) {
        return this.askYesNo(s, this.getNpcId(), ScriptParam.NoEsc, true);
    }

    public int askYesNoNoESC(final String s, final boolean b) {
        return this.askYesNo(s, this.getNpcId(), ScriptParam.NoEsc, b);
    }

    public int askYesNoS(final String s) {
        return this.askYesNoS(s, ScriptParam.Normal, true);
    }

    public int askYesNoS(final String s, final boolean b) {
        return this.askYesNoS(s, ScriptParam.Normal, b);
    }

    public int askYesNoE(final String s) {
        return this.askYesNoE(s, 0, ScriptParam.Normal, false);
    }

    public int askYesNoE(final String s, final int n) {
        return this.askYesNoE(s, n, ScriptParam.Normal, n < 0);
    }

    public int askMenu(final String s) {
        return this.askMenu(s, npcId, ScriptParam.Normal, false);
    }

    public int askMenu(final String s, final int n) {
        return this.askMenu(s, n, ScriptParam.Normal, false);
    }

    public int askMenu(final String s, final boolean b) {
        return this.askMenu(s, npcId, ScriptParam.Normal, b);
    }

    public int askMenuNoESC(final String s) {
        return this.askMenu(s, this.getNpcId(), ScriptParam.NoEsc, true);
    }

    public int askMenuNoESC(final String s, final boolean b) {
        return this.askMenu(s, this.getNpcId(), ScriptParam.NoEsc, b);
    }

    public int askMenuNoESC(final String s, final int n) {
        return this.askMenu(s, n, ScriptParam.NoEsc, false);
    }

    public int askMenuS(final String s) {
        return this.askMenuS(s, ScriptParam.Normal, true);
    }

    public int askMenuE(final String s) {
        return this.askMenuE(s, false);
    }

    public int askMenuE(final String s, final boolean b) {
        return this.askMenuE(s, 0, ScriptParam.Normal, b);
    }

    public int askMenuA(final String s) {
        return this.askMenuA(s, false);
    }

    public int askMenuA(final String s, final boolean b) {
        return this.askMenuA(s, 0, ScriptParam.Normal, b);
    }

    public int askMenuA(final String msg, final int diffnpc) {
        return this.askMenuA(msg, diffnpc, ScriptParam.Normal, false);
    }

    public int askAccept(final String s) {
        return this.askAccept(s, 0, ScriptParam.Normal, true);
    }

    public int askAccept(String msg, int diffNpcID) {
        return this.askAccept(msg, diffNpcID, ScriptParam.Normal, true);
    }

    public int askAccept(final String s, final boolean bLeft) {
        return this.askAccept(s, this.getNpcId(), ScriptParam.Normal, bLeft);
    }

    public int askAcceptNoESC(final String s) {
        return askAccept(s, this.getNpcId(), ScriptParam.NoEsc, true);
    }

    public int askAcceptNoESC(final String s, final boolean b) {
        return this.askAccept(s, this.getNpcId(), ScriptParam.NoEsc, b);
    }

    public int askAcceptS(final String s) {
        return this.askAcceptS(s, ScriptParam.Normal, true);
    }

    public int askAcceptE(final String s) {
        return this.askAcceptE(s, 0, ScriptParam.Normal, false);
    }

    public void askText(final String s, final short n, final short n2) {
        askText(s, "", n, n2);
    }

//    public void askText(final String s, final String def, final short n, final short n2) {
//              return this.askText(s, this.getNpcId(), ScriptParam.Normal, n, n2, true, def);
//    }

    public void askTextNoESC(final String s, final short n, final short n2) {
        askTextNoESC(s, "", n, n2);
    }

    public String askTextNoESC(final String s, final String def, final short n, final short n2) {
        return this.askText(s, this.getNpcId(), ScriptParam.NoEsc, n, n2, true, def);
    }

    public String askTextS(final String s, final short n, final short n2) {
        return askTextS(s, "", n, n2);
    }

    public String askTextS(final String s, final String def, final short n, final short n2) {
        return this.askTextS(s, n, n2, true, def);
    }

    public String askTextE(final String s, final short n, final short n2) {
        return askTextE(s, "", n, n2);
    }

    public String askTextE(final String s, final String def, final short n, final short n2) {
        return this.askTextE(s, n, n2, false, def);
    }

    public int askNumber(final String s, final int n, final int n2, final int n3) {
        return this.askNumber(s, 0, n, n2, n3, true);
    }

    public int askNumberKeypad(final int n) {
        return this.askNumberKeypad((byte) 4, this.getNpcId(), 0, ScriptParam.Normal.getValue(), n);
    }

    public int askUserSurvey(final int n, final String s) {
        return this.askUserSurvey((byte) 4, this.getNpcId(), 0, ScriptParam.Normal.getValue(), n, s);
    }

    public int askNumberS(final String s, final int n, final int n2, final int n3) {
        return this.askNumberS(s, n, n2, n3, true);
    }

    public int askNumberE(final String s, final int n, final int n2, final int n3) {
        return this.askNumberE(s, n, n2, n3, false);
    }

    public String askBoxText(final String s, final String s2, final short n, final short n2) {
        return this.askBoxText(s, s2, 0, n, n2, true);
    }

    public String askBoxTextS(final String s, final String s2, final short n, final short n2) {
        return this.askBoxTextS(s, s2, n, n2, true);
    }

    public String askBoxTextE(final String s, final String s2, final short n, final short n2) {
        return this.askBoxTextE(s, s2, n, n2, false);
    }

    public int askSlideMenu(final int n, final String s) {
        final String[] split;
        if ((split = s.split("#")).length < 3) {
            return -1;
        }
        return this.askSlideMenu((byte) 4, this.getNpcId(), n, Integer.valueOf(split[split.length - 2]), s);
    }

    public int askAvatar(final String message, final int[] array, final int needItem, final boolean isangel, final boolean isbeta) {
        return this.askAvatar((byte) 4, this.getNpcId(), needItem, isangel, isbeta, array, message);
    }

    public int askAndroid(final String s, final int[] array, final int n) {
        return this.askAndroid((byte) 4, this.getNpcId(), n, array, s);
    }

    public int askPetRevive(final String s) {
        final ArrayList<Item> list = new ArrayList<>();
        for (Item pet : this.getAllPetItem()) {
            if (pet.getExpiration() > 0L && pet.getExpiration() < System.currentTimeMillis()) {
                list.add(pet);
            }
        }
        if (list.isEmpty()) {
            return sendOk("你沒有失去魔法的寵物.");
        }
        return this.askPet(s, list);
    }

    public List<Item> getAllPetItem() {
        final ArrayList<Item> list = new ArrayList<Item>();
        for (Item item : this.getPlayer().getInventory(MapleInventoryType.CASH).getInventory().values()) {
            if (ItemConstants.類型.寵物(item.getItemId())) {
                list.add(item);
            }
        }
        return list;
    }

//    public void askPet(final String s, final List<Item> list) {
//              return this.askPet((byte) 4, this.getNpcId(), list, s);
//    }

    public int askSelectMenu(final int n) {
        return this.askSelectMenu((byte) 3, n, 1, null);
    }

    public int askSelectMenu(final int n, final int n2, final String[] array) {
        return this.askSelectMenu((byte) 4, n, n2, array);
    }

    public int askPetEvolution(final String s, final List<Item> list) {
        return this.askPetEvolution((byte) 4, this.getNpcId(), list, s);
    }

    public int askPetAll(final String s) {
        return this.askPetAll(s, this.getAllPetItem());
    }

    public int askPetAll(final String s, final List<Item> list) {
        return this.askPetAll((byte) 4, this.getNpcId(), list, s);
    }

    public int sayImage(final String[] array) {
        return this.sayImage((byte) 4, 3, 0, (byte) 3, array);
    }

//    public void chrSayYesNo(final String[] array) {
//        MaplePacketLittleEndianWriter talk = new MaplePacketLittleEndianWriter();
//        talk.writeShort(array.length);
//        talk.writeHexString("");
//        talk.writeAsciiString(Arrays.toString(array));
//        Objects.requireNonNull(client.get()).getPlayer().send(talk.getPacket());
//    }

    public String askQuiz(final boolean b, final int n, final int n2, final String s, final String s2, final String s3) {
        return this.askQuiz((byte) 0, this.getNpcId(), 0, 0, b, n, n2, s, s2, s3);
    }

    public String askSpeedQuiz(final boolean b, final int n, final int n2, final int n3, final int n4, final int n5) {
        return this.askSpeedQuiz((byte) 0, this.getNpcId(), 0, 0, b, n, n2, n3, n4, n5);
    }

    public String askICQuiz(final boolean b, final String s, final String s2, final int n) {
        return this.askICQuiz((byte) 0, this.getNpcId(), 0, 0, b, s, s2, n);
    }

    public String askOlympicQuiz(final boolean b, final int n, final int n2, final int n3, final int n4, final int n5) {
        return this.askOlympicQuiz((byte) 0, this.getNpcId(), 0, 0, b, n, n2, n3, n4, n5);
    }

    public String sendGetText(String text, String def, int col, int line) {
        return askText((byte) 4, npcId, npcId, ScriptParam.OverrideSpeakerID.getValue(), (short) col, (short) line, text, def);
    }

    public int sendOkIllu(final String s, final int n, final int n2, final boolean b) {
        return this.sendOkIllu(s, n, ScriptParam.Normal, true, n, n2, b);
    }

    public int sayIllu(final String s, final int n, final int n2, final boolean b) {
        return this.sendOkIllu(s, n, ScriptParam.Normal, true, n, n2, b);
    }

    public int sendOkIlluNoESC(final String s, final int n, final int n2, final boolean b) {
        return this.sendOkIllu(s, n, ScriptParam.NoEsc, true, n, n2, b);
    }

    public int sayIlluNoESC(final String s, final int n, final int n2, final boolean b) {
        return this.sendOkIllu(s, n, ScriptParam.NoEsc, true, n, n2, b);
    }

    public int sendNextIllu(final String s, final int n, final int n2, final boolean b) {
        return this.sendNextIllu(s, n, ScriptParam.Normal, true, n, n2, b);
    }

    public int sayNextIllu(final String s, final int n, final int n2, final boolean b) {
        return this.sendNextIllu(s, n, ScriptParam.Normal, true, n, n2, b);
    }

    public int sendNextIlluNoESC(final String s, final int n, final int n2, final boolean b) {
        return this.sendNextIllu(s, n, ScriptParam.NoEsc, true, n, n2, b);
    }

    public int sayNextIlluNoESC(final String s, final int n, final int n2, final boolean b) {
        return this.sendNextIllu(s, n, ScriptParam.NoEsc, true, n, n2, b);
    }

    public int sendPrevIllu(final String s, final int n, final int n2, final boolean b) {
        return this.sendPrevIllu(s, n, ScriptParam.Normal, true, n, n2, b);
    }

    public int sayPrevIllu(final String s, final int n, final int n2, final boolean b) {
        return this.sendPrevIllu(s, n, ScriptParam.Normal, true, n, n2, b);
    }

    public int sendPrevIlluNoESC(final String s, final int n, final int n2, final boolean b) {
        return this.sendPrevIllu(s, n, ScriptParam.NoEsc, true, n, n2, b);
    }

    public int sayPrevIlluNoESC(final String s, final int n, final int n2, final boolean b) {
        return this.sendPrevIllu(s, n, ScriptParam.NoEsc, true, n, n2, b);
    }

    public int sendNextPrevIllu(final String s, final int n, final int n2, final boolean b) {
        return this.sendNextPrevIllu(s, n, ScriptParam.Normal, true, n, n2, b);
    }

    public int sayNextPrevIllu(final String s, final int n, final int n2, final boolean b) {
        return this.sendNextPrevIllu(s, n, ScriptParam.Normal, true, n, n2, b);
    }

    public int sendNextPrevIlluNoESC(final String s, final int n, final int n2, final boolean b) {
        return this.sendNextPrevIllu(s, n, ScriptParam.NoEsc, true, n, n2, b);
    }

    public int sayNextPrevIlluNoESC(final String s, final int n, final int n2, final boolean b) {
        return this.sendNextPrevIllu(s, n, ScriptParam.NoEsc, true, n, n2, b);
    }

    //-----------------------------------------------
    // private npc talk impl start
    //-----------------------------------------------

    private int sendSay(final byte type, final int npcId, final int n3, final int u2, final boolean bPrev, final boolean bNext, final String sText, final int n5) {

        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(npcId);
        nsi.setSpeakerType(type);
        nsi.setOverrideSpeakerTemplateID(npcId);
        nsi.setParam((short) u2);
//        // nsi.setInnerOverrideSpeakerTemplateID(npcId);
        nsi.setText(sText);
        nsi.setPrevPossible(bPrev);
        nsi.setNextPossible(bNext);
        nsi.setDelay(0);
        return (int) (long) sendScriptMessage(sText, Say);

//        getClient().announce(NPCPacket.OnSay(type, npcId, false, 0, n3, (short) u2, bPrev, bNext, sText, n5));
    }

    private int sendOk(final String s, final int n, final ScriptParam j906, final boolean bLeft) {
        return this.sendSay((byte) 4, npcId, n, (bLeft ? ScriptParam.Normal.getValue() : ScriptParam.OverrideSpeakerID.getValue()) | j906.getValue(), false, false, s, 0);
    }

    private int sendOkS(final String s, final ScriptParam j906, final boolean b) {
        return this.sendSay((byte) 3, npcId, 0, (b ? ScriptParam.PlayerAsSpeakerFlip.getValue() : ScriptParam.PlayerAsSpeaker.getValue()) | j906.getValue(), false, false, s, 0);
    }

    private int sendOkE(final String msg, final int npcId, final ScriptParam j906, final boolean b, final int n2) {
        return this.sendSay((byte) (b ? 3 : 4), npcId, npcId, (b ? ScriptParam.PlayerAsSpeaker.getValue() : ScriptParam.Normal.getValue()) | ScriptParam.BoxChat.getValue() | j906.getValue(), false, false, msg, n2);
    }

    private int sendNext(final String s, final int n, final ScriptParam j906, final boolean b) {
        return this.sendSay((byte) 4, npcId, n, (b ? ScriptParam.Normal.getValue() : ScriptParam.OverrideSpeakerID.getValue()) | j906.getValue(), false, true, s, 0);
    }

    private int sendNextS(final String s, final ScriptParam j906, final boolean b) {
        return this.sendSay((byte) 3, npcId, 0, (b ? ScriptParam.PlayerAsSpeakerFlip.getValue() : ScriptParam.PlayerAsSpeaker.getValue()) | j906.getValue(), false, true, s, 0);
    }

    private int sendNextE(final String s, final int n, final ScriptParam j906, final boolean b, final int n2) {
        return this.sendSay((byte) (b ? 3 : 4), npcId, n, (b ? ScriptParam.PlayerAsSpeaker.getValue() : ((n > 0) ? ScriptParam.OverrideSpeakerID.getValue() : ScriptParam.Normal.getValue())) | ScriptParam.BoxChat.getValue() | j906.getValue(), false, true, s, n2);
    }

    private int sendPrev(final String s, final int n, final ScriptParam j906, final boolean b) {
        return this.sendSay((byte) 4, npcId, n, (b ? ScriptParam.Normal.getValue() : ScriptParam.OverrideSpeakerID.getValue()) | j906.getValue(), true, false, s, 0);
    }

    private int sendPrevS(final String s, final ScriptParam j906, final boolean b) {
        return this.sendSay((byte) 3, npcId, 0, (b ? ScriptParam.PlayerAsSpeakerFlip.getValue() : ScriptParam.PlayerAsSpeaker.getValue()) | j906.getValue(), true, false, s, 0);
    }

    private int sendPrevE(final String s, final int n, final ScriptParam j906, final boolean b, final int n2) {
        return this.sendSay((byte) (b ? 3 : 4), npcId, n, (b ? ScriptParam.PlayerAsSpeaker.getValue() : ((n > 0) ? ScriptParam.OverrideSpeakerID.getValue() : ScriptParam.Normal.getValue())) | ScriptParam.BoxChat.getValue() | j906.getValue(), true, false, s, n2);
    }

    private int sendNextPrev(final String s, final int n, final ScriptParam j906, final boolean b) {
        return this.sendSay((byte) 4, npcId, n, (b ? ScriptParam.Normal.getValue() : ScriptParam.OverrideSpeakerID.getValue()) | j906.getValue(), true, true, s, 0);
    }

    private int sendNextPrevS(final String s, final ScriptParam j906, final boolean b) {
        return this.sendSay((byte) 3, npcId, 0, (b ? ScriptParam.PlayerAsSpeakerFlip.getValue() : ScriptParam.PlayerAsSpeaker.getValue()) | j906.getValue(), true, true, s, 0);
    }

    private int sendNextPrevE(final String s, final int n, final ScriptParam j906, final boolean b, final int n2) {
        return this.sendSay((byte) (b ? 3 : 4), npcId, n, (b ? ScriptParam.PlayerAsSpeaker.getValue() : ((n > 0) ? ScriptParam.OverrideSpeakerID.getValue() : ScriptParam.Normal.getValue())) | ScriptParam.BoxChat.getValue() | j906.getValue(), true, true, s, n2);
    }

    private int askYesNo(final byte b, final int n, final int n2, final int n3, final String s) {

        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(n3);
        // nsi.setInnerOverrideSpeakerTemplateID(n2);
        nsi.setText(s);
        return (int) (long) sendScriptMessage(s, AskYesNo);

//        getClient().announce(NPCPacket.OnAskYesNo(b, n, n2, (short) n3, s));
    }

    private int askYesNo(final String s, final int n, final ScriptParam j906, final boolean b) {
        return this.askYesNo((byte) 4, npcId, n, (b ? ScriptParam.Normal.getValue() : ScriptParam.OverrideSpeakerID.getValue()) | j906.getValue(), s);
    }

    public int sendYesNo(String text) {
        return sendYesNo(text, npcId);
    }

    public int sendYesNo(String text, int idd) {
        return askYesNo(text, idd);
    }

    public int sendYesNoS(String text, byte type) {
        return sendYesNoS(text, type, npcId);
    }

    public int sendYesNoS(String text, byte type, int idd) {

        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(npcId);
        nsi.setSpeakerType(3);
        nsi.setParam(type);
        // nsi.setInnerOverrideSpeakerTemplateID(idd);
        nsi.setText(text);
        return (int) (long) sendScriptMessage(text, AskYesNo);

    }

    public int sendYesNoN(String text) {
        return this.sendYesNoN(text, this.getNpcId());
    }

    public int sendYesNoN(String text, int idd) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(idd);
        nsi.setSpeakerType(4);
        nsi.setParam((short) (ScriptParam.OverrideSpeakerID.getValue() | ScriptParam.BoxChat.getValue()));
        // nsi.setInnerOverrideSpeakerTemplateID(idd);
        nsi.setText(text);
        return (int) (long) sendScriptMessage(text, AskYesNo);

//        getClient().announce(NPCPacket.OnAskYesNo((byte) 4, idd, idd, (short) (ScriptParam.OverrideSpeakerID.getValue() | ScriptParam.BoxChat.getValue()), text));
    }

    private int askYesNoS(final String s, final ScriptParam j906, final boolean b) {
        return this.askYesNo((byte) 3, npcId, 0, (b ? ScriptParam.PlayerAsSpeakerFlip.getValue() : ScriptParam.PlayerAsSpeaker.getValue()) | j906.getValue(), s);
    }

    private int askYesNoE(final String s, final int n, final ScriptParam j906, final boolean b) {
        return this.askYesNo((byte) (b ? 3 : 4), npcId, n, (b ? ScriptParam.PlayerAsSpeaker.getValue() : ScriptParam.Normal.getValue()) | ScriptParam.BoxChat.getValue() | j906.getValue(), s);
    }

    private int askMenu(final byte b, final int n, final int diffnpc, final int n3, final String s) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(diffnpc > 0 ? diffnpc : n);
        nsi.setSpeakerType(b);
        nsi.setParam(n3);
//        // nsi.setInnerOverrideSpeakerTemplateID(diffnpc);
        nsi.setText(s);
        return (int) (long)sendScriptMessage(s, AskMenu);
    }

    private int askMenu(final String s, final int n, final ScriptParam j906, final boolean b) {
        return this.askMenu((byte) 4, npcId, n, (b ? ScriptParam.Normal.getValue() : ScriptParam.OverrideSpeakerID.getValue()) | j906.getValue(), s);
    }

    private int askMenuS(final String s, final ScriptParam j906, final boolean b) {
        return this.askMenu((byte) 3, npcId, 0, (b ? ScriptParam.PlayerAsSpeakerFlip.getValue() : ScriptParam.PlayerAsSpeaker.getValue()) | j906.getValue(), s);
    }

    private int askMenuE(final String s, final int n, final ScriptParam j906, final boolean b) {
        return this.askMenu((byte) (b ? 3 : 4), npcId, n, (b ? ScriptParam.PlayerAsSpeaker.getValue() : ScriptParam.Normal.getValue()) | ScriptParam.BoxChat.getValue() | j906.getValue(), s);
    }

    private int askMenuA(final String s, final int diffnpc, final ScriptParam j906, final boolean b) {
        return this.askMenu((byte) (b ? 3 : 4), npcId, diffnpc, (b ? ScriptParam.PlayerAsSpeaker.getValue() : ScriptParam.Normal.getValue()) | ScriptParam.LargeBoxChat.getValue() | j906.getValue(), s);
    }

    private int askAccept(final byte b, final int n, final int diffnpc, final int n3, final String s) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(n3);
        // nsi.setInnerOverrideSpeakerTemplateID(diffnpc);
        nsi.setText(s);
        return (int) (long) sendScriptMessage(s, AskAccept);
//        getClient().announce(NPCPacket.OnAskAccept(b, n, diffnpc, (short) n3, s));
    }

    private int askAccept(final String s, final int diffnpc, final ScriptParam j906, final boolean b) {
        return askAccept((byte) 4, npcId, diffnpc, (b ? ScriptParam.Normal.getValue() : ScriptParam.OverrideSpeakerID.getValue()) | j906.getValue(), s);
    }

    private int askAcceptS(final String s, final ScriptParam j906, final boolean b) {
        return askAccept((byte) 3, npcId, 0, (b ? ScriptParam.PlayerAsSpeakerFlip.getValue() : ScriptParam.PlayerAsSpeaker.getValue()) | j906.getValue(), s);
    }

    private int askAcceptE(final String s, final int diffnpc, final ScriptParam j906, final boolean b) {
        return this.askAccept((byte) (b ? 3 : 4), npcId, diffnpc, (b ? ScriptParam.PlayerAsSpeaker.getValue() : ScriptParam.Normal.getValue()) | ScriptParam.BoxChat.getValue() | j906.getValue(), s);
    }

    private String askText(final byte b, final int n, final int n2, final int n3, final short nLenMin, final short nLenMax, final String sMsg, final String sMsgDefault) {

        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam((short) n3);
        // nsi.setInnerOverrideSpeakerTemplateID(n2);
        nsi.setText(sMsg);
        nsi.setDefaultText(sMsgDefault);
        nsi.setMin(nLenMin);
        nsi.setMax(nLenMax);
        return (String) sendScriptMessage(sMsg.isEmpty() ? sMsgDefault : sMsg, AskText);

//        getClient().announce(NPCPacket.OnAskText(b, n, n2, (short) n3, nLenMin, nLenMax, sMsg, sMsgDefault));
    }

    private String askText(final String sMsg, final int n, final ScriptParam j906, final short nLenMin, final short nLenMax, final boolean bLeft, final String sMsgDefault) {
        return this.askText((byte) 4, npcId, n, (bLeft ? ScriptParam.Normal.getValue() : ScriptParam.OverrideSpeakerID.getValue()) | j906.getValue(), nLenMin, nLenMax, sMsg, sMsgDefault);
    }

    private String askTextS(final String s, final short n, final short n2, final boolean b, final String sMsgDefault) {
        return this.askText((byte) 3, npcId, 0, b ? ScriptParam.PlayerAsSpeakerFlip.getValue() : ScriptParam.PlayerAsSpeaker.getValue(), n, n2, s, sMsgDefault);
    }

    private String askTextE(final String s, final short n, final short n2, final boolean b, final String sMsgDefault) {
        return this.askText((byte) 4, npcId, 0, (b ? ScriptParam.PlayerAsSpeaker.getValue() : ScriptParam.Normal.getValue()) | ScriptParam.BoxChat.getValue(), n, n2, s, sMsgDefault);
    }

    private int askNumber(final byte b, final int n, final int n2, final int n3, final long n4, final long n5, final long n6, final String s) {

        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam((short) n3);
        nsi.setText(s);
        nsi.setDefaultNumber(n4);
        nsi.setMin(n5);
        nsi.setMax(n6);
        return (int) (long) sendScriptMessage(s, AskNumber);

//        getClient().announce(NPCPacket.OnAskNumber(b, n, (short) n3, n4, n5, n6, s));
    }

    private int askNumber(final String s, final int n, final int n2, final int n3, final int n4, final boolean b) {
        return this.askNumber((byte) 4, npcId, n, b ? ScriptParam.Normal.getValue() : ScriptParam.OverrideSpeakerID.getValue(), n2, n3, n4, s);
    }

    private int askNumberS(final String s, final int n, final int n2, final int n3, final boolean b) {
        return this.askNumber((byte) 3, npcId, 0, b ? ScriptParam.PlayerAsSpeakerFlip.getValue() : ScriptParam.PlayerAsSpeaker.getValue(), n, n2, n3, s);
    }

    private int askNumberE(final String s, final int n, final int n2, final int n3, final boolean b) {
        return this.askNumber((byte) 4, npcId, 0, (b ? ScriptParam.PlayerAsSpeaker.getValue() : ScriptParam.Normal.getValue()) | ScriptParam.BoxChat.getValue(), n, n2, n3, s);
    }

    private String askBoxText(final byte b, final int n, final int n2, final int n3, final short n4, final short n5, final String s, final String s2) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(n3);
        // nsi.setInnerOverrideSpeakerTemplateID(n2);
        nsi.setText(s);
        nsi.setDefaultText(s2);
        nsi.setCol(n4);
        nsi.setLine(n4);
        return (String) sendScriptMessage(s.isEmpty() ? s2 : s, AskBoxtext);

//        getClient().announce(NPCPacket.OnAskBoxText(b, n, n2, (short) n3, n4, n5, s, s2));
    }

    private String askBoxText(final String s, final String s2, final int n, final short n2, final short n3, final boolean b) {
        return this.askBoxText((byte) 4, npcId, n, b ? ScriptParam.Normal.getValue() : ScriptParam.OverrideSpeakerID.getValue(), n2, n3, s, s2);
    }

    private String askBoxTextS(final String s, final String s2, final short n, final short n2, final boolean b) {
        return this.askBoxText((byte) 3, npcId, 0, b ? ScriptParam.PlayerAsSpeakerFlip.getValue() : ScriptParam.PlayerAsSpeaker.getValue(), n, n2, s, s2);
    }

    private String askBoxTextE(final String s, final String s2, final short n, final short n2, final boolean b) {
        return this.askBoxText((byte) 4, npcId, 0, (b ? ScriptParam.PlayerAsSpeaker.getValue() : ScriptParam.Normal.getValue()) | ScriptParam.BoxChat.getValue(), n, n2, s, s2);
    }

    private int askSlideMenu(final byte b, final int n, final int n2, final int n3, final String s) {

        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(0);
        nsi.setColor(0);
        nsi.setDlgType(n2);
        nsi.setDefaultSelect(n3);
        nsi.setText(s);
        return (int) (long) sendScriptMessage(s, AskSlideMenu);

//        getClient().announce(NPCPacket.OnAskSlideMenu(b, n, n2, n3, s));
    }

    private int askAvatar(final byte b, final int n, final int n2, final boolean b2, final boolean b3, final int[] array, final String s) {

        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(0);
        nsi.setColor(0);
        nsi.setSecondLookValue(b2 ? 1 : b3 ? 2 : 0);
        nsi.setText(s);
        nsi.setOptions(array);
        nsi.setItemID(n2);
        nsi.setSrcBeauty(0);
        return (int) (long) sendScriptMessage(s, AskAvatar);

//        getClient().announce(NPCPacket.OnAskAvatar(b, n, n2, b2, b3, array, s));
    }

    public void askAvatarZero(int cardID, final int[] array, int[] array2, final String s) {
        askAvatarZero((byte) 4, npcId, cardID, array, array2, s);
    }

    public int askAvatarZero(final byte nSpeakerTypeID, final int nSpeakerTemplateID, int cardID, final int[] array, int[] array2, final String s) {

        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
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
        return (int) (long) sendScriptMessage(s, AskAvatarZero);

//        getClient().announce(NPCPacket.OnAskZeroAvatar(nSpeakerTypeID, nSpeakerTemplateID, cardID, array, array2, s));
    }

    public void askAndroid(final int cardID, final int[] array, final String s) {
        askAndroid((byte) 4, npcId, cardID, array, s);
    }

    public int askAndroid(final byte nSpeakerTypeID, final int nSpeakerTemplateID, final int cardID, final int[] array, final String s) {

        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setTemplateID(nSpeakerTemplateID);
        nsi.setSpeakerType(nSpeakerTypeID);
        nsi.setParam(0);
        nsi.setColor(0);
        nsi.setText(s);
        nsi.setOptions(array);
        nsi.setItemID(cardID);
        nsi.setSrcBeauty(0);
        return (int) (long) sendScriptMessage(s, AskAndroid);

//        getClient().announce(NPCPacket.OnAskAndroid(nSpeakerTypeID, nSpeakerTemplateID, cardID, array, s));
    }

    private int askPet(final byte b, final int n, final List<Item> list, final String s) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(0);
        nsi.setColor(0);
        nsi.setText(s);
        nsi.setItems(list);
        return (int) (long) sendScriptMessage(s, AskPet);
//        getClient().announce(NPCPacket.OnAskPet(b, n, list, s));
    }

    private int askSelectMenu(final byte b, final int n, final int n2, final String[] array) {

        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(0);
        nsi.setColor(0);
        nsi.setOverrideSpeakerTemplateID(n);
        nsi.setDlgType(n2);
        nsi.setDefaultSelect(0);
        nsi.setSelectText(array);
        return (int) (long) sendScriptMessage("", AskSelectMenu);

//        getClient().announce(NPCPacket.OnAskSelectMenu(b, n, n2, array));
    }

    private int askPetEvolution(final byte b, final int n, final List<Item> list, final String s) {

        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(0);
        nsi.setColor(0);
        nsi.setText(s);
        nsi.setItems(list);
        return (int) (long) sendScriptMessage(s, AskActionPetEvolution);

//        getClient().announce(NPCPacket.OnAskPetEvolution(b, n, list, s));
    }

    private int askPetAll(final byte b, final int n, final List<Item> list, final String s) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(0);
        nsi.setColor(0);
        nsi.setText(s);
        nsi.setItems(list);
        return (int) (long) sendScriptMessage(s, AskPetAll);

//        getClient().announce(NPCPacket.OnAskPetAll(b, n, list, s));
    }

    private int sayImage(final byte b, final int n, final int n2, final short n3, final String[] array) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(n2);
        nsi.setImages(array);
        return (int) (long) sendScriptMessage("", AskPetAll);
//        getClient().announce(NPCPacket.OnSayImage(b, n, n3, array));
    }

    private int sendSayIllu(final byte b, final int n, final int n2, final int n3, final boolean b2, final boolean b3, final String s, final int n4, final int n5, final boolean b4) {

        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(n3);
        // nsi.setInnerOverrideSpeakerTemplateID(n2);
        nsi.setText(s);
        nsi.setPrevPossible(b2);
        nsi.setNextPossible(b3);
        nsi.setDelay(n4);
        nsi.setUnk(n5);
        nsi.setBUnk(b4);
        return (int) (long) sendScriptMessage(s, SayIllustration);
//        getClient().announce(NPCPacket.OnSayIllu(b, n, false, 0, n2, (short) n3, b2, b3, s, n4, n5, b4));
    }

    private int sendOkIllu(final String s, final int n, final ScriptParam j906, final boolean b, final int n2, final int n3, final boolean b2) {
        return this.sendSayIllu((byte) 4, n, n, (b ? ScriptParam.Normal.getValue() : ScriptParam.OverrideSpeakerID.getValue()) | j906.getValue(), false, false, s, n2, n3, b2);
    }

    private int sendNextIllu(final String s, final int n, final ScriptParam j906, final boolean b, final int n2, final int n3, final boolean b2) {
        return this.sendSayIllu((byte) 4, n, n, (b ? ScriptParam.Normal.getValue() : ScriptParam.OverrideSpeakerID.getValue()) | j906.getValue(), false, true, s, n2, n3, b2);
    }

    private int sendPrevIllu(final String s, final int n, final ScriptParam j906, final boolean b, final int n2, final int n3, final boolean b2) {
        return this.sendSayIllu((byte) 4, n, n, (b ? ScriptParam.Normal.getValue() : ScriptParam.OverrideSpeakerID.getValue()) | j906.getValue(), true, false, s, n2, n3, b2);
    }

    private int sendNextPrevIllu(final String s, final int n, final ScriptParam j906, final boolean b, final int n2, final int n3, final boolean b2) {
        return this.sendSayIllu((byte) 4, n, n, (b ? ScriptParam.Normal.getValue() : ScriptParam.OverrideSpeakerID.getValue()) | j906.getValue(), true, true, s, n2, n3, b2);
    }

    private String askQuiz(final byte b, final int n, final int n2, final int n3, final boolean b2, final int n4, final int n5, final String s, final String s2, final String s3) {

        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(n2);
        nsi.setType(b2 ? 0 : 1);
        nsi.setTitle(s);
        nsi.setProblemText(s2);
        nsi.setHintText(s3);
        nsi.setMin(n4);
        nsi.setMax(n5);
        nsi.setTime(0);
        return (String) sendScriptMessage("", InitialQuiz);

//        getClient().announce(NPCPacket.OnAskQuiz(b, n, (short) n3, b2, n4, n5, s, s2, s3));
    }

    private String askSpeedQuiz(final byte b, final int n, final int n2, final int n3, final boolean b2, final int n4, final int n5, final int n6, final int n7, final int n8) {

        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(n2);
        nsi.setType(b2 ? 0 : 1);
        nsi.setQuizType(n4);
        nsi.setAnswer(n5);
        nsi.setCorrectAnswers(n6);
        nsi.setRemaining(n7);
        nsi.setTime(n8);
        return (String) sendScriptMessage("", InitialSpeedQuiz);

//        getClient().announce(NPCPacket.OnAskSpeedQuiz(b, n, (short) n3, b2, n4, n5, n6, n7, n8));
    }

    private String askICQuiz(final byte b, final int n, final int n2, final int n3, final boolean b2, final String s, final String s2, final int n4) {

        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(n3);
        nsi.setType(b2 ? 0 : 1);
        nsi.setText(s);
        nsi.setHintText(s2);
        nsi.setTime(n4);
        return (String) sendScriptMessage(s, ICQuiz);

//        getClient().announce(NPCPacket.OnAskICQuiz(b, n, (short) n3, b2, s, s2, n4));
    }

    private String askOlympicQuiz(final byte b, final int n, final int n2, final int n3, final boolean b2, final int nType, final int nQuestion, final int nCorrect, final int nRemain, final int tRemainInitialQuiz) {

        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(1);
        nsi.setTemplateID(n);
        nsi.setSpeakerType(b);
        nsi.setParam(n3);
        nsi.setType(b2 ? 0 : 1);
        nsi.setQuizType(nType);
        nsi.setAnswer(nQuestion);
        nsi.setCorrectAnswers(nCorrect);
        nsi.setRemaining(nRemain);
        nsi.setTime(tRemainInitialQuiz);
        return (String) sendScriptMessage("", AskOlympicQuiz);

//        getClient().announce(NPCPacket.OnAskOlympicQuiz(b, n, (short) n3, b2, nType, nQuestion, nCorrect, nRemain, tRemainInitialQuiz));
    }

    private int askNumberKeypad(final byte b, final int n, final int n2, final int n3, final int n4) {

        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setTemplateID(n);
        nsi.setSpeakerType(3);
        nsi.setParam(n3);
        nsi.setColor(0);
        nsi.setDefaultNumber(n4);
        return (int) (long) sendScriptMessage("", OnAskNumberUseKeyPad);

//        getClient().announce(NPCPacket.OnAskNumberKeypad(n, (short) n3, n4));
    }

    private int askUserSurvey(final byte b, final int n, final int n2, final int n3, final int n4, final String s) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setTemplateID(n);
        nsi.setSpeakerType(3);
        nsi.setParam(n3);
        nsi.setColor(0);
        nsi.setDefaultNumber(n4);
        nsi.setText(s);
        return (int) (long) sendScriptMessage(s, AskUserSurvey);
//        getClient().announce(NPCPacket.OnAskUserSurvey(n, (short) n3, n4, s));
    }

    //-----------------------------------------------
    // private npc talk impl end
    //-----------------------------------------------


    //-----------------------------------------------
    // private new npc talk start
    //-----------------------------------------------
    public ScriptNpc id(int npcId) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setTemplateID(npcId);
        return this;
    }

    public ScriptNpc overrideSpeakerId(int npcId) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        // nsi.setInnerOverrideSpeakerTemplateID(npcId);
        return this;
    }

    public ScriptNpc noEsc() {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.addParam(ScriptParam.NoEsc);
        return this;
    }

    public ScriptNpc me() {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.addParam(ScriptParam.PlayerAsSpeakerFlip);
        return this;
    }

    public ScriptNpc npcFlip() {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.addParam(ScriptParam.FlipSpeaker);
        return this;
    }

    public ScriptNpc meFlip() {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.addParam(ScriptParam.PlayerAsSpeaker);
        return this;
    }

    public ScriptNpc npcRightSide() {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.addParam(ScriptParam.OverrideSpeakerID);
        return this;
    }

    public ScriptNpc replace() {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.addParam(ScriptParam.Replace);
        return this;
    }

    public ScriptNpc line(int line) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setLine(line);
        return this;
    }

    //    配合uiMax
    //    UI 1~3
    public ScriptNpc ui(int ui) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setColor(ui);
        return this;
    }

    public ScriptNpc uiMax() {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.addParam(ScriptParam.LargeBoxChat);
        return this;
    }

    public ScriptNpc prev() {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setPrevPossible(true);
        return this;
    }

    public ScriptNpc next() {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setNextPossible(true);
        return this;
    }

    public ScriptNpc defText(String defText) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setDefaultText(defText);
        return this;
    }

    public ScriptNpc minLen(int minLen) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setMin(minLen);
        return this;
    }

    public ScriptNpc maxLen(int maxLen) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setMax(maxLen);
        return this;
    }

    public ScriptNpc defNum(int defNum) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setDefaultNumber(defNum);
        return this;
    }

    public ScriptNpc minNum(int minNum) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setMin(minNum);
        return this;
    }

    public ScriptNpc maxNum(int maxNum) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setMax(maxNum);
        return this;
    }

    public ScriptNpc time(int time) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setTime(time);
        return this;
    }

    public ScriptNpc delay(int delay) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setDelay(delay);
        return this;
    }

    public ScriptNpc cardId(int cardId) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setItemID(cardId);
        return this;
    }

    public ScriptNpc col(int col) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setCol(col);
        return this;
    }

    public ScriptNpc styles(int[] styles) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setOptions(styles);
        return this;
    }

    /**
     * 神之子專用
     * styles
     *
     * @return
     */
    public ScriptNpc styles2(int[] styles) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setOptions2(styles);
        return this;
    }

    public ScriptNpc defSel(int defSel) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setDefaultSelect(defSel);
        return this;
    }

    public ScriptNpc speakerType(int speakerType) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setSpeakerType(speakerType);
        return this;
    }

    public ScriptNpc items(List<Item> list) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setItems(list);
        return this;
    }

    public ScriptNpc setSrcBeautyX(int setSrcBeauty) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setSrcBeauty(setSrcBeauty);
        return this;
    }

    /**
     * 神之子專用
     * setSrcBeauty
     *
     * @return
     */
    public ScriptNpc setSrcBeautyX2(int setSrcBeauty) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setSrcBeauty2(setSrcBeauty);
        return this;
    }

    public ScriptNpc secondLookValueX(int secondLookValue) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setSecondLookValue(secondLookValue);
        return this;
    }

    public void sayX(String text) {
        sendScriptMessage(text, Say);
    }

    public void sayImageX(String[] text) {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        nsi.setImages(text);
        sendScriptMessage("", SayImage);
        resetNpc();
    }

    public String askTextX(String text) {
        String result = (String) sendScriptMessage(text, AskText);
        resetNpc();
        return result;
    }

    public String askBoxTextX(String text) {
        String result = (String) sendScriptMessage(text, AskBoxtext);
        resetNpc();
        return result;
    }

    public int askMenuX(String text) {
        long result = (long) sendScriptMessage(text, AskMenu);
        resetNpc();
        return (int) result;
    }

    public boolean askYesNoX(String text) {
        boolean result = (long) sendScriptMessage(text, AskYesNo) == 1;
        resetNpc();
        return result;
    }

    public boolean askAcceptX(String text) {
        boolean result = (long) sendScriptMessage(text, AskAccept) == 1;
        resetNpc();
        return result;
    }

    public long askNumberX(String message) {
        long result = (long) sendScriptMessage(message, AskNumber);
        resetNpc();
        return result;
    }

    public boolean askAngelicBusterX() {
        boolean result = ((long) sendScriptMessage("", AskAngelicBuster)) != 0;
        resetNpc();
        return result;
    }

    /**
     * message
     * itemId
     * secondLookValue
     * srcBeauty
     * styles
     *
     * @return
     */
    public int askAvatarX(String message) {
        int result = (int) (long) sendScriptMessage(message, AskAvatar);
        resetNpc();
        return result;
    }

    /**
     * message
     * list
     *
     * @return
     */
    public long askPetX(String message) {
        long result = (long) sendScriptMessage(message, AskPet);
        resetNpc();
        return result;
    }

    public long askAndroidX(String message) {

        long result = (long) sendScriptMessage(message, AskAndroid);
        resetNpc();
        return result;

    }

    /**
     * message
     * itemId
     * srcBeauty
     * srcBeauty2
     * styles
     * styles2
     *
     * @return
     */
    public int askAvatarZeroX(String message) {
        int result = (int) (long) sendScriptMessage(message, AskAvatarZero);
        resetNpc();
        return result;

    }

    /**
     * cardID
     * msg
     * secondLookValue
     * srcBeauty
     *
     * @return
     */
    public int askAvatarMixColorX(String msg) {
        int result = (int) (long) sendScriptMessage(msg, AskAvatarMixColor);
        resetNpc();
        return result;
    }

    /**
     * itemID
     * secondLookValue
     * msg
     *
     * @return
     */
    public boolean askAvatarRandomMixColorX(String msg) {
        boolean result = ((long) sendScriptMessage(msg, AskAvatarRandomMixColor)) != 0;
        resetNpc();
        return result;
    }

    /**
     * msg
     * itemID
     * srcBeauty
     * drtBeauty
     * srcBeauty2
     * drtBeauty2
     *
     * @return
     */
    public int sayAvatarMixColorChangedX(String msg) {
        int result = (int) (long) sendScriptMessage(msg, SayAvatarMixColorChanged);
        resetNpc();
        return result;
    }

    /**
     * itemID
     * secondLookValue
     * srcBeauty
     * srcBeauty2
     *
     * @return
     */
    public boolean askConfirmAvatarChangeX() {
        boolean result = ((long) sendScriptMessage("", AskConfirmAvatarChange)) != 0;
        resetNpc();
        return result;

    }

    private void resetNpc() {
        NpcScriptInfo nsi = getPlayer().getScriptManager().getNpcScriptInfo();
        NpcScriptInfo new_nsi = new NpcScriptInfo();
        new_nsi.setTemplateID(nsi.getTemplateID());
        getPlayer().getScriptManager().setNpcScriptInfo(new_nsi);
    }

    //-----------------------------------------------
    // private new npc talk end
    //-----------------------------------------------


    public void openWebUI(int n, String sUOL, String sURL) {
        getClient().announce(MaplePacketCreator.openWebUI(n, sUOL, sURL));
    }

    public void openWeb(String sURL) {
        getClient().announce(MaplePacketCreator.openWeb((byte) 0, (byte) 1, sURL));
    }

    public void createParty() {
        if (getPlayer().getParty() == null) {
            Party party = Party.createNewParty(false, false, getPlayer().getName() + "的隊伍", getPlayer().getClient().getWorld());
            PartyMember pm = new PartyMember(getPlayer());
            party.setPartyLeaderID(pm.getCharID());
            party.getPartyMembers()[0] = pm;
            getPlayer().setParty(party);
            getPlayer().write(WvsContext.partyResult(PartyResult.createNewParty(party)));
        }
    }

    public void disbandParty() {
        getClient().getPlayer().getParty().disband();
    }

    public void startQuest() {
        MapleQuest quest = MapleQuest.getInstance((int) obj);
        int npc_id = npcId;
        for (MapleQuestRequirement qr : quest.getStartReqs()) {
            if (qr.getType() == MapleQuestRequirementType.npc) {
                npc_id = qr.getIntStore();
                break;
            }
        }
        quest.forceStart(getPlayer(), npc_id, "");
    }

    public void completeQuest() {
        MapleQuest quest = MapleQuest.getInstance((int) obj);
        int npc_id = npcId;
        for (MapleQuestRequirement qr : quest.getCompleteReqs()) {
            if (qr.getType() == MapleQuestRequirementType.npc) {
                npc_id = qr.getIntStore();
                break;
            }
        }
        quest.forceComplete(getPlayer(), npc_id, false);
    }

    public ScriptEvent makeEvent(String script, Object attachment) {
//        getPlayer().getScriptManager().stop(scriptType);
        ScriptEvent event = new EventManager(script, getPlayer().getClient().getChannel(), null).runScript(getPlayer(), script, true, attachment);
        return event;
    }

    public short getPosition() {
        return ((Item) obj).getPosition();
    }

    /***
     * 道具是否正在使用
     * @return
     */
    public boolean used() {
        return used(1);
    }

    /***
     * 道具是否正在使用
     * @param q
     * @return
     */
    public boolean used(int q) {
        return MapleInventoryManipulator.removeFromSlot(getClient(), ItemConstants.getInventoryType(getItemId()), getPosition(), (short) q, true, false);
    }

    public void showPopupSay(int npcid, int time, String msg, String sound) {
        getClient().announce(UIPacket.addPopupSay(npcid, time, msg, sound));
    }

    /***
     * 取的道具工具
     * @return
     */
    public MapleItemInformationProvider getItemInfo() {

        return MapleItemInformationProvider.getInstance();
    }


    public Map<Integer, String> getSearchData(int type, String search) {
        return SearchGenerator.getSearchData(type, search);
    }

    public String searchData(int type, String search) {
        return SearchGenerator.searchData(SearchGenerator.SearchType.valueOf(SearchGenerator.SearchType.nameOf(type)), search);
    }

    public void saying(String message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ScriptMessage.getValue());
        mplew.writeInt(1401380);
        mplew.write(3);
        mplew.writeInt(0);
        mplew.write(1);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeShort(57); // type
        mplew.write(1);
        mplew.write(1);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        mplew.writeMapleAsciiString(message);
        mplew.write(0);
        mplew.write(1);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        getPlayer().send(mplew.getPacket());
    }
}
