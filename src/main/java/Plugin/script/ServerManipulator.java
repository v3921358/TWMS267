package Plugin.script;

import Client.MapleCharacter;
import Net.server.maps.MapleMap;
import Plugin.script.binding.ScriptField;
import Plugin.script.binding.ScriptPlayer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nativeimage.Reflection;

import javax.script.Invocable;
import javax.script.ScriptEngine;

/**
 *
 * server/*.js 調用的hooks函數
 *
 */

@Slf4j
@Reflection(publicConstructors = true, publicMethods = true, publicFields = true, scanPackage = "Plugin.script")
public class ServerManipulator {

    @Getter
    private Invocable globalScope;

    public ServerManipulator(Invocable globalScope) {
        this.globalScope = globalScope;
    }

    public void timerExpired(String timerId) {
        if(((ScriptEngine) globalScope).get("timerExpired") == null){
            return;
        }
        try {
            globalScope.invokeFunction("timerExpired",timerId);
        } catch (Exception e) {
            log.error("error: EventManipulator, method name: timerExpired. {}", e);
        }
    }

    public void playerConnected(MapleCharacter player) {
        if(((ScriptEngine) globalScope).get("playerConnected") == null){
            return;
        }
        try {
            globalScope.invokeFunction("playerConnected",new ScriptPlayer(player));
        } catch (Exception e) {
            log.error("error: EventManipulator, method name: playerConnected. {}", e);
        }
    }

    public void playerDisconnected(MapleCharacter player) {
        if(((ScriptEngine) globalScope).get("playerDisconnected") == null){
            return;
        }
        try {
            globalScope.invokeFunction("playerDisconnected",new ScriptPlayer(player));
        } catch (Exception e) {
            log.error("error: EventManipulator, method name: playerDisconnected. {}", e);
        }
    }

    public void playerChangedMap(MapleCharacter player, MapleMap map) {
        if(((ScriptEngine) globalScope).get("playerChangedMap") == null){
            return;
        }
        try {
            globalScope.invokeFunction("playerChangedMap",new ScriptPlayer(player), new ScriptField(map));
        } catch (Exception e) {
            log.error("error: EventManipulator, method name: playerChangedMap. {}", e);
        }
    }

    public void deinit() {
        if(((ScriptEngine) globalScope).get("deinit") == null){
            return;
        }
        try {
            globalScope.invokeFunction("deinit");
        } catch (Exception e) {
            log.error("error: EventManipulator, method name: deinit. {}", e);
        }

    }


}
