package Server.channel.handler;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.SecondaryStat;
import Net.server.Timer;
import Net.server.life.MapleMonster;
import Net.server.life.MobSkill;
import Net.server.life.MobSkillFactory;
import Net.server.maps.MapleAffectedArea;
import Net.server.maps.MapleFoothold;
import Opcode.Headler.OutHeader;
import Packet.EffectPacket;
import Packet.MaplePacketCreator;
import Packet.MobPacket;
import Server.BossEventHandler.Lucid;
import Server.BossEventHandler.spawnL.Butterfly;
import Server.BossEventHandler.spawnL.FieldLucid;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class MobSkillDelayEndHandler {

    public static void handlePacket(MaplePacketReader slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if (chr == null || chr.getMap() == null) return;
        int oid = slea.readInt();
        int skillID = slea.readInt();
        int level = slea.readInt();
        int n = (slea.readByte() > 0) ? slea.readInt() : 0;
        if (chr.isGm()) {
            chr.dropMessage(15, "[skill-1215] skill="+skillID+", level="+level);
        }
        MobSkill skill = MobSkillFactory.getMobSkill(skillID, level);
        int skillid = skill.getSourceId();
        MapleMonster monster = chr.getMap().getMonsterByOid(oid);
        if (skill == null || monster == null) return;
        if (monster.getStats().getSelfD() != -1) {
            chr.getMap().monsterSelfDestruct(monster);
        }
        switch (skillID) {
            case 170: {
                if (level == 42) {
                    final HashMap<Integer, Point> hashMap = new HashMap<>();
                    for (int i = 0; i < 3; ++i) {
                        hashMap.put(i, new Point(monster.getPosition().x + i * (monster.isFacingLeft() ? -250 : 250), monster.getPosition().y));
                    }
                    chr.getMap().broadcastMessage(MobPacket.monsterDemianDelayedAttackCreate(monster.getObjectId(), 1, hashMap, monster.isFacingLeft()));
                    return;
                }
                if (level > 44 && level <= 47) {
                    final Point point = new Point(monster.getPosition().x + (monster.isFacingLeft() ? -600 : 600), monster.getPosition().y);
                    chr.getMap().broadcastMessage(MobPacket.teleportMonster(monster.getObjectId(), false, 10, point, 0));
                    chr.getMap().broadcastMessage(MobPacket.monsterDemianDelayedAttackCreate(monster.getObjectId(), level, 1, 1, point, monster.isFacingLeft()));
                    return;
                }
                chr.getMap().broadcastMessage(MobPacket.spawnMonster(monster), monster.getPosition());
                final Point point2 = new Point(Randomizer.rand(-650, 660), monster.getPosition().y - 2);
                MapleFoothold fh = chr.getMap().getFootholds().findBelow(point2);
                chr.getMap().broadcastMessage(MobPacket.teleportMonster(chr.getObjectId(), false, skill.getX(), point2, fh == null ? 0 : fh.getId()));
                return;
            }
            case 176: {
                switch (level) {
                    case 25:
                    case 26: {
                        int n2 = 0;
                        switch (monster.getId() % 100) {
                            case 3: {
                                n2 = -81;
                                break;
                            }
                            case 4: {
                                n2 = -190;
                                break;
                            }
                            case 5: {
                                n2 = -322;
                                break;
                            }
                            case 6: {
                                n2 = -448;
                                break;
                            }
                            case 7: {
                                n2 = 65;
                                break;
                            }
                            case 8: {
                                n2 = 218;
                                break;
                            }
                            case 9: {
                                n2 = 362;
                                break;
                            }
                            case 10: {
                                n2 = 508;
                                break;
                            }
                        }
                        if (chr.getPosition().distance(new Point(n2, 85)) >= 75) {
                            break;
                        }
                        c.announce(EffectPacket.showMobSkillHit(-1, skillID, level));
                        chr.getMap().broadcastMessage(chr, EffectPacket.showMobSkillHit(chr.getId(), skillID, level), false);
                        if (chr.getBuffedValue(SecondaryStat.RoyalGuardState) != null) {
                            c.announce(EffectPacket.showRoyalGuardAttack());
                            break;
                        }
                        if (chr.getBuffedValue(SecondaryStat.NotDamaged) == null) {
                            chr.addHPMP(-chr.getStat().getCurrentMaxHP(), 0, false, false);
                        }
                        break;
                    }
                    case 27: {
                        final int n3 = (int)chr.getPosition().getY();
                        int n4 = 0;
                        int n5 = 0;
                        switch (monster.getId() % 100) {
                            case 3:
                            case 7: {
                                n5 = -190;
                                n4 = -260;
                                break;
                            }
                            case 4:
                            case 8: {
                                n5 = -109;
                                n4 = -260;
                                break;
                            }
                            case 5:
                            case 9: {
                                n5 = -15;
                                n4 = -83;
                                break;
                            }
                        }
                        if (n3 <= n4 || n3 >= n5) {
                            break;
                        }
                        c.announce(EffectPacket.showMobSkillHit(-1, skillID, level));
                        chr.getMap().broadcastMessage(chr, EffectPacket.showMobSkillHit(chr.getId(), skillID, level), false);
                        if (chr.getBuffedValue(SecondaryStat.RoyalGuardState) != null) {
                            c.announce(EffectPacket.showRoyalGuardAttack());
                            break;
                        }
                        if (chr.getBuffedValue(SecondaryStat.NotDamaged) == null) {
                            chr.addHPMP(-chr.getStat().getCurrentMaxHP(), 0, false, false);
                        }
                        break;
                    }
                }
                return;
            }
            case 211:
            case 227: {
                MapleAffectedArea area = new MapleAffectedArea(skill.calculateBoundingBox(monster.getPosition(), monster.isFacingLeft()), monster, skill, monster.getPosition());
                area.setAreaType(1);
                area.setSubtype((skillID == 227) ? 8 : 0);
                chr.getMap().createAffectedArea(area);
                break;
            }
            case 217: {
                chr.getMap().broadcastMessage(MobPacket.bounceAttackSkill(oid, skill, chr.getPosition()));
                return;
            }
            case 226: {
                chr.getMap().broadcastMessage(chr, MaplePacketCreator.userTossedBySkill(chr.getId(), oid, skill), true);
                return;
            }
            case 230: {
                if (chr.getMap().getRandRect().get(10 - n).contains(chr.getPosition())) {
                    c.announce(EffectPacket.showMobSkillHit(-1, skillID, level));
                    chr.getMap().broadcastMessage(chr, EffectPacket.showMobSkillHit(chr.getId(), skillID, level), false);
                    chr.addHPMP(-chr.getStat().getCurrentMaxHP(), 0, false, false);
                    return;
                }
                break;
            }
            case 238: {
                final ArrayList<Integer> list = new ArrayList<>();
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(OutHeader.LUCID_DO_SKILL.getValue());
                mplew.writeInt(238);
                mplew.writeInt(level);

                switch (level) {
                    case 1: {
                        mplew.writeInt(0);
                        mplew.writeInt(1000);
                        mplew.writeInt(43);
                        mplew.write(1);
                        c.announce(mplew.getPacket());
                        break;
                    }
                    case 2: {
                        mplew.writeInt(1);
                        mplew.writeInt(1000);
                        mplew.writeInt(43);
                        mplew.write(1);
                        c.announce(mplew.getPacket());
                        break;
                    }
                    case 3: {
                        mplew.writeInt(2);
                        mplew.writeInt(1000);
                        mplew.writeInt(43);
                        mplew.write(1);
                        c.announce(mplew.getPacket());
                        break;
                    }
                    case 12: {
                        mplew.writeInt(2);
                        mplew.writeInt(750);
                        mplew.writeInt(44);
                        mplew.writeInt(1290);
                        mplew.writeInt(45);
                        mplew.writeInt(1200);
                        mplew.writeInt(1620);
                        mplew.writeInt(44);
                        mplew.writeInt(1930);
                        mplew.writeInt(45);
                        mplew.writeInt(1662);
                        c.announce(mplew.getPacket());
                        break;
                    }
                    case 4: {
                        int x = Randomizer.rand(1, 360);
                        int y = Randomizer.rand(1, 360);
                        int z = Randomizer.rand(1, 360);
                        mplew.writeInt(3);
                        mplew.writeInt(2);
                        mplew.writeInt(2640);
                        mplew.writeInt(104);
                        mplew.writeInt(x);
                        mplew.writeInt(1);
                        mplew.writeInt(2675);
                        mplew.writeInt(100);
                        mplew.writeInt(y);
                        mplew.writeInt(1);
                        mplew.writeInt(2689);
                        mplew.writeInt(101);
                        mplew.writeInt(z);
                        c.announce(mplew.getPacket());
                        break;
                    }
                    case 5: {
                        for (int j = 0; j < 15; ++j) {
                            list.add(500);
                        }
                        chr.getMap().broadcastMessage(MobPacket.lucidFieldAttack(c, skill.getSourceId(), skill.getLevel(), Randomizer.nextInt(2), list, monster.getPosition()));
                        c.getPlayer().getMap().showWeatherEffectNotice("露希妲使用強力攻擊!", 222, 2000);
                        break;
                    }
                    case 6: {
                        mplew.writeInt(Randomizer.rand(1,4));
                        c.announce(mplew.getPacket());
                        break;
                    }
                    case 8:{
                        monster.setCustomInfo(23807, 0, 10000);
                        c.getPlayer().getMap().showWeatherEffectNotice("露希妲使用強力攻擊!", 222, 2000);
                        c.getPlayer().getMap().broadcastMessage(Lucid.doRushSkill());
                        break;
                    }
                    case 7: {
                        boolean isLeft = Randomizer.isSuccess(50);
                        if (monster.getMap().getId() == 450004150 || monster.getMap().getId() == 450004450 || monster.getMap().getId() == 450003840) {
                            c.getPlayer().getMap().broadcastMessage(Lucid.createDragon(1, 0, 0, 0, 0, isLeft));
                        } else {
                            int createPosX = isLeft ? -138 : 1498;
                            int createPosY = Randomizer.nextBoolean() ? -1312 : 238;
                            int posX = createPosX;
                            int posY = (monster.getPosition()).y;
                            c.getPlayer().getMap().broadcastMessage(Lucid.createDragon(2, posX, posY, createPosX, createPosY, isLeft));
                        }
                        monster.setCustomInfo(23807, 0, 20000);
                        c.getPlayer().getMap().showWeatherEffectNotice("露希妲召喚了強力的召喚獸!", 222, 2000);
                        c.getPlayer().dropMessage(5, "露希妲召喚了強力的召喚獸!");
                        break;
                    }
                    case 9: {
                        for (MapleMonster mob : c.getPlayer().getMap().getAllMonster()) {
                            if (c.getPlayer().getMap().getMonsters().getFirst().getId() != 8880150 && c.getPlayer().getMap().getMonsters().getFirst().getId() != 8880151 && c.getPlayer().getMap().getMonsters().getFirst().getId() != 8880155) {
                                c.getPlayer().getMap().killMonsterType(mob, 0);
                            }
                        }
                        c.getPlayer().getMap().getMonsters().getFirst().removeCustomInfo(8880140);
                        c.getPlayer().getMap().broadcastMessage(Lucid.RemoveButterfly());
                        c.getPlayer().getMap().broadcastMessage(Lucid.setStainedGlassOnOff(false, FieldLucid.STAINED_GLASS));
                        c.getPlayer().getMap().broadcastMessage(Lucid.setButterflyAction(Butterfly.Mode.MOVE, new int[] { 600, -500 }));
                        c.getPlayer().getMap().broadcastMessage(Lucid.changeStatueState(true, 0, false));
                        c.getPlayer().getMap().broadcastMessage(Lucid.setFlyingMode(true));
                        c.getPlayer().getMap().broadcastMessage(Lucid.doBidirectionShoot(50, 20, 100000, 8));
                        c.getPlayer().getMap().broadcastMessage(Lucid.doSpiralShoot(4, 390, 225, 13, 3, 3500, 10, 10, 1));
                        c.getPlayer().getMap().broadcastMessage(Lucid.doSpiralShoot(5, 10, 15, 20, 40, 4000, 13, 0, 0));
                        c.getPlayer().getMap().broadcastMessage(Lucid.doWelcomeBarrageSkill(2));
                        c.getPlayer().getMap().getMonsters().getFirst().setCustomInfo(23888, 1, 0);
                        c.getPlayer().getMap().getMonsters().getFirst().setCustomInfo(23807, 0, 15700);
                        Timer.MobTimer.getInstance().schedule(() -> {
                            monster.setCustomInfo(23888, 0, 0);
                            c.getPlayer().getMap().broadcastMessage(Lucid.setStainedGlassOnOff(true, FieldLucid.STAINED_GLASS));
                            c.getPlayer().getMap().broadcastMessage(Lucid.setFlyingMode(false));
                            c.getPlayer().getMap().broadcastMessage(Lucid.changeStatueState(true, 0, true));
                        }, 15700L);
                        break;
                    }
                }
                break;
            }
                    case 10:
                    case 13:
                    case 14:
                    case 15:
                    case 16:
                    case 17:
                    case 18:
                    case 19:
                    case 20: {
                        return;
                    }
            case 241: {
                chr.getMap().broadcastMessage(EffectPacket.PapulatusFieldEffect(2, Randomizer.rand(1, 5)));
                break;
            }
        }

    }
}
