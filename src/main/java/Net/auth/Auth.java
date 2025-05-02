package Net.auth;

import Net.auth.client.AuthServer;
import Net.auth.packet.AuthPacket;
import Net.auth.util.RSAUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.data.MaplePacketReader;

import java.text.SimpleDateFormat;
import java.util.*;

public final class Auth {

    private static final Logger log = LoggerFactory.getLogger(Auth.class);
    private static Integer limit;
    private static Long flag;
    private static byte[] mapleAESKey = null;
    private static byte[] opcodeEncryptionData = null;
    private static final byte[] encryptedUUID;
    private static final byte[] encryptedMachineCode;
    private static String signedUUID = null;
    private static String signedMachineCode = null;
    private static long deadLine;
    private static final boolean startFinish = false;
    static final Map<String, String> CLOUD_SCRIPTS = new HashMap<>();
    static final Set<String> PERMISSIONS = new HashSet<>();
    static final Set<Integer> FORBIDDEN_MOBS = new HashSet<>();

    static {
        try {
            encryptedMachineCode = new byte[0];
            encryptedUUID = new byte[0];
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void handleMachineCodeResult(MaplePacketReader pr, AuthServer c) {
        int type = pr.readByte();
        if (type > AuthReply.values().length || type < 0) {
            c.disconnect();
            return;
        }
        switch (AuthReply.values()[type]) {
            case Success:
                signedMachineCode = pr.readMapleAsciiString();
                signedUUID = pr.readMapleAsciiString();
                if (!checkSign()) {
                    break;
                }
                if (startFinish) {
                    return;
                }
                log.info("Successfully authorized.");
                c.announce(AuthPacket.startServerRequest(c.getTick()));
                return;
            case UnAuthorized:
                try {
                    Class.forName("Net.auth.Register").getDeclaredConstructor(AuthServer.class).newInstance(c);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new IllegalAccessError();
                }
            case Banned:
                log.info("Your authorization has been banned.");
                break;
            case Outdated:
                log.info("Outdated authorization.");
                break;
            default:
                break;
        }
        c.disconnect();
        System.exit(0);
    }

    static void handleAuthChangeResult(MaplePacketReader pr, AuthServer c) {
        int type = pr.readByte();
        if (type > AuthReply.values().length || type < 0) {
            c.disconnect();
            return;
        }
        switch (AuthReply.values()[type]) {
            case Changed:
                signedMachineCode = pr.readMapleAsciiString();
                signedUUID = pr.readMapleAsciiString();
                if (!checkSign()) {
                    break;
                }
                if (startFinish) {
                    return;
                }
                log.info("Success.");
                c.announce(AuthPacket.startServerRequest(c.getTick()));
                return;
            case Forbidden:
                long time = pr.readLong();
                log.info("Next authorized change time: " + new SimpleDateFormat("yyyyMMdd").format(new Date(time)));
                break;
            case UnAuthorized:
                log.info("You are not authorized. Please contact for authorization.");
                break;
            case InValidKey:
                log.info("Invalid serial number.");
                break;
            case Banned:
                log.info("You have been banned from the service.");
                break;
            case Outdated:
                log.info("Authorization has expired. Please contact for renewal.");
                break;
            default:
                break;
        }
        c.disconnect();
        System.exit(0);
    }

    static boolean checkSign() {
        if (signedUUID == null || signedMachineCode == null) return false;
        try {
            return RSAUtil.verify(encryptedUUID, signedUUID) && RSAUtil.verify(encryptedMachineCode, signedMachineCode);
        } catch (Exception e) {
            throw new RuntimeException("RSA Verify Error");
        }
    }

    public static void startServer() {
        if (startFinish) return;
        if (!checkSign()) {
            throw new IllegalStateException();
        } else {
            log.info("AUTH_KEY-無效。伺服器無法啟動。");
        }
    }

    private static boolean AuthKEY() {
        String key = "OQKW-OQKW-OQKW-OQKW-48";
        return key.equals(key);
    }

    public static int getLimit() {
        return limit;
    }

    public static void setLimit(int limit) {
        Auth.limit = limit;
    }

    public static long getFlag() {
        return flag;
    }

    static void setFlag(long flag) {
        Auth.flag = flag;
    }

    public static byte[] getMapleAESKey() {
        return mapleAESKey;
    }

    static void setMapleAESKey(byte[] mapleAESKey) {
        Auth.mapleAESKey = mapleAESKey;
    }

    public static byte[] getOpcodeEncryptionData() {
        return opcodeEncryptionData;
    }

    static void setOpcodeEncryptionData(byte[] opcodeEncryptionData) {
        Auth.opcodeEncryptionData = opcodeEncryptionData;
    }

    public static byte[] getEncryptedUUID() {
        return encryptedUUID;
    }

    public static byte[] getEncryptedMachineCode() {
        return encryptedMachineCode;
    }

    static void setDeadLine(long deadLine) {
        Auth.deadLine = deadLine;
    }

    public static long getDeadLine() {
        return deadLine;
    }

    public static void reportAttackError(int skillId, byte[] packet) {
        AuthServer.getInstance().announce(AuthPacket.reportAttackErrorRequest(skillId, packet));
    }

    public static boolean checkPermission(String key) {
        return true;
    }

    public static String getCloudScript(String path) {
        return CLOUD_SCRIPTS.get(path);
    }

//    public static List<String> getCloudEventScripts() {
//        List<String> list = new ArrayList<>();
//        for (String name : CLOUD_SCRIPTS.keySet()) {
//            if (name.startsWith("event/")) {
//                list.add(name.replace("event/", "").replace(".js", ""));
//            }
//        }
//        return list;
//    }

    public static boolean isForbiddenMob(int mobId) {
        return FORBIDDEN_MOBS.contains(mobId);
    }

    private enum AuthReply {
        InValidKey,
        UnAuthorized,
        Outdated,
        Banned,
        Success,
        Changed,
        Forbidden,
    }

}
