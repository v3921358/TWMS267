package Client;

import Config.constants.ServerConstants;
import Database.DatabaseLoader.DatabaseConnection;
import Database.DatabaseLoader.DatabaseConnectionEx;
import Database.tools.SqlTool;
import Net.server.MapleItemInformationProvider;
import Packet.MTSCSPacket;
import Server.login.LoginInformationProvider;
import Server.world.WorldFindService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Pair;
import tools.Triple;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MapleCharacterUtil {

    /**
     * Logger for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(MapleCharacterUtil.class);
    private static final Pattern namePattern = Pattern.compile("^(?!_)(?!.*?_$)[a-zA-Z0-9_\u4e00-\u9fa5]+$");
    private static final Pattern petPattern = Pattern.compile("^(?!_)(?!.*?_$)[a-zA-Z0-9_\u4e00-\u9fa5]+$");
    private static final Charset CHARSET = ServerConstants.MapleType.getByType(ServerConstants.MapleRegion).getCharset();

    public static boolean canCreateChar(String name, boolean gm) {
        return getIdByName(name) == -1 && isEligibleCharName(name, gm) && (!LoginInformationProvider.getInstance().isForbiddenName(name) || gm);
    }

    public static boolean canChangePetName(String name) {
        return name.getBytes(CHARSET).length >= 4 && name.getBytes(CHARSET).length <= 13 && petPattern.matcher(name).matches() && !LoginInformationProvider.getInstance().isForbiddenName(name);
    }

    public static boolean isEligibleCharName(String name, boolean gm) {
        String replaceAll = name.replaceAll("[^\\x00-\\xff]", "**");
        if (replaceAll.getBytes(CHARSET).length < 4 || replaceAll.getBytes(CHARSET).length > 15) {
            return false;
        }
        if (gm) {
            return true;
        }
        return namePattern.matcher(name).matches();
    }

    public static String makeMapleReadable(String in) {
        String wui = in.replace('I', 'i');
        wui = wui.replace('l', 'L');
        wui = wui.replace("rn", "Rn");
        wui = wui.replace("vv", "Vv");
        wui = wui.replace("VV", "Vv");
        return wui;
    }

    /*
     * 角色名字獲取角色ID
     */
    public static int getIdByName(String name) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT id FROM characters WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            int id = rs.getInt("id");
            rs.close();
            ps.close();

            return id;
        } catch (SQLException e) {
            log.error("error 'getIdByName' " + e);
        }
        return -1;
    }

    /*
     * 從角色ID獲取角色名字和帳號ID
     */
    public static Pair<String, Integer> getNameById(int chrId, int world) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE id = ? AND world = ?");
            ps.setInt(1, chrId);
            ps.setInt(2, world);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return null;
            }
            Pair<String, Integer> id = new Pair<>(rs.getString("name"), rs.getInt("accountid"));
            rs.close();
            ps.close();
            return id;
        } catch (Exception e) {
            log.error("error 'getInfoByName' " + e);
        }
        return null;
    }

    public static boolean PromptPoll(int accountid) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean prompt = false;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            ps = con.prepareStatement("SELECT * FROM game_poll_reply WHERE AccountId = ?");
            ps.setInt(1, accountid);
            rs = ps.executeQuery();
            prompt = !rs.next();
        } catch (SQLException ignored) {
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {
            }
        }
        return prompt;
    }

    public static boolean SetPoll(int accountid, int selection) {
        if (!PromptPoll(accountid)) { // Hacking OR spamming the db.
            return false;
        }
        PreparedStatement ps = null;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            ps = con.prepareStatement("INSERT INTO game_poll_reply (AccountId, SelectAns) VALUES (?, ?)");
            ps.setInt(1, accountid);
            ps.setInt(2, selection);

            ps.execute();
        } catch (SQLException ignored) {
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ignored) {
            }
        }
        return true;
    }

    /*
     * -2 = 出現未知的錯誤
     * -1 = 沒有找到賬號信息
     * 0 = 未設置二級密碼.
     * 1 = 輸入的二級密碼錯誤
     * 2 = 二級密碼修改成功
     */
    public static int Change_SecondPassword(int accid, String password, String newpassword) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * from accounts where id = ?");
            ps.setInt(1, accid);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            String secondPassword = rs.getString("2ndpassword");
            String salt2 = rs.getString("salt2");
            if (secondPassword != null && salt2 != null) {
                secondPassword = LoginCrypto.rand_r(secondPassword);
            } else if (secondPassword == null && salt2 == null) {
                rs.close();
                ps.close();
                return 0;
            }
            if (!check_ifPasswordEquals(secondPassword, password, salt2)) {
                rs.close();
                ps.close();
                return 1;
            }
            rs.close();
            ps.close();
            String SHA1hashedsecond;
            try {
                SHA1hashedsecond = LoginCryptoLegacy.encodeSHA1(newpassword);
            } catch (Exception e) {
                return -2;
            }
            ps = con.prepareStatement("UPDATE accounts set 2ndpassword = ?, salt2 = ? where id = ?");
            ps.setString(1, SHA1hashedsecond);
            ps.setString(2, null);
            ps.setInt(3, accid);
            if (!ps.execute()) {
                ps.close();
                return 2;
            }
            ps.close();
            return -2;
        } catch (SQLException e) {
            log.error("修改二級密碼發生錯誤" + e);
            return -2;
        }
    }

    private static boolean check_ifPasswordEquals(String passhash, String pwd, String salt) {
        // Check if the passwords are correct here. :B
        if (LoginCryptoLegacy.isLegacyPassword(passhash) && LoginCryptoLegacy.checkPassword(pwd, passhash)) {
            // Check if a password upgrade is needed.
            return true;
        } else if (salt == null && LoginCrypto.checkSha1Hash(passhash, pwd)) {
            return true;
        } else return LoginCrypto.checkSaltedSha512Hash(passhash, pwd, salt);
    }

    //id accountid gender
    public static Triple<Integer, Integer, Integer> getInfoByName(String name, int world) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE name = ? AND world = ?");
            ps.setString(1, name);
            ps.setInt(2, world);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return null;
            }
            Triple<Integer, Integer, Integer> id = new Triple<>(rs.getInt("id"), rs.getInt("accountid"), rs.getInt("gender"));
            rs.close();
            ps.close();
            return id;
        } catch (Exception e) {
            log.error("error 'getInfoByName' " + e);
        }
        return null;
    }

    public static void setNXCodeUsed(String name, String code) throws SQLException {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE nxcode SET `user` = ?, `valid` = 0, time = CURRENT_TIMESTAMP() WHERE code = ?");
            ps.setString(1, name);
            ps.setString(2, code);
            ps.execute();
            ps.close();
        }
    }

    public static void sendNote(int characters_id, String sender, String message, int type) {
        if (characters_id == -1) return;
        SqlTool.update("INSERT INTO `character_memo` (`characters_id`, `sender`, `message`, `type`) VALUES (?, ?, ?, ?)", characters_id, sender, message, type);
        MapleCharacter chr = WorldFindService.getInstance().findCharacterById(characters_id);
        showNote(chr);
    }

    public static void showNote(MapleCharacter chr) {
        if (chr != null && !getMemoByChrID(chr.getId()).isEmpty()) {
            chr.getClient().announce(MTSCSPacket.MemoReceive());
        }
    }

    public static Triple<Boolean, Integer, Integer> getNXCodeInfo(String code) throws SQLException {
        Triple<Boolean, Integer, Integer> ret = null;
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT `valid`, `type`, `item` FROM nxcode WHERE code = ?");
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ret = new Triple<>(rs.getInt("valid") > 0, rs.getInt("type"), rs.getInt("item"));
            }
            rs.close();
            ps.close();
            return ret;
        }
    }

    public static void addToItemSearch(int itemId) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM itemsearch WHERE itemid = ?");
            ps.setInt(1, itemId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("count");
                PreparedStatement psu = con.prepareStatement("UPDATE itemsearch SET count = ? WHERE itemid = ?");
                psu.setInt(1, count + 1);
                psu.setInt(2, itemId);
                psu.executeUpdate();
                psu.close();
            } else {
                PreparedStatement psi = con.prepareStatement("INSERT INTO itemsearch (itemid, count, itemName) VALUES (?, ?, ?)");
                psi.setInt(1, itemId);
                psi.setInt(2, 1);
                psi.setString(3, ii.getName(itemId));
                psi.executeUpdate();
                psi.close();
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            log.error("addToItemSearch", e);

        }
    }

    public static Pair<Integer, Integer> getCashByAccId(int AccId) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ? ");
            ps.setInt(1, AccId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return null;
            }
            Pair<Integer, Integer> id = new Pair<>(rs.getInt("ACash"), rs.getInt("mPoints"));
            rs.close();
            ps.close();
            return id;
        } catch (Exception e) {
            log.error("error 'getInfoByName' " + e);
        }
        return null;
    }

    public static List<MemoEntry> getMemoByChrID(int id) {
        return DatabaseConnection.domain(con -> {
            List<MemoEntry> list = new ArrayList<>();
            ResultSet rs = SqlTool.query(con, "SELECT * FROM `character_memo` WHERE `characters_id` = ?", id);
            int idx = 0;
            while (rs.next()) {
                ++idx;
                list.add(new MemoEntry(idx, rs.getString("sender"), rs.getString("message"), rs.getTimestamp("timestamp").getTime(), rs.getByte("type")));
                if (idx >= 200) {
                    break;
                }
            }
            return list;
        });
    }
}
