package Database.dao;

import Database.DatabaseLoader.DatabaseConnection;
import Database.tools.SqlTool;
import Server.world.WorldGuildService;
import tools.Pair;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class AccountDao {
    public static List<Pair<Integer, Long>> getPendingDeleteChrId(int accId, int world) {
        return SqlTool.queryAndGetList("SELECT `character_id`, `time` FROM `accounts_deletechr` WHERE `account_id` = ? AND `world` = ?", rs -> new Pair<>(rs.getInt(1), rs.getTimestamp("time").getTime()), accId, world);
    }


    public static void clearOutdatedPendingDeleteChr(int accId, int worldId) {
        List<Pair<Integer, Long>> list = getPendingDeleteChrId(accId, worldId);
        for (Pair<Integer, Long> result : list) {
            if (System.currentTimeMillis() - result.getRight() > 2 * 24 * 60 * 60 * 1000L) {
                deregisterDeleteChr(accId, worldId, result.getLeft());
                deleteCharacter(accId, result.getLeft());
            }
        }
    }


    public static void registerDeleteChr(int accId, int world, int chrId) {
        SqlTool.update("INSERT INTO `accounts_deletechr` (account_id, world, character_id) VALUES (?, ?, ?)", accId, world, chrId);
    }

    public static void deregisterDeleteChr(int accId, int world, int chrId) {
        SqlTool.update("DELETE FROM `accounts_deletechr` WHERE `account_id` = ? AND `world` = ? AND `character_id` = ?", accId, world, chrId);
    }

    public static int deleteCharacter(int accId, int cid) {
        Integer result = DatabaseConnection.domain(con -> {
            PreparedStatement ps = con.prepareStatement("SELECT guildid, guildrank, familyid, name FROM characters WHERE id = ? AND accountid = ?");
            ps.setInt(1, cid);
            ps.setInt(2, accId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return 1;
            }
            if (rs.getInt("guildid") > 0) { // is in a guild when deleted
                if (rs.getInt("guildrank") == 1) { //cant delete when leader
                    rs.close();
                    ps.close();
                    return 1;
                }
                WorldGuildService.getInstance().deleteGuildCharacter(rs.getInt("guildid"), cid);
            }
            SqlTool.update(con, "DELETE FROM characters WHERE id = ?", cid);
            SqlTool.update(con, "DELETE FROM hiredmerch WHERE characterid = ?", cid);
            SqlTool.update(con, "DELETE FROM mts_cart WHERE characterid = ?", cid);
            SqlTool.update(con, "DELETE FROM mts_items WHERE characterid = ?", cid);
            SqlTool.update(con, "DELETE FROM cheatlog WHERE characterid = ?", cid);
            SqlTool.update(con, "DELETE FROM mountdata WHERE characterid = ?", cid);
            SqlTool.update(con, "DELETE FROM inventoryitems WHERE characterid = ?", cid);
            SqlTool.update(con, "DELETE FROM famelog WHERE characterid = ?", cid);
            SqlTool.update(con, "DELETE FROM famelog WHERE characterid_to = ?", cid);
            SqlTool.update(con, "DELETE FROM dueypackages WHERE RecieverId = ?", cid);
            SqlTool.update(con, "DELETE FROM wishlist WHERE characterid = ?", cid); //商城購物車道具
            SqlTool.update(con, "DELETE FROM buddies WHERE characterid = ?", cid);
            SqlTool.update(con, "DELETE FROM buddies WHERE buddyid = ?", cid);
            SqlTool.update(con, "DELETE FROM keymap WHERE characterid = ?", cid); //鍵盤設置
            SqlTool.update(con, "DELETE FROM trocklocations WHERE characterid = ?", cid);
            SqlTool.update(con, "DELETE FROM savedlocations WHERE characterid = ?", cid);
            SqlTool.update(con, "DELETE FROM skills WHERE characterid = ?", cid); //技能信息
            SqlTool.update(con, "DELETE FROM skills WHERE teachId = ?", cid); //技能信息
            SqlTool.update(con, "DELETE FROM familiars WHERE characterid = ?", cid);
            SqlTool.update(con, "DELETE FROM mountdata WHERE characterid = ?", cid); //坐騎信息
            SqlTool.update(con, "DELETE FROM skillmacros WHERE characterid = ?", cid); //技能宏信息
            SqlTool.update(con, "DELETE FROM queststatus WHERE characterid = ?", cid); //任務信息
            SqlTool.update(con, "DELETE FROM inventoryslot WHERE characters_id = ?", cid); //背包空間數量信息
            SqlTool.update(con, "DELETE FROM bank WHERE charid = ?", cid); //銀行系統
            SqlTool.update(con, "DELETE FROM bosslog WHERE characterid = ?", cid); //BOSS挑戰日誌
            return 0;
        }, "刪除角色出錯");
        return result == null ? 1 : result;
    }
}
