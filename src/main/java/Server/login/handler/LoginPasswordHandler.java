package Server.login.handler;

import Client.MapleClient;
import Client.MapleEnumClass;
import Handler.Handler;
import Opcode.Headler.InHeader;
import Packet.LoginPacket;
import Packet.MaplePacketCreator;
import Server.login.LoginServer;
import Server.login.LoginWorker;
import connection.packet.Login;
import tools.Pair;
import tools.StringUtil;
import tools.data.MaplePacketReader;

import java.util.Calendar;


public class LoginPasswordHandler {
    @Handler(op = InHeader.CTX_ENTER_ACCOUNT)
    public static void handlePacket(MaplePacketReader slea, MapleClient c) {
        int[] bytes = new int[6];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = slea.readByteAsInt();
        }
        StringBuilder sps = new StringBuilder();
        for (int aByte : bytes) {
            sps.append(StringUtil.getLeftPaddedStr(Integer.toHexString(aByte).toUpperCase(), '0', 2));
            sps.append("-");
        }
        String macData = sps.toString();
        macData = macData.substring(0, macData.length() - 1);
        c.setMac(macData);
        slea.skip(16);
        String login;
        String pwd;
        login = slea.readMapleAsciiString();
        pwd = slea.readMapleAsciiString();

        boolean isIpBan = c.hasBannedIP();
        boolean isMacBan = c.hasBannedMac();
        boolean isBanned = isIpBan || isMacBan;
        c.setTempInfo(login, pwd, isBanned);

        if (isBanned) {
            c.clearInformation();
            c.announce(MaplePacketCreator.serverNotice(1, "您的賬號已被封停！"));
            c.announce(LoginPacket.getLoginFailed(MapleEnumClass.AuthReply.GAME_DEFINITION_INFO));
            return;
        }
        Login(c);
    }

    public static void Login(MapleClient c) {
        MapleEnumClass.AuthReply loginok;
        String login = c.getTempInfo().getLeft();
        String pwd = c.getTempInfo().getMid();
        Boolean isBanned = c.getTempInfo().getRight();
        loginok = c.login(login, pwd, isBanned, false);
        if ((loginok.is(MapleEnumClass.AuthReply.GAME_ACCOUNT_BANNED) || isBanned) && !c.isGm()) {
            c.clearInformation();
            Calendar tempbannedCalendar = c.getTempBanCalendar();
            long tempbannedTill = tempbannedCalendar == null ? -1 : tempbannedCalendar.getTimeInMillis();
            if (tempbannedTill != -1 && tempbannedTill < System.currentTimeMillis()) {
                tempbannedTill = -1;
            }
            int banType = c.getBanType();
            String banreason;
            banreason = "帳號被封禁";
            c.announce(LoginPacket.getLoginFailedBan(tempbannedTill, banType == 0 ? 1 : banType, banreason));
        } else if (!loginok.is(MapleEnumClass.AuthReply.GAME_LOGIN_SUCCESSFUL)) {
            c.clearInformation();
            c.announce(LoginPacket.getLoginFailed(loginok));
        } else if (c.getGender() == 10 || c.getSecondPassword() == null) {
            c.updateLoginState(MapleClient.ENTERING_PIN);
            c.write(Login.sendChooseGender());
        } else {
            c.loginAttempt = 0;
            c.updateMacs();
            LoginWorker.registerClient(c, false);
        }
    }

    public static void handlerAuthKey(MaplePacketReader slea, MapleClient c) {
        String key = slea.readMapleAsciiString();
        Pair<String, Integer> clientinfo = LoginServer.getLoginAuthKey(key, false);
        c.login(clientinfo.getLeft(), "", false, true);
        c.loginAttempt = 0;
        LoginWorker.registerClient(c, true);
    }

    public static void specLogin(String login, String pwd, MapleClient c) {
        boolean isIpBan = c.hasBannedIP();
        boolean isMacBan = c.hasBannedMac();
        boolean isBanned = isIpBan || isMacBan;
        c.setTempInfo(login, pwd, isBanned);

        if (isBanned) {
            c.clearInformation();
            c.announce(MaplePacketCreator.serverNotice(1, "您的賬號已被封停！"));
            c.announce(LoginPacket.getLoginFailed(MapleEnumClass.AuthReply.GAME_DEFINITION_INFO));
            return;
        }
        Login(c);
    }
}
