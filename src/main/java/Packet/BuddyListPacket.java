/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Packet;

import Client.BuddylistEntry;
import Client.MapleCharacter;
import Config.constants.enums.FriendOperationMode;
import Opcode.Headler.OutHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.data.MaplePacketLittleEndianWriter;

import java.util.Collection;

/**
 * @author PlayDK
 */
public class BuddyListPacket {

    /**
     * Logger for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(BuddyListPacket.class);

    /*
     * 返回好友操作信息
     * 0x0B 好友目錄已滿了。
     * 0x0C 對方的好友目錄已滿了。
     * 0x0D 已經是好友。
     * 0x0E 不能把管理員加為好友。
     * 0x0F 沒登錄的角色。
     * 0x1E 還在對方的好友目錄中
     */
    public static byte[] buddylistMessage(int message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_FriendResult.getValue());
        mplew.write(message);

        return mplew.getPacket();
    }

    public static byte[] updateBuddylist(Collection<BuddylistEntry> buddylist) {
        return updateBuddylist(buddylist, FriendOperationMode.FriendRes_LoadAccountIDOfCharacterFriend_Done.getValue());//更新
    }

    /*
     * 更新好友信息
     */
    public static byte[] updateBuddylist(Collection<BuddylistEntry> buddylist, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_FriendResult.getValue());
        mplew.write(mode);
        if (mode != FriendOperationMode.FriendRes_SetMessengerMode.getValue()) {
            mplew.writeInt(buddylist.size());
        }
        for (BuddylistEntry buddy : buddylist) {
            mplew.writeInt(buddy.getCharacterId());
            mplew.writeAsciiString(buddy.getName(), 15);
            mplew.write(buddy.isVisible() ? 0 : 1);//0普通好友不在線 2普通好友在線 4開啟帳號轉換,5離線賬號好友,7賬號好友在線
            mplew.writeInt(buddy.getChannel() == -1 ? -1 : (buddy.getChannel() - 1));
            mplew.writeAsciiString(buddy.getGroup(), 18); //V.116.修改以前 17位
            mplew.writeZeroBytes(295);
            /*
            mplew.writeInt(0); // buddy.getAccountId()
            mplew.writeAsciiString(buddy.getName(), 13);//別名
            mplew.writeHexString("00 AE 1A 0B 64 FC 34 11 10 0D AB 0F 68");
            mplew.writeAsciiString("", 15);//備註
            mplew.writeZeroBytes(247);
            */
        }
