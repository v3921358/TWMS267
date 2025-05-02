package Client.force;

import java.awt.*;
import java.util.List;

public class MapleForceAtom {
    private boolean fromMob = false;
    private int ownerId;
    private int fromMobOid;
    private int forceType;
    private List<Integer> toMobOid;
    private int firstMobID;
    private int arriveDir;
    private int arriveRange;
    private List<MapleForceInfo> info;
    private Rectangle rect = new Rectangle();
    private Rectangle rect2 = new Rectangle();
    private int bulletItemID;
    private Point forcedTarget = new Point();
    private int skillId;
    private Point pos2;

    public final Point getForcedTarget() {
        return this.forcedTarget;
    }

    public final void setForcedTarget(final Point pos) {
        this.forcedTarget = pos;
    }

    public final int getBulletItemID() {
        return this.bulletItemID;
    }

    public final void setBulletItemID(final int bulletItemID) {
        this.bulletItemID = bulletItemID;
    }

    public final Rectangle getRect() {
        return this.rect;
    }

    public final void setRect(final Rectangle rect) {
        this.rect = rect;
    }

    public final int getSkillId() {
        return this.skillId;
    }

    public final void setSkillId(final int skillId) {
        this.skillId = skillId;
    }

    public final List<MapleForceInfo> getInfo() {
        return this.info;
    }

    public final void setInfo(final List<MapleForceInfo> info) {
        this.info = info;
    }

    public final int getArriveRange() {
        return this.arriveRange;
    }

    public final void setArriveRange(final int arriveRange) {
        this.arriveRange = arriveRange;
    }

    public final int getArriveDir() {
        return this.arriveDir;
    }

    public final void setArriveDir(final int arriveDir) {
        this.arriveDir = arriveDir;
    }

    public final int getFirstMobID() {
        return this.firstMobID;
    }

    public final void setFirstMobID(final int firstMobID) {
        this.firstMobID = firstMobID;
    }

    public final List<Integer> getToMobOid() {
        return this.toMobOid;
    }

    public final void setToMobOid(final List<Integer> toMobOid) {
        this.toMobOid = toMobOid;
    }

    public final int getForceType() {
        return this.forceType;
    }

    public final void setForceType(final int forceType) {
        this.forceType = forceType;
    }

    public final int getFromMobOid() {
        return this.fromMobOid;
    }

    public final void setFromMobOid(final int fromMobOid) {
        this.fromMobOid = fromMobOid;
    }

    public final int getOwnerId() {
        return this.ownerId;
    }

    public final void setOwnerId(final int ownerId) {
        this.ownerId = ownerId;
    }

    public final boolean isFromMob() {
        return this.fromMob;
    }

    public final void setFromMob(final boolean fromMob) {
        this.fromMob = fromMob;
    }

    public Point getPos2() {
        return pos2;
    }

    public void setPos2(Point pos2) {
        this.pos2 = pos2;
    }

    public final Rectangle getRect2() {
        return this.rect2;
    }

    public final void setRect2(final Rectangle rect) {
        this.rect2 = rect;
    }


}
