package Net.server.commands;

import Client.*;
import Client.anticheat.CheatingOffense;
import Client.inventory.*;
import Client.skills.Skill;
import Client.skills.SkillFactory;
import Config.configs.ServerConfig;
import Config.constants.ItemConstants;
import Config.constants.enums.UserChatMessageType;
import Database.DatabaseLoader.DatabaseConnectionEx;
import Net.server.*;
import Net.server.life.*;
import Net.server.maps.*;
import Net.server.quest.MapleQuest;
import Packet.MaplePacketCreator;
import Packet.MobPacket;
import Packet.NPCPacket;
import Server.channel.ChannelServer;
import Server.world.World;
import Server.world.WorldBroadcastService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.DateUtil;
import tools.HexTool;
import tools.Pair;
import tools.StringUtil;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;

/**
 * @author Emilyx3
 */
@SuppressWarnings("unused")


public class SuperGMCommand {

    private static final Logger log = LoggerFactory.getLogger(SuperGMCommand.class);

    /**
     * @return
     */
    public static PlayerRank getPlayerLevelRequired() {
        return PlayerRank.超級管理員;
    }

    public static class 給技能 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "GiveSkill";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 4) {
                c.getPlayer().dropMessage(6, "用法: " + splitted[0] + " <玩家名字> <技能ID> <技能等級> <技能最大等級> [最後一項可以隨便填寫]");
                return 0;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            Skill skill = SkillFactory.getSkill(Integer.parseInt(splitted[2]));
            byte level = (byte) CommandProcessorUtil.getOptionalIntArg(splitted, 3, 1);
            byte masterlevel = (byte) CommandProcessorUtil.getOptionalIntArg(splitted, 4, 1);
            if (level > skill.getMaxLevel()) {
                level = (byte) skill.getMaxLevel();
            }
            if (masterlevel > skill.getMaxLevel()) {
                masterlevel = (byte) skill.getMaxLevel();
            }
            victim.changeSingleSkillLevel(skill, level, masterlevel);
            c.dropMessage("技能添加成功。");
            return 1;
        }
    }


    public static class 解鎖道具 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "UnlockInv";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            Map<Item, MapleInventoryType> eqs = new HashMap<>();
            boolean add = false;
            if (splitted.length < 2 || splitted[1].equals("all")) {
                for (MapleInventoryType type : MapleInventoryType.values()) {
                    for (Item item : c.getPlayer().getInventory(type)) {
                        if (ItemAttribute.Seal.check(item.getAttribute())) {
                            item.removeAttribute(ItemAttribute.Seal.getValue());
                            add = true;
                            //c.announce(MaplePacketCreator.updateSpecialItemUse(item, type.getType()));
                        }
                        if (ItemAttribute.TradeBlock.check(item.getAttribute())) {
                            item.removeAttribute(ItemAttribute.TradeBlock.getValue());
                            add = true;
                            //c.announce(MaplePacketCreator.updateSpecialItemUse(item, type.getType()));
                        }
                        if (add) {
                            eqs.put(item, type);
                        }
                        add = false;
                    }
                }
            } else if (splitted[1].equals("eqp")) {
                for (Item item : c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).newList()) {
                    if (ItemAttribute.Seal.check(item.getAttribute())) {
                        item.removeAttribute(ItemAttribute.Seal.getValue());
                        add = true;
                        //c.announce(MaplePacketCreator.updateSpecialItemUse(item, type.getType()));
                    }
                    if (ItemAttribute.TradeBlock.check(item.getAttribute())) {
                        item.removeAttribute(ItemAttribute.TradeBlock.getValue());
                        add = true;
                        //c.announce(MaplePacketCreator.updateSpecialItemUse(item, type.getType()));
                    }
                    if (add) {
                        eqs.put(item, MapleInventoryType.EQUIP);
                    }
                    add = false;
                }
            } else if (splitted[1].equals("eq")) {
                for (Item item : c.getPlayer().getInventory(MapleInventoryType.EQUIP)) {
                    if (ItemAttribute.Seal.check(item.getAttribute())) {
                        item.removeAttribute(ItemAttribute.Seal.getValue());
                        add = true;
                        //c.announce(MaplePacketCreator.updateSpecialItemUse(item, type.getType()));
                    }
                    if (ItemAttribute.TradeBlock.check(item.getAttribute())) {
                        item.removeAttribute(ItemAttribute.TradeBlock.getValue());
                        add = true;
                        //c.announce(MaplePacketCreator.updateSpecialItemUse(item, type.getType()));
                    }
                    if (add) {
                        eqs.put(item, MapleInventoryType.EQUIP);
                    }
                    add = false;
                }
            } else if (splitted[1].equals("u")) {
                for (Item item : c.getPlayer().getInventory(MapleInventoryType.USE)) {
                    if (ItemAttribute.Seal.check(item.getAttribute())) {
                        item.removeAttribute(ItemAttribute.Seal.getValue());
                        add = true;
                        //c.announce(MaplePacketCreator.updateSpecialItemUse(item, type.getType()));
                    }
                    if (ItemAttribute.TradeBlock.check(item.getAttribute())) {
                        item.removeAttribute(ItemAttribute.TradeBlock.getValue());
                        add = true;
                        //c.announce(MaplePacketCreator.updateSpecialItemUse(item, type.getType()));
                    }
                    if (add) {
                        eqs.put(item, MapleInventoryType.USE);
                    }
                    add = false;
                }
            } else if (splitted[1].equals("s")) {
                for (Item item : c.getPlayer().getInventory(MapleInventoryType.SETUP)) {
                    if (ItemAttribute.Seal.check(item.getAttribute())) {
                        item.removeAttribute(ItemAttribute.Seal.getValue());
                        add = true;
                        //c.announce(MaplePacketCreator.updateSpecialItemUse(item, type.getType()));
                    }
                    if (ItemAttribute.TradeBlock.check(item.getAttribute())) {
                        item.removeAttribute(ItemAttribute.TradeBlock.getValue());
                        add = true;
                        //c.announce(MaplePacketCreator.updateSpecialItemUse(item, type.getType()));
                    }
                    if (add) {
                        eqs.put(item, MapleInventoryType.SETUP);
                    }
                    add = false;
                }
            } else if (splitted[1].equals("e")) {
                for (Item item : c.getPlayer().getInventory(MapleInventoryType.ETC)) {
                    if (ItemAttribute.Seal.check(item.getAttribute())) {
                        item.removeAttribute(ItemAttribute.Seal.getValue());
                        add = true;
                        //c.announce(MaplePacketCreator.updateSpecialItemUse(item, type.getType()));
                    }
                    if (ItemAttribute.TradeBlock.check(item.getAttribute())) {
                        item.removeAttribute(ItemAttribute.TradeBlock.getValue());
                        add = true;
                        //c.announce(MaplePacketCreator.updateSpecialItemUse(item, type.getType()));
                    }
                    if (add) {
                        eqs.put(item, MapleInventoryType.ETC);
                    }
                    add = false;
                }
            } else if (splitted[1].equals("c")) {
                for (Item item : c.getPlayer().getInventory(MapleInventoryType.CASH)) {
                    if (ItemAttribute.Seal.check(item.getAttribute())) {
                        item.removeAttribute(ItemAttribute.Seal.getValue());
                        add = true;
                        //c.announce(MaplePacketCreator.updateSpecialItemUse(item, type.getType()));
                    }
                    if (ItemAttribute.TradeBlock.check(item.getAttribute())) {
                        item.removeAttribute(ItemAttribute.TradeBlock.getValue());
                        add = true;
                        //c.announce(MaplePacketCreator.updateSpecialItemUse(item, type.getType()));
                    }
                    if (add) {
                        eqs.put(item, MapleInventoryType.CASH);
                    }
                    add = false;
                }
            } else if (splitted[1].equals("d")) {
                for (Item item : c.getPlayer().getInventory(MapleInventoryType.DECORATION)) {
                    if (ItemAttribute.Seal.check(item.getAttribute())) {
                        item.removeAttribute(ItemAttribute.Seal.getValue());
                        add = true;
                        //c.announce(MaplePacketCreator.updateSpecialItemUse(item, type.getType()));
                    }
                    if (ItemAttribute.TradeBlock.check(item.getAttribute())) {
                        item.removeAttribute(ItemAttribute.TradeBlock.getValue());
                        add = true;
                        //c.announce(MaplePacketCreator.updateSpecialItemUse(item, type.getType()));
                    }
                    if (add) {
                        eqs.put(item, MapleInventoryType.DECORATION);
                    }
                    add = false;
                }
            } else {
                c.getPlayer().dropMessage(6, "[all/eqp/eq/u/s/e/c/d]");
            }

            for (Entry<Item, MapleInventoryType> eq : eqs.entrySet()) {
                c.getPlayer().forceUpdateItem(eq.getKey().copy());
            }
            return 1;
        }
    }

    public static class 丟 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "Drop";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            int itemId = Integer.parseInt(splitted[1]);
            short quantity = (short) CommandProcessorUtil.getOptionalIntArg(splitted, 2, 1);
            if (quantity < 0) {
                quantity = Short.MAX_VALUE;
            }
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            if (!ii.itemExists(itemId)) {
                c.getPlayer().dropMessage(5, itemId + " 不存在.");
            } else {
                Item toDrop;
                if (ItemConstants.getInventoryType(itemId, false) == MapleInventoryType.EQUIP) {
                    toDrop = ii.randomizeStats(ii.getEquipById(itemId));
                } else {
                    toDrop = new Item(itemId, (byte) 0, ItemConstants.類型.寵物(itemId) ? 1 : quantity, 0);
                }
                if (!c.getPlayer().isAdmin()) {
                    toDrop.setGMLog(c.getPlayer().getName() + " 使用 !" + this.getClass().getSimpleName());
                    toDrop.setOwner(c.getPlayer().getName());
                }
                c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), toDrop, c.getPlayer().getPosition(), true, true);
                c.getPlayer().dropMessage(6, "使用指令成功，丟出道具: " + toDrop.getItemId() + " - " + ii.getName(toDrop.getItemId()) + " 數量: " + toDrop.getQuantity());
                log.info("[管理員指令]   " + c.getPlayer().getName() + " 丟道具: " + toDrop.getItemId() + " 數量: " + toDrop.getQuantity() + " 名稱: " + ii.getName(toDrop.getItemId()));
            }
            return 1;
        }
    }

    public static class 結婚 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "Marry";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(6, "用法: " + splitted[0] + " <角色名> <戒指ID>");
                return 0;
            }
            int itemId = Integer.parseInt(splitted[2]);
            if (!ItemConstants.類型.特效裝備(itemId)) {
                c.getPlayer().dropMessage(6, "Invalid itemID.");
            } else {
                MapleCharacter fff = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
                if (fff == null) {
                    c.getPlayer().dropMessage(6, "玩家必須在線上.");
                } else {
                    int[] ringID = {MapleInventoryIdentifier.getInstance(), MapleInventoryIdentifier.getInstance()};
                    try {
                        MapleCharacter[] chrz = {fff, c.getPlayer()};
                        for (int i = 0; i < chrz.length; i++) {
                            Equip eq = MapleItemInformationProvider.getInstance().getEquipById(itemId, ringID[i]);
                            if (eq == null) {
                                c.getPlayer().dropMessage(6, "Invalid itemID.");
                                return 0;
                            }
                            MapleInventoryManipulator.addbyItem(chrz[i].getClient(), eq.copy());
                            chrz[i].dropMessage(6, "結婚成功 " + chrz[i == 0 ? 1 : 0].getName());
                        }
                        MapleRing.addToDB(itemId, c.getPlayer(), fff.getName(), fff.getId(), ringID);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            return 1;
        }
    }

    public static class useSkill implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "skill";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            Skill skill = SkillFactory.getSkill(Integer.parseInt(splitted[1]));
            if (skill != null) {
                c.getPlayer().getSkillEffect(Integer.parseInt(splitted[1])).applyTo(c.getPlayer());
                c.getPlayer().dropSpouseMessage(UserChatMessageType.暗黃, "[SKILL COMMAND] useSkill :" + Integer.parseInt(splitted[1]));
            } else {
                c.getPlayer().dropSpouseMessage(UserChatMessageType.暗黃, "[SKILL COMMAND] error :" + Integer.parseInt(splitted[1] + "不存在。"));
            }
            return 1;
        }
    }


    public static class 給點數 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "GivePoint";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(6, "需要輸入玩家的名字和數量.");
                return 0;
            }
            MapleCharacter chrs = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (chrs == null) {
                c.getPlayer().dropMessage(6, "錯誤: 要操作的玩家必須是在同一頻道或者玩家不存在.");
            } else {
                chrs.setPoints(chrs.getPoints() + Integer.parseInt(splitted[2]));
                c.getPlayer().dropMessage(6, splitted[1] + " 有 " + chrs.getPoints() + " 點數，在給予 " + splitted[2] + "點之後。");
            }
            return 1;
        }
    }

    public static class 給V點 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "GiveVPoint";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(6, "需要輸入玩家的名字和數量.");
                return 0;
            }
            MapleCharacter chrs = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (chrs == null) {
                c.getPlayer().dropMessage(6, "錯誤: 要操作的玩家必須是在同一頻道或者玩家不存在.");
            } else {
                chrs.setVPoints(chrs.getVPoints() + Integer.parseInt(splitted[2]));
                c.getPlayer().dropMessage(6, splitted[1] + " 有 " + chrs.getVPoints() + " V點, 在給予 " + splitted[2] + "點之後。");
            }
            return 1;
        }
    }

    public static class 地圖廣 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "SpeakMap";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            for (MapleCharacter victim : c.getPlayer().getMap().getCharacters()) {
                if (victim.getId() != c.getPlayer().getId()) {
                    victim.getMap().broadcastMessage(MaplePacketCreator.getChatText(victim.getId(), StringUtil.joinStringFrom(splitted, 1), victim.getName(), victim.isGm(), 0, true, -1));
                }
            }
            return 1;
        }
    }

    public static class 頻道廣 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "SpeakChn";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            for (MapleCharacter victim : c.getChannelServer().getPlayerStorage().getAllCharacters()) {
                if (victim.getId() != c.getPlayer().getId()) {
                    victim.getMap().broadcastMessage(MaplePacketCreator.getChatText(victim.getId(), StringUtil.joinStringFrom(splitted, 1), victim.getName(), victim.isGm(), 0, true, -1));
                }
            }
            return 1;
        }
    }

    public static class 世界廣 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "SpeakWorld";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter victim : cserv.getPlayerStorage().getAllCharacters()) {
                    if (victim.getId() != c.getPlayer().getId()) {
                        victim.getMap().broadcastMessage(MaplePacketCreator.getChatText(victim.getId(), StringUtil.joinStringFrom(splitted, 1), victim.getName(), victim.isGm(), 0, true, -1));
                    }
                }
            }
            return 1;
        }
    }

    public static class 監視 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "monitor";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (target != null) {
                if (target.getClient().isMonitored()) {
                    target.getClient().setMonitored(false);
                    c.getPlayer().dropMessage(5, "停止對 " + target.getName() + " 的監視.");
                } else {
                    target.getClient().setMonitored(true);
                    c.getPlayer().dropMessage(5, "開始監視 " + target.getName() + " 的訊息.");
                }
            } else {
                c.getPlayer().dropMessage(5, "當前頻道中找不到此玩家");
                return 0;
            }
            return 1;
        }
    }

    public static class 重置玩家任務 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "ResetOther";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleQuest.getInstance(Integer.parseInt(splitted[2])).forfeit(c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]));
            return 1;
        }
    }

    public static class 開始玩家任務 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "FStartOther";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleQuest.getInstance(Integer.parseInt(splitted[2])).forceStart(c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]), Integer.parseInt(splitted[3]), splitted.length > 4 ? splitted[4] : null);
            return 1;
        }
    }

    public static class 完成玩家任務 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "FCompleteOther";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleQuest.getInstance(Integer.parseInt(splitted[2])).forceComplete(c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]), Integer.parseInt(splitted[3]));
            return 1;
        }
    }

    public static class 線程 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "Threads";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            Thread[] threads = new Thread[Thread.activeCount()];
            Thread.enumerate(threads);
            String filter = "";
            if (splitted.length > 1) {
                filter = splitted[1];
            }
            for (int i = 0; i < threads.length; i++) {
                String tstring = threads[i].toString();
                if (tstring.toLowerCase().contains(filter.toLowerCase())) {
                    c.getPlayer().dropMessage(6, i + ": " + tstring);
                }
            }
            return 1;
        }
    }

    public static class ShowTrace implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                throw new IllegalArgumentException();
            }
            Thread[] threads = new Thread[Thread.activeCount()];
            Thread.enumerate(threads);
            Thread t = threads[Integer.parseInt(splitted[1])];
            c.getPlayer().dropMessage(6, t.toString() + ":");
            for (StackTraceElement elem : t.getStackTrace()) {
                c.getPlayer().dropMessage(6, elem.toString());
            }
            return 1;
        }
    }

    public static class 懲罰開關 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "ToggleOffense";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            try {
                CheatingOffense co = CheatingOffense.valueOf(splitted[1]);
                co.setEnabled(!co.isEnabled());
            } catch (IllegalArgumentException iae) {
                c.getPlayer().dropMessage(6, "Offense " + splitted[1] + " not found");
            }
            return 1;
        }
    }

    public static class 喇叭開關 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "TMegaphone";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            World.toggleMegaphoneMuteState();
            c.getPlayer().dropMessage(6, "Megaphone state : " + (c.getChannelServer().getMegaphoneMuteState() ? "Enabled" : "Disabled"));
            return 1;
        }
    }

    public static class 召喚反應堆 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "SReactor";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleReactor reactor = new MapleReactor(MapleReactorFactory.getReactor(Integer.parseInt(splitted[1])), Integer.parseInt(splitted[1]));
            reactor.setDelay(-1);
            c.getPlayer().getMap().spawnReactorOnGroundBelow(reactor, new Point(c.getPlayer().getPosition().x, c.getPlayer().getPosition().y - 20));
            return 1;
        }
    }

    public static class 傷OID怪 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "HitMonsterByOID";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleMap map = c.getPlayer().getMap();
            int targetId = Integer.parseInt(splitted[1]);
            long damage = Long.parseLong(splitted[2]);
            MapleMonster monster = map.getMonsterByOid(targetId);
            if (monster != null) {
                map.broadcastMessage(MobPacket.damageMonster(targetId, damage));
                monster.damage(c.getPlayer(), 0, damage, false);
            }
            return 1;
        }
    }

    public static class 傷全圖怪 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "HitAll";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleMap map = c.getPlayer().getMap();
            double range = Double.POSITIVE_INFINITY;
            if (splitted.length > 2) {
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
            long damage = Long.parseLong(splitted[1]);
            MapleMonster mob;
            for (MapleMapObject monstermo : map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Collections.singletonList(MapleMapObjectType.MONSTER))) {
                mob = (MapleMonster) monstermo;
                map.broadcastMessage(MobPacket.damageMonster(mob.getObjectId(), damage));
                mob.damage(c.getPlayer(), 0, damage, false);
            }
            return 1;
        }
    }

    public static class 傷怪 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "HitMonster";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleMap map = c.getPlayer().getMap();
            double range = Double.POSITIVE_INFINITY;
            long damage = Long.parseLong(splitted[1]);
            MapleMonster mob;
            for (MapleMapObject monstermo : map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Collections.singletonList(MapleMapObjectType.MONSTER))) {
                mob = (MapleMonster) monstermo;
                if (mob.getId() == Integer.parseInt(splitted[2])) {
                    map.broadcastMessage(MobPacket.damageMonster(mob.getObjectId(), damage));
                    mob.damage(c.getPlayer(), 0, damage, false);
                }
            }
            return 1;
        }
    }

    public static class 殺怪 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "KillMonster";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleMap map = c.getPlayer().getMap();
            double range = Double.POSITIVE_INFINITY;
            MapleMonster mob;
            for (MapleMapObject monstermo : map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Collections.singletonList(MapleMapObjectType.MONSTER))) {
                mob = (MapleMonster) monstermo;
                if (mob.getId() == Integer.parseInt(splitted[1])) {
                    mob.damage(c.getPlayer(), 0, mob.getHp(), false);
                }
            }
            return 1;
        }
    }

    public static class 清怪掉寶 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "KillAllDrops";
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
                c.getPlayer().dropMessage(6, "輸入的地圖不存在.");
                return 0;
            }
            MapleMonster mob;
            for (MapleMapObject monstermo : map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Collections.singletonList(MapleMapObjectType.MONSTER))) {
                mob = (MapleMonster) monstermo;
                map.killMonster(mob, c.getPlayer(), true, false, (byte) 1, 0);
            }
            return 1;
        }
    }

    public static class 清怪得經驗 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "KillAllExp";
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
                c.getPlayer().dropMessage(6, "輸入的地圖不存在.");
                return 0;
            }
            MapleMonster mob;
            for (MapleMapObject monstermo : map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Collections.singletonList(MapleMapObjectType.MONSTER))) {
                mob = (MapleMonster) monstermo;
                mob.damage(c.getPlayer(), 0, mob.getHp(), false);
            }
            return 1;
        }
    }


    public static class 設置臨時NPC implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "NPC";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            int npcId = Integer.parseInt(splitted[1]);
            MapleNPC npc = MapleLifeFactory.getNPC(npcId, c.getPlayer().getMapId());
            if (npc != null && !npc.getName().equals("MISSINGNO")) {
                npc.setPosition(c.getPlayer().getPosition());
                npc.setCy(c.getPlayer().getPosition().y);
                npc.setRx0(c.getPlayer().getPosition().x + 50);
                npc.setRx1(c.getPlayer().getPosition().x - 50);
                npc.setCurrentFh(c.getPlayer().getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId());
                npc.setCustom(true);
                c.getPlayer().getMap().addMapObject(npc);
                c.getPlayer().getMap().broadcastMessage(NPCPacket.spawnNPC(npc));
            } else {
                c.getPlayer().dropMessage(6, "你應該輸入一個正確的 Npc-Id");
                return 0;
            }
            return 1;
        }
    }

    public static class 添加NPC implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "MakeNpc";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "用法: " + splitted[0] + " <NpcID>");
                return 0;
            }
            int npcId = Integer.parseInt(splitted[1]);
            MapleNPC npc = MapleLifeFactory.getNPC(npcId, c.getPlayer().getMapId());
            if (npc != null && !npc.getName().equals("MISSINGNO")) {
                int xpos = c.getPlayer().getPosition().x;
                int ypos = c.getPlayer().getPosition().y;
                int fh = c.getPlayer().getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId();
                npc.setPosition(c.getPlayer().getPosition());
                npc.setCy(ypos);
                npc.setRx0(xpos + 50);
                npc.setRx1(xpos - 50);
                npc.setCurrentFh(fh);
                npc.setCustom(true);
                try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO spawns ( idd, f, fh, cy, rx0, rx1, type, x, y, mid ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
                    ps.setInt(1, npcId);
                    ps.setInt(2, 0);
                    ps.setInt(3, fh);
                    ps.setInt(4, ypos);
                    ps.setInt(4, ypos);
                    ps.setInt(5, xpos + 50);
                    ps.setInt(6, xpos - 50);
                    ps.setString(7, "n");
                    ps.setInt(8, xpos);
                    ps.setInt(9, ypos);
                    ps.setInt(10, c.getPlayer().getMapId());
                    ps.executeUpdate();
                } catch (SQLException e) {
                    c.getPlayer().dropMessage(6, "儲存Npc訊息到資料庫中出現錯誤.");
                }
                c.getPlayer().getMap().addMapObject(npc);
                c.getPlayer().getMap().broadcastMessage(NPCPacket.spawnNPC(npc));

            } else {
                c.getPlayer().dropMessage(6, "你應該輸入一個正確的 Npc-Id.");
                return 0;
            }
            return 1;
        }
    }

    public static class setGmLevel implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "setGmLevel";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "使用方式: " + splitted[0] + " <GmLevel>");
                return 0;
            }
            int gmLevel;
            try {
                gmLevel = Integer.parseInt(splitted[1]);
            } catch (NumberFormatException e) {
                c.getPlayer().dropMessage(6, "GM 欄位必須是0~6.");
                return 0;
            }

            int accId = c.getPlayer().getAccountID();
            try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
                PreparedStatement ps = con.prepareStatement("UPDATE accounts SET gm = ? WHERE id = ?");
                ps.setInt(1, gmLevel);
                ps.setInt(2, accId);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    c.getPlayer().dropMessage(6, "成功更改 GM 等級為: " + gmLevel);
                } else {
                    c.getPlayer().dropMessage(6, "未能找到對應的帳戶 ID.");
                }
            } catch (SQLException e) {
                c.getPlayer().dropMessage(6, "更新 GM 欄位時出現錯誤.");
            }

            return 1;
        }
    }


    public static class 添加怪物 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "MakeMob";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "用法: " + splitted[0] + " <怪物ID> <刷新時間 預設按秒計算>");
                return 0;
            }
            int mobid = Integer.parseInt(splitted[1]);
            int mobTime = Integer.parseInt(splitted[2]);
            if (splitted[2] == null) {
                mobTime = 1;
            }
            MapleMonster mob = MapleLifeFactory.getMonster(mobid);
            if (mob != null) {
                int xpos = c.getPlayer().getPosition().x;
                int ypos = c.getPlayer().getPosition().y;
                int fh = c.getPlayer().getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId();
                mob.setPosition(c.getPlayer().getPosition());
                mob.setCy(ypos);
                mob.setRx0(xpos + 50);
                mob.setRx1(xpos - 50);
                mob.setCurrentFh(fh);
                try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO spawns ( idd, f, fh, cy, rx0, rx1, type, x, y, mid, mobtime ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
                    ps.setInt(1, mobid);
                    ps.setInt(2, 0);
                    ps.setInt(3, fh);
                    ps.setInt(4, ypos);
                    ps.setInt(5, xpos + 50);
                    ps.setInt(6, xpos - 50);
                    ps.setString(7, "m");
                    ps.setInt(8, xpos);
                    ps.setInt(9, ypos);
                    ps.setInt(10, c.getPlayer().getMapId());
                    ps.setInt(11, mobTime);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    c.getPlayer().dropMessage(6, "儲存Mob訊息到資料庫中出現錯誤.");
                }
                c.getPlayer().getMap().addMonsterSpawn(mob, mobTime, (byte) -1, null);
            } else {
                c.getPlayer().dropMessage(6, "你應該輸入一個正確的 Mob-Id.");
                return 0;
            }
            return 1;
        }
    }

    public static class 添加角色NPC implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "MakePNPC";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            try {
                c.getPlayer().dropMessage(6, "Making playerNPC...");
                MapleCharacter chhr = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
                if (chhr == null) {
                    c.getPlayer().dropMessage(6, splitted[1] + " is not online");
                    return 0;
                }
                PlayerNPC npc = new PlayerNPC(chhr, Integer.parseInt(splitted[2]), c.getPlayer().getMap(), c.getPlayer());
                npc.addToServer();
                c.getPlayer().dropMessage(6, "Done");
            } catch (Exception e) {
                c.getPlayer().dropMessage(6, "NPC failed... : " + e.getMessage());
            }
            return 1;
        }
    }

    public static class 添加離線角色NPC implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "MakeOfflineP";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            try {
                c.getPlayer().dropMessage(6, "Making playerNPC...");
                MapleClient cs = new MapleClient(null, null, null); //new MockIOSession()
                MapleCharacter chhr = MapleCharacter.loadCharFromDB(MapleCharacterUtil.getIdByName(splitted[1]), cs, false);
                if (chhr == null) {
                    c.getPlayer().dropMessage(6, splitted[1] + " does not exist");
                    return 0;
                }
                PlayerNPC npc = new PlayerNPC(chhr, Integer.parseInt(splitted[2]), c.getPlayer().getMap(), c.getPlayer());
                npc.addToServer();
                c.getPlayer().dropMessage(6, "Done");
            } catch (Exception e) {
                c.getPlayer().dropMessage(6, "NPC failed... : " + e.getMessage());
            }
            return 1;
        }
    }

    public static class 移除角色NPC implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "DestroyPNPC";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            try {
                c.getPlayer().dropMessage(6, "Destroying playerNPC...");
                MapleNPC npc = c.getPlayer().getMap().getNPCByOid(Integer.parseInt(splitted[1]));
                if (npc instanceof PlayerNPC) {
                    ((PlayerNPC) npc).destroy(null, true);
                    c.getPlayer().dropMessage(6, "Done");
                } else {
                    c.getPlayer().dropMessage(6, "!destroypnpc [objectid]");
                }
            } catch (Exception e) {
                c.getPlayer().dropMessage(6, "NPC failed... : " + e.getMessage());
            }
            return 1;
        }
    }

    public static class 機器人 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "用法: " + splitted[0] + " <角色ID(不是角色名，去資料庫裡查詢)>");
                return 0;
            }
            ChannelServer cs = ChannelServer.getInstance(1);
            MapleClient nClient = new MapleClient(null, null, null);
            MapleCharacter nChr = MapleCharacter.loadCharFromDB(Integer.parseInt(splitted[1]), nClient, true);
            cs.getPlayerStorage().registerPlayer(nChr);
            nChr.changeMap(c.getPlayer().getMap(), c.getPlayer().getPosition());
            nChr.fakeRelog();
            cs.getPlayerStorage().deregisterPlayer(nChr);
            c.getPlayer().dropMessage(6, "成功召喚機器人到本地圖！");
            return 1;
        }
    }

    public static class 伺服器訊息 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "ServerMessage";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            String outputMessage = StringUtil.joinStringFrom(splitted, 1);
            ServerConfig.EVENT_MSG = outputMessage;
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                cserv.setServerMessage(outputMessage);
            }
            return 1;
        }
    }

    public static class 召喚 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "Spawn";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            int mid = Integer.parseInt(splitted[1]);
            int num = Math.min(CommandProcessorUtil.getOptionalIntArg(splitted, 2, 1), 50000);
            Integer level = CommandProcessorUtil.getNamedIntArg(splitted, 1, "lvl");
            Long hp = CommandProcessorUtil.getNamedLongArg(splitted, 1, "hp");
            Integer exp = CommandProcessorUtil.getNamedIntArg(splitted, 1, "exp");
            Double php = CommandProcessorUtil.getNamedDoubleArg(splitted, 1, "php");
            Double pexp = CommandProcessorUtil.getNamedDoubleArg(splitted, 1, "pexp");

            MapleMonster onemob;
            try {
                onemob = MapleLifeFactory.getMonster(mid);
            } catch (RuntimeException e) {
                c.getPlayer().dropMessage(5, "錯誤: " + e.getMessage());
                return 0;
            }
            if (onemob == null) {
                c.getPlayer().dropMessage(5, "輸入的怪物不存在.");
                return 0;
            }
            long newhp = 0;
            int newExp = 0;
            if (hp != null) {
                newhp = hp;
            } else if (php != null) {
                newhp = (long) (onemob.getMobMaxHp() * (php / 100));
            } else {
                newhp = onemob.getMobMaxHp();
            }
            if (exp != null) {
                newExp = exp;
            } else if (pexp != null) {
                newExp = (int) (onemob.getMobExp() * (pexp / 100));
            } else {
                newExp = (int) onemob.getMobExp();
            }

            if (level == null) {
                level = onemob.getMobLevel();
            }

            if (newhp < 1) {
                newhp = 1;
            }

            for (int i = 0; i < num; i++) {
                MapleMonster mob = MapleLifeFactory.getMonster(mid);
                if (mob == null) {
                    break;
                }
                ForcedMobStat forcedMobStat = new ForcedMobStat(mob.getStats(), level, 1.0);
                forcedMobStat.setExp(newExp);
                mob.setForcedMobStat(forcedMobStat);
                mob.changeHP(newhp);
                c.getPlayer().getMap().spawnMonsterOnGroundBelow(mob, c.getPlayer().getPosition());
            }
            return 1;
        }
    }

    public static class PS implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        protected static StringBuilder builder = new StringBuilder();

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (builder.length() > 1) {
                c.announce(MaplePacketCreator.getPacketFromHexString(builder.toString()));
                builder = new StringBuilder();
            } else {
                c.getPlayer().dropMessage(6, "請輸入要測試的數據包訊息!");
            }
            return 1;
        }
    }

    public static class APS extends PS {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length > 1) {
                builder.append(StringUtil.joinStringFrom(splitted, 1));
                c.getPlayer().dropMessage(6, "String is now: " + builder.toString());
            } else {
                c.getPlayer().dropMessage(6, "請輸入要測試的數據包訊息!");
            }
            return 1;
        }
    }

    public static class CPS extends PS {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            builder = new StringBuilder();
            return 1;
        }
    }

    public static class P implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length > 1) {
                c.announce(MaplePacketCreator.getPacketFromHexString(StringUtil.joinStringFrom(splitted, 1)));
            } else {
                c.getPlayer().dropMessage(6, "請輸入要測試的數據包訊息!");
            }
            return 1;
        }
    }

    public static class 重載地圖 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "ReloadMap";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            int mapId = Integer.parseInt(splitted[1]);
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                if (cserv.getMapFactory().isMapLoaded(mapId) && cserv.getMapFactory().getMap(mapId).getCharactersSize() > 0) {
                    c.getPlayer().dropMessage(5, "There exists characters on channel " + cserv.getChannel());
                    return 0;
                }
            }
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                if (cserv.getMapFactory().isMapLoaded(mapId)) {
                    cserv.getMapFactory().removeMap(mapId);
                }
            }
            return 1;
        }
    }

    public static class 生怪 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "Respawn";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().getMap().respawn(true);
            return 1;
        }
    }

    public static class Crash implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null && c.getPlayer().getGmLevel() >= victim.getGmLevel()) {
                victim.getClient().announce(HexTool.getByteArrayFromHexString("1A 00")); //give_buff with no data :D
                return 1;
            } else {
                c.getPlayer().dropMessage(6, "The victim does not exist.");
                return 0;
            }
        }
    }

    public static class 重置地圖 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "ResetMap";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().getMap().resetFully();
            return 1;
        }
    }

    public static class 重置任務 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "ResetQuest";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleQuest.getInstance(Integer.parseInt(splitted[1])).forfeit(c.getPlayer());
            return 1;
        }
    }

    public static class 開始任務 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "StartQuest";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleQuest.getInstance(Integer.parseInt(splitted[1])).start(c.getPlayer(), Integer.parseInt(splitted[2]));
            return 1;
        }
    }

    public static class 完成任務 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "CompleteQuest";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleQuest.getInstance(Integer.parseInt(splitted[1])).complete(c.getPlayer(), Integer.parseInt(splitted[2]), Integer.parseInt(splitted[3]));
            return 1;
        }
    }

    public static class 強制開始任務 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "FStartQuest";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleQuest.getInstance(Integer.parseInt(splitted[1])).forceStart(c.getPlayer(), Integer.parseInt(splitted[2]), splitted.length >= 4 ? splitted[3] : null);
            return 1;
        }
    }

    public static class 強制完成任務 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "FCompleteQuest";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleQuest.getInstance(Integer.parseInt(splitted[1])).forceComplete(c.getPlayer(), Integer.parseInt(splitted[2]));
            return 1;
        }
    }

    public static class 攻擊反應堆 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "HReactor";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().getMap().getReactorByOid(Integer.parseInt(splitted[1])).hitReactor(c);
            return 1;
        }
    }

    public static class 強制攻擊反應堆 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "FHReactor";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().getMap().getReactorByOid(Integer.parseInt(splitted[1])).forceHitReactor(Byte.parseByte(splitted[2]));
            return 1;
        }
    }

    public static class 破壞反應堆 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "DReactor";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleMap map = c.getPlayer().getMap();
            List<MapleMapObject> reactors = map.getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Collections.singletonList(MapleMapObjectType.REACTOR));
            if (splitted[1].equals("全部") || splitted[1].equals("all")) {
                for (MapleMapObject reactorL : reactors) {
                    MapleReactor reactor2l = (MapleReactor) reactorL;
                    c.getPlayer().getMap().destroyReactor(reactor2l.getObjectId());
                }
            } else {
                c.getPlayer().getMap().destroyReactor(Integer.parseInt(splitted[1]));
            }
            return 1;
        }
    }

    public static class 設置反應堆 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "SetReactor";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().getMap().setReactorState(Byte.parseByte(splitted[1]));
            return 1;
        }
    }

    public static class 重置反應堆 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "ResetReactor";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().getMap().resetReactors();
            return 1;
        }
    }

    public static class 給線上發送留言 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "SendAllNote";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length >= 1) {
                String text = StringUtil.joinStringFrom(splitted, 1);
                for (MapleCharacter mch : c.getChannelServer().getPlayerStorage().getAllCharacters()) {
                    c.getPlayer().sendNote(mch.getName(), text);
                }
            } else {
                c.getPlayer().dropMessage(6, "用法: " + splitted[0] + " <text>");
                return 0;
            }
            return 1;
        }
    }

    public static class 增益技能 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "BuffSkill";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "用法: " + splitted[0] + " <技能ID> <技能等級>");
                return 0;
            }
            SkillFactory.getSkill(Integer.parseInt(splitted[1])).getEffect(Integer.parseInt(splitted[2])).applyTo(c.getPlayer());
            return 1;
        }
    }

    public static class 增益道具 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "BuffItem";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 1) {
                c.getPlayer().dropMessage(6, "用法: " + splitted[0] + " <道具ID>");
                return 0;
            }
            int itemId = Integer.parseInt(splitted[1]);
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            if (!ii.itemExists(itemId)) {
                c.getPlayer().dropMessage(5, itemId + " 這個道具不存在.");
                return 0;
            }
            ii.getItemEffect(Integer.parseInt(splitted[1])).applyTo(c.getPlayer());
            return 1;
        }
    }

    public static class 增益道具EX implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "BuffItemEX";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 1) {
                c.getPlayer().dropMessage(6, "用法: " + splitted[0] + " <道具ID>");
                return 0;
            }
            int itemId = Integer.parseInt(splitted[1]);
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            if (!ii.itemExists(itemId)) {
                c.getPlayer().dropMessage(5, itemId + " 這個道具不存在.");
                return 0;
            }
            ii.getItemEffectEX(itemId).applyTo(c.getPlayer());
            return 1;
        }
    }

    public static class 物品數量 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "ItemSize";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().dropMessage(6, "當前加載的物品數量: " + MapleItemInformationProvider.getInstance().getAllItemSize());
            return 1;
        }
    }

    public static class 漂浮公告 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, "用法: " + splitted[0] + " <道具ID> <公告訊息>");
                return 0;
            }
            int itemId = Integer.parseInt(splitted[1]);
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            if (!ii.itemExists(itemId)) {
                c.getPlayer().dropMessage(5, itemId + " 這個道具不存在.");
                return 0;
            } else if (!ii.isFloatCashItem(itemId)) {
                c.getPlayer().dropMessage(5, itemId + " 不具有漂浮公告的效果.");
                return 0;
            }
            WorldBroadcastService.getInstance().startMapEffect(StringUtil.joinStringFrom(splitted, 2), itemId);
            return 1;
        }
    }

    public static class 刷新排行榜 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            RankingTop.getInstance().initAll();
            c.getPlayer().dropMessage(5, "排行榜刷新完成");
            return 1;
        }
    }

    public static class checkGames implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(6, splitted[0] + " <使用方法：進程名/MD5>");
                return 0;
            }
            int i = 0;
            for (ChannelServer ch : ChannelServer.getAllInstances()) {
                for (MapleCharacter chr : ch.getPlayerStorage().getAllCharacters()) {
                    for (MapleProcess mp : chr.getProcess()) {
                        if (mp.getMD5().equalsIgnoreCase(splitted[1]) || mp.getName().toLowerCase().contains(splitted[1].toLowerCase())) {
                            c.getPlayer().dropMessage(5, "角色：" + chr.getName() + " 進程【\"" + mp.getPath() + "\" MD5：\"" + mp.getMD5() + "\"】");
                            i++;
                        }
                    }
                }
            }
            c.getPlayer().dropMessage(5, "進程檢查完成，本次共檢測到" + i + "個。");
            return 1;
        }
    }

    public static class 重載簽到獎勵 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            MapleDailyGift.initialize();
            c.dropMessage("簽到獎勵重載完成！");
            return 1;
        }
    }

    public static class 給樂豆 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(-11, splitted[0] + " <玩家名稱> <點數> <1-記錄MVP消費,0-不記錄(當點數設定為負數時需要)>");
                return 0;
            }
            int amount = Integer.parseInt(splitted[2]);
            if (amount < 0 && splitted.length < 4) {
                c.getPlayer().dropMessage(-11, splitted[0] + " <玩家名稱> <點數> <1-記錄MVP消費,0-不記錄>");
                return 0;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            victim.modifyCSPoints(1, amount, true, amount < 0 && (splitted[3].equalsIgnoreCase("1")));
            return 1;
        }
    }

    public static class 給楓點 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(-11, splitted[0] + " <玩家名稱> <點數>");
                return 0;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            victim.modifyCSPoints(2, Integer.parseInt(splitted[2]), true);
            return 1;
        }
    }

    public static class 給里程 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(-11, splitted[0] + " <玩家名稱> <數量>");
                return 0;
            }
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            victim.modifyMileage(Integer.parseInt(splitted[2]), 1, true, false, "管理員[" + c.getPlayer().getName() + "]使用[" + splitted[0] + "]指令獲得");
            return 1;
        }
    }

    public static class 重載獎池 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "reloadGach";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            RafflePool.reload();
            c.getPlayer().dropMessage(5, "獎池資料重載完成。");
            return 1;
        }
    }

    public static class HotTime implements CommandExecute {

        private static ScheduledFuture<?> htEvent = null;
        public static List<Pair<Integer, Integer>> HotTimeItems = new LinkedList<>();

        @Override
        public String getShortCommand() {
            return "HT";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 4) {
                c.getPlayer().dropMessage(-11, splitted[0] + " <時間:yyyyMMddhhmm> <道具ID> <數量> <道具ID> <數量> <道具ID> <數量>...");
                return 0;
            }
            long startTime = DateUtil.getStringToTime(splitted[1]) - System.currentTimeMillis();
            if (startTime <= 0) {
                c.getPlayer().dropMessage(-11, splitted[0] + " <時間:yyyyMMddhhmm> <道具ID> <數量> <道具ID> <數量> <道具ID> <數量>...");
                return 0;
            }
            if (htEvent != null) {
                htEvent.cancel(false);
                htEvent = null;
                WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.serverNotice(0, "預訂的HotTime已被取消，將重新預訂HotTime"));
            }
            HotTimeItems.clear();
            int itemID;
            int quantity;
            for (int i = 2; i < splitted.length; i += 2) {
                if (i + 1 >= splitted.length) {
                    c.getPlayer().dropMessage(-11, splitted[0] + " <時間:yyyyMMddhhmm> <道具ID> <數量> <道具ID> <數量> <道具ID> <數量>...");
                    return 0;
                }
                HotTimeItems.add(new Pair(Integer.parseInt(splitted[i]), Integer.parseInt(splitted[i + 1])));
            }
            return 1;
        }
    }

    public static class 獎勵 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "Reward";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 5) {
                c.getPlayer().dropMessage(-11, splitted[0] + " <角色名> <獎勵類型 1-道具 3-楓點 4-楓幣 5-經驗值> <道具ID(只有類型為1時才需要)> <發放的量> <禮物備註> <僅當前角色能領取(預設true)> <領取期限(天):不輸入預設為7, 小於或等於0為永久>");
                return 0;
            }
            int i = 1;
            String chrName = splitted[i++];
            int type = Integer.parseInt(splitted[i++]);
            if (type <= MapleReward.道具 || type > MapleReward.經驗 || type == MapleReward.現金道具) {
                if (type != MapleReward.道具 || splitted.length < 5) {
                    c.getPlayer().dropMessage(-11, splitted[0] + " <角色名> <獎勵類型 1-道具 3-楓點 4-楓幣 5-經驗值> <道具ID(只有類型為1時才需要)> <發放的量> <禮物備註> <僅當前角色能領取(預設true)> <領取期限(天):不輸入預設為7, 小於或等於0為永久>");
                    return 0;
                }
            }
            int itemID = 0;
            if (type == MapleReward.道具) {
                itemID = Integer.parseInt(splitted[i++]);
                if (MapleItemInformationProvider.getInstance().isCash(itemID)) {
                    type = MapleReward.現金道具;
                }
            }
            long amount = Long.parseLong(splitted[i++]);
            String desc = splitted[i++];
            boolean acc = CommandProcessorUtil.getOptionalBooleanArg(splitted, i, true);
            int day = CommandProcessorUtil.getOptionalIntArg(splitted, i++, -1);
            if (day < 0) {
                day = CommandProcessorUtil.getOptionalIntArg(splitted, i++, 7);
            }
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                    if (chrName.equals(mch.getName())) {
                        mch.addReward(acc, DateUtil.getNextDayTime(0), day <= 0 ? 0 : (DateUtil.getNextDayTime(day) - 60000), type, amount, itemID, desc);
                        mch.updateReward();
                        mch.dropMessage(1, "收到管理員發來的禮物，請到左側獎勵箱領收。");
                        c.getPlayer().dropMessage(1, "獎勵已發放到線上角色<" + chrName + ">的獎勵箱。");
                        return 1;
                    }
                }
            }

            try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
                PreparedStatement ps = con.prepareStatement("SELECT id,accountid FROM characters WHERE name = ?");
                ps.setString(1, chrName);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    MapleCharacter.addReward(acc ? rs.getInt("accountid") : 0, rs.getInt("id"), DateUtil.getNextDayTime(0), day <= 0 ? 0 : (DateUtil.getNextDayTime(day) - 60000), type, amount, itemID, desc);
                    c.getPlayer().dropMessage(1, "獎勵已發放到離線角色<" + chrName + ">的獎勵箱。");
                } else {
                    c.getPlayer().dropMessage(1, "找不到角色<" + chrName + ">。");
                }
                rs.close();
                ps.close();
            } catch (SQLException e) {
                log.error("Error get cid, accountid", e);
                return 0;
            }
            return 1;
        }
    }

    public static class 給線上獎勵 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "GOR";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 4) {
                c.getPlayer().dropMessage(-11, splitted[0] + " <獎勵類型 1-道具 3-楓點 4-楓幣 5-經驗值> <道具ID(只有類型為1時才需要)> <發放的量> <禮物備註> <僅當前角色能領取(預設true)> <領取期限(天):不輸入預設為7, 小於或等於0為永久>");
                return 0;
            }
            int i = 1;
            int type = Integer.parseInt(splitted[i++]);
            if (type <= MapleReward.道具 || type > MapleReward.經驗 || type == MapleReward.現金道具) {
                if (type != MapleReward.道具 || splitted.length < 5) {
                    c.getPlayer().dropMessage(-11, splitted[0] + " <獎勵類型 1-道具 3-楓點 4-楓幣 5-經驗值> <道具ID(只有類型為1時才需要)> <發放的量/道具ID> <禮物備註> <僅當前角色能領取(預設true)> <領取期限(天):不輸入預設為7, 小於或等於0為永久>");
                    return 0;
                }
            }
            int itemID = 0;
            if (type == MapleReward.道具) {
                itemID = Integer.parseInt(splitted[i++]);
                if (MapleItemInformationProvider.getInstance().isCash(itemID)) {
                    type = MapleReward.現金道具;
                }
            }
            long amount = Long.parseLong(splitted[i++]);
            String desc = splitted[i++];
            boolean acc = CommandProcessorUtil.getOptionalBooleanArg(splitted, i, true);
            int day = CommandProcessorUtil.getOptionalIntArg(splitted, i++, -1);
            if (day < 0) {
                day = CommandProcessorUtil.getOptionalIntArg(splitted, i++, 7);
            }
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                    mch.addReward(acc, DateUtil.getNextDayTime(0), day <= 0 ? 0 : (DateUtil.getNextDayTime(day) - 60000), type, amount, itemID, desc);
                    mch.updateReward();
                    mch.dropMessage(1, "收到管理員發來的禮物，請到左側獎勵箱領收。");
                }
            }
            return 1;
        }
    }

    public static class 給全部帳號獎勵 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "GAR";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 4) {
                c.getPlayer().dropMessage(-11, splitted[0] + " <獎勵類型 1-道具 3-楓點 4-楓幣 5-經驗值> <道具ID(只有類型為1時才需要)> <發放的量> <禮物備註> <領取期限(天):不輸入預設為7, 小於或等於0為永久>");
                return 0;
            }
            int i = 1;
            int type = Integer.parseInt(splitted[i++]);
            if (type <= MapleReward.道具 || type > MapleReward.經驗 || type == MapleReward.現金道具) {
                if (type != MapleReward.道具 || splitted.length < 5) {
                    c.getPlayer().dropMessage(-11, splitted[0] + " <獎勵類型 1-道具 3-楓點 4-楓幣 5-經驗值> <道具ID(只有類型為1時才需要)> <發放的量> <禮物備註> <領取期限(天):不輸入預設為7, 小於或等於0為永久>");
                    return 0;
                }
            }
            int itemID = 0;
            if (type == MapleReward.道具) {
                itemID = Integer.parseInt(splitted[i++]);
                if (MapleItemInformationProvider.getInstance().isCash(itemID)) {
                    type = MapleReward.現金道具;
                }
            }
            long amount = Long.parseLong(splitted[i++]);
            String desc = splitted[i++];
            int day = CommandProcessorUtil.getOptionalIntArg(splitted, i++, 7);
            try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
                PreparedStatement ps = con.prepareStatement("SELECT id FROM accounts");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    MapleCharacter.addReward(rs.getInt("id"), 0, DateUtil.getNextDayTime(0), day <= 0 ? 0 : (DateUtil.getNextDayTime(day) - 60000), type, amount, itemID, desc);
                }
                rs.close();
                ps.close();
            } catch (SQLException e) {
                log.error("Error get accountid", e);
                return 0;
            }

            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                    mch.updateReward();
                    mch.dropMessage(1, "收到管理員發來的禮物，請到左側獎勵箱領收。");
                }
            }
            return 1;
        }
    }

    public static class 吸怪 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return null;
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            Point pos = c.getPlayer().getPosition();
            for (final MapleMapObject mmo : c.getPlayer().getMap().getAllMonster()) {
                ((MapleMonster) mmo).move(pos);
            }
            return 1;
        }
    }
}
