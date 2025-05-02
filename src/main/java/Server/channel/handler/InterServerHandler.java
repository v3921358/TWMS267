package Server.channel.handler;

import Client.MapleAntiMacro;
import Client.MapleCharacter;
import Client.MapleClient;
import Config.constants.ServerConstants;
import Handler.warpToGameHandler;
import Net.server.ShutdownServer;
import Net.server.maps.FieldLimitType;
import Packet.LoginPacket;
import Packet.MaplePacketCreator;
import Server.ServerType;
import Server.auction.AuctionHandler;
import Server.auction.AuctionServer;
import Server.cashshop.CashShopServer;
import Server.cashshop.handler.CashShopOperation;
import Server.channel.ChannelServer;
import Server.login.LoginServer;
import Server.world.CharacterTransfer;
import Server.world.PlayerBuffStorage;
import Server.world.World;
import Server.world.WorldMessengerService;
import Server.world.messenger.MapleMessengerCharacter;
import connection.OutPacket;
import com.alibaba.druid.sql.visitor.functions.Ascii;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.DateUtil;
import tools.Quadruple;
import tools.StringUtil;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.io.IOException;
import java.util.Date;
import java.util.List;


public class InterServerHandler {

    private static final Logger log = LoggerFactory.getLogger(InterServerHandler.class);

    public static void enterCS(MapleClient c, MapleCharacter chr) {
        if (!chr.isAlive() || chr.isInJailMap() || chr.isBanned() || MapleAntiMacro.isAntiNow(chr.getName()) || chr.checkEvent()) {
            String msg = "無法進入商城，請稍後再試。";
            if (!chr.isAlive()) {
                msg = "現在不能進入商城。";
            } else if (chr.isInJailMap()) {
                msg = "在這個地方無法使用此功能。";
            } else if (MapleAntiMacro.isAntiNow(chr.getName())) {
                msg = "被使用測謊機時無法操作。";
            }
            c.getPlayer().dropMessage(1, msg);
            c.sendEnableActions();
            return;
        }
        ChannelServer ch = ChannelServer.getInstance(c.getChannel());
        chr.changeRemoval();
        if (chr.getMessenger() != null) {
            MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(chr);
            WorldMessengerService.getInstance().leaveMessenger(chr.getMessenger().getId(), messengerplayer);
        }
        chr.updataEnterShop(true);
        chr.updateTodayDate();
        PlayerBuffStorage.addBuffsToStorage(chr.getId(), chr.getAllBuffs());
        PlayerBuffStorage.addCooldownsToStorage(chr.getId(), chr.getCooldowns());
        World.ChannelChange_Data(new CharacterTransfer(chr), chr.getId(), -10);
        ch.removePlayer(chr);
        c.updateLoginState(MapleClient.CHANGE_CHANNEL, c.getSessionIPAddress());
//        chr.saveToCache();
        chr.saveToDB(false, false);
        chr.getMap().userLeaveField(chr);
        c.announce(MaplePacketCreator.getChannelChange(c, CashShopServer.getPort()));

        c.announce(c.getEncryptOpcodesData(ServerConstants.OpcodeEncryptionKey));

        c.setPlayer(null);
        c.setReceiving(false);
    }

    public static void EnterAuction(MapleClient c, MapleCharacter player) {
        AuctionServer as = AuctionServer.getInstance();
        if (player.hasBlockedInventory() || c.getChannelServer() == null || as == null) {
            c.announce(MaplePacketCreator.serverBlocked(2));
            return;
        }
        if (!player.isAlive() || player.isInJailMap() || player.isBanned() || MapleAntiMacro.isAntiNow(player.getName()) || player.checkEvent()) {
            String msg = "無法進入拍賣場，請稍後再試。";
            if (!player.isAlive()) {
                msg = "現在不能進入拍賣場.";
            } else if (player.isInJailMap()) {
                msg = "在這個地方無法使用此功能.";
            } else if (MapleAntiMacro.isAntiNow(player.getName())) {
                msg = "被使用測謊機時無法操作。";
            }
            c.getPlayer().dropMessage(1, msg);
            c.sendEnableActions();
            return;
        }
        player.changeRemoval();
        as.getPlayerStorage().registerPendingPlayer(new CharacterTransfer(player), player.getId());
        player.initialSpawnPoint();
        player.fixOnlineTime();
        player.updateOneQuestInfo(27040, "0", DateUtil.getFormatDate(new Date(), "yyMMddHHmmss"));
        c.updateLoginState(5);
        PlayerBuffStorage.addBuffsToStorage(player.getId(), player.getAllBuffs());
        PlayerBuffStorage.addCooldownsToStorage(player.getId(), player.getCooldowns());
        World.ChannelChange_Data(new CharacterTransfer(player), player.getId(), -10);
        c.getChannelServer().removePlayer(player);
        c.updateLoginState(MapleClient.CHANGE_CHANNEL, c.getSessionIPAddress());
//        chr.saveToCache();
        player.saveToDB(false, false);
        player.getMap().userLeaveField(player);
        c.announce(MaplePacketCreator.getChannelChange(c, AuctionServer.getInstance().getPort()));
        c.setPlayer(null);
        c.setReceiving(false);
        c.announce(CtxPacket((short) 27, "01 CA 50 68 2A 53 22"));
    }

