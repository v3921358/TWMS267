package Server.channel.handler;

import Client.*;
import Client.BuddyList.BuddyAddResult;
import Client.BuddyList.BuddyOperation;
import Config.constants.enums.FriendOperationMode;
import Database.DatabaseLoader.DatabaseConnectionEx;
import Packet.BuddyListPacket;
import Server.channel.ChannelServer;
import Server.world.WorldBuddyService;
import Server.world.WorldFindService;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static Client.BuddyList.BuddyOperation.刪除好友;
import static Client.BuddyList.BuddyOperation.添加好友;

public class BuddyListHandler {

    private static CharacterIdNameBuddyCapacity getCharacterIdAndNameFromDatabase(String name, String group) throws SQLException {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE name LIKE ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            CharacterIdNameBuddyCapacity ret = null;
            if (rs.next()) {
                if (rs.getInt("gm") < 3) {
                    ret = new CharacterIdNameBuddyCapacity(rs.getInt("id"), rs.getString("name"), group, rs.getInt("buddyCapacity"), true);
                }
            }
            rs.close();
            ps.close();
            return ret;
        }
    }

    public static void BuddyOperation(MaplePacketReader slea, MapleClient c) {
        byte mode = slea.readByte();
        FriendOperationMode opcode = FriendOperationMode.getByAction(mode);
        if (opcode == null) {
            System.err.println("未處理好友操作碼：" + mode);
            return;
        }
        BuddyList buddylist = c.getPlayer().getBuddylist();
        switch (opcode) {
            case FriendReq_SetFriend: { // 添加好友
                String addName = slea.readMapleAsciiString();
                String groupName = slea.readMapleAsciiString();
                BuddylistEntry ble = buddylist.get(addName);
                if (addName.getBytes().length > 13 || groupName.getBytes().length > 18) {
                    return;
                } else {
                    try {
                        // 獲取對方信息
                        CharacterIdNameBuddyCapacity charWithId = null;
                        int channel = WorldFindService.getInstance().findChannel(addName);
                        MapleCharacter otherChar;
                        if (channel > 0) {
                            otherChar = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterByName(addName);
                            if (otherChar == null) {
                                charWithId = getCharacterIdAndNameFromDatabase(addName, groupName);
                            } else if (!otherChar.isIntern() || c.getPlayer().isIntern()) {
                                charWithId = new CharacterIdNameBuddyCapacity(otherChar.getId(), otherChar.getName(), groupName, otherChar.getBuddylist().getCapacity(), true);
                            }
                        } else {
                            charWithId = getCharacterIdAndNameFromDatabase(addName, groupName);
                        }
                        if (charWithId != null) {
                            BuddyAddResult buddyAddResult = null;
                            if (channel > 0) {
                                buddyAddResult = WorldBuddyService.getInstance().requestBuddyAdd(addName, c.getChannel(), c.getPlayer().getId(), c.getPlayer().getName(), c.getPlayer().getLevel(), c.getPlayer().getJob());
                            } else {
                                try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
                                    PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) AS buddyCount FROM buddies WHERE characterid = ? AND pending = 0");
                                    ps.setInt(1, charWithId.getId());
                                    ResultSet rs = ps.executeQuery();
                                    if (!rs.next()) {
                                        ps.close();
                                        rs.close();
                                        throw new RuntimeException("Result set expected");
                                    } else {
                                        int count = rs.getInt("buddyCount");
                                        if (count >= charWithId.getBuddyCapacity()) {
                                            buddyAddResult = BuddyAddResult.好友列表已滿;
                                        }
                                    }
                                    rs.close();
                                    ps.close();

                                    ps = con.prepareStatement("SELECT pending FROM buddies WHERE characterid = ? AND buddyid = ?");
                                    ps.setInt(1, charWithId.getId());
                                    ps.setInt(2, c.getPlayer().getId());
                                    rs = ps.executeQuery();
                                    if (rs.next()) {
                                        buddyAddResult = BuddyAddResult.已經是好友關係;
                                    }
                                    rs.close();
                                    ps.close();
                                }
                            }
                            if (buddyAddResult == BuddyAddResult.好友列表已滿) {
                                c.announce(FriendOperationMode.FriendRes_SetFriend_FullOther.getPacket());
                            } else {
                                int displayChannel = -1;
                                int otherCid = charWithId.getId();
                                if (buddyAddResult == BuddyAddResult.已經是好友關係 && channel > 0) {
                                    displayChannel = channel;
                                    notifyRemoteChannel(c, channel, otherCid, groupName, 添加好友);
                                } else if (buddyAddResult != BuddyAddResult.已經是好友關係) {
                                    try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
                                        PreparedStatement ps = con.prepareStatement("INSERT INTO buddies (`characterid`, `buddyid`, `groupname`, `pending`) VALUES (?, ?, ?, 1)");
                                        ps.setInt(1, charWithId.getId());
                                        ps.setInt(2, c.getPlayer().getId());
                                        ps.setString(3, groupName);
                                        ps.executeUpdate();
                                        ps.close();
                                    }
                                }
                                buddylist.put(new BuddylistEntry(charWithId.getName(), otherCid, groupName, displayChannel, true));
                                MaplePacketLittleEndianWriter addFriend = new MaplePacketLittleEndianWriter();
                                addFriend.writeShort(203);
                                addFriend.write(40);
                                addFriend.writeInt(charWithId.getId());
                                addFriend.writeAsciiString(addName, 15);
                                addFriend.write(0);
                                addFriend.write(0);
                                addFriend.write(0);
                                addFriend.write(5);
                                addFriend.writeInt(10);
                                addFriend.writeAsciiString(groupName);
                                addFriend.writeInt(0);
                                addFriend.writeInt(0);
                                addFriend.writeInt(c.getPlayer().getAccountID());
                                addFriend.writeAsciiString(c.getPlayer().getName(), 15);
                                addFriend.writeZeroBytes(272);
                                addFriend.write(1);
                                addFriend.write(0);
                                addFriend.write(1);
                                addFriend.write(0);
                                addFriend.write(0);
                                addFriend.write(0);
                                addFriend.write(0);
                                c.announce(addFriend.getPacket());
                                c.announce(BuddyListPacket.BuddyMess(FriendOperationMode.FriendRes_SetFriend_Done.getValue(), charWithId.getName()));
                            }
                        } else {
                            c.announce(FriendOperationMode.FriendRes_SetFriend_UnknownUser.getPacket());
                        }
                    } catch (SQLException e) {
                        System.err.println("SQL THROW" + e);
                    }
                }
                break;
            }
            case FriendReq_AcceptFriend: { // 接受普通好友邀請
                int otherCid = slea.readInt();
                BuddylistEntry ble = buddylist.get(otherCid);
                if (!buddylist.isFull() && ble != null && !ble.isVisible()) {
                    int channel = WorldFindService.getInstance().findChannel(otherCid);
                    buddylist.put(new BuddylistEntry(ble.getName(), otherCid, "未指定群組", channel, true));
                    c.announce(BuddyListPacket.updateBuddylist(buddylist.getBuddies(), FriendOperationMode.FriendRes_LoadAccountIDOfCharacterFriend_Done.getValue()));
                    notifyRemoteChannel(c, channel, otherCid, "未指定群組", 添加好友);
                } else {
                    c.announce(FriendOperationMode.FriendRes_SetFriend_FullMe.getPacket());
                }
                break;
            }
            case FriendReq_AcceptAccountFriend:  // 接受賬號好友邀請
                break;
            case FriendReq_DeleteFriend: { // 刪除普通好友
                int otherCid = slea.readInt();
                BuddylistEntry blz = buddylist.get(otherCid);
                if (blz != null && blz.isVisible()) {
                    notifyRemoteChannel(c, WorldFindService.getInstance().findChannel(otherCid), otherCid, blz.getGroup(), 刪除好友);
                }
                buddylist.remove(otherCid);
                c.announce(BuddyListPacket.updateBuddylist(buddylist.getBuddies(), FriendOperationMode.FriendRes_LoadAccountIDOfCharacterFriend_Done.getValue()));
                break;
            }
            case FriendReq_DeleteAccountFriend:  // 刪除賬號好友
