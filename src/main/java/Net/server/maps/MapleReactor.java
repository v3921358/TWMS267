package Net.server.maps;

import Client.MapleClient;
import Config.constants.GameConstants;
import Net.server.Timer.MapTimer;
import Packet.MaplePacketCreator;
import tools.Pair;

import java.awt.*;

public class MapleReactor extends MapleMapObject {

    private int rid;
    private MapleReactorStats stats;
    private byte state = 0, facingDirection = 0;
    private int delay = -1;
    private MapleMap map;
    private String name = "";
    private boolean timerActive = false, alive = true, custom = false, pqAction = false;
    private Point srcPos = new Point();
    private short hitStart;
    private byte properEventIdx;
    private byte stateEnd;
    private int ownerID;
    private long gatherTime;

    public MapleReactor(MapleReactorStats stats, int rid) {
        this.stats = stats;
        this.rid = rid;
    }

    public Point getSrcPos() {
        return srcPos;
    }

    public void setSrcPos(Point srcPos) {
        this.srcPos = srcPos;
    }

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

    public MapleReactorStats getStats() {
        return stats;
    }

    public void setStats(MapleReactorStats stats) {
        this.stats = stats;
    }

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean c) {
        this.custom = c;
    }

    public byte getFacingDirection() {
        return facingDirection;
    }

    public void setFacingDirection(byte facingDirection) {
        this.facingDirection = facingDirection;
    }

    public boolean isTimerActive() {
        return timerActive;
    }

    public void setTimerActive(boolean active) {
        this.timerActive = active;
    }

    public int getReactorId() {
        return rid;
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.REACTOR;
    }

    @Override
    public int getRange() {
        return GameConstants.maxViewRange();
    }

    public int getReactorType() {
        return stats.getType(state);
    }

    public byte getTouch() {
        return stats.canTouch(state);
    }

    public MapleMap getMap() {
        return map;
    }

    public void setMap(MapleMap map) {
        this.map = map;
    }

    public Pair<Integer, Integer> getReactItem() {
        return stats.getReactItem(state);
    }

    public boolean isPqAction() {
        return pqAction;
    }

    public void setPqAction(boolean pqAction) {
        this.pqAction = pqAction;
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(MaplePacketCreator.reactorLeaveField(this));
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.announce(MaplePacketCreator.spawnReactor(this));
    }

    public void forceStartReactor(MapleClient c) {
        c.getPlayer().getScriptManager().startReactorScript(this);
//        ReactorScriptManager.getInstance().act(c, this);
    }

    public void forceHitReactor(byte newState) {
        setState(newState);
        setTimerActive(false);
        map.broadcastMessage(MaplePacketCreator.triggerReactor(this));
    }

    //hitReactor command for item-triggered reactors
    public void hitReactor(MapleClient c) {
        hitReactor(0, (short) 0, c);
    }

    public void delayedDestroyReactor(long delay) {
        MapTimer.getInstance().schedule(() -> map.destroyReactor(getObjectId()), delay);
    }

    public void hitReactor(int charPos, short stance, MapleClient c) {
        setHitStart(stance);
        if (stats.getType(state) < 999 && stats.getType(state) != -1 && (stats.getType(state) != 2)) {
            //type 2 = only hit from right (kerning swamp plants), 00 is air left 02 is ground left
            byte oldState = state;
            state = stats.getNextState(state);

            if (stats.getNextState(state) == -1 || stats.getType(state) == 999) { //end of reactor
                if ((stats.getType(state) < 100 || stats.getType(state) == 999) && delay > 0) { //reactor broken
                    map.destroyReactor(getObjectId());
                } else { //item-triggered on step
                    map.broadcastMessage(MaplePacketCreator.triggerReactor(this));
                }
                c.getPlayer().getScriptManager().startReactorScript(this);
//                ReactorScriptManager.getInstance().act(c, this);
            } else { //reactor not broken yet
                if ((rid == 9239001 || rid == 2008009) && !this.isPqAction()) {
                    this.setSrcPos(this.getPosition());
                    this.setPqAction(true);
                    this.ownerID = c.getPlayer().getId();
                } else if (this.isPqAction()) {
                    this.setPqAction(false);
                    c.getPlayer().setReactor(null);
                    this.ownerID = 0;
                }
                boolean done = false;
                map.broadcastMessage(MaplePacketCreator.triggerReactor(this));
                if (rid != 1058018 && rid != 1058019 && (this.stats.getType(this.state) == 9 || this.stats.getType(this.state) == 11 || this.stats.getType(this.state) == 100)) {
//                    ReactorScriptManager.getInstance().act(c, this);
                    c.getPlayer().getScriptManager().startReactorScript(this);
                    done = true;
                }
                if (state == stats.getNextState(state) || this.getReactorId() == 9250140 || this.getReactorId() == 2618000 || this.getReactorId() == 2309000) { //current state = next state, looping reactor
                    if (!done && rid > 200011) {
                        c.getPlayer().getScriptManager().startReactorScript(this);
//                        ReactorScriptManager.getInstance().act(c, this);
                    }
                    done = true;
                }
                if (stats.getTimeOut(state) > 0) {
                    if (!done && rid > 200011) {
                        c.getPlayer().getScriptManager().startReactorScript(this);
//                        ReactorScriptManager.getInstance().act(c, this);
                    }
                    scheduleSetState(state, (byte) 0, stats.getTimeOut(state));
                }
            }
        }
    }

    public Rectangle getArea() {
        int height = stats.getBR().y - stats.getTL().y;
        int width = stats.getBR().x - stats.getTL().x;
        int origX = getPosition().x + stats.getTL().x;
        int origY = getPosition().y + stats.getTL().y;
        return new Rectangle(origX, origY, width, height);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "反應堆 工作ID:" + getObjectId() + " ReactorID: " + rid + " 坐標: " + getPosition().x + "/" + getPosition().y + " 狀態: " + state + " 類型: " + stats.getType(state);
    }

    public void delayedHitReactor(final MapleClient c, final long delay) {
        MapTimer.getInstance().schedule(() -> hitReactor(c), delay);
    }

    public void scheduleSetState(final byte oldState, final byte newState, final long delay) {
        MapTimer.getInstance().schedule(() -> {
            if (MapleReactor.this.state == oldState) {
                forceHitReactor(newState);
            }
        }, delay);
    }

    public short getHitStart() {
        return hitStart;
    }

    public void setHitStart(short hitStart) {
        this.hitStart = hitStart;
    }

    public byte getProperEventIdx() {
        return properEventIdx;
    }

    public void setProperEventIdx(byte properEventIdx) {
        this.properEventIdx = properEventIdx;
    }

    public byte getStateEnd() {
        return stateEnd;
    }

    public void setStateEnd(byte stateEnd) {
        this.stateEnd = stateEnd;
    }

    public int getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(int ownerID) {
        this.ownerID = ownerID;
    }

    public void setGatherTime(long gatherTime) {
        this.gatherTime = gatherTime;
    }

    public long getGatherTime() {
        return gatherTime;
    }
}