//        mplew.writeHexString("69 00 15 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 07 FF FF FF FF CE B4 D6 B8 B6 A8 C8 BA D7 E9 00 00 00 00 00 00 00 00 E9 C9 91 02 CB D1 CB F7 00 00 00 00 00 00 00 00 00 EC AA EC AA 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 05 FF FF FF FF CE B4 D6 B8 B6 A8 C8 BA D7 E9 00 00 00 00 00 00 00 00 79 0F D5 02 CA C7 00 00 00 00 00 00 00 00 00 00 00 B5 C4 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
        return mplew.getPacket();
    }

    /*
     * 更新好友完畢
     */
    public static byte[] updateBuddylistEnd() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_FriendResult.getValue());
        mplew.write(FriendOperationMode.FriendRes_SetMessengerMode.getValue());
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    /*
     * 申請加好友
     */
    public static byte[] aarequestBuddylistAdd(int chrIdFrom, String nameFrom, int channel, int levelFrom, int jobFrom) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_FriendResult.getValue());
        MapleCharacter player = MapleCharacter.getCharacterById(chrIdFrom);
        mplew.write(40);
        mplew.writeInt(player.getId());
        mplew.writeAsciiString(player.getName());
        mplew.writeZeroBytes(10);
        mplew.write(5);
        mplew.writeShort(0);
        mplew.writeShort(0); //V.104新增 貌似是把職業的 Int 改為 Long ?
        mplew.writeAsciiString("群組未指定");
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(11621044);
        mplew.writeAsciiString(player.getName());
        mplew.writeZeroBytes(282);
        mplew.writeInt(1);
        return mplew.getPacket();
    }

    /*
     * 更新好友頻道信息
     */
    public static byte[] updateBuddyChannel(int chrId, int channel, String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_FriendResult.getValue());
        mplew.write(FriendOperationMode.FriendRes_Notify.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(0);
        mplew.write(0); //isVisible() 角色在商城和拍賣的時候為1
        mplew.writeInt(channel);
        mplew.write(0);
        mplew.write(1);

        return mplew.getPacket();
    }

    /*
     * 更新好友數量
     */
    public static byte[] updateBuddyCapacity(int capacity) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(OutHeader.LP_FriendResult.getValue());
        mplew.write(FriendOperationMode.FriendRes_IncMaxCount_Done.getValue());
        mplew.write(capacity);

        return mplew.getPacket();
    }

    /*
     * 更新好友別名
     */
    public static byte[] updateBuddyNamer(BuddylistEntry buddylist, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_FriendResult.getValue());
        MapleCharacter p = MapleCharacter.getCharacterById(buddylist.getCharacterId());
        mplew.write(40);
        mplew.writeInt(buddylist.getCharacterId());
        mplew.writeAsciiString(buddylist.getName(), 15);
        mplew.write(0);
        mplew.writeInt(buddylist.getChannel() == -1 ? -1 : (buddylist.getChannel() - 1));
        mplew.writeAsciiString(buddylist.getGroup(), 18); //V.116.修改以前 17位
        mplew.writeInt(p.getAccountID());
        mplew.writeInt(0); //0
        mplew.writeInt(0); //4
        mplew.writeInt(0); //8
        mplew.writeInt(0); //12
        mplew.writeInt(0); //16
        mplew.writeInt(0); //20
        mplew.writeInt(0); //24
        mplew.writeInt(0); //28
        mplew.writeInt(0); //32
        mplew.writeInt(0); //36
        mplew.writeInt(0); //40
        mplew.writeInt(0); //44
        mplew.writeInt(0); //48
        mplew.writeInt(0); //52
        mplew.writeInt(0); //56
        mplew.writeInt(0); //60
        mplew.writeInt(0); //64
        mplew.writeInt(0); //68
        mplew.writeInt(0); //72
        mplew.writeInt(0); //76
        mplew.writeInt(0); //80
        mplew.writeInt(0); //84
        mplew.writeInt(0); //88
        mplew.writeInt(0); //92
        mplew.writeInt(0); //96
        mplew.writeInt(0); //100
        mplew.writeInt(0); //104
        mplew.writeInt(0); //108
        mplew.writeInt(0); //112
        mplew.writeInt(0); //116
        mplew.writeInt(0); //120
        mplew.writeInt(0); //124
        mplew.writeInt(0); //128
        mplew.writeInt(0); //132
        mplew.writeInt(0); //136
        mplew.writeInt(0); //140
        mplew.writeInt(0); //144
        mplew.writeInt(0); //148
        mplew.writeInt(0); //152
        mplew.writeInt(0); //156
        mplew.writeInt(0); //160
        mplew.writeInt(0); //164
        mplew.writeInt(0); //168
        mplew.writeInt(0); //172
        mplew.writeInt(0); //176
        mplew.writeInt(0); //180
        mplew.writeInt(0); //184
        mplew.writeInt(0); //188
        mplew.writeInt(0); //192
        mplew.writeInt(0); //196
        mplew.writeInt(0); //200
        mplew.writeInt(0); //204
        mplew.writeInt(0); //208
        mplew.writeInt(0); //212
        mplew.writeInt(0); //216
        mplew.writeInt(0); //220
        mplew.writeInt(0); //224
        mplew.writeInt(0); //228
        mplew.writeInt(0); //232
        mplew.writeInt(0); //236
        mplew.writeInt(0); //240
        mplew.writeInt(0); //244
        mplew.writeInt(0); //248
        mplew.writeInt(0); //252
        mplew.writeInt(0); //256
        mplew.writeInt(0); //260
        mplew.writeInt(0); //264
        mplew.writeInt(0); //268
        mplew.writeInt(0); //272
        mplew.write(0); //276
        mplew.writeInt(1); //0
        mplew.writeInt(2); //4
        mplew.writeInt(256); //8
        mplew.write(0); //12
        return mplew.getPacket();
    }

    /*
     * 拒絕好友
     */
    public static byte[] NoBuddy(int buddyid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_FriendResult.getValue());
        MapleCharacter player = MapleCharacter.getCharacterById(buddyid);
        mplew.write(53);
        mplew.writeMapleAsciiString(player.getName());
        return mplew.getPacket();
    }

    /*
     * 好友信息
     */
    public static byte[] BuddyMess(int mode, String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_FriendResult.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(name);
        return mplew.getPacket();
    }
}
