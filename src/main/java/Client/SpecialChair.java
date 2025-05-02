package Client;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpecialChair {
    private final List<Integer> bbt;
    private final int j;
    private final boolean aIr;
    private final Point aZv;
    private final int[] VG;
    private final int[] fa;
    private final Rectangle bbu;
    private final int av;
    private final int ap;

    public SpecialChair(int var1, int var2, int var3, Point var4, int var5, int var6, int var7, boolean var8) {
        int var10000 = 0;
        this.j = var1;
        this.aZv = var4;
        this.av = var2;
        this.ap = var3;
        this.bbu = new Rectangle(var4.x - var5 / 2, var4.y - var6, var5, 10);
        int[] var10007 = new int[var7];
        boolean var10009 = true;
        this.VG = var10007;
        int[] var10005 = new int[var7];
        boolean var9 = true;
        this.fa = var10005;
        this.aIr = var8;
        this.bbt = new ArrayList();

        for (var1 = 0; var10000 < this.fa.length; var10000 = var1) {
            this.fa[var1] = -1;
            this.bbt.add(var1++);
        }

    }

    public void clear() {
        synchronized (this.VG) {
            int var2;
            for (int var10000 = var2 = 0; var10000 < this.VG.length; var10000 = var2) {
                this.VG[var2++] = 0;
            }

        }
    }

    public Point getPosition() {
        return this.aZv;
    }

    public int vq() {
        return this.av;
    }

    public int[] vr() {
        return this.fa;
    }

    public Rectangle vs() {
        return this.bbu;
    }

    public int[] vt() {
        return this.VG;
    }

    public int V() {
        return this.j;
    }

    public int getItemId() {
        return this.ap;
    }

    public void oj(int var1) {
        int[] var2;
        synchronized (var2 = this.VG) {
            int var3;
            int var10000 = var3 = 0;

            int[] var6;
            while (true) {
                if (var10000 >= this.VG.length) {
                    var6 = var2;
                    break;
                }

                if (this.VG[var3] == var1) {
                    var6 = var2;
                    this.bbt.add(var3);
                    this.VG[var3] = 0;
                    break;
                }

                ++var3;
                var10000 = var3;
            }

        }
    }

    public boolean ok(int var1) {
        synchronized (this.VG) {
            if (!this.bbt.isEmpty()) {
                Collections.shuffle(this.bbt);
                int var3 = this.bbt.remove(0);

                int var4;
                for (int var10000 = var4 = 0; var10000 < this.VG.length; var10000 = var4) {
                    if (this.VG[var4] <= 0 && (!this.aIr || this.aIr && var4 == var3)) {
                        this.VG[var4] = var1;
                        return true;
                    }

                    ++var4;
                }
            }

            return false;
        }
    }
}

