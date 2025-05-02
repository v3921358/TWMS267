package Plugin.script;

import Client.MapleCharacter;
import Net.server.life.MapleMonster;
import Net.server.maps.MapleMap;
import Packet.MaplePacketCreator;
import Plugin.script.binding.ScriptField;
import Plugin.script.binding.ScriptMob;
import Plugin.script.binding.ScriptParty;
import Plugin.script.binding.ScriptPlayer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nativeimage.Reflection;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import java.util.Map;

@Slf4j
@Reflection(publicConstructors = true, publicMethods = true, publicFields = true, scanPackage = "Plugin.script")
public class EventManipulator {

    private Map<String, Object> variables;

    @Getter
    private Invocable globalScope;

    public EventManipulator(Invocable  globalScope) {
        this.globalScope = globalScope;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public Object getVariable(String key) {
        return variables.get(key);
    }

    public void init(Object attachment) {
        try {
            if(((ScriptEngine) globalScope).get("init") == null){
                return;
            }
            globalScope.invokeFunction("init",attachment);
        } catch (Exception e) {
            log.error("error: EventManipulator, method name: playerDied. {}", e.getMessage());
        }
    }

    public void playerDied(MapleCharacter player) {
        try {
            if(((ScriptEngine) globalScope).get("playerDied") == null){
                return;
            }
            globalScope.invokeFunction("playerDied",new ScriptPlayer(player));
        } catch (Exception e) {
            log.error("error: EventManipulator, method name: playerDied. {}", e.getMessage());
        }
    }

    public void playerDisconnected(MapleCharacter player) {
        try {
            if(((ScriptEngine) globalScope).get("playerDisconnected") == null){
                return;
            }
            globalScope.invokeFunction("playerDisconnected",new ScriptPlayer(player));
        } catch (Exception e) {
            log.error("error: EventManipulator, method name: playerDisconnected. {}", e.getMessage());
        }
    }

    public void playerChangedMap(MapleCharacter player, MapleMap map) {
        if(((ScriptEngine) globalScope).get("playerChangedMap") == null){
            return;
        }
        try {
            player.send(MaplePacketCreator.practiceMode(player.getEventInstance().isPracticeMode()));
            globalScope.invokeFunction("playerChangedMap",new ScriptPlayer(player), new ScriptField(map));
        } catch (Exception e) {
            log.error("error: EventManipulator, method name: playerChangedMap. {}", e.getMessage());
        }
    }

    public void partyMemberDischarged(MapleCharacter player) {
        if(((ScriptEngine) globalScope).get("partyMemberDischarged") == null){
            return;
        }
        try {
            globalScope.invokeFunction("partyMemberDischarged",new ScriptParty(player.getParty()), new ScriptPlayer(player));
        } catch (Exception e) {
            log.error("error: EventManipulator, method name: partyMemberDischarged. {}", e.getMessage());
        }
    }

    public void mobDied(MapleMonster mob, MapleCharacter player) {
        if(((ScriptEngine) globalScope).get("mobDied") == null){
            return;
        }
        try {
            globalScope.invokeFunction("mobDied",new ScriptMob(mob), new ScriptPlayer(player));
        } catch (Exception e) {
            log.error("error: EventManipulator, method name: mobDied. {}", e.getMessage());
        }
    }

    public void timerExpired(String timerId) {
        if(((ScriptEngine) globalScope).get("timerExpired") == null){
            return;
        }
        try {
            globalScope.invokeFunction("timerExpired",timerId);
        } catch (Exception e) {
            log.error("error: EventManipulator, method name: timerExpired. {}", e.getMessage());
        }

    }

    public void playerHit(MapleCharacter player, MapleMonster mob, long damage) {
        if(((ScriptEngine) globalScope).get("playerHit") == null){
            return;
        }
        try {
            globalScope.invokeFunction("playerHit",new ScriptPlayer(player), new ScriptMob(mob), damage);
        } catch (Exception e) {
            log.error("error: EventManipulator, method name: playerHit. {}", e.getMessage());
        }

    }

    public void playerPickupItem(MapleCharacter player, int itemId) {
        if(((ScriptEngine) globalScope).get("playerPickupItem") == null){
            return;
        }
        try {
            globalScope.invokeFunction("playerPickupItem",new ScriptPlayer(player), itemId);
        } catch (Exception e) {
            log.error("error: EventManipulator, method name: playerPickupItem. {}", e.getMessage());
        }
    }

    public void deinit() {
        if(((ScriptEngine) globalScope).get("deinit") == null){
            return;
        }
        try {
            globalScope.invokeFunction("deinit");
        } catch (Exception e) {
            log.error("error: EventManipulator, method name: deinit. {}", e.getMessage());
        }

    }


}
