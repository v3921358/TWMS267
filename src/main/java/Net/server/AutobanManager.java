package Net.server;

import Client.MapleClient;
import Config.configs.ServerConfig;
import Config.constants.enums.UserChatMessageType;
import Packet.MaplePacketCreator;
import Server.world.WorldBroadcastService;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.data.MaplePacketLittleEndianWriter;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class AutobanManager implements Runnable {

    private static final Logger log = LoggerFactory.getLogger("AutobanManager");
    private static final int AUTOBAN_POINTS = 5000;
    @Getter
    private static final AutobanManager instance = new AutobanManager();
    private final Map<Integer, Integer> points = new HashMap<>();
    private final Map<Integer, List<String>> reasons = new HashMap<>();
    private final Set<ExpirationEntry> expirations = new TreeSet<>();
    private final ReentrantLock lock = new ReentrantLock(true);

    public void autoban(MapleClient c, String reason) {
        if (c.getPlayer() == null) {
            return;
        }
        if (c.getPlayer().isGm()) {
            c.getPlayer().dropMessage(5, "[警告] A/b 觸發: " + reason);
        } else if (ServerConfig.WORLD_AUTOBAN) {
            addPoints(c, AUTOBAN_POINTS, 0, reason);
        } else {
            WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.spouseMessage(UserChatMessageType.淺紫, "[GM消息] 玩家: " + c.getPlayer().getName() + " ID: " + c.getPlayer().getId() + " (等級 " + c.getPlayer().getLevel() + ") 遊戲操作異常. (原因: " + reason + ")"));
        }
    }

    public void addPoints(MapleClient c, int points, long expiration, String reason) {
        lock.lock();
        try {
            List<String> reasonList;
            int acc = c.getPlayer().getAccountID();

            if (this.points.containsKey(acc)) {
                int SavedPoints = this.points.get(acc);
                if (SavedPoints >= AUTOBAN_POINTS) { // Already auto ban'd.
                    return;
                }
                this.points.put(acc, SavedPoints + points); // Add
                reasonList = this.reasons.get(acc);
                reasonList.add(reason);
            } else {
                this.points.put(acc, points); //[賬號ID] [points]
                reasonList = new LinkedList<>();
                reasonList.add(reason);
                this.reasons.put(acc, reasonList); //[賬號ID] [封號原因]
            }

            if (this.points.get(acc) >= AUTOBAN_POINTS) { // See if it's sufficient to auto ban
                log.info("[作弊] 玩家 " + c.getPlayer().getName() + " A/b 觸發 " + reason);
                if (c.getPlayer().isGm()) {
                    c.getPlayer().dropMessage(5, "[警告] A/b 觸發 : " + reason);
                    return;
                }
                StringBuilder sb = new StringBuilder("A/b ");
                sb.append(c.getPlayer().getName());
                sb.append(" (IP ");
                sb.append(c.getSessionIPAddress());
                sb.append("): ");
                for (String s : reasons.get(acc)) {
                    sb.append(s);
                    sb.append(", ");
                }
                WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.serverNotice(0, " <" + c.getPlayer().getName() + "> 被系統封號 (原因: " + reason + ")"));
                c.getPlayer().ban(sb.toString(), false, true, false);
            } else {
                if (expiration > 0) {
                    expirations.add(new ExpirationEntry(System.currentTimeMillis() + expiration, acc, points));
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        for (ExpirationEntry e : expirations) {
            if (e.time <= now) {
                this.points.put(e.acc, this.points.get(e.acc) - e.points);
            } else {
                return;
            }
        }
    }

    private static class ExpirationEntry implements Comparable<ExpirationEntry> {

        public final long time;
        public final int acc;
        public final int points;

        public ExpirationEntry(long time, int acc, int points) {
            this.time = time;
            this.acc = acc;
            this.points = points;
        }

        @Override
        public int compareTo(ExpirationEntry o) {
            return (int) (time - o.time);
        }

        @Override
        public boolean equals(Object oth) {
            if (!(oth instanceof ExpirationEntry ee)) {
                return false;
            }
            return (time == ee.time && points == ee.points && acc == ee.acc);
        }
    }
}
