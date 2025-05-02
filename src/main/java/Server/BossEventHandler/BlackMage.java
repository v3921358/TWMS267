package Server.BossEventHandler;

import Client.MapleCharacter;
import Net.server.Obstacle;
import Net.server.Timer;
import Net.server.fieldskill.FieldSkill;
import Net.server.fieldskill.FieldSkillFactory;
import Net.server.life.MapleLifeFactory;
import Net.server.life.MapleMonster;
import Net.server.maps.MapleMap;
import Net.server.maps.MapleNodes;
import Opcode.Headler.OutHeader;
import Packet.CField;
import Packet.MaplePacketCreator;
import Packet.MobPacket;
import Server.BossEventHandler.Dusk.Dusk;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

// 使用完整路径来避免 Timer 冲突


public class BlackMage {

    public static void start(MapleCharacter player) {
        MapleMonster BLACK_MAGE_UNK_MONSTER = player.getMap().getMonsters().getFirst();
        MapleMap map = player.getMap();
        map.getTimerInstance().scheduleAtFixedRate(() -> {
            MapleMonster monster = BLACK_MAGE_UNK_MONSTER;
            long time = System.currentTimeMillis();
            MaplePacketLittleEndianWriter hp = new MaplePacketLittleEndianWriter();
            hp.writeShort(OutHeader.LP_FieldEffect.getValue());
            hp.write(8);
            hp.writeInt(8880505);
            hp.writeLong(player.getMap().getMonsters().getFirst().getHp() + player.getMap().getMonsters().getLast().getHp());
            hp.writeLong(player.getMap().getMonsters().getFirst().getMaxHP() + player.getMap().getMonsters().getLast().getMaxHP());
            hp.write(1);
            hp.write(5);
            player.send(hp.getPacket());
            if (time - monster.lastObstacleTime >= 20000) {
                monster.lastObstacleTime = time;
                List<Obstacle> obs = new ArrayList<>();
                for (int i = 0; i < 13; i++) {
                    Obstacle ob = new Obstacle(75, new Point(-1800 + (i * 200), -600), new Point(-1800 + (i * 200), 88), 25, 50, 1459, 125, 1, 653, 0);
                    ob.setVperSec(1000);
                    obs.add(ob);
                }

                for (int i = 0; i < obs.size(); ++i) {
                    Obstacle ob = obs.get(i);
                    Timer.MobTimer.getInstance().schedule(() -> {
                        player.getMap().broadcastMessage(MobPacket.createObstacle2(monster, ob, (byte) 4));
                    }, 500L * i);
                }
            }
            if (time - monster.lastThunderTime >= 40000) {
                monster.lastThunderTime = time;
                int[] pointz = {-400, -1000, -1600, 400, 1000, 1600};
                int count = 0;
                for (MapleMonster mob : player.getMap().getAllMonstersThreadsafe(true)) {
                    if (mob.getId() == 8880506) {
                        count++;
                    }
                    if (count < 6) {
                        MapleMonster thunder = MapleLifeFactory.getMonster(8880506);
                        for (int Count = 0; Count <= count; Count++) {
                            player.getMap().killMonster(thunder);
                        }
                        player.getMap().showWeatherEffectNotice("落下紅色閃電身體移動被受限。", 265, 3000);
                        player.getMap().spawnMonsterOnGroundBelow(thunder, new Point(pointz[Randomizer.nextInt(pointz.length)], 84));
                    }
                }
                if (time - monster.lastRedObstacleTime >= 55000) {
                    monster.lastRedObstacleTime = time;
                    MapleMonster redFloorL = MapleLifeFactory.getMonster(8880507);
                    MapleMonster redFloorR = MapleLifeFactory.getMonster(8880508);
                    int[] positions = {2172, 1988, 1824, 1650, 1476, 1302, 1128, 954, 780, 606};
                    int mobSize = player.getMap().getMobSizeByID(8880507);
                    if (mobSize < positions.length) {
                        int pos = positions[mobSize];
                        player.getMap().spawnMonsterOnGroundBelow(redFloorR, new Point(pos, 85));
                        player.getMap().spawnMonsterOnGroundBelow(redFloorL, new Point(-pos, 85));
                        player.getMap().showWeatherEffectNotice("慟哭的障壁竄出吞噬了空間。", 265, 3000);
                    }
                }

            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    public static void start2(MapleCharacter player) {
        MapleMonster BLACK_MAGE_UNK_MONSTER = MapleLifeFactory.getMonster(8880502);
        MapleMap map = player.getMap();
        map.getTimerInstance().scheduleAtFixedRate(() -> {
            MapleMonster monster = BLACK_MAGE_UNK_MONSTER;
//            if (player.getMap().getId() != 450013300) {
//                monster.getSchedule().cancel(true);
//                monster.setSchedule(null);
//            }
            long time = System.currentTimeMillis();
            if (time - monster.lastChainTime >= 8000) {
                monster.lastChainTime = System.currentTimeMillis();
                int useSkillPosRange = Randomizer.rand(1, 2);
                player.send(CField.useFieldSkill(100007, useSkillPosRange));
            }
            if (time - monster.lastObstacleTime >= 30000) {
                monster.lastObstacleTime = time;
                int x = Randomizer.rand(-1700, 1700);
                int[] angle = {973, 808, 690};
                int[] length = {5, 6, 7};

                List<Obstacle> obs = new ArrayList<>();
                for (int i = 0; i < 13; i++) {
                    Obstacle ob = new Obstacle(75, new Point(-1800 + (i * 200), -600), new Point(-1800 + (i * 200), 88), 25, 50, 1459, 125, 1, 653, 0);
                    ob.setVperSec(1000);
                    obs.add(ob);
                }

                for (int i = 0; i < obs.size(); ++i) {
                    Obstacle ob = obs.get(i);
                    Timer.MobTimer.getInstance().schedule(() -> {
                        player.getMap().broadcastMessage(MobPacket.createObstacle2(monster, ob, (byte) 4));
                    }, 500L * i);
                }

                for (int i = 0; i < 3; i++) {
                    Timer.MobTimer.getInstance().schedule(() -> {
                        Obstacle ob = new Obstacle(77, new Point(x, -600), new Point(x - 700, 88), 95, 35, 0x1F1, 125, length[Randomizer.nextInt(length.length)], angle[Randomizer.nextInt(angle.length)], 45);
                        ob.setVperSec(1000);
                        player.getMap().broadcastMessage(MobPacket.createObstacle2(monster, ob, (byte) 5));
                    }, 1000 * i);
                }
                if (time - monster.lastEyeTime >= 40000) {
                    monster.lastEyeTime = System.currentTimeMillis();
                    player.send(CField.useFieldSkill(100012, 1));
                }
                if (time - monster.lastLaserTime >= 60000) {
                    monster.lastLaserTime = System.currentTimeMillis();
                    player.send(getSelectLaser(88, 100017));
                    player.getMap().showWeatherEffectNotice("黑魔法師的紅色閃電壟罩所有地方，必須找尋躲藏之處。", 265, 3000);
                }
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    public static void start3(MapleCharacter player) {
        MapleMonster BLACK_MAGE_UNK_MONSTER = MapleLifeFactory.getMonster(8880503);
        MapleMap map = player.getMap();
        map.getTimerInstance().scheduleAtFixedRate(() -> {
            MapleMonster monster = BLACK_MAGE_UNK_MONSTER;
//            if (player.getMap().getId() != 450013500) {
//                monster.getSchedule().cancel(true);
//                monster.setSchedule(null);
//            }
            long time = System.currentTimeMillis();
            if (time - monster.lastSpearTime >= 1000) {
                monster.lastSpearTime = time;
                player.send(SpawnAngelAttack(new Point((int) player.getMap().getMonsters().getFirst().getPosition().getX(), (int) player.getMap().getMonsters().getFirst().getPosition().getY())));
            }
            if (time - monster.lastObstacleTime >= 30000) {
                monster.lastObstacleTime = time;
                int x = Randomizer.rand(-1700, 1700);
                int[] angle = {973, 808, 690};
                int[] length = {5, 6, 7};

                List<Obstacle> obs = new ArrayList<>();
                for (int i = 0; i < 13; i++) {
                    Obstacle ob = new Obstacle(75, new Point(-1800 + (i * 200), -600), new Point(-1800 + (i * 200), 88), 25, 50, 1459, 125, 1, 653, 0);
                    ob.setVperSec(1000);
                    obs.add(ob);
                }

                for (int i = 0; i < obs.size(); ++i) {
                    Obstacle ob = obs.get(i);
                    map.getTimerInstance().schedule(() -> {
                        player.getMap().broadcastMessage(MobPacket.createObstacle2(monster, ob, (byte) 4));
                    }, 500L * i, TimeUnit.MILLISECONDS);
                }

                for (int i = 0; i < 3; i++) {
                    map.getTimerInstance().schedule(() -> {
                        Obstacle ob = new Obstacle(77, new Point(x, -600), new Point(x - 700, 88), 95, 35, 0x1F1, 125, length[Randomizer.nextInt(length.length)], angle[Randomizer.nextInt(angle.length)], 45);
                        ob.setVperSec(1000);
                        player.getMap().broadcastMessage(MobPacket.createObstacle2(monster, ob, (byte) 5));
                    }, 1000 * i, TimeUnit.MILLISECONDS);
                }
            }
            if (time - monster.changePhase >= 500) {
                monster.changePhase = System.currentTimeMillis();
                if (player.getMap().getMonsters().getFirst() != null) {
                    byte phase;
                    if (player.getMap().getMonsters().getFirst().getHPPercent() > 75) {
                        phase = 1;
                    } else if (player.getMap().getMonsters().getFirst().getHPPercent() > 50) {
                        phase = 2;
                    } else if (player.getMap().getMonsters().getFirst().getHPPercent() > 25) {
                        phase = 3;
                    } else {
                        phase = 4;
                    }
                    if (player.getMap().getMonsters().getFirst().getPhase() != phase) {
                        player.getMap().getMonsters().getFirst().setPhase(phase);
                        player.getMap().broadcastMessage(Dusk.changePhase(player.getMap().getMonsters().getFirst().getObjectId(), phase));
                        player.getMap().broadcastMessage(MobPacket.changeMobZone(monster));
                    }

                    if (time - monster.lastRedObstacleTime >= 7000) {
                        monster.lastRedObstacleTime = time;
                        int randx = Randomizer.rand(-800, 800);
                        Obstacle ob = new Obstacle(76, new Point(randx, 400), new Point(randx, -600), 95, 10, 0x1F1, 30, 5, 1000, 45);
                        ob.setVperSec(500);
                        player.getMap().broadcastMessage(MobPacket.createObstacle2(monster, ob, (byte) 5));

                        randx = Randomizer.rand(-800, 800);
                        ob = new Obstacle(79, new Point(randx, 400), new Point(randx, -600), 95, 10, 0x1F1, 30, 5, 1000, 45);
                        ob.setVperSec(500);
                        player.getMap().broadcastMessage(MobPacket.createObstacle2(monster, ob, (byte) 5));
                    }
                    if (time - monster.lastLaserTime >= 4000) {
                        monster.lastLaserTime = time;

                        int randX = Randomizer.rand(-850, -750);

                        FieldSkill skill = new FieldSkill(100013, Randomizer.rand(1, 2));

                        List<FieldSkill.LaserInfo> infos = new ArrayList<>();
                        for (int i = 0; i < 3; ++i) {
                            infos.add(new FieldSkill.LaserInfo(new Point(randX + i * 500, Randomizer.rand(-400, 300)), Randomizer.rand(5, 50), Randomizer.rand(500, 900)));
                        }
                        skill.setLaserInfoList(infos);

                        player.getMap().broadcastMessage(CField.useFieldSkill(skill));
                        player.getMap().broadcastMessage(MobPacket.forcedSkillAction(player.getMap().getMonsters().getFirst().getObjectId(), 3, false));

                    }
                    if (time - monster.lastThunderTime >= 60000) {
                        monster.lastThunderTime = time;
                        int type = Randomizer.rand(1, 2);
                        List<MapleNodes.Environment> envs = new ArrayList<>();
                        for (MapleNodes.Environment env : player.getMap().getNodez().getEnvironments()) {
                            if (env.getName().contains("foo") && !env.getName().contains("foot")) {
                                env.setShow(true);
                                envs.add(env);
                            }
                        }

                        FieldSkill skill = new FieldSkill(100013, 1);
                        skill.setEnvInfo(envs);
                        player.getMap().broadcastMessage(CField.useFieldSkill(skill));
                        player.getMap().broadcastMessage(CField.getUpdateEnvironment(envs));
                        map.getTimerInstance().schedule(() -> {
                            player.getMap().broadcastMessage(MobPacket.forcedSkillAction(player.getMap().getMonsters().getFirst().getObjectId(), type - 1, true));
                            player.getMap().showWeatherEffectNotice("黑魔法師使用創造與破壞的權能，必須選擇要躲避至上方還是下方。", 265, 3000);
                            player.getMap().broadcastMessage(MaplePacketCreator.createObtacleAtom(40, 78, 78, player.getMap()));
                        }, 5000, TimeUnit.MILLISECONDS);
                        map.getTimerInstance().schedule(() -> {
                            if (type == 1) {

                                player.getMap().broadcastMessage(CField.useFieldSkill(FieldSkillFactory.getFieldSkill(100015, 1)));
//                             player.getMap().setBlackMage3rdSkill(true);
                            } else if (type == 2) {
                                List<Obstacle> obs = new ArrayList<>();

                                for (int i = 0; i < 29; i++) {
                                    int x = -930 + (i * 65);
                                    int y = 85;
                                    if (x <= -500 && x >= -630) {
                                        y = -298;
                                    }
                                    if (x <= -195 && x >= -380) {
                                        y = -211;
                                    }
                                    if (x <= 205 && x >= 75) {
                                        y = -90;
                                    }
                                    if (x <= 575 && x >= 395) {
                                        y = -108;
                                    }
                                    if (x <= 915 && x >= 660) {
                                        y = -309;
                                    }
                                    Point maxheight = new Point(x, -600);
                                    Point minheight = new Point(x, y);
                                    Obstacle ob = new Obstacle(78, maxheight, minheight, 100, 999, 450, 500 - i * 15, 2, y + 600, 0);
                                    obs.add(ob);
                                }

                                player.getMap().broadcastMessage(MobPacket.createObstacle(monster, obs, (byte) 0));
//
                            }
                        }, 7500, TimeUnit.MILLISECONDS);
                        map.getTimerInstance().schedule(() -> {
                            monster.setBarrier(1000000000000L);
                            player.getMap().broadcastMessage(MobPacket.mobBarrier(monster.getObjectId(), 100)); // %값
                        }, 15500, TimeUnit.MILLISECONDS);

                        map.getTimerInstance().schedule(() -> {
                            for (MapleNodes.Environment env : envs) {
                                env.setShow(false);
                            }

                            FieldSkill skills = new FieldSkill(100013, 1);
                            skills.setEnvInfo(envs);

//                         player.getMap().setBlackMage3rdSkill(false);
                            player.getMap().broadcastMessage(CField.useFieldSkill(skills));

                            player.getMap().broadcastMessage(CField.useFieldSkill(skills));
                            player.getMap().broadcastMessage(CField.getUpdateEnvironment(envs));
                        }, 15500, TimeUnit.MILLISECONDS);
                    }
                    if (time - monster.lastEyeTime >= 47720) {
                        monster.lastEyeTime = time;
                        player.getMap().showWeatherEffectNotice("破壞天使從虛無中被創造出來。", 265, 3000);
                        player.send(SpawnAngel(new Point(Randomizer.rand(0, 1960) - 970, Randomizer.rand(-380, -210)), false));
                        player.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880509), new Point(Randomizer.rand(0, 1960) - 970, 85)); // rand point
                        player.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880510), new Point(Randomizer.rand(0, 1960) - 970, 85));
                        player.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8880511), new Point(Randomizer.rand(0, 1960) - 970, 85));

                        map.getTimerInstance().schedule(() -> {
                            player.getMap().killMonster(8880509);
                            player.getMap().killMonster(8880510);
                            player.getMap().killMonster(8880511);
                        }, 20000, TimeUnit.MILLISECONDS);
                    }
                }
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    public static void start4(MapleCharacter player) {
        MapleMonster BLACK_MAGE_UNK_MONSTER = MapleLifeFactory.getMonster(8880504);
        player.setKeyValue("BlackMage", "0");
        MapleMap map = player.getMap();
        map.getTimerInstance().scheduleAtFixedRate(() -> {
            MapleMonster monster = BLACK_MAGE_UNK_MONSTER;
            long time = System.currentTimeMillis();
            if (time - monster.lastLaserTime >= 10000) {
                monster.lastLaserTime = time;
                player.getClient().ctx(OutHeader.LP_FieldSkillRequest.getValue(), "B0 86 01 00 01 00 00 00 04 00 00 00 74 04 00 00 00 8E FF FF FF 5F FE FF FF 8C 01 00 00 5D 00 00 00 01 00 00 00 C9 07 00 00 54 FD FF FF B5 FF FF FF 52 FF FF FF B3 01 00 00 00 00 00 00 C9 07 00 00 01 01 00 00 42 FF FF FF FF 02 00 00 40 01 00 00 00 00 00 00 D5 07 00 00 6E FC FF FF C1 FD FF FF 6C FE FF FF BF FF FF FF 00 00 00 00 02 07 00 00");
            }
            if (time - monster.lastChainTime >= 15000) {
                monster.lastChainTime = time;
                player.getClient().ctx(OutHeader.LP_FieldSkillRequest.getValue(), "B0 86 01 00 01 00 00 00 04 00 00 00 74 04 00 00 00 7A FE FF FF 6D FE FF FF 78 00 00 00 6B 00 00 00 01 00 00 00 8D 03 00 00 66 FC FF FF 1E FE FF FF 64 FE FF FF 1C 00 00 00 01 00 00 00 F4 08 00 00 76 01 00 00 2F FF FF FF 74 03 00 00 2D 01 00 00 00 00 00 00 56 08 00 00 5D FE FF FF 78 FF FF FF 5B 00 00 00 76 01 00 00 01 00 00 00 79 02 00 00");
            }
            if (time - monster.lastThunderTime >= 45000) {
                monster.lastThunderTime = time;
                player.getClient().ctx(OutHeader.LP_FieldSkillRequest.getValue(), "AE 86 01 00 01 00 00 00 0E 00 00 00 8C 0A 00 00 01 DF FC FF FF 6C FE FF FF 57 FD FF FF EE 00 00 00 03 00 00 00 00 00 00 00 57 FD FF FF 6C FE FF FF CF FD FF FF EE 00 00 00 03 00 00 00 00 00 00 00 CF FD FF FF 6C FE FF FF 47 FE FF FF EE 00 00 00 02 00 00 00 00 00 00 00 47 FE FF FF 6C FE FF FF BF FE FF FF EE 00 00 00 01 00 00 00 00 00 00 00 BF FE FF FF 6C FE FF FF 37 FF FF FF EE 00 00 00 02 00 00 00 00 00 00 00 37 FF FF FF 6C FE FF FF AF FF FF FF EE 00 00 00 02 00 00 00 00 00 00 00 AF FF FF FF 6C FE FF FF 27 00 00 00 EE 00 00 00 03 00 00 00 00 00 00 00 27 00 00 00 6C FE FF FF 9F 00 00 00 EE 00 00 00 02 00 00 00 00 00 00 00 9F 00 00 00 6C FE FF FF 17 01 00 00 EE 00 00 00 02 00 00 00 00 00 00 00 17 01 00 00 6C FE FF FF 8F 01 00 00 EE 00 00 00 01 00 00 00 00 00 00 00 8F 01 00 00 6C FE FF FF 07 02 00 00 EE 00 00 00 02 00 00 00 00 00 00 00 07 02 00 00 6C FE FF FF 7F 02 00 00 EE 00 00 00 03 00 00 00 00 00 00 00 7F 02 00 00 6C FE FF FF F7 02 00 00 EE 00 00 00 00 00 00 00 00 00 00 00 F7 02 00 00 6C FE FF FF 6F 03 00 00 EE 00 00 00 02 00 00 00 00 00 00 00");
                player.getMap().showWeatherEffectNotice("最接近於神之人的權能被激發出，必須選擇要蘊含著有創造還是破壞的力量。",265, 5000);
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    public static byte[] getSelectLaser(int type, int skillid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UserEffectLocal.getValue());
        mplew.write(type); // 37
        mplew.writeInt(skillid);
        switch (type) {
            case 88:
                mplew.writeInt(1);
                mplew.writeShort(2);
                break;
        }
        return mplew.getPacket();
    }

    public static byte[] getSelectPower(int type, int code) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_ActionBarResult.getValue());
        mplew.writeInt(type);
        mplew.writeInt(code);
        switch (type) {
            case 8:
                mplew.writeInt(1);
                mplew.writeInt(80002623);
                mplew.writeInt(3);
                mplew.writeInt(1);
                mplew.writeInt(1278807629);
                break;
            case 9:
                mplew.writeInt(80002623);
                mplew.writeInt(1);
                mplew.writeInt(1);
                break;
        }
        return mplew.getPacket();
    }

    public static byte[] SpawnAngel(Point xy, boolean hide) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_EmployeeEnterField.getValue());
        mplew.writeInt(47);
        mplew.writeInt(1);
        mplew.writePosInt(xy);
        mplew.write(hide);
        return mplew.getPacket();
    }

    public static byte[] SpawnAngelAttack(Point xy) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.SPEICAL_SUMMON_ATTACK.getValue());
        mplew.writeInt(47);
        mplew.writeInt(0);
        mplew.writePosInt(xy);
        mplew.writeInt(500001);
        mplew.writeInt(1);
        mplew.writeInt(183);
        mplew.writeInt(-1294967296);
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }
}
