package Net.server.maps;


/**
 * 該類表示楓之谷地圖上所有具體生命特徵的對象.例如玩家、怪物等.
 *
 * @author dongjak
 */
public abstract class AnimatedMapleMapObject extends MapleMapObject {

    private int stance;
    private int homeFH;
    private int currentFh;
    private long lastMoveTime;

    public long getLastMoveTime() {
        return this.lastMoveTime;
    }

    public void setLastMoveTime(final int n) {
        this.lastMoveTime = System.currentTimeMillis() + n;
    }

    public int getHomeFH() {
        return this.homeFH;
    }

    public int getCurrentFH() {
        return this.currentFh;
    }

    public void setHomeFH(final int bua) {
        this.homeFH = bua;
    }

    public void setCurrentFh(final int bub) {
        this.currentFh = bub;
    }

    public int getStance() {
        return stance;
    }

    public void setStance(int stance) {
        this.stance = stance;
    }

    public boolean isFacingLeft() {
        return getStance() % 2 != 0;
    }

    public int getFacingDirection() {
        return Math.abs(getStance() % 2);
    }
}
