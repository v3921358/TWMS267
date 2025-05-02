package Net.server.commands;

import Client.MapleCharacter;
import Client.MapleCharacterUtil;
import Client.MapleClient;
import Client.MapleStat;
import Client.inventory.Item;
import Client.inventory.MapleInventory;
import Client.inventory.MapleInventoryType;
import Client.skills.SkillFactory;
import Config.configs.Config;
import Config.configs.ServerConfig;
import Config.constants.ServerConstants;
import Net.server.MapleInventoryManipulator;
import Net.server.MapleItemInformationProvider;
import Net.server.ShutdownServer;
import Net.server.Timer.*;
import Net.server.life.MapleMonsterInformationProvider;
import Net.server.shop.MapleShopFactory;
import Opcode.Headler.InHeader;
import Opcode.Headler.OutHeader;
import Packet.MaplePacketCreator;
import Plugin.provider.loaders.SkillData;
import Plugin.script.ReactorManager;
import Server.MapleServerHandler;
import Server.channel.ChannelServer;
import Server.world.WorldFindService;
import Server.world.WorldRespawnService;
import tools.StringUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author Emilyx3
 */

/**
 * 已經棄用，請勿使用 - 已用CommandManager替代
 */
@Deprecated
public class AdminCommand {

    /**
     * @return
     */
    public static PlayerRank getPlayerLevelRequired() {
        return PlayerRank.伺服管理員;
    }

