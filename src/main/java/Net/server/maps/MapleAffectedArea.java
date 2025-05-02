package Net.server.maps;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.SecondaryStat;
import Client.SecondaryStatValueHolder;
import Client.skills.Skill;
import Client.skills.SkillFactory;
import Config.constants.skills.*;
import Config.constants.skills.冒險家_技能群組.type_法師.主教;
import Config.constants.skills.冒險家_技能群組.type_法師.冰雷;
import Config.constants.skills.冒險家_技能群組.type_法師.火毒;
import Config.constants.skills.冒險家_技能群組.夜使者;
import Config.constants.skills.冒險家_技能群組.暗影神偷;
import Config.constants.skills.皇家騎士團_技能群組.暗夜行者;
import Config.constants.skills.皇家騎士團_技能群組.烈焰巫師;
import Net.server.buffs.MapleStatEffect;
import Net.server.life.MapleMonster;
import Net.server.life.MobSkill;
import Packet.MaplePacketCreator;

import java.awt.*;
import java.util.EnumMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 表示楓之谷地圖中所有具體煙幕效果的對象.例如俠盜職業的四轉技能煙幕彈
 *
 * @author dongjak
 */
public class MapleAffectedArea extends MapleMapObject {

    private final Rectangle area;
    private final boolean isMobMist;
    private final int skilllevel, ownerId;
    private MapleStatEffect effect;
    private MobSkill skill;
    private boolean poisonMist, facingLeft, needHandle;
    private int skillDelay, areaType, healCount/*神聖源泉恢復的總次數*/, healHPr, subtype, force, forcex, duration, skillID;
    private ScheduledFuture<?> schedule = null, poisonSchedule = null;
    private Point ownerPosition;
    private MapleCharacter owner;
    private final long startTime = System.currentTimeMillis();
    private long cancelTime = 0;
    public boolean BUnk1 = false, BUnk2 = false;

    public MapleAffectedArea(Rectangle area, MapleMonster mob, MobSkill skill, Point position) {
        this.area = area;
        this.ownerId = mob.getObjectId();
        this.skill = skill;
        this.skillID = skill.getSourceId();
        this.skilllevel = skill.getLevel();
        this.isMobMist = true;
        this.poisonMist = true;
        this.areaType = 0;
        this.skillDelay = 0;
        this.force = skill.getForce();
        this.forcex = skill.getForcex();
        this.setPosition(position);
        this.owner = null;
        this.duration = skill.getDuration();
        this.facingLeft = !mob.isFacingLeft();
        this.needHandle = true;
    }

