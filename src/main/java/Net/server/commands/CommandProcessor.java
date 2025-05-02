package Net.server.commands;

import Client.MapleCharacter;
import Client.MapleClient;
import Config.configs.ServerConfig;
import Config.constants.enums.BroadcastMessageType;
import Config.constants.enums.UserChatMessageType;
import Database.DatabaseLoader.DatabaseConnectionEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Pair;

import java.io.File;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class CommandProcessor {

    private static final Logger log = LoggerFactory.getLogger("CommandProcessor");
    private static final HashMap<Integer, HashMap<String, CommandObject>> commands = new HashMap<>();
    private static final HashMap<Integer, HashMap<String, Pair<String, String>>> commandScripts = new HashMap<>();

    static {

        Class<?>[] CommandFiles = {
                PlayerCommand.class,
                InternCommand.class,
                GMCommand.class,
                SuperGMCommand.class,
                AdminCommand.class
        };

        for (Class<?> clasz : CommandFiles) {
            try {
                PlayerRank rankNeeded = (PlayerRank) clasz.getMethod("getPlayerLevelRequired").invoke(null, (Object[]) null);
                Class<?>[] a = clasz.getDeclaredClasses();
                HashMap<String, CommandObject> cM = new HashMap<>();
                for (Class<?> c : a) {
                    try {
                        if (!Modifier.isAbstract(c.getModifiers()) && !c.isSynthetic()) {
                            Object o = c.getDeclaredConstructor().newInstance();
                            boolean enabled;
                            try {
                                enabled = c.getDeclaredField("enabled").getBoolean(c.getDeclaredField("enabled"));
                            } catch (NoSuchFieldException ex) {
                                enabled = true; //Enable all coded commands by default.
                            }
                            if (o instanceof CommandExecute && enabled) {
                                cM.put(rankNeeded.getCommandPrefix() + c.getSimpleName().toLowerCase(), new CommandObject((CommandExecute) o, rankNeeded.getLevel()));
                            }
                        }
                    } catch (Exception ex) {
                        log.error("CommandProcessor", ex);
                    }
                }
                commands.put(rankNeeded.getLevel(), cM);
            } catch (Exception ex) {
                log.error("CommandProcessor", ex);
            }
        }

        loadScript();
    }

    public static void loadScript() {
        commandScripts.clear();
        for (PlayerRank rank : PlayerRank.values()) {
            HashMap<String, Pair<String, String>> cM = new HashMap<>();
            File cmdfiles = new File(ServerConfig.WORLD_SCRIPTSPATH + "/commands/" + rank.getFolderName());
            checkScript(cM, cmdfiles, rank.getCommandPrefix(), rank.getFolderName());
            cmdfiles = new File(ServerConfig.WORLD_SCRIPTSPATH + "/command/" + rank.getFolderName());
            checkScript(cM, cmdfiles, rank.getCommandPrefix(), rank.getFolderName());
            cmdfiles = new File(ServerConfig.WORLD_SCRIPTSPATH2 + "/commands/" + rank.getFolderName());
            checkScript(cM, cmdfiles, rank.getCommandPrefix(), rank.getFolderName());
            cmdfiles = new File(ServerConfig.WORLD_SCRIPTSPATH2 + "/command/" + rank.getFolderName());
            checkScript(cM, cmdfiles, rank.getCommandPrefix(), rank.getFolderName());
            if (!cM.isEmpty()) {
                commandScripts.put(rank.getLevel(), cM);
            }
        }
    }

    private static void checkScript(HashMap<String, Pair<String, String>> cM, File cmdfiles, char cp, String folderName) {
        if (!cmdfiles.exists() || !cmdfiles.isDirectory()) {
            return;
        }
        for (String s : Objects.requireNonNull(cmdfiles.list())) {
            if (s.toLowerCase().endsWith(".js") || s.toLowerCase().endsWith(".jse") || s.toLowerCase().endsWith(".jsc")) {
                String cmd = s.substring(0, s.indexOf("."));
                String cmd1;
                String cmd2;
                if (cmd.contains("_")) {
                    cmd1 = cmd.substring(0, cmd.indexOf("_"));
                    cmd2 = cmd.substring(cmd.indexOf("_") + 1);
                    if (cmd1.isEmpty()) {
                        cmd1 = "_" + cmd2;
                    } else if (cmd2.isEmpty()) {
                        cmd1 += "_";
                    }
                    cmd1 = cp + cmd1.toLowerCase();
                    if (cmd2.isEmpty()) {
                        cmd2 = null;
                    } else {
                        cmd2 = cmd2.toLowerCase();
                    }
                } else {
                    cmd1 = cp + cmd.toLowerCase();
                    cmd2 = null;
                }
                if (!cM.containsKey(cmd1)) {
                    cM.put(cmd1, new Pair<>(cmd2, folderName + "/" + cmd));
                }
            }
        }

    }

    private static void sendDisplayMessage(MapleClient c, String msg, CommandType type) {
        if (c.getPlayer() == null) {
            return;
        }
        switch (type) {
            case NORMAL:
                c.getPlayer().dropMessage(6, msg);
                break;
            case TRADE:
                c.getPlayer().dropMessage(-2, "錯誤 : " + msg);
                break;
        }

    }

    public static void dropHelp(MapleClient c, boolean showAll) {
        if (!c.isGm()) {
            return;
        }
        c.getPlayer().dropSpouseMessage(UserChatMessageType.頻道喇叭, "指令表: ");
        if (showAll) {
            for (int i = PlayerRank.普通.getLevel(); i <= PlayerRank.MVP紅鑽.getLevel(); i++) {
                dropCommandList(c, i);
            }
        }
        for (int i = 1; i <= c.getPlayer().getGmLevel(); i++) {
            dropCommandList(c, i + PlayerRank.實習管理員.getLevel() - 1);
        }
    }

    public static void dropCommandList(MapleClient c, int level) {
        dropCommandList(c, level, "");
    }

    public static void dropCommandList(MapleClient c, int level, String search) {
        String commandList = getCommandsForLevel(level, search);
        if (commandList == null) {
            return;
        }
        final PlayerRank pRank = PlayerRank.getByLevel(level);
        String comment = "";
        if (c.isGm()) {
            comment += "/,";
        }
        comment += pRank.getCommandPrefix();
        if (pRank.getFullWidthCommandPrefix() != null) {
            comment += "," + pRank.getFullWidthCommandPrefix();
        }
        c.getPlayer().dropSpouseMessage(UserChatMessageType.方塊洗洗樂, "[" + pRank.name() + "指令] 前綴\"" + comment + "\" : ");
        c.getPlayer().dropMessage(BroadcastMessageType.NOTICE_WITHOUT_PREFIX, commandList);
    }

    private static String getCommandsForLevel(int level, String search) {
        StringBuilder sb = new StringBuilder();
        List<String> sl = new LinkedList<>();
        if (commandScripts.containsKey(level)) {
            for (Map.Entry<String, Pair<String, String>> entry : commandScripts.get(level).entrySet()) {
                String cmd = entry.getKey();
                Pair<String, String> pair = entry.getValue();
                if (cmd == null || pair == null) {
                    continue;
                }
                if (!search.isEmpty() && !fuzzyMatch(cmd, search) && !fuzzyMatch(pair.getLeft(), search)) {
                    continue;
                }
                if (sl.contains(cmd.substring(1).toLowerCase())) {
                    continue;
                }
                sl.add(cmd.substring(1).toLowerCase());
                sb.append(cmd.substring(1));
                if (pair.getLeft() != null) {
                    sb.append("(").append(pair.getLeft()).append(")");
                }
                sb.append(" | ");
            }
        }
        if (commands.containsKey(level)) {
            for (Map.Entry<String, CommandObject> entry : commands.get(level).entrySet()) {
                String cmd = entry.getKey();
                CommandObject co = entry.getValue();
                if (cmd == null || co == null) {
                    continue;
                }
                if (!search.isEmpty() && !fuzzyMatch(cmd, search) && !fuzzyMatch(co.getShortCommand(), search)) {
                    continue;
                }
                if (sl.contains(cmd.substring(1).toLowerCase())) {
                    continue;
                }
                sl.add(cmd.substring(1).toLowerCase());
                sb.append(cmd.substring(1));
                if (co.getShortCommand() != null) {
                    sb.append("(").append(co.getShortCommand()).append(")");
                }
                sb.append(" | ");
            }
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - 3) : null;
    }

    public static boolean processCommand(MapleClient c, String line, CommandType type) {
        if (line.length() < 2) {
            return false;
        }
        if ((line.charAt(0) != '/' && PlayerRank.getByCommandPrefix(line.charAt(0)) == null) || line.charAt(0) == line.charAt(1)) {
            return false;
        }

        String[] splitted = line.split(" ");
        splitted[0] = splitted[0].toLowerCase();

        List<CommandObject> coList = new ArrayList<>();
        List<Map.Entry<String, Pair<String, String>>> csList = new ArrayList<>();

        List<String> sl = new LinkedList<>();
        for (PlayerRank rank : PlayerRank.values()) {
            if ((rank.isGm() && c.getGmLevel() < rank.getSpLevel()) || (!rank.isGm() && !c.isIntern() && c.getPlayer().getMvpLevel() < rank.getLevel())) {
                break;
            }
            String cmd;
            if (line.charAt(0) == '/' || PlayerRank.getByCommandPrefix(line.charAt(0)) != null) {
                cmd = rank.getCommandPrefix() + splitted[0].substring(1);
            } else {
                cmd = splitted[0];
            }

            List<CommandObject> cList = new ArrayList<>();
            List<Map.Entry<String, Pair<String, String>>> cList_S = new ArrayList<>();
            if (commandScripts.containsKey(rank.getLevel())) {
                for (Map.Entry<String, Pair<String, String>> entry : commandScripts.get(rank.getLevel()).entrySet()) {
                    if (entry == null || entry.getKey() == null || entry.getValue() == null || entry.getKey().charAt(0) != cmd.charAt(0) || CommandType.NORMAL != type) {
                        continue;
                    }
                    if (fuzzyMatch(entry.getKey(), cmd.substring(1)) || fuzzyMatch(entry.getValue().getLeft(), cmd.substring(1))) {
                        if (entry.getKey().equals(cmd) || (entry.getValue().getLeft() != null && entry.getValue().getLeft().toLowerCase().equals(cmd.substring(1)))) {
                            cList_S.clear();
                            cList_S.add(entry);
                            sl = null;
                            break;
                        } else {
                            if (!sl.contains(entry.getKey().toLowerCase())) {
                                sl.add(entry.getKey().toLowerCase());
                                cList_S.add(entry);
                            }
                        }
                    }
                }
            }
            csList.addAll(cList_S);
            if (sl != null && commands.containsKey(rank.getLevel())) {
                for (Map.Entry<String, CommandObject> entry : commands.get(rank.getLevel()).entrySet()) {
                    if (entry == null || entry.getKey() == null || entry.getValue() == null || entry.getKey().charAt(0) != cmd.charAt(0) || entry.getValue().getType() != type) {
                        continue;
                    }
                    if (fuzzyMatch(entry.getKey(), cmd.substring(1)) || fuzzyMatch(entry.getValue().getShortCommand(), cmd.substring(1))) {
                        if (entry.getKey().equals(cmd) || (entry.getValue().getShortCommand() != null && entry.getValue().getShortCommand().toLowerCase().equals(cmd.substring(1)))) {
                            cList.clear();
                            cList.add(entry.getValue());
                            break;
                        } else {
                            if (!sl.contains(entry.getKey().toLowerCase())) {
                                sl.add(entry.getKey().toLowerCase());
                                cList.add(entry.getValue());
                            }
                        }
                    }
                }
            }
            coList.addAll(cList);
        }


        if (csList.size() > 1) {
            Map.Entry<String, Pair<String, String>> csm = null;
            for (Map.Entry<String, Pair<String, String>> i : csList) {
                if (i.getKey().substring(1).toLowerCase().equals(splitted[0].substring(1))
                        || (i.getValue().getLeft() != null && i.getValue().getLeft().toLowerCase().equals(splitted[0].substring(1)))) {
                    csm = i;
                }
            }
            if (csm == null) {
                String cmd = null;
                for (Map.Entry<String, Pair<String, String>> i : csList) {
                    if (cmd == null) {
                        cmd = i.getKey().toLowerCase();
                        csm = i;
                    } else if (!cmd.equals(i.getKey().toLowerCase())) {
                        csm = null;
                        break;
                    }
                }
            }
            if (csm != null) {
                csList.clear();
                csList.add(csm);
            }
        }
        if (coList.size() > 1) {
            CommandObject co = null;
            for (CommandObject i : coList) {
                if (i.getName().toLowerCase().equals(splitted[0].substring(1))
                        || (i.getShortCommand() != null && i.getShortCommand().toLowerCase().equals(splitted[0].substring(1)))) {
                    co = i;
                }
            }
            if (co == null) {
                String cmd = null;
                for (CommandObject i : coList) {
                    if (cmd == null) {
                        cmd = i.getName();
                        co = i;
                    } else if (!cmd.equals(i.getName())) {
                        co = null;
                        break;
                    }
                }
            }
            if (co != null) {
                coList.clear();
                coList.add(co);
            }
        }

        boolean bHelp = "幫助".contains(splitted[0].substring(1)) || "help".contains(splitted[0].substring(1).toLowerCase());
        if (csList.size() != 1 && coList.size() != 1) {
            if (csList.isEmpty() && coList.isEmpty() && bHelp) {
                dropHelp(c, true);
            } else {
                sendDisplayMessage(c, "輸入的指令不存在" + ((csList.isEmpty() && coList.isEmpty()) ? "" : (", 您是可能需要使用如下指令" + (!c.isGm() ? ":" : "(可用\"/\"前綴):"))), type);
                String cmds = "";
                sl.clear();
                for (Map.Entry<String, Pair<String, String>> co : csList) {
                    if (sl.contains(co.getKey().toLowerCase())) {
                        continue;
                    }
                    sl.add(co.getKey().toLowerCase());
                    cmds += co.getKey();
                    if (co.getValue().getLeft() != null) {
                        cmds += "(" + co.getValue().getLeft() + ")";
                    }
                    cmds += " | ";
                }
                for (CommandObject co : coList) {
                    String name = PlayerRank.getByLevel(co.getReqLevel()).getCommandPrefix() + co.getName();
                    if (sl.contains(name.toLowerCase())) {
                        continue;
                    }
                    sl.add(name.toLowerCase());
                    cmds += name;
                    if (co.getShortCommand() != null) {
                        cmds += "(" + co.getShortCommand() + ")";
                    }
                    cmds += " | ";
                }
                if (!cmds.isEmpty()) {
                    sendDisplayMessage(c, cmds.substring(0, cmds.length() - 3), type);
                }
            }
            return true;
        } else if (csList.size() == 1) {
            Map.Entry<String, Pair<String, String>> csm = csList.get(0);
            PlayerRank pRank = PlayerRank.getByFolderName(csm.getValue().getRight().substring(0, csm.getValue().getRight().indexOf("/")));
            if ((pRank.isGm() && c.getGmLevel() >= pRank.getSpLevel()) || (!pRank.isGm() && (c.isIntern() || c.getPlayer().getMvpLevel() >= pRank.getLevel()))) {
                int ret = 0;
                try {
                    c.getPlayer().getScriptManager().startCommandScript(splitted, 0, csm.getValue().getRight());
                } catch (Exception e) {
                    ret = 1;
                    if (pRank.isGm()) {
                        if (e instanceof ArrayIndexOutOfBoundsException) {
                            sendDisplayMessage(c, "使用命令出錯，該命令必須帶參數才能使用: " + e, type);
                        }
                        log.error("使用指令" + splitted[0] + "出錯", e);
                    } else {
                        sendDisplayMessage(c, "使用指令出現錯誤.", type);
                    }
                    if (c.getPlayer().isGm()) {
                        sendDisplayMessage(c, "錯誤: " + e, type);
                        log.error(e.getMessage());
                    }
                }
                if (bHelp) {
                    dropHelp(c, ret > 0);
                }
                if (ret > 0 && c.getPlayer() != null) { //incase d/c after command or something
                    logCommandToDB(c.getPlayer(), line, "gmlog");
                }
            } else {
                if ((!pRank.isGm() && pRank.getLevel() > PlayerRank.普通.getLevel() && c.getPlayer().isBronzeIMvp()) || (pRank.isGm() && c.getGmLevel() < pRank.getSpLevel())) {
                    sendDisplayMessage(c, "您的權限等級不足以使用此指令.", type);
                } else {
                    return false;
                }
            }
        } else {
            CommandObject co = coList.get(0);
            if ((co.isGm() && c.getGmLevel() >= co.getSpReqLevel()) || (!co.isGm() && (c.isIntern() || c.getPlayer().getMvpLevel() >= co.getReqLevel()))) {
                int ret = 0;
                try {
                    co.execute(c, splitted); //Don't really care about the return value. ;D
                } catch (Exception e) {
                    ret = 1;
                    if (co.isGm()) {
                        if (e instanceof ArrayIndexOutOfBoundsException) {
                            sendDisplayMessage(c, "使用命令出錯，該命令必須帶參數才能使用: " + e, type);
                        }
                        log.error("使用指令" + splitted[0] + "出錯", e);
                    } else {
                        sendDisplayMessage(c, "使用指令出現錯誤.", type);
                    }
                    if (c.getPlayer().isGm()) {
                        sendDisplayMessage(c, "錯誤: " + e, type);
                        log.error(e.getMessage());
                    }
                }
                if (bHelp) {
                    dropHelp(c, ret > 0);
                }
                if (ret > 0 && c.getPlayer() != null) { //incase d/c after command or something
                    logCommandToDB(c.getPlayer(), line, "gmlog");
                }
            } else {
                if ((!co.isGm() && co.getReqLevel() > PlayerRank.普通.getLevel() && c.getPlayer().isBronzeIMvp()) || (co.isGm() && c.getGmLevel() < co.getSpReqLevel())) {
                    sendDisplayMessage(c, "您的權限等級不足以使用此指令.", type);
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean fuzzyMatch(String toMatch, String regex) {
        if (toMatch == null || regex == null) {
            return false;
        }
        toMatch = toMatch.toLowerCase();
        regex = regex.toLowerCase();
        for (int i = 0; i < regex.length(); i++) {
            if (!toMatch.contains(regex.substring(i, i + 1))) {
                return false;
            }
        }
        return true;
    }

    private static void logCommandToDB(MapleCharacter player, String command, String table) {
        try (Connection con = DatabaseConnectionEx.getInstance().getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO " + table + " (cid, name, command, mapid) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, player.getId());
                ps.setString(2, player.getName());
                ps.setString(3, command);
                ps.setInt(4, player.getMap().getId());
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            log.error(ex.getMessage());
        }
    }
}
