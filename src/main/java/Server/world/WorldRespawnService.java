/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.world;

import Client.MapleCharacter;
import Client.SecondaryStat;
import Client.SecondaryStatValueHolder;
import Client.force.MapleForceFactory;
import Client.inventory.MapleInventoryType;
import Client.inventory.MaplePet;
import Client.inventory.PetDataFactory;
import Client.skills.handler.冒險家.弓手類別.神射手;
import Client.skills.handler.冒險家.法師類別.冰雷大魔導士;
import Client.skills.handler.冒險家.海盜類別.拳霸;
import Client.stat.DeadDebuff;
import Config.configs.ServerConfig;
import Config.constants.JobConstants;
import Config.constants.skills.*;
import Config.constants.skills.冒險家_技能群組.type_法師.主教;
import Config.constants.skills.冒險家_技能群組.type_法師.火毒;
import Config.constants.skills.冒險家_技能群組.影武者;
import Net.server.MapleItemInformationProvider;
import Net.server.Timer.WorldTimer;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import Net.server.maps.*;
import Opcode.Headler.OutHeader;
import Packet.BuffPacket;
import Packet.EffectPacket;
import Packet.ForcePacket;
import Packet.MaplePacketCreator;
import Server.channel.ChannelServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author admin
 */
public class WorldRespawnService {

    private static final Logger log = LoggerFactory.getLogger(WorldRespawnService.class);

    private WorldRespawnService() {
        ////log.info("正在啟動[WorldRespawnService]"); /* 暫時移除告知 */
        Integer[] chs = ChannelServer.getAllInstance().toArray(new Integer[0]);
        for (int i = 1; i <= chs.length; i++) {
            WorldTimer.getInstance().register(new Respawn(i), 1000);
        }
    }

    public static WorldRespawnService getInstance() {
        return SingletonHolder.instance;
    }

