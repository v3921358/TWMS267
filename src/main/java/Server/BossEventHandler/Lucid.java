package Server.BossEventHandler;

import Client.MapleCharacter;
import Net.server.life.MapleLifeFactory;
import Net.server.life.MapleMonster;
import Net.server.maps.MapleMap;
import Opcode.Headler.OutHeader;
import Server.BossEventHandler.spawnL.Butterfly;
import Server.BossEventHandler.spawnL.FairyDust;
import tools.data.MaplePacketLittleEndianWriter;

import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Lucid {

    public static void start(MapleCharacter player) {
        MapleMap map = player.getMap();
        MapleMonster flower = MapleLifeFactory.getMonster(8880158);
        map.spawnMonsterOnGroundBelow(flower, new Point(1000, 43));
        map.getTimerInstance().scheduleAtFixedRate(() -> {
            player.send(createDargon());
        }, 1000,1000, TimeUnit.MILLISECONDS);
    }

    public static byte[] createDargon() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LUCID_DRAGON_CREATE.getValue());
        mplew.writeInt(1);
        mplew.writeInt(80);
        mplew.writeInt(-145);
        mplew.writeInt(80);
        mplew.writeInt(-659);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] createButterfly(int initId, List<Butterfly> butterflies) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LUCID_BUTTERFLY_CREATE.getValue());
        mplew.writeInt(initId);
        mplew.writeInt(butterflies.size());
        for (Butterfly butterfly : butterflies) {
            mplew.writeInt(butterfly.type);
            mplew.writeInt(butterfly.pos.x);
            mplew.writeInt(butterfly.pos.y);
        }
        return mplew.getPacket();
    }

    public static byte[] RemoveButterfly() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LUCID_BUTTERFLY_CREATE.getValue());
        mplew.writeInt(3);
        mplew.writeInt(3);
        mplew.writeInt(40);
        for (int i = 0; i < 40; i++) {
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static byte[] AttackButterfly(List<Integer> chrid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LUCID_BUTTERFLY_CREATE.getValue());
        mplew.writeInt(2);
        mplew.writeInt(3);
        mplew.writeInt(chrid.size());
        for (Integer chra : chrid) {
            mplew.writeInt(chra.intValue());
        }
        return mplew.getPacket();
    }

    public static byte[] setButterflyAction(Butterfly.Mode mode, int... args) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LUCID_BUTTERFLY_ACTION.getValue());
        mplew.writeInt(mode.code);
        switch (mode) {
            case ADD:
                mplew.writeInt(args[0]);
                mplew.writeInt(args[1]);
                mplew.writeInt(args[2]);
                mplew.writeInt(args[3]);
                break;
            case MOVE:
                mplew.writeInt(args[0]);
                mplew.writeInt(args[1]);
                break;
            case ATTACK:
                mplew.writeInt(args[0]);
                mplew.writeInt(args[1]);
                break;
        }
        return mplew.getPacket();
    }

    public static byte[] createDragon(int phase, int posX, int posY, int createPosX, int createPosY, boolean isLeft) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LUCID_DRAGON_CREATE.getValue());
        mplew.writeInt(phase);
        mplew.writeInt(posX);
        mplew.writeInt(posY);
        mplew.writeInt(createPosX);
        mplew.writeInt(createPosY);
        mplew.write(isLeft);
        return mplew.getPacket();
    }


    public static byte[] doFlowerTrapSkill(int level, int pattern, int x, int y, boolean flip) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LUCID_DO_SKILL.getValue());
        mplew.writeInt(238);
        mplew.writeInt(level);
        mplew.writeInt(pattern);
        mplew.writeInt(x);
        mplew.writeInt(y);
        mplew.write(flip);
        return mplew.getPacket();
    }

    public static byte[] doLaserRainSkill(int startDelay, List<Integer> intervals) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LUCID_DO_SKILL.getValue());
        mplew.writeInt(238);
        mplew.writeInt(5);
        mplew.writeInt(startDelay);
        mplew.writeInt(intervals.size());
        for (Iterator<Integer> iterator = intervals.iterator(); iterator.hasNext(); ) {
            int interval = iterator.next().intValue();
            mplew.writeInt(interval);
        }
        return mplew.getPacket();
    }

    public static byte[] doFairyDustSkill(int level, List<FairyDust> fairyDust) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LUCID_DO_SKILL.getValue());
        mplew.writeInt(238);
        mplew.writeInt(level);
        mplew.writeInt(fairyDust.size());
        for (FairyDust fd : fairyDust) {
            mplew.writeInt(fd.scale);
            mplew.writeInt(fd.createDelay);
            mplew.writeInt(fd.moveSpeed);
            mplew.writeInt(fd.angle);
        }
        return mplew.getPacket();
    }

    public static byte[] doForcedTeleportSkill(int splitId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LUCID_DO_SKILL.getValue());
        mplew.writeInt(238);
        mplew.writeInt(6);
        mplew.writeInt(splitId);
        return mplew.getPacket();
    }

    public static byte[] doRushSkill() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LUCID_DO_SKILL.getValue());
        mplew.writeInt(238);
        mplew.writeInt(8);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] SpawnLucidDream(List<Point> pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LUCID_DO_SKILL.getValue());
        mplew.writeInt(238);
        mplew.writeInt(12);
        mplew.writeInt(pos.size());
        for (Point p : pos) {
            mplew.writeInt(p.x);
            mplew.writeInt(p.y);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(p.x);
        }
        return mplew.getPacket();
    }

    public static byte[] setStainedGlassOnOff(boolean enable, List<String> tags) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LUCID2_STAINED_GLASS_ON_OFF.getValue());
        mplew.write(enable);
        mplew.writeInt(tags.size());
        for (String name : tags) {
            mplew.writeMapleAsciiString(name);
        }
        return mplew.getPacket();
    }

    public static byte[] breakStainedGlass(List<String> tags) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LUCID2_STAINED_GLASS_BREAK.getValue());
        mplew.writeInt(tags.size());
        for (String name : tags) {
            mplew.writeMapleAsciiString(name);
        }
        return mplew.getPacket();
    }

    public static byte[] setFlyingMode(boolean enable) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LUCID2_SET_FLYING_MODE.getValue());
        mplew.write(enable);
        return mplew.getPacket();
    }

    public static byte[] changeStatueState(boolean placement, int gauge, boolean used) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LUCID_STATUE_STATE_CHANGE.getValue());
        mplew.writeInt(placement ? 1 : 0);
        if (placement) {
            mplew.write(used);
        } else {
            mplew.writeInt(gauge);
            mplew.write(used);
        }
        return mplew.getPacket();
    }

    public static byte[] doShoot(int angle, int speed) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LUCID2_WELCOME_BARRAGE.getValue());
        mplew.writeInt(0);
        mplew.writeInt(angle);
        mplew.writeInt(speed);
        return mplew.getPacket();
    }

    public static byte[] doBidirectionShoot(int angleRate, int speed, int interval, int shotCount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LUCID2_WELCOME_BARRAGE.getValue());
        mplew.writeInt(3);
        mplew.writeInt(angleRate);
        mplew.writeInt(speed);
        mplew.writeInt(interval);
        mplew.writeInt(shotCount);
        return mplew.getPacket();
    }

    public static byte[] doSpiralShoot(int type, int angle, int angleRate, int angleDiff, int speed, int interval, int shotCount, int bulletAngleRate, int bulletSpeedRate) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LUCID2_WELCOME_BARRAGE.getValue());
        mplew.writeInt(type);
        mplew.writeInt(angle);
        mplew.writeInt(angleRate);
        mplew.writeInt(angleDiff);
        mplew.writeInt(speed);
        mplew.writeInt(interval);
        mplew.writeInt(shotCount);
        mplew.writeInt(bulletAngleRate);
        mplew.writeInt(bulletSpeedRate);
        return mplew.getPacket();
    }

    public static byte[] doWelcomeBarrageSkill(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LUCID2_WELCOME_BARRAGE.getValue());
        mplew.writeInt(type);
        return mplew.getPacket();
    }
}