    public static class 脫掉所有人 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "StripEveryone";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            ChannelServer cs = c.getChannelServer();
            for (MapleCharacter mchr : cs.getPlayerStorage().getAllCharacters()) {
                if (mchr.isGm()) {
                    continue;
                }
                MapleInventory equipped = mchr.getInventory(MapleInventoryType.EQUIPPED);
                MapleInventory equip = mchr.getInventory(MapleInventoryType.EQUIP);
                MapleInventory decoration = mchr.getInventory(MapleInventoryType.DECORATION);
                Map<Short, Boolean> ids = new LinkedHashMap<>();
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                for (Item item : equipped.newList()) {
                    ids.put(item.getPosition(), ii.isCash(item.getItemId()));
                }
                for (Map.Entry<Short, Boolean> entry : ids.entrySet()) {
                    MapleInventoryManipulator.unequip(mchr.getClient(), entry.getKey(), entry.getValue() ? decoration.getNextFreeSlot() : equip.getNextFreeSlot());
                }
            }
            return 1;
        }
    }

    public static class 給所有人楓幣 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "MesoEveryone";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 1) {
                c.getPlayer().dropMessage(6, "用法: " + splitted[0] + " [楓幣數量]");
                return 0;
            }
            int meso;
            try {
                meso = Integer.parseInt(splitted[1]);
            } catch (NumberFormatException nfe) {
                c.getPlayer().dropMessage(6, "輸入的楓幣數量無效.");
                return 0;
            }
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                    mch.gainMeso(meso, true);
                }
            }
            return 1;
        }
    }

    public static class 測試距離 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "用法: " + splitted[0] + " x坐標 y坐標  說明: 此指令可以測試輸入的坐標訊息和角色當前坐標的距離");
                return 0;
            }
            c.getPlayer().dropMessage(6, "當前距離: " + c.getPlayer().getPosition().distance(Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2])));
            return 1;
        }
    }

    public static class 給所有人點數 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "CashEveryone";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "用法: " + splitted[0] + " [點數類型1-2] [點數數量]");
                return 0;
            }
            int type = Integer.parseInt(splitted[1]);
            int quantity = Integer.parseInt(splitted[2]);
            if (type <= 0 || type > 2) {
                type = 2;
            }
            int ret = 0;
            StringBuilder sb = new StringBuilder();
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                    mch.modifyCSPoints(type, quantity, false);
                    mch.dropMessage(-11, "[系統提示] 恭喜您獲得管理員贈送給您的 " + quantity + (type == 1 ? "樂豆點" : " 楓點"));
                    ret++;
                    sb.append(MapleCharacterUtil.makeMapleReadable(mch.getName()));
                    sb.append(", ");
                }
            }
            c.getPlayer().dropMessage(6, "以下是獲得" + (type == 1 ? " 樂豆點" : " 楓點") + "的玩家名單:");
            c.getPlayer().dropMessage(6, sb.toString());
            c.getPlayer().dropMessage(6, "指令使用成功，當前共有: " + ret + " 個玩家獲得: " + quantity + (type == 1 ? "樂豆點 " : " 楓點 ") + " 計總: " + (ret * quantity));
            return 1;
        }
    }

    public static class 給所有人元寶 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "PayEveryone";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 1) {
                c.getPlayer().dropMessage(6, "用法: " + splitted[0] + " [數量]");
                return 0;
            }
            int hb;
            try {
                hb = Integer.parseInt(splitted[1]);
            } catch (NumberFormatException nfe) {
                c.getPlayer().dropMessage(6, "輸入的楓幣數量無效.");
                return 0;
            }
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                    mch.gainTWD(hb);
                    mch.dropMessage(-11, "[系統提示] 恭喜您獲得管理員贈送給您的" + hb + "楓幣。");
                }
            }
            return 1;
        }
    }

    public static class 經驗倍率 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "ExpRate";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length > 1) {
                int rate = Integer.parseInt(splitted[1]);
                if (splitted.length > 2 && splitted[2].equalsIgnoreCase("all")) {
                    for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                        cserv.setExpRate(rate);
                    }
                } else {
                    c.getChannelServer().setExpRate(rate);
                }
                c.getPlayer().dropMessage(6, "經驗倍率已經修改為: " + rate + "倍.");
            } else {
                c.getPlayer().dropMessage(6, "用法: " + splitted[0] + " <number> [all]");
            }
            return 1;
        }
    }

    public static class 楓幣倍率 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "MesoRate";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length > 1) {
                int rate = Integer.parseInt(splitted[1]);
                if (splitted.length > 2 && splitted[2].equalsIgnoreCase("all")) {
                    for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                        cserv.setMesoRate(rate);
                    }
                } else {
                    c.getChannelServer().setMesoRate(rate);
                }
                c.getPlayer().dropMessage(6, "楓幣倍率已經修改為: " + rate + "倍.");
            } else {
                c.getPlayer().dropMessage(6, "用法: " + splitted[0] + " <number> [all]");
            }
            return 1;
        }
    }

    public static class 掉寶倍率 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "DropRate";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length > 1) {
                int rate = Integer.parseInt(splitted[1]);
                if (splitted.length > 2 && splitted[2].equalsIgnoreCase("all")) {
                    for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                        cserv.setDropRate(rate);
                    }
                } else {
                    c.getChannelServer().setDropRate(rate);
                }
                c.getPlayer().dropMessage(6, "怪物掉寶率已經修改為: " + rate + "倍.");
            } else {
                c.getPlayer().dropMessage(6, "用法: " + splitted[0] + " <number> [all]");
            }
            return 1;
        }
    }

    public static class 經驗加倍 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        private int change = 0;

        @Override
        public int execute(MapleClient c, String[] splitted) {
            change = Integer.parseInt(splitted[1]);
            if (change == 1 || change == 2) {
                c.getPlayer().dropMessage(6, "以前 - 經驗: " + c.getChannelServer().getExpRate() + " 楓幣: " + c.getChannelServer().getMesoRate() + " 掉寶: " + c.getChannelServer().getDropRate());
                for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                    cserv.setDoubleExp(change);
                }
                c.getPlayer().dropMessage(6, "現在 - 經驗: " + c.getChannelServer().getExpRate() + " 楓幣: " + c.getChannelServer().getMesoRate() + " 掉寶: " + c.getChannelServer().getDropRate());
                return 1;
            } else {
                c.getPlayer().dropMessage(6, "輸入的數字無效，1為關閉活動經驗，2為開啟活動經驗。當前輸入為: " + change);
                return 0;
            }
        }
    }

    public static class 經驗訊息 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().dropMessage(5, "當前遊戲設置訊息:");
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                String rateStr = "頻道 " + cserv.getChannel()
                        + " 經驗: "
                        + cserv.getExpRate()
                        + " 楓幣: "
                        + cserv.getMesoRate()
                        + " 掉寶: "
                        + cserv.getDropRate()
                        + " 活動: "
                        + cserv.getDoubleExp();
                c.getPlayer().dropMessage(5, rateStr);
            }
            return 1;
        }
    }

    public static class 全部人下線 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "DCAll";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            int range = -1;
            switch (splitted[1]) {
                case "m":
                    range = 0;
                    break;
                case "c":
                    range = 1;
                    break;
                case "w":
                    range = 2;
                    break;
            }
            if (range == -1) {
                range = 1;
            }
            if (range == 0) {
                c.getPlayer().getMap().disconnectAll();
                c.getPlayer().dropMessage(5, "已成功斷開當前地圖所有玩家的連接.");
            } else if (range == 1) {
                c.getChannelServer().getPlayerStorage().disconnectAll(true);
                c.getPlayer().dropMessage(5, "已成功斷開當前頻道所有玩家的連接.");
            } else if (range == 2) {
                for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                    cserv.getPlayerStorage().disconnectAll(true);
                }
                c.getPlayer().dropMessage(5, "已成功斷開當前遊戲所有玩家的連接.");
            }
            return 1;
        }
    }

    public static class 查看掉寶 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().getScriptManager().startNpcScript(9010000, 0, "1");
