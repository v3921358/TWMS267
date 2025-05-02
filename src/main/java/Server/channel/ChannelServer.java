package Server.channel;

import Client.MapleCharacter;
import Config.configs.ServerConfig;
import Net.server.events.*;
import Net.server.life.PlayerNPC;
import Net.server.maps.AramiaFireWorks;
import Net.server.maps.MapleMap;
import Net.server.maps.MapleMapFactory;
import Net.server.market.MarketEngine;
import Net.server.shops.HiredFisher;
import Net.server.shops.HiredMerchant;
import Packet.MaplePacketCreator;
import Plugin.script.binding.ScriptEvent;
import Server.ServerType;
import Server.channel.handler.HiredFisherStorage;
import Server.login.LoginServer;
import Server.netty.ServerConnection;
import Server.world.CheaterData;
import nativeimage.Reflection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Reflection(publicConstructors = true, publicMethods = true, publicFields = true, scanPackage = "Server.channel")
public class ChannelServer {
    private static final Logger log = LoggerFactory.getLogger(ChannelServer.class);
    private static final Map<Integer, ChannelServer> instances = new HashMap<>();
    public static long serverStartTime;
    private final MapleMapFactory mapFactory;
    private final Map<MapleEventType, MapleEvent> events = new EnumMap<>(MapleEventType.class);
    private int channel;
    private final int flags = 0;
    private final MarketEngine me = new MarketEngine();
    private final List<PlayerNPC> playerNPCs = new LinkedList<>();
    private ServerConnection init;
    private int doubleExp = 1;
    private short port;
    private volatile boolean shutdown = false, finishedShutdown = false, MegaphoneMuteState = false;
    private PlayerStorage players;
    private MerchantStorage merchants;
    private HiredFisherStorage fishers;
    private ScriptEvent eventSM;
    private int eventmap = -1;
    private final AtomicInteger runningIdx = new AtomicInteger(0);
    private static final ExecutorService saveExecutor = Executors.newSingleThreadExecutor();
    public enum ChannelType {
        NORMAL(0x01),
        CHAOS(0x02),
        ABNORMAL(0x04),
        MVP_BRONZE(0x08),
        MVP_SILVER(0x10),
        MVP_GOLD(0x20),
        MVP_DIAMOND(0x40),
        MVP_RED(0x80);

        private final int type;

        ChannelType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }

        public static ChannelType getByType(int type) {
            for (ChannelType ct : values()) {
                if (ct.getType() == type) {
                    return ct;
                }
            }
            return NORMAL;
        }

