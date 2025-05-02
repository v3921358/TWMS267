package Plugin.script.binding;

import Net.server.Timer;
import Net.server.maps.MapleMap;
import Packet.UIPacket;
import Plugin.script.EventManager;
import Plugin.script.EventManipulator;
import Server.channel.ChannelServer;
import Server.world.WorldBroadcastService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.script.ScriptEngine;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ScriptEvent {

    private static final AtomicInteger runningInstanceMapId = new AtomicInteger(0);
    @Getter
    private final String scriptName;
    private final byte channel;
    private final Map<String, Object> variables;
    private final Map<String, ScheduledFuture<?>> timers;
    @Getter
    private final Map<Integer, ScriptField> fields;
    private final ScriptEngine globalScope;
    @Getter
    private final EventManipulator hooks;

    @Setter
    @Getter
    private boolean practice = false;

    @Getter
    @Setter
    private boolean practiceMode = false;

    @Getter
    private static final Timer timerInstance = new Timer();

    @Getter
    private final List<Integer> onFirstUserMapIds = new ArrayList<>();

    public ScriptEvent(String scriptName, byte channel, EventManipulator hooks, ScriptEngine globalScope) {
        this.globalScope = globalScope;
        this.scriptName = scriptName;
        this.channel = channel;
        this.hooks = hooks;
        variables = new ConcurrentHashMap<String, Object>();
        timers = new ConcurrentHashMap<String, ScheduledFuture<?>>();
        fields = new ConcurrentHashMap<Integer, ScriptField>();
    }


    protected EventManipulator getScriptInterface() {
        return hooks;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }

    public Object getVariable(String key) {
        return variables.get(key);
    }

    public void clearVariables() {
        variables.clear();
    }

    public ScriptField makeMap(int mapId) {
        int assignedid = EventManager.getNewInstanceMapId();
        MapleMap m = ChannelServer.getInstance(channel).getMapFactory().CreateInstanceMap(mapId, true, true, true, assignedid);
        ScriptField map = new ScriptField(m);
        fields.put(mapId, map);
        return map;
    }

    public void initMap(int[] mapIds){
        for(int mapId : mapIds){
            makeMap(mapId);
        }
    }

    public ScriptField getMap(int mapId) {
        return fields.get(mapId);
    }

    public void destroyMap(ScriptField map) {
        map.endFieldEvent();
        ChannelServer.getInstance(channel).getMapFactory().removeInstanceMap(map.getInstanceId());
        fields.remove(map.getId());
    }

    public void destroyMaps() {
        for (ScriptField map : this.fields.values()) {
            map.endFieldEvent();
            ChannelServer.getInstance(channel).getMapFactory().removeInstanceMap(map.getInstanceId());
        }
        fields.clear();
    }

    public void startTimer(final String key, int millisDelay) {
        synchronized (timers) {
            stopTimer(key);
            timers.put(key, getTimerInstance().schedule(() -> {
                synchronized (timers) {
                    timers.remove(key);
                }
                try {
                    hooks.timerExpired(key);
                } catch (Exception e) {
                    log.error("error: startTimer. {}", e.getMessage());
                }
            }, millisDelay));
        }
    }

    public void stopTimer(String key) {
        ScheduledFuture<?> future = timers.remove(key);
        if (future != null) {
            future.cancel(true);
        }
    }

    public void stopTimers() {
        for (ScheduledFuture<?> future : timers.values()) {
            future.cancel(true);
        }
        timers.clear();
    }

    public int getChannel() {
        return channel;
    }

    public void destroyEvent() {
        this.hooks.deinit();
        this.destroyMaps();
        this.stopTimers();
    }

    public void runScript(ScriptPlayer player, String scriptName, int npcId) {
        player.getPlayer().getScriptManager().startNpcScript(npcId, 0, scriptName);
    }

    public void broadcastWeatherEffectNotice(final String s, final int n, final int n2) {
        WorldBroadcastService.getInstance().broadcastMessage(UIPacket.showWeatherEffectNotice(s, n, n2, true));
    }

    public String getName() {
        return scriptName;
    }
}
