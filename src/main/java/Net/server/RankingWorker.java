package Net.server;

import Client.MapleJob;
import Config.configs.ServerConfig;
import Database.DatabaseLoader.DatabaseConnection;
import Net.server.Timer.WorldTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.StringUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class RankingWorker {

    private final static Logger log = LoggerFactory.getLogger(RankingWorker.class);
    private final static Map<Integer, List<RankingInformation>> rankings = new HashMap<>();
    private final static Map<String, Integer> jobCommands = new HashMap<>();
    private final static List<Integer> itemSearch = new ArrayList<>(); //熱門搜索道具

    public static Integer getJobCommand(String job) {
        return jobCommands.get(job);
    }

    public static Map<String, Integer> getJobCommands() {
        return jobCommands;
    }

    public static List<RankingInformation> getRankingInfo(int job) {
        return rankings.get(job);
    }

    public static List<Integer> getItemSearch() {
        return itemSearch;
    }

    public static void start() {
        //log.info("系統自動更新玩家排名功能已啟動..."); /* 暫時移除告知 */
        //log.info("更新間隔時間為: " + ServerConfig.WORLD_REFRESHRANK + " 分鐘1次。"); /* 暫時移除告知 */
        WorldTimer.getInstance().register(() -> {
            jobCommands.clear();
            rankings.clear();
            itemSearch.clear();
            updateRank();
        }, 1000L * 60 * ServerConfig.WORLD_REFRESHRANK); //4小時刷新1次排名
    }

    public static void updateRank() {
//        log.info("開始更新玩家排名...");
//        long startTime = System.currentTimeMillis();
        loadJobCommands();
        DatabaseConnection.domain(con -> {
            updateRanking(con);
            updateItemSearch(con);
            return null;
        }, "更新玩家排名出錯");
    }

    private static void updateRanking(Connection con) throws SQLException {
        String sb = "SELECT c.id, c.job, c.exp, c.level, c.name, c.jobRank, c.rank, c.fame" + " FROM characters AS c LEFT JOIN accounts AS a ON c.accountid = a.id WHERE a.gm = 0 AND a.banned = 0 AND c.level >= 160" +
                " ORDER BY c.level DESC , c.exp DESC , c.fame DESC , c.rank ASC";

        PreparedStatement charSelect = con.prepareStatement(sb);
        ResultSet rs = charSelect.executeQuery();
        PreparedStatement ps = con.prepareStatement("UPDATE characters SET jobRank = ?, jobRankMove = ?, rank = ?, rankMove = ? WHERE id = ?");
        int rank = 0; //for "all"
        Map<Integer, Integer> rankMap = new LinkedHashMap<>();
        for (int i : jobCommands.values()) {
            rankMap.put(i, 0); //job to rank
            rankings.put(i, new ArrayList<>());
        }
        while (rs.next()) {
            int job = rs.getInt("job");
            if (!rankMap.containsKey(job / 100)) { //not supported.
                continue;
            }
            int jobRank = rankMap.get(job / 100) + 1;
            rankMap.put(job / 100, jobRank);
            rank++;
            rankings.get(-1).add(new RankingInformation(rs.getString("name"), job, rs.getInt("level"), rs.getLong("exp"), rank, rs.getInt("fame")));
            rankings.get(job / 100).add(new RankingInformation(rs.getString("name"), job, rs.getInt("level"), rs.getLong("exp"), jobRank, rs.getInt("fame")));
            ps.setInt(1, jobRank);
            ps.setInt(2, rs.getInt("jobRank") - jobRank);
            ps.setInt(3, rank);
            ps.setInt(4, rs.getInt("rank") - rank);
            ps.setInt(5, rs.getInt("id"));
            ps.addBatch(); //添加要更新執行的SQL
        }
        ps.executeBatch(); //一次更新上面所有的addBatch() Batch update should be faster.
        rs.close();
        charSelect.close();
        ps.close();
    }

    private static void updateItemSearch(Connection con) throws SQLException {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        PreparedStatement ps = con.prepareStatement("SELECT itemid, count FROM itemsearch WHERE count > 0 ORDER BY count DESC LIMIT 10");
        ResultSet rs = ps.executeQuery();
        itemSearch.clear(); //清理列表
        while (rs.next()) {
            int itemId = rs.getInt("itemid");
            /*
             * 如果列表中這個道具 或者 道具不存在就跳過
             */
            if (itemSearch.contains(itemId) || !ii.itemExists(itemId)) {
                continue;
            }
            itemSearch.add(itemId); //添加道具
        }
        rs.close();
        ps.close();
    }

    public static void loadJobCommands() {
        jobCommands.put("所有", -1);
        jobCommands.put("初心者", 0);
        jobCommands.put("劍士", 1);
        jobCommands.put("法師", 2);
        jobCommands.put("弓箭手", 3);
        jobCommands.put("盜賊", 4);
        jobCommands.put("海盜", 5);
        jobCommands.put("貴族", 10);
        jobCommands.put("聖魂劍士", 11);
        jobCommands.put("烈焰巫師", 12);
        jobCommands.put("破風使者", 13);
        jobCommands.put("暗夜使者", 14);
        jobCommands.put("閃雷悍將", 15);
        jobCommands.put("英雄", 20);
        jobCommands.put("狂狼勇士", 21);
        jobCommands.put("龍魔導士", 22);
        jobCommands.put("精靈遊俠", 23);
        jobCommands.put("幻影", 24);
        jobCommands.put("夜光", 27);
        jobCommands.put("反抗軍", 30);
        jobCommands.put("惡魔殺手", 31);
        jobCommands.put("煉獄巫師", 32);
        jobCommands.put("狂豹獵人", 33);
        jobCommands.put("機甲戰神", 35);
        jobCommands.put("米哈逸", 50);
    }

    public static class RankingInformation {

        public final String toString;
        public final int rank;

        public RankingInformation(String name, int job, int level, long exp, int rank, int fame) {
            this.rank = rank;
            String builder = "排名 " + StringUtil.getRightPaddedStr(String.valueOf(rank), ' ', 3) +
                    " : " +
                    StringUtil.getRightPaddedStr(name, ' ', 13) +
                    " 等級: " +
                    StringUtil.getRightPaddedStr(String.valueOf(level), ' ', 3) +
                    " 職業: " +
                    StringUtil.getRightPaddedStr(MapleJob.getNameById(job), ' ', 10) +
                    "\r\n";
            //builder.append(" 經驗: ");
            //builder.append(exp);
            //builder.append(" 人氣: ");
            //builder.append(fame);
            this.toString = builder; //Rank 1 : KiDALex - Level 200 Blade Master | 0 EXP, 30000 Fame
        }

        @Override
        public String toString() {
            return toString;
        }
    }
}