    /*
     * 角色技能召喚的煙霧
     */
    public MapleAffectedArea(Rectangle area, MapleCharacter owner, MapleStatEffect effect, Point point) {
        this.owner = owner;
        this.area = area;
        this.ownerPosition = owner.getPosition();
        this.ownerId = owner.getId();
        this.areaType = 0;
        this.effect = effect;
        this.skillID = effect.getSourceId();
        this.skillDelay = 0;
        this.isMobMist = false;
        this.poisonMist = false;
        this.healCount = 0;
        this.needHandle = !effect.getStatups().isEmpty() || !effect.getMonsterStatus().isEmpty();
        this.skilllevel = effect.getLevel();
        this.facingLeft = !owner.isFacingLeft();
        this.duration = effect.getSummonDuration(owner);
        this.setPosition(point);
        switch (effect.getSourceId()) {
            case 烈焰巫師.燃燒軍團:
            case 35121010:
            case 陰陽師.結界_櫻:
            case 狂狼勇士.瑪哈的領域_MIST:
            case 菈菈.釋放_日光井_2:
            case 菈菈.釋放_日光井_5:
            case 菈菈.神木:
            case 22161003:
                this.skillDelay = 2;
                break;
            case 菈菈.發現_風之鞦韆:
            case 菈菈.發現_充滿陽光之處:
                this.skillDelay = 2;
                this.needHandle = true;
                break;
            case 冰雷.冰雪結界_1:
                this.skillDelay = 2;
                this.BUnk2 = true;
                break;
            case 狂豹獵人.連弩陷阱:
                this.skillDelay = 5;
                break;
            case 狂豹獵人.鑽孔集裝箱:
                this.skillDelay = 8;
                break;
            case 暗影神偷.煙幕彈:
            case 亞克.迷惑之拘束_1:
            case 龍魔導士.迅捷_回來吧_1:
                this.skillDelay = 3;
                break;
            case 阿戴爾.魔力爆裂:
                this.BUnk1 = true;
                this.skillDelay = 3;
                break;
            case 煉獄巫師.魔法屏障:
                this.areaType = 0;
                this.BUnk2 = true;
                break;
            case 傑諾.時空膠囊:
                this.areaType = 5;
                this.needHandle = true;
                break;
            case 冰雷.寒冰迅移:
                this.skillDelay = 3;
                this.poisonMist = true;
                this.needHandle = false;
                break;
            case 皮卡啾.帕拉美:
                this.skillDelay = 12;
                break;
            case 火毒.致命毒霧:
                this.poisonMist = true;
                break;
            case 幻影俠盜.玫瑰四重曲:
                this.needHandle = false;
                break;
            case 惡魔復仇者.惡魔狂亂_魔族之血:
            case 冰雷.冰河紀元:
                this.skillDelay = 3;
                this.poisonMist = true;
                break;
            case 陰陽師.結界_鈴蘭:
                this.skillDelay = 10;
                break;
            case 陰陽師.靈脈的氣息:
                this.skillDelay = 2;
                this.poisonMist = true;
                break;
            case 凱撒.龍烈焰:
            case 冰雷.落雷凝聚_2:
            case 暗夜行者.影之槍_1:
            case 暗夜行者.影之槍:
                this.duration = 10000;
                this.poisonMist = true;
                break;
            case 冰雷.落雷凝聚_1:
//                this.skillDelay = 26;
                this.duration = 300;
                this.poisonMist = true;
                break;
            case 神之子.進階碎地猛擊:
                this.skillDelay = 16;
                break;
            case 主教.神聖之泉:
                this.healCount = effect.getY();
                this.needHandle = false;
                break;
            case 主教.神聖之水:
                int intValue = owner.getStat().getTotalInt();
                this.duration = effect.getQ();
                if (intValue >= effect.getS2()) {
                    duration += (intValue / effect.getS2()) * effect.getQ2();
                }
                this.duration = effect.calcSummonDuration(duration * 1000, owner);
                this.healHPr = effect.getU2();
                if (intValue >= effect.getDot()) {
                    healHPr += (intValue / effect.getDot()) * effect.getW2();
                }
                this.skillDelay = 10;
                this.BUnk2 = true;
                this.needHandle = false;
                break;
            case 重砲指揮官.ICBM_3:
                this.duration = owner.getSkillEffect(重砲指揮官.ICBM).getU2() * 1000;
                this.poisonMist = true;
                break;
            case 重砲指揮官.精準轟炸_2:
                this.healHPr = effect.getX();
                break;
            case 火毒.劇毒領域:
                this.skillDelay = 14;
                this.duration = effect.getS() * 1000;
                this.poisonMist = true;
                this.BUnk2 = true;
                break;
        }
    }

    public MapleAffectedArea(Rectangle area, MapleCharacter owner) {
        this.area = area;
        this.ownerId = owner.getId();
        this.effect = new MapleStatEffect();
        this.skilllevel = 30;
        this.areaType = 0;
        this.isMobMist = false;
        this.poisonMist = false;
        this.skillDelay = 10;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.AFFECTED_AREA;
    }

    public Skill getSourceSkill() {
        return SkillFactory.getSkill(effect.getSourceId());
    }

    /*
     * 是否為怪物召喚的煙霧
     */
    public boolean isMobMist() {
        return isMobMist;
    }

    /*
     * 是否為中毒效果的煙霧
     */
    public boolean isPoisonMist() {
        return poisonMist;
    }

    public int getHealCount() {
        return healCount;
    }

    public void setHealCount(int count) {
        healCount = count;
    }

    public int getHealHPR() {
        return healHPr;
    }

    public void setHealHPR(int hpr) {
        healHPr = hpr;
    }

    public int getAreaType() {
        return areaType;
    }

    public void setAreaType(int areaType) {
        this.areaType = areaType;
    }

    public int getSkillDelay() {
        return skillDelay;
    }

    public void setSkillDelay(int skillDelay) {
        this.skillDelay = skillDelay;
    }

