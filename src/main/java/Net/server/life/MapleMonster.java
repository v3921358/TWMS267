package Net.server.life;

import Client.*;
import Client.force.MapleForceAtom;
import Client.force.MapleForceFactory;
import Client.inventory.MapleInventory;
import Client.inventory.MaplePet;
import Client.skills.Skill;
import Client.skills.SkillFactory;
import Client.stat.DeadDebuff;
import Client.status.MonsterStatus;
import Config.configs.ServerConfig;
import Config.constants.GameConstants;
import Config.constants.JobConstants;
import Config.constants.skills.冒險家_技能群組.夜使者;
import Config.constants.skills.凱殷;
import Config.constants.skills.皮卡啾;
import Config.constants.skills.精靈遊俠;
import Config.constants.skills.陰陽師;
import Net.server.SkillCustomInfo;
import Net.server.Timer.EtcTimer;
import Net.server.Timer.MapTimer;
import Net.server.buffs.MapleStatEffect;
import Net.server.buffs.MapleStatEffectFactory;
import Net.server.factory.MobCollectionFactory;
import Net.server.fieldskill.FieldSkillFactory;
import Net.server.maps.MapleFoothold;
import Net.server.maps.MapleMap;
import Net.server.maps.MapleMapObject;
import Net.server.maps.MapleMapObjectType;
import Net.server.movement.LifeMovementFragment;
import Net.server.movement.MovementNormal;
import Packet.*;
import Plugin.script.binding.ScriptEvent;
import Server.BossEventHandler.Seren;
import Server.channel.ChannelServer;
import Server.channel.handler.MovementParse;
import SwordieX.client.party.Party;
import SwordieX.client.party.PartyMember;
import connection.packet.FieldPacket;
import SwordieX.field.fieldeffect.FieldEffect;
import SwordieX.world.World;
import tools.DateUtil;
import tools.Pair;
import tools.Randomizer;
import tools.Triple;

