package Net.server.maps.field;

import Client.status.MonsterStatus;
import Net.server.life.MapleMonster;
import Net.server.maps.MapleMap;
import Opcode.Headler.OutHeader;
import Packet.WillPacket;
import Plugin.provider.MapleData;
import Plugin.provider.MapleDataProviderFactory;
import Plugin.provider.MapleDataTool;
import tools.Pair;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;


public class BossWillField extends MapleMap {

    public static final Map<Integer, Map<Integer, List<List<Point>>>> BossWillConfig = new HashMap<>();
    private int mob1 = 0;
    private int mob2 = 0;
    private int dummy = 0;
    private final Map<Integer, List<Point>> aoI = new HashMap<>();
    private final Map<Integer, List<Pair<Integer, Point>>> aoJ = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public static void init() {
        try {
            for (final MapleData zj : MapleDataProviderFactory.getEtc().getData("BossWill.img").getChildByPath("Beholder").getChildByPath("Config")) {
                final HashMap<Integer, List<List<Point>>> hashMap = new HashMap<>();
                for (final MapleData zj2 : zj) {
                    final ArrayList<List<Point>> list = new ArrayList<>();
                    for (final MapleData zj3 : zj2.getChildByPath("Gen")) {
                        final ArrayList<Point> list2 = new ArrayList<>();
                        for (final MapleData zj4 : zj3) {
                            list2.add(new Point(MapleDataTool.getIntConvert("x", zj4), MapleDataTool.getIntConvert("ry", zj4)));
                        }
                        list.add(list2);
                    }
                    hashMap.put(hashMap.size(), list);
                }
                BossWillConfig.put(Integer.valueOf(zj.getName()), hashMap);
            }
        } catch (NullPointerException e) {
            log.error("[BossWillField] wz/Etc.wz/BossWill.img/Beholder is not found.", e);
        }
    }

    public BossWillField(int mapid, int channel, int returnMapId, float monsterRate) {
        super(mapid, channel, returnMapId, monsterRate);
    }

    public final void setWill(final int aoH, final int aoF, final int aoG) {
        this.dummy = aoH;
        this.mob1 = aoF;
        this.mob2 = aoG;
    }

    public final int getMob1() {
        return this.mob1;
    }

    public final int getDummy() {
        return this.dummy;
    }

    public final int getMob2() {
        return this.mob2;
    }

    public final void actionBeholder(final int n, final int n2, final int n3) {
        final List<List<Point>> list = BossWillConfig.get(n).get(n2);
        if ((list) != null) {
            for (Point point : list.get(Randomizer.nextInt(list.size()))) {
                this.broadcastMessage(WillPacket.WillBeholder(n3, point));
            }
            this.broadcastMessage(WillPacket.WillBeholder(n3, true, new Rectangle(this.getLeft(), -2634, this.getRight(), -2019)));
        }
    }

    public final void showWillHpBar(int[] array) {
        final MapleMonster mobObjectByID = this.getMobObjectByID(this.dummy);
        final MapleMonster mobObjectByID2 = this.getMobObjectByID(this.mob1);
        final MapleMonster mobObjectByID3 = this.getMobObjectByID(this.mob2);

        boolean mobObjectByIDNotNull = mobObjectByID != null;
        boolean mobObjectByID2NotNull = mobObjectByID2 != null;
        boolean mobObjectByID3NotNull = mobObjectByID3 != null;

        final MaplePacketLittleEndianWriter mplew;
        (mplew = new MaplePacketLittleEndianWriter()).writeShort(OutHeader.WillPhaseHp.getValue());
        mplew.writeInt(3);
        mplew.writeInt(666);
        mplew.writeInt(333);
        mplew.writeInt(3);
        mplew.write(mobObjectByIDNotNull);
        mplew.writeInt(mobObjectByID.getId());
        mplew.writeLong(mobObjectByID.getMobMaxHp());
        mplew.writeLong(mobObjectByID.getHp());
        mplew.write(mobObjectByID2NotNull);
        mplew.writeInt(mobObjectByID2.getId());
        mplew.writeLong(mobObjectByID2.getMobMaxHp());
        mplew.writeLong(mobObjectByID2.getHp());
        mplew.write(mobObjectByID3NotNull);
        mplew.writeInt(mobObjectByID3.getId());
        mplew.writeLong(mobObjectByID3.getMobMaxHp());
        mplew.writeLong(mobObjectByID3.getHp());
        this.broadcastMessage(mplew.getPacket());
    }

    public final void prepareCheckMobHp() {
        final MapleMonster mobObjectByID;
        if ((mobObjectByID = this.getMobObjectByID(this.mob2)) != null) {
            this.broadcastMessage(WillPacket.WillSkillAction(this.getMob2()), mobObjectByID.getPosition());
        }
    }