    public static void Loggedin(MaplePacketReader slea, MapleClient c, ServerType type) {
        if (ShutdownServer.getInstance().isShutdown()) {
            c.getSession().close();
            return;
        }
        slea.readInt();
        int accountId = slea.readInt();
        int playerid = slea.readInt();
        byte[] code = slea.read(24);
        CharacterTransfer transfer = null;
        try {
            transfer = CashShopServer.getPlayerStorage().getPendingCharacter(playerid);
        } catch (IOException e) {
            log.error("讀取臨時角色失敗", e);
        }

        if (ServerConstants.MapleMajor > 198 && ServerConstants.MapleMajor < 215) {
            final byte[] desKey = new byte[24];
            byte[] bCharacterID = String.valueOf(playerid).getBytes();
            System.arraycopy(bCharacterID, 0, desKey, 0, Math.min(desKey.length, bCharacterID.length));
            if (bCharacterID.length < desKey.length) {
                System.arraycopy(code, 0, desKey, bCharacterID.length, desKey.length - bCharacterID.length);
            }
            c.announce(c.getEncryptOpcodesData(desKey));
        }
        if (type.equals(ServerType.CashShopServer)) {
            if (transfer != null) {
                CashShopOperation.EnterCS(transfer, c);
            }
            return;
        }
        if (type.equals(ServerType.AuctionServer)) {
            try {
                transfer = AuctionServer.getInstance().getPlayerStorage().getPendingCharacter(playerid);
            } catch (IOException e) {
                log.error("讀取臨時角色失敗", e);
            }
            if (transfer != null) {
                AuctionHandler.EnterAuction(transfer, c);
            }
            return;
        }

        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            try {
                transfer = cserv.getPlayerStorage().getPendingCharacter(playerid);
            } catch (IOException e) {
                log.error("讀取臨時角色失敗", e);
            }
            if (transfer != null) {
                c.setChannel(cserv.getChannel());
                break;
            }
        }

