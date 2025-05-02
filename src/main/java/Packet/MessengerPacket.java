/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Packet;

import Client.MapleCharacter;
import Opcode.Headler.OutHeader;
import Server.world.WorldAllianceService;
import Server.world.WorldGuildService;
import Server.world.guild.MapleGuild;
import Server.world.guild.MapleGuildAlliance;
import Server.world.messenger.MessengerRankingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.DateUtil;
import tools.data.MaplePacketLittleEndianWriter;

/**
 * @author PlayDK
 */
public class MessengerPacket {

    private static final Logger log = LoggerFactory.getLogger(MessengerPacket.class);

    /*
     * 增加聊天招待的角色
     */
    public static byte[] addMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_Messenger.getValue());
        mplew.write(0x00);
        mplew.write(position);
        mplew.writeInt(0);
        chr.getAvatarLook().encode(mplew, true);
        mplew.writeMapleAsciiString(from);
        mplew.write(channel);
        mplew.write(position); //難道是位置？
        chr.writeJobData(mplew); //職業ID

        return mplew.getPacket();
    }

    /*
     * 同意加入聊天招待
     * 這個是發送在聊天招待裡面的位置
     */
    public static byte[] joinMessenger(int position) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_Messenger.getValue());
        mplew.write(0x01);
        mplew.write(position);

        return mplew.getPacket();
    }

    /*
     * 聊天招待
     * 玩家退出
     */
    public static byte[] removeMessengerPlayer(int position) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_Messenger.getValue());
        mplew.write(0x02);
        mplew.write(position);

        return mplew.getPacket();
    }

    /*
     * 收到玩家的聊天邀請
     */
    public static byte[] messengerInvite(String from, int messengerId, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_Messenger.getValue());
        mplew.write(3);
        mplew.writeInt(messengerId);
        mplew.writeMapleAsciiString(from);
        mplew.writeInt(0);
        mplew.write(channel);
        mplew.writeInt(messengerId);
        mplew.write(0x00);

        return mplew.getPacket();
    }

    /*
     * 聊天招待說話
     */
    public static byte[] messengerChat(MapleCharacter chr, String text, String postxt) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_Messenger.getValue());
        mplew.write(0x06);
        mplew.writeMapleAsciiString(text);
        mplew.writeMapleAsciiString(postxt);
        PacketHelper.addChaterName(mplew, chr.getName(), text, chr.getId());
        return mplew.getPacket();
    }

    public static byte[] npcEffectChat_BlackLock(String text) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserInGameDirectionEvent.getValue());
        mplew.write(0x0F);
        mplew.writeMapleAsciiString(text);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] updateMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_Messenger.getValue());
        mplew.write(0x08);
        mplew.write(position);
        chr.getAvatarLook().encode(mplew, true);
        //mplew.writeMapleAsciiString(from);
        //mplew.write(0x00); //是否寫職業ID 0x01 為需要寫
        //mplew.writeInt(0x00); //職業ID 上面為1時就要寫職業ID

        return mplew.getPacket();
    }

    /*
     * 聊天招待中給玩家加好感度的返回
     */
    public static byte[] giveLoveResponse(int mode, String charName, String targetName) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_Messenger.getValue());
        mplew.write(0x0B);
        /*
         * 0x00 'xxxx'成功提升了'xxxx'的好感度。
         * 0x01 由於未知原因，提升好感度失敗。
         * 0x02 今天之內無法再次提升'xxxx'的好感度。
         */
        mplew.write(mode);
        mplew.writeMapleAsciiString(charName);
        mplew.writeMapleAsciiString(targetName);

        return mplew.getPacket();
    }

    /*
     * 在聊天招待中查看玩家的信息
     */
    public static byte[] messengerPlayerInfo(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_Messenger.getValue());
        mplew.write(0x0C);
        mplew.writeMapleAsciiString(chr.getName()); //角色名字
        mplew.writeInt(chr.getLevel()); //等級
        chr.writeJobData(mplew); //職業
        mplew.writeInt(chr.getFame()); //人氣
        mplew.writeInt(chr.getLove()); //好感度
        if (chr.getGuildId() <= 0) {
            mplew.writeMapleAsciiString("-");
            mplew.writeMapleAsciiString("");
        } else {
            MapleGuild guild = WorldGuildService.getInstance().getGuild(chr.getGuildId());
            if (guild != null) {
                mplew.writeMapleAsciiString(guild.getName());
                if (guild.getAllianceId() > 0) {
                    MapleGuildAlliance alliance = WorldAllianceService.getInstance().getAlliance(guild.getAllianceId());
                    if (alliance != null) {
                        mplew.writeMapleAsciiString(alliance.getName());
                    } else {
                        mplew.writeMapleAsciiString("");
                    }
                } else {
                    mplew.writeMapleAsciiString("");
                }
            } else {
                mplew.writeMapleAsciiString("-");
                mplew.writeMapleAsciiString("");
            }
        }
        mplew.write(0x00); //未知

        return mplew.getPacket();
    }

    /*
     * 聊天招待中私聊
     */
    public static byte[] messengerWhisper(String nameFrom, String chatText) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_Messenger.getValue());
        mplew.write(0x0F);
        mplew.writeMapleAsciiString(nameFrom);
        mplew.writeMapleAsciiString(chatText);

        return mplew.getPacket();
    }

    public static byte[] messengerNote(String text, int mode, int mode2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_Messenger.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(text);
        mplew.write(mode2);

        return mplew.getPacket();
    }

    public static byte[] updateLove(int love) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_AskAfterErrorAck.getValue());
        mplew.write(0);
        mplew.writeInt(love); //好感度
        mplew.writeLong(DateUtil.getFileTimestamp(System.currentTimeMillis()));
        mplew.writeInt(0x03);

        return mplew.getPacket();
    }

    public static byte[] showLoveRank(int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_AskAfterErrorAck.getValue());
        mplew.write(mode);
        MessengerRankingWorker rank = MessengerRankingWorker.getInstance();
        for (int i = 0; i < 2; i++) {
            MapleCharacter player = rank.getRankingPlayer(i);
            mplew.write(player != null ? 1 : 0);
            if (player != null) {
                mplew.writeInt(player.getId());
                mplew.writeInt(player.getLove());
                mplew.writeLong(DateUtil.getFileTimestamp(rank.getLastUpdateTime(i)));
                mplew.writeMapleAsciiString(player.getName());
                player.getAvatarLook().encode(mplew, false);
            }
        }

        return mplew.getPacket();
    }

    public static byte[] getInTheGameMessage() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(-2);
        //※ 泰里的相機是透過#b聚集相同職業6名一起拍照#k 挑戰任務的協助按鍵可領取?
        mplew.writeHexString("56 75 8F 00 94 11 00 00 77 00 A6 AC A8 EC AE F5 A8 BD AA BA AC DB BE F7 AB E1 BD D0 B0 D1 A5 5B AC DD AC DD AF 53 A7 4F AA BA AC 44 BE D4 A5 F4 B0 C8 A1 49 0D 0A 0D 0A A1 B0 20 AE F5 A8 BD AA BA AC DB BE F7 AC 4F B3 7A B9 4C 23 62 BB 45 B6 B0 AC DB A6 50 C2 BE B7 7E 36 A6 57 A4 40 B0 5F A9 E7 B7 D3 23 6B 20 AC 44 BE D4 A5 F4 B0 C8 AA BA A8 F3 A7 55 AB F6 C1 E4 A5 69 BB E2 A8 FA A1 43 00 00 00");
        return mplew.getPacket();
    }

}
