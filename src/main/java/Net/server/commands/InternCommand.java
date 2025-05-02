package Net.server.commands;

import Client.*;
import Client.anticheat.ReportType;
import Client.inventory.Item;
import Client.inventory.MapleInventory;
import Client.inventory.MapleInventoryType;
import Client.skills.Skill;
import Client.skills.SkillFactory;
import Config.constants.GameConstants;
import Config.constants.enums.UserChatMessageType;
import Config.constants.skills.管理員;
import Net.server.MapleInventoryManipulator;
import Net.server.MapleItemInformationProvider;
import Net.server.MaplePortal;
import Net.server.life.MapleMonster;
import Net.server.life.MapleNPC;
import Net.server.maps.*;
import Net.server.quest.MapleQuest;
import Packet.MaplePacketCreator;
import Plugin.provider.MapleData;
import Plugin.provider.MapleDataProvider;
import Plugin.provider.MapleDataProviderFactory;
import Plugin.provider.MapleDataTool;
import Plugin.script.binding.ScriptEvent;
import Server.channel.ChannelServer;
import Server.world.CheaterData;
import Server.world.World;
import Server.world.WorldBroadcastService;
import Server.world.WorldFindService;
import connection.packet.FieldPacket;
import SwordieX.field.ClockPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Pair;
import tools.StringUtil;

import java.awt.*;
import java.text.DateFormat;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Emilyx3
 */
@Deprecated
public class InternCommand {

    private static final Logger log = LoggerFactory.getLogger(InternCommand.class);

    /**
     * @return
     */
    public static PlayerRank getPlayerLevelRequired() {
        return PlayerRank.實習管理員;
    }

