package Net.server.maps;

import tools.Randomizer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MapleSwordNode {
    private int bKM = 0;
    private int count = 0;
    private final List<MapleSwordNodeInfo> swordNodeInfos = new ArrayList<>();
    private Point bKQ = null;
    private final int monsterId;
    private final int bKO;
    private final Point point;

    public MapleSwordNode(final int monsterId, final int bKO, final Point point) {
        this.monsterId = monsterId;
        this.bKO = bKO;
        this.point = point;
        swordNodeInfos.add(new MapleSwordNodeInfo(1, bKO, 0, 0, 0, 0, 0, 0, 0, point));
    }

    public final void gainCount() {
        count++;
    }


    public final void a(final int top, final int bottom, final int left, final int right, final boolean bl2) {
        if (bl2) {
            swordNodeInfos.clear();
        }
        int size = swordNodeInfos.size();
        if (size != 14) {
            int n6 = 14;
            if (size <= n6 - 1) {
                for (int i2 = size; i2 < n6; i2++) {
                    int n7 = 1;
                    int n10 = i2 == 12 ? 60 : 35;
                    int n11 = i2 == 12 ? 500 : 0;
                    int n12 = i2 == 13 ? 11000 : 0;
                    int n13 = 0;
                    int n14 = i2 == 13 ? 1 : 0;
                    int n15 = 0;
                    Point point = new Point(Randomizer.rand(left, right), Randomizer.rand(top / 2, bottom - 20));
                    if (i2 == 12) {
                        this.bKQ = new Point(Randomizer.rand(left, right), 15);
                        point = bKQ;
                    } else if (n14 > 0) {
                        point = bKQ;
                    }
                    swordNodeInfos.add(new MapleSwordNodeInfo(n7, bKO, i2, n10, n11, n12, n13, n14, n15, point));
                }
            }

        }
    }

    public int getBKM() {
        return bKM;
    }

    public void setBKM(int n) {
        this.bKM = n;
    }

    public int getCount() {
        return count;
    }

    public List<MapleSwordNodeInfo> getSwordNodeInfos() {
        return swordNodeInfos;
    }

    public int getMonsterId() {
        return monsterId;
    }

    public Point getPoint() {
        return point;
    }

    public class MapleSwordNodeInfo {
        private final int nodeType;
        private final int bKS;
        private final int nodeIndex;
        private final int bKU;
        private final int bKV;
        private final int bKW;
        private final int bKX;
        private final int bKY;
        private final int bKZ;
        private final Point pos;

        public final int getNodeType() {
            return this.nodeType;
        }

        public final int getBKS() {
            return this.bKS;
        }

        public final int getNodeIndex() {
            return this.nodeIndex;
        }

        public final int getBKU() {
            return this.bKU;
        }

        public final int getBKV() {
            return this.bKV;
        }

        public final int getBKW() {
            return this.bKW;
        }

        public final int getBKX() {
            return this.bKX;
        }

        public final int getBKY() {
            return this.bKY;
        }

        public final int getBKZ() {
            return this.bKZ;
        }

        public final Point getPos() {
            return this.pos;
        }

        public MapleSwordNodeInfo(final int nodeType, final int bKS, final int nodeIndex, final int bKU, final int bKV, final int bKW, final int bKX, final int bKY, final int bKZ, final Point pos) {
            this.nodeType = nodeType;
            this.bKS = bKS;
            this.nodeIndex = nodeIndex;
            this.bKU = bKU;
            this.bKV = bKV;
            this.bKW = bKW;
            this.bKX = bKX;
            this.bKY = bKY;
            this.bKZ = bKZ;
            this.pos = pos;
        }
    }
}


