package Client;

import tools.Randomizer;

import java.awt.*;
import java.util.List;
import java.util.*;

public class SpecialChairTW {
    private final int[] bbv;
    private final Map<Integer, Integer> bbw;
    private final Map<Integer, Integer> bbx;
    private final int j;
    private final List<Integer> bby;
    private final int[] cc;
    private final int m;
    private final Point aQV;
    private final Rectangle bbu;
    private final boolean bX;
    private final int ap;

    public SpecialChairTW(int var1, int var2, int var3, Point var4, int var5, int var6, int var7, boolean var8) {
        int var10000 = 0;
        this.j = var1;
        this.aQV = var4;
        this.m = var2;
        this.ap = var3;
        this.bbu = new Rectangle(var4.x - var5 / 2, var4.y - var6, var5, 10);
        this.bbv = new int[var7];
        this.cc = new int[var7];
        this.bX = var8;
        this.bby = new ArrayList<>();

        for (var1 = 0; var10000 < this.cc.length; var10000 = var1) {
            this.cc[var1++] = 0;
        }

        for (var10000 = var1 = 0; var10000 < var7; var10000 = var1) {
            this.bby.add(var1++);
        }

        this.bbx = new HashMap<>();
        this.bbw = new HashMap<>();
    }

    public void clear() {
        int var10000 = 0;
        this.bbx.clear();
        this.bbw.clear();

        for (int var1 = 0; var10000 < this.bbv.length; var10000 = var1) {
            this.bbv[var1++] = 0;
        }

    }

    public Point getPosition() {
        return this.aQV;
    }

    public int[] vr() {
        return this.cc;
    }

    public Rectangle vs() {
        return this.bbu;
    }

    public boolean lj() {
        return this.bby.isEmpty();
    }

    public Map<Integer, Integer> vu() {
        return this.bbx;
    }

    public int V() {
        return this.j;
    }

    public Map<Integer, Integer> vv() {
        return this.bbw;
    }

    public int getItemId() {
        return this.ap;
    }

    public void oj(int var1) {
        this.bbx.remove(var1);
        int var2 = 0;

        for (int var10000 = var2; var10000 < this.bbv.length; var10000 = var2) {
            if (this.bbv[var2] == var1) {
                this.bby.add(var2);
                this.bbv[var2] = 0;
                return;
            }

            ++var2;
        }

    }

    public void ol(int var1) {
        this.bbw.put(var1, Randomizer.nextInt(this.cc.length));
    }

    public void om(int var1) {
        this.bbw.remove(var1);
    }

    public boolean ok(int var1) {
        if (!this.bby.isEmpty()) {
            Collections.shuffle(this.bby);
            int var2 = this.bby.remove(0);

            int var3;
            for (int var10000 = var3 = 0; var10000 < this.bbv.length; var10000 = var3) {
                if (this.bbv[var3] <= 0 && (!this.bX || var3 == var2)) {
                    this.bbv[var3] = var1;
                    this.bbx.put(var1, var3);
                    return true;
                }

                ++var3;
            }
        }

        return false;
    }
}