    public static void handleMap(MapleMap map, int numTimes, int size, long now) {
        // 檢測地圖上面的道具消失時間
        if (map.getItemsSize() > 0) {
            map.checkMapItemExpire(now);
        }
        // 檢測地圖上面的隨機傳點消失時間
        for (MapleMapObject obj : map.getAllRandomPortalThreadsafe()) {
            MapleRandomPortal portal = (MapleRandomPortal) obj;
            if (portal.getStartTime() + portal.getDuration() <= now) {
                map.disappearMapObject(obj);
            }
        }
        // 如果地圖上面有玩家或者地圖為: 931000500 秘密地圖 - 美洲豹棲息地
        if (size > 0 || map.getId() == 931000500) {
            map.handleMapObject();
            // 該地圖是否能刷新怪物
            if (map.canSpawn(now)) {
                map.respawn(false, now);
            }
            boolean hurt = map.canHurt(now);
            for (MapleCharacter chr : map.getCharacters()) {
                handleCharacter(chr, numTimes, hurt, now);
            }

            // 對地圖裡面的怪物進行檢測
            if (map.getMobsSize() > 0) {
                for (MapleMonster mons : map.getMonsters()) {
                    if (mons.isAlive() && mons.shouldKill(now)) {
                        map.killMonster(mons);
                    }
                    if (mons.isAlive()) {
                        //檢測怪物buff，取消過期的buff
                        mons.checkEffectExpiration();

                        //刷新怪物仇恨對像
                        map.updateMonsterController(mons);
                    }
                }
            }
            if (map.getRunesSize() > 0) {
                MapleRuneStone oldCurseRune = map.getCurseRune();
                int oldCurseStage = oldCurseRune == null ? 0 : oldCurseRune.getCurseStage();
                for (MapleRuneStone rune : map.getAllRune()) {
                    long runeTime = now - rune.getSpawnTime();
                    if (runeTime >= 5 * 60 * 1000) {
                        int curseStage = (int) (runeTime / (5 * 60 * 1000));
                        curseStage = Math.min(4, Math.max(1, curseStage));
                        if (curseStage != rune.getCurseStage()) {
                            rune.setCurseStage(curseStage);
                        }
                    }
                }
                MapleRuneStone curseRune = map.getCurseRune();
                if (curseRune != null) {
                    long runeTime = now - curseRune.getSpawnTime();
                    if (runeTime >= 4 * 60 * 1000) {
                        if (runeTime >= 5 * 60 * 1000) {
                            if (numTimes % 10 == 0) {
                                if (curseRune.getCurseStage() != oldCurseStage) {
                                    map.showRuneCurseStage();
                                }
                            }
                        } else if (runeTime >= (4 * 60 * 1000 + 50 * 1000)) {
                            int curseTime = 10 - (int) ((runeTime - (4 * 60 * 1000 + 50 * 1000)) / 1000);
                            map.broadcastRuneCurseMessage(MaplePacketCreator.sendRuneCurseMsg("需要解放輪來解開精英Boss的詛咒！！\\n" + curseTime + "秒後地圖上開始精英Boss的詛咒！！"));
                        } else if (runeTime <= (4 * 60 * 1000 + 2000)) {
                            map.broadcastRuneCurseMessage(MaplePacketCreator.sendRuneCurseMsg("需要解放輪來解開精英Boss的詛咒！！\\n稍後就會開始菁英Boss的詛咒！！"));
                        }
                    }
                }
            }

            // 燃燒場地更新
            if (numTimes % 60 == 0) {
                map.updateBreakTimeField();
            }

            // 防搶圖剩餘時間檢測
            if (map.getOwner() != -1 && numTimes % 60 == 0) {
                long ownerTime = System.currentTimeMillis() - map.getOwnerStartTime();
                if (ownerTime >= 30 * 60 * 1000) {
                    map.setOwner(-1);
                } else if (ownerTime >= 25 * 60 * 1000) {
                    int ownerSurplusTime = 5 - (int) ((ownerTime - (25 * 60 * 1000)) / (60 * 1000));
                    map.broadcastMessage(EffectPacket.showCombustionMessage("#fn哥德 ExtraBold##fs26#          " + ownerSurplusTime + "分鐘後解除防搶圖！！   ", 4000, -100));
                }
            }
        } else {
            if (map.getBreakTimeFieldStep() > ServerConfig.MAX_BREAKTIMEFIELD_STEP || (!map.isBreakTimeField() && map.getBreakTimeFieldStep() > 0)) {
                map.setBreakTimeFieldStep(!map.isBreakTimeField() ? 0 : ServerConfig.MAX_BREAKTIMEFIELD_STEP);
            }
        }

        // 廣播菁英BOSS位置
        if (numTimes % 30 == 0 && map.getAreaBroadcastMobId() > 0) {
            map.broadcastAreaMob(2);
        }
    }