//        } else if (mode == 0x06) { //擴充好友數量 在好友目錄中添加5人需要消耗5萬楓幣。你要擴充好友目錄嗎？
//            int capacity = c.getPlayer().getBuddyCapacity();
//            if (capacity >= 100 || c.getPlayer().getMeso() < 50000) {
//                c.getPlayer().dropMessage(1, "楓幣不足，或已擴充達到上限。包括基本格數在內，好友目錄中只能加入100個好友。您當前的好友數量為: " + capacity);
//            } else {
//                int newcapacity = capacity + 5;
//                c.getPlayer().gainMeso(-50000, true, true);
//                c.getPlayer().setBuddyCapacity((byte) newcapacity);
//            }
                break;
            case FriendReq_RefuseFriend:
            case FriendReq_RefuseAccountFriend: { // 賬號好友拒絕
                int otherCid = slea.readInt();
                //拒絕信息
                BuddylistEntry ble = buddylist.get(otherCid);
                if (ble == null) {
                    c.announce(FriendOperationMode.FriendRes_SetFriend_Unknown.getPacket());
                    return;
                }
                //     c.announce(BuddyListPacket.NoBuddy(otherCid, FriendOperationMode.FriendRes_DeleteFriend_Done.getValue(), opcode == FriendOperationMode.FriendReq_RefuseAccountFriend));
                MapleCharacter addChar = ChannelServer.getInstance(ble.getChannel()).getPlayerStorage().getCharacterById(ble.getCharacterId());
                addChar.getClient().announce(BuddyListPacket.BuddyMess(FriendOperationMode.FriendRes_DeleteFriend_Unknown.getValue(), c.getPlayer().getName()));
                buddylist.remove(otherCid);
                break;
            }
            case FriendReq_ConvertAccountFriend: //帳號好友轉換
                c.getPlayer().dropMessage(1, "暫時無法添加帳號整合好友，\r\n請添加普通好友。");
                c.sendEnableActions();
                break;
            case FriendReq_ModifyFriend: {//好友備註
                slea.readByte();
                int otherCid = slea.readInt();
//            int type = slea.readInt();//43108841 存在?
//            String Namer = slea.readMapleAsciiString();
//            String note = slea.readMapleAsciiString();
                slea.skip(4);
                slea.skip(slea.readShort());
                slea.skip(slea.readShort());
                BuddylistEntry ble = buddylist.get(otherCid);
                if (ble != null) {
                    c.announce(BuddyListPacket.updateBuddyNamer(ble, FriendOperationMode.FriendRes_NotifyChange_FriendInfo.getValue()));
                }
                break;
            }
        }
    }

    private static void notifyRemoteChannel(MapleClient c, int remoteChannel, int otherCid, String group, BuddyOperation operation) {
        MapleCharacter player = c.getPlayer();
        if (remoteChannel > 0) {
            WorldBuddyService.getInstance().buddyChanged(otherCid, player.getId(), player.getName(), c.getChannel(), operation, group);
        }
    }

    private static final class CharacterIdNameBuddyCapacity extends CharacterNameAndId {

        private final int buddyCapacity;

        public CharacterIdNameBuddyCapacity(int id, String name, String group, int buddyCapacity, boolean visible) {
            super(id, name, group, visible);
            this.buddyCapacity = buddyCapacity;
        }

        public int getBuddyCapacity() {
            return buddyCapacity;
        }
    }
}