//            NPCScriptManager.getInstance().start(c, 9010000, "1");
            return 1;
        }
    }

    public static class 關機 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "Shutdown";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            try {
                int seconds = Integer.parseInt(splitted[1]);
                new Thread(() -> {
                    try {
                        Thread.sleep(seconds * 1000L);
                        ShutdownServer.getInstance().run();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
                c.dropMessage("服務器關閉指令已經接收,將在 " + seconds + " 秒後關閉服務器..");
            } catch (NumberFormatException e) {
                c.dropMessage("請提供正確的秒數..");
            } catch (Exception e) {
                c.dropMessage(e.getMessage());
            }
            return 1;
        }
    }


    public static class Subcategory implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().setSubcategory(Byte.parseByte(splitted[1]));
            return 1;
        }
    }

    public static class 刷楓幣 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "getMeso";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().gainMeso(ServerConfig.CHANNEL_PLAYER_MAXMESO - c.getPlayer().getMeso(), true);
            return 1;
        }
    }

    public static class 刷樂豆點 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(5, "請輸入數量.");
                return 0;
            }
            c.getPlayer().modifyCSPoints(1, Integer.parseInt(splitted[1]), true);
            return 1;
        }
    }

    public static class 刷楓點 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(5, "請輸入數量.");
                return 0;
            }
            c.getPlayer().modifyCSPoints(2, Integer.parseInt(splitted[1]), true);
            return 1;
        }
    }

    public static class 刷里程 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(5, "請輸入數量.");
                return 0;
            }
            c.getPlayer().modifyMileage(Integer.parseInt(splitted[1]), 1, true, false, "使用[" + splitted[0] + "]指令獲得");
            return 1;
        }
    }

    public static class GainP implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(5, "請輸入數量.");
                return 0;
            }
            c.getPlayer().setPoints(c.getPlayer().getPoints() + Integer.parseInt(splitted[1]));
            return 1;
        }
    }

    public static class GainVP implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(5, "請輸入數量.");
                return 0;
            }
            c.getPlayer().setVPoints(c.getPlayer().getVPoints() + Integer.parseInt(splitted[1]));
            return 1;
        }
    }

    public static class 重載包頭 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "reloadop";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (Config.isDevelop()) {
                Config.load();
            }
            InHeader.reloadValues();
            OutHeader.reloadValues();
            c.getPlayer().dropMessage(5, "重新獲取包頭完成.");
            return 1;
        }
    }

    public static class 重載掉寶 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "reloadDrop";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleMonsterInformationProvider.getInstance().clearDrops();
            ReactorManager.getInstance().clearDrops();
            c.getPlayer().dropMessage(5, "重新加載掉寶完成.");
            return 1;
        }
    }

    public static class 重載傳送 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "reloadPortal";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
