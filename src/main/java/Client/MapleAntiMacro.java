package Client;

import Config.constants.GameConstants;
import Config.constants.MapConstants;
import Config.constants.enums.UserChatMessageType;
import Net.server.Timer;
import Net.server.maps.MapleMap;
import Net.server.quest.MapleQuest;
import Packet.MaplePacketCreator;
import Server.world.WorldBroadcastService;
import tools.CheckCodeImageCreator;
import tools.Randomizer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * @author pungin
 */
public class MapleAntiMacro {

    public static class MapleAntiMacroInfo {

        private final MapleCharacter source;
        private final int mode;
        private String code;
        private final long startTime;
        private ScheduledFuture<?> schedule;
        private int timesLeft = 2;

        MapleAntiMacroInfo(MapleCharacter from, int mode, String code, long time, ScheduledFuture<?> schedule) {
            source = from;
            this.mode = mode;
            this.code = code;
            startTime = time;
            this.schedule = schedule;
        }

        public MapleCharacter getSourcePlayer() {
            return source;
        }

        public int antiMode() {
            return mode;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setSchedule(ScheduledFuture<?> schedule) {
            cancelSchedule();
            this.schedule = schedule;
        }

        public void cancelSchedule() {
            if (schedule != null) {
                schedule.cancel(false);
            }
        }

        public int antiFailure() {
            return --timesLeft;
        }

        public int getTimesLeft() {
            return timesLeft - 1;
        }
    }

    private static final Map<String, MapleAntiMacroInfo> antiPlayers = new HashMap<>();
    private static final Map<String, Long> lastAntiTime = new HashMap<>();

    // 測謊機類型
    public final static byte SYSTEM_ANTI = 0; // 系統自動偵測
    public final static byte ITEM_ANTI = 1; // 道具測謊
    public final static byte GM_SKILL_ANTI = 2; // 管理員技能測謊

    // 角色測謊狀態常量
    public final static int CAN_ANTI = 0; // 可測謊
    public final static int NON_ATTACK = 1; // 非攻擊狀態
    public final static int ANTI_COOLING = 2; // 已通過測謊
    public final static int ANTI_NOW = 3; // 正在測謊
    public final static int BOSS_MAP = 4; // BOSS地圖

    public static int getCharacterState(MapleCharacter chr) {
        // 判斷是否正在被測謊
        if (isAntiNow(chr.getName())) {
            return ANTI_NOW;
        }

        // 判斷冷卻狀態
        if (isCooling(chr.getName())) {
            return ANTI_COOLING;
        }

        // 判斷非攻擊狀態
        if (!chr.getCheatTracker().isAttacking()) {
            return NON_ATTACK;
        }

        // BOSS地圖
        if (MapConstants.isBossMap(chr.getMapId())) {
            return BOSS_MAP;
        }

        // 可測謊
        return CAN_ANTI;
    }

    public static void updateCooling(String name) {
        lastAntiTime.put(name, System.currentTimeMillis());
    }

    public static boolean isCooling(String name) {
        if (lastAntiTime.containsKey(name)) {
            if (System.currentTimeMillis() - lastAntiTime.get(name) < (long) (20 + Randomizer.nextInt(10)) * 60 * 1000) { // 20~30分鐘冷卻
                return true;
            } else {
                lastAntiTime.remove(name);
            }
        }
        return false;
    }

    public static boolean isAntiNow(String name) {
        // 5分鐘
        return antiPlayers.containsKey(name) && System.currentTimeMillis() - antiPlayers.get(name).getStartTime() < 5 * 60 * 1000;
    }

    public static boolean startAnti(MapleCharacter chr, MapleCharacter victim, byte mode) {
        return startAnti(chr, victim, mode, false);
    }

    public static boolean startAnti(MapleCharacter chr, MapleCharacter victim, byte mode, boolean force) {
        int antiState = MapleAntiMacro.getCharacterState(victim);
        switch (antiState) {
            case MapleAntiMacro.CAN_ANTI: {
                break;
            }
            case MapleAntiMacro.NON_ATTACK: {
                if (force) {
                    break;
                }
                if (chr != null) {
                    chr.dropMessage(1, "角色不在攻擊狀態");
//                    chr.getClient().announce(MaplePacketCreator.AntiMacro.nonAttack());
                }
                return false;
            }
            case MapleAntiMacro.ANTI_COOLING:
            case MapleAntiMacro.BOSS_MAP: {
                if (force) {
                    break;
                }
                if (chr != null) {
                    chr.dropMessage(1, "角色已經通過測謊");
//                    chr.getClient().announce(MaplePacketCreator.AntiMacro.alreadyPass());
                }
                return false;
            }
            case MapleAntiMacro.ANTI_NOW: {
                if (chr != null && !force) {
                    chr.dropMessage(1, "角色正在被測謊");
//                    chr.getClient().announce(MaplePacketCreator.AntiMacro.antiMacroNow());
                }
                return false;
            }
            default: {
                System.out.println("測謊機狀態出現未知類型：" + antiState);
                return false;
            }
        }

        MapleAntiMacroInfo ami = new MapleAntiMacroInfo(chr, mode, CheckCodeImageCreator.getRandCode(true), System.currentTimeMillis(),
                Timer.MapTimer.getInstance().schedule(() -> {
                    if (antiPlayers.containsKey(victim.getName())) {
                        antiFailure(victim);
                    }
                }, 5 * 60 * 1000));
        antiPlayers.put(victim.getName(), ami);
        //victim.getClient().announce(MaplePacketCreator.AntiMacro.getImage(CheckCodeImageCreator.createImage(ami.getCode()), ami.getTimesLeft()));
        if (chr != null) {
            chr.dropMessage(1, "已經對\"" + victim.getName() + "\"進行測謊");
//            chr.getClient().announce(MaplePacketCreator.AntiMacro.antiMsg(mode, victim.getName()));
        }
        return true;
    }