    public void checkMobHp(final boolean b) {
        int[] array = {666, 333, 3};
        final MapleMonster mobObjectByID = this.getMobObjectByID(this.dummy);
        final MapleMonster mobObjectByID2 = this.getMobObjectByID(this.mob1);
        final MapleMonster mobObjectByID3 = this.getMobObjectByID(this.mob2);
        if (mobObjectByID2 != null && mobObjectByID3 != null) {
            final int n;
            switch (n = (int) Math.ceil(mobObjectByID2.getHpLimitPercent() * 100.0)) {
                case 67: {
                    array = new int[]{666, 333, 3};
                    break;
                }
                case 34: {
                    array = new int[]{333, 3};
                    break;
                }
                case 1: {
                    array = new int[]{3};
                    break;
                }
            }
            if (b) {
                if (mobObjectByID2.getHPPercent() <= n && mobObjectByID2.getHPPercent() <= n) {
                    double hpLimitPercent = 0.003;
                    switch (n) {
                        case 67: {
                            array = new int[]{333, 3};
                            hpLimitPercent = 1.0 / 3;
                            break;
                        }
                        case 34: {
                            array = new int[]{3};
                            hpLimitPercent = 0.003;
                            break;
                        }
                        case 1: {
                            hpLimitPercent = 0.0;
                            this.killAllMonsters(true);
                            break;
                        }
                    }
                    mobObjectByID2.setHpLimitPercent(hpLimitPercent);
                    mobObjectByID2.removeEffect(Collections.singletonList(MonsterStatus.Invincible));
                    mobObjectByID3.setHpLimitPercent(hpLimitPercent);
                    mobObjectByID3.removeEffect(Collections.singletonList(MonsterStatus.Invincible));
                    if (mobObjectByID != null) {
                        mobObjectByID.setHpLimitPercent(hpLimitPercent);
                    }
                }
            } else {
                final long max = Math.max(mobObjectByID2.getHp(), mobObjectByID3.getHp());
                mobObjectByID2.setHp(max);
                mobObjectByID2.damage(null, 0, 0L, true);
                mobObjectByID3.setHp(max);
                mobObjectByID3.damage(null, 0, 0L, true);
                if (mobObjectByID != null) {
                    mobObjectByID.setHp(max);
                    mobObjectByID.damage(null, 0, 0L, true);
                }
            }
        }
        this.showWillHpBar(array);
    }

    public final void clearNarrowWeb() {
        this.lock.lock();
        try {
            this.aoJ.clear();
            final MaplePacketLittleEndianWriter hh;
            (hh = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.WillNarrowWeb);
            hh.writeInt(5);
            this.broadcastMessage(hh.getPacket());
        } finally {
            this.lock.unlock();
        }
    }

    public final void addNarrowWeb() {
        this.lock.lock();
        try {
            if (this.aoI.isEmpty()) {
                for (int i = 0; i < 10; ++i) {
                    final ArrayList<Point> list = new ArrayList<Point>();
                    for (int n = 900 - i * 80, j = 0; j < 360; j += (int) (32400.0 / (Math.PI * n))) {
                        list.add(new Point((int) (0.0 + n * Math.cos(Math.PI * (j - 90) / 180.0)), (int) (281.0 + n * Math.sin(Math.PI * (j - 90) / 180.0))));
                    }
                    Collections.shuffle(list);
                    this.aoI.put(i, list);
                }
            }
            int intValue = -1;
            Point point = null;
            for (final Map.Entry<Integer, List<Point>> entry : this.aoI.entrySet()) {
                final List<Pair<Integer, Point>> list2 = this.aoJ.get(entry.getKey());
                if ((list2) == null || list2.size() != entry.getValue().size()) {
                    intValue = entry.getKey();
                    point = entry.getValue().get((list2 == null) ? 0 : list2.size());
                    break;
                }
            }
            if (intValue != -1 && point != null) {
                List<Pair<Integer, Point>> list3 = this.aoJ.computeIfAbsent(intValue, k -> new ArrayList<>());
                final int nextInt;
                final int n2 = ((nextInt = Randomizer.nextInt(100)) < 5) ? 2 : ((nextInt < 35) ? 0 : 1);
                final int n3 = intValue * 100 + list3.size();
                final MaplePacketLittleEndianWriter mplew;
                (mplew = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.WillNarrowWeb);
                mplew.writeInt(3);
                mplew.writeInt(n3);
                mplew.writeInt(n2);
                mplew.writePosInt(point);
                int n7 = 80;
                switch (n2) {
                    case 0: {
                        n7 = 80;
                        break;
                    }
                    case 1: {
                        n7 = 160;
                        break;
                    }
                    case 2: {
                        n7 = 210;
                        break;
                    }
                }
                mplew.writeInt(n7);
                mplew.writeInt(n7);
                this.broadcastMessage(mplew.getPacket());
                list3.add(new Pair<>(n2, point));
            }
        } finally {
            this.lock.unlock();
        }
    }
}
