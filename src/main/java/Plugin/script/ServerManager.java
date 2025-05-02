package Plugin.script;

import Plugin.script.binding.ScriptHelper;
import Plugin.script.binding.ScriptServer;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nativeimage.Reflection;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.io.IOAccess;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Setter
@Getter
@Slf4j
@Reflection(publicConstructors = true, publicMethods = true, publicFields = true, scanPackage = "Plugin.script")
public class ServerManager {
    private final ConcurrentMap<String, ScriptServer> activePlugins;

    private static final String DIRECTORY_PATH = "/server/";

    public ServerManager() {
        activePlugins = new ConcurrentHashMap<>();
    }

    private static String getFileNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex > 0) ? fileName.substring(0, lastDotIndex) : fileName;
    }

    public void initialize(int channelId) {
        File directory = new File(DIRECTORY_PATH);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    ScriptServer server = runScript(DIRECTORY_PATH + file.getName(), channelId);
                    if (server != null) {
                        activePlugins.put(getFileNameWithoutExtension(file.getName()), server);
                    }
                }
            }
        }
    }

    @SneakyThrows
    public ScriptServer runScript(String scriptName, int attachment) {
            ScriptEngine engine = createGraalJSScriptEngine();
            var scriptString = ScriptManager.getScriptString(scriptName);
            if (scriptString != null && !scriptString.isEmpty()) {
                CompiledScript cs = ((Compilable) engine).compile(scriptString);
                cs.eval();
                Invocable invocable = (Invocable) cs.getEngine();
                ServerManipulator delegator = new ServerManipulator(invocable);
                ScriptServer server = new ScriptServer(delegator, invocable, attachment);
                ScriptHelper sh = new ScriptHelper();
                engine.put("server", server);
                engine.put("sh", sh);
                invocable.invokeFunction("init", attachment);
                return server;
        }
        return null;
    }

    private ScriptEngine createGraalJSScriptEngine() {
        Path commonJsRoot = Paths.get(".").toAbsolutePath();
        return GraalJSScriptEngine.create(
                Engine.newBuilder()
                        .option("engine.WarnInterpreterOnly", "false")
                        .build(),
                Context.newBuilder("js")
                        .allowHostAccess(HostAccess.ALL)
                        .allowHostClassLookup(s -> true)
                        .allowHostClassLoading(true)
                        .allowAllAccess(true)
                        .allowNativeAccess(true)
                        .allowCreateThread(true)
                        .allowCreateProcess(true)
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
    }
}