import java.awt.*;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class MapleMonster extends AbstractLoadedMapleLife {

    private final Map<MonsterStatus, List<MonsterEffectHolder>> effects = new LinkedHashMap<>();
    private long lastSpecialAttackTime = System.currentTimeMillis();
    private long lastSeedCountedTime = System.currentTimeMillis();
    private long lastStoneTime = System.currentTimeMillis();
    private final long lastSpawnBlindMobtime = System.currentTimeMillis();
    private ScheduledFuture<?> schedule = null;
    private final List<AttackerEntry> attackers = new LinkedList<>();
    private final Map<Pair<Integer, Integer>, Integer> skillsUsed = new HashMap<>();
    private final Map<String, Integer> aggroRank = new HashMap<>();
    private Map<Integer, Long> usedSkills;
    private MapleMonsterStats stats;
    private ForcedMobStat forcedMobStat = null;
    private long hp;
    private long nextKill = 0;
    private long lastDropTime = 0;
    private final long lastUpdateController = 0;
    private long barrier = 0;
    private int mp;
    private final boolean shouldDropItem = false;
    private final boolean killed = false;
    private final boolean isseperated = false;
    private final boolean isMobGroup = false;
    private boolean isSkillForbid = false;
    private boolean useSpecialSkill = false;
    private byte carnivalTeam = -1;
    private byte phase = 0;
    private final byte bigbangCount = 0;
    private boolean demianChangePhase = false;
    private MapleMap map;
    private WeakReference<MapleMonster> sponge = new WeakReference<>(null);
    private int linkoid = 0;
    private int lastNode = -1;
    private final int highestDamageChar = 0;
    private int linkCID = 0; // Just a reference for monster EXP distribution after deadge
    private WeakReference<MapleCharacter> controller = new WeakReference<>(null);
    private boolean fake = false, dropsDisabled = false, controllerHasAggro = false, controllerKnowsAboutAggro, spawnRevivesDisabled = false;
    private ScriptEvent eventInstance;
    private MonsterListener listener = null;
    private final byte[] reflectpack = null;
    private byte[] nodepack = null;
    private ScheduledFuture deadBound = null;
    private int stolen = -1; //monster can only be stolen ONCE
    private boolean shouldDropAssassinsMark = true;
    private long changeTime = 0;
    private int reduceDamageType = -1;
    private int followChrID;
    private int StigmaType;
    private int TotalStigma;
    private int eliteType;
    private long lastMove;
    private int triangulation = 0;
    private int patrolScopeX1;
    private int patrolScopeX2;
    private long lastSmiteTime = 0;

    public boolean isSkillForbid() {
        return this.isSkillForbid;
    }

    public void setSkillForbid(boolean isSkillForbid) {
        this.isSkillForbid = isSkillForbid;
    }

    private long lastLucidSmiteTime = 0;
    private int zoneDataType = 0;
    private final ReentrantLock effectLock = new ReentrantLock();
    private int seperateSoulSrcOID = 0;
    private final List<Integer> reviveMobs = new ArrayList<>();
    private final ReentrantLock hpLock = new ReentrantLock();
    private double reduceDamageR = 0.0;
    private final ReentrantLock controllerLock = new ReentrantLock();
    private short appearType;
    private boolean newSpawn = true;
    private final transient Map<Integer, SkillCustomInfo> customInfo = new LinkedHashMap<Integer, SkillCustomInfo>();
    private boolean steal;
    private boolean soul;
    private long maxHP;
    private int maxMP;
    private final List<Integer> eliteMobActive = new ArrayList<>();
    private double hpLimitPercent;
    private int lastKill;
    private byte animation;
    private boolean spongeMob;
    private int mobCtrlSN;
    private final List<Integer> spawnList = new ArrayList<>();
    private List<Integer> willHplist = new ArrayList<>();
    private long transTime;
    private int scale;
    private int showMobId = 0;
    private int rewardSprinkleCount = 0;
    private int bUnk = 0;
    private int bUnk1 = 0;
    private int owner = -1, eliteGrade = -1;
    private long shield = 0L, shieldmax = 0L;
    private int nextSkill = 0;
    private int nextSkillLvl = 0;
    private final int freezingOverlap = 0;
    private final int curseBound = 0;
    private String specialtxt;
    public long lastObstacleTime = System.currentTimeMillis(), lastBWBliThunder = System.currentTimeMillis(), lastBWThunder = System.currentTimeMillis(), lastLaserTime = System.currentTimeMillis(), lastRedObstacleTime = System.currentTimeMillis(), lastSpearTime = System.currentTimeMillis();
    public long lastDistotionTime = System.currentTimeMillis(), lastCapTime = 0, astObstacleTime = System.currentTimeMillis(), lastChainTime = System.currentTimeMillis(), lastThunderTime = System.currentTimeMillis(), lastEyeTime = System.currentTimeMillis();
    public long DrgonPower = System.currentTimeMillis();
    public long dropAttack = System.currentTimeMillis();
    public long dropAttack_II  = System.currentTimeMillis();
    public long dropAttack_III = System.currentTimeMillis();
    public long dropAttack_IV = System.currentTimeMillis();
    public long fieldSpawn = System.currentTimeMillis();
    public long fieldSpawn_II  = System.currentTimeMillis();
    public long fieldSpawn_III = System.currentTimeMillis();
    public long fieldSpawn_IV = System.currentTimeMillis();
    public long skillAttack = System.currentTimeMillis();
    public long skillAttack_II = System.currentTimeMillis();
    public long skillAttack_III = System.currentTimeMillis();
    public long skillAttack_IV = System.currentTimeMillis();
    public long changePhase = System.currentTimeMillis();
    public long changePhase_II = System.currentTimeMillis();
    public long changePhase_III = System.currentTimeMillis();
    public long changePhase_IV = System.currentTimeMillis();

    public long lastDropAttackTime = System.currentTimeMillis();

    public int getStigmaType() {
        return this.StigmaType;
    }

    public int getTotalStigma() {
        return this.TotalStigma;
    }

    public void setTotalStigma(int TotalStigma) {
        this.TotalStigma = TotalStigma;
    }

    public String getSpecialtxt() {
        return this.specialtxt;
    }

    public void setSpecialtxt(String specialtxt) {
        this.specialtxt = specialtxt;
    }


    private MapleInventory[] inventory; // 處置80%追加
    private transient Pair<Long, List<LifeMovementFragment>> lastres;

    public long getShield() {
        return this.shield;
    }

    public void setShield(long l, boolean b, long shield) {
        this.shield = shield;
    }

    public long getShieldmax() {
        return this.shieldmax;
    }

    public void setShieldmax(long shieldmax) {
        this.shieldmax = shieldmax;
    }

    public int getShieldPercent() {
        return (int) Math.ceil(this.shield * 100.0D / this.shieldmax);
    }

    public MapleMonster(int id, MapleMonsterStats stats) {
        super(id);
        initWithStats(stats);
    }

    public MapleMonster(MapleMonster monster) {
        super(monster);
        initWithStats(monster.stats);
    }

    public long getBarrier() {
        return barrier;
    }

    public void setBarrier(long barrier) {
        this.barrier = barrier;
    }

    public void initWithStats(MapleMonsterStats stats) {
        setStance(5);
        this.stats = stats;
        hp = stats.getHp();
        mp = stats.getMp();
        maxHP = stats.getHp();
        maxMP = stats.getMp();
        scale = 100;

        if (stats.getSkillSize() > 0) {
            usedSkills = new HashMap<Integer, Long>();
        }
    }

    public List<AttackerEntry> getAttackers() {
        return attackers;
    }

    public boolean canNextSmite() {
        if (System.currentTimeMillis() > this.lastSmiteTime) {
            this.lastSmiteTime = System.currentTimeMillis() + 110000L;
            return true;
        }
        return false;
    }

    public boolean canNextLucidSmite() {
        if (System.currentTimeMillis() > this.lastLucidSmiteTime) {
            this.lastLucidSmiteTime = System.currentTimeMillis() + 90000L;
            return true;
        }
        return false;
    }

    public void checkMobZoneState() {
        if (this.getStats().getMobZone().size() > 0 && this.zoneDataType > 0 && this.getHPPercent() < (this.getStats().getMobZone().size() - this.zoneDataType) * 100 / this.getStats().getMobZone().size()) {
            this.setZoneDataType(this.zoneDataType + 1);
        }
    }

    public void checkMobZone(MapleCharacter player) {
        if (this.zoneDataType > 0) {
            final int min = Math.min(this.zoneDataType, this.getStats().getMobZone().size());
            if (min <= 0) {
                return;
            }
            Rectangle rectangle = MapleStatEffectFactory.calculateBoundingBox(getPosition(), isFacingLeft(), getStats().getMobZone().get(min - 1).getLeft(), getStats().getMobZone().get(min - 1).getRight(), 0);
            if (rectangle.contains(player.getPosition()) && this.zoneDataType < 3) {
                if (player.getMobZoneState() <= 0) {
                    player.setMobZoneState(this.zoneDataType);
                    player.getClient().announce(BuffPacket.giveMobZoneState(player, this.getObjectId()));
                }
            } else if (player.getMobZoneState() > 0) {
                final List<SecondaryStat> singletonList = Collections.singletonList(SecondaryStat.MobZoneState);
                player.setMobZoneState(0);
                player.getClient().announce(BuffPacket.temporaryStatReset(singletonList, player));
            }
        }
    }

    public final long getLastSkillUsed(final int skillId) {
        if (usedSkills.containsKey(skillId)) {
            return usedSkills.get(skillId);
        }
        return 0;
    }

    public final void setLastSkillUsed(Integer skillId, final long now, final long cooltime) {
        switch (skillId) {
            case 140:
                usedSkills.put(skillId, now + (cooltime << 1));
                usedSkills.put(141, now);
                break;
            case 141:
                usedSkills.put(skillId, now + (cooltime << 1));
                usedSkills.put(140, now + cooltime);
                break;
            case 143: {
                usedSkills.put(skillId, now + (cooltime << 1));
                usedSkills.put(144, now + cooltime);
                usedSkills.put(145, now + cooltime);
                break;
            }
            case 144: {
                usedSkills.put(skillId, now + (cooltime << 1));
                usedSkills.put(143, now + cooltime);
                usedSkills.put(145, now + cooltime);
                break;
            }
            case 145: {
                usedSkills.put(skillId, now + (cooltime << 1));
                usedSkills.put(143, now + cooltime);
                usedSkills.put(144, now + cooltime);
                break;
            }
            case 228: {
                usedSkills.put(skillId, now + 30000L);
                break;
            }
            default:
                usedSkills.put(skillId, now + cooltime);
                break;
        }
    }

    public int getZoneDataType() {
        return zoneDataType;
    }

    public void setZoneDataType(int zoneDataType) {
        this.zoneDataType = zoneDataType;
        if (this.map != null) {
            map.broadcastMessage(MobPacket.changeMonsterZone(getObjectId(), zoneDataType), this.getPosition());
        }
    }

    public void healHPMP(long hpHeal, int mpHeal) {
        this.hpLock.lock();

        try {
            if (this.hp + hpHeal > this.maxHP) {
                hpHeal = this.maxHP - this.hp;
            }

            int arg9999 = this.mp + mpHeal;
            if (arg9999 > this.maxMP) {
                mpHeal = this.maxMP - this.mp;
            }

            this.hp = Math.min(this.hp + hpHeal, this.maxHP);
            int arg10000 = this.mp + mpHeal;
            this.mp = Math.min(arg10000, this.maxMP);
            this.map.broadcastMessage(MobPacket.MobDamaged(this, 0, hpHeal, false), this.getPosition());
        } finally {
            this.hpLock.unlock();
        }
        MapleMonster sponge = this.sponge.get();
        if (sponge != null) {
            sponge.healHPMP(hpHeal, mpHeal);
        }
    }


    public Map<MonsterStatus, List<MonsterEffectHolder>> getEffects() {
        effectLock.lock();
        try {
            return effects;
        } finally {
            effectLock.unlock();
        }
    }

    public Map<MonsterStatus, List<MonsterEffectHolder>> getAllEffects() {
        effectLock.lock();
        try {
            return new LinkedHashMap<>(effects);
        } finally {
            effectLock.unlock();
        }
    }

    public List<MonsterEffectHolder> getIndieEffectHolder(MonsterStatus stat) {
        return getIndieEffectHolder(-1, stat);
    }

    public List<MonsterEffectHolder> getIndieEffectHolder(final int chrID, MonsterStatus stat) {
        effectLock.lock();
        try {
            List<MonsterEffectHolder> list = new ArrayList<>();
            if (effects.containsKey(stat)) {
                for (MonsterEffectHolder meh : effects.get(stat)) {
                    if (chrID < 0 || meh.fromChrID == chrID) {
                        list.add(meh);
                    }
                }
            }
            return list;
        } finally {
            effectLock.unlock();
        }
    }

    public MonsterEffectHolder getEffectHolder(MonsterStatus stat) {
        return getEffectHolder(0, stat);
    }

    public MonsterEffectHolder getEffectHolder(final int chrID, MonsterStatus stat) {
        return getEffectHolder(chrID, stat, -1);
    }

    public MonsterEffectHolder getEffectHolder(int skillID) {
        return getEffectHolder(0, skillID);
    }

    public MonsterEffectHolder getEffectHolder(final int chrID, int skillID) {
        effectLock.lock();
        try {
            return effects.values().stream().flatMap(Collection::stream).filter(meh -> (meh.fromChrID == chrID || chrID < 0) && meh.sourceID == skillID).findFirst().orElse(null);
        } finally {
            effectLock.unlock();
        }
    }

    public MonsterEffectHolder getEffectHolder(MonsterStatus stat, int skillID) {
        return getEffectHolder(0, stat, skillID);
    }

    public MonsterEffectHolder getEffectHolder(final int chrID, MonsterStatus stat, int skillID) {
        effectLock.lock();
        try {
            return effects.get(stat) == null ? null : effects.get(stat).stream().filter(meh -> (meh.fromChrID == chrID || chrID <= 0) && (meh.sourceID == skillID || skillID == -1)).findFirst().orElse(null);
        } finally {
            effectLock.unlock();
        }
    }

    public Map<MonsterStatus, MonsterEffectHolder> getMonsterHolderMap(Map<MonsterStatus, Integer> status) {
        final EnumMap<MonsterStatus, MonsterEffectHolder> holderMap = new EnumMap<>(MonsterStatus.class);
        for (final Entry<MonsterStatus, Integer> entry : status.entrySet()) {
            MonsterEffectHolder meh = getEffectHolder(entry.getKey(), entry.getValue());
            if (meh == null) {
                if (entry.getKey().isDefault()) {
                    meh = new MonsterEffectHolder(0, 0, 0);
                }
            }
            if (meh != null) {
                holderMap.put(entry.getKey(), meh);
            }
        }
        return holderMap;
    }

    public boolean isBuffed(MonsterStatus status) {
        return getEffectHolder(status) != null;
    }

    public void removeEffect(List<MonsterStatus> statusList) {
        removeEffect(statusList, 0, -1);
    }

    public void removeEffect(int chrID, int skillID) {
        removeEffect(Collections.emptyList(), chrID, skillID);
    }

    public void removeEffect(final List<MonsterStatus> statusList, final int chrID, final int skillID) {
        effectLock.lock();
        try {
            EnumMap<MonsterStatus, MonsterEffectHolder> statups = null;
            for (Entry<MonsterStatus, List<MonsterEffectHolder>> entry : effects.entrySet()) {
                if (!statusList.isEmpty() && !statusList.contains(entry.getKey())) {
                    continue;
                }
                if (statups == null) {
                    statups = new EnumMap<>(MonsterStatus.class);
                }
                final Iterator<MonsterEffectHolder> iterator = entry.getValue().iterator();
                while (iterator.hasNext()) {
                    final MonsterEffectHolder holder = iterator.next();
                    if ((chrID <= 0 || holder.fromChrID == chrID) && (skillID == -1 || holder.sourceID == skillID)) {
                        iterator.remove();
                        statups.put(entry.getKey(), holder);
                    }
                }
            }
            if (statups != null && !statups.isEmpty()) {
                this.cancelStatus(statups, true);
            }
        } finally {
            effectLock.unlock();
        }
    }

    public void registerEffect(final EnumMap<MonsterStatus, MonsterEffectHolder> enumMap) {
        effectLock.lock();
        try {
            EnumMap<MonsterStatus, MonsterEffectHolder> statups = null;
            for (final Entry<MonsterStatus, MonsterEffectHolder> entry : enumMap.entrySet()) {
                if (effects.containsKey(entry.getKey())) {
                    if (statups == null) {
                        statups = new EnumMap<>(MonsterStatus.class);
                    }
                    if (entry.getKey().isIndieStat()) {
                        final Iterator<MonsterEffectHolder> iterator = effects.computeIfAbsent(entry.getKey(), k -> new LinkedList<>()).iterator();
                        while (iterator.hasNext()) {
                            final MonsterEffectHolder holder = iterator.next();
                            if (holder.fromChrID == entry.getValue().fromChrID && holder.sourceID == entry.getValue().sourceID) {
                                iterator.remove();
                                statups.put(entry.getKey(), holder);
                            }
                        }
                    } else {
                        statups.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            if (statups != null && !statups.isEmpty()) {
                cancelStatus(statups, true);
            }

            for (final Entry<MonsterStatus, MonsterEffectHolder> entry : enumMap.entrySet()) {
                effects.computeIfAbsent(entry.getKey(), k -> new LinkedList<>()).add(entry.getValue());
            }
        } finally {
            effectLock.unlock();
        }
    }

    private void cancelStatus(final EnumMap<MonsterStatus, MonsterEffectHolder> statups, final boolean b) {
        for (Entry<MonsterStatus, MonsterEffectHolder> entry : statups.entrySet()) {
            if (!entry.getKey().isIndieStat()) {
                effects.remove(entry.getKey());
            } else {
                effects.get(entry.getKey()).remove(entry.getValue());
                if (effects.get(entry.getKey()).isEmpty()) {
                    effects.remove(entry.getKey());
                }
            }
        }
        if (statups.containsKey(MonsterStatus.SeperateSoulP)) {
            this.seperateSoulSrcOID = 0;
        }
        if (map != null && this.getMap().getCharactersSize() > 0) {
            map.broadcastMessage(MobPacket.cancelMonsterStatus(this, statups), getPosition());
        }
    }

    private void dotDamage(final MapleCharacter chr, final long damage) {
        if (chr != null) {
            damage(chr, 0, damage, true);
        }
    }

    public void checkEffectExpiration() {
        List<MonsterEffectHolder> burnedstartups = null;
        effectLock.lock();
        try {
            EnumMap<MonsterStatus, MonsterEffectHolder> statups = null;
            for (Entry<MonsterStatus, List<MonsterEffectHolder>> entry : effects.entrySet()) {
                final Iterator<MonsterEffectHolder> iterator = entry.getValue().iterator();
                while (iterator.hasNext()) {
                    final MonsterEffectHolder holder = iterator.next();
                    if (holder.getLeftTime() <= 0L) {
                        if (statups == null) {
                            statups = new EnumMap<>(MonsterStatus.class);
                        }
                        statups.put(entry.getKey(), holder);
                        iterator.remove();
                    } else if (entry.getKey() == MonsterStatus.Burned) {
                        if (burnedstartups == null) {
                            burnedstartups = new LinkedList<>();
                        }
                        if (holder.canNextDot()) {
                            //dotDamage(map.getPlayerObject(holder.fromChrID), holder.dotDamage);
                            burnedstartups.add(holder);
                        }
                    }
                }
            }
            if (statups != null && !statups.isEmpty()) {
                cancelStatus(statups, true);
            }
        } finally {
            effectLock.unlock();
        }
        if (burnedstartups != null && !burnedstartups.isEmpty()) {
            burnedstartups.forEach(holder -> dotDamage(map.getPlayerObject(holder.fromChrID), holder.dotDamage));
        }
    }


    public MapleMonsterStats getStats() {
        return stats;
    }

    public boolean isBoss() {
        return stats.isBoss() || eliteGrade >= 0;
    }

    public int getAnimationTime(String name) {
        return stats.getAnimationTime(name);
    }

    public void disableDrops() {
        this.dropsDisabled = true;
    }

    public boolean dropsDisabled() {
        return dropsDisabled;
    }

    public final void disableSpawnRevives() {
        this.spawnRevivesDisabled = true;
    }

    public final boolean spawnRevivesDisabled() {
        return spawnRevivesDisabled;
    }

    public final Map<String, Integer> getHitParts() {
        return stats.getHitParts();
    }

    public int getMobLevel() {
        if (forcedMobStat != null) {
            return forcedMobStat.getLevel();
        }
        return stats.getLevel();
    }

    public long getHp() {
        return hp;
    }

    public void setHp(long hp) {
        this.hp = hp;
    }

    public ForcedMobStat getForcedMobStat() {
        return forcedMobStat;
    }

    public void setForcedMobStat(ForcedMobStat ostat) {
        this.forcedMobStat = ostat;
    }

    public long getMobMaxHp() {
        return maxHP;
    }

    public long getMaxHP() {
        return maxHP;
    }

    public void setMaxHP(long maxHP) {
        this.maxHP = maxHP;
    }

    public int getMp() {
        return mp;
    }

    public void setMp(int mp) {
        if (mp < 0) {
            mp = 0;
        }
        this.mp = mp;
    }

    public int getMobMaxMp() {
        return maxMP;
    }

    public int getMaxMP() {
        return maxMP;
    }

    public void setMaxMP(int maxMP) {
        this.maxMP = maxMP;
    }

    public long getMobExp() {
        if (forcedMobStat != null) {
            return forcedMobStat.getExp();
        }
        return stats.getExp();
    }

    public void changeLevelmod(int newLevel, int mutiplier) {
        setForcedMobStat(newLevel);
    }

    public MapleMonster getSponge() {
        return sponge.get();
    }

    public void setSponge(MapleMonster mob) {
        sponge = new WeakReference<>(mob);
        if (linkoid <= 0) {
            linkoid = mob.getObjectId();
        }
    }

    public void sendMobZone(final int reduceDamageType) {
        this.getMap().broadcastMessage(MobPacket.showMonsterPhaseChange(this.getObjectId(), reduceDamageType), this.getPosition());
        this.getMap().broadcastMessage(MobPacket.changeMonsterZone(this.getObjectId(), reduceDamageType), this.getPosition());
    }

    public void damage(MapleCharacter from, int lastSkill, long damage, boolean notKill) {
        if (from == null || damage <= 0 || !isAlive()) {
            return;
        }
        this.hpLock.lock();
        try {
            if (notKill) {
                if (this.hp <= 1L) {
                    return;
                }
            }
            AttackerEntry attacker;
            if (from.getParty() != null) {
                attacker = new PartyAttackerEntry(from.getParty().getId());
            } else {
                attacker = new SingleAttackerEntry(from);
            }
            boolean replaced = false;
            for (AttackerEntry aentry : getAttackers()) {
                if (aentry.equals(attacker)) {
                    attacker = aentry;
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                attackers.add(attacker);
            }
            damage *= (100.0 - Math.max(0.0, Math.min(this.reduceDamageR, 99.0))) / 100.0;
            if (this.hpLimitPercent > 0.0) {
                damage = Math.min((long) Math.max(0.0, this.hp - this.getMobMaxHp() * this.hpLimitPercent), damage);
            }
            if (notKill) {
                if (this.hp < damage) {
                    damage = this.hp - 1L;
                }
            }
            long rDamage = Math.max(0, Math.min(damage, hp));
            final MapleStatEffect effect = from.getEffectForBuffStat(SecondaryStat.Thaw);
            if (effect != null && !effect.isSkill()) {
                rDamage = 1L;
            }
            attacker.addDamage(from, rDamage);
            if (stats.getSelfD() != -1) {
                hp -= rDamage;
                if (hp > 0) {
                    for (AttackerEntry mattacker : getAttackers()) {
                        for (int cattacker : mattacker.getAttackers()) {
                            MapleCharacter chr = map.getPlayerObject(cattacker);
                            if (chr != null) {
                                chr.send(MobPacket.showMonsterHP(getObjectId(), getHPPercent()));
                            }
                        }
                    }
                }
                if (hp < stats.getSelfDHp()) {
                    map.killMonster(this, from, false, false, stats.getSelfD(), lastSkill);
                }

                if (getHPPercent() <= 30 && (getId() == 8880100 || getId() == 8880110)) {

                    if (!demianChangePhase) {
                        map.showWeatherEffectNotice("戴米安已經完全陷入黑暗.", 216, 30000000);
                        map.broadcastMessage(MobPacket.ChangePhaseDemian(this, 0x4F));
                        demianChangePhase = true;

                        MapTimer.getInstance().schedule(() -> {
                            map.killMonster(this);
                        }, 5000);
                    }
                    return;
                }

            } else {
                final MapleMonster sponge = this.sponge.get();
                if (this.hp > 0L && (!this.getStats().isInvincible() || getId() == 9400080)) {
                    this.hp -= rDamage;
                }
                if (eventInstance != null) {
                    eventInstance.getHooks().playerHit(from, this, damage);
//                    eventInstance.monsterDamaged(from, this, damage);
                } else {
                    ScriptEvent em = from.getEventInstance();
                    if (em != null) {
                        em.getHooks().playerHit(from, this, damage);
//                        em.monsterDamaged(from, this, damage);
                    }
                }
                if (sponge == null) {
                    switch (stats.getHPDisplayType()) {
                        case 0:
                            map.broadcastMessage(FieldPacket.fieldEffect(FieldEffect.mobHPTagFieldEffect(this)), this.getPosition());
                            break;
                        case 1:
                            map.broadcastMessage(from, MobPacket.MobDamaged(this, 1, damage, true), true);
                            break;
                        case 2:
                            map.broadcastMessage(MobPacket.showMonsterHP(getObjectId(), getHPPercent()));
                            from.mulung_EnergyModify(true);
                            break;
                        case 3:
                            for (AttackerEntry mattacker : getAttackers()) {
                                for (int cattacker : mattacker.getAttackers()) {
                                    MapleCharacter chr = map.getPlayerObject(cattacker);
                                    if (chr != null) {
                                        chr.getClient().announce(MobPacket.showMonsterHP(getObjectId(), getHPPercent()));
                                        continue;
                                    }
                                }
                            }
                            break;
                        default:
                            map.broadcastMessage(MobPacket.showMonsterHP(getObjectId(), getHPPercent()));
                            break;
                    }
                }
                if (hp <= 0) {
                    if (stats.getHPDisplayType() == 0) {
                        map.broadcastMessage(FieldPacket.fieldEffect(FieldEffect.mobHPTagFieldEffect(getId(), -1, 1, (byte) 1, (byte) 5)), this.getPosition());
                    }
                    SecondaryStatValueHolder holder = from.getBuffStatValueHolder(SecondaryStat.RunePurification);
                    if (holder != null && holder.value == 1) {
                        from.send(MaplePacketCreator.objSkillEffect(getObjectId(), 80002888, from.getId(), new Point(0, 0)));
                        final MapleForceAtom force = MapleForceFactory.getInstance().getMapleForce(from, from.getEffectForBuffStat(SecondaryStat.RunePurification), 1, getObjectId());
                        force.setForcedTarget(getPosition());
                        from.send(ForcePacket.forceAtomCreate(force));
                        holder.z = Math.min(holder.z + 25, 1000);
                    }
                    this.lastKill = from.getId();
                    from.getCheatTracker().gainMultiKill();
                    from.getCheatTracker().setLastKillMobOid(getObjectId());
                    from.getKillMonsterExp().addAndGet(stats.getExp());
//                    from.beforeKillMonster(getObjectId(), lastSkill);
                    map.killMonster(this, from, true, false, (byte) 1, lastSkill); //殺死怪物
                    this.sponge.clear();
                }

                MapleStatEffect eff = from.getSkillEffect(凱殷.事前準備);
                if (eff == null) {
                    eff = from.getSkillEffect(凱殷.事前準備_傳授);
                }
                if (eff != null) {
                    boolean apply = false;
                    if (hp <= 0) {
                        int killCount = (int) from.getTempValues().getOrDefault("事前準備擊殺", 0) + 1;
                        if (killCount >= eff.getX()) {
                            killCount = 0;
                            apply = true;
                        }
                        from.getTempValues().put("事前準備擊殺", killCount);
                    } else if (isBoss()) {
                        int attackCount = (int) from.getTempValues().getOrDefault("事前準備攻擊", 0) + 1;
                        if (attackCount >= eff.getU()) {
                            attackCount = 0;
                            apply = true;
                        }
                        from.getTempValues().put("事前準備攻擊", attackCount);
                    }
                    if (apply) {
                        SecondaryStatValueHolder mbsvh;
                        if ((mbsvh = from.getBuffStatValueHolder(凱殷.事前準備_計數)) == null) {
                            SkillFactory.getSkill(凱殷.事前準備_計數).getEffect(eff.getLevel()).applyTo(from);
                        } else {
                            int oldValue = mbsvh.value;
                            if (mbsvh.value < eff.getW() - 1) {
                                mbsvh.value += 1;
                            } else if (from.isSkillCooling(eff.getSourceId())) {
                                if (mbsvh.value != eff.getW()) {
                                    mbsvh.value = eff.getW();
                                }
                            } else {
                                from.dispelEffect(凱殷.事前準備_計數);
                                eff.applyTo(from);
                            }
                            if (oldValue != mbsvh.value) {
                                from.send(BuffPacket.giveBuff(from, mbsvh.effect, Collections.singletonMap(SecondaryStat.NALinkSkill, mbsvh.sourceID)));
                            }
                        }
                    }
                }

                if ((eff = from.getSkillEffect(凱殷.掌握痛苦)) != null) {
                    boolean apply = false;
                    if (hp <= 0) {
                        int killCount = (int) from.getTempValues().getOrDefault("掌握痛苦擊殺", 0) + 1;
                        if (killCount >= eff.getQ()) {
                            killCount = 0;
                            apply = true;
                        }
                        from.getTempValues().put("掌握痛苦擊殺", killCount);
                    } else if (isBoss()) {
                        int attackCount = (int) from.getTempValues().getOrDefault("掌握痛苦攻擊", 0) + 1;
                        if (attackCount >= eff.getQ2()) {
                            attackCount = 0;
                            apply = true;
                        }
                        from.getTempValues().put("掌握痛苦攻擊", attackCount);
                    }
                    if (apply) {
                        SecondaryStatValueHolder mbsvh;
                        if ((mbsvh = from.getBuffStatValueHolder(凱殷.掌握痛苦)) == null) {
                            eff.applyTo(from, true);
                        } else {
                            int oldValue = mbsvh.value;
                            if (mbsvh.value < eff.getU() - 1) {
                                mbsvh.value += 1;
                            } else if (mbsvh.value != eff.getU()) {
                                mbsvh.value = eff.getU();
                            }
                            if (oldValue != mbsvh.value) {
                                from.send(BuffPacket.giveBuff(from, mbsvh.effect, Collections.singletonMap(SecondaryStat.NAOminousStream, mbsvh.sourceID)));
                            }
                        }
                    }
                }

                if (sponge != null) {
                    sponge.damage(from, lastSkill, rDamage, notKill);
                }
//                if (this.getEventInstance() != null && (this.getEventInstance().getName().equals("BossMagnus_EASY") || this.getEventInstance().getName().equals("BossMagnus_NORMAL") || this.getEventInstance().getName().equals("BossMagnus_HARD"))) {
//                    final int reduceDamageType = (int) (this.getStats().getMobZone().size() - Math.ceil(hp / this.getMobMaxHp() * this.getStats().getMobZone().size())) + 1;
//                    if (Integer.valueOf(this.getEventInstance().getProperty("HPstate")) < reduceDamageType) {
//                        this.getEventInstance().setProperty("HPstate", String.valueOf(reduceDamageType));
//                        this.setReduceDamageType(reduceDamageType);
//                        this.sendMobZone(reduceDamageType);
//                    }
//                }
                checkMobZoneState();
            }
            startDropItemSchedule();
        } finally {
            this.hpLock.unlock();
        }
    }

    public int getHPPercent() {
        return (int) Math.ceil(hp * 100.0 / getMobMaxHp());
    }

    public void killed() {
        if (listener != null) {
            listener.monsterKilled();
        }
        listener = null;
        if (deadBound != null) {
            deadBound.cancel(false);
        }
        deadBound = null;
        hp = 0;
    }

    private void giveExpToCharacter(MapleCharacter attacker, long baseEXP, final int partyMenberSize, int recallRingEXP, final int lastSkill, final boolean lastKill) {
        if (baseEXP > 0) {
            attacker.getTrait(MapleTraitType.charisma).addExp(stats.getCharismaEXP(), attacker);
            handleExp(attacker, baseEXP, partyMenberSize, recallRingEXP, lastKill);
            attacker.getStat().checkEquipLevels(attacker, baseEXP);
            if (lastKill) {
                MobCollectionFactory.tryCollect(attacker, this);
            }
        }
        //角色擊殺怪物數量計算
        attacker.mobKilled(getId(), lastSkill);
        // 等級範圍怪物
        if (Math.abs(getMobLevel() - attacker.getLevel()) <= 20) {
            attacker.mobKilled(9101025, lastSkill);
        }
        // 菁英怪物
        if (isEliteMob()) {
            attacker.mobKilled(9101067, lastSkill);
        }
        // 菁英BOSS
        if (getEliteType() == 2) {
            attacker.mobKilled(9101064, lastSkill);
        }
        // 燃燒地圖怪物
        if (getMap() != null && getMap().getBreakTimeFieldStep() > 0 && !isBoss()) {
            attacker.mobKilled(9101114, lastSkill);
        }
        // 星力怪物
        if (getMap() != null && getMap().getBarrier() > 0 && !isBoss()) {
            attacker.mobKilled(9101084, lastSkill);
        }
        if ((attacker.getLevel() <= 200 && this.getMobLevel() >= attacker.getLevel() - 10 && this.getMobLevel() <= attacker.getLevel() + 10) || (attacker.getLevel() > 200 && this.getMobLevel() >= 200)) {
            final String date = DateUtil.getFormatDate(System.currentTimeMillis(), "yyyyMMdd");
            if (!date.equals(attacker.getQuestInfo(502117, "date"))) {
                attacker.updateOneQuestInfo(502117, "date", date);
                attacker.updateOneQuestInfo(502117, "count", "0");
            }
            attacker.updateOneQuestInfo(502117, "count", String.valueOf(Integer.valueOf(attacker.getQuestInfo(502117, "count")) + 1));
        }
    }

    public void handleExp(MapleCharacter chr, long gain, int partyMenberSize, final int recallRingId, final boolean lastKill) {
        if (ServerConfig.WORLD_BANGAINEXP) {
            chr.dropMessage(6, "管理員禁止了經驗獲取。");
            return;
        }
        if (chr.getEventInstance() != null && chr.getEventInstance().isPractice()) {
            return;
        }
        if (map == null) {
            return;
        }
        AtomicLong totalExp = new AtomicLong(0);
        AtomicLong expLost = new AtomicLong(0);
        int diseaseType = 0;
        int decExpR = 100;
        DeadDebuff deadBuff = DeadDebuff.getDebuff(chr, -1);
        if (deadBuff != null) {
            diseaseType = 2;
            decExpR = Math.max(0, decExpR - deadBuff.DecExpR);
        }
        int runeCurseRate;
        if (chr.getRuneUseCooldown() <= 0 && (runeCurseRate = map.getRuneCurseRate()) > 0) {
            diseaseType = 1;
            decExpR = Math.max(0, decExpR - runeCurseRate);
        }
        if (!isBoss()) {
            if (map.getBarrier() > 0 || map.getBarrierArc() > 0 || map.getBarrierAut() > 0) {
                int lvGap = Math.abs(chr.getLevel() - getMobLevel());
                if (lvGap >= 0 && lvGap < 5) {
                    gain = Math.round(gain * 1.1);
                } else if (lvGap > 5 && lvGap < 10) {
                    gain = Math.round(gain * 1.05);
                }
            }
            if (Math.abs(chr.getLevel() - getMobLevel()) > 10) {
                if (getMobLevel() > chr.getLevel()) {
                    int lvGap = getMobLevel() - chr.getLevel();
                    if (lvGap <= 20) {
                        gain = Math.round(gain * (110 - lvGap) / 100.0);
                    } else if (lvGap <= 35) {
                        gain = Math.round(gain * (70 - (4 * (lvGap - 21))) / 100.0);
                    } else {
                        gain = Math.round(gain * 0.1);
                    }
                } else {
                    int lvGap = chr.getLevel() - getMobLevel();
                    if (lvGap <= 20) {
                        gain = Math.round(gain * (100 - Math.ceil((lvGap - 10) / 2)) / 100.0);
                    } else if (lvGap <= 40) {
                        gain = Math.round(gain * (110 - lvGap) / 100.0);
                    } else {
                        gain = Math.round(gain * 0.7);
                    }
                }
            }
        }
        gain = gain * ChannelServer.getInstance(map.getChannel()).getBaseExpRate() / 100;
        // 詛咒減少50%經驗
        if (chr.getBuffedValue(SecondaryStat.Curse) != null) {
            gain = gain * 50 / 100;
        }
        if (gain < 0) {
            gain = 0;
        }
        long eachGain = gain * decExpR / 100;
        totalExp.addAndGet(eachGain);
        expLost.addAndGet(gain - eachGain);

        long mobExp = Math.min(gain, Integer.MAX_VALUE);

        Map<MapleExpStat, Object> expStatup = new EnumMap<>(MapleExpStat.class);
        // 組隊經驗
        int expRate = 0;
        // 測試80%戒指，partyMemberSize只要1就會觸發
        if (partyMenberSize >= 1) {
            partyMenberSize = Math.min(partyMenberSize, 6);
            expRate += (5 * (partyMenberSize * (3 + (1 + partyMenberSize) / 2)) - 20);
            if (stats.getPartyBonusRate() > 0) {
                expRate += stats.getPartyBonusRate() * Math.min(4, partyMenberSize);
            } else if (map.getPartyBonusRate() > 0) {
                expRate += map.getPartyBonusRate() * Math.min(4, partyMenberSize);
            }
        }
        long eachExp = Math.max(Math.min(expRate * mobExp / 100, Integer.MAX_VALUE), 0);
        eachGain = eachExp * decExpR / 100;
        totalExp.addAndGet(eachGain);
        expLost.addAndGet(eachExp - eachGain);
        if (eachExp > 0) {
            expStatup.put(MapleExpStat.組隊額外經驗值, eachExp);
        }
        // 結婚紅利經驗
        expRate = 0;
        if (chr.getMarriageId() > 0) {
            MapleCharacter marrChr = map.getPlayerObject(chr.getMarriageId());
            if (marrChr != null) {
                expRate = 100;
            }
        }
        eachExp = Math.max(Math.min(expRate * mobExp / 100, Integer.MAX_VALUE), 0);
        eachGain = eachExp * decExpR / 100;
        totalExp.addAndGet(eachGain);
        expLost.addAndGet(eachExp - eachGain);
        if (eachExp > 0) {
            expStatup.put(MapleExpStat.結婚紅利經驗值, eachExp);
        }
        // 道具裝備紅利經驗
        expRate = 0;
        for (Entry<Integer, List<Integer>> bonusExps : chr.getStat().getEquipmentBonusExps().entrySet()) {
            expRate += bonusExps.getValue().get(!chr.getFairys().containsKey(bonusExps.getKey()) ? 0 : chr.getFairys().get(bonusExps.getKey()).getLeft());
        }
        eachExp = Math.max(Math.min(expRate * mobExp / 100, Integer.MAX_VALUE), 0);
        eachGain = eachExp * decExpR / 100;
        totalExp.addAndGet(eachGain);
        expLost.addAndGet(eachExp - eachGain);
        if (eachExp > 0) {
            expStatup.put(MapleExpStat.道具裝備紅利經驗值, eachExp);
        }
        // 高級服務贈送經驗
        expRate = 0;
        if (chr.haveItem(5420008)) {
            if (chr.isRedMvp()) {
                expRate += 200;
            } else if (chr.isDiamondMvp()) {
                expRate += 150;
            } else if (chr.isGoldMvp()) {
                expRate += 100;
            } else if (chr.isSilverMvp()) {
                expRate += 50;
            } else {
                expRate += 30;
            }
        }
        eachExp = Math.max(Math.min(expRate * mobExp / 100, Integer.MAX_VALUE), 0);
        eachGain = eachExp * decExpR / 100;
        totalExp.addAndGet(eachGain);
        expLost.addAndGet(eachExp - eachGain);
        if (eachExp > 0) {
            expStatup.put(MapleExpStat.高級服務贈送經驗值, eachExp);
        }
        // 秘藥額外經驗
        eachExp = Math.min(Math.max(mobExp * chr.getStat().plusExpRate / 100, 0), Integer.MAX_VALUE);
        eachGain = eachExp * decExpR / 100;
        totalExp.addAndGet(eachGain);
        expLost.addAndGet(eachExp - eachGain);
        if (eachExp > 0) {
            expStatup.put(MapleExpStat.秘藥額外經驗值, eachExp);
        }
        // 神使 神之子，精靈的祝福，皮卡的啾品格
        expRate = 0;
        Skill skil = SkillFactory.getSkill(精靈遊俠.精靈的祝福);
        int skilLevel = chr.getTotalSkillLevel(skil);
        if (skilLevel > 0 && JobConstants.is精靈遊俠(chr.getJob())) {
            expRate += skil.getEffect(skilLevel).getExpR();
        } else {
            skil = SkillFactory.getSkill(精靈遊俠.精靈的祝福_傳授);
            skilLevel = chr.getTotalSkillLevel(skil);
            if (skilLevel > 0) {
                expRate += skil.getEffect(skilLevel).getExpR();
            }
        }
        // 神使 神之子處理
        skil = SkillFactory.getSkill(71000711);
        skilLevel = chr.getTotalSkillLevel(skil);
        if (skilLevel > 0) {
            expRate += skil.getEffect(skilLevel).getExpR();
        }
        skil = SkillFactory.getSkill(皮卡啾.皮卡啾的品格);
        skilLevel = chr.getTotalSkillLevel(skil);
        if (skilLevel > 0) {
            expRate += skil.getEffect(skilLevel).getExpR();
        }
        skil = SkillFactory.getSkill(陰陽師.鬼神召喚);
        skilLevel = chr.getTotalSkillLevel(skil);
        if (skilLevel > 0) {
            expRate += skil.getEffect(skilLevel).getExpR();
        }
        eachExp = Math.max(Math.min((int) ((expRate + chr.getStat().expRPerM / 100d) * mobExp) / 100, Integer.MAX_VALUE), 0);
        eachGain = eachExp * decExpR / 100;
        totalExp.addAndGet(eachGain);
        expLost.addAndGet(eachExp - eachGain);
        if (chr.getSkillLevel(80000420) > 0 && eachExp > 0) {
            expStatup.put(MapleExpStat.極限屬性經驗值, eachExp);
        }
        // 加持獎勵經驗
        expRate = 0;
        // 挑釁
        MonsterEffectHolder ms = getEffectHolder(MonsterStatus.Showdown);
        if (ms != null && ms.sourceID == 夜使者.挑釁契約) {
            expRate += ms.value;
        }
        expRate += chr.getEXPMod() * 100 - 100;
        expRate += chr.getStat().expBuff;
        expRate += chr.getStat().getIncEXPr();
        eachExp = Math.max(Math.min(expRate * mobExp / 100, Integer.MAX_VALUE), 0);
        eachGain = eachExp * decExpR / 100;
        totalExp.addAndGet(eachGain);
        expLost.addAndGet(eachExp - eachGain);
        if (eachExp > 0) {
            expStatup.put(MapleExpStat.加持獎勵經驗值, eachExp);
        }
        // 燃燒場地獎勵
        expRate = 0;
        int BuringFieldStep = Math.max(0, map.getBreakTimeFieldStep()) * 10;
        expRate += BuringFieldStep;
        eachExp = Math.max(Math.min(expRate * mobExp / 100, Integer.MAX_VALUE), 0);
        eachGain = eachExp * decExpR / 100;
        totalExp.addAndGet(eachGain);
        expLost.addAndGet(eachExp - eachGain);
        if (eachExp > 0) {
            expStatup.put(MapleExpStat.燃燒場地獎勵經驗, new Pair<>(eachExp, BuringFieldStep));
        }
        // 召回戒指經驗
        expRate = 0;
        if (recallRingId > 0) {
            expRate += 80;
        }
        eachExp = Math.max(Math.min(expRate * mobExp / 100, Integer.MAX_VALUE), 0);
        eachGain = eachExp * decExpR / 100;
        totalExp.addAndGet(eachGain);
        expLost.addAndGet(eachExp - eachGain);
        if (eachExp > 0) {
            expStatup.put(MapleExpStat.道具名經驗值, new Pair<>(eachExp, recallRingId));
        }
        // 寵物訓練紅利經驗
        expRate = 0;
        for (MaplePet pet : chr.getSpawnPets()) {
            if (pet == null || pet.getAddSkill() != 6) {
                continue;
            }
            expRate += expRate == 0 ? 5 : expRate == 5 ? 7 : 8;
        }
        expRate = Math.min(20, expRate);
        eachExp = Math.max(Math.min(expRate * mobExp / 100, Integer.MAX_VALUE), 0);
        eachGain = eachExp * decExpR / 100;
        totalExp.addAndGet(eachGain);
        expLost.addAndGet(eachExp - eachGain);
        if (eachExp > 0) {
            expStatup.put(MapleExpStat.寵物訓練紅利經驗值, (int) eachExp);
        }
        // 活動獎勵經驗
        expRate = Math.max(ChannelServer.getInstance(map.getChannel()).getDoubleExp() - 1, 0) * 100;
        if (chr.getLevel() < 10 && JobConstants.notNeedSPSkill(chr.getJob())) {
            expRate = 0;
        }
        eachExp = Math.max(Math.min(expRate * mobExp / 100, Integer.MAX_VALUE), 0);
        eachGain = eachExp * decExpR / 100;
        totalExp.addAndGet(eachGain);
        expLost.addAndGet(eachExp - eachGain);
        if (eachExp > 0) {
            expStatup.put(MapleExpStat.活動獎勵經驗值2, eachExp);
        }
        // 伺服器倍率經驗
        expRate = Math.max(ChannelServer.getInstance(map.getChannel()).getExpRate() - 1, 0);
        if (chr.getLevel() < 10 && JobConstants.notNeedSPSkill(chr.getJob())) {
            expRate = 0;
        }
        eachExp = Math.max(Math.min(expRate * mobExp, Integer.MAX_VALUE), 0);
        eachGain = eachExp * decExpR / 100;
        totalExp.addAndGet(eachGain);
        expLost.addAndGet(eachExp - eachGain);
        if (eachExp > 0) {
            expStatup.put(MapleExpStat.伺服器加持經驗值, expRate * 100);
        }
        // 艾爾達斯還原追加經驗
        expRate = 0;
        ms = getEffectHolder(MonsterStatus.ErdaRevert);
        if (ms != null) {
            expRate += 150;
        }
        eachExp = Math.max(Math.min(expRate * mobExp / 100, Integer.MAX_VALUE), 0);
        eachGain = eachExp * decExpR / 100;
        totalExp.addAndGet(eachGain);
        expLost.addAndGet(eachExp - eachGain);
        if (eachExp > 0) {
            expStatup.put(MapleExpStat.艾爾達斯還原追加經驗值, (int) eachExp);
        }
        if (totalExp.get() < 0) {
            totalExp.set(Integer.MAX_VALUE);
        }
        chr.gainExp(totalExp.get(), false, false, lastKill);
        chr.send(MaplePacketCreator.showGainExp(mobExp, true, false, diseaseType, -expLost.get(), expStatup));
    }

    public void killBy(MapleCharacter killer) {
        if (this.eventInstance != null) {
            this.eventInstance.getHooks().mobDied(this, killer);
//            this.eventInstance.monsterKilled(killer, this);
//            this.eventInstance.unregisterMonster(this);
            this.eventInstance = null;
            return;
        }
        if (killer != null && killer.getEventInstance() != null) {
            killer.getEventInstance().getHooks().mobDied(this, killer);
//            killer.getEventInstance().monsterKilled(killer, this);
        }
    }

    public void killGainExp(MapleCharacter chr, int lastSkill) {
        long mobExp = this.getMobExp();
        List<AttackerEntry> list = getAttackers();
        long baseExp;
        for (AttackerEntry attackEntry : list) {
            if (attackEntry != null) {
                baseExp = (long) Math.ceil(mobExp * (0.2 * (chr != null && attackEntry.contains(chr) ? 1 : 0) + 0.8 * ((double) attackEntry.getDamage() / getMobMaxHp())));
                if (ServerConfig.TESPIA && ServerConfig.MULTIPLAYER_TEST) {
                    assert chr != null;
                    if (chr.getMap() == null || chr.getMap().getMapObjectsInRange(chr.getPosition(), chr.getRange(), Collections.singletonList(MapleMapObjectType.PLAYER)).size() < 2) {
                        chr.dropMessage(-1, "由於需要測試多人BUG, 測試服必須附近有其他玩家才能獲得經驗。");
                        if (!chr.isIntern()) {
                            continue;
                        }
                    }
                }
                attackEntry.killedMob(chr != null && chr.getClient() != null ? chr.getClient().getWorld() : null, this, baseExp, lastSkill);
            }
        }
    }

    public void spawnRevives() {
        List<Integer> toSpawn = stats.getRevives();
        if (toSpawn == null || this.getLinkCID() > 0 || spawnRevivesDisabled) {
            //FileoutputUtil.log(FileoutputUtil.Pinkbean_Log, "開始 : toSpawn == null 跳過", true);
            return;
        }
        MapleMonster spongy = null;
        long spongyHp = 0;
        //FileoutputUtil.log(FileoutputUtil.Pinkbean_Log, "-----------------------------------------------", true);
        //FileoutputUtil.log(FileoutputUtil.Pinkbean_Log, "開始 : " + getId() + " - " + stats.getName(), true);
        switch (getId()) {
        }
        switch (getId()) {
            case 8820103: //混沌之賢者所羅門
            case 8820104: //混沌之賢者萊克斯
            case 8820105: //混沌火鷹雕像
            case 8820106: //混沌冰鷹雕像
                if (!toSpawn.contains(getId() - 79)) {
                    toSpawn.add(getId() - 79);
                }
            case 8820003: //賢者所羅門
            case 8820004: //賢者萊克斯
            case 8820005: //火鷹雕像
            case 8820006: { //冰鷹雕像
                for (int i : toSpawn) {
                    MapleMonster mob = MapleLifeFactory.getMonster(i);
                    if (mob == null) {
                        continue;
                    }
//                    if (eventInstance != null) {
//                        eventInstance.registerMonster(mob);
//                    }
                    mob.setPosition(getPosition());
                    mob.setLinkOid(getLinkOid());
                    if (dropsDisabled()) {
                        mob.disableDrops();
                    }
                    map.spawnMonster(mob, -2, true);
                }
                break;
            }
            case 8820002: //女神雕像
            case 8820102: //混沌女神雕像
            case 8820115: //混沌之賢者所羅門
            case 8820116: //混沌之賢者萊克斯
            case 8820117: //混沌火鷹雕像
            case 8820118: //混沌冰鷹雕像
            case 8820213: //混沌之賢者所羅門
            case 8820214: //混沌之賢者所羅門
            case 8820215: //混沌之賢者萊克斯
            case 8820216: //混沌之賢者萊克斯
            case 8820217: //混沌火鷹雕像
            case 8820218: //混沌火鷹雕像
            case 8820219: //混沌火鷹雕像
            case 8820220: //混沌火鷹雕像
            case 8820221: //混沌冰鷹雕像
            case 8820222: //混沌冰鷹雕像
            case 8820223: //混沌冰鷹雕像
            case 8820224: //混沌冰鷹雕像
            case 8820225: //混沌女神雕像
            case 8820226: //混沌女神雕像
            case 8820227: //混沌女神雕像
            case 8840000: //凡雷恩
            case 6160003: //薛西斯
            case 8850011: //西格諾斯
            case 8950000:
            case 8950001:
            case 8950100:
            case 8950101:
                //FileoutputUtil.log(FileoutputUtil.Pinkbean_Log, "跳過 : " + getId() + " - " + stats.getName(), true);
                break;
            case 8810118: //進階闇黑龍王
            case 8810119: //進階闇黑龍王
            case 8810120: //進階闇黑龍王
            case 8810121: //進階闇黑龍王
                for (int i : toSpawn) {
                    MapleMonster mob = MapleLifeFactory.getMonster(i);
                    mob.setPosition(getPosition());
//                    if (eventInstance != null) {
//                        eventInstance.registerMonster(mob);
//                    }
                    if (dropsDisabled()) {
                        mob.disableDrops();
                    }
                    switch (mob.getId()) {
                        case 8810119: //進階闇黑龍王
                        case 8810120: //進階闇黑龍王
                        case 8810121: //進階闇黑龍王
                        case 8810122: //進階闇黑龍王
                            spongy = mob;
                            spongy.setSpongeMob(true);
                            break;
                    }
                }
                if (spongy != null && map.getMobObjectByID(spongy.getId()) == null) {
                    map.spawnMonster(spongy, -2);
                    for (MapleMapObject mon : map.getMonsters()) {
                        MapleMonster mons = (MapleMonster) mon;
                        if (mons.getObjectId() != spongy.getObjectId() && (mons.getSponge() == this || mons.getLinkOid() == this.getObjectId())) { //sponge was this, please update
                            mons.setSponge(spongy);
                        }
                    }
                }
                break;
            case 8820300: //混沌皮卡啾1階段
            case 8820301: //混沌皮卡啾2階段
            case 8820302: //混沌皮卡啾3階段
            case 8820303: //混沌皮卡啾4階段
                MapleMonster linkMob = MapleLifeFactory.getMonster(getId() - 190);
                if (linkMob != null) {
                    toSpawn = linkMob.getStats().getRevives();
                }
            case 8820108: //寶寶BOSS召喚用透明怪物
            case 8820109: //set0透明怪物
                List<MapleMonster> cs_mobs = new ArrayList<>();
                for (int i : toSpawn) {
                    MapleMonster mob = MapleLifeFactory.getMonster(i);
                    assert mob != null;
                    mob.setPosition(getPosition());
//                    if (eventInstance != null) {
//                        eventInstance.registerMonster(mob);
//                    }
                    if (dropsDisabled()) {
                        mob.disableDrops();
                    }
                    switch (mob.getId()) {
                        case 8820109: //set0透明怪物
                        case 8820300: //混沌皮卡啾1階段
                        case 8820301: //混沌皮卡啾2階段
                        case 8820302: //混沌皮卡啾3階段
                        case 8820303: //混沌皮卡啾4階段
                        case 8820304: //混沌皮卡啾5階段
                            spongy = mob;
                            spongy.setSpongeMob(true);
                            //FileoutputUtil.log(FileoutputUtil.Pinkbean_Log, "第2次 : spongy : " + mob.getId() + " - " + mob.getStats().getName(), true);
                            break;
                        default:
                            if (mob.isFirstAttack()) {
                                spongyHp += mob.getMobMaxHp();
                            }
                            cs_mobs.add(mob);
                            //FileoutputUtil.log(FileoutputUtil.Pinkbean_Log, "第2次 : addMob : " + mob.getId() + " - " + mob.getStats().getName(), true);
                            break;
                    }
                }
                if (spongy != null && map.getMobObjectByID(spongy.getId()) == null) {
                    if (spongyHp > 0) {
                        spongy.setHp(spongyHp);
                        spongy.getStats().setHp(spongyHp);
                    }
                    //FileoutputUtil.log(FileoutputUtil.Pinkbean_Log, "第2次 : spongyHp : " + spongyHp, true);
                    map.spawnMonster(spongy, -2);
                    for (MapleMonster i : cs_mobs) {
                        map.spawnMonster(i, -2);
                        i.setSponge(spongy);
                    }
                }
                break;
            case 8880100:
            case 8880101:
            case 8880110:
            case 8880111:
                if (getId() == 8880111 || getId() == 8880101) {
                    if (getCustomValue0(8880111) == 1L) {
                        addSkillCustomInfo(8880112, 99);
                        if (getCustomValue0(8880112) >= 1000000000L) {
                            getMap().broadcastMessage(MobPacket.demianRunaway(this, (byte) 1, MobSkillFactory.getMobSkill(214, 14), 0));
                            setCustomInfo(8880111, 2, 0);
                        }
                    }
                }
                break;
            case 8810026: //召喚闇黑龍王
            case 8810130: //召喚進階闇黑龍王
            case 8820008: //寶寶BOSS召喚用透明怪物
            case 8820009: //set0透明怪物
            case 8820010: //皮卡啾
            case 8820011: //皮卡啾
            case 8820012: //皮卡啾
            case 8820013: //皮卡啾
                List<MapleMonster> mobs = new ArrayList<>();
                long maxHP = 0;
                for (int i : toSpawn) {
                    MapleMonster mob = MapleLifeFactory.getMonster(i);
                    mob.setPosition(getPosition());
//                    if (eventInstance != null) {
//                        eventInstance.registerMonster(mob);
//                    }
                    if (dropsDisabled()) {
                        mob.disableDrops();
                    }
                    switch (mob.getId()) {
                        case 8810122: //進階黑暗龍王
                        case 8810018: //闇黑龍王的靈魂
                        case 8810118: //進階闇黑龍王
                        case 8820009: //set0透明怪物
                        case 8820010: //皮卡啾1
                        case 8820011: //皮卡啾2
                        case 8820012: //皮卡啾3
                        case 8820013: //皮卡啾4
                        case 8820014: //皮卡啾5
                            spongy = mob;
                            spongy.setSpongeMob(true);
                            //FileoutputUtil.log(FileoutputUtil.Pinkbean_Log, "第2次 : spongy : " + mob.getId() + " - " + mob.getStats().getName(), true);
                            break;
                        case 8820002: //賢者所羅門
                        case 8820003: //賢者所羅門
                        case 8820004: //賢者萊克斯
                        case 8820005: //火鷹雕像
                        case 8820006: //冰鷹雕像
                            maxHP += mob.getMaxHP();
                        default:
                            mobs.add(mob);
                            //FileoutputUtil.log(FileoutputUtil.Pinkbean_Log, "第2次 : addMob : " + mob.getId() + " - " + mob.getStats().getName(), true);
                            break;
                    }
                }
                if (spongy != null && map.getMobObjectByID(spongy.getId()) == null) {
                    //FileoutputUtil.log(FileoutputUtil.Pinkbean_Log, "第2次 : spongyHp : " + spongyHp, true);
                    if (maxHP > 0 && spongy.getMaxHP() != maxHP) {
                        spongy.getStats().setChange(true);
                        spongy.changeHP(maxHP);
                    }
                    map.spawnMonster(spongy, -2);
                    for (MapleMonster i : mobs) {
                        map.spawnMonster(i, -2);
                        i.setSponge(spongy);
                    }
                }
                break;
            case 8820304: //混沌皮卡啾
                MapleMonster linkMob_1 = MapleLifeFactory.getMonster(getId() - 190);
                if (linkMob_1 != null) {
                    toSpawn = linkMob_1.getStats().getRevives();
                }
            case 8820014: //皮卡啾
            case 8820101: //混沌皮卡啾
            case 8820200: //混沌皮卡啾
            case 8820201: //混沌皮卡啾
            case 8820202: //混沌皮卡啾
            case 8820203: //混沌皮卡啾
            case 8820204: //混沌皮卡啾
            case 8820205: //混沌皮卡啾
            case 8820206: //混沌皮卡啾
            case 8820207: //混沌皮卡啾
            case 8820208: //混沌皮卡啾
            case 8820209: //混沌皮卡啾
            case 8820210: //混沌皮卡啾
            case 8820211: {//混沌皮卡啾
                for (int i : toSpawn) {
                    MapleMonster mob = MapleLifeFactory.getMonster(i);
//                    if (eventInstance != null) {
//                        eventInstance.registerMonster(mob);
//                    }
                    mob.setPosition(getPosition());
                    if (dropsDisabled()) {
                        mob.disableDrops();
                    }
                    map.spawnMonster(mob, -2);
                    //FileoutputUtil.log(FileoutputUtil.Pinkbean_Log, "第3次 : " + mob.getId() + " - " + mob.getStats().getName(), true);
                }
                break;
            }
            default:
                for (int i : toSpawn) {
                    MapleMonster mob = MapleLifeFactory.getMonster(i);
                    if (mob == null) {
                        continue;
                    }
//                    if (eventInstance != null) {
//                        eventInstance.registerMonster(mob);
//                    }
                    mob.setPosition(getPosition());
                    if (dropsDisabled()) {
                        mob.disableDrops();
                    }
                    map.spawnRevives(mob, this.getObjectId());
                }
                break;
        }
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public byte getCarnivalTeam() {
        return carnivalTeam;
    }

    public void setCarnivalTeam(byte team) {
        carnivalTeam = team;
    }

    public MapleCharacter getController() {
        controllerLock.lock();
        try {
            return controller.get();
        } finally {
            controllerLock.unlock();
        }
    }

    public void updateController(List<MapleCharacter> characters) {
        if (this.getController() != null || this.hp <= 0L) {
            return;
        }
        controllerLock.lock();
        try {
            int mobControlledSize = -1;
            MapleCharacter newController = null;
            for (MapleCharacter chr : characters) {
                if (!chr.isHidden() && (this.getDwOwnerID() <= 0 || this.getDwOwnerID() == chr.getId()) && chr.getMap() == map && (chr.getMobControlledSize() < mobControlledSize || mobControlledSize == -1) && chr.getPosition().distance(getPosition()) < getRange()) {
                    mobControlledSize = chr.getMobControlledSize();
                    newController = chr;
                }
            }
            if (newController != null) {
                setController(newController);
            }
        } finally {
            controllerLock.unlock();
        }
    }

    public void switchController(MapleCharacter newController) {
        if (!controllerHasAggro && this.isAlive()) {
            final MapleCharacter controller;
            if ((controller = getController()) != null) {
                if (controller == newController) {
                    controllerHasAggro = true;
                    return;
                }
                removeController(controller);
            }
            controllerLock.lock();
            try {
                controllerHasAggro = true;
                setController(newController);
            } finally {
                controllerLock.unlock();
            }
        }
    }

    public void setController(MapleCharacter newController) {
        if (stats.isDefenseMob()) {
            return;
        }
        this.controller = new WeakReference<>(newController);
        newController.controlMonster(this);
        newController.getClient().announce(MobPacket.monsterChangeControllerNew(this, newController));
        newController.getClient().announce(MobPacket.monsterChangeController(this, 1, 0));
        if (isBoss()) {

            changeAggroTip(newController);
        }
    }

    public void removeController(final MapleCharacter chr) {
        controllerLock.lock();
        try {
            if (chr == this.controller.get()) {
                this.controller = new WeakReference<>(null);
                this.controllerHasAggro = false;
                chr.controlMonsterRemove(this);

                chr.getClient().announce(MobPacket.monsterChangeControllerNew(this, chr));
                chr.getClient().announce(MobPacket.monsterChangeController(this, 0, 0));
            }
        } finally {
            controllerLock.unlock();
        }
    }

    public void changeAggroTip(final MapleCharacter newController) {
        final StringBuilder sb = new StringBuilder();
        switch (this.getId()) {
            case 8850011:
            case 8880000:
            case 8880140:
            case 8880141:
            case 8880142:
            case 8880150:
            case 8880151:
            case 8880152:
            case 8880153:
            case 8880154: {
                this.aggroRank.put(newController.getName(), 9999);
                newController.send_other(MobPacket.showMonsterNotice(this.getId(), 0, sb.append(this.getStats().getName()).append("把").append(newController.getName()).append("視作了最危險的敵人。").toString()), true);
                //Lucid.ShowRank(this.getController().getClient(), aggroRank.get(0));
                break;
            }
        }
    }

    public boolean checkAggro(boolean bl2) {
        boolean bl3;
        boolean bl4 = false;
        boolean bl5 = false;
        boolean bl6 = bl3 = System.currentTimeMillis() - lastUpdateController > 3000;
        if (bl3 && (!this.isControllerKnowsAboutAggro() || bl2 && !this.getAggroRank().isEmpty())) {
            MapleCharacter controller = this.getController();
            ArrayList<Entry<String, Integer>> arrayList = new ArrayList<>(aggroRank.entrySet());
            arrayList.sort((entry, entry2) -> entry2.getValue() - entry.getValue());
            for (Entry<String, Integer> entry3 : arrayList) {
                boolean bl7;
                MapleCharacter newController = map.getCharacterByName(entry3.getKey());
                if (newController == null) {
                    aggroRank.put(entry3.getKey(), 0);
                    continue;
                }
                bl7 = newController.getPosition().distance(this.getPosition()) < 317;
                if (newController.isAlive() && this.getController() == newController && bl7) {
                    bl4 = true;
                    continue;
                }
                if (!newController.isAlive() || bl4 || !bl7) {
                    continue;
                }
                controller = map.getCharacterByName(entry3.getKey());
                bl4 = true;
            }
            if (controller != null && controller != this.getController()) {
                this.switchController(controller);
                bl5 = true;
            }
        }
        return bl5;
    }

    public void addListener(MonsterListener listener) {
        this.listener = listener;
    }

    public boolean isControllerHasAggro() {
        return controllerHasAggro;
    }

    public void setControllerHasAggro(boolean controllerHasAggro) {
        this.controllerHasAggro = controllerHasAggro;
    }

    public boolean isControllerKnowsAboutAggro() {
        return this.controllerKnowsAboutAggro;
    }

    public void setControllerKnowsAboutAggro(boolean controllerKnowsAboutAggro) {
        this.controllerKnowsAboutAggro = controllerKnowsAboutAggro;
    }

    public long getChangeTime() {
        return this.changeTime;
    }

    public void setChangeTime(final long changeTime) {
        this.changeTime = changeTime;
    }

    public int getReduceDamageType() {
        return this.reduceDamageType;
    }

    public void setReduceDamageType(final int reduceDamageType) {
        this.reduceDamageType = reduceDamageType;
    }

    public int getFollowChrID() {
        return this.followChrID;
    }

    public void setFollowChrID(final int followChrID) {
        this.followChrID = followChrID;
    }

    public boolean isEliteMob() {
        return this.eliteGrade >= 0;
    }

    public int getEliteGrade() {
        return this.eliteGrade;
    }

    public void setEliteGrade(final int monsterType) {
        this.eliteGrade = monsterType;
    }

    public int getEliteType() {
        return this.eliteType;
    }

    public void setEliteType(final int eliteType) {
        this.eliteType = eliteType;
    }

    public void sendStatus(MapleClient client) {
        if (ServerConfig.CHANNEL_APPLYMONSTERSTATUS) {
            return;
        }
        if (reflectpack != null) {
            client.announce(reflectpack);
        }
    }

    @Override
    public void sendSpawnData(MapleClient client) {
//        if (!isAlive()) {
//            return;
//        }
        client.announce(MobPacket.spawnMonster(this));
//        sendStatus(Client.client);
//        if (map != null && !stats.isEscort() && Client.getPlayer() != null && Client.getPlayer().getPosition().distance(getPosition()) <= GameConstants.maxViewRangeSq_Half()) {
//            map.updateMonsterController(this);
//        }
//        if (this.getEventInstance() != null && (this.getEventInstance().getName().equals("BossMagnus_EASY") || this.getEventInstance().getName().equals("BossMagnus_NORMAL") || this.getEventInstance().getName().equals("BossMagnus_HARD"))) {
//            this.sendMobZone((int) (5.0 - Math.ceil(hp / this.getMobMaxHp() * 4.0)));
//            Client.announce(MobPacket.getBossHatred(aggroRank));
//        }
//        if (this.getReduceDamageType() > 0) {
//            Client.announce(MobPacket.showMonsterPhaseChange(this.getObjectId(), this.getReduceDamageType()));
//        }
    }

    @Override
    public void sendDestroyData(MapleClient client) {
//        if (stats.isEscort() && getEventInstance() != null && lastNode >= 0) { //shammos
//            map.resetShammos(Client.client);
//        } else {
//            if (getController() != null && Client.getPlayer() != null && Client.getPlayer().getId() == getController().getId()) {
//                removeController(Client.getPlayer());
//            }
        MobPacket.killMonster(client.getPlayer(), getObjectId(), this.getAnimation());
//        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(stats.getName());
        sb.append(" Level=");
        sb.append(stats.getLevel());
        if (forcedMobStat != null) {
            sb.append("→");
            sb.append(forcedMobStat.getLevel());
        }
        sb.append("信息: ");
        sb.append(getHp());
        sb.append("/");
        sb.append(getMobMaxHp());
        sb.append("Hp, ");
        sb.append(getMp());
        sb.append("/");
        sb.append(getMobMaxMp());
        sb.append("Mp");
        sb.append("||仇恨目標: ");
        MapleCharacter chr = controller.get();
        sb.append(chr != null ? chr.getName() : "無");
        return sb.toString();
    }

    public String toMoreString() {
        StringBuilder sb = new StringBuilder();

        sb.append(stats.getName());
        sb.append("(");
        sb.append(getId());
        sb.append(") Level=");
        sb.append(stats.getLevel());
        if (forcedMobStat != null) {
            sb.append("→");
            sb.append(forcedMobStat.getLevel());
        }
        sb.append(" pos(");
        sb.append(getPosition().x);
        sb.append(",");
        sb.append(getPosition().y);
        sb.append(") 信息: ");
        sb.append(getHp());
        sb.append("/");
        sb.append(getMobMaxHp());
        sb.append("Hp, ");
        sb.append(getMp());
        sb.append("/");
        sb.append(getMobMaxMp());
        sb.append("Mp, oid: ");
        sb.append(getObjectId());
        sb.append(", def").append(forcedMobStat == null ? stats.getPDRate() : forcedMobStat.getPDRate());
        sb.append("||仇恨目標: ");
        MapleCharacter chr = controller.get();
        sb.append(chr != null ? chr.getName() : "無");
        return sb.toString();
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.MONSTER;
    }

    @Override
    public int getRange() {
        return GameConstants.maxViewRange();
    }

    public ScriptEvent getEventInstance() {
        return eventInstance;
    }

    public void setEventInstance(ScriptEvent eventInstance) {
        this.eventInstance = eventInstance;
        if (eventInstance == null && deadBound != null) {
            deadBound.cancel(true);
            deadBound = null;
        }
    }

    public boolean isMobile() {
        return this.stats.isMobile();
    }

    public ElementalEffectiveness getEffectiveness(Element e) {
//        if (mobEffects.size() > 0 && mobEffects.containsKey(MonsterStatus.Venom_2)) {
//            return ElementalEffectiveness.正常;
//        }
        return stats.getEffectiveness(e);
    }

    public void setDeadBound(Runnable command, long period) {
        if (deadBound != null) {
            deadBound.cancel(true);
        }
        long repeatTime = 0;
        deadBound = EtcTimer.getInstance().register(command, repeatTime);
    }

    public void setTempEffectiveness(final Element e, final long milli) {
        stats.setEffectiveness(e, ElementalEffectiveness.虛弱);
        EtcTimer.getInstance().schedule(() -> stats.removeEffectiveness(e), milli);
    }

    public boolean isFake() {
        return fake;
    }

    public void setFake(boolean fake) {
        this.fake = fake;
    }

    public MapleMap getMap() {
        return map;
    }

    public void setMap(MapleMap map) {
        this.map = map;
        startDropItemSchedule();
    }

    public List<Triple<Integer, Integer, Integer>> getSkills() {
        return stats.getSkills();
    }

    public int getSkillSize() {
        return stats.getSkillSize();
    }

    public boolean isFirstAttack() {
        return stats.isFirstAttack();
    }

    public int getBuffToGive() {
        return stats.getBuffToGive();
    }

    public int getStolen() {
        return stolen;
    }

    public void setStolen(int s) {
        this.stolen = s;
    }

    public int getLinkOid() {
        return linkoid;
    }

    public void setLinkOid(int lo) {
        this.linkoid = lo;
    }

    public int getLastNode() {
        return lastNode;
    }

    public void setLastNode(int lastNode) {
        this.lastNode = lastNode;
    }

    public boolean checkLastMove() {
        return System.currentTimeMillis() - this.lastMove > 8000L && this.lastMove > 0L;
    }

    public void setLastMove() {
        this.lastMove = System.currentTimeMillis();
    }

    public int getRewardSprinkleCount() {
        return rewardSprinkleCount;
    }

    public void setRewardSprinkleCount(int count) {
        rewardSprinkleCount = count;
    }

    public void cancelDropItem() {
        lastDropTime = 0;
        rewardSprinkleCount = 0;
    }

    public void startDropItemSchedule() {
        cancelDropItem();
        if (((!stats.isRewardSprinkle() || stats.getRewardSprinkleCount() <= 0) && stats.getDropItemPeriod() <= 0) || !isAlive()) {
            return;
        }
        rewardSprinkleCount = stats.getRewardSprinkleCount();
        lastDropTime = System.currentTimeMillis();
    }

    public boolean shouldDrop(long now) {
        return lastDropTime > 0 && isAlive() && (!stats.isRewardSprinkle() || rewardSprinkleCount > 0) && lastDropTime + (stats.isRewardSprinkle() ? 2 : stats.getDropItemPeriod()) * 1000L < now;
    }

    public void doDropItem(long now) {
        if (!isAlive() || (stats.isRewardSprinkle() && rewardSprinkleCount <= 0)) {
            cancelDropItem();
            return;
        }
        List<Integer> drops = new ArrayList<>();
        for (MonsterDropEntry mde : MapleMonsterInformationProvider.getInstance().retrieveDrop(getId())) {
            if (map != null && !mde.channels.isEmpty() && !mde.channels.contains(map.getChannel())) {
                continue;
            }
            if (mde.itemId > 0 && Randomizer.nextInt(999999) < mde.chance) {
                drops.add(mde.itemId);
            }
        }
        if (9300061 == getId()) { //月妙
            drops.add(4001101); //月妙的年糕
        }
        if (isAlive() && map != null) {
            for (int i = 0; i < drops.size(); i++) {
                Collections.shuffle(drops);
                int rewardSprinkleSpeed = stats == null ? 0 : stats.getRewardSprinkleSpeed();
                map.spawnAutoDrop(drops.get(0), getPosition(), rewardSprinkleSpeed, rewardSprinkleSpeed > 0 ? getObjectId() : 0);
            }
        }
        if (drops.isEmpty()) {
            return;
        }
        lastDropTime = now;
    }

    public byte[] getNodePacket() {
        return nodepack;
    }

    public void setNodePacket(byte[] np) {
        this.nodepack = np;
    }

    public void registerKill(long next) {
        this.nextKill = System.currentTimeMillis() + next;
    }

    public boolean shouldKill(long now) {
        return nextKill > 0 && now > nextKill;
    }

    public int getLinkCID() {
        return linkCID;
    }

    public void setLinkCID(int lc) {
        this.linkCID = lc;
    }

    public int getMobFH() {
        MapleFoothold fh = getMap().getFootholds().findBelow(getPosition());
        if (fh != null) {
            return fh.getId();
        }
        return 0;
    }

    public Map<String, Integer> getAggroRank() {
        return aggroRank;
    }

    public int getTriangulation() {
        return triangulation;
    }

    public void setTriangulation(int triangulation) {
        this.triangulation = triangulation;
    }

    public int getPatrolScopeX1() {
        return patrolScopeX1;
    }

    public void setPatrolScopeX1(int patrolScopeX1) {
        this.patrolScopeX1 = patrolScopeX1;
    }

    public int getPatrolScopeX2() {
        return patrolScopeX2;
    }

    public void setPatrolScopeX2(int patrolScopeX2) {
        this.patrolScopeX2 = patrolScopeX2;
    }

    public int getSeperateSoulSrcOID() {
        return seperateSoulSrcOID;
    }

    public void setSeperateSoulSrcOID(int seperateSoulSrcOID) {
        this.seperateSoulSrcOID = seperateSoulSrcOID;
    }

    public List<Integer> getReviveMobs() {
        return reviveMobs;
    }

    public short getAppearType() {
        return this.newSpawn ? this.appearType : -1;
    }

    public void setAppearType(short appearType) {
        this.appearType = appearType;
    }

    public boolean isSteal() {
        return steal;
    }

    public void setSteal(boolean steal) {
        this.steal = steal;
    }

    public void setSoul(boolean soul) {
        this.soul = soul;
    }

    public boolean isSoul() {
        return soul;
    }

    public void setNewSpawn(boolean newSpawn) {
        this.newSpawn = newSpawn;
    }

    public void changeHP(long hp) {
        this.maxHP = hp;
        this.hp = hp;
    }

    /**
     * Spawns a monster on the map.
     *
     * @param mobId The ID of the monster.
     * @param hp    The HP of the monster.
     */
    public void changeBaseHp(int mobId, int x, int y, long hp) {
        MapleMonster monster = MapleLifeFactory.getMonster(mobId);
        if (monster != null) {
            monster.changeHP(hp);
        }
    }

    public List<Integer> getEliteMobActive() {
        return eliteMobActive;
    }

    public void setForcedMobStat(int level) {
        double rate = Math.max(level * 1.0 / stats.getLevel(), 1);
        this.forcedMobStat = new ForcedMobStat(stats, level, rate);
        if (getMobMaxHp() == stats.getHp()) {
            changeHP(Math.round((!stats.isBoss() ? GameConstants.getMonsterHP(level) : (stats.getHp() * rate))));
        }
    }

    public void setForcedMobStat(int level, double rate) {
        this.forcedMobStat = new ForcedMobStat(stats, level, rate);
        if (getMobMaxHp() == stats.getHp()) {
            changeHP(Math.round((!stats.isBoss() ? GameConstants.getMonsterHP(level) : (stats.getHp() * rate))));
        }
    }

    public void setForcedMobStat(MapleMonsterStats stats) {
        this.forcedMobStat = new ForcedMobStat(stats, stats.getLevel(), 1);
        if (getMobMaxHp() == stats.getHp()) {
            changeHP(stats.getHp());
        }
    }

    public void setChangeHP(long l) {
        changeHP(l);
    }

    public void changeBaseHp(long l) {
        changeHP(l);
    }


    public void setReduceDamageR(double reduceDamageR) {
        this.reduceDamageR = reduceDamageR;
    }

    public double getHpLimitPercent() {
        return this.hpLimitPercent;
    }

    public void setHpLimitPercent(final double aEe) {
        this.hpLimitPercent = aEe;
    }

    public int getLastKill() {
        return lastKill;
    }

    public void setLastKill(int lastKill) {
        this.lastKill = lastKill;
    }

    public void setAnimation(byte animation) {
        this.animation = animation;
    }

    public byte getAnimation() {
        return animation;
    }

    public void setSpongeMob(boolean spongeMob) {
        this.spongeMob = spongeMob;
    }

    public boolean isSpongeMob() {
        return spongeMob;
    }

    public void setMobCtrlSN(int mobCtrlSN) {
        this.mobCtrlSN = mobCtrlSN;
    }

    public int getMobCtrlSN() {
        return mobCtrlSN;
    }

    public long getTransTime() {
        return transTime;
    }

    public void setTransTime(long transTime) {
        this.transTime = transTime;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public int getScale() {
        return scale;
    }

    public void setShowMobId(int id) {
        this.showMobId = id;
    }

    public int getShowMobId() {
        return showMobId;
    }

    public boolean isShouldDropAssassinsMark() {
        return shouldDropAssassinsMark;
    }

    public void setShouldDropAssassinsMark(boolean b) {
        shouldDropAssassinsMark = b;
    }

    public void setUnk(int b) {
        this.bUnk = b;
    }

    public int getUnk() {
        return bUnk;
    }

    public void setUnk1(int b) {
        this.bUnk1 = b;
    }

    public int getUnk1() {
        return bUnk1;
    }

    public Pair<Long, List<LifeMovementFragment>> getLastRes() {
        return lastres;
    }

    public void setLastRes(Pair<Long, List<LifeMovementFragment>> lastres) {
        this.lastres = lastres;
    }

    public void move(List<LifeMovementFragment> lastres) {
        this.lastres = new Pair(System.currentTimeMillis(), lastres);
        MovementParse.updatePosition(lastres, this, 0);
        getMap().objectMove(0, this, MobPacket.moveMonster(getObjectId(), false, -1, 0, 0, (short) 0, Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap(), 0, 0, getPosition(), new Point(0, 0), lastres));
    }

    public void move(Point pos) {
        MovementNormal alm = new MovementNormal(0, 500, 4, (byte) 0);
        alm.setPosition(new Point(pos));
        alm.setFH((short) getMap().getFootholds().findBelow(alm.getPosition()).getId());
        alm.setPixelsPerSecond(new Point(0, 0));
        alm.setOffset(new Point(0, 0));
        move(Collections.singletonList(alm));
    }

    public List<Integer> getWillHplist() {
        return this.willHplist;
    }

    public void setWillHplist(List<Integer> willHplist) {
        this.willHplist = willHplist;
    }


    /* update */
    public byte getPhase() {
        return phase;
    }

    public void setPhase(byte phase) {
        this.phase = phase;
    }

    public long setLastSpecialAttackTime(long lastSpecialAttackTime) {
        this.lastSpecialAttackTime = lastSpecialAttackTime;
        return lastSpecialAttackTime;
    }

    public ScheduledFuture<?> getSchedule() {
        return schedule;
    }

    public void setSchedule(ScheduledFuture<?> schedule) {
        this.schedule = schedule;
    }

    public long getLastStoneTime() {
        return lastStoneTime;
    }

    public void setLastStoneTime(long lastStoneTime) {
        this.lastStoneTime = lastStoneTime;
    }

    public long getLastSeedCountedTime() {
        return lastSeedCountedTime;
    }

    public void setLastSeedCountedTime(long lastSeedCountedTime) {
        this.lastSeedCountedTime = lastSeedCountedTime;
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int id) {
        owner = id;
    }

    public long getLastSpecialAttackTime() {
        return lastSpecialAttackTime;
    }

    public boolean isUseSpecialSkill() {
        return useSpecialSkill;
    }

    public void setUseSpecialSkill(boolean useSpecialSkill) {
        this.useSpecialSkill = useSpecialSkill;
    }

    public int getNextSkill() {
        return nextSkill;
    }

    public void setNextSkill(int nextSkill) {
        this.nextSkill = nextSkill;
    }

    public int getNextSkillLvl() {
        return nextSkillLvl;
    }

    public void setNextSkillLvl(int nextSkillLvl) {
        this.nextSkillLvl = nextSkillLvl;
    }

    public long getCustomValue0(int skillid) {
        if (this.customInfo.containsKey(Integer.valueOf(skillid))) {
            return this.customInfo.get(Integer.valueOf(skillid)).getValue();
        }
        return 0L;
    }

    public Long getCustomValue(int skillid) {
        if (this.customInfo.containsKey(Integer.valueOf(skillid))) {
            return Long.valueOf(this.customInfo.get(Integer.valueOf(skillid)).getValue());
        }
        return null;
    }

    public void removeCustomInfo(int skillid) {
        this.customInfo.remove(Integer.valueOf(skillid));
    }


    public void setCustomInfo(int skillid, int value, int time) {
        if (getCustomValue(skillid) != null) {
            removeCustomInfo(skillid);
        }
        this.customInfo.put(Integer.valueOf(skillid), new SkillCustomInfo(value, time));
    }


    private final boolean extreme = false;

    public double bonusHp() {
        int level = this.stats.getLevel();
        double bonus = 1.0D;
        if (level >= 200 && level <= 210) {
            bonus = 1.5D;
        } else if (level >= 211 && level <= 220) {
            bonus = 2.0D;
        } else if (level >= 221 && level <= 230) {
            bonus = 2.5D;
        } else if (level >= 231 && level <= 240) {
            bonus = 3.0D;
        } else if (level >= 241) {
            bonus = 3.5D;
        }
        if (this.stats.getId() >= 9833070 && this.stats.getId() <= 9833099) {
            bonus = 1.0D;
        }
        if (this.stats.isBoss()) {
            switch (this.stats.getId()) {
                case 8644650:
                case 8644655:
                case 8645009:
                    bonus *= 15.0D;
                    break;
                case 8880405:
                case 8880408:
                case 8880409:
                case 8880410:
                    bonus *= 10.0D;
                    break;
                default:
                    bonus *= 2.0D;
                    break;
            }
        }
        if (this.extreme) {
            switch (this.stats.getId()) {
                case 8880100:
                case 8880101:
                case 8950000:
                case 8950001:
                case 8950002:
                    bonus *= 6.0D;
                    break;
                case 8880141:
                case 8880151:
                case 8880153:
                case 8880300:
                case 8880301:
                case 8880302:
                case 8880303:
                case 8880304:
                    bonus *= 4.5D;
                    break;
            }
        }
        return 1.0D;
    }

    private boolean willSpecialPattern = false;

    public boolean isWillSpecialPattern() {
        return this.willSpecialPattern;
    }

    public void setWillSpecialPattern(boolean willSpecialPattern) {
        this.willSpecialPattern = willSpecialPattern;
    }

    private long eliteHp = 0L;

    public long getEliteHp() {
        return this.eliteHp;
    }

    public void setEliteHp(long eliteHp) {
        this.eliteHp = eliteHp;
    }

    private int SerenTimetype;

    private int SerenNoonTotalTime;

    private int SerenSunSetTotalTime;

    private int SerenMidNightSetTotalTime;

    private int SerenDawnSetTotalTime;

    private int SerenNoonNowTime;

    private int SerenSunSetNowTime;

    private int SerenMidNightSetNowTime;

    private int SerenDawnSetNowTime;

    public int getSerenNoonTotalTime() {
        return this.SerenNoonTotalTime;
    }

    public void setSerenNoonTotalTime(int SerenNoonTotalTime) {
        this.SerenNoonTotalTime = SerenNoonTotalTime;
    }

    public int getSerenSunSetTotalTime() {
        return this.SerenSunSetTotalTime;
    }

    public void setSerenSunSetTotalTime(int SerenSunSetTotalTime) {
        this.SerenSunSetTotalTime = SerenSunSetTotalTime;
    }

    public int getSerenMidNightSetTotalTime() {
        return this.SerenMidNightSetTotalTime;
    }

    public void setSerenMidNightSetTotalTime(int SerenMidNightSetTotalTime) {
        this.SerenMidNightSetTotalTime = SerenMidNightSetTotalTime;
    }

    public int getSerenDawnSetTotalTime() {
        return this.SerenDawnSetTotalTime;
    }

    public void setSerenDawnSetTotalTime(int SerenDawnSetTotalTime) {
        this.SerenDawnSetTotalTime = SerenDawnSetTotalTime;
    }

    public int getSerenNoonNowTime() {
        return this.SerenNoonNowTime;
    }

    public void setSerenNoonNowTime(int SerenNoonNowTime) {
        this.SerenNoonNowTime = SerenNoonNowTime;
    }

    public int getSerenSunSetNowTime() {
        return this.SerenSunSetNowTime;
    }

    public void setSerenSunSetNowTime(int SerenSunSetNowTime) {
        this.SerenSunSetNowTime = SerenSunSetNowTime;
    }

    public int getSerenMidNightSetNowTime() {
        return this.SerenMidNightSetNowTime;
    }

    public void setSerenMidNightSetNowTime(int SerenMidNightSetNowTime) {
        this.SerenMidNightSetNowTime = SerenMidNightSetNowTime;
    }

    public int getSerenDawnSetNowTime() {
        return this.SerenDawnSetNowTime;
    }

    public void setSerenDawnSetNowTime(int SerenDawnSetNowTime) {
        this.SerenDawnSetNowTime = SerenDawnSetNowTime;
    }

    public int getSerenTimetype() {
        return this.SerenTimetype;
    }

    public void setSerenTimetype(int SerenTimetype) {
        this.SerenTimetype = SerenTimetype;
    }

    public void ResetSerenTime(boolean show) {
        this.SerenTimetype = 1;
        this.SerenNoonNowTime = 110;
        this.SerenNoonTotalTime = 110;
        this.SerenSunSetNowTime = 110;
        this.SerenSunSetTotalTime = 110;
        this.SerenMidNightSetNowTime = 30;
        this.SerenMidNightSetTotalTime = 30;
        this.SerenDawnSetNowTime = 110;
        this.SerenDawnSetTotalTime = 110;
        if (show) {
            getMap().broadcastMessage(Seren.SerenTimer(0, 360000, this.SerenNoonTotalTime, this.SerenSunSetTotalTime, this.SerenMidNightSetTotalTime, this.SerenDawnSetTotalTime));
        }
    }

    public void AddSerenTotalTimeHandler(int type, int add, int turn) {
        getMap().broadcastMessage(Seren.SerenTimer(1, this.SerenNoonTotalTime, this.SerenSunSetTotalTime, this.SerenMidNightSetTotalTime, this.SerenDawnSetTotalTime, turn));
    }

    public void AddSerenTimeHandler(int type, int add) {
        int nowtime = 0;
        switch (type) {
            case 1:
                this.SerenNoonNowTime += add;
                break;
            case 2:
                this.SerenSunSetNowTime += add;
                for (MapleCharacter chr : getMap().getAllChracater()) {
                    if (chr.isAlive() && chr.getBuffedValue(SecondaryStat.NotDamaged) == null && chr.getBuffedValue(SecondaryStat.NotDamaged) == null) {
                        int minushp = (int) (-chr.getStat().getCurrentMaxHp() / 100L);
                        chr.addHP(minushp);
                        chr.getClient().getSession().writeAndFlush(CField.showEffect(chr, 0, minushp, 36, 0, 0, (byte) 0, true, null, null, null, null));
                    }
                }
                break;
            case 3:
                this.SerenMidNightSetNowTime += add;
                break;
            case 4:
                this.SerenDawnSetNowTime += add;
                break;
        }
        nowtime = (type == 4) ? this.SerenDawnSetNowTime : ((type == 3) ? this.SerenMidNightSetNowTime : ((type == 2) ? this.SerenSunSetNowTime : this.SerenNoonNowTime));
        MapleMonster seren = null;
        int[] serens = {8880603, 8880607, 8880609, 8880612};
        for (int ids : serens) {
            seren = getMap().getMobObjectByID(ids);
            if (seren != null) {
                break;
            }
        }
        if (nowtime == 3) {
            for (MapleMonster mob : getMap().getAllMonster()) {
                if (mob.getId() == seren.getId() + 1) {
                    //getMap().broadcastMessage(DemianPacket.ChangePhaseDemian(mob, 79));
                    getMap().killMonsterType(mob, 2);
                }
            }
        }
        if (nowtime <= 0) {
            if (seren != null) {
                Point pos = seren.getPosition();
                getMap().broadcastMessage(Seren.SerenTimer(2, 1));
                setCustomInfo(8880603, 1, 0);
                getMap().broadcastMessage(Seren.SerenChangePhase("Mob/" + seren.getId() + ".img/skill3", 0, seren));
                for (MapleMonster mob : getMap().getAllMonster()) {
                    if (mob.getId() == seren.getId() || mob.getId() == 8880605 || mob.getId() == 8880606 || mob.getId() == 8880611) {
                        //getMap().broadcastMessage(DemianPacket.ChangePhaseDemian(mob, 79));
                        getMap().killMonsterType(mob, 2);
                    }
                }
                this.SerenTimetype++;
                if (this.SerenTimetype > 4) {
                    this.SerenTimetype = 1;
                }
                switch (this.SerenTimetype) {
                    case 1:
                        addHp(this.shield, false);
                        this.shield = -1L;
                        this.shieldmax = -1L;
                        getMap().broadcastMessage(FieldPacket.fieldEffect(FieldEffect.mobHPTagFieldEffect(this)));
                        getMap().broadcastMessage(MobPacket.mobBarrier(this));
                        this.SerenNoonNowTime = this.SerenNoonTotalTime;
                        break;
                    case 2:
                        this.SerenSunSetNowTime = this.SerenSunSetTotalTime;
                        break;
                    case 3:
                        this.SerenMidNightSetNowTime = this.SerenMidNightSetTotalTime;
                        break;
                    case 4:
                        this.SerenDawnSetNowTime = this.SerenDawnSetTotalTime;
                        break;
                }
                MapTimer.getInstance().schedule(() ->
                {
                    getMap().broadcastMessage(CField.ClearObstacles());
                    FieldSkillFactory.getInstance();
                    getMap().broadcastMessage(CField.useFieldSkill(FieldSkillFactory.getFieldSkill(100024, 1)));
                }, 500L);
                MapTimer.getInstance().schedule(() ->
                {
                    int nextid = (type == 4) ? 8880607 : ((type == 3) ? 8880603 : ((type == 2) ? 8880612 : 8880609));
                    getMap().spawnMonsterOnGroundBelow(Objects.requireNonNull(MapleLifeFactory.getMonster(nextid)), pos);
                    getMap().spawnMonsterOnGroundBelow(Objects.requireNonNull(MapleLifeFactory.getMonster(nextid + 1)), new Point(-49, 305));
                    if (nextid == 8880603) {
                        getMap().spawnMonsterOnGroundBelow(Objects.requireNonNull(MapleLifeFactory.getMonster(8880605)), pos);
                        MapleMonster totalseren = getMap().getMobObjectByID(8880602);
                        if (totalseren != null) {
                            totalseren.gainShield(totalseren.getStats().getHp() * 15L / 100L, !(totalseren.getShield() > 0L), 0);
                        }
                    }
                    getMap().broadcastMessage(Seren.SerenTimer(2, 0));
                    setCustomInfo(8880603, 0, 0);
                    getMap().broadcastMessage(Seren.SerenChangeBackground(this.SerenTimetype));
                }, 3560L);
            }
        }
    }

    public final void addHp(long hp, boolean brodcast) {
        this.hp = getHp() + hp;
        if (this.hp > getStats().getHp()) {
            this.hp = getStats().getHp();
        }
        if (brodcast) {
            getMap().broadcastMessage(FieldPacket.fieldEffect(FieldEffect.mobHPTagFieldEffect(this)));
        }
        if (this.hp <= 0L) {
            this.map.killMonster(this, this.controller.get(), true, false, (byte) 1, 0);
        }
    }

    public void gainShield(long energy, boolean first, int delayremove) {
        this.shield += energy;
        if (first) {
            this.shield = energy;
            this.shieldmax = energy;
            if (delayremove > 0) {
                EtcTimer.getInstance().schedule(() ->
                {
                    this.shield = 0L;
                    this.shieldmax = 0L;
                    getMap().broadcastMessage(MobPacket.mobBarrier(this));
                }, (delayremove * 1000L));
            }
        }
        getMap().broadcastMessage(MobPacket.mobBarrier(this));
    }

    public void setShield(long shield) {
        this.shield = shield;
    }

    public void addSkillCustomInfo(int skillid, long value) {
        this.customInfo.put(skillid, new SkillCustomInfo(getCustomValue0(skillid) + value, 0L));
    }

    public void setStigmaType(int rand) {
    }

    public void DemainChangePhase(MapleCharacter from) {
        if (!this.demianChangePhase) {
            this.map.showWeatherEffectNotice("戴米安已經完全陷入黑暗.", 216, 30000000);
            this.map.broadcastMessage(MobPacket.ChangePhaseDemian(this, 79));
            this.demianChangePhase = true;
            if (from.getEventInstance() != null) {
                from.getEventInstance().getHooks().mobDied(this, from);
//                from.getEventInstance().monsterKilled(from, this);
            }
            MapTimer.getInstance().schedule(() -> this.map.killMonsterType(this, 0), 6000L);
        }
    }

    public boolean isDemianChangePhase() {
        return demianChangePhase;
    }

    public void setDemianChangePhase(boolean demianChangePhase) {
        this.demianChangePhase = demianChangePhase;
    }

    //<editor-fold defaultstate="collapsed" desc="Inner Classes">
    public interface AttackerEntry {

        List<Integer> getAttackers();

        void addDamage(MapleCharacter from, long damage);

        long getDamage();

        boolean contains(MapleCharacter chr);

        void killedMob(World world, MapleMonster monster, long baseExp, int lastSkill);
    }

    private static final class OnePartyAttacker {

        public int partyID;
        public long damage;

        public OnePartyAttacker(int partyID, long damage) {
            this.partyID = partyID;
            this.damage = damage;
        }
    }

    private static final class SingleAttackerEntry implements AttackerEntry {

        private final int chrid;
        private long damage = 0;

        public SingleAttackerEntry(MapleCharacter from) {
            this.chrid = from.getId();
        }

        @Override
        public void addDamage(MapleCharacter from, long damage) {
            if (chrid == from.getId()) {
                this.damage += damage;
            }
        }

        @Override
        public List<Integer> getAttackers() {
            return Collections.singletonList(this.chrid);
        }

        @Override
        public boolean contains(MapleCharacter chr) {
            if (chr == null) {
                return false;
            }
            return chrid == chr.getId();
        }

        @Override
        public long getDamage() {
            return damage;
        }

        @Override
        public void killedMob(World world, MapleMonster monster, long baseExp, int lastSkill) {
            MapleCharacter chr = monster.getMap().getPlayerObject(chrid);
            if (chr != null && chr.isAlive()) {
                monster.giveExpToCharacter(chr, baseExp, 1, 0, 0, chrid == monster.getLastKill());
            }
        }

        @Override
        public int hashCode() {
            return chrid;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || obj != null && getClass() == obj.getClass() && chrid == ((SingleAttackerEntry) obj).chrid;
        }
    }

    private static class PartyAttackerEntry implements AttackerEntry {

        private final Map<Integer, OnePartyAttacker> attackers = new HashMap<>(6);
        private final int partyid;
        private long totDamage = 0;

        public PartyAttackerEntry(int partyid) {
            this.partyid = partyid;
        }

        @Override
        public List<Integer> getAttackers() {
            return new ArrayList<>(attackers.keySet());
        }

        @Override
        public boolean contains(MapleCharacter chr) {
            if (chr == null) {
                return false;
            }
            return attackers.containsKey(chr.getId());
        }

        @Override
        public long getDamage() {
            return totDamage;
        }

        @Override
        public void addDamage(MapleCharacter from, long damage) {
            OnePartyAttacker oldPartyAttacker = attackers.get(from.getId());
            if (oldPartyAttacker != null) {
                oldPartyAttacker.damage += damage;
                oldPartyAttacker.partyID = from.getParty().getId();
            } else {
                OnePartyAttacker onePartyAttacker = new OnePartyAttacker(from.getParty().getId(), damage);
                attackers.put(from.getId(), onePartyAttacker);
            }
            totDamage += damage;
        }

        @Override
        public void killedMob(World world, MapleMonster monster, long baseExp, int lastSkill) {
            MapleCharacter pchr;
            long iexp;
            Party party = world == null ? null : world.getPartybyId(this.partyid);
            double addedPartyLevel = 0, levelMod;
            int recallRingId = 0;
            List<MapleCharacter> memberApplicable = new ArrayList<>();
            if (party != null) {
                for (PartyMember pm : party.getMembers()) {
                    if (pm == null || !pm.isOnline()) {
                        continue;
                    }
                    pchr = pm.getChr();
                    if (pchr != null) {
                        if (pm.getChannel() != monster.getMap().getChannel() || pm.getFieldID() != monster.getMap().getId()) {
                            continue;
                        }
                        if (recallRingId == 0 && pchr.getStat().getRecallRingId() > 0) {
                            recallRingId = pchr.getStat().getRecallRingId();
                        }
                        int lvGap = Math.abs(monster.getMobLevel() - pchr.getLevel());
                        if (pchr.isAlive() && (pchr.getId() == monster.getLastKill() || lvGap < 40 && pchr.getCheatTracker().isAttacking())) {
                            memberApplicable.add(pchr);
                        }
                    }
                }
            }
            List<MapleCharacter> expApplicable = new ArrayList<>();
            Map<MapleCharacter, Double> damageDealtMap = new HashMap<>();
            for (MapleCharacter playerObject : memberApplicable) {
                boolean isChrLvGap = false;
                for (PartyMember pm : party.getMembers()) {
                    if (!pm.isOnline() || playerObject.getId() == pm.getCharID()) {
                        continue;
                    }
                    if (Math.abs(pm.getLevel() - playerObject.getLevel()) <= 5) {
                        isChrLvGap = true;
                        break;
                    }
                }
                long damage = 0;
                for (final Entry<Integer, OnePartyAttacker> entry : this.attackers.entrySet()) {
                    if (entry.getKey() == playerObject.getId()) {
                        damage = entry.getValue().damage;
                        break;
                    }
                }
                if (playerObject.getMap() != monster.getMap()) {
                    continue;
                }
                if (damage <= 0 && Math.abs(monster.getMobLevel() - playerObject.getLevel()) > 5 && !isChrLvGap && playerObject.getId() != monster.getLastKill()) {
                    continue;
                }
                expApplicable.add(playerObject);
                addedPartyLevel += playerObject.getLevel();
                damageDealtMap.put(playerObject, (double) damage / this.totDamage);
            }
            if (expApplicable.isEmpty()) {
                return;
            }
            Map<MapleCharacter, ExpMap> expMap = new HashMap<>();
            for (final MapleCharacter expReceiver : expApplicable) {
                double damageDealt = (damageDealtMap.get(expReceiver) != null) ? damageDealtMap.get(expReceiver) : 0.0;
                levelMod = 0.8 * expReceiver.getLevel() / addedPartyLevel;
                iexp = Math.round(baseExp * (0.2 * damageDealt + levelMod));
                expMap.put(expReceiver, new ExpMap(iexp, (byte) expApplicable.size(), 0, 0, expApplicable.size() > 1 ? recallRingId : 0));
            }
            ExpMap expmap;
            for (Entry<MapleCharacter, ExpMap> expReceiver : expMap.entrySet()) {
                expmap = expReceiver.getValue();
                monster.giveExpToCharacter(expReceiver.getKey(), expmap.exp, expmap.ptysize, expmap.RecallRingId, lastSkill, expReceiver.getKey().getId() == monster.getLastKill());
            }
        }

        @Override
        public int hashCode() {
            return 31 + partyid;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || obj != null && getClass() == obj.getClass() && partyid == ((PartyAttackerEntry) obj).partyid;
        }


        private static final class ExpMap {

            private final long exp;
            private final byte ptysize;
            private final int Class_Bonus_EXP;
            private final int Premium_Bonus_EXP;
            private final int RecallRingId;

            public ExpMap(final long exp, final byte ptysize, final int Class_Bonus_EXP, final int Premium_Bonus_EXP, final int RecallRingId) {
                this.exp = exp;
                this.ptysize = ptysize;
                this.Class_Bonus_EXP = Class_Bonus_EXP;
                this.Premium_Bonus_EXP = Premium_Bonus_EXP;
                this.RecallRingId = RecallRingId;
            }
        }
        //</editor-fold>
    }
}