    public static class 隱藏 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "Hide";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            Skill skill = SkillFactory.getSkill(管理員.終極隱藏);
            MapleCharacter player = c.getPlayer();
            if (c.getPlayer().isHidden() || player.isInvincible()) {
                c.getPlayer().cancelEffect(skill.getEffect(1), false, -1);
                player.setInvincible(false);
                player.dropMessage(-7, "〔 GM技能 : 金鋼不壞 / 終極隱藏 〕已關閉...");
                player.dropMessage(-11, "〔 GM技能 : 金鋼不壞 / 終極隱藏 〕已關閉...");
            } else {
                skill.getEffect(1).applyTo(c.getPlayer());
                player.setInvincible(true);
                player.resetAllCooldowns(false);
                player.dropMessage(-7, "〔 GM技能 : 金鋼不壞 / 終極隱藏 〕已開啟...");
                player.dropMessage(-11, "〔 GM技能 : 金鋼不壞 / 終極隱藏 〕已開啟...");
            }
            return 0;
        }
    }


    public static class 低血量 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "LowHP";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().updateSingleStat(MapleStat.HP, 1);
            c.getPlayer().updateSingleStat(MapleStat.MP, 1);
            c.getPlayer().getStat().setHp((short) 1, c.getPlayer());
            c.getPlayer().getStat().setMp((short) 1);
            return 0;
        }
    }


    public static class 治癒 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "Heal";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().getStat().heal(c.getPlayer());
            c.getPlayer().removeDebuffs();
            return 0;
        }
    }


    public static class 治癒全圖 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "HealHere";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            for (MapleCharacter mch : player.getMap().getCharacters()) {
                if (mch != null) {
                    mch.getStat().setHp(mch.getStat().getCurrentMaxHP(), mch);
                    mch.updateSingleStat(MapleStat.HP, mch.getStat().getCurrentMaxHP());
                    mch.getStat().setMp(mch.getStat().getCurrentMaxMP());
                    mch.updateSingleStat(MapleStat.MP, mch.getStat().getCurrentMaxMP());
                    mch.removeDebuffs();
                }
            }
            return 1;
        }
    }

    public static class 限時鎖帳 implements CommandExecute {

        private final String[] types = {"HACK", "BOT", "AD", "HARASS", "CURSE", "SCAM", "MISCONDUCT", "SELL", "ICASH", "TEMP", "GM", "IPROGRAM", "MEGAPHONE"};
        protected boolean ipBan = false;

        @Override
        public String getShortCommand() {
            return "TempBan";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 4) {
                c.getPlayer().dropMessage(6, splitted[0] + " [玩家名字] [理由] [多少天]");
                StringBuilder s = new StringBuilder("Tempban reasons: ");
                for (int i = 0; i < types.length; i++) {
                    s.append(i + 1).append(" - ").append(types[i]).append(", ");
                }
                c.getPlayer().dropMessage(6, s.toString());
                return 0;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            int reason = Integer.parseInt(splitted[2]);
            int numDay = Integer.parseInt(splitted[3]);

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, numDay);
            DateFormat df = DateFormat.getInstance();

            if (victim == null || reason < 0 || reason >= types.length) {
                c.getPlayer().dropMessage(6, "Unable to find character or reason was not valid, type tempban to see reasons");
                return 0;
            }
            victim.tempban("Temp banned by " + c.getPlayer().getName() + " for " + types[reason] + " reason", cal, reason, ipBan);
            c.getPlayer().dropMessage(6, "The character " + splitted[1] + " has been successfully tempbanned till " + df.format(cal.getTime()));
            return 1;
        }
    }

    public static class 封鎖帳號 implements CommandExecute {

        protected boolean hellban = false,


        ipBan = false;

        @Override
        public String getShortCommand() {
            return "Ban";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(5, "[用法] " + splitted[0] + " <IGN> <Reason>");
                return 0;
            }
            StringBuilder sb = new StringBuilder();
            if (hellban) {
                sb.append("Banned ").append(splitted[1]).append(": ").append(StringUtil.joinStringFrom(splitted, 2));
            } else {
                sb.append(c.getPlayer().getName()).append(" banned ").append(splitted[1]).append(": ").append(StringUtil.joinStringFrom(splitted, 2));
            }
            MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (target != null) {
                if (c.getPlayer().getGmLevel() > target.getGmLevel() || c.getPlayer().isAdmin()) {
                    sb.append(" (IP: ").append(target.getClient().getSessionIPAddress()).append(")");
                    if (target.ban(sb.toString(), hellban || ipBan, false, hellban)) {
                        c.getPlayer().dropMessage(6, "[" + splitted[0].substring(1) + "] 已成功將玩家 " + splitted[1] + " 進行封號處理.");
                        return 1;
                    } else {
                        c.getPlayer().dropMessage(6, "[" + splitted[0].substring(1) + "] 封號失敗.");
                        return 0;
                    }
                } else {
                    c.getPlayer().dropMessage(6, "[" + splitted[0].substring(1) + "] 對方的管理權限比你高無法對其進行封號...");
                    return 1;
                }
            } else {
                if (MapleCharacter.ban(splitted[1], sb.toString(), false, c.getPlayer().getGmLevel(), hellban)) { //c.getPlayer().isAdmin() ? 250 : 刪除此項 以免封停玩家的IP錯誤
                    c.getPlayer().dropMessage(6, "[" + splitted[0].substring(1) + "] 已成功將玩家 " + splitted[1] + " 進行離線封號.");
                    return 1;
                } else {
                    c.getPlayer().dropMessage(6, "[" + splitted[0].substring(1) + "] 離線封號失敗 " + splitted[1]);
                    return 0;
                }
            }
        }
    }


    public static class CC implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().changeChannel(Integer.parseInt(splitted[1]));
            return 1;
        }
    }


    public static class CCPlayer implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().changeChannel(WorldFindService.getInstance().findChannel(splitted[1]));
            return 1;
        }
    }


    public static class 踢下線 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "DC";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[splitted.length - 1]);
            if (victim != null && c.getPlayer().getGmLevel() >= victim.getGmLevel()) {
                victim.getClient().disconnect(true, false);
                victim.getClient().getSession().close();
                c.getPlayer().dropMessage(6, "已經成功斷開 " + victim.getName() + " 的連接.");
                return 1;
            } else {
                c.getPlayer().dropMessage(6, "使用的對象不存在或者角色名字錯誤或者對方的GM權限比你高.");
                return 0;
            }
        }
    }


    public static class 殺 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "Kill";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter player = c.getPlayer();
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "用法: " + splitted[0] + " <list player names>");
                return 0;
            }
            MapleCharacter victim = null;
            for (int i = 1; i < splitted.length; i++) {
                try {
                    victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[i]);
                } catch (Exception e) {
                    c.getPlayer().dropMessage(6, "沒有找到名字為: " + splitted[i] + " 的玩家.");
                }
                if (player.allowedToTarget(victim) && player.getGmLevel() >= victim.getGmLevel()) {
                    victim.addHPMP(-100, -100);
                }
            }
            return 1;
        }
    }

    public static class 殺全圖 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "KillMap";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            for (MapleCharacter victim : c.getPlayer().getMap().getCharacters()) {
                if (victim != null && victim.getGmLevel() <= c.getGmLevel()) {
                    victim.addHPMP(-100, -100);
                }
            }
            return 1;
        }
    }


    public static class 我在哪裡 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "WhereAmI";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().dropMessage(5, "你所在的地圖為: " + c.getPlayer().getMap().toString());
            return 1;
        }
    }


    public static class 清理道具 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "ClearInv";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            Map<Pair<Short, Short>, MapleInventoryType> eqs = new HashMap<>();
            switch (splitted[1]) {
                case "全部":
                case "all":
                    for (MapleInventoryType type : MapleInventoryType.values()) {
                        for (Item item : c.getPlayer().getInventory(type)) {
                            eqs.put(new Pair<>(item.getPosition(), item.getQuantity()), type);
                        }
                    }
                    break;
                case "已裝備":
                case "eqp":
                    for (Item item : c.getPlayer().getInventory(MapleInventoryType.EQUIPPED)) {
                        eqs.put(new Pair<>(item.getPosition(), item.getQuantity()), MapleInventoryType.EQUIPPED);
                    }
                    break;
                case "裝備":
                case "eq":
                    for (Item item : c.getPlayer().getInventory(MapleInventoryType.EQUIP)) {
                        eqs.put(new Pair<>(item.getPosition(), item.getQuantity()), MapleInventoryType.EQUIP);
                    }
                    break;
                case "消耗":
                case "u":
                    for (Item item : c.getPlayer().getInventory(MapleInventoryType.USE)) {
                        eqs.put(new Pair<>(item.getPosition(), item.getQuantity()), MapleInventoryType.USE);
                    }
                    break;
                case "裝飾":
                case "s":
                    for (Item item : c.getPlayer().getInventory(MapleInventoryType.SETUP)) {
                        eqs.put(new Pair<>(item.getPosition(), item.getQuantity()), MapleInventoryType.SETUP);
                    }
                    break;
                case "其他":
                case "e":
                    for (Item item : c.getPlayer().getInventory(MapleInventoryType.ETC)) {
                        eqs.put(new Pair<>(item.getPosition(), item.getQuantity()), MapleInventoryType.ETC);
                    }
                    break;
                case "特殊":
                case "c":
                    for (Item item : c.getPlayer().getInventory(MapleInventoryType.CASH)) {
                        eqs.put(new Pair<>(item.getPosition(), item.getQuantity()), MapleInventoryType.CASH);
                    }
                    break;
                case "時裝":
                case "d":
                    for (Item item : c.getPlayer().getInventory(MapleInventoryType.DECORATION)) {
                        eqs.put(new Pair<>(item.getPosition(), item.getQuantity()), MapleInventoryType.DECORATION);
                    }
                    break;
                default:
                    c.getPlayer().dropMessage(6, "[全部(all)/已裝備(eqp)/裝備(eq)/消耗(u)/裝飾(s)/其他(e)/特殊(c)/時裝(d)]");
                    break;
            }
            for (Entry<Pair<Short, Short>, MapleInventoryType> eq : eqs.entrySet()) {
                MapleInventoryManipulator.removeFromSlot(c, eq.getValue(), eq.getKey().left, eq.getKey().right, false, false);
            }
            return 1;
        }
    }

    public static class 線上 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "online";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().dropMessage(6, "頻道[ " + c.getChannel() + " ]:");
            c.getPlayer().dropMessage(6, c.getChannelServer().getPlayerStorage().getOnlinePlayers(true));
            return 1;
        }
    }


    public static class 頻道線上 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().dropMessage(6, "頻道[ " + Integer.parseInt(splitted[1]) + " ]:");
            c.getPlayer().dropMessage(6, ChannelServer.getInstance(Integer.parseInt(splitted[1])).getPlayerStorage().getOnlinePlayers(true));
            return 1;
        }
    }


    public static class ItemCheck implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3 || splitted[1] == null || splitted[1].equals("") || splitted[2] == null || splitted[2].equals("")) {
                c.getPlayer().dropMessage(6, "用法: " + splitted[0] + " <玩家名字> <道具ID>");
                return 0;
            } else {
                int item = Integer.parseInt(splitted[2]);
                MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
                int itemamount = chr.getItemQuantity(item, true);
                if (itemamount > 0) {
                    c.getPlayer().dropMessage(6, chr.getName() + " 擁有 " + itemamount + " (" + item + ").");
                } else {
                    c.getPlayer().dropMessage(6, chr.getName() + " 沒有ID為 (" + item + ") 的道具.");
                }
            }
            return 1;
        }
    }


    public static class Song implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.musicChange(splitted[1]));
            return 1;
        }
    }


    public static class CheckPoint implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "請輸入玩家的名字.");
                return 0;
            }
            MapleCharacter chrs = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (chrs == null) {
                c.getPlayer().dropMessage(6, "當前頻道沒有找到這個玩家.");
            } else {
                c.getPlayer().dropMessage(6, chrs.getName() + " 擁有 " + chrs.getPoints() + " 點");
            }
            return 1;
        }
    }


    public static class CheckVPoint implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "請輸入玩家的名字.");
                return 0;
            }
            MapleCharacter chrs = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (chrs == null) {
                c.getPlayer().dropMessage(6, "當前頻道沒有找到這個玩家");
            } else {
                c.getPlayer().dropMessage(6, chrs.getName() + " 擁有 " + chrs.getVPoints() + " 點.");
            }
            return 1;
        }
    }


    public static class PermWeather implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().getMap().getPermanentWeather() > 0) {
                c.getPlayer().getMap().setPermanentWeather(0);
                c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.removeMapEffect());
                c.getPlayer().dropMessage(5, "當前地圖的效果已禁止.");
            } else {
                int weather = CommandProcessorUtil.getOptionalIntArg(splitted, 1, 5120000);
                if (!MapleItemInformationProvider.getInstance().itemExists(weather) || weather / 10000 != 512) {
                    c.getPlayer().dropMessage(5, "請輸入ID.");
                } else {
                    c.getPlayer().getMap().setPermanentWeather(weather);
                    c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.startMapEffect("", weather, false));
                    c.getPlayer().dropMessage(5, "當前地圖的效果已開啟.");
                }
            }
            return 1;
        }
    }


    public static class 角色訊息 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "charinfo";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            StringBuilder builder = new StringBuilder();
            MapleCharacter other;
            if (splitted.length == 1) {
                other = c.getPlayer();
            } else {
                other = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            }
            if (other == null) {
                builder.append("輸入的角色不存在...");
                c.getPlayer().dropMessage(6, builder.toString());
                return 0;
            }

            builder.append(MapleClient.getLogMessage(other, ""));
            builder.append(" 坐標 ").append(other.getPosition().x);
            builder.append(" /").append(other.getPosition().y);
            builder.append("\r\n裝備列表:");

            // 獲取角色裝備的所有欄位
            MapleInventory equippedInventory = other.getInventory(MapleInventoryType.EQUIPPED);
            for (short i = -128; i < 0; i++) {
                Item equip = equippedInventory.getItem(i);
                if (equip != null) {
                    builder.append(String.format("\r\nSlot:" + i));
                    builder.append(String.format("\r\n名稱: " + equip.getName()));
                    builder.append(String.format("\r\n道具ID: " + equip.getItemId()));
                }
            }

            //builder.append(" || HP : ");
            //builder.append(other.getStat().getHp());
            //builder.append(" /");
            //builder.append(other.getStat().getCurrentMaxHP());

            //builder.append(" || MP : ");
            //builder.append(other.getStat().getMp());
            //builder.append(" /");
            //builder.append(other.getStat().getCurrentMaxMP());

            //builder.append(" || 屬性攻擊力 : ");
            //builder.append(Math.round(other.getStat().getMinDamage(other)) + "-" + Math.round(other.getStat().getMaxDamage(other)));

            //builder.append(" || 傷害% : ");
            //builder.append(other.getStat().getDamageRate());
            //builder.append(" || BOSS傷害% : ");
            //builder.append(other.getStat().getBossDamageRate());
            //builder.append(" || 最終傷害% : ");
            //builder.append(Math.round(other.getStat().getIncFinalDamage() * 100 / 100));
            //builder.append(" || 最終傷害% : ");
            //builder.append(other.getStat().percent_damage);
            //builder.append(" || BUFF持續時間% : ");
            //builder.append(other.getStat().getIncBuffTime());
            //builder.append(" || 無視防禦率% : ");
            //builder.append(other.getStat().getIgnoreMobpdpR());
            //builder.append(" || 道具掉落率% : ");
            //builder.append(other.getStat().getDropBuff());
            //builder.append(" || 爆擊機率% : ");
            //builder.append(other.getStat().getCriticalRate());
            //builder.append(" || 楓幣獲得量% : ");
            //builder.append(other.getStat().getMesoBuff());
            //builder.append(" || 爆擊傷害% : ");
            //builder.append(other.getStat().getCriticalDamage());
            //builder.append(" || 攻擊速度 : ");
            //builder.append(other.getStat().attackSpeed);
            //builder.append(" || 攻擊力 : ");
            //builder.append(other.getStat().getTotalWatk());
            //builder.append(" || 魔法攻擊力 : ");
            //builder.append(other.getStat().getTotalMagic());
            //builder.append(" || 狀態異常耐性 : ");
            //builder.append(other.getStat().asrR);
            //builder.append(" || 所有屬性耐性增加 : ");
            //builder.append(other.getStat().terR);
            //builder.append(" || 檔格 : ");
            //builder.append(other.getStat().getStanceProp());
            //builder.append(" || 防禦力 : ");
            //builder.append(other.getStat().wdef);
            //builder.append(" || 星力 : ");
            //builder.append(other.getStat().getStarForce());
            //builder.append(" || 移動速度 : ");
            //builder.append(other.getStat().getSpeed());
            //builder.append(" || 神秘力量 : ");
            //builder.append(other.getStat().getArc());
            //builder.append(" || 跳躍力 : ");
            //builder.append(other.getStat().getJump());

            //builder.append(" || 力量 : ");
            //builder.append(other.getStat().getStr());
            //builder.append(" || 敏捷 : ");
            //builder.append(other.getStat().getDex());
            //builder.append(" || 智力 : ");
            //builder.append(other.getStat().getInt());
            //builder.append(" || 幸運 : ");
            //builder.append(other.getStat().getLuk());

            //builder.append(" || 全部力量 : ");
            //builder.append(other.getStat().getTotalStr());
            //builder.append(" || 全部敏捷 : ");
            //builder.append(other.getStat().getTotalDex());
            //builder.append(" || 全部智力 : ");
            //builder.append(other.getStat().getTotalInt());
            //builder.append(" || 全部幸運 : ");
            //builder.append(other.getStat().getTotalLuk());

            //builder.append(" || 經驗值 : ");
            //builder.append(other.getExp());
            //builder.append(" || 楓幣 : ");
            //builder.append(other.getMeso());

            //builder.append(" || 是否組隊 : ");
            //builder.append(other.getParty() == null ? -1 : other.getParty().getId());

            //builder.append(" || 是否交易: ");
            //builder.append(other.getTrade() != null);

            c.getPlayer().dropMessage(6, builder.toString());
            return 1;
        }
    }


    public static class Reports implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            List<CheaterData> cheaters = World.getReports();
            for (int x = cheaters.size() - 1; x >= 0; x--) {
                CheaterData cheater = cheaters.get(x);
                c.getPlayer().dropMessage(6, cheater.getInfo());
            }
            return 1;
        }
    }


    public static class ClearReport implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3) {
                StringBuilder ret = new StringBuilder("用法 " + splitted[0] + " [ign] [all/");
                for (ReportType type : ReportType.values()) {
                    ret.append(type.theId).append('/');
                }
                ret.setLength(ret.length() - 1);
                c.getPlayer().dropMessage(6, ret.append(']').toString());
                return 0;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim == null) {
                c.getPlayer().dropMessage(5, "輸入的角色不存在...");
                return 0;
            }
            ReportType type = ReportType.getByString(splitted[2]);
            if (type != null) {
                victim.clearReports(type);
            } else {
                victim.clearReports();
            }
            c.getPlayer().dropMessage(5, "完成.");
            return 1;
        }
    }


    public static class 檢測作弊 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            List<CheaterData> cheaters = World.getCheaters();
            for (int x = cheaters.size() - 1; x >= 0; x--) {
                CheaterData cheater = cheaters.get(x);
                c.getPlayer().dropMessage(6, cheater.getInfo());
            }
            return 1;
        }
    }


    public static class 線上人數 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            Map<Integer, Integer> connected = World.getConnected();
            StringBuilder conStr = new StringBuilder("線上人數: ");
            boolean first = true;
            for (int i : connected.keySet()) {
                if (!first) {
                    conStr.append(", ");
                } else {
                    first = false;
                }
                if (i == 0) {
                    conStr.append("計總: ");
                    conStr.append(connected.get(i));
                } else if (i == -10) {
                    conStr.append("商場: ");
                    conStr.append(connected.get(i));
                } else {
                    conStr.append("頻道");
                    conStr.append(i);
                    conStr.append(": ");
                    conStr.append(connected.get(i));
                }
            }
            c.getPlayer().dropMessage(6, conStr.toString());
            return 1;
        }
    }


    public static class 附近傳送點 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "NearestPortal";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MaplePortal portal = c.getPlayer().getMap().findClosestPortal(c.getPlayer().getPosition());
            c.getPlayer().dropMessage(6, portal.getName() + " ID: " + portal.getId() + " 腳本: " + portal.getScriptName() + " 坐標: " + portal.getPosition().x + "," + portal.getPosition().y + " 目標地圖: " + portal.getTargetMapId() + " / " + portal.getTarget());
            return 1;
        }
    }


    public static class SpawnDebug implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().dropMessage(6, c.getPlayer().getMap().spawnDebug());
            return 1;
        }
    }


    public static class FakeRelog implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().fakeRelog();
            return 1;
        }
    }


    public static class 清理掉寶 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "RemoveDrops";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().dropMessage(5, "已成功將當前地圖的 " + c.getPlayer().getMap().getNumItems() + " 個道具清理.");
            c.getPlayer().getMap().removeDrops();
            return 1;
        }
    }


    public static class ListInstances implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
