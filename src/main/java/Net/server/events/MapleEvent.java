package Net.server.events;

import Client.MapleCharacter;
import Config.constants.MapConstants;
import Net.server.MapleInventoryManipulator;
import Net.server.Timer.EventTimer;
import Net.server.maps.FieldLimitType;
import Net.server.maps.MapleMap;
import Net.server.maps.SavedLocationType;
import Packet.MaplePacketCreator;
import Server.channel.ChannelServer;
import Server.world.WorldBroadcastService;
import connection.packet.FieldPacket;
import SwordieX.field.ClockPacket;
import tools.DateUtil;
import tools.Randomizer;
import tools.StringUtil;

public abstract class MapleEvent {

    protected final MapleEventType type;
    protected final int channel;
    protected int playerCount = 0;
    protected boolean isRunning = false;

    public MapleEvent(int channel, MapleEventType type) {
        this.channel = channel;
        this.type = type;
    }

    public static void givePrize(MapleCharacter chr) {
        int reward = Randomizer.rand(0,5); // 組滿五人
        if (reward == 0) {
            int mes = Randomizer.nextInt(900000) + 100000;
            chr.gainMeso(mes, true, false);
            chr.dropMessage(5, "你獲得了 " + mes + " 楓幣.");
        } else if (reward == 1) {
            int cs = Randomizer.nextInt(50) + 50;
            chr.modifyCSPoints(2, cs, true);
            chr.dropMessage(5, "你獲得了 " + cs + " 點楓點.");
        } else if (reward == 2) {
            int fe = Randomizer.nextInt(5) + 1;
            chr.addFame(fe);
            chr.dropMessage(5, "你獲得了 " + fe + " 點人氣.");
        } else if (reward == 3) {
            chr.dropMessage(5, "你悲劇了，什麼也沒得到.");
        } else {
            int max_quantity = 1;
            switch (reward) {
                case 5062000: //編號:　5062000  名稱:　神奇方塊
                    max_quantity = 1;
                    break;
                case 5220040: //編號:　5220040  名稱:　楓之谷轉蛋券
                    max_quantity = 1;
                    break;
                case 5062002: //編號:　5062002  名稱:　高級神奇方塊
                    max_quantity = 1;
                    break;
            }
            final int quantity = (max_quantity > 1 ? Randomizer.nextInt(max_quantity) : 0) + 1;
            if (MapleInventoryManipulator.checkSpace(chr.getClient(), reward, quantity, "")) {
                MapleInventoryManipulator.addById(chr.getClient(), reward, quantity, "活動獲得 " + DateUtil.getNowTime());
            } else {
                givePrize(chr); //do again until they get
            }
        }
    }

    public static void setEvent(ChannelServer cserv, boolean auto) {
        if (auto && cserv.getEvent() > -1) {
            for (MapleEventType t : MapleEventType.values()) {
                final MapleEvent e = cserv.getEvent(t);
                if (e.isRunning) {
                    for (int i : e.type.mapids) {
                        if (cserv.getEvent() == i) {
                            WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.serverNotice(0, "活動入口已關閉!"));
                            e.broadcast(MaplePacketCreator.serverNotice(0, " 活動將在30秒後開始!"));
                            e.broadcast(FieldPacket.clock(ClockPacket.secondsClock(30)));
                            EventTimer.getInstance().schedule(e::startEvent, 30000);
                            break;
                        }
                    }
                }
            }
        }
        cserv.setEvent(-1);
    }

    public static void mapLoad(MapleCharacter chr, int channel) {
        if (chr == null) {
            return;
        } //o_o
        for (MapleEventType t : MapleEventType.values()) {
            final MapleEvent e = ChannelServer.getInstance(channel).getEvent(t);
            if (e.isRunning) {
                if (chr.getMapId() == 109050000) { //finished map
                    e.finished(chr);
                }
                for (int i = 0; i < e.type.mapids.length; i++) {
                    if (chr.getMapId() == e.type.mapids[i]) {
                        e.onMapLoad(chr);
                        if (i == 0) { //first map
                            e.incrementPlayerCount();
                        }
                    }
                }
            }
        }
    }

    public static void onStartEvent(MapleCharacter chr) {
        for (MapleEventType t : MapleEventType.values()) {
            MapleEvent e = chr.getClient().getChannelServer().getEvent(t);
            if (e.isRunning) {
                for (int i : e.type.mapids) {
                    if (chr.getMapId() == i) {
                        e.startEvent();
                        setEvent(chr.getClient().getChannelServer(), false);
                        chr.dropMessage(5, t.desc + " 已經開始了!");
                    }
                }
            }
        }
    }

    public static String scheduleEvent(MapleEventType event, ChannelServer cserv) {
        if (cserv.getEvent() != -1 || cserv.getEvent(event) == null) {
            return "[提示] 活動不能進行設置,活動已經進行.";
        }
        for (int i : cserv.getEvent(event).type.mapids) {
            if (cserv.getMapFactory().getMap(i).getCharactersSize() > 0) {
                return "[提示] 活動已經開始或者活動地圖有玩家存在！";
            }
        }
        cserv.setEvent(cserv.getEvent(event).type.mapids[0]);
        cserv.getEvent(event).reset();
        WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.serverNotice(0, "Hello " + cserv.getServerName() + "! " + StringUtil.makeEnumHumanReadable(event.desc) + "活動在 " + cserv.getChannel() + "頻道開始了，快來參加吧！"));
        return "";
    }

    public void incrementPlayerCount() {
        playerCount++;
        if (playerCount == 250) {
            setEvent(ChannelServer.getInstance(channel), true);
        }
    }

    public MapleEventType getType() {
        return type;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public MapleMap getMap(int i) {
        return getChannelServer().getMapFactory().getMap(type.mapids[i]);
    }

    public ChannelServer getChannelServer() {
        return ChannelServer.getInstance(channel);
    }

    public void broadcast(byte[] packet) {
        for (int i = 0; i < type.mapids.length; i++) {
            getMap(i).broadcastMessage(packet);
        }
    }

    public abstract void finished(MapleCharacter chr); //most dont do shit here

    public abstract void startEvent();

    public void onMapLoad(MapleCharacter chr) { //most dont do shit here
        if (MapConstants.isEventMap(chr.getMapId()) && FieldLimitType.WEDDINGINVITATIONLIMIT.check(chr.getMap().getFieldLimit()) && FieldLimitType.ANTIMACROLIMIT.check(chr.getMap().getFieldLimit())) {
            chr.send(MaplePacketCreator.showEventInstructions());
        }
    }

    public void warpBack(MapleCharacter chr) {
        int map = chr.getSavedLocation(SavedLocationType.EVENT);
        if (map <= -1) {
            map = 104000000;
        }
        final MapleMap mapp = chr.getClient().getChannelServer().getMapFactory().getMap(map);
        chr.changeMap(mapp, mapp.getPortal(0));
    }

    public void reset() {
        isRunning = true;
        playerCount = 0;
    }

    public void unreset() {
        isRunning = false;
        playerCount = 0;
    }
}
