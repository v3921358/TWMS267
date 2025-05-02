package Handler;

import Client.MapleClient;
import Opcode.Headler.OutHeader;
import connection.InPacket;
import connection.OutPacket;

import static Opcode.Headler.InHeader.*;

public class UIHandler {

    @Handler(op = CP_CHECK_PLAYER_STATUS)
    public static void getCharStatUI(MapleClient c, InPacket inPacket) {
        OutPacket packet = new OutPacket(OutHeader.LP_CharacterInfo);
        packet.encodeArr(new byte[9]);
        packet.encodeBoolean(true);
        c.write(packet);
    }

    @Handler(op = CP_CHAR_USE_WARP_ITEM)
    public static void useWarpItem(MapleClient c, InPacket inPacket) {
        OutPacket packet = new OutPacket(OutHeader.LP_CHAR_USE_WARP_ITEM);
        int warpCrc = inPacket.decodeInt();
        int warpType = inPacket.decodeInt();
        int unk = inPacket.decodeInt();
        int mapid = inPacket.decodeInt();
        packet.encodeInt(warpCrc);
        packet.encodeInt(warpType);
        packet.encodeInt(0);
        c.write(packet);
    }

    /* 使用P建需要反射*/
    @Handler(op = USER_USE_PARTY_KEYBOARD)
    public static void CharUsePkeyBoard(MapleClient c, InPacket inPacket) {
        OutPacket packet = new OutPacket(OutHeader.LP_PartyCandidateResult);
        packet.encodeByte((byte) 0);
        c.write(packet);
    }

    /* 稱號開關 */
    @Handler(op = USER_SHOW_TITLE)
    public static void ShowTitle(MapleClient c, InPacket inPacket) {
        c.getPlayer().getScriptManager().startNpcScript(9900000, 0, "BossUIEventNotice");
    }

    @Handler(op = CTX_PLAYER_ENTER_EVENT)
    public static void chechEventStart(MapleClient c, InPacket inPacket) {
        int BossType = inPacket.decodeShort();
        inPacket.decodeInt();
        int isNormal = inPacket.decodeShort();
        if (isNormal == 1) {
            c.getPlayer().getScriptManager().startNpcScript(2184000, 0, "Boss_EventUI/" + BossType + "_BossEvent_NORMAL");
        } else {
            c.getPlayer().getScriptManager().startNpcScript(9900000, 0, "Boss_EventUI/" + BossType + "_BossEvent_HARD");
        }
    }
}