    public int getSkillLevel() {
        return skilllevel;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public MobSkill getMobSkill() {
        return this.skill;
    }

    public Rectangle getArea() {
        return area;
    }

    public MapleStatEffect getEffect() {
        return effect;
    }

    public Point getOwnerPosition() {
        return ownerPosition;
    }

    public boolean isFacingLeft() {
        return facingLeft;
    }

    public int getSubtype() {
        return subtype;
    }

    public void setSubtype(int subtype) {
        this.subtype = subtype;
    }

    public int getForce() {
        return force;
    }

    public int getForcex() {
        return forcex;
    }

    @Override
    public int getRange() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void sendSpawnData(MapleClient c) {
        c.announce(MaplePacketCreator.spawnMist(this));
    }

    @Override
    public void sendDestroyData(MapleClient c) {
        c.announce(MaplePacketCreator.removeMist(getObjectId(), false));
    }

    public boolean makeChanceResult() {
        return effect.makeChanceResult();
    }

    public MapleCharacter getOwner() {
        return this.owner;
    }

    public boolean shouldCancel(long now) {
        return (cancelTime > 0 && cancelTime <= now);
    }

    /*
     * 獲取怪物取消BUFF的時間
     */
    public long getCancelTask() {
        return this.cancelTime;
    }

    /*
     * 設置取消怪物BUFF的時間
     */
    public void setCancelTask(long cancelTask) {
        this.cancelTime = System.currentTimeMillis() + cancelTask;
    }

    public int getLeftTime() {
        if (this.skillID == 冰雷.落雷凝聚_1) {
            return (int) (-(this.startTime + this.duration - System.currentTimeMillis()));
        }
        return (int) (this.startTime + this.duration - System.currentTimeMillis());
    }

    public void setSchedule(ScheduledFuture<?> schedule) {
        this.schedule = schedule;
    }

    public void setPoisonSchedule(ScheduledFuture<?> poisonSchedule) {
        this.poisonSchedule = poisonSchedule;
    }

    public final void cancel() {
        if (this.poisonSchedule != null) {
            this.poisonSchedule.cancel(true);
            this.poisonSchedule = null;
        }
        if (this.schedule != null) {
            this.schedule.cancel(true);
            this.schedule = null;
        }
    }

    public boolean isNeedHandle() {
        return needHandle;
    }

    public int getDuration() {
        return duration;
    }

    public int getSkillID() {
        return skillID;
    }

    public void handleEffect(MapleCharacter chr, int numTimes) {
        if (getSkillID() == 火毒.劇毒領域) {
            return;
        }
        if (isMobMist()) {
            if (getSkillID() == 131 && chr.getPosition().y > -20) {
                final int x;
                if ((x = getMobSkill().getX()) < 50) {
                    chr.addHPMP(-10, 0);
                } else {
                    chr.addHPMP(-x, 0, false, true);
                }
            }
            return;
        }
        final MapleCharacter fchr;
        if (numTimes > -2 && (fchr = chr.getMap().getPlayerObject(getOwnerId())) != null && (fchr == chr || (fchr.getParty() != null && fchr.getParty() == chr.getParty()))) {
            if (area.getBounds().contains(chr.getPosition())) {
                if (getSkillID() == 傑諾.時空膠囊) {
                    fchr.reduceAllSkillCooldown(4000, true);
                } else if (getSkillID() == 菈菈.發現_風之鞦韆 || getSkillID() == 菈菈.發現_充滿陽光之處) {
                    int skil;
                    if (getSkillID() == 菈菈.發現_充滿陽光之處) {
                        if (numTimes < 0 || numTimes % getEffect().getZ() == 0) {
                            chr.addHPMP(getEffect().getHp(), 0);
                        }
                        skil = 菈菈.發現_充滿陽光之處_1;
                    } else {
                        skil = 菈菈.發現_風之鞦韆_1;
                    }
                    if (chr.getBuffStatValueHolder(skil) == null) {
                        MapleStatEffect effect = fchr.getSkillEffect(skil);
                        if (effect != null) {
                            int duration = effect.calcBuffDuration(fchr == chr ? effect.getDuration() : (getEffect().getX() * 1000), fchr);
                            effect.applyTo(fchr, chr, duration, false, false, true, getPosition());
                        }
                    }
                } else if (chr.getBuffStatValueHolder(getSkillID()) == null) {
                    if (菈菈.神木 != getSkillID() || fchr == chr) {
                        getEffect().applyTo(fchr, chr, getLeftTime(), false, false, true, getPosition());
                    }
                }
                return;
            }
        }

        final SecondaryStatValueHolder mbsvh = chr.getBuffStatValueHolder(getSkillID());
        if (mbsvh != null) {
            EnumMap<SecondaryStat, Integer> status = new EnumMap(mbsvh.effect.getStatups());
            status.remove(SecondaryStat.IndieBuffIcon);
            chr.cancelEffect(mbsvh.effect, false, mbsvh.startTime, status);
        }
    }

    public void handleMonsterEffect(MapleMap map, int numTimes) {
        final MapleStatEffect effect;
        final MapleCharacter chr;
        if (!isMobMist() && (effect = getEffect()) != null && (chr = map.getPlayerObject(getOwnerId())) != null) {
            if (!effect.getMonsterStatus().isEmpty()) {
                for (MapleMonster monster : map.getMonsters()) {
                    if (numTimes > -2 && getArea().contains(monster.getPosition())) {
                        if (monster.getSponge() == null) {
                            effect.applyMonsterEffect(chr, monster, effect.getMobDebuffDuration(chr));
                        }
                    } else {
                        if (skillID == 夜使者.絕對領域) {
                            monster.removeEffect(ownerId, skillID);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Rectangle getBounds() {
        return effect.calculateBoundingBox(getPosition());
    }
}
