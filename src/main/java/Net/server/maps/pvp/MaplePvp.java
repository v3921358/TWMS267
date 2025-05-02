/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Net.server.maps.pvp;

import Client.MapleCharacter;
import Client.SecondaryStat;
import Client.skills.Skill;
import Client.skills.SkillFactory;
import Config.constants.enums.UserChatMessageType;
import Config.constants.skills.夜光;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleLifeFactory;
import Net.server.life.MapleMonster;
import Net.server.maps.MapleMap;
import Packet.MaplePacketCreator;
import Server.channel.handler.AttackInfo;
import Server.world.WorldBroadcastService;
import Server.world.WorldGuildService;
import tools.Randomizer;

import java.awt.*;

/**
 * @author PlayDK
 */
public class MaplePvp {

    /*
     * 解析Pvp角色的傷害
     */
    private static PvpAttackInfo parsePvpAttack(AttackInfo attack, MapleCharacter player, MapleStatEffect effect) {
        PvpAttackInfo ret = new PvpAttackInfo();
        double maxdamage = player.getLevel() + 100.0;
        int skillId = attack.skillId;
        ret.skillId = skillId;
        ret.critRate = 5; //爆擊概率
        ret.ignoreDef = 0; //無視防禦
        ret.skillDamage = 100; //技能攻擊
        ret.mobCount = 1; //攻擊角色的數量
        ret.attackCount = 1; //攻擊角色的次數
        int pvpRange = attack.isCloseRangeAttack ? 35 : 70; //攻擊的距離
        ret.facingLeft = attack.direction < 0;
        if (skillId != 0 && effect != null) {
            ret.critRate += effect.getCritical();
            ret.ignoreDef += effect.getIgnoreMobpdpR();
            ret.skillDamage = (effect.getDamage() + player.getStat().getSkillDamageIncrease(skillId));
            ret.mobCount = Math.max(1, effect.getMobCount(player));
            ret.attackCount = Math.max(effect.getBulletCount(player), effect.getAttackCount(player));
            ret.box = effect.calculateBoundingBox(player.getPosition(), ret.facingLeft, pvpRange);
        } else {
            ret.box = calculateBoundingBox(player.getPosition(), ret.facingLeft, pvpRange);
        }
        boolean mirror = player.getBuffedValue(SecondaryStat.ShadowPartner) != null || player.getBuffedIntValue(SecondaryStat.PoseType) == 1;
        ret.attackCount *= (mirror ? 2 : 1);
        maxdamage *= ret.skillDamage / 100.0;
        ret.maxDamage = maxdamage * ret.attackCount;
        if (player.isDebug()) {
            player.dropSpouseMessage(UserChatMessageType.管理員對話, "Pvp傷害解析 - 最大攻擊: " + maxdamage + " 數量: " + ret.mobCount + " 次數: " + ret.attackCount + " 爆擊: " + ret.critRate + " 無視: " + ret.ignoreDef + " 技能傷害: " + ret.skillDamage);
        }
        return ret;
    }

