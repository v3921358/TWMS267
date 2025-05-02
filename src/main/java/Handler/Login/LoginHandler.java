package Handler.Login;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.MapleEnumClass;
import Config.configs.ServerConfig;
import Config.constants.GameConstants;
import Config.constants.enums.SecurityCheckType;
import Handler.Handler;
import Net.NetRun;
import Opcode.Headler.OutHeader;
import Packet.LoginPacket;
import Packet.MaplePacketCreator;
import Plugin.provider.MapleDataProviderFactory;
import Server.channel.ChannelServer;
import Server.login.LoginServer;
import Server.login.LoginWorker;
import Server.login.handler.LoginPasswordHandler;
import connection.InPacket;
import connection.OutPacket;
import connection.packet.Login;
import connection.packet.MapLoadable;
import SwordieX.enums.WorldId;
import SwordieX.world.World;
import tools.CRC32;
import tools.Pair;
import tools.StringUtil;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static Opcode.Headler.InHeader.*;

public class LoginHandler {

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Handler(op = CP_ClientFileLog)
    public static OutPacket clientLog(MapleClient c, InPacket inPacket) {
        OutPacket pong = new OutPacket(OutHeader.LP_AliveReq);
        return pong;
    }

    @Handler(op = CP_WAIT_FOR_LOAD_FILE_LIST)
    public static void LoadWait(MapleClient c, InPacket inPacket) {
        c.write(Login.sendWZCheckList()); // 52
    }

    @Handler(op = CP_v255_163)
    public static void handleAliveAck(MapleClient c, InPacket inPacket) {
        c.pongReceived();
    }

    @Handler(op = CP_SecurityPacket)
    public static void handleSecurityPacket(MapleClient c, InPacket inPacket) {
        SecurityCheckType memoryHash = GameConstants.getSecurityMemoryHash(inPacket.decodeByte());
        switch (memoryHash) {
            case SECURITYPACKET_FULL_MEMORY_CHECK: {
                int crc = inPacket.decodeInt();
                int guardPoint = inPacket.decodeInt();
                break;
            }
            case SECURITYPACKET_CHECK_CLIENT_INTEGRITY: {
                int clientVersion = inPacket.decodeInt();
                int majorVersion = clientVersion / 100;
                int minorVersion = clientVersion % 100;
                int crc1 = inPacket.decodeInt();
                int crc2 = inPacket.decodeInt();
                int crc3 = inPacket.decodeInt();
                int rnd = inPacket.decodeInt() ^ CRC32.getTable(27);
                int dr0 = inPacket.decodeInt() ^ rnd ^ CRC32.getTable(127);
                int dr1 = inPacket.decodeInt() ^ rnd ^ CRC32.getTable(6);
                int dr2 = (inPacket.decodeInt() + CRC32.getTable(4)) ^ rnd;
                int dr3 = (inPacket.decodeInt() - CRC32.getTable(210)) ^ rnd;
                int dr6 = inPacket.decodeInt();
                int dr7 = inPacket.decodeInt();
                break;
            }
            case SECURITYPACKET_CLIENT_VERSION: {
                int clientVersion = inPacket.decodeInt();
                int majorVersion = clientVersion / 100;
                int minorVersion = clientVersion % 100;
            }
            case SECURITYPACKET_NGS_CLIENTTOSERVER: { // 服務端發hash 7 時收到
                //int len = inPacket.decodeInt();
                break;
            }
            default:
                break;
        }
    }

    @Handler(op = CP_PermissionRequest)
    public static void handlePermissionRequest(MapleClient c, InPacket inPacket) {
        c.write(Login.sendSecurityPacket());      // 34
        c.write(Login.sendServerValues());        // 73
        c.write(Login.sendServerEnvironment());   // 74
        c.write(Login.sendFileCheckList()); // 51
        c.write(Login.sendSecurityCodePacket());  // 35
        c.write(Login.sendSecurityCodePacket_Attach());  // 39
        c.write(Login.AliveReq()); // 28
    }

    @Handler(ops = CP_AliveAck)
    public static void AliveAck(MapleClient c, InPacket inPacket) {
        scheduler.schedule(() -> {
            OutPacket outPacket = new OutPacket(OutHeader.LP_AliveReq);
            outPacket.encodeLong(System.currentTimeMillis());
            c.write(outPacket);
        }, 13, TimeUnit.SECONDS);
    }

    @Handler(op = CP_GetGuidKey)
    public static void getGuildKeyEncry(MapleClient c, InPacket inPacket) {
        OutPacket outPacket = new OutPacket(OutHeader.LP_EncryGuildKey);
        outPacket.encodeArr("4A 00 D0 C0 C4 C8 D4 D8 D8 C8 DC C4 D8 E0 91 DC 85 D8 85 85 89 95 89 89 C0 E4 91 91 DC 95 D8 99 C0 E4 CC 91 99 91 D4 E0 E4 95 85 D0 8D C0 D4 D8 D0 8D 95 DC 89 C4 DC E4 CC DC D0 E0 91 DC D8 C8 95 89 E0 C4 D8 DC CC DC E0 8D 99 CC");
        c.write(outPacket);
    }