//            PortalScriptManager.getInstance().clearScripts();
            c.getPlayer().dropMessage(5, "重新加載傳送點腳本完成.");
            return 1;
        }
    }

    public static class 重載指令 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "reloadcommand";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            CommandProcessor.loadScript();
            c.getPlayer().dropMessage(5, "重新加載指令腳本完成.");
            return 1;
        }
    }

    public static class 重載商店 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleShopFactory.getInstance().clear();
            c.getPlayer().dropMessage(5, "重新加載商店販賣道具完成.");
            return 1;
        }
    }

    public static class 重載活動 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "reloadevent";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            Config.load();
            try {
                for (ChannelServer instance : ChannelServer.getAllInstances()) {
                    if (splitted.length > 1) {
                        instance.loadEvents();
                    } else {
                        instance.loadEvents();
                    }
                }
                c.getPlayer().dropMessage(5, "重新加載活動腳本完成.");
            } catch (Exception e) {
                c.getPlayer().dropMessage(5, e.getMessage());
            }
            return 1;
        }
    }

    public static class 封包測試 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            //c.StartWindow();
            return 1;
        }
    }

    public static class 重載複製 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            SkillFactory.loadMemorySkills();
            c.getPlayer().dropMessage(5, "加載複製技能完成...");
            return 1;
        }
    }

    public static class 重載封包禁止 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            Config.load();
            return 1;
        }
    }

    public static class 線上玩家 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            int total = 0;
            c.getPlayer().dropMessage(6, "---------------------------------------------------------------------------------------");
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                int curConnected = cserv.getConnectedClients();
                c.getPlayer().dropMessage(6, "頻道: " + cserv.getChannel() + " 線上人數: " + curConnected);
                total += curConnected;
                for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                    if (chr != null) {
                        StringBuilder ret = new StringBuilder();
                        ret.append("[ID:");
                        ret.append(StringUtil.getRightPaddedStr(chr.getId(), ' ', 5));
                        ret.append("] ");
                        ret.append(StringUtil.getRightPaddedStr(chr.getName(), ' ', 15));
                        ret.append(" Lv.");
                        ret.append(StringUtil.getRightPaddedStr(String.valueOf(chr.getLevel()), ' ', 3));
                        if (chr.getMap() != null) {
                            ret.append(" | ");
                            ret.append(chr.getMap());
                        }
                        c.getPlayer().dropMessage(6, ret.toString());
                    }
                }
            }
            c.getPlayer().dropMessage(6, "當前伺服器計總線上數: " + total);
            c.getPlayer().dropMessage(6, "---------------------------------------------------------------------------------------");
            return 1;
        }
    }

    /*
     * 測試傾向系統用
     */
    public static class 增加經驗 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(5, "請輸入數量.");
                return 0;
            }
            c.getPlayer().addTraitExp(Integer.parseInt(splitted[1]));
            return 1;
        }
    }

    public static class 設置經驗 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(5, "請輸入數量.");
                return 0;
            }
            c.getPlayer().setTraitExp(Integer.parseInt(splitted[1]));
            return 1;
        }
    }

    public static class 檢測複製 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            List<String> msgs = new ArrayList<>();
            Map<Integer, CopyItemInfo> checkItems = new LinkedHashMap<>();
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter player : cserv.getPlayerStorage().getAllCharacters()) {
                    if (player != null && player.getMap() != null) {
                        //檢測背包裝備
                        check(player.getInventory(MapleInventoryType.EQUIP), player, checkItems, msgs);
                        //檢測身上的裝備
                        check(player.getInventory(MapleInventoryType.EQUIPPED), player, checkItems, msgs);
                        //檢測背包時裝
                        check(player.getInventory(MapleInventoryType.DECORATION), player, checkItems, msgs);
                    }
                }
            }
            checkItems.clear();
            if (msgs.size() > 0) {
                c.getPlayer().dropMessage(5, "檢測完成，共有: " + msgs.size() + " 個複製訊息");
//                FileoutputUtil.log("裝備複製.txt", "檢測完成，共有: " + msgs.size() + " 個複製信息", true);
                for (String s : msgs) {
                    c.getPlayer().dropMessage(5, s);
//                    FileoutputUtil.log("裝備複製.txt", s, true);
                }
                c.getPlayer().dropMessage(5, "以上訊息為擁有複製道具的玩家.");
            } else {
                c.getPlayer().dropMessage(5, "未檢測到遊戲中的角色有複製的道具訊息.");
            }
            return 1;
        }

        public void check(MapleInventory equip, MapleCharacter player, Map<Integer, CopyItemInfo> checkItems, List<String> msgs) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            for (Item item : equip.list()) {
                if (item.getSN() > 0) {
                    CopyItemInfo ret = new CopyItemInfo(item.getItemId(), player.getId(), player.getName());
                    if (checkItems.containsKey(item.getSN())) {
                        ret = checkItems.get(item.getSN());
                        if (ret.itemId == item.getItemId()) {
                            if (ret.isFirst()) {
                                ret.setFirst(false);
                                msgs.add("角色: " + StringUtil.getRightPaddedStr(ret.name, ' ', 13) + " 角色ID: " + StringUtil.getRightPaddedStr(String.valueOf(ret.chrId), ' ', 6) + " 道具: " + ret.itemId + " - " + ii.getName(ret.itemId) + " 唯一ID: " + item.getSN());
                            } else {
                                msgs.add("角色: " + StringUtil.getRightPaddedStr(player.getName(), ' ', 13) + " 角色ID: " + StringUtil.getRightPaddedStr(String.valueOf(player.getId()), ' ', 6) + " 道具: " + item.getItemId() + " - " + ii.getName(item.getItemId()) + " 唯一ID: " + item.getSN());
                            }
                        }
                    } else {
                        checkItems.put(item.getSN(), ret);
                    }
                }
            }
        }
    }

    public static class 查看進程 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "正在查看自己的進程訊息.");
                c.announce(MaplePacketCreator.SystemProcess());
                return 0;
            }
            String name = splitted[1];
            MapleCharacter chrs = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (chrs == null) {
                c.getPlayer().dropMessage(6, "當前頻道沒有找到玩家[" + name + "]的訊息.");
            } else {
                c.getPlayer().dropMessage(6, "正在查看玩家[" + name + "]的進程訊息.");
                chrs.getClient().announce(MaplePacketCreator.SystemProcess());
            }
            return 1;
        }
    }

    public static class 重載配置 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            Config.load();
            WorldRespawnService.getInstance();
            c.getPlayer().dropMessage(6, "重新加載配置檔案完成.");
            return 1;
        }
    }

    public static class 異常訊息 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            ServerConstants.setShowGMMessage(!ServerConstants.isShowGMMessage());
            c.getPlayer().dropMessage(6, "顯示異常訊息功能：" + (ServerConstants.isShowGMMessage() ? "已開啟" : "已關閉"));
            return 1;
        }
    }

    public static class 重載商城封包 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                cserv.getPlayerStorage();
            }
            c.getPlayer().dropMessage(6, "重新加載商城封包完成.");
            return 1;
        }
    }

    public static class 管理登入模式 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            int change = Integer.parseInt(splitted[1]);
            if (change == 1 || change == 2) {
                c.getPlayer().dropMessage(6, "以前 - 是否管理登入: " + ServerConfig.WORLD_ONLYADMIN);
                ServerConfig.WORLD_ONLYADMIN = (change == 2);
                c.getPlayer().dropMessage(6, "現在 - 是否管理登入: " + ServerConfig.WORLD_ONLYADMIN);
                return 1;
            } else {
                c.getPlayer().dropMessage(6, "輸入的數字無效，1為關閉管理登入模式，2為開啟管理登入模式。當前輸入為: " + change);
                return 0;
            }
        }
    }

    public static class 重載特殊掉寶 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleMonsterInformationProvider.getInstance().clearDrops();
            c.getPlayer().dropMessage(6, "重新加載特殊掉寶完成.");
            return 1;
        }
    }

    public static class 查看裝備 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(6, splitted[0] + " [角色ID] [裝備位置] ");
                return 0;
            }
            if (splitted[1].matches("/^([0-9])*$") || splitted[2].matches("/^([0-9])*$")) {
                c.getPlayer().dropMessage(6, "輸入錯誤，角色ID和裝備位置必須為數字！");
                return 0;
            }
            int victimId = Integer.parseInt(splitted[1]);
            int position = Integer.parseInt(splitted[2]);
            int ch = WorldFindService.getInstance().findChannel(victimId);
            if (ch <= 0) {
                c.getPlayer().dropMessage(6, "找不到角色ID為: " + victimId + " 的訊息.");
                return 0;
            }
            MapleCharacter victim = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(victimId);
            if (victim != null) {
                byte pos = (byte) (position * (position > 0 ? -1 : 1));
                Item item = victim.getInventory(MapleInventoryType.EQUIPPED).getItem(pos);
                if (item == null) {
                    c.getPlayer().dropMessage(6, "[查看裝備] 玩家 : " + victim.getName() + " ID: " + victimId + " 身上裝備位置為 " + position + " 空或者輸入位置錯誤");
                    return 0;
                }
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                c.announce(MaplePacketCreator.getGachaponMega("[查看裝備]", " : 玩家 " + victim.getName() + " 身上裝備位置為 " + position + " 的裝備{" + ii.getName(item.getItemId()) + "}訊息..", item, 3, c.getChannel()));
                return 1;
            } else {
                c.getPlayer().dropMessage(6, "找不到角色ID為: " + victimId + " 的訊息.");
            }
            return 0;
        }
    }

    //    public static class 坐標傳送 implements CommandExecute {
