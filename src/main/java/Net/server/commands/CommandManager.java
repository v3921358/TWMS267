package Net.server.commands;

import Client.MapleClient;
import Config.constants.enums.ConversationType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class CommandManager {
    @Getter
    private final MapleClient client;

    public CommandManager(MapleClient client) {
        this.client = client;
    }

    public boolean formatCommand(String command) {

        // 定义正则表达式匹配以#!@/开头的字符串
        String regex = "^[#!@/](.*)";

        // 编译正则表达式
        Pattern pattern = Pattern.compile(regex);

        // 创建匹配器对象
        Matcher matcher = pattern.matcher(command);

        // 检查是否匹配
        if (matcher.find()) {
            // 获取匹配的组
            String result = matcher.group(1);
            String[] parts = result.split(" ");
            log.info(Arrays.toString(parts));
            if (parts.length > 1) {
                getClient().getPlayer().getScriptManager().startCommandScript(parts, 9000030, "entry");
                return true;
            } else if (parts.length == 1) {
                if ("ea".equals(parts[0]) || "EA".equals(parts[0]) || "解卡".equals(parts[0])) {
                    client.removeClickedNPC();
                    if (client.getPlayer() != null)
                        client.getPlayer().getScriptManager().dispose();
                    client.getPlayer().setConversation(ConversationType.NONE);
                    client.getPlayer().setDirection(-1);
                    client.sendEnableActions();
                    client.sendEnableActions(false);
                    client.getPlayer().dropMessage(15, "解卡成功。");
                } else {
                    getClient().getPlayer().getScriptManager().startCommandScript(new String[]{result, " "}, 9000030, "entry");
                }

                return true;
            }

        }
        return false;

    }

}