    public static void handleCharacter(MapleCharacter chr, int numTimes, boolean hurt, long now) {
        if (chr == null) {
            return;
        }
        if (numTimes % 5 == 0) {
            DeadDebuff.getDebuff(chr, 1);
        }
        MapleStatEffect effect;
        SecondaryStatValueHolder mbsvh;
        if (numTimes % 3 == 0 && JobConstants.is冒險家法師(chr.getJob()) && (mbsvh = chr.getBuffStatValueHolder(SecondaryStat.Infinity)) != null) {
            mbsvh.value++;
            chr.send(BuffPacket.giveBuff(chr, mbsvh.effect, Collections.singletonMap(SecondaryStat.Infinity, mbsvh.effect.getSourceId())));
            chr.addHPMP(mbsvh.effect.getY(), mbsvh.effect.getY());
        }
        if (JobConstants.is火毒(chr.getJob())) {
            if (numTimes % 4 == 0) {
                if ((effect = chr.getSkillEffect(火毒.元素吸收)) != null) {
                    effect.unprimaryPassiveApplyTo(chr);
                }
            }
        } else if (JobConstants.is主教(chr.getJob())) {
            if (numTimes % 5 == 0) {
                if ((effect = chr.getSkillEffect(主教.祝福福音)) != null) {
                    effect.unprimaryPassiveApplyTo(chr);
                }
            }
            chr.checkTownPortalLeave();
        } else if (JobConstants.is神射手(chr.getJob())) {
            神射手.sendSnipeStatSet(chr);
        } else if (JobConstants.is拳霸(chr.getJob())) {
            拳霸.sendViperMark(chr);
        } else if (JobConstants.is夜光(chr.getJob()) && chr.isAlive()) {
            if ((effect = chr.getSkillEffect(夜光.光暗轉換)) != null && chr.getStat().getLifeTidal() != chr.getBuffedIntValue(SecondaryStat.LifeTidal)) {
                effect.unprimaryPassiveApplyTo(chr);
            }
        } else if (JobConstants.is惡魔殺手(chr.getJob()) && chr.isAlive()) {
            if (numTimes % 4 == 0 && chr.getEffectForBuffStat(SecondaryStat.InfinityForce) != null && chr.getSkillEffect(惡魔殺手.高貴血統) != null && chr.isSkillCooling(惡魔殺手.高貴血統)) {
                chr.reduceSkillCooldown(惡魔殺手.高貴血統, 2000);
            }
        } else if (JobConstants.is狂豹獵人(chr.getJob())) {
            if (chr.getEffectForBuffStat(SecondaryStat.JaguarSummoned) != null && chr.getCheatTracker().canNextPantherAttackS()) {
                chr.getClient().announce(MaplePacketCreator.openPantherAttack(false));
            }
            if (chr.getSkillEffect(狂豹獵人.美洲豹連接) != null) {
                final String keyValue;
                if ((keyValue = chr.getKeyValue("JaguarCount")) != null) {
                    final int min = Math.min(6, Integer.valueOf(keyValue));
                    if (min != chr.getBuffedIntValue(SecondaryStat.JaguarCount)) {
                        chr.getSkillEffect(狂豹獵人.美洲豹連接).unprimaryPassiveApplyTo(chr);
                    }
                }
            }
        } else if (JobConstants.is爆拳槍神(chr.getJob())) {
            if ((effect = chr.getEffectForBuffStat(SecondaryStat.RWBarrier)) != null) {
                effect.unprimaryPassiveApplyTo(chr);
            }
        } else if (JobConstants.is凱內西斯(chr.getJob())) {
            if (chr.getEffectForBuffStat(SecondaryStat.KinesisPsychicOver) != null) {
                chr.handlePPCount(2);
            }
        } else if (JobConstants.is伊利恩(chr.getJob()) && chr.getEffectForBuffStat(SecondaryStat.LefGloryWing) != null) {
            final List<MapleMapObject> mapObjectsInRange = chr.getMap().getMapObjectsInRange(chr.getPosition(), 742, Collections.singletonList(MapleMapObjectType.MONSTER));
            final List<Integer> moboids = mapObjectsInRange.stream().map(MapleMapObject::getObjectId).collect(Collectors.toList());
            if ((effect = chr.getSkillEffect(伊利恩.榮耀之翼_強化暗器_2)) != null) {
                chr.getMap().broadcastMessage(chr, ForcePacket.forceAtomCreate(MapleForceFactory.getInstance().getMapleForce(chr, effect, 0, moboids, chr.getPosition())), true);
            }
        } else if (JobConstants.is煉獄巫師(chr.getJob())) {
            if ((effect = chr.getEffectForBuffStat(SecondaryStat.BMageAuraYellow)) != null) {
                effect.applyToParty(chr.getMap(), chr);
            }
        } else if (JobConstants.is神之子(chr.getJob())) {
            if ((effect = chr.getEffectForBuffStat(SecondaryStat.ZeroAuraStr)) != null) {
                effect.applyToParty(chr.getMap(), chr);
            }
        } else if (JobConstants.is卡蒂娜(chr.getJob())) {
            mbsvh = chr.getBuffStatValueHolder(SecondaryStat.WeaponVarietyFinale);
            if (mbsvh != null && mbsvh.effect != null && mbsvh.value < 3 && (effect = chr.getSkillEffect(mbsvh.effect.getSourceId())) != null && System.currentTimeMillis() - mbsvh.startTime >= (effect.getX() * 1000L)) {
                mbsvh.value = Math.min(3, mbsvh.value + 1);
                mbsvh.startTime = System.currentTimeMillis();
                chr.send(BuffPacket.giveBuff(chr, effect, Collections.singletonMap(SecondaryStat.WeaponVarietyFinale, effect.getSourceId())));
            }
        }

        if (numTimes % 4 == 0) {
            冰雷大魔導士.handleIceReiki(chr);
        }
        Client.skills.handler.冒險家.法師類別.主教.handlePassive(chr, numTimes);

        if (chr.getBuffedValue(SecondaryStat.FinalCut) != null && chr.getCheatTracker().canNext絕殺刃()) {
            chr.getCheatTracker().setNext絕殺刃(0L);
            if ((effect = chr.getSkillEffect(影武者.絕殺刃)) != null) {
                effect.applyBuffEffect(chr, chr, effect.getBuffDuration(chr), false, false, true, chr.getPosition());
            }
        }
        if (chr.getBuffedValue(SecondaryStat.GuidedBullet) != null && chr.getMap().getMobObject(chr.getLinkMobObjectID()) == null) {
            chr.setLinkMobObjectID(0);
            chr.dispelEffect(SecondaryStat.GuidedBullet);
        }
        if (chr.getBuffedValue(SecondaryStat.Curse) != null && chr.getMap().getMobObject(chr.getLinkMobObjectID()) == null) {
            chr.setLinkMobObjectID(0);
            chr.dispelEffect(SecondaryStat.Curse);
        }
        if (chr.getBuffedValue(SecondaryStat.Shadower_Assassination) != null && chr.getMap().getMobObject(chr.getBuffedIntZ(SecondaryStat.Shadower_Assassination)) == null) {
            chr.dispelEffect(SecondaryStat.Shadower_Assassination);
        }
        if (chr.getStat().mpcon_eachSecond != 0 && chr.getStat().getMp() <= 0) {
            if (chr.getBuffedValue(SecondaryStat.BMageAuraYellow) != null) {
                chr.dispelEffect(SecondaryStat.BMageAuraYellow);
            } else if (chr.getBuffedValue(SecondaryStat.IceAura) != null) {
                chr.dispelEffect(SecondaryStat.IceAura);
            } else if (chr.getBuffedValue(SecondaryStat.FireAura) != null) {
                chr.dispelEffect(SecondaryStat.FireAura);
            }
        }
        if (hurt && chr.isAlive() && chr.getInventory(MapleInventoryType.EQUIPPED).findById(chr.getMap().getProtectItem()) == null) {
            Integer value = chr.getBuffedValue(SecondaryStat.Thaw);
            final int n3 = value == null ? 0 : value;
            if (chr.getMap().getDecHP() > 0) {
                chr.addHPMP(-Math.max(0, chr.getMap().getDecHP() - n3), 0, false, false);
            }
            if (chr.getMap().getDecHPr() > 0) {
                chr.addHPMP(-chr.getMap().getDecHPr(), 0);
            }
        }
        // 如果角色沒有死亡
        if (chr.isAlive()) {

//            if (chr.canHPRecover(now)) {
//                chr.addHP((int) chr.getStat().getHealHP());
//            }
//            if (chr.canMPRecover(now)) {
//                chr.addMP((int) chr.getStat().getHealMP());
//                if (chr.getJob() == 3111 || chr.getJob() == 3112) {
//                    chr.addDemonMp((int) chr.getStat().getHealMP());
//                }
//            }
            chr.doHealPerTime();
            if (chr.getLevel() >= 200) {
                chr.check5thJobQuest();
            }
            if (chr.canFairy(now)) {
                chr.doFairy();
            }
            if (chr.canExpiration(now)) {
                chr.expirationTask(false);
            }
            if (numTimes % 5 == 0) {
                chr.checkFairy();
                if (chr.getBuffStatValueHolder(80011248) != null) {
                    chr.addHPMP(-5, 0);
                }
                MapleStatEffect eff = chr.getSkillEffect(菈菈.龍脈的迴響_效果強化);
                if (eff != null && chr.getBuffStatValueHolder(菈菈.龍脈的迴響) != null) {
                    chr.addHPMP(eff.getW(), 0);
                }
            }
            if (numTimes % 2 == 0) {
                if (chr.getBuffStatValueHolder(80001756) != null) {
                    List<MapleMapObject> objs = chr.getMap().getMonstersInRange(chr.getPosition(), 196);
                    if (objs != null && objs.size() > 0) {
                        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                        mplew.writeShort(OutHeader.LP_UserRandAreaAttackRequest.getValue());
                        mplew.writeInt(80001762); // 打雷之輪雷擊
                        mplew.writeInt(1);
                        mplew.writePosInt(objs.get(0).getPosition());
                        mplew.writeInt(objs.get(0).getObjectId());
                        mplew.writeInt(500); // v264 +
                        chr.send(mplew.getPacket());
                    }
                }
            }
        }
        if (numTimes % 2 == 0 && chr.getInnerStormValue() > 0) {
            chr.checkInnerStormValue();
        }

        if (chr.getBuffedValue(SecondaryStat.ChangeFoxMan) == null) {
            if (chr.getBuffStatValueHolder(SecondaryStat.BlessEnsenble, 陰陽師.幽玄氣息) != null) {
                chr.dispelBuff(陰陽師.幽玄氣息);
            }
            if (chr.getBuffStatValueHolder(SecondaryStat.BlessEnsenble, 陰陽師.幽玄氣息2) != null) {
                chr.dispelBuff(陰陽師.幽玄氣息2);
            }
        }

        mbsvh = chr.getBuffStatValueHolder(SecondaryStat.FoxBless);
        if (mbsvh != null) {
            boolean dispel;
            if (chr.getId() == mbsvh.fromChrID) {
                dispel = chr.getBuffedValue(SecondaryStat.ChangeFoxMan) == null;
            } else {
                MapleCharacter fromChr;
                dispel = chr.getParty() == null || chr.getParty().getPartyMemberByID(mbsvh.fromChrID) == null || (fromChr = chr.getMap().getPlayerObject(mbsvh.fromChrID)) == null || fromChr.getBuffedValue(SecondaryStat.ChangeFoxMan) == null;
            }

            if (dispel) {
                chr.dispelEffect(SecondaryStat.FoxBless);
            }
        }

        /*
         * 對角色坐騎進行檢測
         */
        if (numTimes % 7 == 0 && chr.getMount() != null && chr.getMount().canTire(now)) {
            chr.getMount().increaseFatigue();
        }
        if (ServerConfig.JMS_SOULWEAPON_SYSTEM && numTimes % 10 == 0 && chr.checkSoulWeapon()) {
            if (now - 600000 >= chr.getLastFullSoulMP() && chr.getSoulMP() > 0) {
                chr.addSoulMP(-Randomizer.rand(10, 11));
            }
        }
        if (numTimes % 60 == 0) { //we're parsing through the characters anyway (:
            for (MaplePet pet : chr.getSummonedPets()) {
                if (MapleItemInformationProvider.getInstance().getLimitedLife(pet.getPetItemId()) > 0) {
                    pet.setSecondsLeft(Math.max(pet.getSecondsLeft() - 60, 0));
                    if (pet.getSecondsLeft() == 0) {
                        chr.unequipSpawnPet(pet, true, (byte) 2);
                        return;
                    }
                }
                int newFullness = pet.getFullness() - PetDataFactory.getHunger(pet.getPetItemId());
                if (newFullness <= 5) {
                    pet.setFullness(15);
                    chr.unequipSpawnPet(pet, true, (byte) 1);
                } else {
                    pet.setFullness(newFullness);
                    chr.petUpdateStats(pet, true);
                }
            }
        }
    }

    private static class Respawn implements Runnable {

        private final ChannelServer cserv;
        private int numTimes = 0;

        public Respawn(int ch) {
            //String sb = "[ Mob_Spawn ] Working now for channel " + ch;
            cserv = ChannelServer.getInstance(ch);
            //log.info(sb);
        }

        @Override
        public void run() {
            numTimes++;
            long now = System.currentTimeMillis();

            if (numTimes % 60 == 0) World.updateHoliday();

            if (!cserv.hasFinishedShutdown()) {
                for (MapleMap map : cserv.getMapFactory().getAllLoadedMaps()) { //iterating through each map o_x
                    try {
                        handleMap(map, numTimes, map.getCharactersSize(), now);
                    } catch (Exception e) {
                        log.error("[WorldRespawnService] Exception occurred in method handleMap.", e);
                    }
                }
            }
        }
    }

    private static class SingletonHolder {

        protected static final WorldRespawnService instance = new WorldRespawnService();
    }
}
