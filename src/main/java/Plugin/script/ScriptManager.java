package Plugin.script;

import Client.MapleCharacter;
import Client.inventory.Item;
import Config.configs.ServerConfig;
import Config.constants.enums.*;
import Net.server.MapleItemInformationProvider;
import Net.server.MaplePortal;
import Net.server.ScriptedItem;
import Net.server.life.MapleLifeFactory;
import Net.server.maps.MapleMap;
import Net.server.maps.MapleReactor;
import Net.server.maps.MapleReactorFactory;
import Net.server.quest.MapleQuest;
import Packet.UIPacket;
import Plugin.script.binding.*;
import SwordieX.client.character.Char;
import connection.packet.ScriptMan;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import nativeimage.Reflection;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.io.IOAccess;
import tools.AesUtil;
import tools.ComputerUniqueIdentificationUtil;
import tools.StringUtil;

import javax.script.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
@Slf4j
@Reflection(publicConstructors = true, publicMethods = true, publicFields = true, scanPackage = "Plugin.script")
public class ScriptManager {
    private static final String SCRIPT_PATH_PREFIX = ServerConfig.WORLD_SCRIPTSPATH + File.separator;
    private static final String ENCRYPTED_SCRIPT = ".jse";
    private static final String CUSTOM_SCRIPT = ".jsc";
    private static final String DEFAULT_SCRIPT = ".js";
    public static final String INTENDED_NPE_MSG = "Intended NPE by forceful Plugin.script stop.";
    private static final Lock fileReadLock = new ReentrantLock();
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final boolean isField;
    @Setter
    private NpcScriptInfo npcScriptInfo;
    private final Map<ScriptType, ScriptInfo> scripts = new HashMap<>();
    private ScriptType lastActiveScriptType;
    @Getter
    private final MapleCharacter chr;
    private final MapleMap field;
    @Getter
    private final ScriptMemory memory = new ScriptMemory();
    private final FieldTransferInfo fieldTransferInfo = new FieldTransferInfo();
    private final Map<String, String> bossUIMap = new HashMap<>();
    public ScriptManager(MapleCharacter chr, MapleMap field) {
        this.chr = chr;
        this.field = field;
        this.npcScriptInfo = new NpcScriptInfo();
        this.isField = chr == null;
        this.lastActiveScriptType = ScriptType.None;
        initBossUIMap();
    }
    public ScriptManager(Char chr) {
        this(chr.getCharacter());
    }
    public ScriptManager(MapleCharacter chr) {
        this(chr, chr.getMap());
    }
    public ScriptManager(MapleMap field) {
        this(null, field);
    }
    public static String getScriptString(String path) throws Exception {
        String script = "";
        File file = new File(getScriptPath(path));
        if (file.exists()) {
            script = Files.readString(file.toPath());
            if (file.getName().endsWith(ENCRYPTED_SCRIPT)) {
                script = AesUtil.decryptScriptByMachineHash(ComputerUniqueIdentificationUtil.getHashCache(), "MapleStory", script);
            }
            return script;
        }
        return null;
    }
    public static String getScriptPath(String scriptName) {
        String path = getScriptPath(scriptName, CUSTOM_SCRIPT);
        if (new File(path).exists()) {
            log.debug("使用自訂腳本:{}", path);
            return path;
        }
        path = getScriptPath(scriptName, DEFAULT_SCRIPT);
        if (new File(path).exists()) {
            log.debug("使用預設腳本:{}", path);
            return path;
        }
        path = getScriptPath(scriptName, ENCRYPTED_SCRIPT);
        if (new File(path).exists()) {
            log.debug("使用加密腳本:{}", path);
            return path;
        }
        log.debug("未找到腳本");
        return "";
    }
    private static String getScriptPath(String scriptName, String extension) {
        return SCRIPT_PATH_PREFIX + scriptName + extension;
    }
    public void initBossUIMap() {
        bossUIMap.put("0", "balog");
        bossUIMap.put("1", "zakum");
        bossUIMap.put("2", "hontale");
        bossUIMap.put("3", "hillah");
        bossUIMap.put("4", "pierre");
        bossUIMap.put("5", "banban");
        bossUIMap.put("6", "bloody");
        bossUIMap.put("7", "bellum");
        bossUIMap.put("8", "vanleon");
        bossUIMap.put("9", "akayrum");
        bossUIMap.put("10", "magnus");
        bossUIMap.put("11", "pinkbeen");
        bossUIMap.put("12", "shinas");
        bossUIMap.put("13", "suu");
        bossUIMap.put("14", "ursus");
        bossUIMap.put("15", "demian");
        bossUIMap.put("16", "beidler");
        bossUIMap.put("17", "ranmaru");
        bossUIMap.put("18", "princessno");
        bossUIMap.put("19", "lucid");
        bossUIMap.put("21", "caoong");
        bossUIMap.put("22", "papulatus");
        bossUIMap.put("23", "will");
        bossUIMap.put("24", "jinhillah");
        bossUIMap.put("25", "blackmage");
        bossUIMap.put("26", "dusk");
        bossUIMap.put("27", "dunkel");
        bossUIMap.put("28", "seren");
        bossUIMap.put("29", "slime");
        bossUIMap.put("30", "kalos");
        bossUIMap.put("31", "karing");
    }
    private Bindings getBindingsByType(ScriptType scriptType) {
        ScriptInfo si = getScriptInfoByType(scriptType);
        return si == null ? null : si.getBindings();
    }
    public ScriptInfo getScriptInfoByType(ScriptType scriptType) {
        return scripts.getOrDefault(scriptType, null);
    }
    public Invocable getInvocableByType(ScriptType scriptType) {
        return getScriptInfoByType(scriptType).getInvocable();
    }
    public int getParentIDByScriptType(ScriptType scriptType) {
        return getScriptInfoByType(scriptType) != null ? getScriptInfoByType(scriptType).getParentID() : 2007;
    }
    public String startScript(int parentID, String scriptName, ScriptType scriptType) {
        return startScript(parentID, 0, scriptName, scriptType, null);
    }