//
//        @Override
//        public String getShortCommand() {
//            return null;
//        }
//
//        @Override
//        public int execute(MapleClient c, String[] splitted) {
//            if (splitted.length < 2) {
//                c.getPlayer().dropMessage(5, "用法: " + splitted[0] + " [要傳送的坐標點] 或者 " + splitted[0] + " [X坐標] [Y坐標] ");
//                return 0;
//            }
//            if (splitted.length == 2) {
//                int portalId;
//                try {
//                    portalId = Integer.parseInt(splitted[1]);
//                } catch (NumberFormatException nfe) {
//                    c.getPlayer().dropMessage(6, "輸入的傳送點ID無效.");
//                    return 0;
//                }
//                int maxPortalId = c.getPlayer().getMap().getPortals().size();
//                MaplePortal portal = c.getPlayer().getMap().getPortal(portalId);
//                if (portal == null || portalId > maxPortalId) {
//                    c.getPlayer().dropMessage(6, "輸入的傳送點ID[" + portalId + "]無效或者當前地圖沒有這個傳送點,當前地圖最大傳送點為 ：" + maxPortalId);
//                    return 0;
//                }
//                c.getPlayer().instantMapWarp(portal.getPosition());
//            } else {
//                int posX;
//                int posY;
//                try {
//                    posX = Integer.parseInt(splitted[1]);
//                } catch (NumberFormatException nfe) {
//                    c.getPlayer().dropMessage(6, "輸入的X坐標無效.");
//                    return 0;
//                }
//                try {
//                    posY = Integer.parseInt(splitted[2]);
//                } catch (NumberFormatException nfe) {
//                    c.getPlayer().dropMessage(6, "輸入的Y坐標無效.");
//                    return 0;
//                }
//                Point portalPos = new Point(posX, posY);
//                c.getPlayer().instantMapWarp(portalPos);
//            }
//            return 1;
//        }
//    }
    public static class 查看線程 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(5, "請輸入查看線程的參數[1-7]");
                return 0;
            }
            int poolId;
            try {
                poolId = Integer.parseInt(splitted[1]);
            } catch (NumberFormatException nfe) {
                c.getPlayer().dropMessage(6, "輸入查看的線程ID錯誤.");
                return 0;
            }
            ScheduledThreadPoolExecutor scheduledPool;
            String name;
            switch (poolId) {
                case 1:
                    name = "World Timer";
                    scheduledPool = WorldTimer.getInstance().getSES();
                    break;
                case 2:
                    name = "Etc Timer";
                    scheduledPool = EtcTimer.getInstance().getSES();
                    break;
                case 3:
                    name = "Map Timer";
                    scheduledPool = MapTimer.getInstance().getSES();
                    break;
                case 4:
                    name = "Clone Timer";
                    scheduledPool = CloneTimer.getInstance().getSES();
                    break;
                case 5:
                    name = "Event Timer";
                    scheduledPool = EventTimer.getInstance().getSES();
                    break;
                case 6:
                    name = "Buff Timer";
                    scheduledPool = BuffTimer.getInstance().getSES();
                    break;
                case 7:
                    name = "Ping Timer";
                    scheduledPool = PingTimer.getInstance().getSES();
                    break;
                default:
                    return 0;
            }
            if (scheduledPool != null) {
                c.getPlayer().dropMessage(-11, "--------------------------------------------------------------------");
                c.getPlayer().dropMessage(-11, "查看的線程名稱 : " + name + " 輸入的參數: " + poolId);
                c.getPlayer().dropMessage(-11, "ActiveCount: ...... " + StringUtil.getRightPaddedStr(scheduledPool.getActiveCount(), ' ', 8) + " 註釋: 主動執行任務的線程數");
                c.getPlayer().dropMessage(-11, "CorePoolSize: ..... " + StringUtil.getRightPaddedStr(scheduledPool.getCorePoolSize(), ' ', 8) + " 註釋: 核心線程數");
                c.getPlayer().dropMessage(-11, "PoolSize: ......... " + StringUtil.getRightPaddedStr(scheduledPool.getPoolSize(), ' ', 8) + " 註釋: 池中的當前啟動的核心線程數");
                c.getPlayer().dropMessage(-11, "LargestPoolSize: .. " + StringUtil.getRightPaddedStr(scheduledPool.getLargestPoolSize(), ' ', 8) + " 註釋: 曾經同時位於池中的最大核心線程數");
                c.getPlayer().dropMessage(-11, "MaximumPoolSize: .. " + StringUtil.getRightPaddedStr(scheduledPool.getMaximumPoolSize(), ' ', 8) + " 註釋: 允許的最大核心線程數");
                c.getPlayer().dropMessage(-11, "CompletedTaskCount: " + StringUtil.getRightPaddedStr(scheduledPool.getCompletedTaskCount(), ' ', 8) + " 註釋: 已完成執行的線程任務總數");
                c.getPlayer().dropMessage(-11, "QueuedTaskCount: ..." + StringUtil.getRightPaddedStr(scheduledPool.getQueue().size(), ' ', 8) + " 註釋: 使用的任務隊列數量");
                c.getPlayer().dropMessage(-11, "TaskCount: ........ " + StringUtil.getRightPaddedStr(scheduledPool.getTaskCount(), ' ', 8) + " 註釋: 計劃執行的線程任務總數");
            }
            return 1;
        }
    }

    public static class 清理線程 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(5, "請輸入查看線程的參數[1-7]");
                return 0;
            }
            int poolId;
            try {
                poolId = Integer.parseInt(splitted[1]);
            } catch (NumberFormatException nfe) {
                c.getPlayer().dropMessage(6, "輸入查看的線程ID錯誤.");
                return 0;
            }
            ScheduledThreadPoolExecutor scheduledPool;
            String name;
            switch (poolId) {
                case 1:
                    name = "World Timer";
                    scheduledPool = WorldTimer.getInstance().getSES();
                    break;
                case 2:
                    name = "Etc Timer";
                    scheduledPool = EtcTimer.getInstance().getSES();
                    break;
                case 3:
                    name = "Map Timer";
                    scheduledPool = MapTimer.getInstance().getSES();
                    break;
                case 4:
                    name = "Clone Timer";
                    scheduledPool = CloneTimer.getInstance().getSES();
                    break;
                case 5:
                    name = "Event Timer";
                    scheduledPool = EventTimer.getInstance().getSES();
                    break;
                case 6:
                    name = "Buff Timer";
                    scheduledPool = BuffTimer.getInstance().getSES();
                    break;
                case 7:
                    name = "Ping Timer";
                    scheduledPool = PingTimer.getInstance().getSES();
                    break;
                default:
                    return 0;
            }
            if (scheduledPool != null) {
                scheduledPool.purge();
                c.getPlayer().dropMessage(-11, "--------------------------------------------------------------------");
                c.getPlayer().dropMessage(-11, "查看的線程名稱 : " + name + " 輸入的參數: " + poolId);
                c.getPlayer().dropMessage(-11, "ActiveCount: ...... " + StringUtil.getRightPaddedStr(scheduledPool.getActiveCount(), ' ', 8) + " 註釋: 主動執行任務的線程數");
                c.getPlayer().dropMessage(-11, "CorePoolSize: ..... " + StringUtil.getRightPaddedStr(scheduledPool.getCorePoolSize(), ' ', 8) + " 註釋: 核心線程數");
                c.getPlayer().dropMessage(-11, "PoolSize: ......... " + StringUtil.getRightPaddedStr(scheduledPool.getPoolSize(), ' ', 8) + " 註釋: 池中的當前啟動的核心線程數");
                c.getPlayer().dropMessage(-11, "LargestPoolSize: .. " + StringUtil.getRightPaddedStr(scheduledPool.getLargestPoolSize(), ' ', 8) + " 註釋: 曾經同時位於池中的最大核心線程數");
                c.getPlayer().dropMessage(-11, "MaximumPoolSize: .. " + StringUtil.getRightPaddedStr(scheduledPool.getMaximumPoolSize(), ' ', 8) + " 註釋: 允許的最大核心線程數");
                c.getPlayer().dropMessage(-11, "CompletedTaskCount: " + StringUtil.getRightPaddedStr(scheduledPool.getCompletedTaskCount(), ' ', 8) + " 註釋: 已完成執行的線程任務總數");
                c.getPlayer().dropMessage(-11, "QueuedTaskCount: ..." + StringUtil.getRightPaddedStr(scheduledPool.getQueue().size(), ' ', 8) + " 註釋: 使用的任務隊列數量");
                c.getPlayer().dropMessage(-11, "TaskCount: ........ " + StringUtil.getRightPaddedStr(scheduledPool.getTaskCount(), ' ', 8) + " 註釋: 計劃執行的線程任務總數");
            }
            return 1;
        }
    }

    public static class 重置IP黑名單 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleServerHandler.blockIPList.clear();
            c.getPlayer().dropMessage(6, "清除完畢.");
            return 1;
        }
    }

    public static class 重載腳本 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "ReloadScript";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
