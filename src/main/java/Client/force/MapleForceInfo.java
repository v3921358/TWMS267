package Client.force;

import java.awt.*;

public class MapleForceInfo {
    private int key;
    private int inc;
    private int firstImpact;
    private int secondImpact;
    private int angle;
    private int startDelay;
    private Point position;
    private long time;
    private int maxHitCount;
    private int effectIdx;
    public Point pos2;

    public final int getEffectIdx() {
        return this.effectIdx;
    }

    public final void setEffectIdx(final int effectIdx) {
        this.effectIdx = effectIdx;
    }

    public final int getMaxHitCount() {
        return this.maxHitCount;
    }

    public final void setMaxHitCount(final int maxHitCount) {
        this.maxHitCount = maxHitCount;
    }

    public final long getTime() {
        return this.time;
    }

    public final void setTime(final long time) {
        this.time = time;
    }

    public final Point getPosition() {
        return this.position;
    }

    public final void setPosition(final Point position) {
        this.position = position;
    }

    public final int getStartDelay() {
        return this.startDelay;
    }

    public final void setStartDelay(final int startDelay) {
        this.startDelay = startDelay;
    }

    public final int getAngle() {
        return this.angle;
    }

    public final void setAngle(final int angle) {
        this.angle = angle;
    }

    public final int getSecondImpact() {
        return this.secondImpact;
    }

    public final void setSecondImpact(final int secondImpact) {
        this.secondImpact = secondImpact;
    }

    public final int getFirstImpact() {
        return this.firstImpact;
    }

    public final void setFirstImpact(final int firstImpact) {
        this.firstImpact = firstImpact;
    }

    public final int getInc() {
        return this.inc;
    }

    public final void setInc(final int inc) {
        this.inc = inc;
    }

    public final int getKey() {
        return this.key;
    }

    public final void setKey(final int key) {
        this.key = key;
    }
}