    @Handler(op = CP_BEGIN_USER_DUMMY)
    public static void getNowTime(MapleClient c, InPacket inPacket) {
        OutPacket outPacket = new OutPacket(OutHeader.LP_ServerTimeResult);
        outPacket.encodeLong(System.currentTimeMillis());
        c.write(outPacket);
    }

    @Handler(op = CP_CHECK_CLIENT_LOAD_DATA)
    public static void sendLoadCheck(MapleClient c, InPacket inPacket) {
        OutPacket outPacket = new OutPacket(OutHeader.CLIENT_ALIVE);
        c.write(outPacket);
    }

    /**
     * 熱修復文件的發送即 Data.wz
     *
     * @return 封包
     * @updateVersion:262.1
     * @Check_Date:2024/6/26
     */
    @Handler(op = CP_APPLY_HOTFIX)
    public static void handleCheckHotfix(MapleClient c, InPacket inPacket) {
        String sha1 = MapleDataProviderFactory.getHotfixCheck();

//        byte[] checkdata = inPacket.decodeArr(4);
//        Collections.reverse(Collections.singletonList(checkdata));
//        String check = HexTool.toString(checkdata).replaceAll(" ", "");
//
//        if (sha1 == null || sha1.startsWith(check.toLowerCase())) {
//            c.write(Login.sendHotfix(0, 0, new byte[0], ""));
//        } else {
//            int chunkSize = 65536;
//            byte[] data;
//            try {
//                RandomAccessFile f = new RandomAccessFile(MapleDataProviderFactory.HOTFIX_DATA_PATH, "r");
//                data = new byte[(int) f.length()];
//                f.readFully(data);
//            } catch (IOException e) {
//                data = new byte[0];
//            }
//            for (int i = 0; i < data.length; i += chunkSize) {
//                int end = Math.min(data.length, i + chunkSize);
//                byte[] chunk = Arrays.copyOfRange(data, i, end);
//                c.write(Login.sendHotfix(data.length, i / chunkSize, chunk, sha1.substring(0, 8)));
//            }
//        }
        String clientDataWzHash = Integer.toHexString(inPacket.decodeInt());
        File dataWz = new File(MapleDataProviderFactory.HOTFIX_DATA_PATH);
        String serverDataWzHash = "";
        int chunkSize = 65536;
        if (dataWz.exists()) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                byte[] data = Files.readAllBytes(dataWz.toPath());
                digest.update(data, 0, data.length);
                serverDataWzHash  = new BigInteger(1, digest.digest()).toString(16);
                String byte1 = serverDataWzHash.substring(0,2);
                String byte2 = serverDataWzHash.substring(2,4);
                String byte3 = serverDataWzHash.substring(4,6);
                String byte4 = serverDataWzHash.substring(6,8);
                serverDataWzHash = byte4 + byte3 + byte2 + byte1;
                if (!serverDataWzHash.equals(clientDataWzHash)) {
                    for (int i = 0; i < data.length; i += chunkSize) {
                        int end = Math.min(data.length, i + chunkSize);
                        byte[] chunk = Arrays.copyOfRange(data, i, end);
                        c.write(Login.sendHotfix(data.length, i / chunkSize, chunk, serverDataWzHash));
                    }
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            c.write(Login.sendHotfix(0,0, new byte[0], ""));
        }
    }

