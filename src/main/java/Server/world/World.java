package Server.world;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.SecondaryStat;
import Client.SecondaryStatValueHolder;
import Config.configs.ServerConfig;
import Config.constants.enums.Holiday;
import Config.constants.skills.冒險家_技能群組.type_法師.主教;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MobSkill;
import Net.server.shops.HiredFisher;
import Net.server.shops.HiredMerchant;
import Packet.BuffPacket;
import Server.auction.AuctionServer;
import Server.cashshop.CashShopServer;
import Server.channel.ChannelServer;
import Server.channel.PlayerStorage;
import com.github.heqiao2010.lunar.LunarCalendar;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class World {

    public static final int CASH_SHOP_CHANNEL = -10;
    public static final int AUCTION_CHANNEL = -20;
    public static final int WORLD_TOTAL = 0;

    private static int lastCheckHolidayDay = 0;
    private static Holiday holiday = Holiday.None;

    public static void init() {
        WorldFindService.getInstance();
        WorldBroadcastService.getInstance();
        WorldAllianceService.getInstance();
        WorldBuddyService.getInstance();
        WorldGuildService.getInstance();
        WorldMessengerService.getInstance();
    }

    public static String getStatus() {
        StringBuilder ret = new StringBuilder();
        int totalUsers = 0;
        for (ChannelServer cs : ChannelServer.getAllInstances()) {
            ret.append("頻道 ");
            ret.append(cs.getChannel());
            ret.append(": ");
            int channelUsers = cs.getConnectedClients();
            totalUsers += channelUsers;
            ret.append(channelUsers);
            ret.append(" 玩家\n");
        }
        ret.append("總計在線: ");
        ret.append(totalUsers);
        ret.append("\n");
        return ret.toString();
    }

    public static void updateHoliday() {
        LocalDate currentDate = LocalDate.now();
        if (lastCheckHolidayDay == currentDate.getDayOfMonth()) return;
        lastCheckHolidayDay = currentDate.getDayOfMonth();
        for (Holiday d : Holiday.values()) {
            if (d == Holiday.None) continue;
            if (isHolidayWeek(currentDate, d)) {
                holiday = d;
                break;
            }
        }
    }

    public static Holiday getHoliday() {
        return holiday;
    }

    private static boolean isHolidayWeek(LocalDate date, Holiday holiday) {
        return isDateBetween(date.getYear(), date, holiday) || isDateBetween(date.getYear() + 1, date, holiday);
    }

    private static boolean isDateBetween(int year, LocalDate date, Holiday holiday) {
        LocalDate holidayDate;
        if (holiday.isLunar()) {
            Calendar calendar = LunarCalendar.lunar2Solar(year, holiday.getMonth(), holiday.getDayOfMonth(), false);
            holidayDate = LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        } else {
            holidayDate = LocalDate.of(year, holiday.getMonth(), holiday.getDayOfMonth());
        }

        LocalDate startOfWeek = holidayDate.minusDays(14);
        LocalDate endOfWeek = holidayDate.plusDays(14);

        return date.isEqual(startOfWeek) || date.isEqual(endOfWeek) || (date.isAfter(startOfWeek) && date.isBefore(endOfWeek));
    }

    public static Map<Integer, Integer> getConnected() {
        Map<Integer, Integer> ret = new LinkedHashMap<>();
        int total = 0;
        for (ChannelServer ch : ChannelServer.getAllInstances()) {
            int chOnline = ch.getConnectedClients();
            ret.put(ch.getChannel(), chOnline);
            total += chOnline;
        }
        int csOnline = CashShopServer.getConnectedClients();
        ret.put(CASH_SHOP_CHANNEL, csOnline);
        total += csOnline;
        ret.put(WORLD_TOTAL, total);
        return ret;
    }

    public static int getTotalConnected() {
        Map<Integer, Integer> connected = getConnected();
        return connected.get(WORLD_TOTAL);
    }

    public static List<CheaterData> getCheaters() {
        List<CheaterData> allCheaters = new ArrayList<>();
        for (ChannelServer cs : ChannelServer.getAllInstances()) {
            allCheaters.addAll(cs.getCheaters());
        }
        Collections.sort(allCheaters);
        return allCheaters.subList(0, 20);
    }

    public static List<CheaterData> getReports() {
        List<CheaterData> allCheaters = new ArrayList<>();
        for (ChannelServer cs : ChannelServer.getAllInstances()) {
            allCheaters.addAll(cs.getReports());
        }
        Collections.sort(allCheaters);
        return allCheaters.subList(0, 20);
    }

    public static boolean isConnected(String charName) {
        return WorldFindService.getInstance().findChannel(charName) > 0;
    }

    public static void toggleMegaphoneMuteState() {
        for (ChannelServer cs : ChannelServer.getAllInstances()) {
            cs.toggleMegaphoneMuteState();
        }
    }

    public static void clearChannelChangeDataByAccountId(int accountid) {
        for (ChannelServer cs : ChannelServer.getAllInstances()) {
            getStorage(cs.getChannel()).deregisterPendingPlayerByAccountId(accountid);
        }
        getStorage(-20).deregisterPendingPlayerByAccountId(accountid);
        getStorage(-10).deregisterPendingPlayerByAccountId(accountid);
    }

    public static void ChannelChange_Data(CharacterTransfer Data, int characterid, int toChannel) {
        getStorage(toChannel).registerPendingPlayer(Data, characterid);
    }

    public static boolean isCharacterListConnected(List<String> charNames) {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (String name : charNames) {
                if (cserv.isConnected(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getAllowLoginTip(List<String> charNames) {
        StringBuilder ret = new StringBuilder("賬號下其他角色在遊戲: ");
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (String name : charNames) {
                if (cserv.isConnected(name)) {
                    ret.append(name);
                    ret.append(" ");
                }
            }
        }
        return ret.toString();
    }

    /*
     * 檢測賬號是否開過僱傭商店
     */
    public static boolean hasMerchant(int accountID) {
        for (ChannelServer cs : ChannelServer.getAllInstances()) {
            if (cs.containsMerchant(accountID)) {
                return true;
            }
        }
        return false;
    }

    /*
     * 檢測賬號下的玩家是否開過僱傭商店
     */
    public static boolean hasMerchant(int accountID, int characterID) {
        for (ChannelServer cs : ChannelServer.getAllInstances()) {
            if (cs.containsMerchant(accountID, characterID)) {
                return true;
            }
        }
        return false;
    }

    /*
     * 獲取賬號下的玩家的僱傭商店信息
     * 返回 僱傭商店
     */
    public static HiredMerchant getMerchant(int accountID, int characterID) {
        for (ChannelServer cs : ChannelServer.getAllInstances()) {
            if (cs.containsMerchant(accountID, characterID)) {
                return cs.getHiredMerchants(accountID, characterID);
            }
        }
        return null;
    }

    public static PlayerStorage getStorage(int channel) {
        if (channel == -20) {
            return AuctionServer.getInstance().getPlayerStorage();
        } else if (channel == -10) {
            return CashShopServer.getPlayerStorage();
        }
        return ChannelServer.getInstance(channel).getPlayerStorage();
    }

    public static boolean isChannelAvailable(int ch) {
        return !(ChannelServer.getInstance(ch) == null || ChannelServer.getInstance(ch).getPlayerStorage() == null) && ChannelServer.getInstance(ch).getPlayerStorage().getConnectedClients() < ServerConfig.LOGIN_USERLIMIT;
    }

    public static boolean hasFisher(int accountID, int characterID) {
        for (ChannelServer cs : ChannelServer.getAllInstances()) {
            if (cs.containsFisher(accountID, characterID)) {
                return true;
            }
        }
        return false;
    }

    public static HiredFisher getFisher(int accountID, int characterID) {
        for (ChannelServer cs : ChannelServer.getAllInstances()) {
            if (cs.containsFisher(accountID, characterID)) {
                return cs.getHiredFisher(accountID, characterID);
            }
        }
        return null;
    }

    public static class Client {

        private static final Set<MapleClient> clients = new HashSet<>();

        public static void addClient(MapleClient c) {
            synchronized (World.class) {
                clients.add(c);
            }
        }

        public static boolean isStuck(MapleClient c, int accId) {
            synchronized (World.class) {
                MapleClient stuckClient = null;
                for (MapleClient client : getClients()) {
                    if (client != c && client.getAccID() == accId) {
                        stuckClient = client;
                        break;
                    }
                }
                if (stuckClient == null) {
                    return true;
                } else if (stuckClient.getSession() == null) {
                    clients.remove(stuckClient);
                    return true;
                } else {
                    stuckClient.getSession().close();
                    return false;
                }
            }
        }

        public static boolean removeClient(MapleClient c) {
            synchronized (World.class) {
                return clients.remove(c);
            }
        }

        public static ArrayList<MapleClient> getClients() {
            synchronized (World.class) {
                return new ArrayList<>(clients);
            }
        }
    }

    public static class TemporaryStat {
        public static final Map<Integer, Map<SecondaryStat, List<SecondaryStatValueHolder>>> TemporaryStats = new LinkedHashMap<>();

        public static boolean IsSaveStat(SecondaryStat stat, SecondaryStatValueHolder mbsvh) {
            if (mbsvh.effect instanceof MobSkill) {
                return false;
            }
            switch (stat) {
                case IndieEXP:
                case ExpBuffRate:
                case PlusExpRate:
                    return mbsvh.sourceID < 0;
            }
            return IsNotRemoveSaveStat(stat, mbsvh);
        }

        public static boolean IsNotRemoveSaveStat(SecondaryStat stat, SecondaryStatValueHolder mbsvh) {
            if (mbsvh.effect instanceof MobSkill) {
                return false;
            }
            switch (mbsvh.sourceID) {
                case 主教.聖十字魔法盾_CD:
                    return true;
                case 主教.天堂之門_BUFF:
                    return stat == SecondaryStat.HeavensDoorNotTime;
            }
            switch (stat) {
                case RuneStoneNoTime:
                case ViperTimeLeap:
                    return true;
            }
            return false;
        }

        public static void SaveData(MapleCharacter chr) {
            Map<SecondaryStat, List<SecondaryStatValueHolder>> effects = TemporaryStats.computeIfAbsent(chr.getId(), k -> new LinkedHashMap<>());
            chr.getEffects().forEach((key, value) -> value.stream().filter(mbsvh -> IsSaveStat(key, mbsvh))
                    .forEach(mbsvh -> effects.computeIfAbsent(key, k -> new LinkedList<>()).add(mbsvh)));
            if (effects.isEmpty()) {
                TemporaryStats.remove(chr.getId());
            }
        }

        public static void LoadData(MapleCharacter chr) {
            if (!TemporaryStats.containsKey(chr.getId())) {
                return;
            }
            Map<Integer, List<SecondaryStat>> stats = new LinkedHashMap<>();
            TemporaryStats.remove(chr.getId()).forEach((mapleBuffStat, v) -> v.forEach(mbsvh -> {
                if (mbsvh.getLeftTime() >= 5000 && mbsvh.schedule != null && !mbsvh.schedule.isCancelled() && !mbsvh.schedule.isDone()) {
                    chr.getEffects().computeIfAbsent(mapleBuffStat, k -> new LinkedList<>()).add(mbsvh);
                    if (mbsvh.CancelAction != null) {
                        mbsvh.CancelAction.changeTarget(chr);
                    }
                    stats.computeIfAbsent(mbsvh.sourceID, k -> new LinkedList<>()).add(mapleBuffStat);
                }
            }));
            stats.forEach((key, value) -> chr.send(BuffPacket.giveBuff(chr, null, value.stream().collect(Collectors.toMap(stat -> stat, stat -> key, (a, b) -> b, LinkedHashMap::new)))));
        }

        public static void CancelStat(int cid, List<SecondaryStat> stats, MapleStatEffect effect, long startTime) {
            if (!TemporaryStats.containsKey(cid)) {
                return;
            }
            Map<SecondaryStat, List<SecondaryStatValueHolder>> effects = TemporaryStats.get(cid);
            stats.stream().filter(effects::containsKey).forEach(stat -> {
                List<SecondaryStatValueHolder> holderList = effects.get(stat);
                Iterator<SecondaryStatValueHolder> holderIterator = holderList.iterator();
                while (holderIterator.hasNext()) {
                    final SecondaryStatValueHolder mbsvh = holderIterator.next();
                    if (mbsvh.effect.sameSource(effect) && (mbsvh.startTime == startTime || startTime == -1L) || !mbsvh.effect.sameSource(effect) && !stat.canStack()) {
                        mbsvh.cancel();
                        holderIterator.remove();
                    }
                }
                if (holderList.isEmpty()) {
                    effects.remove(stat);
                }
            });
            if (effects.isEmpty()) {
                TemporaryStats.remove(cid);
            }
        }
    }
}
