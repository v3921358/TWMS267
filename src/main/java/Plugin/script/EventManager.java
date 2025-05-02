package Plugin.script;

import Client.MapleCharacter;
import Config.constants.enums.UserChatMessageType;
import Plugin.script.binding.ScriptEvent;
import Plugin.script.binding.ScriptHelper;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import lombok.extern.slf4j.Slf4j;
import nativeimage.Reflection;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.io.IOAccess;

import javax.script.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Reflection(publicConstructors = true, publicMethods = true, publicFields = true, scanPackage = "Plugin.script")
public class EventManager {
    private static final AtomicInteger runningInstanceMapId = new AtomicInteger(0);
    private final String eventPath;
    private final int channel;

    public EventManager(String scriptPath, int channel, String[] activateNow) {
        this.eventPath = "/events/";
        this.channel = channel;
    }

    public static int getNewInstanceMapId() {
        return runningInstanceMapId.incrementAndGet();
    }

    public final ScriptEvent runScript(MapleCharacter player, String scriptName, boolean associateWithName, Object attachment) {

        ScriptEvent event;
        String scriptPath = null;
        try {
            if (player.isGm()) {
                player.dropSpouseMessage(UserChatMessageType.粉, "[event]" + eventPath + scriptName);
            }
            EventManipulator delegator;
            Path commonJsRoot = Paths.get(".").toAbsolutePath();
            ScriptEngine engine = GraalJSScriptEngine.create(
                    Engine.newBuilder()
                            .option("engine.WarnInterpreterOnly", "false")
                            .build(),
                    Context.newBuilder("js")
                            .allowHostAccess(HostAccess.ALL)
                            .allowHostClassLookup(s -> true)
                            .allowHostClassLoading(true)
                            .allowCreateThread(true)
                            .allowCreateProcess(true)
                            .allowAllAccess(true)
                            .allowNativeAccess(true)
                            .allowExperimentalOptions(true)
                            .allowValueSharing(true)
                            .allowIO(IOAccess.ALL)
                            .option("js.nashorn-compat", "true")
                            .option("js.ecmascript-version", "2022")
                            .option("js.commonjs-require", "true")
                            .option("js.commonjs-require-cwd", commonJsRoot.toString())
                            .option("js.syntax-extensions", "true")
                            .option("js.esm-eval-returns-exports", "true")
                            .option("js.strict", "false")
            );

//            engine.getContext().setAttribute(ScriptEngine.FILENAME, "script.mjs", ScriptContext.ENGINE_SCOPE);
            engine.getBindings(ScriptContext.ENGINE_SCOPE);
            var scriptString = ScriptManager.getScriptString(eventPath + scriptName);
            if (!scriptString.isEmpty()){
                CompiledScript cs = ((Compilable) engine).compile(scriptString);
                cs.eval();
                Invocable invocable = (Invocable) cs.getEngine();
                delegator = new EventManipulator(invocable);
                event = new ScriptEvent(associateWithName ? scriptName : null, (byte) channel, delegator, engine);
                delegator.setVariables(event.getVariables());
                ((ScriptEngine) invocable).put("event", event);
                ((ScriptEngine) invocable).put("sh", new ScriptHelper());

                invocable.invokeFunction("init", attachment);
            }else{
                if (player.isGm()) {
                    player.dropSpouseMessage(UserChatMessageType.粉, "[event] 未找到腳本 ：" + scriptPath);
                }
                log.error("error: 未找到腳本 {}",scriptPath);
                return null;
            }

        } catch (Exception e) {
            if (player.isGm()) {
                player.dropSpouseMessage(UserChatMessageType.粉, "[event] 未找到腳本 ：" + scriptPath);
            }
            log.error("error: runScript. {}", e);
            return null;
        }
        return event;
    }
}