    @Handler(op = CP_ClientUnkLoginLog)
    public static void handleClientUnkLoginLog(MapleClient c, InPacket inPacket) {
        byte unk = inPacket.decodeByte();
        byte unk1 = inPacket.decodeByte();
        List<Integer> val = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            val.add(inPacket.decodeInt());
        }
    }

    @Handler(op = CTX_REGIST)
    public static void handleSetGenderRequest(MapleClient c, InPacket inPacket) {
        String username = inPacket.decodeString();
        String secondPassword = inPacket.decodeString();
        byte gender = inPacket.decodeByte();
        if (!c.getAccountName().equals(username) || c.getSecondPassword() != null || gender < 0 || gender > 1) {
            c.write(Login.sendSetGender(false));
            return;
        }
        if (secondPassword.length() >= 5) {
            c.setGender(gender);
            c.setSecondPassword(secondPassword);
            c.updateSecondPassword();
            c.write(Login.sendSetGender(true));
            c.announce(LoginPacket.getLoginFailed(MapleEnumClass.AuthReply.GAME_PROTOCOL_INFO));
        } else {
            c.write(Login.sendSetGender(false));
        }
    }

    @Handler(op = CTX_ENTER_ACCOUNT)
    public static void handleCheckLogin(MapleClient c, InPacket inPacket) {
        int[] bytes = new int[6];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = inPacket.decodeByte();
        }
        StringBuilder sps = new StringBuilder();
        for (int aByte : bytes) {
            sps.append(StringUtil.getLeftPaddedStr(Integer.toHexString(aByte).toUpperCase(), '0', 2));
            sps.append("-");
        }
        String macData = sps.toString();
        macData = macData.substring(0, macData.length() - 1);
        c.setMac(macData);
        inPacket.decodeArr(16);
        String login;
        String pwd;
        login = inPacket.decodeString();
        pwd = inPacket.decodeString();

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
        LoginPasswordHandler.Login(c);
    }
    
    @Handler(op = LP_SELECT_CHANNEL)
    public static void handleWorldStatusRequest(MapleClient c, InPacket inPacket) {
        inPacket.decodeByte();
        int n = inPacket.decodeInt();
        /*
         * 0 - 沒有消息
         * 1 - 當前世界連接數量較多，這可能會導致登錄遊戲時有些困難。
         * 2 - 當前世界上的連接已到達最高限制。請選擇別的伺服器進行遊戲或稍後再試。
         */
        int numPlayer = LoginServer.getUsersOn();
        int userLimit = LoginServer.getUserLimit();
        WorldId server = WorldId.getByVal(ServerConfig.WORLD_ID);
        if (numPlayer >= userLimit) {
            c.write(Login.sendWorldStatusResult(server.getVal(), 2));
        } else if (numPlayer >= userLimit * .8) {
            c.write(Login.sendWorldStatusResult(server.getVal(), 1));
        } else {
            c.write(Login.sendWorldStatusResult(server.getVal(), 0));
        }
    }

    /**
     * TCP / SELECT CHARACTER
     *
     * @return 封包
     * @updateVersion:262.1
     * @Check_Date:2024/6/26
     */
    @Handler(op = CP_SELECT_WORLD_LOGIN)
    public static void handleSelectWorld(MapleClient c, InPacket inPacket) {
        inPacket.decodeByte();
        int server = inPacket.decodeByte() & 0xFF; //V.106修改
        int channel = inPacket.decodeByte() + 1;
        boolean useKey = inPacket.decodeByte() == 1;
        if (useKey) {
            inPacket.decodeByte();
            Pair<String, Integer> loginAuthKey = LoginServer.getLoginAuthKey(inPacket.decodeString(), true);
            if (loginAuthKey == null) {
                c.getSession().close();
                return;
            }
            c.login(loginAuthKey.getLeft(), "", false, true);
            c.loginAttempt = 0;
            LoginWorker.registerClient(c, true);
        } else if (!c.isLoggedIn()) {
            c.getSession().close();
            return;
        }

        ChannelServer toch = ChannelServer.getInstance(channel);
        if (!NetRun.Server.getInstance().isOnline() || toch == null) { //TODOO: MULTI WORLDS
            c.announce(LoginPacket.getLoginFailed(MapleEnumClass.AuthReply.GAME_CONNECTION_BUSY)); //cannot process so many
            return;
        }

        System.out.println("[ NOTICE ] 嘗試連接IP : " + c.getSessionIPAddress() + " 成功連結: " + server + " 頻道: " + channel);

        c.setChannel(channel);
        List<MapleCharacter> chars = c.loadCharacters(server);
        if (chars != null && ChannelServer.getInstance(channel) != null) {
            c.setWorldId(server);
            c.setChannel(channel);
            c.write(Login.sendSetClientKey()); // 13
            c.write(Login.sendSetPhysicalWorldID(server)); // 14
            c.write(Login.sendSetPhysicalWorldAttach("TW")); // 617
            c.announce(LoginPacket.getCharList(chars, c.getAccCharSlots(), c)); // 16
        } else {
            c.getSession().close();
        }
    }

    @Handler(op = CP_PrivateServerPacket)
    public static void handlePrivateServerPacket(MapleClient c, InPacket inPacket) {
        int pid = inPacket.decodeInt();
        c.write(Login.sendPrivateServerPacket(pid)); // 角色選擇清單驗證
    }


    @Handler(op = CTX_WORLD_RETURN_INFO)
    public static void handleWorldListRequest(MapleClient c, InPacket inPacket) {
        for (World world : NetRun.Server.getInstance().getWorlds()) {
            c.write(Login.sendWorldInformation(world, LoginServer.getLoad(), null));
        }
        c.write(Login.sendWorldInformationEnd());
    }

    @Handler(op = CTX_OUT_WORLD)
    public static void handleLogoutWorld(MapleClient c, InPacket inPacket) {
        c.write(MapLoadable.setMapTaggedObjectVisisble(LoginServer.getRandomWorldSelectBG()));
    }

    @Handler(op = CTX_IN_FIELD_UNLOCK)
    public static void RequestUserChangeMapSec(MapleClient c, InPacket inPacket) {
        OutPacket say = new OutPacket(OutHeader.CTX_IN_FIELD_UNLOCK_PACKET);
        say.encodeByte(1); // [01]
        c.write(say);
    }

}
