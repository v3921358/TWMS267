package Net.server;

import Client.MapleCharacter;
import Client.MapleClient;
import Net.server.maps.MapleMap;
import Server.channel.ChannelServer;

import java.awt.*;

public class MaplePortal {

    public static final int DOOR_PORTAL = 6;
    public static int MAP_PORTAL = 2;
    private final int type;
    private String name, target, scriptName;
    private Point position;
    private int targetmap;
    private int id;
    private boolean portalState = true;

    public MaplePortal(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public int getTargetMapId() {
        return targetmap;
    }

    public void setTargetMapId(int targetmapid) {
        this.targetmap = targetmapid;
    }

    public int getType() {
        return type;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public void enterPortal(MapleClient c) {
        MapleCharacter player = c.getPlayer();
//        if (getPosition().distance(player.getPosition()) > 90 && !c.getPlayer().isGM() && c.getPlayer().getMapId() != 4000010) {
//            c.sendEnableActions();
//            c.getPlayer().getCheatTracker().registerOffense(CheatingOffense.USING_FARAWAY_PORTAL);
//            return;
//        }
        MapleMap currentmap = c.getPlayer().getMap();
//        if (!c.getPlayer().hasBlockedInventory() && (portalState || c.getPlayer().isGM())) {
        if (getScriptName() != null) {
            c.getPlayer().checkFollow();
            c.getPlayer().getScriptManager().startPortalScript(this);
//            PortalScriptManager.getInstance().executePortalScript(this, c);
        } else if (getTargetMapId() != 999999999) {
            MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(getTargetMapId());
            if (to == null) {
                c.sendEnableActions();
                return;
            }
            if (!c.getPlayer().isGm()) {
                if (to.getLevelLimit() > 0 && to.getLevelLimit() > c.getPlayer().getLevel()) {
                    c.getPlayer().dropMessage(-1, "未達到等級要求，無法進入該地區.");
                    c.sendEnableActions();
                    return;
                }
                //if (to.getForceMove() > 0 && to.getForceMove() < c.getPlayer().getLevel()) {
                //    c.getPlayer().dropMessage(-1, "You are too high of a level to enter this place.");
                //    c.sendEnableActions(true);
                //    return;
                //}
            }
            c.getPlayer().changeMap(to, to.getPortal(getTarget()) == null ? to.getPortal(0) : to.getPortal(getTarget())); //late resolving makes this harder but prevents us from loading the whole world at once
        }
//        }
        if (c.getPlayer() != null && c.getPlayer().getMap() == currentmap) { // Character is still on the same map.
            c.sendEnableActions();
        }
    }

    public boolean getPortalState() {
        return portalState;
    }

    public void setPortalState(boolean ps) {
        this.portalState = ps;
    }
}