        public boolean check(int type) {
            return (type & getType()) != 0;
        }
    }

    private ChannelServer(int channel) {
        this.channel = channel;
        this.mapFactory = new MapleMapFactory(channel);
    }

    public static Set<Integer> getAllInstance() {
        return new HashSet<>(instances.keySet());
    }

    public static ChannelServer newInstance(int channel) {
        return new ChannelServer(channel);
    }

    public static ChannelServer getInstance(int channel) {
        return instances.get(channel);
    }

    public static List<ChannelServer> getAllInstances() {
        return new ArrayList<>(instances.values());
    }

    public static void startChannel_Main() {
        serverStartTime = System.currentTimeMillis();
        int ch = Math.min(ServerConfig.CHANNELS_PER_WORLD, 40);
        for (int i = 1; i <= ch; i++) {
            newInstance(i).run_startup_configurations();
        }
        log.info("所有頻道已啟動完成.");
    }

    public static Map<Integer, Integer> getChannelLoad() {
        return instances.values().stream()
                .collect(Collectors.toMap(ChannelServer::getChannel, ChannelServer::getConnectedClients));
    }

    public static MapleCharacter getCharacterById(int id) {
        return instances.values().stream()
                .map(cserv -> cserv.getPlayerStorage().getCharacterById(id))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public static MapleCharacter getCharacterByName(String name) {
        return instances.values().stream()
                .map(cserv -> cserv.getPlayerStorage().getCharacterByName(name))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }


    public static int getChannelStartPort() {
        return ServerConfig.CHANNEL_START_PORT;
    }

    public void loadEvents() {
        if (!events.isEmpty()) return;

        events.put(MapleEventType.CokePlay, new MapleCoconut(channel, MapleEventType.CokePlay));
        events.put(MapleEventType.Coconut, new MapleCoconut(channel, MapleEventType.Coconut));
        events.put(MapleEventType.Fitness, new MapleFitness(channel, MapleEventType.Fitness));
        events.put(MapleEventType.OlaOla, new MapleOla(channel, MapleEventType.OlaOla));
        events.put(MapleEventType.OxQuiz, new MapleOxQuiz(channel, MapleEventType.OxQuiz));
        events.put(MapleEventType.Snowball, new MapleSnowball(channel, MapleEventType.Snowball));
        events.put(MapleEventType.Survival, new MapleSurvival(channel, MapleEventType.Survival));
    }

    public MapleEvent getEvent(MapleEventType t) {
        return events.get(t);
    }

    public void run_startup_configurations() {
        setChannel(channel);
        try {
            players = new PlayerStorage(channel);
            merchants = new MerchantStorage(channel);
            fishers = new HiredFisherStorage(channel);
            port = (short) (getChannelStartPort() + channel - 1);
            init = new ServerConnection(port, 0, channel, ServerType.ChannelServer);
            init.run();
            log.info("CHANNEL {} listening on port: {}", channel, port);
            // 加載 Scripts/Server/下的全局插件
            //getServerManager().Init(channel);
            loadEvents();
        } catch (Exception e) {
            throw new RuntimeException("綁定連接埠: " + port + " 失敗 (ch: " + getChannel() + ")", e);
        }
    }

    public void shutdown() {
        if (finishedShutdown) return;

        shutdown = true;
        log.info("Channel {} saving map information", channel);
        mapFactory.getAllMaps().forEach(MapleMap::saveBreakTimeFieldStep);
        log.info("Channel {} cleaning event scripts", channel);
//        eventSM.cancel();
        log.info("Channel {} unbinding port", channel);
        init.close();
        instances.remove(channel);
    }


    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        instances.put(channel, this);
        LoginServer.addChannel(channel);
    }

    public MapleMapFactory getMapFactory() {
        return mapFactory;
    }

    public void addPlayer(MapleCharacter chr) {
        getPlayerStorage().registerPlayer(chr);
    }

    public PlayerStorage getPlayerStorage() {
        if (players == null) {
            players = new PlayerStorage(channel);
        }
        return players;
    }


    public void removePlayer(MapleCharacter chr) {
        removePlayer(chr.getId());
    }

    public void removePlayer(int idz) {
        getPlayerStorage().deregisterPlayer(idz);
    }

    public String getServerMessage() {
        return ServerConfig.EVENT_MSG;
    }

    public void setServerMessage(String newMessage) {
        broadcastPacket(MaplePacketCreator.serverMessage(newMessage));
    }

    public void broadcastPacket(byte[] data) {
        getPlayerStorage().broadcastPacket(data);
    }

    public void broadcastSmegaPacket(byte[] data) {
        getPlayerStorage().broadcastSmegaPacket(data);
    }

    public void broadcastGMPacket(byte[] data) {
        getPlayerStorage().broadcastGMPacket(data);
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public int getLoadedMaps() {
        return mapFactory.getLoadedMaps();
    }

    public ScriptEvent getEventSM() {
        return eventSM;
    }

//    public void reloadEvents() {
//        eventSM.cancel();
//        eventSM = new EventScriptManager(this, ServerConfig.CHANNEL_EVENTS.split(","));
//        eventSM.init();
//    }

    /**
     * 根據事件名稱重載事件
     * 其他事件不會受到影響
     *
     * @param eventName 事件名稱
     * @see EventScriptManager#reload(String)
     */
//    public void reloadEvent(String eventName) {
//        eventSM.reload(eventName);
//    }

    /**
     * 基礎經驗倍率
     */
    public int getBaseExpRate() {
        return ServerConfig.CHANNEL_RATE_BASEEXP;
    }

    /**
     * 遊戲經驗倍率
     */
    public int getExpRate() {
        return ServerConfig.CHANNEL_RATE_EXP;
    }

    public void setExpRate(int rate) {
        ServerConfig.CHANNEL_RATE_EXP = rate;
    }

    /**
     * 遊戲楓幣爆率
     */
    public int getMesoRate() {
        return ServerConfig.CHANNEL_RATE_MESO;
    }

    public void setMesoRate(int rate) {
        ServerConfig.CHANNEL_RATE_MESO = rate;
    }

    /**
     * 遊戲裝備爆率
     */
    public int getDropRate() {
        return ServerConfig.CHANNEL_RATE_DROP;
    }

    public void setDropRate(int rate) {
        ServerConfig.CHANNEL_RATE_DROP = rate;
    }

    /**
     * 特殊數據庫道具的爆率
     */
    public int getDropgRate() {
        return ServerConfig.CHANNEL_RATE_GLOBALDROP;
    }

    public void setDropgRate(int rate) {
        ServerConfig.CHANNEL_RATE_GLOBALDROP = rate;
    }

    /**
     * 雙倍經驗活動
     */
    public int getDoubleExp() {
        if (doubleExp < 0 || doubleExp > 2) {
            return 1;
        } else {
            return doubleExp;
        }
    }

    public void setDoubleExp(int doubleExp) {
        if (doubleExp < 0 || doubleExp > 2) {
            this.doubleExp = 1;
        } else {
            this.doubleExp = doubleExp;
        }
    }

    /**
     * 關閉所有僱傭商店
     */
    public void closeAllMerchants() {
        merchants.closeAllMerchants();
    }

    /**
     * 添加僱傭商店
     */
    public int addMerchant(HiredMerchant hMerchant) {
        return merchants.addMerchant(hMerchant);
    }

    /**
     * 刪除僱傭商店
     */
    public void removeMerchant(HiredMerchant hMerchant) {
        merchants.removeMerchant(hMerchant);
    }

    /**
     * 檢測賬號是否開過僱傭商店
     */
    public boolean containsMerchant(int accId) {
        return merchants.containsMerchant(accId);
    }

    /**
     * 檢測賬號下的玩家是否開過僱傭商店
     */
    public boolean containsMerchant(int accId, int chrId) {
        return merchants.containsMerchant(accId, chrId);
    }

    public List<HiredMerchant> searchMerchant(int itemSearch) {
        return merchants.searchMerchant(itemSearch);
    }

    /**
     * 獲取賬號下的玩家的僱傭商店信息 返回 僱傭商店
     */
    public HiredMerchant getHiredMerchants(int accId, int chrId) {
        return merchants.getHiredMerchants(accId, chrId);
    }

    public void closeAllFisher() {
        fishers.closeAllFisher();
    }

    public int addFisher(HiredFisher hiredFisher) {
        return fishers.addFisher(hiredFisher);
    }

    public void removeFisher(HiredFisher hiredFisher) {
        fishers.removeFisher(hiredFisher);
    }

    public boolean containsFisher(int accId, int chrId) {
        return fishers.containsFisher(accId, chrId);
    }

    public HiredFisher getHiredFisher(int accId, int chrId) {
        return fishers.getHiredFisher(accId, chrId);
    }

    public void toggleMegaphoneMuteState() {
        this.MegaphoneMuteState = !this.MegaphoneMuteState;
    }

    public boolean getMegaphoneMuteState() {
        return MegaphoneMuteState;
    }

    public int getEvent() {
        return eventmap;
    }

    public void setEvent(int ze) {
        this.eventmap = ze;
    }

    public Collection<PlayerNPC> getAllPlayerNPC() {
        return playerNPCs;
    }

    public void addPlayerNPC(PlayerNPC npc) {
        if (playerNPCs.contains(npc)) {
            return;
        }
        playerNPCs.add(npc);
        getMapFactory().getMap(npc.getMapId()).addMapObject(npc);
    }

    public void removePlayerNPC(PlayerNPC npc) {
        if (playerNPCs.contains(npc)) {
            playerNPCs.remove(npc);
            getMapFactory().getMap(npc.getMapId()).removeMapObject(npc);
        }
    }

    public String getServerName() {
        return ServerConfig.SERVER_NAME;
    }

    public void setServerName(String sn) {
        ServerConfig.SERVER_NAME = sn;
    }

    public String getTrueServerName() {
        return ServerConfig.SERVER_NAME.substring(0, ServerConfig.SERVER_NAME.length() - 3);
    }

    public int getPort() {
        return port;
    }

    public void setShutdown() {
        this.shutdown = true;
        this.finishedShutdown = true;
        log.info("頻道 " + channel + " 已關閉完成.");
    }


    public boolean hasFinishedShutdown() {
        return finishedShutdown;
    }

    public int getTempFlag() {
        return flags;
    }

    public int getConnectedClients() {
        return getPlayerStorage().getConnectedClients();
    }

    public List<CheaterData> getCheaters() {
        List<CheaterData> cheaters = getPlayerStorage().getCheaters();
        Collections.sort(cheaters);
        return cheaters;
    }

    public List<CheaterData> getReports() {
        List<CheaterData> cheaters = getPlayerStorage().getReports();
        Collections.sort(cheaters);
        return cheaters;
    }

    public void broadcastPacket(ByteBuffer data) {
        getPlayerStorage().broadcastPacket(data.array());
    }

    public void broadcastSmegaPacket(ByteBuffer data) {
        getPlayerStorage().broadcastSmegaPacket(data.array());
    }

    public void broadcastGMPacket(ByteBuffer data) {
        getPlayerStorage().broadcastGMPacket(data.array());
    }

    public void broadcastMapAreaMessage(int area, ByteBuffer message) {
        for (MapleMap load : getMapFactory().getAllMaps()) {
            if (load.getId() / 10000000 == area && load.getCharactersSize() > 0) {
                load.broadcastMessage(message.array());
            }
        }
    }

    public void startMapEffect(String msg, int itemId) {
        startMapEffect(msg, itemId, 10);
    }

    public void startMapEffect(String msg, int itemId, int time) {
        for (MapleMap load : getMapFactory().getAllMaps()) {
            if (load.getCharactersSize() > 0) {
                load.startMapEffect(msg, itemId, time);
            }
        }
    }

    public AramiaFireWorks getFireWorks() {
        return getFireWorks();
    }


    public boolean isConnected(String name) {
        return getPlayerStorage().getCharacterByName(name) != null;
    }

    public MarketEngine getMarket() {
        return me;
    }

    public int getSessionIdx() {
        return runningIdx.getAndIncrement();
    }

    public static ExecutorService getSaveExecutor() {
        return saveExecutor;
    }

    public ChannelType getChannelType() {
        String[] chList = new String[]{
        };

        int i = 0;
        for (String chArrs : chList) {
            for (String s : chArrs.split(",")) {
                }
            i++;
        }
        return ChannelType.NORMAL;
    }

    public static void main(String[] args) {
        startChannel_Main();
    }
}
