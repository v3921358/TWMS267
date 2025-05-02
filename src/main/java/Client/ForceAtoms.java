package Client;

import java.awt.*;

public class ForceAtoms {

    private short delay;
    private int oid;
    private int key;
    private int inc;
    private int firstimpact;
    private int secondimpact;
    private int angle;
    private Point pos;

    public ForceAtoms(int oid, int key, int inc, int firstimpact, int secondimpact, int angle, short delay) {
        this.oid = oid;
        this.key = key;
        this.inc = inc;
        this.firstimpact = firstimpact;
        this.secondimpact = secondimpact;
        this.angle = angle;
        this.delay = delay;
        this.pos = new Point(0, 0);
    }

    public ForceAtoms(int oid, int key, int inc, int firstimpact, int secondimpact, int angle, short delay, Point pos) {
        this.oid = oid;
        this.key = key;
        this.inc = inc;
        this.firstimpact = firstimpact;
        this.secondimpact = secondimpact;
        this.angle = angle;
        this.delay = delay;
        this.pos = pos;
    }

    public short getDelay() {
        return delay;
    }

    public void setDelay(short delay) {
        this.delay = delay;
    }

    public int getObjectId() {
        return oid;
    }

    public void setObjectId(int oid) {
        this.oid = oid;
    }

    public int getInc() {
        return inc;
    }

    public void setInc(int inc) {
        this.inc = inc;
    }

    public int getFirstimpact() {
        return firstimpact;
    }

    public void setFirstimpact(int firstimpact) {
        this.firstimpact = firstimpact;
    }

    public int getSecondimpact() {
        return secondimpact;
    }

    public void setSecondimpact(int secondimpact) {
        this.secondimpact = secondimpact;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public Point getPos() {
        return pos;
    }

    public void setPos(Point pos) {
        this.pos = pos;
    }
}
