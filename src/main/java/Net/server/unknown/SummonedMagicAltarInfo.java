package Net.server.unknown;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SummonedMagicAltarInfo {
    public byte action;
    public int skillId;
    public int skillLv;
    public int a1;
    public int a2;
    public int a3;
    public int a4;
    public int a5;
    public int a6;
    public int a7;
    public Point position;
    public Rectangle area;
    public final List<SubInfo> subSummon;

    public SummonedMagicAltarInfo() {
        this.subSummon = new ArrayList<>();
    }

    public static final class SubInfo {
        public int a1;
        public Point position;
        public int a2;
        public int a3;
        public boolean b1;
        public int a4;
        public int a5;
        public boolean b2;
        public int a6;
        public int a7;
        public int a8;
    }
}
