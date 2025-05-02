/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Net.server;

import Client.MapleCharacter;
import Client.MapleClient;
import Config.configs.ServerConfig;
import Database.DatabaseLoader.DatabaseConnectionEx;
import Packet.MaplePacketCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MapleActivity {

    private final static Logger log = LoggerFactory.getLogger(MapleActivity.class);
    private final static int activity_max = 150;
    private final static int[] stage = {20, 40, 80, 120, 150};

    public static void initAllActivity() {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM missionstatus WHERE missionid >= 120101 AND missionid <= 120114")) {
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException e) {
            log.error("Error MissionDelete:", e);
        }
    }

    public static void loginTip(MapleClient c) {
        String message;
        message = ServerConfig.WELLCOME_MESSAGE;
        c.announce(MaplePacketCreator.sendHint(message, 150, 60, null));
    }

    public static int getActivity(final MapleCharacter player) {
        int ret = 0;
        for (QuestActivity q : QuestActivity.values()) {
            int times = player.MissionGetFinish(player.getId(), q.id);
            ret += times * q.activity;
        }
        return ret;
    }

    public static void finish(final MapleCharacter player, int questid) {
        if (!player.MissionStatus(player.getId(), questid, 0, 4)) {
            player.MissionMake(player.getId(), questid, 0, 0, 0, 0);
        } else {
            if (player.MissionGetFinish(player.getId(), questid) >= QuestActivity.getMaxTimesById(questid)) {
            }
        }
    }

    public static int getMaxActivity() {
        return activity_max;
    }

    public static int getDiffActivity(final MapleCharacter player) {
        return activity_max - getActivity(player);
    }

    public static int getNextStage(final MapleCharacter player) {
        int stage_ = 0;
        final int activity = getActivity(player);
        for (int i = 0; i < stage.length; i++) {
            if (activity < stage[i]) {
                stage_ = i + 1;
                break;
            }
        }
        return stage_;
    }

    public static int getNextStageNeed(final MapleCharacter player) {
        return stage[Math.min(stage.length - 1, getNextStage(player) - 1)] - getActivity(player);
    }

    public static int getRecevieReward(final MapleCharacter player) {
        final int activity = getActivity(player);
        for (int i = 1; i <= stage.length; i++) {
            if (activity >= stage[i - 1] && player.getBossLog("活躍度禮包" + i) == 0) {
                return i;
            }
        }
        return -1;
    }

    public enum QuestActivity {
        每日簽到(120101, 5, 1),
        裝備砸卷(120102, 2, 5),
        使用方塊(120103, 2, 5),
        廢棄任務(120104, 5, 2),
        挑戰殘暴炎魔(120105, 2, 5),
        挑戰皮卡啾(120106, 10, 1),
        擊殺任意BOSS(120107, 10, 1),
        在線300分鐘(120108, 5, 1),
        在線800分鐘(120109, 10, 1),
        環任務(120110, 1, 20),
        每日紅包(120111, 10, 1),
        兌換中介幣(120112, 10, 1),
        兌換樂豆點(120113, 10, 1),
        闖關副本(120114, 20, 1);

        private final int id, activity, maxtimes;

        QuestActivity(int id, int activity, int maxtimes) {
            this.id = id;
            this.activity = activity;
            this.maxtimes = maxtimes;
        }

        public static int getActivityById(int id) {
            for (QuestActivity q : QuestActivity.values()) {
                if (q.id == id) {
                    return q.activity;
                }
            }
            return 0;
        }

        public static int getMaxTimesById(int id) {
            for (QuestActivity q : QuestActivity.values()) {
                if (q.id == id) {
                    return q.maxtimes;
                }
            }
            return 0;
        }
    }
}
