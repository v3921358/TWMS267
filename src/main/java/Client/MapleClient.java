package Client;

import Config.$Crypto.MapleAESOFB;
import Config.configs.ServerConfig;
import Config.constants.GameConstants;
import Config.constants.ServerConstants;
import Database.DatabaseLoader.DatabaseConnectionEx;
import Database.tools.SqlTool;
import Net.NetRun;
import Net.auth.Auth;
import Net.server.CharacterCardFactory;
import Net.server.ShutdownServer;
import Net.server.Timer;
import Net.server.commands.PlayerRank;
import Net.server.maps.MapleMap;
import Net.server.quest.MapleQuest;
import Net.server.shops.IMaplePlayerShop;
import Opcode.Headler.InHeader;
import Opcode.Headler.OutHeader;
import Packet.LoginPacket;
import Packet.MaplePacketCreator;
import Packet.UIPacket;
import Server.cashshop.CashShopServer;
import Server.cashshop.handler.BuyCashItemHandler;
import Server.channel.ChannelServer;
import Server.login.LoginServer;
import Server.login.handler.AutoRegister;
import Server.netty.MaplePacketDecoder;
import Server.world.*;
import Server.world.guild.MapleGuildCharacter;
import Server.world.messenger.MapleMessengerCharacter;
import connection.Packet;
import connection.packet.Login;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import nativeimage.Reflection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Pair;
import tools.Randomizer;
import tools.Triple;
import tools.data.MaplePacketLittleEndianWriter;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.script.ScriptEngine;
import java.awt.*;
import java.io.Serializable;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Reflection(publicConstructors = true, publicMethods = true, publicFields = true, scanPackage = "Client")
public class MapleClient implements Serializable {

    public static final byte LOGIN_NOTLOGGEDIN = 0, LOGIN_SERVER_TRANSITION = 1, LOGIN_LOGGEDIN = 2, CHANGE_CHANNEL = 3, ENTERING_PIN = 4, PIN_CORRECT = 5, LOGIN_CS_LOGGEDIN = 6;
    public static final AttributeKey<MapleClient> CLIENT_KEY = AttributeKey.newInstance("Client");
    private static final Logger log = LoggerFactory.getLogger(MapleClient.class);
    private static final long serialVersionUID = 9179541993413738569L;
    private final static Lock login_mutex = new ReentrantLock(true);
    private final transient Lock mutex = new ReentrantLock(true);
    private final transient Lock npc_mutex = new ReentrantLock();
    private final transient List<Integer> allowedChar = new LinkedList<>();
    private final transient Map<String, ScriptEngine> engines = new HashMap<>();
    private final Map<Integer, Pair<Integer, Short>> charInfo = new LinkedHashMap<>();
    private final List<String> proesslist = new ArrayList<>();
    public transient short loginAttempt = 0;
    private transient MapleAESOFB send, receive;
    private transient Channel session;
    private MapleCharacter player;
    private int channel = 1;
    private int accId = -1;
    private int worldId;
    private int birthday;
    private int charslots = Math.min(GameConstants.MAX_CHARS_SLOTS, ServerConfig.CHANNEL_PLAYER_MAXCHARACTERS); //可創建角色的數量
    private int cardslots = 3; //角色卡的數量
    private boolean loggedIn = false, serverTransition = false;
    private transient Calendar tempban = null;
    private String accountName;
    private boolean monitored = false, receiving = true;
    private int gmLevel, maplePoint;
    private byte greason = 1, gender = -1;
    private transient String mac = "00-00-00-00-00-00";
    private transient List<String> maclist = new LinkedList<>();
    private transient ScheduledFuture<?> idleTask = null;
    private transient String secondPassword, salt2, tempIP = ""; // To be used only on login
    private long lastNpcClick = 0, sessionId;
    private final byte loginattempt = 0;
    private Triple<String, String, Boolean> tempinfo = null;
    private final Map<Short, Short> encryptedOpcodes = new LinkedHashMap<>();
    private int sessionIdx;
    private String name;
    private final AtomicInteger aliveCheckCount = new AtomicInteger(0);
    private ScheduledFuture aliveCheckSchedule = null;
    private volatile Boolean disconnecting = false;
    private final Object disconnectLock = new Object();
    private static final Map<Short, Short> Opcodes = new LinkedHashMap<>();
    private static byte[] OpcodeEncryptPacket = null;
    private MapleClient c;

    public MapleClient() {
        MapleClient();
    }

    public MapleClient MapleClient() {
        return this;
    }

    public MapleClient(MapleAESOFB send, MapleAESOFB receive, Channel session) {
        this.send = send;
        this.receive = receive;
        this.session = session;
    }

