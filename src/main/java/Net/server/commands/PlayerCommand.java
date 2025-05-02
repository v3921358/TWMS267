package Net.server.commands;

import Client.MapleClient;
import Config.constants.enums.ConversationType;
@Deprecated
public class PlayerCommand {

    /**
     * 使用權限
     *
     * @return 權限
     */
    public static PlayerRank getPlayerLevelRequired() {
        return PlayerRank.普通;
    }

    /**
     * 角色解卡
     */
    public static class 解卡 implements CommandExecute {

        @Override
        public String getShortCommand() {
            return "EA";
        }

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.removeClickedNPC();
            if (c.getPlayer() != null)
                c.getPlayer().getScriptManager().dispose();
//            NPCScriptManager.getInstance().dispose(c);
            c.getPlayer().setConversation(ConversationType.NONE);
            c.getPlayer().setDirection(-1);
            c.sendEnableActions();
            c.sendEnableActions(false);
            //c.write(OverseasPacket.extraSystemResult(ExtraSystemResult.enableActions()));
            c.getPlayer().dropMessage(15, "[指令] 您輸入了@EA,嘗試解卡。");
            return 1;
        }
    }
}
