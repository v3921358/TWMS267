package SwordieX.client.character.keys;

import connection.OutPacket;
import lombok.Getter;
import lombok.Setter;
import tools.data.MaplePacketLittleEndianWriter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


@Setter
@Getter
public class FuncKeyMap {
    private Map<Integer, Keymapping> keymaps;
    private boolean changed = false;
    private int slot = 0;
    /**
     * 琳恩(幻獸師) 切換精靈召喚模式用到
     */
    private int mode = 1;
    public final static int MAX_LAYOUT = 3;
    private int maxKeyBinds = 89;
    private int maxCombination = 10;

    public FuncKeyMap(Map<Integer, Keymapping> keymaps) {
        this.keymaps = keymaps;
    }

    public static void init(Connection con, int charid, boolean oldkey) throws SQLException {
        //以前的模式
        int[] array1 = {1, 2, 3, 4, 5, 6, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 29, 31, 34, 35, 37, 38, 39, 40, 41, 43, 44, 45, 46, 47, 48, 50, 56, 57, 59, 60, 61, 63, 64, 65, 66, 70};
        int[] array2 = {4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 4, 4, 4, 4, 5, 5, 6, 6, 6, 6, 6, 6, 6, 4};
        int[] array3 = {46, 10, 12, 13, 18, 23, 8, 5, 0, 4, 27, 30, 39, 1, 41, 19, 14, 15, 52, 2, 17, 11, 3, 20, 26, 16, 22, 9, 50, 51, 6, 31, 29, 7, 53, 54, 100, 101, 102, 103, 104, 105, 106, 47};
        //新的鍵盤模式
        int[] new_array1 = {1, 20, 21, 22, 23, 25, 26, 27, 29, 34, 35, 36, 37, 38, 39, 40, 41, 43, 44, 45, 46, 47, 48, 49, 50, 52, 56, 57, 59, 60, 61, 63, 64, 65, 66, 70, 71, 73, 79, 82, 83};
        int[] new_array2 = {4, 4, 4, 4, 4, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 4, 4, 4, 4, 4, 4, 5, 5, 6, 6, 6, 6, 6, 6, 6, 4, 4, 4, 4, 4, 4};
        int[] new_array3 = {46, 27, 30, 0, 1, 19, 14, 15, 52, 17, 11, 8, 3, 20, 26, 16, 22, 9, 50, 51, 2, 31, 29, 5, 7, 4, 53, 54, 100, 101, 102, 103, 104, 105, 106, 47, 12, 13, 23, 10, 18};

        PreparedStatement ps = con.prepareStatement("INSERT INTO keymap (characterid, `slot`, `key`, `type`, `action`) VALUES (?, ?, ?, ?, ?)");
        ps.setInt(1, charid);
        ps.setInt(2, 0);
        int keylength = oldkey ? array1.length : new_array1.length;
        for (int j = 0; j < keylength; j++) {
            ps.setInt(3, oldkey ? array1[j] : new_array1[j]);
            ps.setInt(4, oldkey ? array2[j] : new_array2[j]);
            ps.setInt(5, oldkey ? array3[j] : new_array3[j]);
            ps.execute();
        }
        ps.close();
    }

    public static Map<Integer, FuncKeyMap> load(Connection con, int charid, int slot, Map<Integer, FuncKeyMap> funcKeyMaps) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT `key`,`type`,`action` FROM keymap WHERE characterid = ? AND slot = ?");
        ps.setInt(1, charid);
        ps.setInt(2, slot);
        ResultSet rs = ps.executeQuery();
        Map<Integer, Keymapping> keymaps = new HashMap<>();
        while (rs.next()) {
            keymaps.put(rs.getInt("key"), new Keymapping(rs.getByte("type"), rs.getInt("action")));
        }
        funcKeyMaps.put(slot, new FuncKeyMap(keymaps));
        rs.close();
        ps.close();
        return funcKeyMaps;
    }

    public void save(Connection con, int charid, int slot) throws SQLException {
        if (!changed) {
            return;
        }
        PreparedStatement ps = con.prepareStatement("DELETE FROM keymap WHERE characterid = ? AND slot = ?");
        ps.setInt(1, charid);
        ps.setInt(2, slot);
        ps.execute();
        ps.close();
        if (keymaps.isEmpty()) {
            return;
        }

        ps = con.prepareStatement("INSERT INTO keymap VALUES (DEFAULT, ?, ?, ?, ?, ?)");
        ps.setInt(1, charid);
        ps.setInt(2, slot);
        for (Map.Entry<Integer, Keymapping> keybinding : keymaps.entrySet()) {
            ps.setInt(3, keybinding.getKey());
            ps.setByte(4, keybinding.getValue().getType());
            ps.setInt(5, keybinding.getValue().getAction());
            ps.execute();
        }
        ps.close();
    }

    public void encode(MaplePacketLittleEndianWriter mplew) {
        OutPacket outPacket = new OutPacket();
        encode(outPacket);
        mplew.write(outPacket.getData());
    }

    public void encode(OutPacket outPacket) {
        outPacket.encodeByte(keymaps.isEmpty() ? 1 : 0);
        if (!keymaps.isEmpty()) {
            Keymapping binding;
            // 基本按鍵
            for (int i = 0; i < mode; i++) {
                for (int x = 0; x < maxKeyBinds; x++) {
                    binding = keymaps.get(x);
                    if (binding != null) {
                        outPacket.encodeByte(binding.getType());
                        outPacket.encodeInt(binding.getAction());
                    } else {
                        outPacket.encodeByte(0);
                        outPacket.encodeInt(0);
                    }
                }
            }
            // 組合鍵
            for (int i = 0; i < maxCombination; i++) {
                binding = keymaps.get(102 + i);
                if (binding != null) {
                    outPacket.encodeByte(binding.getType());
                    outPacket.encodeInt(binding.getAction());
                } else {
                    outPacket.encodeByte(0);
                    outPacket.encodeInt(0);
                }
            }
        }
    }
}
