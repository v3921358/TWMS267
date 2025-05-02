package Packet;

import Client.MapleCharacter;
import Net.server.life.MapleMonster;
import Net.server.maps.MapleMap;
import Opcode.Headler.OutHeader;
import Server.BossEventHandler.spider.spider;
import tools.Pair;
import tools.Triple;
import tools.data.MaplePacketLittleEndianWriter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WillPacket {

    public static byte[] setMoonGauge(int add) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.WILL_SET_MOON_GAUGE.getValue());
        mplew.writeInt(add);
        return mplew.getPacket();
    }

    public static byte[] addMoonGauge(int add) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.WILL_INFO_MOON_GAUGE.getValue());
        mplew.writeInt(add);
        return mplew.getPacket();
    }

    public static byte[] cooldownMoonGauge(int length) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.MOONLIGHT_SKILL_COOLDOWN.getValue());
        mplew.writeInt(length);
        return mplew.getPacket();
    }

    public static byte[] createBulletEyes(int... args) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.WILL_CREATE_BULLETEYE.getValue());
        mplew.writeInt(args[0]);
        mplew.writeInt(args[1]);
        mplew.writeInt(args[2]);
        mplew.writeInt(args[3]);
        if (args[0] == 1) {
            mplew.writeInt(1800);
            mplew.writeInt(5);
            mplew.write(true);
            mplew.writeInt(args[4]);
            mplew.writeInt(args[5]);
            mplew.writeInt(args[6]);
            mplew.writeInt(args[7]);
        }
        return mplew.getPacket();
    }

    public static byte[] setWillHp(List<Integer> counts, MapleMap map, int mobId1, int mobId2, int mobId3) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.WILL_SET_HP.getValue());
        MapleMonster life1 = map.getMonsterById(mobId1);
        MapleMonster life2 = map.getMonsterById(mobId2);
        MapleMonster life3 = map.getMonsterById(mobId3);
        mplew.writeInt(3);
        mplew.writeInt(666);
        mplew.writeInt(333);
        mplew.writeInt(3);
        mplew.write(life1 != null);
        if (life1 != null) {
            mplew.writeInt(mobId1);
            mplew.writeLong(life1.getHp());
            mplew.writeLong((long) ((double) life1.getMobMaxHp() * life1.bonusHp()));
        }
        mplew.write(life2 != null);
        if (life2 != null) {
            mplew.writeInt(mobId2);
            mplew.writeLong(life2.getHp());
            mplew.writeLong((long) ((double) life2.getMobMaxHp() * life2.bonusHp()));
        }
        mplew.write(life3 != null);
        if (life3 != null) {
            mplew.writeInt(mobId2);
            mplew.writeLong(life3.getHp());
            mplew.writeLong((long) ((double) life3.getMobMaxHp() * life3.bonusHp()));
        }
        return mplew.getPacket();
    }

    public static byte[] setWillHp(List<Integer> counts) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.WILL_SET_HP2.getValue());
        mplew.writeInt(counts.size());
        for (int i : counts) {
            mplew.writeInt(i);
        }
        return mplew.getPacket();
    }

    public static byte[] WillSpiderAttack(int id, int skill, int level, int type, List<Triple<Integer, Integer, Integer>> values) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.WILL_SPIDER_ATTACK.getValue());
        mplew.writeInt(id);
        mplew.writeInt(skill);
        mplew.writeInt(level);
        switch (level) {
            case 1:
            case 2:
            case 3:
            case 14: {
                if (level == 14) {
                    mplew.writeInt(type);
                }
                mplew.writeInt(level == 14 ? 9 : 4);
                mplew.writeInt(1200);
                mplew.writeInt(level == 14 ? 5000 : 9000);
                mplew.writeInt(level == 14 && type == 2 ? -60 : -40);
                mplew.writeInt(-600);
                mplew.writeInt(level == 14 && type == 2 ? 60 : 40);
                mplew.writeInt(10);
                mplew.writeInt(values.size());
                for (Triple<Integer, Integer, Integer> value : values) {
                    mplew.writeInt((Integer) value.left);
                    mplew.writeInt((Integer) value.mid);
                    mplew.writeInt((Integer) value.right);
                    mplew.writeInt(0);
                }
                break;
            }
            case 4: {
                mplew.writeInt(type);
                mplew.write(type != 0);
                break;
            }
            case 5: {
                mplew.writeInt(2);
                if (type == 0) {
                    mplew.write(0);
                    mplew.writeInt(-690);
                    mplew.writeInt(-455);
                    mplew.writeInt(695);
                    mplew.writeInt(160);
                    mplew.write(1);
                    mplew.writeInt(-690);
                    mplew.writeInt(-2378);
                    mplew.writeInt(695);
                    mplew.writeInt(-2019);
                    break;
                }
                mplew.write(0);
                mplew.writeInt(-690);
                mplew.writeInt(-2378);
                mplew.writeInt(695);
                mplew.writeInt(-2019);
                mplew.write(1);
                mplew.writeInt(-690);
                mplew.writeInt(-455);
                mplew.writeInt(695);
                mplew.writeInt(160);
            }
        }
        return mplew.getPacket();
    }

    public static byte[] WillSpiderAttackPaten(int id, int paten, List<Pair<Integer, Integer>> spider) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.WILL_SPIDER_ATTACK.getValue());
        mplew.writeInt(id);
        mplew.writeInt(242);
        mplew.writeInt(14);
        mplew.writeInt(paten);
        mplew.writeInt(9);
        mplew.writeInt(1200);
        mplew.writeInt(9000);
        mplew.writeInt(-40);
        mplew.writeInt(-600);
        mplew.writeInt(40);
        mplew.writeInt(10);
        mplew.writeInt(spider.size());
        int i = 0;
        for (Pair<Integer, Integer> a : spider) {
            mplew.writeInt(i);
            mplew.writeInt(a.getLeft());
            mplew.writeInt(a.getRight());
            mplew.writeInt(0);
            ++i;
        }
        return mplew.getPacket();
    }

    public static byte[] willUseSpecial() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.WILL_USE_SPECIAL.getValue());
        mplew.writeInt(8880300);
        mplew.writeInt(242);
        mplew.writeInt(5);
        mplew.writeInt(2);
        mplew.write(2);
        mplew.writeInt(-690);
        mplew.writeInt(-2621);
        mplew.writeInt(695);
        mplew.writeInt(-2019);
        mplew.write(1);
        mplew.writeInt(-690);
        mplew.writeInt(-442);
        mplew.writeInt(695);
        mplew.writeInt(160);
        return mplew.getPacket();
    }

    public static byte[] willStun() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.WILL_STUN.getValue());
        return mplew.getPacket();
    }

    public static byte[] willThirdOne() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.WILL_THIRD_ONE.getValue());
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] willSpider(boolean make, spider web) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.WILL_SPIDER.getValue());
        mplew.writeInt(make ? 3 : 4);
        mplew.writeInt(web.getObjectId());
        mplew.writeInt(web.getPattern());
        mplew.writeInt(web.getX1());
        mplew.writeInt(web.getY1());
        switch (web.getPattern()) {
            case 0:
                mplew.writeInt(100);
                mplew.writeInt(100);
                return mplew.getPacket();
            case 1:
                mplew.writeInt(160);
                mplew.writeInt(160);
                return mplew.getPacket();
            case 2:
                mplew.writeInt(270);
                mplew.writeInt(270);
                return mplew.getPacket();
        }
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] teleport() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.WILL_TELEPORT.getValue());
        mplew.writeInt(1);
        return mplew.getPacket();
    }


    public static byte[] WillBeholder(final int n, final Point point) {
        final MaplePacketLittleEndianWriter mplew;
        (mplew = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.WILL_CREATE_BULLETEYE);
        mplew.writeInt(0);
        mplew.writeInt(n);
        mplew.writePosInt(point);
        return mplew.getPacket();
    }

    public static byte[] WillBeholder(final int n, final boolean b, final Rectangle rectangle) {
        final MaplePacketLittleEndianWriter mplew;
        (mplew = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.WILL_CREATE_BULLETEYE);
        mplew.writeInt(1);
        mplew.writeInt(8880303);
        mplew.writeInt(300);
        mplew.writeInt(100);
        mplew.writeInt(1800);
        mplew.writeInt(5);
        mplew.writeBool(true);
        mplew.writeInt(-690);
        mplew.writeInt(-2611);
        mplew.writeInt(695);
        mplew.writeInt(-2019);
        return mplew.getPacket();
    }

    public static byte[] posion(MapleCharacter player, int objid, int type, int x, int y) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.WILL_POSION.getValue());
        mplew.writeInt(objid);
        mplew.writeInt(player.getId());
        mplew.write(type);
        mplew.writeInt(x);
        mplew.writeInt(y);
        return mplew.getPacket();
    }

    public static byte[] AttackPoison(MapleCharacter player, int objid, int type, int x, int y) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.WILL_POSION_REMOVE.getValue());
        mplew.writeInt(objid);
        mplew.writeInt(player.getId());
        mplew.write(type);
        mplew.writeInt(x);
        mplew.writeInt(y);
        return mplew.getPacket();
    }

    public static byte[] removePoison(MapleCharacter player, int objid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.WILL_POSION_REMOVE.getValue());
        mplew.writeInt(1);
        mplew.writeInt(objid);
        player.send(mplew.getPacket());
        return mplew.getPacket();
    }

    public static byte[] WillSkillAction(final int n, final int n2, final boolean b, final Point point,
                                         final Point point2, final Map<Integer, Pair<Integer, Integer>> map) {
        final MaplePacketLittleEndianWriter mplew;
        (mplew = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.WillSkillAction);
        mplew.writeInt(n);
        mplew.writeInt(242);
        mplew.writeInt(n2);
        switch (n2) {
            case 14: {
                mplew.writeInt((b ? 1 : 0));
            }
            case 1:
            case 2:
            case 3: {
                mplew.writeInt(4);
                mplew.writeInt(1200);
                mplew.writeInt(9000);
                mplew.writePosInt(point);
                mplew.writePosInt(point2);
                mplew.writeInt(map.size());
                for (Map.Entry<Integer, Pair<Integer, Integer>> entry : map.entrySet()) {
                    Integer n3 = entry.getKey();
                    Pair<Integer, Integer> ahg = entry.getValue();
                    mplew.writeInt(n3);
                    mplew.writeInt(ahg.getLeft());
                    mplew.writeInt(ahg.getRight());
                    mplew.writeInt(0);
                }
                break;
            }
        }
        return mplew.getPacket();
    }

    public static byte[] WillSkillAction(final int n, final int n2) {
        final MaplePacketLittleEndianWriter mplew;
        (mplew = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.WillSkillAction);
        mplew.writeInt(n);
        mplew.writeInt(242);
        mplew.writeInt(4);
        mplew.writeInt(n2);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] WillSkillAction(final int n) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        (mplew).writeOpcode(OutHeader.WillSkillAction);
        mplew.writeInt(n);
        mplew.writeInt(242);
        mplew.writeInt(5);
        final ArrayList<Pair<Boolean, Rectangle>> list = new ArrayList<>();
        list.add(new Pair<>(false, new Rectangle(-690, -455, 1385, 615)));
        list.add(new Pair<>(true, new Rectangle(-690, -2634, 1385, 615)));
        mplew.writeInt(list.size());
        for (final Pair<Boolean, Rectangle> ahg : list) {
            mplew.writeBool(ahg.getLeft());
            mplew.writeRect(ahg.getRight());
        }
        return mplew.getPacket();
    }

}
