package Plugin.script.binding;

import Client.MapleClient;
import Net.server.MaplePortal;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;

@Slf4j
public class ScriptPortal extends PlayerScriptInteraction {
    @Getter
    private final MaplePortal portal;


    private boolean warped;

    public ScriptPortal(MapleClient Client, MaplePortal portal) {
        super(Client.getPlayer());
        this.portal = portal;
        this.warped = true;
    }

    public String getName() {
        MaplePortal portal = getPortal();
        return getPortal().getName();
    }

    public int getId() {
        return getPortal().getId();
    }

    public Point getPosition() {
        return getPortal().getPosition();
    }

    public void abortWarp() {
        warped = false;
    }

    public boolean warped() {
        return warped;
    }

    public void playPortalSE() {
        warped = false;
    }

//    public void runScript(int npcId, String scriptName) {
//        super.runScript(npcId, scriptName);
//        new ScriptManager(getPlayer()).startScript(npcId, scriptName, ScriptType.Npc);
//    }

    public int[] resetRememberedMap(String variable) {

        String rMap = getPlayer().getQuestInfo(100642, variable + "_rMap");
        String rPoratl = getPlayer().getQuestInfo(100642, variable + "_rPoratl");

        if (rMap == null || rMap.equals("")) {
            rMap = "100000000";
        } else {
            getPlayer().updateOneQuestInfo(100642, variable + "_rMap", "");
        }
        if (rPoratl == null || rPoratl.equals("")) {
            rPoratl = "0";
        } else {
            getPlayer().updateOneQuestInfo(100642, variable + "_rPoratl", "");
        }

        return new int[]{Integer.parseInt(rMap), Integer.parseInt(rPoratl)};

    }

    public void rememberMap(String variable) {
        getPlayer().updateOneQuestInfo(100642, variable + "_rMap", Integer.toString(getPlayer().getMapId()));
        getPlayer().updateOneQuestInfo(100642, variable + "_rPoratl", Integer.toString(getPortal().getId()));
    }

}
