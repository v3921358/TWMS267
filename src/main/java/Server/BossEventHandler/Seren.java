package Server.BossEventHandler;

import Client.MapleCharacter;
import Client.MapleClient;
import Net.server.Obstacle;
import Net.server.Timer;
import Net.server.life.MapleLifeFactory;
import Net.server.life.MapleMonster;
import Opcode.Headler.OutHeader;
import Packet.CField;
import Packet.MaplePacketCreator;
import Packet.MobPacket;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Seren {

    public static void start(MapleCharacter player, MapleMonster monster) {
        MapleMonster Seren = player.getMap().getMonsters().getFirst();
        Seren.setSchedule(Timer.MobTimer.getInstance().register(() -> {
            if(player.getMapId() != 410002020){
                Seren.getSchedule().cancel(true);
                Seren.setSchedule(null);
            }
            long time = System.currentTimeMillis();
                if (time - monster.lastObstacleTime >= 1000) {
                    monster.lastObstacleTime = time;
                    player.getMap().broadcastMessage(MaplePacketCreator.createObtacleAtom(2, 84, 84, player.getMap()));
                    player.addSerenGauge(+5);
                }
        },1000));
    }

    public static void startField2(MapleCharacter player, MapleMonster monster) {
        player.setSerenStunGauge(1);
        Seren.SerenHandler(player, monster);
        Seren.initDayUI(player);
        monster.setSchedule(Timer.MobTimer.getInstance().register(() -> {
            if(player.getMapId() != 410002060){
                monster.getSchedule().cancel(true);
                monster.setSchedule(null);
            }
            long time = System.currentTimeMillis();

            if(player.getMap().getMonsterById(8880607) != null) {
                if (time - monster.lastLaserTime >= 20 * 1000) {
                    monster.lastLaserTime = time;
                    player.send(CField.useFieldSkill(100023, 1));
                    player.addSerenGauge(+20);
                }
                if (time - monster.changePhase >= 112 * 1000) {
                    monster.changePhase = time;
                    player.send(Seren.changePhaseEffect());
                    player.send(Seren.SerenChangeBackground(2));
                    MapleMonster Sunset = MapleLifeFactory.getMonster(8880609);
                    MapleMonster Sunset1 = MapleLifeFactory.getMonster(8880610);

                    Sunset.setMaxHP(player.getMap().getMonsters().getFirst().getMaxHP());
                    Sunset.setHp(player.getMap().getMonsters().getFirst().getHp());
                    player.getMap().spawnMonsterOnGroundBelow(Sunset, new Point(-12, 305));
                    player.getMap().spawnMonsterOnGroundBelow(Sunset1, new Point(-12, 305));
                    player.getMap().killMonster(8880607);
                    player.getMap().killMonster(8880608);
                }
            }

            if(player.getMap().getMonsterById(8880609) != null) {
                if (time - monster.changePhase_II >= 222 * 1000) {
                    monster.changePhase_II = time;
                    player.send(Seren.changePhaseEffect());
                    player.send(Seren.SerenChangeBackground(3));
                    MapleMonster Night = MapleLifeFactory.getMonster(8880612);
                    MapleMonster Night1 = MapleLifeFactory.getMonster(8880613);
                    Night.setMaxHP(player.getMap().getMonsters().getFirst().getMaxHP());
                    Night.setHp(player.getMap().getMonsters().getFirst().getHp());
                    player.getMap().spawnMonsterOnGroundBelow(Night, new Point(-12, 305));
                    player.getMap().spawnMonsterOnGroundBelow(Night1, new Point(-12, 305));
                    player.getMap().killMonster(8880609);
                    player.getMap().killMonster(8880610);
                }
            }

            if(player.getMap().getMonsterById(8880612) != null) {
                if (time - monster.changePhase_III >= 272 * 1000) {
                    monster.changePhase_III = time;
                    player.send(Seren.changePhaseEffect());
                    player.send(Seren.SerenChangeBackground(4));
                    MapleMonster Dawn = MapleLifeFactory.getMonster(8880603);
                    MapleMonster Dawn1 = MapleLifeFactory.getMonster(8880604);
                    Dawn.setMaxHP(player.getMap().getMonsters().getFirst().getMaxHP());
                    Dawn.setHp(player.getMap().getMonsters().getFirst().getHp());
                    player.getMap().spawnMonsterOnGroundBelow(Dawn, new Point(-12, 305));
                    player.getMap().spawnMonsterOnGroundBelow(Dawn1, new Point(-12, 305));
                    player.getMap().killMonster(8880612);
                    player.getMap().killMonster(8880613);
                }
            }

            if(player.getMap().getMonsterById(8880603) != null) {
                if (time - monster.changePhase_IV >= 355 * 1000) {
                    monster.changePhase_IV = time;
                    player.send(Seren.changePhaseEffect());
                    player.send(Seren.SerenChangeBackground(1));
                    MapleMonster Noon = MapleLifeFactory.getMonster(8880607);
                    MapleMonster Noon1 = MapleLifeFactory.getMonster(8880608);
                    Noon.setMaxHP(player.getMap().getMonsters().getFirst().getMaxHP());
                    Noon.setHp(player.getMap().getMonsters().getFirst().getHp());
                    player.getMap().spawnMonsterOnGroundBelow(Noon, new Point(-12, 305));
                    player.getMap().spawnMonsterOnGroundBelow(Noon1, new Point(-12, 305));
                    player.getMap().killMonster(8880603);
                    player.getMap().killMonster(8880604);
                    Seren.initDayUI(player);
                }
            }
        },1000));
    }

    public static void SerenHandler(MapleCharacter player, MapleMonster monster) {
        switch (monster.getId()) {
            case 8880600:
                monster.setSchedule(Timer.MobTimer.getInstance().register(() -> {
                    List<Obstacle> obs = new ArrayList<>();
                    for (int i = 0; i < 3; i++) {
                        int x = Randomizer.rand(-1030, 1030);
                        Obstacle ob = new Obstacle(84, new Point(x, -440), new Point(x, 275), 30, 15, i * 1000, Randomizer.rand(16, 32), 3, 715, 0);
                        obs.add(ob);
                    }
                    for (MapleCharacter chr : player.getMap().getAllChracater()) {
                        if (chr.isAlive())
                            chr.addSerenGauge(-10);
                    }
                }, 5000L));
                break;
            case 8880601:
            case 8880604:
            case 8880608:
            case 8880613: {
                int time = monster.getId() == 8880601 ? 7000 : Randomizer.rand(7000, 13000);
                monster.setSchedule(Timer.MobTimer.getInstance().register(() -> {
                    player.getMap().broadcastMessage(MobPacket.enableOnlyFsmAttack(monster, 1, 0));
                }, time));
                break;
            }
            case 8880602:
                monster.ResetSerenTime(true);
                monster.setSchedule(Timer.MobTimer.getInstance().register(() -> {
                    if (player.getMap().getAllChracater().size() > 0 && monster.getCustomValue0(8880603) == 0L) {
                        monster.addSkillCustomInfo(8880602, 1L);
                        if (monster.getCustomValue0(8880602) >= (long)(monster.getSerenTimetype() == 3 ? 1 : 5)) {
                            monster.removeCustomInfo(8880602);
                            if (monster.getSerenTimetype() == 4) {
                                monster.gainShield(monster.getStats().getHp() / 100L, monster.getShield() <= 0L, 0);
                            }

                            Iterator var1 = player.getMap().getAllChracater().iterator();

                            while(var1.hasNext()) {
                                MapleCharacter chr = (MapleCharacter)var1.next();
                                if (chr.isAlive()) {
                                    chr.addSerenGauge(monster.getSerenTimetype() == 3 ? -20 : 20);
                                }
                            }
                        }

                        monster.AddSerenTimeHandler(monster.getSerenTimetype(), -1);
                    }

                }, 1000L));
                break;
            case 8880603:
                monster.setSchedule(Timer.MobTimer.getInstance().register(() -> {
                    if (monster == null || player.getMap().getMonsterById(8880603) == null) {
                        monster.getSchedule().cancel(true);
                        monster.setSchedule(null);
                    }

                    if (monster != null) {
                        for (int i = 0; i < 2; ++i) {
                            int time = i == 0 ? 10 : 1000;
                            Timer.MobTimer.getInstance().schedule(() -> {
                                player.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880606), new Point(470, 305));
                                player.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880606), new Point(-10, 305));
                                player.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880606), new Point(-450, 305));
                            }, time);
                        }
                    }

                }, Randomizer.rand(7000, 11000)));
                break;

            case 8880605:
                monster.setSchedule(Timer.MobTimer.getInstance().register(() -> {
                    for (MapleCharacter chr : player.getMap().getAllChracater()) {
                        if (chr.isAlive() && (chr.getPosition()).x - 300 <= (monster.getPosition()).x && (chr.getPosition()).x + 300 >= (monster.getPosition()).x) {
                            monster.addSkillCustomInfo(8880605, 1L);
                            if (monster.getCustomValue0(8880605) >= 10L) {
                                monster.switchController(chr.getClient().getRandomCharacter());
                                monster.removeCustomInfo(8880605);
                            }
                            player.getMap().broadcastMessage(MobPacket.enableOnlyFsmAttack(monster, 1, 0));
                            break;
                        }
                    }
                }, 2000L));
                break;
            case 8880606:
            case 8880609:
            case 8880611:
            case 8880612:
                break;
            case 8880607:
                break;
            case 8880610:
                monster.setSchedule(Timer.MobTimer.getInstance().register(() -> {
                    if (monster != null) {
                        int type = Randomizer.rand(0, 2);
                        if (type == 0) {
                            player.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880611), new Point(-320, 305));
                            player.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880611), new Point(470, 305));
                        } else {
                            player.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880611), (type == 1) ? new Point(-320, 305) : new Point(470, 305));
                        }
                    }
                }, Randomizer.rand(30000, 50000)));
                break;
        }
    }

    // 2031
    public static void initDayUI(MapleCharacter player) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_SEREN_MOVE_DAY_TIME.getValue());
        mplew.writeInt(0);
        mplew.writeInt(400000);
        mplew.writeInt(108);
        mplew.writeInt(108);
        mplew.writeInt(36); // 夜晚
        mplew.writeInt(108);
        player.getClient().getSession().write(mplew.getPacket());
    }

    // 2031
    public static void MoveDayTime(MapleCharacter player, int move) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BOSS_SEREN_MOVE_DAY_TIME.getValue());
        mplew.writeInt(3); // TIME TYPE;
        mplew.writeInt(305 % 360 + move);
        player.send(mplew.getPacket());
    }



    /* 1=正午 / 2=黃昏 / 3=夜晚 / 4=黎明 */
    public static byte[] changePhaseEffect() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_FieldSkillRequest.getValue());
        mplew.writeInt(100024);
        mplew.writeInt(1);
        mplew.writeInt(7);
        mplew.writeInt(5);
        mplew.writeInt(3060);
        mplew.writeInt(2700);
        mplew.writeInt(0);
        mplew.writeInt(8880602); //mplew.writeInt(8880602);
        mplew.writeInt(0);
        mplew.writeInt(1);
        mplew.writeInt(4);
        mplew.writeInt(8880603);  //mplew.writeInt(8880603);
        mplew.writeInt(8880607); //mplew.writeInt(8880607);
        mplew.writeInt(8880609); //mplew.writeInt(8880609);
        mplew.writeInt(8880612); //mplew.writeInt(8880612);
        return mplew.getPacket();
    }

    public static byte[] StageOneUIInfo(MapleClient c, int add) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.SEREN_STUN_GAUGE.getValue());
        if (c.getPlayer() != null) {
            mplew.writeInt(1200); // max
            mplew.writeInt(add);
            return mplew.getPacket();
        }
        return null;
    }


    public static byte[] StageTwoTimerInfo(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.SEREN_TIMER.getValue());
        mplew.writeInt(0);
        mplew.writeInt(400000);
        mplew.writeInt(108);
        mplew.writeInt(108);
        mplew.writeInt(36);
        mplew.writeInt(108);
        c.announce(mplew.getPacket());
        return mplew.getPacket();
    }


    public static byte[] StageTwoMoveTimer(MapleClient c, int add) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.SEREN_TIMER.getValue());
        mplew.writeInt(3);
        mplew.writeInt(307 + add); // 307 為起始 2秒 +1值
        c.announce(mplew.getPacket());
        return mplew.getPacket();
    }


    public static byte[] changeFieldMapType(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_FieldSkillRequest.getValue());
        mplew.writeInt(100024);
        mplew.writeInt(2);
        mplew.writeInt(7);
        mplew.writeInt(5);
        mplew.writeInt(3720);
        mplew.writeInt(3360);
        mplew.writeInt(0);
        mplew.writeInt(8880632);
        mplew.writeInt(0);
        mplew.writeInt(1);
        mplew.writeInt(4);
        mplew.writeInt(8880633);
        mplew.writeInt(8880637);
        mplew.writeInt(8880639);
        mplew.writeInt(8880642);
        c.announce(mplew.getPacket());
        return mplew.getPacket();
    }


    /* 雷射集中 -50% */
    public static void SerenRazerHit(MaplePacketReader slea, MapleClient c, MapleCharacter player) {
        if (player == null || player.getMap() == null) return;
        if (slea.readByte() != 0 && !player.isInvincible()) {
            player.addHPMP(-25, 0);  // 打中 -50% 是官方設定 , 但是是兩下 = 50%
            player.addSerenGauge(+25); // 增加日光儀表
        }
    }


    /* 設置時間刻度 */
    public static void setTime(MapleCharacter player, int x, int y) {
        final MaplePacketLittleEndianWriter hh;
        (hh = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.SEREN_TIMER);
        int[] dayTimeArr = new int[]{110, 110, 30, 110};
        hh.writeInt(x);
        hh.writeInt(y);
        for (int i = 0; i < 4; i++) {
            hh.writeInt(dayTimeArr[i]);
        }
        player.getClient().announce(hh.getPacket());
    }

    /* 更新資訊 */
    public static void SerenUpdateInfo(MaplePacketReader slea, MapleClient c, MapleCharacter player) {
    }

    /* SCRIPT HIT PLAYER -SUNLIGHT 操作 */
    /* INFO 氣候視窗UI PACKET決定起始點 MAX 360 INT 一圈 */
    public static void updateTimeChart(MapleCharacter player, int daytime, MapleMonster monster) {
        final MaplePacketLittleEndianWriter hh;
        (hh = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.SEREN_TIMER);
        hh.writeInt(3);// 3=修改指針
        hh.writeInt(360 % daytime);
        //指針位置
        hh.writeInt(0);
        hh.writeInt(0);
        hh.writeInt(0);
        player.getMap().broadcastMessage(hh.getPacket());
    }


    public static void initTime(MapleCharacter player, int daytime, MapleMonster monster) {
        final MaplePacketLittleEndianWriter hh;
        (hh = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.SEREN_TIMER);
        hh.writeInt(3);// 3=修改指針
        hh.writeInt(daytime);//指針位置
        hh.writeInt(0);
        hh.writeInt(0);
        hh.writeInt(0);
        player.getMap().broadcastMessage(hh.getPacket());
    }

    /* 410002060 交叉光炮軌跡 */
    public static void seren_unk1715(MapleCharacter player, int mobId, int second) { //不知道幹啥用
        final MaplePacketLittleEndianWriter hh;
        (hh = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.SEREN_UNK);
        hh.writeInt(mobId);
        hh.writeInt(second);
        player.getMap().broadcastMessage(hh.getPacket());
    }

    /* OnPacket:: */

    public static void modifySunlightValue(MapleCharacter player, int max, int now) {
        final MaplePacketLittleEndianWriter hh;
        (hh = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.SEREN_STUN_GAUGE);
        hh.writeInt(max);
        hh.writeInt(now);
        player.getClient().announce(hh.getPacket());
    }


    /* 黎明狀態 召喚術 + 戴斯克 add filed skill */
    public static byte[] SerenSpawnAttack(final int mobId, final boolean isFacingLeft, Point point) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.SEREN_SPAWN_OTHER_MIST.getValue());
        mplew.write(0);
        mplew.writeInt(263);
        mplew.writeInt(1);
        mplew.writeInt(mobId);
        mplew.writeBool(isFacingLeft);
        mplew.writeInt(point.x);
        mplew.writeInt(point.y);
        return mplew.getPacket();
    }

    /* 六角光束 */
    public static byte[] SerenRazerAttack(MapleCharacter player, MapleMonster mob, int skilllv, int delaytime) {
        final MaplePacketLittleEndianWriter mplew;
        (mplew = new MaplePacketLittleEndianWriter()).writeOpcode(OutHeader.SEREN_MOB_LASER);
        mplew.writeInt(mob.getObjectId());
        mplew.writeInt(skilllv);
        mplew.writeInt(delaytime);
        List<Point> pos = new ArrayList<>();
        if (skilllv == 1) {
            int rand = Randomizer.rand(2, 6);
            if (rand == 0) {
                pos.add(new Point(-2511, -780));
                pos.add(new Point(-1300, -2359));
                pos.add(new Point(673, -2618));
                pos.add(new Point(2252, -1407));
                pos.add(new Point(2511, 566));
                pos.add(new Point(1300, 2145));
                pos.add(new Point(-673, 2404));
                pos.add(new Point(-2252, 1193));
            } else if (rand == 1) {
                pos.add(new Point(-2252, -1407));
                pos.add(new Point(-673, -2618));
                pos.add(new Point(1300, -2359));
                pos.add(new Point(2511, -780));
                pos.add(new Point(2252, 1193));
                pos.add(new Point(673, 2404));
                pos.add(new Point(-1300, 2145));
                pos.add(new Point(-2511, 566));
            } else if (rand == 2) {
                pos.add(new Point(-2600, -107));
                pos.add(new Point(-1838, -1945));
                pos.add(new Point(0, -2707));
                pos.add(new Point(1838, -1945));
                pos.add(new Point(2600, -107));
                pos.add(new Point(1838, 1731));
                pos.add(new Point(0, 2493));
                pos.add(new Point(-1838, 1731));
            } else if (rand == 3) {
                pos.add(new Point(-2561, -558));
                pos.add(new Point(-1491, -2237));
                pos.add(new Point(451, -2668));
                pos.add(new Point(2130, -1598));
                pos.add(new Point(2561, 344));
                pos.add(new Point(1491, 2023));
                pos.add(new Point(-451, 2454));
                pos.add(new Point(-2130, 1384));
            } else if (rand == 4) {
                pos.add(new Point(-2130, -1598));
                pos.add(new Point(-451, -2668));
                pos.add(new Point(1491, -2237));
                pos.add(new Point(2561, -558));
                pos.add(new Point(2130, 1384));
                pos.add(new Point(451, 2454));
                pos.add(new Point(-1491, 2023));
                pos.add(new Point(-2561, 344));
            } else if (rand == 5) {
                pos.add(new Point(-1992, -1778));
                pos.add(new Point(-227, -2697));
                pos.add(new Point(1671, -2099));
                pos.add(new Point(2590, -334));
                pos.add(new Point(1992, 1564));
                pos.add(new Point(227, 2483));
                pos.add(new Point(-1671, 1885));
                pos.add(new Point(-2590, 120));
            } else if (rand == 6) {
                pos.add(new Point(-2443, -996));
                pos.add(new Point(-1099, -2463));
                pos.add(new Point(889, -2550));
                pos.add(new Point(2356, -1206));
                pos.add(new Point(2443, 782));
                pos.add(new Point(1099, 2249));
                pos.add(new Point(-889, 2336));
                pos.add(new Point(-2356, 992));
            }
        } else {
            pos.add(new Point(1300, 0));
            pos.add(new Point(1800, -651));
            pos.add(new Point(1300, -1400));
            pos.add(new Point(750, -3500));
            pos.add(new Point(-750, -3500));
            pos.add(new Point(-1900, -1700));
            pos.add(new Point(-1900, -500));
            pos.add(new Point(-800, 200));
            pos.add(new Point(-800, 900));
            pos.add(new Point(2200, 1600));
            pos.add(new Point(-10, 0));
            pos.add(new Point(40, 0));
        }
        mplew.writeInt(pos.size());
        for (Point po : pos) {
            mplew.writeInt(0);
            mplew.writeInt(-107);
            mplew.writePosInt(po);
        }
        player.addSerenGauge(+25);
        return mplew.getPacket();
    }

    /* stage */
    public static byte[] SerenChangePhase(String str, int type, MapleMonster mob) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.SEREN_CHANGE_PHASE.getValue());
        mplew.writeInt(mob.getObjectId());
        mplew.writeMapleAsciiString(str);
        mplew.writeInt(type);
        mplew.writeInt(0);
        mplew.writeInt(1);
        mplew.writeInt(mob.getId());
        return mplew.getPacket();
    }

    public static byte[] SerenTimer(int value, int... info) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.SEREN_TIMER.getValue());
        mplew.writeInt(value);
        switch (value) {
            case 0:
                mplew.writeInt(info[0]);
                mplew.writeInt(info[1]);
                mplew.writeInt(info[2]);
                mplew.writeInt(info[3]);
                mplew.writeInt(info[4]);
                break;
            case 1:
                mplew.writeInt(info[0]);
                mplew.writeInt(info[1]);
                mplew.writeInt(info[2]);
                mplew.writeInt(info[3]);
                mplew.writeInt(info[4]);
                break;
            case 2:
                mplew.write(info[0]);
                break;
            case 3:
                mplew.writeInt(0);
                break;
        }
        return mplew.getPacket();
    }

    public static byte[] SerenChangeBackground(int code) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.SEREN_BACKGROUND_CHANGE.getValue());
        mplew.writeInt(code);
        return mplew.getPacket();
    }

    public static byte[] SerenUserStunGauge(int max, int now) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.SEREN_STUN_GAUGE.getValue());
        mplew.writeInt(max);
        mplew.writeInt(now);
        return mplew.getPacket();
    }

    public static void changePhaseEffect(MapleClient c, int BOSS, int change) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_FieldSkillRequest.getValue());
        mplew.writeInt(100024);
        mplew.writeInt(2);
        mplew.writeInt(7);
        mplew.writeInt(5);
        mplew.writeInt(3720);
        mplew.writeInt(3360);
        mplew.writeInt(0);
        mplew.writeInt(BOSS);
        mplew.writeInt(0);
        mplew.writeInt(1);

        mplew.writeInt(4);
        mplew.writeInt(change);
        mplew.writeInt(8880637);
        mplew.writeInt(8880639);
        mplew.writeInt(8880642);
        c.announce(mplew.getPacket());
    }

    public static void showSerenHp(MapleClient c, int Seren) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(636);
        mplew.write(8);
        mplew.writeInt(Seren);
        mplew.writeLong(2066186662);
        mplew.writeLong(2066186662);
        mplew.write(1);
        mplew.write(5);
        c.announce(mplew.getPacket());
    }

    public static void changePhase(MapleClient c, int Seren) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.SEREN_CHANGE_PHASE.getValue());
        mplew.writeInt(Seren);
        mplew.writeInt(2);
        mplew.write(1);
        mplew.write(0);
        c.announce(mplew.getPacket());
    }

    public static void spawnAttackByMoon(MapleClient c, int obj) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.SEREN_SPAWN_OTHER_MIST.getValue());
        mplew.write(0);
        mplew.writeInt(263);
        mplew.writeInt(1);
        mplew.writeInt(c.getPlayer().getMap().getMonsters().getFirst().getObjectId());
        mplew.write(c.getPlayer().getMap().getMonsters().getFirst().isFacingLeft());
        mplew.writePosInt(c.getPlayer().getPosition());
        c.announce(mplew.getPacket());
    }

    public static void addHpFieldSkill(MapleClient c, int obj) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.BLACK_MAGE_SHIELD.getValue());
        mplew.writeInt(obj);
        mplew.writeInt(20);
        mplew.writeLong(1050000000);
        c.announce(mplew.getPacket());
    }

}