        MapleCharacter player = null;
        int[] bytes = new int[6];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = code[i];
        }
        StringBuilder sps = new StringBuilder();
        for (int aByte : bytes) {
            sps.append(StringUtil.getLeftPaddedStr(Integer.toHexString(aByte).toUpperCase(), '0', 2));
            sps.append("-");
        }
        String macData = sps.toString();
        macData = macData.substring(0, macData.length() - 1);
        boolean firstLoggedIn = true; //設置只有第1次登錄的提示開關
        if (transfer == null) { // Player isn't in storage, probably isn't CC
            Quadruple<String, String, Integer, String> ip = LoginServer.getLoginAuth(playerid);
            String s = c.getSessionIPAddress();
            if (ip == null || (!s.substring(s.indexOf('/') + 1).equals(ip.one) && !c.getMac().equals(macData))) {
                if (ip != null) {
                    LoginServer.putLoginAuth(playerid, ip.one, ip.two, ip.three, ip.four);
                } else {
                    c.getSession().close();
                    return;
                }
            }
            c.setTempIP(ip.two);
            c.setChannel(ip.three);
            try {
                player = MapleCharacter.loadCharFromDB(playerid, c, true);
                player.setLogintime(System.currentTimeMillis());
            } catch (Exception e) {
                c.dropMessage("加載角色數據出錯，角色ID:" + playerid + ", 請聯繫管理員解決問題。");
                log.error("加載角色數據出錯，角色ID:" + playerid, e);
            }
        } else {
            player = MapleCharacter.ReconstructChr(transfer, c, true);
            firstLoggedIn = false;
        }
        if (player == null) {
            c.dropMessage("加載角色出錯，角色為空");
            c.getSession().close();
            return;
        }
        long sessionId = slea.readLong();
        ChannelServer channelServer = c.getChannelServer();
        c.setPlayer(player);
        c.setSessionId(sessionId);
        if (sessionId != c.getSessionId()) {
            c.disconnect(true, false);
            return;
        }
        c.setAccID(player.getAccountID());
        if (!c.CheckIPAddress()) { // Remote hack
            String msg = "檢測連接地址不合法 服務端斷開這個連接 [角色ID: " + player.getId() + " 名字: " + player.getName() + " ]";
            c.getSession().close();
            log.info(msg);
            return;
        }
        if (!player.getMap().canEnterField(player.getId())) {
            player.dropMessage(1, "親愛滴玩家：地圖已經開啟防搶圖模式，現在無法進入。");
            if (!player.isIntern()) {
                c.getSession().close();
                return;
            }
        }
        int state = c.getLoginState();
        boolean allowLogin = false;
        String allowLoginTip = null;
        if (state == MapleClient.LOGIN_SERVER_TRANSITION || state == MapleClient.CHANGE_CHANNEL || state == MapleClient.LOGIN_NOTLOGGEDIN) {
            List<String> charNames = c.loadCharacterNames(c.getWorldId());
            allowLogin = !World.isCharacterListConnected(charNames);
            if (!allowLogin) {
                allowLoginTip = World.getAllowLoginTip(charNames);
            }
        }
        //返回為 True 角色才能進入遊戲
        if (!allowLogin) {
            String msg = "檢測賬號下已有角色登陸遊戲 服務端斷開這個連接 [角色ID: " + player.getId() + " 名字: " + player.getName() + " ]\r\n" + allowLoginTip;
            c.setPlayer(null);
            c.getSession().close();
            log.info(msg);
            return;
        }
        c.updateLoginState(MapleClient.LOGIN_LOGGEDIN, c.getSessionIPAddress());
        channelServer.addPlayer(player);
        warpToGameHandler.Start(c);
        //TODO: 解進入遊戲先註解

    }

    public static void ChangeChannel(MaplePacketReader slea, MapleClient c, MapleCharacter chr) {
        if (chr == null || chr.hasBlockedInventory() || chr.isInBlockedMap() || FieldLimitType.MIGRATELIMIT.check(chr.getMap().getFieldLimit())) {
            c.sendEnableActions();
            return;
        }
        if (chr.isBanned()) {
            c.sendEnableActions();
            return;
        }
        if (MapleAntiMacro.isAntiNow(chr.getName())) {
            chr.dropMessage(5, "被使用測謊機時無法操作。");
            c.sendEnableActions();
            return;
        }
        int chc = slea.readByte() + 1;
        chr.changeChannel(chc);
    }

    public static void ChangePlayer(MaplePacketReader slea, MapleClient c) {
        // final String account = slea.readMapleAsciiString();
        if (c.getAccountName() == null) {
            c.disconnect(true, false);
            return;
        }
        char[] ss = new char[256];

        for (int i = 0; i < ss.length; ++i) {
            int f = (int) (Math.random() * 3.0D);
            if (f == 0) {
                ss[i] = (char) ((int) (65.0D + Math.random() * 26.0D));
            } else if (f == 1) {
                ss[i] = (char) ((int) (97.0D + Math.random() * 26.0D));
            } else {
                ss[i] = (char) ((int) (48.0D + Math.random() * 10.0D));
            }
        }
        String key = new String(ss);
        LoginServer.putLoginAuthKey(key, c.getAccountName(), c.getChannel());
        c.announce(LoginPacket.changePlayerKey(key));
    }

    public static void ExitAuction(MapleClient c, MapleCharacter player) {
        AuctionServer as = AuctionServer.getInstance();
        int channel = c.getChannel(); //角色要更換的頻道
        ChannelServer toch = ChannelServer.getInstance(channel); //角色從商城出來更換的頻道信息
        if (toch == null) {
            c.getSession().close();
            return;
        }
        //開始處理
        World.ChannelChange_Data(new CharacterTransfer(player), player.getId(), c.getChannel());
        as.getPlayerStorage().deregisterPlayer(player);
        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, c.getSessionIPAddress());
        c.announce(MaplePacketCreator.getChannelChange(c, toch.getChannel() + ChannelServer.getChannelStartPort())); //發送更換頻道的封包信息
        player.fixOnlineTime();
        c.disconnect(false, true);
        c.setPlayer(null);
        c.setReceiving(false);
    }

    public static OutPacket Packet(short header, Object... data) {
        OutPacket say = new OutPacket(header);
        for (Object obj : data) {
            if (obj instanceof Integer) {
                say.encodeInt((Integer) obj);
            } else if (obj instanceof Short) {
                say.encodeShort((Short) obj);
            } else if (obj instanceof Byte) {
                say.encodeByte((Byte) obj);
            } else if (obj instanceof String) {
                say.encodeString((String) obj);
            }

        }
        return say;
    }


    public static byte[] CtxPacket(short header, Object... data) {
        MaplePacketLittleEndianWriter PACKET = new MaplePacketLittleEndianWriter();
        PACKET.writeShort(header);
        for (Object obj : data) {
            if (obj instanceof Integer) {
                PACKET.writeInt((Integer) obj);
            } else if (obj instanceof Short) {
                PACKET.writeShort((Short) obj);
            } else if (obj instanceof Byte) {
                PACKET.write((Byte) obj);
            } else if (obj instanceof String) {
                PACKET.writeHexString((String) obj);
            } else if (obj instanceof Ascii) {
                PACKET.writeMapleAsciiString((String) obj);
            }

        }
        return PACKET.getPacket();
    }
}