    //    public String startScript(int parentID, int objID, ScriptType scriptType) {
//        return startScript(parentID, objID, null, scriptType, null);
//    }
    public String startNpcScript(int parentID, int objID, String scriptName) {
        String scriptPath;
        int npcID = 0;
        if (parentID >= 0) {
            npcID = parentID;
        }
        if (scriptName != null && StringUtil.isNaturalNumber(scriptName)) {
            scriptName = npcID > 0 ? ("npc_" + npcID + "_" + scriptName) : ("expand_" + scriptName);
        }
        String wzScriptName = MapleLifeFactory.getNpcScriptName(npcID);
        if (wzScriptName == null && (scriptName == null || scriptName.isEmpty()))
            scriptName = "npc_" + npcID;
//        log.error("startNpcScript - "+wzScriptName);
        return startScript(npcID, objID, wzScriptName, ScriptType.Npc, scriptName);
    }
    public String startBossUIScript(int parentID, String bossId, String difficulty) {
        int npcID = 0;
        if (parentID >= 0) {
            npcID = parentID;
        }
        String bossName = bossUIMap.get(bossId);
//        Map<String, String> BossUIObj = Map.of(bossName, difficulty);
        return startScript(npcID, 0, bossName, ScriptType.BossUI, difficulty);
    }
    public String startItemScript(Item item, int parentID, String scriptName) {
        if (item == null) {
            return "";
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        ScriptedItem info = ii.getScriptedItemInfo(item.getItemId());
        int npcID = 0;
        if (parentID >= 0) {
            npcID = parentID;
        } else if (info != null) {
            npcID = info.getNpc();
        }
        if (scriptName == null || scriptName.isEmpty()) {
            scriptName = info == null ? null : info.getScript();
            if (scriptName == null || scriptName.isEmpty()) {
                switch (item.getItemId() / 1000000) {
                    case 1:
                        scriptName = "equip_";
                        break;
                    case 2:
                        scriptName = "consume_";
                        break;
                    case 3:
                        scriptName = "install_";
                        break;
                    case 4:
                        scriptName = "etc_";
                        break;
                    case 5:
                        scriptName = "cash_";
                        break;
                    default:
                        scriptName = "item_";
                        break;
                }
                scriptName += item.getItemId();
            }
        }
        return startScript(npcID, 0, scriptName, ScriptType.Item, item);
    }
    public String startQuestSScript(int parentID, int questId) {
        return startQuestScript(parentID, questId, true);
    }
    public String startQuestEScript(int parentID, int questId) {
        return startQuestScript(parentID, questId, false);
    }
    private String startQuestScript(int parentID, int questId, boolean isStartScript) {
        MapleQuest quest = MapleQuest.getInstance(questId);
        String scriptName = "";
        if (quest != null)
            scriptName = isStartScript ? quest.getStartScript() : quest.getEndScript();
        if (scriptName.isEmpty()) scriptName = "q" + questId + (isStartScript ? "s" : "e");
        return startScript(parentID, 0, scriptName, isStartScript ? ScriptType.QuestStart : ScriptType.QuestEnd, questId);
    }
    public String startPortalScript(MaplePortal portal) {
        return startScript(0, 0, portal.getScriptName(), ScriptType.Portal, portal);
    }
    public String startOnUserScript(String scriptName) {
        return startScript(0, 0, scriptName, ScriptType.onUserEnter, scriptName);
    }
    public String startOnFirstUserScript(String scriptName) {
        if (chr.getEventInstance() != null) {
            if (!chr.getEventInstance().getOnFirstUserMapIds().contains(chr.getMapId())) {
                chr.getEventInstance().getOnFirstUserMapIds().add(chr.getMapId());
                return startScript(0, 0, scriptName, ScriptType.onFirstUserEnter, scriptName);
            }
            return "";
        } else {
            return startScript(0, 0, scriptName, ScriptType.onFirstUserEnter, scriptName);
        }
    }
    public String startRandomPortalScript(int parentID, int objID, String scriptName) {
        return startScript(parentID, objID, scriptName, ScriptType.Npc, null);
    }
    public String startReactorScript(MapleReactor reactor) {
        int id = reactor.getReactorId();
        String scriptName = MapleReactorFactory.getAction(id);
        if (scriptName == null || scriptName.isEmpty()) {
            scriptName = "reactor_" + id;
        }
        return startScript(reactor.getReactorId(), reactor.getObjectId(), scriptName, ScriptType.Reactor, reactor);
    }
    public String startCommandScript(String[] line, int parentID, String scriptName) {
        return startScript(parentID, 0, scriptName, ScriptType.Command, line);
    }
    private String startScript(int parentID, int objID, String scriptName, ScriptType scriptType, Object obj) {
        if (scriptName == null || scriptName.isEmpty()) {
            return "";
        }
        if (scriptType == ScriptType.None/* || (scriptType == ScriptType.Quest && !isQuestScriptAllowed())*/) {
            log.error(String.format("Did not allow Plugin.script %s to go through (type %s)  |  Active Script Type: %s", scriptName, scriptType, getLastActiveScriptType()));
            return "";
        }
        if (isActive(scriptType) && (scriptType != ScriptType.Map && scriptType != ScriptType.onFirstUserEnter)) { // because Field Scripts don't get disposed.
            return "您當前已經開啟對話. 如果不是請輸入 @ea 命令進行解卡。";
        }
        setLastActiveScriptType(scriptType);
        ScriptEngine engine = GraalJSScriptEngine.create(
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
//                        .option("js.commonjs-require", "true")
//                        .option("js.commonjs-require-cwd", commonJsRoot.toString())
                        .option("js.syntax-extensions", "true")
//                        .option("js.esm-eval-returns-exports", "true")
                        .option("js.strict", "false")
        );
        engine.getContext().setAttribute(ScriptEngine.FILENAME, "script.mjs", ScriptContext.ENGINE_SCOPE);
        getNpcScriptInfo().setParam(0);
        Bindings bindings = engine.createBindings();
        bindings.put("party", getChr().getParty() == null ? null : new ScriptParty(getChr().getParty()));
        bindings.put("player", new ScriptPlayer(getChr()));
        bindings.put("map", new ScriptField(getChr().getMap()));
        bindings.put("sh", new ScriptHelper());
        switch (String.valueOf(scriptType)) {
            case "Portal" -> bindings.put("portal", new ScriptPortal(getChr().getClient(), (MaplePortal) obj));
            case "Reactor" -> {
                getNpcScriptInfo().setObjectID(((MapleReactor) obj).getObjectId());
                bindings.put("reactor", new ScriptReactor(getChr().getClient(), (MapleReactor) obj));
            }
            case "onFirstUserEnter" -> {
                getNpcScriptInfo().setTemplateID(parentID);
                bindings.put("npc", new ScriptNpc(getChr().getClient(), parentID, scriptName, ScriptType.Npc, obj));
            }
            case "BossUI" -> {
//                Map<String, String> BossUIObj = (Map<String, String>) obj;
//                bindings.put("index",BossUIObj.get("index"));
//                bindings.put("difficulty",BossUIObj.get("difficulty"));
                bindings.put("difficulty", Integer.parseInt((String) obj));
                getNpcScriptInfo().setTemplateID(parentID);
                bindings.put("npc", new ScriptNpc(getChr().getClient(), parentID, scriptName, ScriptType.Npc, obj));
            }
            case "Item", "Npc", "QuestStart", "QuestEnd", "Command", "onUserEnter" -> {
                getNpcScriptInfo().setTemplateID(parentID);
                bindings.put("npc", new ScriptNpc(getChr().getClient(), parentID, scriptName, scriptType, obj));
            }
        }
        String scriptPath;
        if (scriptType == ScriptType.Npc && obj instanceof String && !((String) obj).isEmpty()) {
            scriptPath = String.format("expands" + File.separator + "%s", obj);
        } else {
            scriptPath = String.format("%s" + File.separator + "%s", scriptType.getDir(), scriptName);
        }
        getNpcScriptInfo().setObjectID(parentID);
        String result = scriptPath;
        UserChatMessageType messageColor = UserChatMessageType.青;
        try {
            fileReadLock.lock();
            String scriptString = getScriptString(scriptPath);
            if (scriptString == null) throw new FileNotFoundException();
            ScriptInfo scriptInfo = new ScriptInfo(scriptType, bindings, parentID, scriptPath);
            scriptInfo.setObjectID(objID);
            getScripts().put(scriptType, scriptInfo);
            // 异步执行腳本
            CompletableFuture.runAsync(() -> startScript(engine, scriptString, scriptPath, scriptType));
        } catch (IOException ex) {
            messageColor = UserChatMessageType.粉;
            result = "";
        }catch (Exception e) {
            String firstLine = e.getMessage().split("\n")[0];
            if (!firstLine.contains(INTENDED_NPE_MSG)) {
                if (chr.isGm()) {
                    chr.dropSpouseMessage(UserChatMessageType.粉, "[" + scriptType + "] " + scriptPath + " " + firstLine);
                }
                e.printStackTrace();
            }
        } finally {
            fileReadLock.unlock();
        }
        if (!isField()) {
            if (chr.isGm() && !chr.getScriptManagerDebug().contains(scriptType.getDir())) {
                switch (scriptType) {
                    case ScriptType.Npc -> chr.dropSpouseMessage(messageColor, "[Npc] npcs/" + scriptName +".js");
                    case ScriptType.QuestEnd, ScriptType.QuestStart -> chr.dropSpouseMessage(messageColor, "[Quest] quests/" + scriptName + ".js");
                    case ScriptType.Item -> chr.dropSpouseMessage(messageColor, "[Item] items/" + scriptName +".js");
                    case ScriptType.onFirstUserEnter -> chr.dropSpouseMessage(messageColor, "[onFirstUserEnter] maps/onFirstUserEnter/" + scriptName +".js");
                    case ScriptType.onUserEnter -> chr.dropSpouseMessage(messageColor, "[onUserEnter] maps/onUserEnter/" + scriptName +".js");
                    case ScriptType.Command -> chr.dropSpouseMessage(messageColor, "[Command] commands/" + scriptName +".js");
                    case ScriptType.Reactor -> chr.dropSpouseMessage(messageColor, "[Reactor] reactors/" + scriptName +".js");
                    case ScriptType.Portal -> chr.dropSpouseMessage(messageColor, "[Portal] portals/" + scriptName +".js");
                }
                System.err.println("[" + scriptType + "] " + scriptPath+".js");
            }
        }
        return result;
    }
    public String processSource(String data) {
        return data
                // require('xxx') 預設用 .js 執行，要是帶入副檔名 .jse .jsc .js 就自動帶入執行
                .replaceAll("(require\\([\"'])([^\"']+)(?<!\\.js)(?<!\\.jse)(?<!\\.jsc)([\"']\\))", "$1$2.js$3")
                .replaceAll("(require\\([\"'])([^\"']+[.jse|.jsc|.js])([\"']\\))", "$1$2$3")
                // import {xxx} from 'xxx' 預設用 .js 執行，要是帶入副檔名 .jse .jsc .js 就自動帶入執行
                .replaceAll("import +\\{(.+)} +from +(['\"][^'\"]+)(?<!\\.js)(?<!\\.jse)(?<!\\.jsc)(['\"])$", "const {$1} = require($2.js$3)")
                .replaceAll("import +\\{(.+)} +from +(['\"][^'\"]+[.jse|.jsc|.js])(['\"])", "const {$1} = require($2$3)")
                // import * as xxx from 'xxx' 預設用 .js 執行，要是帶入副檔名 .jse .jsc .js 就自動帶入執行
                .replaceAll("import +\\* +as +(\\w+) +from +(['\"][^'\"]+)(?<!\\.js)(?<!\\.jse)(?<!\\.jsc)(['\"])", "const $1 = require($2.js$3)")
                .replaceAll("import +\\* +as +(\\w+) +from +(['\"][^'\"]+[.jse|.jsc|.js])(['\"])", "const $1 = require($2$3)")
                // import xxx from 'xxx' 預設用 .js 執行，要是帶入副檔名 .jse .jsc .js 就自動帶入執行
                .replaceAll("import +(\\w+) +from +(['\"][^'\"]+)(?<!\\.js)(?<!\\.jse)(?<!\\.jsc)(['\"])", "const $1 = require($2.js$3)")
                .replaceAll("import +(\\w+) +from +(['\"][^'\"]+[.jse|.jsc|.js])(['\"])", "const $1 = require($2$3)");
    }

    private void startScript(ScriptEngine engine, String data, String name, ScriptType scriptType) {
        ScriptInfo si = getScriptInfoByType(scriptType);
        si.setActive(true);
        if (chr != null) {
            chr.setConversation(ConversationType.TALK_TO_NPC);
            if (chr.getClient() != null)
                chr.getClient().setClickedNPC();
        }
        CompiledScript cs;
        Bindings bindings = getBindingsByType(scriptType);
        si.setBindings(bindings);
        si.setInvocable((Invocable) engine);
        try {
//            String ss = processSource(data);
            cs = ((Compilable) engine).compile(data);
            cs.eval(bindings);
            si.setInvocable((Invocable) engine);
        } catch (Exception e) {
            String firstLine = e.getMessage().split("\n")[0];
            if (!firstLine.contains(INTENDED_NPE_MSG)) {
                if (chr.isGm()) {
                    chr.dropSpouseMessage(UserChatMessageType.粉, "[" + scriptType + "] " + name + " " + firstLine);
                }
                e.printStackTrace();
                lockInGameUI(false); // so players don't get stuck if a Plugin.script fails
            }
        } finally {
//            if (si.isActive() && name.equals(si.getScriptName()) &&
//                    ((scriptType != ScriptType.Map && scriptType != ScriptType.onUserEnter && scriptType != ScriptType.onFirstUserEnter)
//                            || (chr != null && chr.getMapId() == si.getParentID()))) {
            if (si.isActive() && name.equals(si.getScriptName()) && chr != null) {
                stop(scriptType);
            }
            FieldTransferInfo fti = getFieldTransferInfo();
            if (!fti.isInit()) {
                if (fti.isField()) {
                    fti.warp(field);
                } else {
                    fti.warp(chr);
                }
            }
        }
    }
    public void stop(ScriptType scriptType) {
        NpcScriptInfo nsi = getNpcScriptInfo();
        nsi.removeParam(ScriptParam.PlayerAsSpeaker);
        boolean isNotCancellable = nsi.hasParam(ScriptParam.NoEsc);
        nsi.setTemplateID(0);
        if (isNotCancellable) {
            nsi.addParam(ScriptParam.NoEsc);
        }
        if (getLastActiveScriptType() == scriptType) {
            setLastActiveScriptType(ScriptType.None);
        }
        ScriptInfo si = getScriptInfoByType(scriptType);
        if (si != null) {
            si.reset();
        }
        npcScriptInfo = new NpcScriptInfo();
        getMemory().clear();
        if (chr != null) {
            chr.dispose();
            chr.setConversation(ConversationType.NONE);
            if (chr.getClient() != null)
                chr.getClient().removeClickedNPC();
        }
    }
    public void handleAction(ScriptType scriptType, NpcMessageType lastType, int response, long answer, String text) {
        switch (response) {
            case -1:
            case 5:
                stop(scriptType);
                break;
            default:
                ScriptMemory sm = getMemory();
                if (sm.get().isPrevPossible() && response == 0) {
                    // back button pressed
                    NpcScriptInfo prev = sm.decrementAndGet();
                    chr.write(ScriptMan.scriptMessage(prev, prev.getMessageType()));
                } else {
                    if (sm.hasNext()) {
                        NpcScriptInfo next = sm.incrementAndGet();
                        chr.write(ScriptMan.scriptMessage(next, next.getMessageType()));
                    } else {
                        ScriptInfo si = getScriptInfoByType(scriptType);
                        if (isActive(scriptType)) {
                            switch (lastType.getResponseType()) {
                                case Response:
                                    si.addResponse((long) response);
                                    break;
                                case Answer:
                                    si.addResponse(answer);
                                    break;
                                case Text:
                                    si.addResponse(text);
                                    break;
                            }
                        }
                    }
                }
        }
    }
    public boolean isActive(ScriptType scriptType) {
        return getScriptInfoByType(scriptType) != null && getScriptInfoByType(scriptType).isActive();
    }
    public NpcScriptInfo getNpcScriptInfo() {
        return npcScriptInfo;
    }
    public Map<ScriptType, ScriptInfo> getScripts() {
        return scripts;
    }
    public int getParentID() {
        int res = 0;
        for (ScriptType type : ScriptType.values()) {
            if (getScriptInfoByType(type) != null) {
                res = getScriptInfoByType(type).getParentID();
            }
        }
        return res;
    }
    public boolean isField() {
        return isField;
    }
    public MapleMap getField() {
        return field;
    }
    public ScriptType getLastActiveScriptType() {
        return lastActiveScriptType;
    }
    public void setLastActiveScriptType(ScriptType lastActiveScriptType) {
        this.lastActiveScriptType = lastActiveScriptType;
    }
    public FieldTransferInfo getFieldTransferInfo() {
        return fieldTransferInfo;
    }
    public void lockInGameUI(boolean lock) {
        lockInGameUI(lock, true);
    }
    public void lockInGameUI(boolean lock, boolean blackFrame) {
        if (chr != null) {
            chr.send(UIPacket.SetInGameDirectionMode(lock, blackFrame, false, !lock));
        }
    }
    public void dispose() {
        dispose(false);
    }
    public void dispose(boolean stop) {
        if (chr != null) {
            chr.getClient().removeClickedNPC();
        }
        npcScriptInfo = new NpcScriptInfo();
        getMemory().clear();
        ScriptType st = getLastActiveScriptType();
        stop(st);
        dispose(st);
        if (stop) {
            throw new NullPointerException(INTENDED_NPE_MSG); // makes the underlying Plugin.script stop
        }
    }
    public void dispose(ScriptType scriptType) {
        getMemory().clear();
        stop(scriptType);
    }
}