//            AbstractScriptManager.clearScriptCache();
            c.getPlayer().dropMessage(6, "腳本重載完畢.");
            return 1;
        }
    }

    public static class 重載技能 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "ReloadSkill";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            try {
                Config.load();
                SkillFactory.reloadSkills(80011261);
                SkillFactory.loadSkillData();
                SkillFactory.loadDelays();
                SkillFactory.loadMemorySkills();
                SkillData.loadAllSkills();
                c.getPlayer().dropMessage(6, "重載完畢.");
            } catch (Exception e) {
                c.getPlayer().dropMessage(6, "指令執行異常. " + e);
            }
            return 1;
        }
    }

    public static class 禁止技能 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length != 2 || !StringUtil.isNumber(splitted[1])) {
                c.getPlayer().dropMessage(5, "格式: " + splitted[0] + " <技能ID>");
                return 0;
            }
            int skillid = Integer.valueOf(splitted[1]);
            if (SkillFactory.getSkill(skillid) == null) {
                c.getPlayer().dropMessage(5, "不存在的技能,無法禁用");
                return 0;
            }
            if (!SkillFactory.addBlockedSkill(Integer.valueOf(splitted[1]))) {
                c.getPlayer().dropMessage(5, "禁用完畢.");
                return 0;
            } else {
                c.getPlayer().dropMessage(5, "已經禁用的技能ID");
            }
            return 1;
        }
    }

    public static class 能力點 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "gainAP";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2 || !StringUtil.isNaturalNumber(splitted[1])) {
                c.getPlayer().dropMessage(5, "格式: " + splitted[0] + " <取得數量>");
                return 0;
            }
            c.getPlayer().gainAp(Short.parseShort(splitted[1]));
            c.getPlayer().dropMessage(5, "獲得AP: " + splitted[1]);
            return 1;
        }
    }

    public static class 魔量 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "MP";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2 || !StringUtil.isNaturalNumber(splitted[1])) {
                c.getPlayer().dropMessage(5, "格式: " + splitted[0] + " <魔量>");
                return 0;
            }
            c.getPlayer().getStat().setInfo(c.getPlayer().getStat().getMaxHp(false), Integer.parseInt(splitted[1]), c.getPlayer().getStat().getHp(), Integer.parseInt(splitted[1]));
            c.getPlayer().updateSingleStat(MapleStat.MP, Integer.parseInt(splitted[1]));
            c.getPlayer().updateSingleStat(MapleStat.MAXMP, Integer.parseInt(splitted[1]));
            c.getPlayer().getStat().recalcLocalStats(c.getPlayer());
            return 1;
        }
    }

    public static class 血量 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "HP";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2 || !StringUtil.isNaturalNumber(splitted[1])) {
                c.getPlayer().dropMessage(5, "格式: " + splitted[0] + " <血量>");
                return 0;
            }
            c.getPlayer().getStat().setInfo(Integer.parseInt(splitted[1]), c.getPlayer().getStat().getMaxMp(false), Integer.parseInt(splitted[1]), c.getPlayer().getStat().getMp());
            c.getPlayer().updateSingleStat(MapleStat.HP, Integer.parseInt(splitted[1]));
            c.getPlayer().updateSingleStat(MapleStat.MAXHP, Integer.parseInt(splitted[1]));
            c.getPlayer().getStat().recalcLocalStats(c.getPlayer());
            return 1;
        }
    }

    public static class 滿技能 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "maxSkill";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            int jobId = c.getPlayer().getJob();
            return 1;
        }
    }


    public static class 開腳本 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "OpenNpc";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(5, "格式: " + splitted[0] + " [NPC代碼(可選)] <mode/腳本名稱>");
                return 0;
            }
            String script = null;
            int npcId = 0;
            if (StringUtil.isNaturalNumber(splitted[1])) {
                npcId = Integer.parseInt(splitted[1]);
            } else {
                script = splitted[1];
            }
            if (script == null && splitted.length > 2) {
                script = splitted[2];
            }
            if (npcId == 0 && script == null) {
                c.getPlayer().dropMessage(5, "格式: " + splitted[0] + " [NPC代碼(可選)] <mode/腳本名稱>");
                return 0;
            }
            c.getPlayer().getScriptManager().startNpcScript(npcId, 0, script);
//            NPCScriptManager.getInstance().start(c, npcId, script);
            return 1;
        }
    }

    public static class 封包記錄 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "LogPacket";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            ServerConstants.setLogPacket(!ServerConstants.isLogPacket());
            c.getPlayer().dropMessage(5, "封包記錄已" + (ServerConstants.isLogPacket() ? "開啟" : "關閉"));
            return 1;
        }
    }
}