    private static Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft, int range) {
        Point lt = new Point(-70, -30);
        Point rb = new Point(-10, 0);
        Point mylt;
        Point myrb;
        if (facingLeft) {
            mylt = new Point(lt.x + posFrom.x - range, lt.y + posFrom.y);
            myrb = new Point(rb.x + posFrom.x, rb.y + posFrom.y);
        } else {
            myrb = new Point(lt.x * -1 + posFrom.x + range, rb.y + posFrom.y);
            mylt = new Point(rb.x * -1 + posFrom.x, lt.y + posFrom.y);
        }
        return new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
    }

    public static boolean inArea(MapleCharacter chr) {
        for (Rectangle rect : chr.getMap().getAreas()) {
            if (rect.contains(chr.getPosition())) {
                return true;
            }
        }
        return false;
    }

    private static void monsterBomb(MapleCharacter player, MapleCharacter attacked, MapleMap map, PvpAttackInfo attack) {
        if (player == null || attacked == null || map == null) {
            return;
        }
        double maxDamage = attack.maxDamage;
        boolean isCritDamage = false;
        //等級壓制處理
        if (player.getLevel() > attacked.getLevel() + 10) {
            maxDamage *= 1.05;
        } else if (player.getLevel() < attacked.getLevel() - 10) {
            maxDamage /= 1.05;
        } else if (player.getLevel() > attacked.getLevel() + 20) {
            maxDamage *= 1.10;
        } else if (player.getLevel() < attacked.getLevel() - 20) {
            maxDamage /= 1.10;
        } else if (player.getLevel() > attacked.getLevel() + 30) {
            maxDamage *= 1.15;
        } else if (player.getLevel() < attacked.getLevel() - 30) {
            maxDamage /= 1.15;
        }
        //爆擊概率處理
        if (Randomizer.nextInt(100) < attack.critRate) {
            maxDamage *= 1.50;
            isCritDamage = true;
        }
        int attackedDamage = (int) (Math.floor(Math.random() * ((int) maxDamage * 0.35) + (int) maxDamage * 0.65));
        int MAX_PVP_DAMAGE = 9999; //最大Pvp傷害
        int MIN_PVP_DAMAGE = 100; //最小Pvp傷害
        if (attackedDamage > MAX_PVP_DAMAGE) {
            attackedDamage = MAX_PVP_DAMAGE;
        }
        if (attackedDamage < MIN_PVP_DAMAGE) {
            attackedDamage = MIN_PVP_DAMAGE;
        }
        int hploss = attackedDamage, mploss = 0;
        if (attackedDamage > 0) {
            if (attacked.getBuffedValue(SecondaryStat.MagicGuard) != null) {
                mploss = (int) (attackedDamage * (attacked.getBuffedValue(SecondaryStat.MagicGuard).doubleValue() / 100.0));
                hploss -= mploss;
                if (attacked.getBuffedValue(SecondaryStat.Infinity) != null) {
                    mploss = 0;
                } else if (mploss > attacked.getStat().getMp()) {
                    mploss = attacked.getStat().getMp();
                    hploss -= mploss;
                }
                attacked.addHPMP(-hploss, -mploss, false, false);
            } else if (attacked.getTotalSkillLevel(夜光.魔法防禦) > 0) {
                Skill skill = SkillFactory.getSkill(夜光.魔法防禦);
                MapleStatEffect effect = skill.getEffect(attacked.getTotalSkillLevel(夜光.魔法防禦));
                mploss = (int) (attackedDamage * (effect.getX() / 100.0));
                hploss -= mploss;
                if (mploss > attacked.getStat().getMp()) {
                    mploss = attacked.getStat().getMp();
                    hploss -= mploss;
                }
                attacked.addHPMP(-hploss, -mploss, false, false);
            } else if (attacked.getStat().mesoGuardMeso > 0) {
                hploss = (int) Math.ceil(attackedDamage * attacked.getStat().mesoGuard / 100.0);
                int mesoloss = (int) (attackedDamage * (attacked.getStat().mesoGuardMeso / 100.0));
                if (attacked.getMeso() < mesoloss) {
                    attacked.gainMeso(-attacked.getMeso(), false);
                    attacked.dispelEffect(SecondaryStat.BloodyExplosion);
                } else {
                    attacked.gainMeso(-mesoloss, false);
                }
                attacked.addHP(-hploss);
            } else {
                attacked.addHP(-hploss);
            }
        }
        MapleMonster pvpMob = MapleLifeFactory.getMonster(9400711);
        map.spawnMonsterOnGroundBelow(pvpMob, attacked.getPosition());
        map.broadcastMessage(MaplePacketCreator.damagePlayer(attacked.getId(), 2, pvpMob.getId(), hploss));
        if (isCritDamage) {
            player.dropMessage(6, "你對玩家 " + attacked.getName() + " 造成了 " + hploss + " 點爆擊傷害! 對方血量: " + attacked.getStat().getHp() + "/" + attacked.getStat().getCurrentMaxHP());
            attacked.dropMessage(6, "玩家 " + player.getName() + " 對你造成了 " + hploss + " 點爆擊傷害!");
        } else {
            player.dropTopMsg("你對玩家 " + attacked.getName() + " 造成了 " + hploss + " 點傷害! 對方血量: " + attacked.getStat().getHp() + "/" + attacked.getStat().getCurrentMaxHP());
            attacked.dropTopMsg("玩家 " + player.getName() + " 對你造成了 " + hploss + " 點傷害!");
        }
        map.killMonster(pvpMob, player, false, false, (byte) 1, 0);
        //最終獎勵處理
        if (attacked.getStat().getHp() <= 0 && !attacked.isAlive()) {
            int expReward = attacked.getLevel() * 10 * (attacked.getLevel() / player.getLevel());
            int gpReward = (int) (Math.floor(Math.random() * 10 + 10));
            if (player.getPvpKills() * .25 >= player.getPvpDeaths()) {
                expReward *= 2;
            }
            player.gainExp(expReward, true, false, true);
            if (player.getGuildId() > 0 && player.getGuildId() != attacked.getGuildId()) {
                WorldGuildService.getInstance().gainGP(player.getGuildId(), gpReward);
            }
            player.gainPvpKill();
            player.dropMessage(6, "你擊敗了玩家 " + attacked.getName() + "!! ");
            int pvpVictory = attacked.getPvpVictory();
            attacked.gainPvpDeath();
            attacked.dropMessage(6, player.getName() + " 將你擊敗!");
            byte[] packet = MaplePacketCreator.spouseMessage(UserChatMessageType.管理員對話, "[Pvp] 玩家 " + player.getName() + " 終結了 " + attacked.getName() + " 的 " + pvpVictory + " 連斬。");
            if (pvpVictory >= 5 && pvpVictory < 10) {
                map.broadcastMessage(packet);
            } else if (pvpVictory >= 10 && pvpVictory < 20) {
                player.getClient().getChannelServer().broadcastPacket(packet);
            } else if (pvpVictory >= 20) {
                WorldBroadcastService.getInstance().broadcastMessage(packet);
            }
        }
    }

}