    public static byte unban(String charname) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?")) {
                ps.setString(1, charname);
                int accid;
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return -1;
                    }
                    accid = rs.getInt(1);
                }

                try (PreparedStatement psu = con.prepareStatement("UPDATE accounts SET banned = 0, banreason = '' WHERE id = ?")) {
                    psu.setInt(1, accid);
                    psu.executeUpdate();
                }
            }
        } catch (SQLException e) {
            log.error("Error while unbanning", e);
            return -2;
        }
        return 0;
    }

    public static String getLogMessage(MapleClient cfor, String message) {
        return getLogMessage(cfor, message, new Object[0]);
    }

    public static String getLogMessage(MapleCharacter cfor, String message) {
        return getLogMessage(cfor == null ? null : cfor.getClient(), message);
    }

    public static String getLogMessage(MapleCharacter cfor, String message, Object... parms) {
        return getLogMessage(cfor == null ? null : cfor.getClient(), message, parms);
    }

    public static String getLogMessage(MapleClient cfor, String message, Object... parms) {
        StringBuilder builder = new StringBuilder();
        if (cfor != null) {
            if (cfor.getPlayer() != null) {
                builder.append("<");
                builder.append(MapleCharacterUtil.makeMapleReadable(cfor.getPlayer().getName()));
                builder.append(" (角色ID: ");
                builder.append(cfor.getPlayer().getId());
                builder.append(")> ");
            }
            if (cfor.getAccountName() != null) {
                builder.append("(賬號: ");
                builder.append(cfor.getAccountName());
                builder.append(") ");
            }
        }
        builder.append(message);
        int start;
        for (Object parm : parms) {
            start = builder.indexOf("{}");
            builder.replace(start, start + 2, parm.toString());
        }
        return builder.toString();
    }

    public static int findAccIdForCharacterName(String charName) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?")) {
                ps.setString(1, charName);
                ResultSet rs = ps.executeQuery();
                int ret = -1;
                if (rs.next()) {
                    ret = rs.getInt("accountid");
                }
                return ret;
            }
        } catch (SQLException e) {
            log.error("findAccIdForCharacterName SQL error", e);
        }
        return -1;
    }

    public static byte unbanIPMacs(String charname) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            ps.setString(1, charname);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            int accid = rs.getInt(1);
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
            ps.setInt(1, accid);
            rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            String sessionIP = rs.getString("sessionIP");
            String macs = rs.getString("macs");
            rs.close();
            ps.close();
            byte ret = 0;
            if (sessionIP != null) {
                PreparedStatement psa = con.prepareStatement("DELETE FROM ipbans WHERE ip LIKE ?");
                psa.setString(1, sessionIP);
                psa.execute();
                psa.close();
                ret++;
            }
            if (macs != null) {
                String[] macz = macs.split(", ");
                for (String mac : macz) {
                    if (!mac.equals("")) {
                        PreparedStatement psa = con.prepareStatement("DELETE FROM macbans WHERE mac = ?");
                        psa.setString(1, mac);
                        psa.execute();
                        psa.close();
                    }
                }
                ret++;
            }
            return ret;
        } catch (SQLException e) {
            log.error("Error while unbanning", e);
            return -2;
        }
    }

    public static byte unHellban(String charname) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            ps.setString(1, charname);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            int accid = rs.getInt(1);
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
            ps.setInt(1, accid);
            rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            String sessionIP = rs.getString("sessionIP");
            String email = rs.getString("email");
            rs.close();
            ps.close();
            ps = con.prepareStatement("UPDATE accounts SET banned = 0, banreason = '' WHERE email = ?" + (sessionIP == null ? "" : " OR sessionIP = ?"));
            ps.setString(1, email);
            if (sessionIP != null) {
                ps.setString(2, sessionIP);
            }
            ps.execute();
            ps.close();
            return 0;
        } catch (SQLException e) {
            log.error("Error while unbanning", e);
            return -2;
        }
    }

    public static String getAccInfo(String accname, boolean admin) {
        StringBuilder ret = new StringBuilder("帳號 " + accname + " 的信息 -");
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts WHERE name = ?");
            ps.setString(1, accname);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int banned = rs.getInt("banned");
                ret.append(" 狀態: ");
                ret.append(banned > 0 ? "已封" : "正常");
                ret.append(" 封號理由: ");
                ret.append(banned > 0 ? rs.getString("banreason") : "(無描述)");
                if (admin) {
                    ret.append(" 樂豆點: ");
                    ret.append(rs.getInt("ACash"));
                    ret.append(" 楓點: ");
                    ret.append(rs.getInt("mPoints"));
                }
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            log.error("獲取玩家封號理由信息出錯", ex);
        }
        return ret.toString();
    }

    public static String getAccInfoByName(String charname, boolean admin) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?")) {
                ps.setString(1, charname);
                int accid;
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    accid = rs.getInt(1);
                }
                try (PreparedStatement psu = con.prepareStatement("SELECT * FROM accounts WHERE id = ?")) {
                    psu.setInt(1, accid);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            return null;
                        }

                        StringBuilder ret = new StringBuilder("玩家 " + charname + " 的帳號信息 -");
                        int banned = rs.getInt("banned");
                        if (admin) {
                            ret.append(" 賬號: ");
                            ret.append(rs.getString("name"));
                        }
                        ret.append(" 狀態: ");
                        ret.append(banned > 0 ? "已封" : "正常");
                        ret.append(" 封號理由: ");
                        ret.append(banned > 0 ? rs.getString("banreason") : "(無描述)");
                        return ret.toString();
                    }
                }
            }
        } catch (SQLException ex) {
            log.error("獲取玩家封號理由信息出錯", ex);
            return null;
        }
    }

    public MapleAESOFB getReceiveCrypto() {
        return receive;
    }

    public MapleAESOFB getSendCrypto() {
        return send;
    }

    public void write(Packet msg) {
        announce(msg.getData());
    }

    public void send(Packet msg) {
        announce(msg.getData());
    }

    public void announce(final byte[] array) {
        if (session == null || ShutdownServer.getInstance().isShutdown()) {
            return;
        }
        session.writeAndFlush(array);
    }


    public void ctx(int header, Object... data) {
        MaplePacketLittleEndianWriter ctx = new MaplePacketLittleEndianWriter();
        ctx.writeShort(header);
        for (Object obj : data) {
            if (obj instanceof Integer) {
                ctx.writeInt((Integer) obj);
            } else if (obj instanceof Short) {
                ctx.writeShort((Short) obj);
            } else if (obj instanceof Byte) {
                ctx.write((Byte) obj);
            } else if (obj instanceof String) {
                ctx.writeHexString((String) obj);
            }
        }
        this.announce(ctx.getPacket());
    }

    public void outPacket(int header, Object... data) {
        MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
        packet.writeShort(header);
        for (Object obj : data) {
            if (obj instanceof Integer) {
                packet.writeInt((Integer) obj);
            } else if (obj instanceof Long) {
                packet.writeLong((Long) obj);
            } else if (obj instanceof Short) {
                packet.writeShort((Short) obj);
            } else if (obj instanceof Byte) {
                packet.write((Byte) obj);
            } else if (obj instanceof String) {
                packet.writeHexString((String) obj);
            }
        }
        this.announce(packet.getPacket());
    }

    public void sendEnableActions() {
        if (session == null || player == null || ShutdownServer.getInstance().isShutdown()) {
            return;
        }
        player.enableActions(true);
    }

    public void sendEnableActions(boolean useTriggerForUI) {
        if (session == null || player == null || ShutdownServer.getInstance().isShutdown()) {
            return;
        }
        player.enableActions(useTriggerForUI);
    }

    public Channel getSession() {
        return session;
    }

    public long getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public Lock getLock() {
        return mutex;
    }

    public Lock getNPCLock() {
        return npc_mutex;
    }

    public MapleCharacter getPlayer() {
        return this.player;
    }



    public void setPlayer(MapleCharacter player) {
        this.player = player;
    }

    public void createdChar(int id) {
        allowedChar.add(id);
    }

    public boolean login_Auth(int id) {
        return allowedChar.contains(id);
    }

    public List<MapleCharacter> loadCharacters(int serverId) {
//        AccountDao.clearOutdatedPendingDeleteChr(this.getAccID(), serverId);
        List<MapleCharacter> chars = new LinkedList<>();
        Map<Integer, CardData> cards = CharacterCardFactory.getInstance().loadCharacterCards(accId, serverId);
        for (CharNameAndId cni : loadCharactersInternal(serverId)) {
            MapleCharacter chr = MapleCharacter.loadCharFromDB(cni.id, this, false, cards);
            chars.add(chr);
            charInfo.put(chr.getId(), new Pair<>(chr.getLevel(), chr.getJob()));
            if (!login_Auth(chr.getId())) {
                allowedChar.add(chr.getId());
            }
        }
        return chars;
    }

    public void updateCharacterCards(Map<Integer, Integer> cids) {
        if (charInfo.isEmpty()) { //沒有角色
            return;
        }
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM `character_cards` WHERE `accid` = ?")) {
                ps.setInt(1, accId);
                ps.executeUpdate();
            }

            try (PreparedStatement psu = con.prepareStatement("INSERT INTO `character_cards` (accid, worldid, characterid, position) VALUES (?, ?, ?, ?)")) {
                for (Entry<Integer, Integer> ii : cids.entrySet()) {
                    Pair<Integer, Short> info = charInfo.get(ii.getValue());
                    if (info == null || ii.getValue() == 0 || !CharacterCardFactory.getInstance().canHaveCard(info.getLeft(), info.getRight())) {
                        continue;
                    }
                    psu.setInt(1, accId);
                    psu.setInt(2, worldId);
                    psu.setInt(3, ii.getValue());
                    psu.setInt(4, ii.getKey());
                    psu.executeUpdate();
                }
            }
        } catch (SQLException e) {
            log.error("Failed to update character cards. Reason:", e);
        }
    }

    public int getCharacterJob(int cid) {
        if (charInfo.containsKey(cid)) {
            return charInfo.get(cid).getRight();
        }
        return -1;
    }

    public boolean canMakeCharacter(int serverId) {
        return loadCharactersSize(serverId) < getAccCharSlots();
    }

    public List<String> loadCharacterNames(int serverId) {
        List<String> chars = new LinkedList<>();
        for (CharNameAndId cni : loadCharactersInternal(serverId)) {
            chars.add(cni.name);
        }
        return chars;
    }

    private List<CharNameAndId> loadCharactersInternal(int serverId) {
        List<CharNameAndId> chars = new LinkedList<>();
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT id, name FROM characters WHERE accountid = ? AND world = ? ORDER BY position, id")) {
                ps.setInt(1, accId);
                ps.setInt(2, serverId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        chars.add(new CharNameAndId(rs.getString("name"), rs.getInt("id")));
                        LoginServer.getLoginAuth(rs.getInt("id"));
                    }
                }
            }
        } catch (SQLException e) {
            log.error("error loading characters internal", e);
        }
        return chars;
    }

    /*
     * 獲取遊戲帳號下已經創建的角色個數
     */
    public int loadCharactersSize(int serverId) {
        int chars = 0;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT count(*) FROM characters WHERE accountid = ? AND world = ?")) {
                ps.setInt(1, accId);
                ps.setInt(2, serverId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        chars = rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("error loading characters size", e);
        }
        return chars;
    }

    public boolean isLoggedIn() {
        return loggedIn && accId >= 0;
    }

    private Calendar getTempBanCalendar(ResultSet rs) throws SQLException {
        Calendar lTempban = Calendar.getInstance();
        Timestamp tempbanTime = rs.getTimestamp("tempban");
        if (tempbanTime == null || tempbanTime.equals(Timestamp.valueOf("1970-01-01 00:00:01"))) {
            lTempban.setTimeInMillis(0);
            return lTempban;
        }
        Calendar today = Calendar.getInstance();
        lTempban.setTimeInMillis(tempbanTime.getTime());
        if (today.getTimeInMillis() < lTempban.getTimeInMillis()) {
            return lTempban;
        }
        lTempban.setTimeInMillis(0);
        return lTempban;
    }

    public Calendar getTempBanCalendar() {
        return tempban;
    }

    public byte getBanType() {
        return greason;
    }

    public boolean hasBannedIP() {
        boolean ret = false;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM ipbans WHERE ? LIKE CONCAT(ip, '%')")) {
                ps.setString(1, getSessionIPAddress());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        ret = true;
                    }
                }
            }
        } catch (SQLException ex) {
            log.error("Error checking ip bans", ex);
        }
        return ret;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String macData) {
        if (macData.equalsIgnoreCase("00-00-00-00-00-00") || macData.length() != 17) {
            return;
        }
        this.mac = macData;
    }

    public boolean hasBannedMac() {
        if (mac.equalsIgnoreCase("00-00-00-00-00-00") || mac.length() != 17) {
            return false;
        }
        boolean ret = false;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM macbans WHERE mac = ?")) {
                ps.setString(1, mac);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        ret = true;
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Error checking mac bans", e);
        }
        return ret;
    }

    public void banMacs() {
        banMacs(mac);
    }

    public void banMacs(String macData) {
        if (macData.equalsIgnoreCase("00-00-00-00-00-00") || macData.length() != 17) {
            return;
        }
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO macbans (mac) VALUES (?)")) {
                ps.setString(1, macData);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("Error banning MACs", e);
        }
    }

    public void updateMacs() {
        updateMacs(mac);
    }

    public void updateMacs(String macData) {
        if (macData.equalsIgnoreCase("00-00-00-00-00-00") || macData.length() != 17) {
            return;
        }
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET macs = ? WHERE id = ?")) {
                ps.setString(1, macData);
                ps.setInt(2, accId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("Error saving MACs", e);
        }
    }

    /**
     * Returns 0 on success, a state to be used for
     * {@link LoginPacket#getLoginFailed(MapleEnumClass.AuthReply)} otherwise.
     *
     * @return The state of the login.
     */
    public int finishLogin() {
        login_mutex.lock();
        try {
            byte state = getLoginState();
            if (state > MapleClient.LOGIN_NOTLOGGEDIN) { // already loggedin
                loggedIn = false;
                return 7;
            }
            updateLoginState(MapleClient.LOGIN_LOGGEDIN, getSessionIPAddress());
        } finally {
            login_mutex.unlock();
        }
        return 0;
    }

    public void clearInformation() {
        accountName = null;
        accId = -1;
        secondPassword = null;
        salt2 = null;
        gmLevel = 0;
        maplePoint = 0;
        loggedIn = false;
        mac = "00-00-00-00-00-00";
        maclist.clear();
        this.player = null;
    }

    public int changePassword(String oldpwd, String newpwd) {
        int ret = -1;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts WHERE name = ?");
            ps.setString(1, getAccountName());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                boolean updatePassword = false;
                String passhash = rs.getString("password");
                String salt = rs.getString("salt");
                if (passhash == null || passhash.isEmpty()) {
                    ret = -1;
                } else if (LoginCryptoLegacy.isLegacyPassword(passhash) && LoginCryptoLegacy.checkPassword(oldpwd, passhash)) {
                    ret = 0;
                    updatePassword = true;
                } else if (oldpwd.equals(passhash)) {
                    ret = 0;
                    updatePassword = true;
                } else if (salt == null && LoginCrypto.checkSha1Hash(passhash, oldpwd)) {
                    ret = 0;
                    updatePassword = true;
                } else if (LoginCrypto.checkSaltedSha512Hash(passhash, oldpwd, salt)) {
                    ret = 0;
                    updatePassword = true;
                } else {
                    ret = -1;
                }
                if (updatePassword) {
                    try (PreparedStatement pss = con.prepareStatement("UPDATE `accounts` SET `password` = ?, `salt` = ? WHERE id = ?")) {
                        String newSalt = LoginCrypto.makeSalt();
                        pss.setString(1, LoginCrypto.makeSaltedSha512Hash(newpwd, newSalt));
                        pss.setString(2, newSalt);
                        pss.setInt(3, accId);
                        pss.executeUpdate();
                    }
                }
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            log.error("修改遊戲帳號密碼出現錯誤.\r\n", e);
        }
        return ret;
    }

    public MapleEnumClass.AuthReply login(String login, String pwd, boolean ipMacBanned, boolean useKey) {
        MapleEnumClass.AuthReply loginok = MapleEnumClass.AuthReply.GAME_ACCOUNT_NOT_LANDED;
//        if (!useKey) {
//            loginattempt++;
//            if (loginattempt > 6) {
//                log.info("賬號[" + login + "]登錄次數達到6次還未登錄遊戲，服務端斷開連接.");
//                getSession().close();
//            }
//        }
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts WHERE name = ?")) {
                ps.setString(1, login);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int banned = rs.getInt("banned");
                        String passhash = rs.getString("password");
                        String salt = rs.getString("salt");
                        String oldSession = rs.getString("SessionIP");

                        accountName = login;
                        accId = rs.getInt("id");
                        secondPassword = rs.getString("2ndpassword");
                        salt2 = rs.getString("salt2");
                        gmLevel = rs.getInt("gm");
                        greason = rs.getByte("greason");
                        tempban = getTempBanCalendar(rs);
                        gender = rs.getByte("gender");

                        maclist = new LinkedList<>();
                        String macStrs = rs.getString("maclist");
                        if (macStrs != null) {
                            String[] macData = macStrs.split(",");
                            for (String macData1 : macData) {
                                if (macData1.length() == 17) {
                                    maclist.add(macData1);
                                }
                            }
                        }

                        if (secondPassword != null && salt2 != null) {
                            secondPassword = LoginCrypto.rand_r(secondPassword);
                        }
                        ps.close();

                        if (useKey) {
                            loginok = MapleEnumClass.AuthReply.GAME_LOGIN_SUCCESSFUL;
                        } else {
                            if ((banned > 0 || (tempban != null && tempban.getTimeInMillis() > System.currentTimeMillis())) && gmLevel == 0) {
                                loginok = MapleEnumClass.AuthReply.GAME_ACCOUNT_BANNED;
                            } else {
                                if (banned == -1) {
                                    unban();
                                }
                                boolean updatePasswordHash = false;
                                // Check if the passwords are correct here. :B
                                if (passhash == null || passhash.isEmpty()) {
                                    //match by sessionIP
                                    if (oldSession != null && !oldSession.isEmpty()) {
                                        loggedIn = getSessionIPAddress().equals(oldSession);
                                        loginok = loggedIn ? MapleEnumClass.AuthReply.GAME_LOGIN_SUCCESSFUL : MapleEnumClass.AuthReply.GAME_PASSWORD_ERROR;
                                        updatePasswordHash = loggedIn;
                                    } else {
                                        loginok = MapleEnumClass.AuthReply.GAME_PASSWORD_ERROR;
                                        loggedIn = false;
                                    }
                                } else if (LoginCryptoLegacy.isLegacyPassword(passhash) && LoginCryptoLegacy.checkPassword(pwd, passhash)) {
                                    // Check if a password upgrade is needed.
                                    loginok = MapleEnumClass.AuthReply.GAME_LOGIN_SUCCESSFUL;
                                    updatePasswordHash = true;
                                } else if (pwd.equals(passhash)) {
                                    loginok = MapleEnumClass.AuthReply.GAME_LOGIN_SUCCESSFUL;
                                    updatePasswordHash = true;
                                } else if (salt == null && LoginCrypto.checkSha1Hash(passhash, pwd)) {
                                    loginok = MapleEnumClass.AuthReply.GAME_LOGIN_SUCCESSFUL;
                                    updatePasswordHash = true;
                                } else if (LoginCrypto.checkSaltedSha512Hash(passhash, pwd, salt)) {
                                    loginok = MapleEnumClass.AuthReply.GAME_LOGIN_SUCCESSFUL;
                                } else {
                                    loggedIn = false;
                                    loginok = MapleEnumClass.AuthReply.GAME_PASSWORD_ERROR;
                                }
                                if (updatePasswordHash) {
                                    try (PreparedStatement pss = con.prepareStatement("UPDATE `accounts` SET `password` = ?, `salt` = ? WHERE id = ?")) {
                                        String newSalt = LoginCrypto.makeSalt();
                                        pss.setString(1, LoginCrypto.makeSaltedSha512Hash(pwd, newSalt));
                                        pss.setString(2, newSalt);
                                        pss.setInt(3, accId);
                                        pss.executeUpdate();
                                    }
                                }

                                if (loginok == MapleEnumClass.AuthReply.GAME_LOGIN_SUCCESSFUL) {
                                    if (World.Client.isStuck(this, accId)) {
                                        updateLoginState(0);
                                    }

                                    byte loginstate = getLoginState();
                                    if (loginstate > MapleClient.LOGIN_NOTLOGGEDIN) { // already loggedin
                                        loggedIn = false;
                                        loginok = MapleEnumClass.AuthReply.GAME_CONNECTING_ACCOUNT;
                                    }
                                }
                            }
                        }
                    } else if (ServerConfig.AUTORIGISTER) {
                        if (AutoRegister.createAccount(login, pwd)) {
                            loginok = login(login, pwd, ipMacBanned, useKey);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.error("登錄遊戲帳號出現錯誤. 賬號: " + login + " \r\n", e);
        }
        return loginok;
    }

    public boolean CheckSecondPassword(String in) {
        if (secondPassword == null) {
            try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
                ps.setInt(1, accId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    secondPassword = rs.getString("2ndpassword");
                    if (secondPassword != null && rs.getString("salt2") != null) {
                        secondPassword = LoginCrypto.rand_r(secondPassword);
                    }
                    salt2 = rs.getString("salt2");
                }
            } catch (SQLException e) {
            }
            if (secondPassword == null) {
                log.error("讀取二次密碼錯誤");
                return false;
            }
        }
        boolean allow = false;
        boolean updatePasswordHash = false;
        // Check if the passwords are correct here. :B
        if (LoginCryptoLegacy.isLegacyPassword(secondPassword) && LoginCryptoLegacy.checkPassword(in, secondPassword)) {
            // Check if a password upgrade is needed.
            allow = true;
            updatePasswordHash = true;
        } else if (salt2 == null && LoginCrypto.checkSha1Hash(secondPassword, in)) {
            allow = true;
            updatePasswordHash = true;
        } else if (in.equals(secondPassword)) {
            // 檢查密碼是否未做任何加密
            allow = true;
            updatePasswordHash = true;
        } else if (LoginCrypto.checkSaltedSha512Hash(secondPassword, in, salt2)) {
            allow = true;
        }
        if (updatePasswordHash) {
            setSecondPassword(in);
            return updateSecondPassword();
        }
        return allow;
    }

    private void unban() {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banned = 0, banreason = '' WHERE id = ?")) {
                ps.setInt(1, accId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("Error while unbanning", e);
        }
    }

    public int getAccID() {
        return this.accId;
    }

    public void setAccID(int id) {
        this.accId = id;
    }

    public void updateLoginState(int newstate) {
        updateLoginState(newstate, getSessionIPAddress());
    }

    public void updateLoginState(int newstate, String SessionID) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET loggedin = ?, SessionIP = ?, lastlogin = CURRENT_TIMESTAMP() WHERE id = ?")) {
                ps.setInt(1, newstate);
                ps.setString(2, SessionID);
                ps.setInt(3, getAccID());
                ps.executeUpdate();
            }
        } catch (Exception e) {
            log.error("Error updating login state", e);
        } finally {
            if (newstate == MapleClient.LOGIN_NOTLOGGEDIN) {
                loggedIn = false;
                serverTransition = false;
            } else {
                serverTransition = (newstate == MapleClient.LOGIN_SERVER_TRANSITION || newstate == MapleClient.CHANGE_CHANNEL);
                loggedIn = !serverTransition;
            }
        }
    }

    public boolean updateSecondPassword() {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE `accounts` SET `2ndpassword` = ?, `salt2` = ? WHERE id = ?")) {
                String newSalt = LoginCrypto.makeSalt();
                ps.setString(1, LoginCrypto.rand_s(LoginCrypto.makeSaltedSha512Hash(secondPassword, newSalt)));
                ps.setString(2, newSalt);
                ps.setInt(3, accId);
                ps.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public byte getLoginState() {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT loggedin, lastlogin, banned, `birthday` + 0 AS `bday` FROM accounts WHERE id = ?")) {
                ps.setInt(1, getAccID());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return MapleClient.LOGIN_NOTLOGGEDIN;
                    }
                    birthday = rs.getInt("bday");
                    byte state = rs.getByte("loggedin");

                    /*
                     * 如果是在更換頻道或者登錄過渡
                     * 檢測 lastlogin 的時間加 20秒 小於當前系統的時間
                     * 就更新登錄狀態為 0
                     */
                    if (state == MapleClient.LOGIN_SERVER_TRANSITION || state == MapleClient.CHANGE_CHANNEL) {
                        if (rs.getTimestamp("lastlogin").getTime() + 20000 < System.currentTimeMillis()) { // connecting to chanserver timeout
                            state = MapleClient.LOGIN_NOTLOGGEDIN;
                            updateLoginState(state, getSessionIPAddress());
                        }
                    }
                    loggedIn = state == MapleClient.LOGIN_LOGGEDIN;
                    return state;
                }
            }
        } catch (SQLException e) {
            loggedIn = false;
            log.error("error getting login state", e);
            return MapleClient.LOGIN_NOTLOGGEDIN;
        }
    }

    public boolean checkBirthDate(int date) {
        return birthday == date;
    }

    public void removalTask(boolean shutdown) {
        try {
            player.removeAllEffect();
            if (player.getMarriageId() > 0) {
                MapleQuestStatus stat1 = player.getQuestNoAdd(MapleQuest.getInstance(160001));
                MapleQuestStatus stat2 = player.getQuestNoAdd(MapleQuest.getInstance(160002));
                if (stat1 != null && stat1.getCustomData() != null && (stat1.getCustomData().equals("2_") || stat1.getCustomData().equals("2"))) {
                    //dc in process of marriage
                    if (stat2 != null && stat2.getCustomData() != null) {
                        stat2.setCustomData("0");
                    }
                    stat1.setCustomData("3");
                }
            }
            if (player.getMapId() == GameConstants.JAIL && !player.isIntern()) {
                MapleQuestStatus stat1 = player.getQuestNAdd(MapleQuest.getInstance(GameConstants.JAIL_TIME));
                MapleQuestStatus stat2 = player.getQuestNAdd(MapleQuest.getInstance(GameConstants.JAIL_QUEST));
                if (stat1.getCustomData() == null) {
                    stat1.setCustomData(String.valueOf(System.currentTimeMillis()));
                } else if (stat2.getCustomData() == null) {
                    stat2.setCustomData("0"); //seconds of jail
                } else { //previous seconds - elapsed seconds
                    int seconds = Integer.parseInt(stat2.getCustomData()) - (int) ((System.currentTimeMillis() - Long.parseLong(stat1.getCustomData())) / 1000);
                    if (seconds < 0) {
                        seconds = 0;
                    }
                    stat2.setCustomData(String.valueOf(seconds));
                }
            }
            player.changeRemoval(true);
            IMaplePlayerShop shop = player.getPlayerShop();
            if (shop != null) {
                shop.removeVisitor(player);
                if (shop.isOwner(player)) {
                    if (shop.getShopType() == 1 && shop.isAvailable() && !shutdown) {
                        shop.setOpen(true);
                    } else {
                        shop.closeShop(true, !shutdown);
                    }
                }
            }
            player.setMessenger(null);
            MapleAntiMacro.stopAnti(player.getName());
            if (player.getMap() != null) {
                if (shutdown || (getChannelServer() != null && getChannelServer().isShutdown())) {
                    int questID = -1;
                    switch (player.getMapId()) {
                        case 240060200: //生命之穴 - 闇黑龍王洞穴
                            questID = 160100;
                            break;
                        case 240060201: //生命之穴 - 進階闇黑龍王洞穴
                            questID = 160103;
                            break;
                        case 280030100: //最後的任務 - 殘暴炎魔的祭台
                        case 280030000: //神秘島 - 殘暴炎魔的祭台
                            questID = 160101;
                            break;
                        case 280030001: //最後的任務 - 進階殘暴炎魔的祭台
                            questID = 160102;
                            break;
                        case 270050100: //神殿的深處 - 神的黃昏
                            questID = 160104;
                            break;
                        case 105100300: //巴洛古神殿 - 巴洛古的墓地
                        case 105100400: //巴洛古神殿 - 巴洛古的墓地
                            questID = 160106;
                            break;
                        case 211070000: //獅子王之城 - 接見室走廊
                        case 211070100: //獅子王之城 - 接見室
                        case 211070101: //獅子王之城 - 空中監獄
                        case 211070110: //獅子王之城 - 復活塔樓
                            questID = 160107;
                            break;
                        case 551030200: //馬來西亞 - 陰森世界
                            questID = 160108;
                            break;
                        case 271040100: //騎士團要塞 - 西格諾斯的殿堂
                            questID = 160109;
                            break;
                    }
                    if (questID > 0) {
                        player.getQuestNAdd(MapleQuest.getInstance(questID)).setCustomData("0"); //reset the time.
                    }

                } else if (player.isAlive()) {
                    switch (player.getMapId()) {
                        case 541010100: //新加坡 - 輪機艙
                        case 541020800: //新加坡 - 千年樹精王遺跡Ⅱ
                        case 220080001: //玩具城 - 時間塔的本源
                            player.getMap().addDisconnected(player.getId());
                            break;
                    }
                }
                player.getMap().userLeaveField(player);
            }
        } catch (Throwable e) {
            log.error("error removalTask", e);
        }
    }

    public void disconnect(boolean RemoveInChannelServer, boolean fromCS) {
        disconnect(RemoveInChannelServer, fromCS, false);
    }

    public void disconnect(boolean RemoveInChannelServer, boolean fromCS, boolean shutdown) {
        if (disconnecting) {
            return;
        }
        synchronized (disconnectLock) {
            if (disconnecting) {
                return;
            }
            disconnecting = true;
        }

        if (aliveCheckSchedule != null) {
            aliveCheckSchedule.cancel(true);
            aliveCheckSchedule = null;
        }
        if (player != null) {
            MapleMap map = player.getMap();
            int idz = player.getId(), messengerId = player.getMessenger() == null ? 0 : player.getMessenger().getId(), gid = player.getGuildId(), fid;
            BuddyList chrBuddy = player.getBuddylist();
            MapleMessengerCharacter chrMessenger = new MapleMessengerCharacter(player);
            MapleGuildCharacter chrGuild = player.getMGC();
            removalTask(shutdown);

            LoginServer.getLoginAuth(player.getId());
            //LoginServer.getLoginAuthKey(accountName, true);
            if (!fromCS) {
                player.expirationTask(true);
            }
            player.saveToDB(true, fromCS);
            if (shutdown) {
                player = null;
                receiving = false;
                return;
            }

            player.setOnline(false);
            if (!fromCS) {
                ChannelServer ch = ChannelServer.getInstance(map == null ? channel : map.getChannel());
                int chz = WorldFindService.getInstance().findChannel(idz);
                if (chz < -1) {
                    disconnect(RemoveInChannelServer, true);//u lie
                    return;
                }
                try {
                    if (chz == -1 || ch == null || ch.isShutdown()) {
                        player = null;
                        return;//no idea
                    }
                    if (messengerId > 0) {
                        WorldMessengerService.getInstance().leaveMessenger(messengerId, chrMessenger);
                    }
                    if (player.getParty() != null && player.getParty().getPartyLeaderID() == idz) {
                        player.getParty().changeLeaderDC();
                    }
                    if (chrBuddy != null) {
                        if (!serverTransition) {
                            WorldBuddyService.getInstance().loggedOff(idz, channel, chrBuddy.getBuddyIds());
                        } else { // Change channel
                            WorldBuddyService.getInstance().loggedOn(idz, channel, chrBuddy.getBuddyIds());
                        }
                    }
                    if (gid > 0 && chrGuild != null) {
                        WorldGuildService.getInstance().setGuildMemberOnline(chrGuild, false, -1);
                    }
                } catch (Exception e) {
                    log.error(getLogMessage(this, "ERROR") + e);
                } finally {
                    if (RemoveInChannelServer && ch != null && player != null) {
                        ch.removePlayer(player.getId()); //這個地方是清理角色的信息
                    }
                    player = null;
                }
            } else {
                int ch = WorldFindService.getInstance().findChannel(idz);
                if (ch > 0) {
                    disconnect(RemoveInChannelServer, false); //如果頻道大於 0 角色應該是在頻道伺服器中
                    return;
                }
                try {
                    if (!serverTransition) {
                        WorldBuddyService.getInstance().loggedOff(idz, channel, chrBuddy.getBuddyIds());
                    } else { // Change channel
                        WorldBuddyService.getInstance().loggedOn(idz, channel, chrBuddy.getBuddyIds());
                    }
                    if (gid > 0 && chrGuild != null) {
                        WorldGuildService.getInstance().setGuildMemberOnline(chrGuild, false, -1);
                    }
                    if (player != null) {
                        player.setMessenger(null);
                    }
                } catch (Exception e) {
                    log.error(getLogMessage(this, "ERROR") + e);
                } finally {
                    if (RemoveInChannelServer && ch == -10) {
                        CashShopServer.getPlayerStorage().deregisterPlayer(idz);
                    }
                    player = null;
                }
            }
        }
        if (!serverTransition && isLoggedIn()) {
            if (!shutdown) {
                updateLoginState(MapleClient.LOGIN_NOTLOGGEDIN, getSessionIPAddress());
            }
            session.attr(MapleClient.CLIENT_KEY).set(null);
            session.attr(MaplePacketDecoder.DECODER_STATE_KEY).set(null);
            session.close();
        }
        engines.clear();
    }

    public String getSessionIPAddress() {
        if (session == null || !session.isActive()) {
            return "0.0.0.0";
        }
        return session.remoteAddress().toString().split(":")[0].replace("/", "");
    }

    public String getSessionLocalIPAddress() {
        if (session == null || !session.isActive()) {
            return "0.0.0.0";
        }
        return session.localAddress().toString().split(":")[0].replace("/", "");
    }

    public boolean CheckIPAddress() {
        if (this.accId < 0) {
            return false;
        }
        boolean canlogin = true;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT SessionIP, banned FROM accounts WHERE id = ?");
            ps.setInt(1, this.accId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (rs.getInt("banned") > 0) {
                        canlogin = false; //canlogin false = close Client.client
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Failed in checking IP address for Client.", e);
        }
        return canlogin;
    }

    public void DebugMessage(StringBuilder sb) {
        sb.append(getSession().remoteAddress());
        sb.append(" 是否連接: ");
        sb.append(getSession().isActive());
        sb.append(" 是否斷開: ");
        sb.append(!getSession().isOpen());
        sb.append(" 密匙狀態: ");
        sb.append(getSession().attr(MapleClient.CLIENT_KEY) != null);
        sb.append(" 登錄狀態: ");
        sb.append(isLoggedIn());
        sb.append(" 是否有角色: ");
        sb.append(getPlayer() != null);
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public ChannelServer getChannelServer() {
        return ChannelServer.getInstance(channel);
    }

    public byte getGender() {
        return gender;
    }

    public void setGender(byte gender) {
        this.gender = gender;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET gender = ? WHERE id = ?")) {
                ps.setByte(1, gender);
                ps.setInt(2, accId);
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException e) {
            log.error("保存角色性別出錯", e);
        }
    }

    public String getSecondPassword() {
        return secondPassword;
    }

    public void setSecondPassword(String secondPassword) {
        this.secondPassword = secondPassword;
    }

    public void setSalt2(String salt2) {
        this.salt2 = salt2;
    }

    public String getAccountName() {
        return accountName;
    }

    public boolean checkSecuredAccountName(String accountName) {
        if (getAccountName().length() != accountName.length()) {
            return false;
        }
        for (int i = 0; i < accountName.length(); i++) {
            if (accountName.charAt(i) == '*') {
                continue;
            }
            if (accountName.charAt(i) != this.accountName.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public String getSecurityAccountName() {
        StringBuilder sb = new StringBuilder(accountName);
        if (sb.length() >= 4) {
            sb.replace(1, 3, "**");
        } else if (sb.length() >= 3) {
            sb.replace(1, 2, "*");
        }
        if (sb.length() > 4) {
            sb.replace(sb.length() - 1, sb.length(), "*");
        }
        return sb.toString();
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public SwordieX.world.World getWorld() {
        return NetRun.Server.getInstance().getWorldById(getWorldId());
    }

    public int getWorldId() {
        return worldId;
    }

    public void setWorldId(int worldId) {
        this.worldId = worldId;
    }

    public void pongReceived() {
//        if (Config.isDevelop()) log.info("pong received " + aliveCheckCount.get());
        aliveCheckCount.set(0);
        if (this.aliveCheckSchedule != null) {
            aliveCheckSchedule.cancel(true);
            aliveCheckSchedule = null;
        }

        aliveCheckSchedule = Timer.PingTimer.getInstance().register(() -> {
            write(Login.sendAliveReq());
            write(Login.sendPingCheckResultClientToGame());
            write(Login.sendSecurityPacket());
            outPacket(OutHeader.LP_UseAttack.getValue(), 0);
            outPacket(OutHeader.LP_SpecialChairTWSitResult.getValue(), 0);
        }, 5000);
    }

    /*public void startPingSchedule(ServerType type) {
        if (this.aliveCheckSchedule != null) {
            aliveCheckSchedule.cancel(true);
            aliveCheckSchedule = null;
        }

        aliveCheckSchedule = PingTimer.getInstance().register(() -> {
            if (Config.isDevelop()) log.info("Client.client ping " + aliveCheckCount.get());
            if (type == ServerType.LoginServer) {
                write(Login.sendAliveReq());
            } else {
                write(Login.sendPingCheckResultClientToGame());
               // write(OverseasPacket.extraSystemResult(ExtraSystemResult.extraTimerSystem(0xBB_0E_30_A4_1E_73_E0_40L, System.currentTimeMillis())));
                write(Login.sendSecurityPacket());
            }
            PlayerHandler.PlayerUpdate(this, player);
            try {
                if (aliveCheckCount.incrementAndGet() > (Config.isDevelop() ? 1000 : 50)) {
                    boolean close = false;
                    if (getSession() != null && getSession().isActive()) {
                        close = true;
                        //getSession().close();
                    }
                    log.info(getLogMessage(MapleClient.this, "自動斷線 : Ping超時 " + close));
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }, 5000, 5000);
    }*/

    public boolean isIntern() {
        return gmLevel >= PlayerRank.實習管理員.getSpLevel();
    }

    public boolean isGm() {
        return gmLevel >= PlayerRank.遊戲管理員.getSpLevel();
    }

    public boolean isSuperGm() {
        return gmLevel >= PlayerRank.超級管理員.getSpLevel();
    }

    public boolean isAdmin() {
        return gmLevel >= PlayerRank.伺服管理員.getSpLevel();
    }

    public int getGmLevel() {
        return gmLevel;
    }

    public boolean hasGmLevel(int level) {
        return gmLevel >= level;
    }

    public void setGmLevel(int level) {
        this.gmLevel = level;
    }

    public void updateGmLevel() {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET gm = ? WHERE id = ?")) {
                ps.setInt(1, gmLevel);
                ps.setInt(2, accId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
        }
    }

    public ScheduledFuture<?> getIdleTask() {
        return idleTask;
    }

    public void setIdleTask(ScheduledFuture<?> idleTask) {
        this.idleTask = idleTask;
    }

    /**
     * 獲取帳號可創建角色數量
     */
    public int getAccCharSlots() {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM character_slots WHERE accid = ? AND worldid = ?")) {
                ps.setInt(1, accId);
                ps.setInt(2, worldId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        charslots = Math.min(GameConstants.MAX_CHARS_SLOTS, rs.getInt("charslots"));
                    } else {
                        charslots = Math.min(GameConstants.MAX_CHARS_SLOTS, charslots);
                        try (PreparedStatement psu = con.prepareStatement("INSERT INTO character_slots (accid, worldid, charslots) VALUES (?, ?, ?)")) {
                            psu.setInt(1, accId);
                            psu.setInt(2, worldId);
                            psu.setInt(3, charslots);
                            psu.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.error("獲取帳號可創建角色數量出現錯誤", e);
        }
        return charslots;
    }

    /**
     * 增加帳號可創建角色數量
     */
    public boolean gainAccCharSlot() {
        if (getAccCharSlots() >= GameConstants.MAX_CHARS_SLOTS) {
            return false;
        }
        charslots++;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE character_slots SET charslots = ? WHERE worldid = ? AND accid = ?")) {
                ps.setInt(1, charslots);
                ps.setInt(2, worldId);
                ps.setInt(3, accId);
                ps.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            log.error("增加帳號可創建角色數量出現錯誤", e);
            return false;
        }
    }

    /**
     * 獲取帳號下的角色卡數量
     */
    public int getAccCardSlots() {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts_info WHERE accId = ? AND worldId = ?")) {
                ps.setInt(1, accId);
                ps.setInt(2, worldId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        cardslots = rs.getInt("cardSlots");
                    } else {
                        try (PreparedStatement psu = con.prepareStatement("INSERT INTO accounts_info (accId, worldId, cardSlots) VALUES (?, ?, ?)")) {
                            psu.setInt(1, accId);
                            psu.setInt(2, worldId);
                            psu.setInt(3, cardslots);
                            psu.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.error("獲取帳號下的角色卡數量出現錯誤", e);
        }
        return cardslots;
    }

    /**
     * 增加角色卡的數量
     */
    public boolean gainAccCardSlot() {
        if (getAccCardSlots() >= 9) {
            return false;
        }
        cardslots++;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE accounts_info SET cardSlots = ? WHERE worldId = ? AND accId = ?")) {
                ps.setInt(1, cardslots);
                ps.setInt(2, worldId);
                ps.setInt(3, accId);
                ps.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            log.error("增加角色卡的數量出現錯誤", e);
            return false;
        }
    }

    public boolean isMonitored() {
        return monitored;
    }

    public void setMonitored(boolean m) {
        this.monitored = m;
    }

    public boolean isReceiving() {
        return receiving;
    }

    public void setReceiving(boolean m) {
        this.receiving = m;
    }

    public String getTempIP() {
        return tempIP;
    }

    public void setTempIP(String s) {
        this.tempIP = s;
    }

    public boolean isLocalhost() {
        return ServerConstants.isIPLocalhost(getSessionIPAddress());
    }

    public boolean hasCheck(int accid) {
        boolean ret = false;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?")) {
                ps.setInt(1, accid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ret = rs.getInt("check") > 0;
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Error checking ip Check", e);
        }
        return ret;
    }

    public void setScriptEngine(String name, ScriptEngine e) {
        engines.put(name, e);
    }

    public ScriptEngine getScriptEngine(String name) {
        return engines.get(name);
    }

    public void removeScriptEngine(String name) {
        engines.remove(name);
    }

    public boolean canClickNPC() {
        return lastNpcClick + 500 < System.currentTimeMillis();
    }

    public void setClickedNPC() {
        lastNpcClick = System.currentTimeMillis();
    }

    public void removeClickedNPC() {
        lastNpcClick = 0;
    }

    //public NPCConversationManager getCM() {
    //    return NPCScriptManager.getInstance().getCM(this);
    //}

    public boolean hasCheckMac(String macData) {
        return !(macData.equalsIgnoreCase("00-00-00-00-00-00") || macData.length() != 17 || maclist.isEmpty()) && maclist.contains(macData);
    }

    public void setTempInfo(String login, String pwd, boolean isBanned) {
        tempinfo = new Triple<>(login, pwd, isBanned);
    }

    public Triple<String, String, Boolean> getTempInfo() {
        return tempinfo;
    }

    public void addProcessName(String process) {
        proesslist.add(process);
    }

    public boolean hasProcessName(String process) {
        for (String p : proesslist) {
            if (p.startsWith(process)) {
                return true;
            }
        }
        return proesslist.contains(process);
    }

    public void dropMessage(String message) {
        announce(MaplePacketCreator.serverNotice(1, message));
    }

    public boolean modifyCSPoints(int type, int quantity) {
        switch (type) {
            case 1:
                if (getACash() + quantity < 0) {
                    return false;
                }
                setACash(getACash() + quantity);
                break;
            case 2:
                if (quantity < 0 && ServerConfig.mileageAsMaplePoint) {
                    int mileage = getMileage();
                    if (mileage >= Math.abs(quantity)) {
                        modifyMileage(quantity);
                        BuyCashItemHandler.addCashshopLog(this, 0, 5440000, 1, 0, Math.abs(quantity), 1, "里程兌換楓點");
                        return true;
                    } else {
                        if (getMaplePoints(true) + mileage + quantity < 0) {
                            return false;
                        }
                        modifyMileage(-mileage);
                        BuyCashItemHandler.addCashshopLog(this, 0, 5440000, 1, 0, Math.abs(mileage), 1, "里程兌換楓點");
                        quantity = mileage + quantity;
                    }
                }
                if (getMaplePoints(true) + quantity < 0) {
                    return false;
                }
                setMaplePoints(getMaplePoints(true) + quantity);
                break;
            default:
                return false;
        }
        return true;
    }

    public int getCSPoints(int type) {
        switch (type) {
            case 1:
                return getACash();
            case 2:
                return getMaplePoints();
            default:
                return 0;
        }
    }

    public int getACash() {
        int point = 0;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT ACash FROM accounts WHERE id = ?")) {
                ps.setInt(1, getAccID());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        point = rs.getInt("ACash");
                    }
                }
            }
        } catch (SQLException e) {
            log.error("獲取角色樂豆點失敗。" + e);
        }
        return point;
    }

    public void setACash(final int point) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET ACash = ? WHERE id = ?")) {
                ps.setInt(1, point);
                ps.setInt(2, getAccID());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("獲取角色樂豆點失敗。" + e);
        }
    }

    public int getMaplePoints() {
        return getMaplePoints(false);
    }

    public int getMaplePoints(boolean onlyMPoint) {
        int point = maplePoint;
        if (!onlyMPoint && ServerConfig.mileageAsMaplePoint) {
            point += getMileage();
        }
        return point;
    }

    public void setMaplePoints(final int point) {
        maplePoint = point;
    }

    public List<Pair<Triple<Integer, Integer, Integer>, Long>> getMileageRechargeRecords() {
        List<Pair<Triple<Integer, Integer, Integer>, Long>> recordList = new LinkedList<>();
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("DELETE A FROM mileage_recharge_record A INNER JOIN (SELECT id FROM mileage_recharge_record WHERE accId = ? ORDER BY Time DESC LIMIT " + (ServerConfig.mileageMonthlyLimitMax * 2) + "," + (ServerConfig.mileageMonthlyLimitMax * 2) + ") B ON A.id=B.id");
            ps.setInt(1, getAccID());
            ps.execute();
            ps.close();
            ps = con.prepareStatement("SELECT * FROM mileage_recharge_record WHERE accId = ? AND mileage > 0 ORDER BY Time DESC LIMIT " + ServerConfig.mileageMonthlyLimitMax);
            ps.setInt(1, getAccID());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                recordList.add(new Pair(new Triple(rs.getInt("mileage"), rs.getInt("type"), rs.getInt("status")), rs.getTimestamp("Time").getTime()));
            }
            ps.close();
        } catch (SQLException e) {
        }
        return recordList;
    }

    public List<Pair<Triple<Integer, Integer, Integer>, Long>> getMileagePurchaseRecords() {
        List<Pair<Triple<Integer, Integer, Integer>, Long>> recordList = new LinkedList<>();
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("DELETE A FROM cashshop_log A INNER JOIN (SELECT id FROM cashshop_log WHERE accId = ? ORDER BY Time DESC LIMIT 1000,1000) B ON A.id=B.id");
            ps.setInt(1, getAccID());
            ps.execute();
            ps.close();
            ps = con.prepareStatement("SELECT * FROM cashshop_log WHERE accId = ? AND mileage > 0 ORDER BY Time DESC LIMIT 100");
            ps.setInt(1, getAccID());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                recordList.add(new Pair(new Triple(rs.getInt("mileage"), rs.getInt("itemId"), rs.getInt("SN")), rs.getTimestamp("Time").getTime()));
            }
            ps.close();
        } catch (SQLException e) {
        }
        return recordList;
    }

    public List<Pair<Integer, Long>> getMileageRecords() {
        List<Pair<Integer, Long>> recordList = new LinkedList<>();
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM mileage_record WHERE mileage <= 0 OR Time IS NULL OR Time <= CURDATE()");
            ps.execute();
            ps.close();

            ps = con.prepareStatement("SELECT * FROM mileage_record WHERE accId = ? AND mileage > 0 ORDER BY Time DESC");
            ps.setInt(1, getAccID());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                recordList.add(new Pair(rs.getInt("mileage"), rs.getTimestamp("Time").getTime()));
            }
            ps.close();
        } catch (SQLException e) {
        }
        return recordList;
    }

    public int getMileage() {
        List<Pair<Integer, Long>> recordList = getMileageRecords();
        int point = 0;
        for (Pair<Integer, Long> record : recordList) {
            point += record.getLeft();
        }
        return point;
    }

    public int rechargeMileage(final int quantity, final int type, final boolean limitMax, String log) {
        if (quantity <= 0) {
            return quantity == 0 ? 0 : -1;
        }
        int result = modifyMileage(quantity, limitMax);
        if (result != 0) {
            return result;
        }
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("INSERT INTO `mileage_recharge_record` (accId, mileage, type, log) VALUES (?, ?, ?, ?)");
            ps.setInt(1, getAccID());
            ps.setInt(2, quantity);
            ps.setInt(3, type);
            if (log == null) {
                log = type == 1 ? "購買儲值" : "活動儲值";
            }
            ps.setString(4, log);
            ps.executeUpdate();
        } catch (SQLException e) {
            return -1;
        }
        return 0;
    }

    public int modifyMileage(int quantity) {
        return modifyMileage(quantity, true);
    }

    public int modifyMileage(int quantity, final boolean limitMax) {
        List<Pair<Integer, Long>> recordList = null;
        if (quantity == 0) {
            return 0;
        } else if (quantity < 0) {
            recordList = getMileageRecords();
            int point = 0;
            for (Pair<Integer, Long> record : recordList) {
                point += record.getLeft();
            }
            if (point < Math.abs(quantity)) {
                return -1;
            }
        } else if (limitMax) {
            if (ServerConfig.mileageDailyLimitMax > 0 || ServerConfig.mileageMonthlyLimitMax > 0) {
                try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
                    int mileageDaily = 0;
                    int mileageMonthly = 0;
                    PreparedStatement ps;
                    ResultSet rs;
                    int point;
                    if (ServerConfig.mileageDailyLimitMax > 0) {
                        ps = con.prepareStatement("SELECT SUM(mileage) FROM mileage_recharge_record WHERE accId = ? AND DATE_FORMAT(Time, '%Y%m%d') = DATE_FORMAT(CURDATE(), '%Y%m%d')");
                        ps.setInt(1, getAccID());
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            point = rs.getInt(1);
                            if (point >= ServerConfig.mileageDailyLimitMax) {
                                return 1;
                            }
                            if (point + quantity > ServerConfig.mileageDailyLimitMax) {
                                quantity = ServerConfig.mileageDailyLimitMax - point;
                            }
                            mileageDaily = point + quantity;
                        }
                        ps.close();
                    }
                    if (ServerConfig.mileageMonthlyLimitMax > 0) {
                        ps = con.prepareStatement("SELECT SUM(mileage) FROM mileage_recharge_record WHERE accId = ? AND DATE_FORMAT(Time, '%Y%m') = DATE_FORMAT(CURDATE(), '%Y%m')");
                        ps.setInt(1, getAccID());
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            point = rs.getInt(1);
                            if (point >= ServerConfig.mileageMonthlyLimitMax) {
                                return 2;
                            }
                            if (point + quantity > ServerConfig.mileageMonthlyLimitMax) {
                                quantity = ServerConfig.mileageMonthlyLimitMax - point;
                            }
                            mileageMonthly = point + quantity;
                        }
                        ps.close();
                    }
                    announce(UIPacket.addPopupSay(9030200, 1100,
                            "里程上限："
                                    + (ServerConfig.mileageDailyLimitMax > 0 ? "\r\n每日：(" + mileageDaily + "/" + ServerConfig.mileageDailyLimitMax + ")" : "")
                                    + (ServerConfig.mileageMonthlyLimitMax > 0 ? "\r\n每月：(" + mileageMonthly + "/" + ServerConfig.mileageMonthlyLimitMax + ")" : ""),
                            ""));
                } catch (SQLException e) {
                }
            }
        }
        Calendar date = Calendar.getInstance();
        date.add(Calendar.MONTH, 1);
        date.set(Calendar.DAY_OF_MONTH, 0);
        date.set(Calendar.HOUR_OF_DAY, 9);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps;
            ResultSet rs;
            if (quantity < 0) {
                ps = con.prepareStatement("SELECT mileage FROM mileage_record WHERE accId = ? AND DATE_FORMAT(Time, '%Y%m%d') = ?");
                ps.setInt(1, getAccID());
                ps.setString(2, format.format(date.getTime()));
                rs = ps.executeQuery();
                if (rs.next()) {
                    int point = 0;
                    for (Pair<Integer, Long> record : recordList) {
                        if (format.format(new Date(record.getRight())).equalsIgnoreCase(format.format(date.getTime()))) {
                            point = record.getLeft();
                            break;
                        }
                    }
                    if (point + quantity <= 0) {
                        ps = con.prepareStatement("DELETE FROM mileage_record WHERE accId = ? AND DATE_FORMAT(Time, '%Y%m%d') = ?");
                        ps.setInt(1, getAccID());
                        ps.setString(2, format.format(date.getTime()));
                        ps.execute();
                        ps.close();
                        quantity = point + quantity;
                        if (quantity == 0) {
                            return 0;
                        }
                    } else {
                        ps = con.prepareStatement("UPDATE mileage_record SET mileage = mileage + ? WHERE accId = ? AND DATE_FORMAT(Time, '%Y%m%d') = ?");
                        ps.setInt(1, quantity);
                        ps.setInt(2, getAccID());
                        ps.setString(3, format.format(date.getTime()));
                        ps.executeUpdate();
                        return 0;
                    }
                }
            }

            date.add(Calendar.MONTH, 1);
            ps = con.prepareStatement("SELECT mileage FROM mileage_record WHERE accId = ? AND DATE_FORMAT(Time, '%Y%m%d') = ?");
            ps.setInt(1, getAccID());
            ps.setString(2, format.format(date.getTime()));
            rs = ps.executeQuery();
            if (rs.next()) {
                ps.close();
                if (quantity < 0) {
                    int point = 0;
                    for (Pair<Integer, Long> record : recordList) {
                        if (format.format(new Date(record.getRight())).equalsIgnoreCase(format.format(date.getTime()))) {
                            point = record.getLeft();
                            break;
                        }
                    }
                    if (point + quantity <= 0) {
                        ps = con.prepareStatement("DELETE FROM mileage_record WHERE accId = ? AND DATE_FORMAT(Time, '%Y%m%d') = ?");
                        ps.setInt(1, getAccID());
                        ps.setString(2, format.format(date.getTime()));
                        ps.execute();
                        ps.close();
                        return 0;
                    }
                }
                ps = con.prepareStatement("UPDATE mileage_record SET mileage = mileage + ? WHERE accId = ? AND DATE_FORMAT(Time, '%Y%m%d') = ?");
                ps.setInt(1, quantity);
                ps.setInt(2, getAccID());
                ps.setString(3, format.format(date.getTime()));
            } else {
                ps.close();
                if (quantity < 0) {
                    return -1;
                }
                ps = con.prepareStatement("INSERT INTO `mileage_record` (accId, mileage, Time) VALUES (?, ?, ?)");
                ps.setInt(1, getAccID());
                ps.setInt(2, quantity);
                ps.setTimestamp(3, new Timestamp(date.getTimeInMillis()));
            }
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            return -1;
        }
        return 0;
    }

    public Map<String, String> getAccInfoFromDB() {
        Map<String, String> ret = new HashMap<>();
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?")) {
                ps.setInt(1, accId);
                try (ResultSet rs = ps.executeQuery()) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    if (rs.next()) {
                        for (int i = 1; i <= metaData.getColumnCount(); i++) {
                            String result = "";
//                        if (metaData.getColumnTypeName(i).equalsIgnoreCase("DATE") || metaData.getColumnTypeName(i).equalsIgnoreCase("TIMESTAMP")) {
//                            result = rs.getDate(i).toString();
//                        } else {
                            result = rs.getString(metaData.getColumnName(i));
//                        }

                            ret.put(metaData.getColumnName(i), result);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.error("獲取帳號數據失敗", e);
        }
        return ret;
    }

    public void decryptOpcode(byte[] packet) {
        int b1 = packet[0] & 0xFF;
        int b2 = packet[1] & 0xFF;
        short op = (short) ((b2 << 8) + b1);
        if (encryptedOpcodes != null && encryptedOpcodes.containsKey(op)) {
            short nop = encryptedOpcodes.get(op);
            packet[0] = (byte) (nop & 0xFF);
            packet[1] = (byte) ((nop >> 8) & 0xFF);
        }
    }

    public byte[] getEncryptOpcodesData(String key) {
        if (Opcodes.isEmpty() || OpcodeEncryptPacket == null) {
            OpcodeEncryptPacket = getEncryptOpcodesData(key.getBytes());
            Opcodes.putAll(encryptedOpcodes);
        } else {
            encryptedOpcodes.clear();
            encryptedOpcodes.putAll(Opcodes);
        }
        return OpcodeEncryptPacket;
    }

    public byte[] getEncryptOpcodesData(byte[] keyBytes) {
        StringBuilder string = new StringBuilder();
        encryptedOpcodes.clear();
        // 計算加密總數，加到encryptedOpcodes
        short maxcount = (short)(InHeader.Count.getValue() - (InHeader.CP_BEGIN_USER.getValue() + 1));
        string.append(String.format("%d", maxcount));
        encryptedOpcodes.put(maxcount, InHeader.CP_BEGIN_USER.getValue());
        for (short i = (short)(InHeader.CP_BEGIN_USER.getValue()); i < InHeader.Count.getValue(); ++i) {
            short rand = 0;
            while (rand == 0 || encryptedOpcodes.containsKey(rand)) {
                rand = (short) Randomizer.rand(InHeader.CP_BEGIN_USER.getValue(), Short.MAX_VALUE);
            }
            final String randStr = String.format("|%d", rand);
            if (!encryptedOpcodes.containsKey(rand)) {
                encryptedOpcodes.put(rand, i);
                string.append(randStr);
            }
        }
        // v267
        String ss = "1648|10875|46358|36290|64449|62007|31199|47727|13498|16974|63052|41757|31165|46660|48866|50970|33335|36772|20770|30687|54305|61779|61454|17703|42364|22711|16882|14596|24330|55589|51266|57770|16004|33297|6039|50759|52430|19782|30159|38158|33711|23643|8201|45186|64227|60006|28074|25322|10952|28646|52869|26272|63827|52456|13569|43667|25410|62114|14697|44468|15606|44678|61131|59343|58238|34220|28046|12146|61665|34556|62526|16472|65325|10170|34398|28284|22838|52444|58051|41069|38639|22514|40273|20279|12736|6784|25077|9295|63016|18761|25353|13917|37129|32973|24845|31044|27996|57603|21567|48721|49602|15263|37469|54691|19869|32648|30368|5891|60031|27671|29602|5568|33644|7318|35332|33539|51137|28916|9472|45384|46534|40420|23514|52478|6123|29790|31210|54960|9637|9608|60552|28156|36876|31470|34224|23066|14700|26246|22052|23157|14144|41395|11568|29545|20652|21961|32671|51781|28416|12405|20377|31089|29551|18636|34371|20386|8040|55230|13413|64934|17805|61784|9165|59456|12011|37370|58114|34909|23774|21760|27076|32027|34425|32927|48050|32934|40462|63580|16755|15987|41250|20548|42039|42169|49360|51206|10804|23912|10590|31780|43122|39754|27668|24679|53269|47486|29977|57669|50248|63287|45318|26421|62005|17404|52169|22504|10057|61399|49622|35385|31255|6927|45289|53078|59172|52739|17883|51815|36651|13919|16344|49642|54921|35820|15664|22456|64012|59225|46973|8469|50089|24923|19469|48765|59540|32716|9576|50278|34217|30281|6647|48238|21839|10289|10366|13993|28719|15459|60186|50106|43902|57485|64350|27762|32273|46139|54856|37145|22287|25405|15495|5520|31408|5760|19801|26491|62233|44754|42989|45015|25940|23388|15414|56933|17491|20363|35437|17034|8063|9941|38182|53539|54670|40362|60005|61106|19334|51215|20024|46881|53564|6348|25673|64073|50039|28976|26206|15108|14079|52353|31291|33088|65509|17129|26254|6326|12128|19229|25811|23856|6111|54125|31747|32247|6091|8352|41777|28717|52804|18959|29311|20437|40358|37178|50087|7970|46565|55610|45847|57118|22630|20212|62590|22603|51303|37181|28825|64765|30289|44239|64595|39307|22290|14720|61394|24015|14863|25963|60011|28366|49038|50224|53657|47324|48966|40980|22770|8456|29390|54804|38919|50328|55164|38329|42456|42551|30367|34675|5929|18788|40865|57938|40027|6648|60565|24425|20981|36624|31339|33897|6497|55058|58212|22133|8153|6343|59500|60185|11422|8179|47109|16769|26110|58075|17192|35173|28330|32864|53261|33555|48280|46778|59471|26596|18808|6062|35951|65444|46720|15190|28769|19753|16966|51734|26780|11234|9466|17080|37706|10125|55395|18199|5884|26718|36087|65496|44853|50515|39377|26336|40513|34843|48989|63801|17806|42644|28100|41083|62001|7282|8913|50858|22107|65417|27057|58619|23337|40038|18038|12327|8069|11388|19269|55476|45096|7173|33985|21017|28608|30136|7167|13069|36373|33147|30375|24958|58777|32490|39572|62077|24306|23018|52438|24065|62716|44432|26327|44268|32526|37915|58847|45433|22534|16206|51766|39160|24424|43240|40272|18618|19438|54169|32207|62594|44426|24373|19498|10600|44705|22198|8835|31511|19154|19814|17781|14772|11085|25523|59046|45692|35717|14175|60293|18227|7448|33908|55693|26300|18351|9696|28816|42482|19340|12196|60259|34712|24627|43793|38001|29891|62517|12083|30808|20984|22041|58423|58732|24691|8892|21444|41716|30681|17946|32984|57695|64465|9681|10359|58828|19010|6137|46687|18073|5898|52697|13263|58290|5066|26665|46969|63816|23232|59191|25013|6604|30123|7250|26831|34997|28585|38014|64165|15904|41364|40404|54377|28024|52687|35747|40639|52126|32015|62931|17235|19590|58617|56655|30153|42586|42937|32111|25381|24416|35608|52235|29914|49238|35841|65127|10509|56980|13722|23211|17259|18391|42509|15323|9871|18395|20765|50283|56869|22896|58438|47996|34464|38368|48254|51914|54143|12956|64919|17220|21177|57622|30295|30060|53758|9611|31568|53700|58624|24187|56158|40155|35143|34880|64889|57846|30952|11769|30254|10799|9337|15649|44598|62975|38761|61678|63613|35180|49682|55175|34513|16366|31389|51828|29469|54589|40659|48886|41867|60859|38760|52136|17714|45222|35766|34322|26644|36496|58487|11016|31251|52572|14916|16043|54373|45250|61648|17610|50976|8476|19430|18408|49793|23188|36826|35824|21559|19561|29587|46873|8319|31802|59183|13752|43438|38498|49856|49328|57814|54951|45827|53919|42480|33814|50975|11010|30761|13530|33845|61002|39557|60692|20922|46504|58317|29899|50373|39891|17904|13449|30350|60572|24850|7404|11941|5795|52142|33376|14958|55756|46576|63465|21146|37470|6627|18413|42996|60102|27493|45137|31179|27802|18334|39582|20698|62389|65107|31452|50048|52924|7307|10405|16786|19937|56777|41538|44636|60926|20833|52854|51130|12210|19219|38272|28786|35866|11658|11452|5231|60741|11966|8590|54700|9702|21343|36273|20385|10493|15914|6694|65038|13595|41444|57406|17395|47445|13133|65370|28447|34975|61409|64729|38709|47572|23178|28866|53477|58921|6306|65045|56409|41846|17787|60777|36988|41841|50366|48505|22527|33412|34897|46850|35567|26442|24109|48315|7986|13418|32026|45942|20241|22648|29064|62479|56027|8863|26453|5432|43743|33537|39766|25767|7310|16271|10656|17333|57934|58474|54862|47138|64556|19069|45326|44821|38746|25803|21729|5178|41355|18090|7496|49813|54383|13985|40572|51775|12355|34394|24718|51779|29327|44381|6855|60308|38372|56752|22444|41780|27408|50070|32345|59299|9947|38138|12878|31241|39708|22500|59765|64961|43180|53365|17645|5980|43857|58306|22228|41407|12984|45974|5462|43917|36967|35201|34343|35976|9989|25964|44931|9957|30230|39974|6176|59762|15995|55203|46780|64642|18224|5341|16739|30889|18591|54896|13210|10832|6592|11624|12908|33116|11636|30019|17901|20358|65121|16187|15964|28642|28915|27223|17180|28650|14435|36834|64122|28302|47311|41451|29713|60489|13378|23365|59434|21154|44397|63894|28239|20815|20428|36765|51160|8241|54238|65188|26493|21596|43363|39842|38411|57727|39627|23970|17923|47581|12304|44858|15175|49228|23430|36175|57478|50123|59112|18791|31564|12766|46869|9117|39345|41684|58557|7283|19264|18370|6808|15552|16085|60720|41293|32260|44024|50537|49811|52603|24989|34848|5796|45148|58143|10993|19740|45484|31830|24204|30413|11502|61142|32557|46258|51642|34170|7638|52445|17249|41138|53983|55281|29924|46507|18998|56055|53346|28031|9711|35169|61343|64396|25883|34955|37601|54640|43868|52862|5045|31279|23492|11063|24341|37104|17088|56020|43824|58988|10448|31540|61573|23493|12976|54416|26116|38831|53562|35077|42849|28727|39699|50296|39348|23723|51663|43348|45003|54444|64927|13663|28507|9101|59737|51808|54086|48526|47825|15484|18571|61422|19614|18380|20163|25419|53713|9830|47228|23537|60676|6887|18028|39383|8971|10695|33810|21450|15700|17453|41387|62893|54034|47377|9726|39431|61837|61952|30640|23227|41959|11926|12020|42380|62864|63304|33798|22544|10853|55387|27990|15830|51504|43582|48758|60002|55452|36838|54681|29999|20943|42990|26837|39724|30611|9308|8208|8202|27295|21011|27260|34905|64629|58370|22669|17495|65455|45959|64596|5152|37556|62954|6823|29278|32796|42495|45691|39217|41315|53626|64392|53556|6333|58336|5054|12344|36694|62610|35441|60984|46959|59877|49835|29450|61602|39904|31365|40480|63536|45891|33760|44517|20099|5482|30479|49035|17385|31513|6358|19827|16945|44704|43298|46195|59442|23670|61985|41106|36901|15453|21008|47776|19461|18711|31425|24813|39917|43169|56964|25990|47575|63747|11435|11738|17045|38179|16773|59427|17526|60427|15741|20176|8438|21220|16234|51689|45750|41057|64158|12995|60942|8114|56329|63130|33324|38142|44290|40912|34406|31396|50981|12316|63731|46295|65150|47166|10539|45850|16154|39623|28410|33450|26335|52773|6584|21615|29585|53270|57866|13108|18583|51391|6810|16499|19925|43140|14748|55700|30377|53333|14681|15884|27686|6680|16011|42589|48841|48400|33219|48756|12113|22464|32712|14789|57518|19987|7710|52496|30639|34919|13410|27289|37994|57737|64068|55552|9674|15935|46814|43041|36029|43830|47381|8890|26245|7792|43429|12904|56439|43529|15170|51586|13013|39791|9925|28643|36958|17685|46392|62028|8994|60161|59106|10496|20468|13801|45189|44504|49042|13675|57722|23330|30860|48576|12540|8918|34537|62989|54184|54544|18714|48000|55065|19515|38801|12388|17012|45226|33856|62379|20734|9497|13358|24431|48464|5518|42401|31232|39407|52596|26235|35117|44367|35243|45979|48988|39609|33924|38736|60018|27108|48808|51328|18775|26325|12846|16661|6926|20245|52884|58054|50906|47108|35126|28611|52564|60280|37541|8750|62353|15177|36517|51040|30387|57032|52968|46836|31621|51872|19309|30584|39356|43514|9698|9240|39995|50112|62100|10370|36867|40532|11570|11316|50023|51353|24334|46113|50666|6448|49638|6923|28651|8574|38533|57287|54464|8714|27537|11877|15567|7704|59665|49729|62107|21755|46264|38690|18789|30534|18033|53522|42614|54252|40277|27746|40252|43954|57203|61656|63535|30009|39215|59981|31123|62097|34976|11284|16556|24988|61416|39706|55043|55542|13496|6148|47559|20378|48363|29350|14376|46005|33291|41723|42236|52707|24260|49594|25754|11229|44360|22910|34887|10253|49070|24484|40566|31848|10440|39857|32487|43279|55385|6683|29131|43077|13780|26968|14096|56652|24595|49164|42148|29537|47590|56688|59245|49008|51709|44837|54840|23449|20522|43314|22889|62290|31883|6899|10004|34652|34338|6125|64403|57922|33005|41957|58986|21539|55685|50104|32002|5059|39339|28495|39606|29276|40419|17719|15690|48263|54924|17330|48122|64755|65082|28513|7266|23672|32841|27737|58536|36375|34714|25743|8414|29626|40099|25820|16604|30366|52871|60751|21049|10385|56035|24426|60287|8555|52965|16042|23448|12894|45149|36081|45229|6322|39322|45107|41149|33172|51690|30246|50772|44281|29607|6924|21993|15946|55207|30381|31811|55426|49329|49392|38834|41552|30488|50799|13796|5855|7867|7901|10471|57743|5504|16749|52755|59374|43242|13381|40318|60099|46373|37303|11419|48004|45241|52525|50673|60240|19389|42828|53561|25751|59200|10086|62397|45086|56193|13052|11978|29676|44213|39548|63969|51533|5451|59521|27827|56389|19913|35706|22781|18696|50383|42655|45077|43139|31504|65012|63397|50963|8746|61915";
        String[] sp = ss.split("\\|");
        for (short i = 1; i < sp.length - 1; i++) {
            encryptedOpcodes.put((short) Integer.parseInt(sp[i]), (short) (InHeader.CP_BEGIN_USER.getValue() + i));
        }
        try {
            MaplePacketLittleEndianWriter encodeData = new MaplePacketLittleEndianWriter();
            encodeData.writeAsciiString(ss);
            Cipher cipher = Cipher.getInstance("DESede");
            final byte[] dKey = new byte[24];
            System.arraycopy(keyBytes, 0, dKey, 0, Math.min(dKey.length, keyBytes.length));
            if (keyBytes.length < dKey.length) {
                System.arraycopy(dKey, 0, dKey, keyBytes.length, dKey.length - keyBytes.length);
            }
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(dKey, "DESede"));
            final byte[] crypted = cipher.doFinal(encodeData.getPacket());

            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(OutHeader.LP_OpcodeEncryption.getValue());
            mplew.writeInt(crypted.length);
            mplew.write(crypted);
            return mplew.getPacket();
        } catch (Exception ex) {
            log.error("EncryptedOpcodes Error!", ex);
        }
        return new byte[0];
    }


    /**
     * 获取自己邀请码
     */
    public String getRefCode() {
        if (!Auth.checkPermission("InviteRebate")) {
            return null;
        }
        String refCode = SqlTool.queryAndGet("select ref_code from accounts where id = ?", rs -> rs.getString(1), accId);
        if (refCode == null) {
            while (refCode == null) {
                char[] ss = new char[6];

                for (int i = 0; i < ss.length; ++i) {
                    int f = (int) (Math.random() * 3.0D);
                    if (f == 0) {
                        ss[i] = (char) ((int) (65.0D + Math.random() * 14.0D));
                    } else if (f == 1) {
                        ss[i] = (char) ((int) (80.0D + Math.random() * 11.0D));
                    } else {
                        ss[i] = (char) ((int) (49.0D + Math.random() * 9.0D));
                    }
                }
                refCode = new String(ss);
                if (SqlTool.queryAndGet("select ref_code from accounts where ref_code = ?", rs -> rs.getString(1), refCode) != null) {
                    refCode = null;
                }
            }
            SqlTool.update("UPDATE accounts SET ref_code = ? WHERE id = ?", refCode, accId);
        }
        return refCode;
    }

    /**
     * 获取自己邀请的下级总人数
     */
    public int getRefCount(int chargeAmount) {
        if (!Auth.checkPermission("InviteRebate")) {
            return 0;
        }
        try {
            return SqlTool.queryAndGet("select count(accounts.id) from accounts, hypay where accounts.name = hypay.accname and accounts.up_id = ? and hypay.payUsed >= ?", rs -> rs.getInt(1), accId, chargeAmount);
        } catch (Exception e) {
            return 0;
        }
    }

    public int getUpRefCount(int chargeAmount) {
        if (!Auth.checkPermission("InviteRebate")) {
            return 0;
        }
        try {
            return SqlTool.queryAndGet("select count(accounts.id) from accounts, hypay where accounts.name = hypay.accname and accounts.up_id = ? and hypay.payUsed >= ?", rs -> rs.getInt(1), getUpId(), chargeAmount);
        } catch (Exception e) {
            return 0;
        }
    }

    public int getUpId() {
        if (!Auth.checkPermission("InviteRebate")) {
            return -1;
        }
        try {
            return SqlTool.queryAndGet("select up_id from accounts where id = ?", rs -> rs.getInt(1), accId);
        } catch (Exception e) {
            return -1;
        }
    }

    public int setUpRefCode(String upRefCode) {
        if (!Auth.checkPermission("InviteRebate")) {
            return -1;
        }
        String upName = SqlTool.queryAndGet("select name from accounts where ref_code = ? and id <> ?", rs -> rs.getString(1), upRefCode, accId);
        if (upName == null) {
            return 1;
        }
        int upId = SqlTool.queryAndGet("select id from accounts where ref_code = ? and id <> ?", rs -> rs.getInt(1), upRefCode, accId);
        try {
            SqlTool.update("UPDATE accounts SET up_ref_code = ? WHERE id = ?", upRefCode, accId);
            SqlTool.update("UPDATE accounts SET up_id = ? WHERE id = ?", upId, accId);
            SqlTool.update("UPDATE accounts SET up_name = ? WHERE id = ?", upName, accId);
            SqlTool.update("UPDATE accounts SET ref_time = CURRENT_TIMESTAMP() WHERE id = ?", accId);
        } catch (Exception e) {
            return -1;
        }
        return 0;
    }

    public void setSessionIdx(int sessionIdx) {
        this.sessionIdx = sessionIdx;
    }

    public int getSessionIdx() {
        return sessionIdx;
    }

    public void dispose() {
        announce(MaplePacketCreator.ExclRequest());
    }

    public int size() {
        return 0;
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean contains(Object o) {
        return false;
    }

    public Iterator<Triple<Point, Integer, List<Rectangle>>> iterator() {
        return null;
    }

    public Object[] toArray() {
        return new Object[0];
    }

    public <T> T[] toArray(T[] a) {
        return null;
    }

    public boolean add(Triple<Point, Integer, List<Rectangle>> pointIntegerListTriple) {
        return false;
    }

    public boolean remove(Object o) {
        return false;
    }

    public boolean containsAll(Collection<?> c) {
        return false;
    }

    public boolean addAll(Collection<? extends Triple<Point, Integer, List<Rectangle>>> c) {
        return false;
    }

    public boolean addAll(int index, Collection<? extends Triple<Point, Integer, List<Rectangle>>> c) {
        return false;
    }


    public boolean removeAll(Collection<?> c) {
        return false;
    }

    public boolean retainAll(Collection<?> c) {
        return false;
    }

    public void clear() {
    }

    public Triple<Point, Integer, List<Rectangle>> get(int index) {
        return null;
    }

    public Triple<Point, Integer, List<Rectangle>> set(int index, Triple<Point, Integer, List<Rectangle>> element) {
        return null;
    }

    public void add(int index, Triple<Point, Integer, List<Rectangle>> element) {
    }

    public Triple<Point, Integer, List<Rectangle>> remove(int index) {
        return null;
    }

    public int indexOf(Object o) {
        return 0;
    }

    public int lastIndexOf(Object o) {
        return 0;
    }

    public ListIterator<Triple<Point, Integer, List<Rectangle>>> listIterator() {
        return null;
    }

    public ListIterator<Triple<Point, Integer, List<Rectangle>>> listIterator(int index) {
        return null;
    }

    public List<Triple<Point, Integer, List<Rectangle>>> subList(int fromIndex, int toIndex) {
        return null;
    }

    public MapleCharacter getRandomCharacter() {
        MapleCharacter chr = null;
        List<MapleCharacter> players = new ArrayList<>();
        if (getPlayer().getMap().getCharacters().size() > 0) {
            players.addAll(getPlayer().getMap().getCharacters());
            Collections.addAll(players);
            Collections.shuffle(players);
            for (MapleCharacter chr3 : players) {
                if (chr3.isAlive()) {
                    chr = chr3;
                    break;
                }
            }
        } else {
            return null;
        }
        return chr;
    }

    public String getName() {
        return this.name;
    }

    protected static class CharNameAndId {

        public String name;
        public final int id;

        public CharNameAndId(String name, int id) {
            super();
            this.name = name;
            this.id = id;
        }
    }
}