    public static void antiSuccess(MapleCharacter victim) {
        MapleAntiMacroInfo ami = null;
        if (antiPlayers.containsKey(victim.getName())) {
            ami = antiPlayers.get(victim.getName());
            if (ami.antiMode() == ITEM_ANTI) {
                victim.gainMeso(5000, true);
            }
            MapleCharacter chr = ami.getSourcePlayer();
            if (chr != null) {
                chr.dropMessage(1, "玩家\"" + victim.getName() + "\"已經通過測謊");
//                chr.getClient().announce(MaplePacketCreator.AntiMacro.successMsg(2, victim.getName()));
            }
        }
        victim.setAntiMacroFailureTimes(0);
        victim.dropMessage(1, "您已通過測謊" + (ami != null && ami.antiMode() == ITEM_ANTI ? ", 獲得 5000 楓幣獎勵" : ""));
//        victim.getClient().announce(MaplePacketCreator.AntiMacro.success(ami == null ? SYSTEM_ANTI : ami.antiMode()));
        stopAnti(victim.getName());
        updateCooling(victim.getName());
    }

    public static void antiFailure(MapleCharacter victim) {
        MapleAntiMacroInfo ami = null;
        if (antiPlayers.containsKey(victim.getName())) {
            ami = antiPlayers.get(victim.getName());
            MapleCharacter chr = ami.getSourcePlayer();
//            if (chr != null && ami.antiMode() == GM_SKILL_ANTI) {
//                chr.getClient().announce(MaplePacketCreator.AntiMacro.failureScreenshot(victim.getName()));
//            }
        }
        stopAnti(victim.getName());
        if (victim.addAntiMacroFailureTimes() < 5) {
            victim.changeMap(victim.getMap().getReturnMap());
            victim.dropMessage(1, "已通過測謊。");
//            victim.getClient().announce(MaplePacketCreator.AntiMacro.failure(ami == null ? SYSTEM_ANTI : ami.antiMode()));
            victim.dropMessage(5, "連續五次失敗,就會被關進監獄。");
        } else {
            victim.setAntiMacroFailureTimes(0);
            MapleMap map = victim.getClient().getChannelServer().getMapFactory().getMap(GameConstants.JAIL);
            victim.getQuestNAdd(MapleQuest.getInstance(GameConstants.JAIL_QUEST)).setCustomData(String.valueOf(30 * 60));
            victim.changeMap(map, map.getPortal(0));
            WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.spouseMessage(UserChatMessageType.淺紫, "[GM消息] 玩家: " + victim.getName() + " (等級 " + victim.getLevel() + ") 未通過測謊機檢器，系統將其監禁30分鐘！"));
        }
    }

    public static void stopAnti(String name) {
        if (antiPlayers.containsKey(name)) {
            antiPlayers.get(name).cancelSchedule();
        }
        antiPlayers.remove(name);
    }

    public static void antiReduce(MapleCharacter victim) {
        if (antiPlayers.containsKey(victim.getName())) {
            MapleAntiMacroInfo ami = antiPlayers.get(victim.getName());
            if (ami.antiFailure() > 0) {
                refreshCode(victim);
            } else {
                antiFailure(victim);
            }
        }
    }

    public static boolean verifyCode(String name, String code) {
        if (!antiPlayers.containsKey(name)) {
            return false;
        }
        return antiPlayers.get(name).getCode().equalsIgnoreCase(code);
    }

    public static void refreshCode(MapleCharacter victim) {
        if (antiPlayers.containsKey(victim.getName())) {
            MapleAntiMacroInfo ami = antiPlayers.get(victim.getName());
            ami.setCode(CheckCodeImageCreator.getRandCode(true));
            ami.setSchedule(Timer.MapTimer.getInstance().schedule(() -> {
                if (antiPlayers.containsKey(victim.getName())) {
                    antiFailure(victim);
                }
            }, 5 * 60 * 1000));
            // victim.getClient().announce(MaplePacketCreator.AntiMacro.getImage(CheckCodeImageCreator.createImage(ami.getCode()), ami.getTimesLeft()));
        }
    }
}