//            EventManager em = c.getChannelServer().getEventSM().getEventManager(StringUtil.joinStringFrom(splitted, 1));
//            if (em == null || em.getInstances().size() <= 0) {
//                c.getPlayer().dropMessage(5, "沒有找到.");
//            } else {
//                for (ScriptEvent eim : em.getInstances()) {
//                    c.getPlayer().dropMessage(5, "Event " + eim.getName() + ", charSize: " + eim.getPlayers().size() + ", dcedSize: " + eim.getDisconnected().size() + ", mobSize: " + eim.getMobs().size() + ", eventManager: " + em.getName() + ", timeLeft: " + eim.getTimeLeft() + ", iprops: " + eim.getProperties().toString() + ", eprops: " + em.getProperties().toString());
//                }
//            }
            return 0;
        }
    }


    public static class Uptime implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().dropMessage(6, "Server has been up for " + StringUtil.getReadableMillis(ChannelServer.serverStartTime, System.currentTimeMillis()));
            return 1;
        }
    }


    public static class 副本訊息 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "EventInstance";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().getEventInstance() == null) {
                c.getPlayer().dropMessage(5, "沒有找到.");
            } else {
                ScriptEvent eim = c.getPlayer().getEventInstance();
//                c.getPlayer().dropMessage(5, "Event " + eim.getName() + ", ", dcedSize: " + eim.getDisconnected().size() + ", mobSize: " + eim.getMobs().size() + ", eventManager: " + eim.getEventManager().getName() + ", timeLeft: " + eim.getTimeLeft() + ", iprops: " + eim.getProperties().toString() + ", eprops: " + eim.getEventManager().getProperties().toString());
            }
            return 1;
        }
    }


    public static class 去往 implements CommandExecute {

        private static final HashMap<String, Integer> gotomaps = new HashMap<>();

        static {
            gotomaps.put("GM地圖", 180000000);
            gotomaps.put("工作場所", 180000000);
            gotomaps.put("gmmap", 180000000);
            gotomaps.put("楓之港", 2000000);
            gotomaps.put("southperry", 2000000);
            gotomaps.put("楓之島", 1010000);
            gotomaps.put("amherst", 1010000);
            gotomaps.put("弓箭手村", 100000000);
            gotomaps.put("henesys", 100000000);
            gotomaps.put("魔法森林", 101000000);
            gotomaps.put("ellinia", 101000000);
            gotomaps.put("愛裡涅湖水", 101071300);
            gotomaps.put("勇士之村", 102000000);
            gotomaps.put("perion", 102000000);
            gotomaps.put("墮落城市", 103000000);
            gotomaps.put("kerning", 103000000);
            gotomaps.put("維多利亞港", 104000000);
            gotomaps.put("harbor", 104000000);
            gotomaps.put("奇幻村", 105000000);
            gotomaps.put("sleepywood", 105000000);
            gotomaps.put("florina", 120000300);
            gotomaps.put("天空之城", 200000000);
            gotomaps.put("orbis", 200000000);
            gotomaps.put("幸福村", 209000000);
            gotomaps.put("happyville", 209000000);
            gotomaps.put("冰原雪域", 211000000);
            gotomaps.put("elnath", 211000000);
            gotomaps.put("玩具城", 220000000);
            gotomaps.put("ludibrium", 220000000);
            gotomaps.put("水之都", 230000000);
            gotomaps.put("aquaroad", 230000000);
            gotomaps.put("神木村", 240000000);
            gotomaps.put("leafre", 240000000);
            gotomaps.put("桃花仙境", 250000000);
            gotomaps.put("mulung", 250000000);
            gotomaps.put("藥靈幻境", 251000000);
            gotomaps.put("herbtown", 251000000);
            gotomaps.put("omegasector", 221000000);
            gotomaps.put("koreanfolktown", 222000000);
            gotomaps.put("新葉城", 600000000);
            gotomaps.put("newleafcity", 600000000);
            gotomaps.put("sharenian", 990000000);
            gotomaps.put("海怒斯", 230040420);
            gotomaps.put("pianus", 230040420);
            gotomaps.put("闇黑龍王", 240060200);
            gotomaps.put("horntail", 240060200);
            gotomaps.put("混沌闇黑龍王", 240060201);
            gotomaps.put("chorntail", 240060201);
            gotomaps.put("格瑞芬多", 240020101);
            gotomaps.put("griffey", 240020101);
            gotomaps.put("噴火龍", 240020401);
            gotomaps.put("manon", 240020401);
            gotomaps.put("殘暴炎魔", 280030100);
            gotomaps.put("zakum", 280030100);
            gotomaps.put("混沌殘暴炎魔", 280030000);
            gotomaps.put("czakum", 280030001);
            gotomaps.put("拉圖斯", 220080001);
            gotomaps.put("papulatus", 220080001);
            gotomaps.put("昭和村", 801000000);
            gotomaps.put("showatown", 801000000);
            gotomaps.put("江戶村", 800000000);
            gotomaps.put("zipangu", 800000000);
            gotomaps.put("納希沙漠", 260000100);
            gotomaps.put("ariant", 260000100);
            gotomaps.put("鯨魚號", 120000000);
            gotomaps.put("nautilus", 120000000);
            gotomaps.put("耶雷弗", 130000000);
            gotomaps.put("erev", 130000000);
            gotomaps.put("艾靈森林", 300000000);
            gotomaps.put("ellin", 300000000);
            gotomaps.put("鄉村鎮", 551000000);
            gotomaps.put("kampung", 551000000);
            gotomaps.put("結婚村莊", 680000000);
            gotomaps.put("amoria", 680000000);
            gotomaps.put("三扇門", 270000000);
            gotomaps.put("timetemple", 270000000);
            gotomaps.put("皮卡啾", 270050100);
            gotomaps.put("pinkbean", 270050100);
            gotomaps.put("自由市場", 910000000);
            gotomaps.put("fm", 910000000);
            gotomaps.put("freemarket", 910000000);
            gotomaps.put("選邊站", 109020001);
            gotomaps.put("oxquiz", 109020001);
            gotomaps.put("向上攀升", 109030101);
            gotomaps.put("ola", 109030101);
            gotomaps.put("障礙競走", 109040000);
            gotomaps.put("fitness", 109040000);
            gotomaps.put("滾雪球", 109060000);
            gotomaps.put("snowball", 109060000);
            gotomaps.put("golden", 950100000);
            gotomaps.put("phantom", 610010000);
            gotomaps.put("cwk", 610030000);
            gotomaps.put("瑞恩村", 140000000);
            gotomaps.put("rien", 140000000);
            gotomaps.put("埃德爾斯坦", 310000000);
            gotomaps.put("edel", 310000000);
            gotomaps.put("專業技術村", 910001000);
            gotomaps.put("ardent", 910001000);
            gotomaps.put("craft", 910001000);
            gotomaps.put("pvp", 960000000);
            gotomaps.put("未來之門", 271000000);
            gotomaps.put("future", 271000000);
            gotomaps.put("萬神殿", 400000000);
            gotomaps.put("六條岔道", 104020000);
            gotomaps.put("黃昏的勇士之村", 273000000);
            gotomaps.put("克林森烏德城", 301000000);
        }

        @Override
        public String getShortCommand() {
            return "GoTo";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "用法: " + splitted[0] + " <mapname>");
            } else {
                if (gotomaps.containsKey(splitted[1])) {
                    MapleMap target = c.getChannelServer().getMapFactory().getMap(gotomaps.get(splitted[1]));
                    if (target == null) {
                        c.getPlayer().dropMessage(6, "輸入的地圖不存在.");
                        return 0;
                    }
                    MaplePortal targetPortal = target.getPortal(0);
                    c.getPlayer().changeMap(target, targetPortal);
                } else {
                    if (splitted[1].equals("locations") || splitted[1].equals("列表")) {
                        c.getPlayer().dropMessage(6, "Use " + splitted[0] + " <列表/location>. Locations are as follows:");
                        StringBuilder sb = new StringBuilder();
                        for (String s : gotomaps.keySet()) {
                            sb.append(s).append(", ");
                        }
                        c.getPlayer().dropMessage(6, sb.substring(0, sb.length() - 2));
                    } else {
                        c.getPlayer().dropMessage(6, "Invalid command syntax - Use " + splitted[0] + " <列表/location>. For a list of locations, use " + splitted[0] + " locations.");
                    }
                }
            }
            return 1;
        }
    }


    public static class MonsterDebug implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleMap map = c.getPlayer().getMap();
            double range = Double.POSITIVE_INFINITY;

            if (splitted.length > 1) {
                //&& !splitted[0].equals("!killmonster") && !splitted[0].equals("!hitmonster") && !splitted[0].equals("!hitmonsterbyoid") && !splitted[0].equals("!killmonsterbyoid")) {
                int irange = Integer.parseInt(splitted[1]);
                if (splitted.length <= 2) {
                    range = irange;
                } else {
                    map = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(splitted[2]));
                }
            }
            if (map == null) {
                c.getPlayer().dropMessage(6, "輸入的地圖ID無效.");
                return 0;
            }
            MapleMonster mob;
            for (MapleMapObject monstermo : map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Collections.singletonList(MapleMapObjectType.MONSTER))) {
                mob = (MapleMonster) monstermo;
                c.getPlayer().dropMessage(6, "怪物: " + mob.toString());
            }
            return 1;
        }
    }


    public static class 查看NPC implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            for (MapleMapObject reactor1l : c.getPlayer().getMap().getAllNPCsThreadsafe()) {
                MapleNPC reactor2l = (MapleNPC) reactor1l;
                c.getPlayer().dropMessage(5, "NPC訊息: 工作ID: " + reactor2l.getObjectId() + " npcID: " + reactor2l.getId() + " 位置: " + reactor2l.getPosition().toString() + " 名字: " + reactor2l.getName());
            }
            return 0;
        }
    }


    public static class 查看反應堆 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "lookReactor";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            for (MapleMapObject reactor1l : c.getPlayer().getMap().getAllReactorsThreadsafe()) {
                MapleReactor reactor2l = (MapleReactor) reactor1l;
                c.getPlayer().dropMessage(5, "反應堆訊息: 工作ID: " + reactor2l.getObjectId() + " reactorID: " + reactor2l.getReactorId() + " 位置: " + reactor2l.getPosition().toString() + " 狀態: " + reactor2l.getState() + " 名字: " + reactor2l.getName());
            }
            return 0;
        }
    }


    public static class 查看傳送門 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            for (MaplePortal portal : c.getPlayer().getMap().getPortals()) {
                c.getPlayer().dropMessage(5, "傳送門訊息: ID: " + portal.getId() + " Plugin.script: " + portal.getScriptName() + " name: " + portal.getName() + " pos: " + portal.getPosition().x + "," + portal.getPosition().y + " target: " + portal.getTargetMapId() + " / " + portal.getTarget());
            }
            return 0;
        }
    }


    public static class MyNPCPos implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            Point pos = c.getPlayer().getPosition();
            c.getPlayer().dropMessage(6, "X: " + pos.x + " | Y: " + pos.y + " | RX0: " + (pos.x + 50) + " | RX1: " + (pos.x - 50) + " | FH: " + c.getPlayer().getFH());
            return 1;
        }
    }


    public static class Clock implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().getMap().broadcastMessage(FieldPacket.clock(ClockPacket.secondsClock(CommandProcessorUtil.getOptionalIntArg(splitted, 1, 60))));
            return 1;
        }
    }


    public static class 來這裡 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "WarpHere";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null) {
                if (c.getPlayer().inPVP() || (!c.getPlayer().isGm() && (victim.isInBlockedMap() || victim.isGm()))) {
                    c.getPlayer().dropMessage(5, "請稍後在試.");
                    return 0;
                }
                victim.changeMap(c.getPlayer().getMap(), c.getPlayer().getMap().findClosestPortal(c.getPlayer().getPosition()));
            } else {
                int ch = WorldFindService.getInstance().findChannel(splitted[1]);
                if (ch < 0) {
                    c.getPlayer().dropMessage(5, "沒有找到玩家[" + splitted[1] + "],請確認玩家是否在線上或者輸入的角色名字是否正確.");
                    return 0;
                }
                victim = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(splitted[1]);
                if (victim == null || victim.inPVP() || (!c.getPlayer().isGm() && (victim.isInBlockedMap() || victim.isGm()))) {
                    c.getPlayer().dropMessage(5, "請稍後在試.");
                    return 0;
                }
                c.getPlayer().dropMessage(5, "Victim is cross changing channel.");
                victim.dropMessage(5, "Cross changing channel.");
                if (victim.getMapId() != c.getPlayer().getMapId()) {
                    MapleMap mapp = victim.getClient().getChannelServer().getMapFactory().getMap(c.getPlayer().getMapId());
                    victim.changeMap(mapp, mapp.findClosestPortal(c.getPlayer().getPosition()));
                }
                victim.changeChannel(c.getChannel());
            }
            return 1;
        }
    }


    public static class 全部來這裡 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "WarpHereAll";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            boolean all = splitted.length > 1;
            for (ChannelServer cs : ChannelServer.getAllInstances()) {
                for (MapleCharacter chr : cs.getPlayerStorage().getAllCharacters()) {
                    if (chr == null || chr.isGm() || (chr.isInBlockedMap() && !all)) {
                        continue;
                    }
                    chr.changeMap(c.getPlayer().getMap(), c.getPlayer().getMap().findClosestPortal(c.getPlayer().getPosition()));
                    chr.dropMessage(5, "您已被GM傳送於此。");
                    if (chr.getClient().getChannel() != c.getChannel()) {
                        chr.changeChannel(c.getChannel());
                    }
                }
            }
            return 1;
        }
    }


    public static class 傳送ID implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "WarpId";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "用法: " + splitted[0] + " [角色ID]");
                return 0;
            }
            int victimId = Integer.parseInt(splitted[1]);
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterById(victimId);
            if (victim != null) {
                c.getPlayer().changeMap(victim.getMap(), victim.getMap().findClosestSpawnpoint(victim.getPosition()));
            } else {
                int ch = WorldFindService.getInstance().findChannel(victimId);
                if (ch <= 0) {
                    c.getPlayer().dropMessage(6, "找不到角色ID為: " + victimId + " 的訊息.");
                    return 0;
                }
                victim = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(victimId);
                if (victim != null) {
                    c.getPlayer().dropMessage(6, "正在切換頻道，請等待...");
                    if (victim.getMapId() != c.getPlayer().getMapId()) {
                        MapleMap mapp = c.getChannelServer().getMapFactory().getMap(victim.getMapId());
                        c.getPlayer().changeMap(mapp, mapp.findClosestPortal(victim.getPosition()));
                    }
                    c.getPlayer().changeChannel(ch);
                }
            }
            return 1;
        }
    }


    public static class 傳送 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "Warp";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null && c.getPlayer().getGmLevel() >= victim.getGmLevel() && !victim.inPVP() && !c.getPlayer().inPVP()) {
                if (splitted.length == 2) {
                    c.getPlayer().changeMap(victim.getMap(), victim.getMap().findClosestSpawnpoint(victim.getPosition()));
                } else {
                    MapleMap target = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(Integer.parseInt(splitted[2]));
                    if (target == null) {
                        c.getPlayer().dropMessage(6, "輸入的地圖不存在.");
                        return 0;
                    }
                    MaplePortal targetPortal = null;
                    if (splitted.length > 3) {
                        try {
                            targetPortal = target.getPortal(Integer.parseInt(splitted[3]));
                        } catch (IndexOutOfBoundsException e) {
                            // noop, assume the gm didn't know how many portals there are
                            c.getPlayer().dropMessage(5, "Invalid portal selected.");
                        } catch (NumberFormatException a) {
                            // noop, assume that the gm is drunk
                        }
                    }
                    if (targetPortal == null) {
                        targetPortal = target.getPortal(0);
                    }
                    victim.changeMap(target, targetPortal);
                }
            } else {
                try {
                    victim = c.getPlayer();
                    int ch = WorldFindService.getInstance().findChannel(splitted[1]);
                    if (ch < 0) {
                        MapleMap target = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(splitted[1]));
                        if (target == null) {
                            c.getPlayer().dropMessage(6, "輸入的地圖不存在.");
                            return 0;
                        }
                        MaplePortal targetPortal = null;
                        if (splitted.length > 2) {
                            try {
                                targetPortal = target.getPortal(Integer.parseInt(splitted[2]));
                            } catch (IndexOutOfBoundsException e) {
                                // noop, assume the gm didn't know how many portals there are
                                c.getPlayer().dropMessage(5, "Invalid portal selected.");
                            } catch (NumberFormatException a) {
                                // noop, assume that the gm is drunk
                            }
                        }
                        if (targetPortal == null) {
                            targetPortal = target.getPortal(0);
                        }
                        c.getPlayer().changeMap(target, targetPortal);
                    } else {
                        victim = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(splitted[1]);
                        c.getPlayer().dropMessage(6, "正在切換頻道，請等待...");
                        if (victim.getMapId() != c.getPlayer().getMapId()) {
                            MapleMap mapp = c.getChannelServer().getMapFactory().getMap(victim.getMapId());
                            c.getPlayer().changeMap(mapp, mapp.findClosestPortal(victim.getPosition()));
                        }
                        c.getPlayer().changeChannel(ch);
                    }
                } catch (Exception e) {
                    c.getPlayer().dropMessage(6, "出現錯誤: " + e.getMessage());
                    return 0;
                }
            }
            return 1;
        }
    }


    public static class 監禁 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(6, "用法: " + splitted[0] + " [玩家名字] [多少分鐘, 0 = forever]");
                return 0;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            int minutes = Math.max(0, Integer.parseInt(splitted[2]));
            if (victim != null && c.getPlayer().getGmLevel() >= victim.getGmLevel()) {
                MapleMap target = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(GameConstants.JAIL);
                victim.getQuestNAdd(MapleQuest.getInstance(GameConstants.JAIL_QUEST)).setCustomData(String.valueOf(minutes * 60));
                victim.changeMap(target, target.getPortal(0));
                victim.gainWarning(true);
            } else {
                c.getPlayer().dropMessage(6, "請確保要監禁的玩家處於線上狀態.");
                return 0;
            }
            return 1;
        }
    }


    public static class 說 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "Say";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length > 1) {
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                if (!c.getPlayer().isGm()) {
                    sb.append(" ");
                }
                sb.append(c.getPlayer().getName());
                sb.append("] ");
                sb.append(StringUtil.joinStringFrom(splitted, 1));
                WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.serverNotice(c.getPlayer().isGm() ? 6 : 5, sb.toString()));
            } else {
                c.getPlayer().dropMessage(6, "用法: " + splitted[0] + " <message>");
                return 0;
            }
            return 1;
        }
    }


    public static class Letter implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(6, "用法: " + splitted[0] + " <color (green/red)> <word>");
                return 0;
            }
            int start, nstart;
            if (splitted[1].equalsIgnoreCase("green")) {
                start = 3991026;
                nstart = 3990019;
            } else if (splitted[1].equalsIgnoreCase("red")) {
                start = 3991000;
                nstart = 3990009;
            } else {
                c.getPlayer().dropMessage(6, "未知的顏色!");
                return 0;
            }
            String splitString = StringUtil.joinStringFrom(splitted, 2);
            List<Integer> chars = new ArrayList<>();
            splitString = splitString.toUpperCase();
            // System.out.println(splitString);
            for (int i = 0; i < splitString.length(); i++) {
                char chr = splitString.charAt(i);
                if (chr == ' ') {
                    chars.add(-1);
                } else if ((chr) >= 'A' && (chr) <= 'Z') {
                    chars.add((int) (chr));
                } else if ((chr) >= '0' && (chr) <= ('9')) {
                    chars.add((chr) + 200);
                }
            }
            final int w = 32;
            int dStart = c.getPlayer().getPosition().x - (splitString.length() / 2 * w);
            for (Integer i : chars) {
                if (i == -1) {
                    dStart += w;
                } else if (i < 200) {
                    int val = start + i - ('A');
                    Item item = new Item(val, (byte) 0, (short) 1);
                    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), item, new Point(dStart, c.getPlayer().getPosition().y), false, false);
                    dStart += w;
                } else if (i >= 200 && i <= 300) {
                    int val = nstart + i - ('0') - 200;
                    Item item = new Item(val, (byte) 0, (short) 1);
                    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), item, new Point(dStart, c.getPlayer().getPosition().y), false, false);
                    dStart += w;
                }
            }
            return 1;
        }
    }


    public static class Find implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length == 1) {
                c.getPlayer().dropMessage(6, splitted[0] + ": <NPC> <MOB> <ITEM> <MAP> <SKILL> <QUEST>");
            } else if (splitted.length == 2) {
                c.getPlayer().dropMessage(6, "Provide something to search.");
            } else {
                String type = splitted[1];
                String search = StringUtil.joinStringFrom(splitted, 2);
                MapleData data = null;
                MapleDataProvider dataProvider = MapleDataProviderFactory.getString();
                c.getPlayer().dropMessage(6, "<<Type: " + type + " | Search: " + search + ">>");

                if (type.equalsIgnoreCase("NPC")) {
                    List<String> retNpcs = new ArrayList<>();
                    data = dataProvider.getData("Npc.img");
                    List<Pair<Integer, String>> npcPairList = new LinkedList<>();
                    for (MapleData npcIdData : data.getChildren()) {
                        npcPairList.add(new Pair<>(Integer.parseInt(npcIdData.getName()), MapleDataTool.getString(npcIdData.getChildByPath("name"), "NO-NAME")));
                    }
                    for (Pair<Integer, String> npcPair : npcPairList) {
                        if (npcPair.getRight().toLowerCase().contains(search.toLowerCase())) {
                            retNpcs.add(npcPair.getLeft() + " - " + npcPair.getRight());
                        }
                    }
                    if (retNpcs != null && retNpcs.size() > 0) {
                        for (String singleRetNpc : retNpcs) {
                            c.getPlayer().dropMessage(6, singleRetNpc);
                        }
                    } else {
                        c.getPlayer().dropMessage(6, "No NPC's Found");
                    }

                } else if (type.equalsIgnoreCase("MAP")) {
                    List<String> retMaps = new ArrayList<>();
                    data = dataProvider.getData("Map.img");
                    List<Pair<Integer, String>> mapPairList = new LinkedList<>();
                    for (MapleData mapAreaData : data.getChildren()) {
                        for (MapleData mapIdData : mapAreaData.getChildren()) {
                            mapPairList.add(new Pair<>(Integer.parseInt(mapIdData.getName()), MapleDataTool.getString(mapIdData.getChildByPath("streetName"), "NO-NAME") + " - " + MapleDataTool.getString(mapIdData.getChildByPath("mapName"), "NO-NAME")));
                        }
                    }
                    for (Pair<Integer, String> mapPair : mapPairList) {
                        if (mapPair.getRight().toLowerCase().contains(search.toLowerCase())) {
                            retMaps.add(mapPair.getLeft() + " - " + mapPair.getRight());
                        }
                    }
                    if (retMaps != null && retMaps.size() > 0) {
                        for (String singleRetMap : retMaps) {
                            c.getPlayer().dropMessage(6, singleRetMap);
                        }
                    } else {
                        c.getPlayer().dropMessage(6, "No Maps Found");
                    }
                } else if (type.equalsIgnoreCase("MOB")) {
                    List<String> retMobs = new ArrayList<>();
                    data = dataProvider.getData("Mob.img");
                    List<Pair<Integer, String>> mobPairList = new LinkedList<>();
                    for (MapleData mobIdData : data.getChildren()) {
                        mobPairList.add(new Pair<>(Integer.parseInt(mobIdData.getName()), MapleDataTool.getString(mobIdData.getChildByPath("name"), "NO-NAME")));
                    }
                    for (Pair<Integer, String> mobPair : mobPairList) {
                        if (mobPair.getRight().toLowerCase().contains(search.toLowerCase())) {
                            retMobs.add(mobPair.getLeft() + " - " + mobPair.getRight());
                        }
                    }
                    if (retMobs != null && retMobs.size() > 0) {
                        for (String singleRetMob : retMobs) {
                            c.getPlayer().dropMessage(6, singleRetMob);
                        }
                    } else {
                        c.getPlayer().dropMessage(6, "No Mobs Found");
                    }

                } else if (type.equalsIgnoreCase("ITEM")) {
                    List<String> retItems = new ArrayList<>();
                    for (Entry<Integer, String> entry : MapleItemInformationProvider.getInstance().getAllItemNames().entrySet()) {
                        if (entry.getValue().toLowerCase().contains(search.toLowerCase())) {
                            retItems.add(entry.getKey() + " - " + entry.getValue());
                        }
                    }
                    if (retItems != null && retItems.size() > 0) {
                        for (String singleRetItem : retItems) {
                            c.getPlayer().dropMessage(6, singleRetItem);
                        }
                    } else {
                        c.getPlayer().dropMessage(6, "No Items Found");
                    }
                } else if (type.equalsIgnoreCase("QUEST")) {
                    List<String> retItems = new ArrayList<>();
                    for (MapleQuest itemPair : MapleQuest.getAllInstances()) {
                        if (itemPair.getName().length() > 0 && itemPair.getName().toLowerCase().contains(search.toLowerCase())) {
                            retItems.add(itemPair.getId() + " - " + itemPair.getName());
                        }
                    }
                    if (retItems != null && retItems.size() > 0) {
                        for (String singleRetItem : retItems) {
                            c.getPlayer().dropMessage(6, singleRetItem);
                        }
                    } else {
                        c.getPlayer().dropMessage(6, "No Quests Found");
                    }
                } else if (type.equalsIgnoreCase("SKILL")) {
                    List<String> retSkills = new ArrayList<>();
                    for (Entry<Integer, String> skil : SkillFactory.getAllSkills().entrySet()) {
                        if (skil.getValue() != null && skil.getValue().toLowerCase().contains(search.toLowerCase())) {
                            retSkills.add(skil.getKey() + " - " + skil.getValue());
                        }
                    }
                    if (!retSkills.isEmpty() && retSkills.size() > 0) {
                        for (String singleRetSkill : retSkills) {
                            c.getPlayer().dropMessage(6, singleRetSkill);
                        }
                    } else {
                        c.getPlayer().dropMessage(6, "No skills Found");
                    }
                } else {
                    c.getPlayer().dropMessage(6, "Sorry, that search call is unavailable");
                }
            }
            return 0;
        }
    }


    public static class ID extends Find {
    }


    public static class LookUp extends Find {
    }


    public static class Search extends Find {
    }


    public static class WarpMap implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            try {
                MapleMap target = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(splitted[1]));
                if (target == null) {
                    c.getPlayer().dropMessage(6, "輸入的地圖不存在.");
                    return 0;
                }
                MapleMap from = c.getPlayer().getMap();
                for (MapleCharacter chr : from.getCharacters()) {
                    chr.changeMap(target, target.getPortal(0));
                }
            } catch (Exception e) {
                c.getPlayer().dropMessage(5, "錯誤: " + e.getMessage());
                return 0; //assume drunk GM
            }
            return 1;
        }
    }


    public static class 清怪 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "KillAll";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleMap map = c.getPlayer().getMap();
            double range = Double.POSITIVE_INFINITY;
            if (splitted.length > 1) {
                int irange = Integer.parseInt(splitted[1]);
                if (splitted.length <= 2) {
                    range = irange;
                } else {
                    map = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(splitted[2]));
                }
            }
            if (map == null) {
                c.getPlayer().dropMessage(6, "輸入的地圖不存在.");
                return 0;
            }
            MapleMonster mob;
            for (MapleMapObject monstermo : map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Collections.singletonList(MapleMapObjectType.MONSTER))) {
                mob = (MapleMonster) monstermo;
                if (!mob.getStats().isBoss() || mob.getStats().isPartyBonus() || c.getPlayer().isGm()) {
                    map.killMonster(mob, c.getPlayer(), false, false, (byte) 1, 0);
                }
            }
            return 1;
        }
    }


    public static class 查看封號 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "[用法] " + splitted[0] + " <角色名字>");
                return 0;
            }
            String msg = MapleClient.getAccInfoByName(splitted[1], c.getPlayer().isAdmin());
            if (msg != null) {
                c.getPlayer().dropMessage(6, msg);
            } else {
                c.getPlayer().dropMessage(6, "輸入的角色名字錯誤，無法找到訊息.");
            }
            return 1;
        }
    }


    public static class 查看帳號 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "[用法] " + splitted[0] + " <玩家帳號>");
                return 0;
            }
            String msg = MapleClient.getAccInfo(splitted[1], c.getPlayer().isAdmin());
            if (msg != null) {
                c.getPlayer().dropMessage(6, msg);
            } else {
                c.getPlayer().dropMessage(6, "輸入的帳號錯誤，無法找到訊息.");
            }
            return 1;
        }
    }


    public static class 測謊機 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <角色名字>");
                return 0;
            }
            MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (chr != null && c.getPlayer().getGmLevel() >= chr.getGmLevel()) {
                if (chr.getMapId() == GameConstants.JAIL) {
                    c.getPlayer().dropMessage(6, "玩家 " + chr.getName() + " 在監獄地圖無法對其使用.");
                } else if (!MapleAntiMacro.startAnti(c.getPlayer(), chr, MapleAntiMacro.GM_SKILL_ANTI, true)) {
                    c.getPlayer().dropMessage(6, "對玩家 " + chr.getName() + " 使用測謊機失敗.");
                } else {
                    c.getPlayer().dropMessage(6, "已成功對玩家 " + chr.getName() + " 使用測謊機.");
                }
            } else {
                c.getPlayer().dropMessage(6, "請確保要測謊的玩家處於線上狀態.");
                return 0;
            }
            return 1;
        }
    }

    /*
     * 解除封號
     */


    public static class HellBan extends 封鎖帳號 {

        public HellBan() {
            hellban = true;
        }

        @Override
        public String getShortCommand() {
            return null;
        }
    }


    public static class UnHellBan extends 解鎖帳號 {

        public UnHellBan() {
            hellban = true;
        }

        @Override
        public String getShortCommand() {
            return null;
        }
    }


    public static class 解鎖帳號 implements CommandExecute {

        protected boolean hellban = false;

        @Override
        public String getShortCommand() {
            return "UnBan";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "[用法] " + splitted[0] + " <IGN>");
                return 0;
            }
            byte ret;
            if (hellban) {
                ret = MapleClient.unHellban(splitted[1]);
            } else {
                ret = MapleClient.unban(splitted[1]);
            }
            if (ret == -2) {
                c.getPlayer().dropMessage(6, "[" + splitted[0].substring(1) + "] 資料庫查詢出錯.");
                return 0;
            } else if (ret == -1) {
                c.getPlayer().dropMessage(6, "[" + splitted[0].substring(1) + "] 角色 " + splitted[1] + " 不存在.");
                return 0;
            } else {
                c.getPlayer().dropMessage(6, "[" + splitted[0].substring(1) + "] 已經成功將玩家 " + splitted[1] + " 解除封停!");
                log.info("[管理員指令]   " + c.getPlayer().getName() + " 將玩家 " + splitted[1] + " 解除封號.");
            }
            byte ret_ = MapleClient.unbanIPMacs(splitted[1]);
            if (ret_ == -2) {
                c.getPlayer().dropMessage(6, "[UnbanIP] 資料庫查詢出錯.");
            } else if (ret_ == -1) {
                c.getPlayer().dropMessage(6, "[UnbanIP] 輸入的角色不存在.");
            } else if (ret_ == 0) {
                c.getPlayer().dropMessage(6, "[UnbanIP] No IP or Mac with that character exists!");
            } else if (ret_ == 1) {
                c.getPlayer().dropMessage(6, "[UnbanIP] IP/Mac -- one of them was found and unbanned.");
            } else if (ret_ == 2) {
                c.getPlayer().dropMessage(6, "[UnbanIP] Both IP and Macs were unbanned.");
            }
            return ret_ > 0 ? 1 : 0;
        }
    }


    public static class UnbanIP implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "[用法] " + splitted[0] + " <IGN>");
                return 0;
            }
            byte ret = MapleClient.unbanIPMacs(splitted[1]);
            if (ret == -2) {
                c.getPlayer().dropMessage(6, "[UnbanIP] 資料庫查詢出錯.");
            } else if (ret == -1) {
                c.getPlayer().dropMessage(6, "[UnbanIP] 輸入的角色不存在.");
            } else if (ret == 0) {
                c.getPlayer().dropMessage(6, "[UnbanIP] No IP or Mac with that character exists!");
            } else if (ret == 1) {
                c.getPlayer().dropMessage(6, "[UnbanIP] IP/Mac -- one of them was found and unbanned.");
            } else if (ret == 2) {
                c.getPlayer().dropMessage(6, "[UnbanIP] Both IP and Macs were unbanned.");
            }
            if (ret > 0) {
                return 1;
            }
            return 0;
        }
    }

    public static class 檢索指令 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "FindCommand";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <關鍵字詞>");
                return 0;
            }
            c.getPlayer().dropSpouseMessage(UserChatMessageType.頻道喇叭, "檢索指令(關鍵字詞:" + splitted[1] + ")結果如下:");
            for (int i = 0; i <= PlayerRank.MVP紅鑽.getLevel(); i++) {
                CommandProcessor.dropCommandList(c, i, splitted[1]);
            }
            for (int i = 0; i <= c.getPlayer().getGmLevel(); i++) {
                CommandProcessor.dropCommandList(c, i, splitted[1]);
            }
            return 1;
        }
    }

    public static class 召喚符文輪 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "SpawnRune";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            int type;
            if (splitted.length < 2 || (type = Integer.parseInt(splitted[1])) < 0 || type > 10) {
                c.getPlayer().dropMessage(6, splitted[0] + " <地圖輪種類:0-破滅之輪/1-打雷之輪/2-地震之輪/3-黑暗之輪/4-超越之輪/5-淨化之輪/6-光束之輪/7-轉移之輪>");
                return 0;
            }
            if (c.getPlayer().getMap().getRunesSize() >= 1) {
                c.getPlayer().dropMessage(6, "地圖上已存在符文輪, 無法重複召喚");
                return 0;
            }
            c.getPlayer().dispelEffect(SecondaryStat.RuneStoneNoTime);
            MapleRuneStone runeStone = new MapleRuneStone(type);
            runeStone.setPosition(c.getPlayer().getPosition());
            c.getPlayer().getMap().spawnRune(runeStone);
            return 1;
        }
    }
}
