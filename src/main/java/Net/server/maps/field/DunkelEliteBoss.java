package Net.server.maps.field;

import Client.MapleCharacter;
import Net.server.life.MapleMonster;
import tools.Randomizer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DunkelEliteBoss {
    private final byte b1;

    private final byte b2;
    private final short Arrowvelocity;
    private final short v2;
    private final short v3;
    private final short v4;
    private final short v5;
    private final int unk1;
    private final int unk2;
    private final int unk3;
    private final int unk4;
    private final int unk5;
    private final int unk6;
    private final int unk7;
    private final List<Point> points = new ArrayList<>();
    private short bosscode = 0;
    private short v1 = 0;
    private int unk8;
    private int unk9;
    private int unk10;
    private int unk11;
    private int unk12;
    private int unk13;
    private Point p1 = new Point(0, 0), p2 = new Point(0, 0), p3 = new Point(0, 0);

    public DunkelEliteBoss(short bosscode, short v1, int unk1, int unk2, int unk3, int unk4, int unk5, int unk6, int unk7, short v2, short Arrowvelocity, short v3, byte b1, byte b2, Point p1, Point p2, Point p3, short v4, short v5) {
        this.bosscode = bosscode;
        this.b1 = b1;
        this.b2 = b2;
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.v4 = v4;
        this.v5 = v5;
        this.Arrowvelocity = Arrowvelocity;
        this.p1 = p1;
        this.unk1 = unk1;
        this.unk2 = unk2;
        this.unk3 = unk3;
        this.unk4 = unk4;
        this.unk5 = unk5;
        this.unk6 = unk6;
        this.unk7 = unk7;
        this.p2 = p2;
        this.p3 = p3;
    }

    public byte getb1() {
        return this.b1;
    }

    public byte getb2() {
        return this.b2;
    }

    public short getbosscode() {
        return this.bosscode;
    }

    public short getv1() {
        return this.v1;
    }

    public short getv2() {
        return this.v2;
    }

    public short getv3() {
        return this.v3;
    }

    public short getv4() {
        return this.v4;
    }

    public short getv5() {
        return this.v5;
    }

    public short getArrowvelocity() {
        return this.Arrowvelocity;
    }

    public Point getP1() {
        return this.p1;
    }

    public Point getP2() {
        return this.p2;
    }

    public Point getP3() {
        return this.p3;
    }

    public int getUnk1() {
        return this.unk1;
    }

    public int getUnk2() {
        return this.unk2;
    }

    public int getUnk3() {
        return this.unk3;
    }

    public int getUnk4() {
        return this.unk4;
    }

    public int getUnk5() {
        return this.unk5;
    }

    public int getUnk6() {
        return this.unk6;
    }

    public int getUnk7() {
        return this.unk7;
    }

    public int getUnk8() {
        return this.unk8;
    }

    public int getUnk9() {
        return this.unk9;
    }

    public int getUnk10() {
        return this.unk10;
    }

    public int getUnk11() {
        return this.unk11;
    }

    public int getUnk12() {
        return this.unk12;
    }

    public int getUnk13() {
        return this.unk13;
    }

    public static DunkelEliteBoss getEliteBossAttack(MapleMonster Dunkel) {
        DunkelEliteBoss eboss;
        int type = Randomizer.nextInt(10);
        List<MapleCharacter> chrlist = Dunkel.getMap().getCharacters();
        Collections.shuffle(chrlist);
        MapleCharacter chr = chrlist.get(0);
        Point cp = chr.getPosition();
        byte isLeft = (byte) (Randomizer.nextBoolean() ? 0 : 1);
        if (type == 0) {
            eboss = new DunkelEliteBoss((short) type, (short) 1, 2800, 1440, 1, 3, 0, 0, 0, (short) 0, (short) 0, (short) 0, (byte) 0, isLeft, new Point(-280, -220), new Point(10, 10), cp, (short) 0, (short) 0);
        } else if (type == 1) {
            eboss = new DunkelEliteBoss((short) type, (short) 2, 3000, 1620, 1, 4, 300, 0, 1200, (short) 65, (short) 100, (short) 0, (byte) 0, (byte) 1, new Point(-100, -75), new Point(0, 0), new Point(0, 0), (short) 0, (short) 2);
        } else if (type == 2) {
            eboss = new DunkelEliteBoss((short) type, (short) 2, 3000, 1800, 1, 5, 0, 1, 1600, (short) 35, (short) 600, (short) 0, (byte) 0, (byte) 0, new Point(-45, -20), new Point(0, 0), new Point(0, 0), (short) 0, (short) 2);
        } else if (type == 3) {
            eboss = new DunkelEliteBoss((short) type, (short) 1, 2900, 1500, 1, 6, 0, 0, 0, (short) 0, (short) 0, (short) 0, (byte) 0, isLeft, new Point(-620, -135), new Point(50, 5), cp, (short) 0, (short) 0);
        } else if (type == 4) {
            eboss = new DunkelEliteBoss((short) type, (short) 3, 3300, 1710, 5, 7, 0, 0, 0, (short) 0, (short) 0, (short) 0, (byte) 1, isLeft, new Point(-40, -80), new Point(40, 0), cp, (short) Randomizer.rand(5, 8), (short) 0);
        } else if (type == 5) {
            eboss = new DunkelEliteBoss((short) type, (short) 1, 4800, 3630, 1, 8, 0, 0, 0, (short) 0, (short) 0, (short) 1, (byte) 1, isLeft, new Point(-290, -420), new Point(270, 25), cp, (short) 0, (short) 0);
        } else if (type == 6) {
            eboss = new DunkelEliteBoss((short) type, (short) 3, 3000, 2160, 7, 11, 0, 0, 0, (short) 0, (short) 0, (short) 1, (byte) 1, isLeft, new Point(-50, -170), new Point(50, 5), new Point(0, 0), (short) Randomizer.rand(5, 9), (short) 0);
        } else if (type == 7) {
            eboss = new DunkelEliteBoss((short) type, (short) 1, 4800, 3630, 1, 8, 0, 0, 0, (short) 0, (short) 0, (short) 1, (byte) 1, isLeft, new Point(-290, -420), new Point(270, 25), cp, (short) 0, (short) 0);
        } else if (type == 8) {
            eboss = new DunkelEliteBoss((short) type, (short) 1, 2800, 840, 1, 12, 0, 0, 0, (short) 0, (short) 0, (short) 0, (byte) 0, isLeft, new Point(-360, -155), new Point(10, 10), cp, (short) 0, (short) 0);
        } else if (type == 9) {
            eboss = new DunkelEliteBoss((short) type, (short) 1, 2700, 840, 1, 10, 0, 0, 0, (short) 0, (short) 0, (short) 0, (byte) 0, isLeft, new Point(-350, -155), new Point(10, 10), cp, (short) 0, (short) 0);
        } else {
            eboss = null;
        }
        return eboss;
    }
}

