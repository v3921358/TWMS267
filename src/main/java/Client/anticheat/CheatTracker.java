package Client.anticheat;

import Client.MapleAntiMacro;
import Client.MapleCharacter;
import Client.MapleCharacterUtil;
import Client.skills.SkillFactory;
import Config.constants.SkillConstants;
import Config.constants.enums.UserChatMessageType;
import Config.constants.skills.卡蒂娜;
import Net.server.AutobanManager;
import Net.server.maps.MapleRuneStone;
import Packet.MaplePacketCreator;
import Packet.PacketHelper;
import Server.world.WorldBroadcastService;
import Server.world.WorldFindService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Randomizer;
import tools.StringUtil;

import java.awt.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CheatTracker {

    private static final Logger log = LoggerFactory.getLogger(CheatTracker.class);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock rL = lock.readLock();
    private final Lock wL = lock.writeLock();
    private final Map<CheatingOffense, CheatingOffenseEntry> offenses = new EnumMap<>(CheatingOffense.class);
    private Integer ownerid;
    private volatile long lastAttackTime = 0;
    private volatile int lastAttackTickCount = 0;
    private volatile byte tickResetCount = 0;
    private volatile long Server_ClientAtkTickDiff = 0;
    private volatile long lastDamage = 0;
    private volatile long takingDamageSince;
    private volatile int numSequentialDamage = 0;
    private volatile long lastDamageTakenTime = 0;
    private volatile byte numZeroDamageTaken = 0;
    private volatile int numSameDamage = 0;
    private volatile Point lastMonsterMove;
    private volatile int monsterMoveCount;
    private volatile int attacksWithoutHit = 0;
    private volatile byte dropsPerSecond = 0;
    private volatile long lastDropTime = 0;
    private volatile byte msgsPerSecond = 0;
    private volatile long lastMsgTime = 0;
    private ScheduledFuture<?> invalidationTask;
    private volatile int gm_message = 0;
    private volatile int lastTickCount = 0, tickSame = 0, inMapTimeCount = 0, lastPickupkCount = 0;
    private volatile long lastSmegaTime = 0, lastBBSTime = 0, lastASmegaTime = 0, lastMZDTime = 0, lastCraftTime = 0, lastSaveTime = 0, lastLieDetectorTime = 0, lastPickupkTime = 0, lastlogonTime;
    private volatile int numSequentialFamiliarAttack = 0;
    private volatile long familiarSummonTime = 0;
    private volatile int lastAttackSkill;

    private volatile long next絕殺刃;
    private volatile long next死神契約;
    private volatile long nextBonusAttack;
    private volatile long nextShadowDodge;
    private volatile long nextAegisSystem;
    private volatile int lastChannelTick;
    private volatile long lastEnterChannel;
    private volatile long mapChangeTime;
    public volatile int inMapAttackMinutes;
    private volatile long nextVampiricTouch;
    private volatile int shadowBat;
    private volatile long next追縱火箭;
    private volatile long nextElementalFocus;
    private volatile long nextPantherAttack;
    private volatile long nextHealHPMP = 0;
    private volatile long nextHealHPMPS = 0;
    private volatile long lastRecoveryPowerTime;
    private final AtomicInteger monsterCombo = new AtomicInteger(1);
    private final Map<Integer, Long> bgn = new HashMap<>();
    private final AtomicInteger multiKill = new AtomicInteger(0);
    private final AtomicInteger lastKillMobOid = new AtomicInteger(0);
    private volatile long next幻影分身符;
    private volatile long next蝶夢;

    public CheatTracker(Integer ownerid) {
        start(ownerid);
    }

    private MapleCharacter getPlayer() {
        return WorldFindService.getInstance().findCharacterById(ownerid);
    }

    /**
     * 檢測玩家攻擊
     */
    public void checkAttack(int skillId, int tickcount) {
        wL.lock();
        try {
            updateTick(tickcount);

            int AtkDelay = SkillConstants.getAttackDelay(skillId, SkillFactory.getSkill(skillId));
            lastAttackTime = System.currentTimeMillis();
            if (skillId != 卡蒂娜.武器變換Ⅰ_攻擊 && skillId != 卡蒂娜.武器變換Ⅱ_攻擊 && skillId != 卡蒂娜.武器變換Ⅲ_攻擊) {
                lastAttackSkill = skillId;
            }
            if (lastAttackTime - mapChangeTime > 0) {
                inMapAttackMinutes = (int) ((lastAttackTime - mapChangeTime) / 60000);
                MapleCharacter player = getPlayer();
                if (player != null) {
                    // 調試訊息
                    System.out.println("開始檢測 - 是否檢測: " + !player.isInTownMap() + " 是否有怪物: " + player.getMap().getMobsSize() + " 是否在活動地圖: " + (player.getEventInstance() != null));
                    if (inMapAttackMinutes >= 60) {
                        WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.spouseMessage(UserChatMessageType.淺紫, "[GM訊息] " + player.getName() + " ID: " + player.getId() + " (等級 " + player.getLevel() + ") 在地圖: " + player.getMapId() + " 打怪時間超過1小時，該玩家可能是在掛機打怪。"));
                    }
                    inMapTimeCount++;
                    MapleRuneStone rune;
                    if (player.getRuneUseCooldown() <= 0 && player.getMap() != null && (rune = player.getMap().getCurseRune()) != null && rune.getCurseStage() > 0) {
                        if (inMapAttackMinutes >= 1 && inMapTimeCount >= Randomizer.nextInt(10) && rune.getCurseRate() > Randomizer.nextInt(1000)) {
                            inMapTimeCount = 0;
                            if (MapleAntiMacro.startAnti(null, player, MapleAntiMacro.SYSTEM_ANTI)) {
                                log.info("[作弊] " + player.getName() + " (等級 " + player.getLevel() + ") 在詛咒" + rune.getCurseStage() + "階段的地圖: " + player.getMapId() + " 打怪時間超過 1 分鐘，系統啟動測謊機系統。");
                                WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.spouseMessage(UserChatMessageType.淺紫, "[GM訊息] " + player.getName() + " ID: " + player.getId() + " (等級 " + player.getLevel() + ") 在詛咒" + rune.getCurseStage() + "階段地圖: " + player.getMapId() + " 打怪時間超過 1 分鐘，系統啟動測謊機系統。"));
                            }
                        }
                    }
                }
            }
            tickResetCount++; // 如果沒有這個，差異將始終為100
            if (tickResetCount >= (AtkDelay <= 200 ? 1 : 4)) {
                tickResetCount = 0;
            }

            long STime_TC = lastAttackTime - tickcount; // hack = - more
            if (Server_ClientAtkTickDiff - STime_TC > 1000) { // 250是延遲
                MapleCharacter player = getPlayer();
                if (player != null && player.isAdmin()) {
                    player.dropMessage(-5, "攻擊速度異常2 技能: " + skillId + " 當前: " + (Server_ClientAtkTickDiff - STime_TC));
                }
                registerOffense(CheatingOffense.FASTATTACK2, "攻擊速度異常。");
            }
            // 如果是加速，客戶端的tickcount值將以更快的速度運行
            // 對於延遲，不是問題，因為時間是同步運行的，客戶端將發送較舊時間的值
            // System.out.println("Delay [" + skillId + "] = " + (tickcount - lastAttackTickCount) + ", " + (Server_ClientAtkTickDiff - STime_TC));
            updateTick(tickcount);
            multiKill.set(0);
        } finally {
            wL.unlock();
        }
    }

    /*
     * 重置角色在地圖的時間檢測次數
     */
    public void resetInMapTime() {
        inMapTimeCount = 0;
        mapChangeTime = PacketHelper.getTime(System.currentTimeMillis());
    }

    /**
     * 檢測玩家在PVP地圖中的攻擊
     */
    public void checkPVPAttack(int skillId) {
        int AtkDelay = SkillConstants.getAttackDelay(skillId, skillId == 0 ? null : SkillFactory.getSkill(skillId));
        long STime_TC = System.currentTimeMillis() - lastAttackTime; // hack = - more
        if (STime_TC < AtkDelay) { // 250是延遲
            registerOffense(CheatingOffense.FASTATTACK, "攻擊速度異常。");
        }
        lastAttackTime = System.currentTimeMillis();
    }

    public long getLastAttack() {
        return lastAttackTime;
    }

    /**
     * 檢測玩家受到傷害
     */
    public void checkTakeDamage(int damage) {
        numSequentialDamage++;
        lastDamageTakenTime = System.currentTimeMillis();

        if (lastDamageTakenTime - takingDamageSince / 500 < numSequentialDamage) {
            registerOffense(CheatingOffense.FAST_TAKE_DAMAGE, "掉血次數異常。");
        }
        if (lastDamageTakenTime - takingDamageSince > 4500) {
            takingDamageSince = lastDamageTakenTime;
            numSequentialDamage = 0;
        }
        /*
         * (非飛俠) 最小Miss率: 2% 最大Miss率: 80%
         * (飛俠) 最小Miss率: 5% 最大Miss率: 95%
         */
        if (damage == 0) {
            numZeroDamageTaken++;
            if (numZeroDamageTaken >= 50) { // 當次數達到50次就封掉玩家
                numZeroDamageTaken = 0;
                registerOffense(CheatingOffense.HIGH_AVOID, "迴避率過高。");
            }
        } else if (damage != -1) {
            numZeroDamageTaken = 0;
        }
    }

    /**
     * 重置檢測玩家受到傷害
     */
    public void resetTakeDamage() {
        numZeroDamageTaken = 0;
    }

    /**
     * 檢測攻擊傷害是否一樣
     */
    public void checkSameDamage(long dmg, double expected) {
        MapleCharacter player = getPlayer();
        if (dmg > 2000 && lastDamage == dmg && player != null && (player.getLevel() < 190 || dmg > expected * 2)) {
            numSameDamage++;
            if (numSameDamage > 5) {
                registerOffense(CheatingOffense.SAME_DAMAGE, numSameDamage + " 次，攻擊傷害 " + dmg + "，預期傷害 " + expected + " [等級: " + player.getLevel() + "，職業: " + player.getJob() + "]");
                numSameDamage = 0;
            }
        } else {
            lastDamage = dmg;
            numSameDamage = 0;
        }
    }

    /*
     * 檢測攻擊傷害過高
     */
    public void checkHighDamage(int eachd, double maxDamagePerHit, int mobId, int skillId) {
        MapleCharacter player = getPlayer();
        if (eachd > maxDamagePerHit && maxDamagePerHit > 2000 && player != null) {
            registerOffense(CheatingOffense.HIGH_DAMAGE, "[傷害: " + eachd + "，預計傷害: " + maxDamagePerHit + "，怪物ID: " + mobId + "] [職業: " + player.getJob() + "，等級: " + player.getLevel() + "，技能: " + skillId + "]");
            if (eachd > maxDamagePerHit * 2) {
                registerOffense(CheatingOffense.HIGH_DAMAGE_2, "[傷害: " + eachd + "，預計傷害: " + maxDamagePerHit + "，怪物ID: " + mobId + "] [職業: " + player.getJob() + "，等級: " + player.getLevel() + "，技能: " + skillId + "]");
            }
        }
    }

    /**
     * 檢測怪物移動
     */
    public void checkMoveMonster(Point pos) {
        if (pos.equals(lastMonsterMove)) {
            monsterMoveCount++;
            if (monsterMoveCount > 10) {
                registerOffense(CheatingOffense.MOVE_MONSTERS, "吸怪 座標: " + pos.x + ", " + pos.y);
                monsterMoveCount = 0;
            }
        } else {
            lastMonsterMove = pos;
            monsterMoveCount = 1;
        }
    }

    public void resetFamiliarAttack() {
        familiarSummonTime = System.currentTimeMillis();
        numSequentialFamiliarAttack = 0;
    }

    public boolean checkFamiliarAttack() {
        numSequentialFamiliarAttack++;
        if ((System.currentTimeMillis() - familiarSummonTime) / 1000 < numSequentialFamiliarAttack) {
            registerOffense(CheatingOffense.FAST_SUMMON_ATTACK, "召喚獸攻擊速度異常。");
            return false;
        }
        return true;
    }

    /**
     * 檢測拾取道具
     */
    public void checkPickup(int count, boolean pet) {
        if ((System.currentTimeMillis() - lastPickupkTime) < 1000) {
            lastPickupkCount++;
            MapleCharacter player = getPlayer();
            if (lastPickupkCount >= count && player != null && !player.isGm()) {
                log.info("[作弊] " + player.getName() + " (等級 " + player.getLevel() + ") " + (pet ? "寵物" : "角色") + "拾取次數過多: " + lastPickupkCount + "，伺服器斷開他的連接。");
                player.getClient().disconnect(true, false);
                if (player.getClient() != null && player.getClient().getSession().isActive()) {
                    player.getClient().getSession().close();
                }
            }
        } else {
            lastPickupkCount = 0;
        }
        lastPickupkTime = System.currentTimeMillis();
    }

    /**
     * 檢測丟棄道具
     */
    public void checkDrop() {
        checkDrop(false);
    }

    /**
     * 檢測丟棄道具
     */
    public void checkDrop(boolean dc) {
        if ((System.currentTimeMillis() - lastDropTime) < 1000) {
            dropsPerSecond++;
            MapleCharacter player = getPlayer();
            if (dropsPerSecond >= (dc ? 32 : 16) && player != null && !player.isGm()) {
                if (dc) {
                    player.getClient().disconnect(true, false);
                    if (player.getClient().getSession().isActive()) {
                        player.getClient().getSession().close();
                    }
                    log.info("[作弊] " + player.getName() + " (等級 " + player.getLevel() + ") 丟棄次數過多: " + dropsPerSecond + "，伺服器斷開他的連接。");
                } else {
                    player.getClient().setMonitored(true);
                }
            }
        } else {
            dropsPerSecond = 0;
        }
        lastDropTime = System.currentTimeMillis();
    }

    /**
     * 檢測是否能聊天
     */
    public void checkMsg() {
        if ((System.currentTimeMillis() - lastMsgTime) < 1000) {
            msgsPerSecond++;
            MapleCharacter player = getPlayer();
            if (msgsPerSecond > 10 && player != null && !player.isGm()) {
                player.getClient().disconnect(true, false);
                if (player.getClient().getSession().isActive()) {
                    player.getClient().getSession().close();
                }
                log.info("[作弊] " + player.getName() + " (等級 " + player.getLevel() + ") 發送訊息過多: " + msgsPerSecond + "，伺服器斷開他的連接。");
            }
        } else {
            msgsPerSecond = 0;
        }
        lastMsgTime = System.currentTimeMillis();
    }

    public int getAttacksWithoutHit() {
        return attacksWithoutHit;
    }

    public void setAttacksWithoutHit(boolean increase) {
        if (increase) {
            this.attacksWithoutHit++;
        } else {
            this.attacksWithoutHit = 0;
        }
    }

    public void registerOffense(CheatingOffense offense) {
        registerOffense(offense, null);
    }

    public void registerOffense(CheatingOffense offense, String param) {
        MapleCharacter chrhardref = getPlayer();
        if (chrhardref == null || !offense.isEnabled() || chrhardref.isGm()) {
            return;
        }
        CheatingOffenseEntry entry = null;
        rL.lock();
        try {
            entry = offenses.get(offense);
        } finally {
            rL.unlock();
        }
        if (entry != null && entry.isExpired()) {
            expireEntry(entry);
            entry = null;
            gm_message = 0;
        }
        if (entry == null) {
            entry = new CheatingOffenseEntry(offense, chrhardref.getId());
        }
        if (param != null) {
            entry.setParam(param);
        }
        entry.incrementCount();
        if (offense.shouldAutoban(entry.getCount())) {
            byte type = offense.getBanType();
            if (type == 1) {
                AutobanManager.getInstance().autoban(chrhardref.getClient(), StringUtil.makeEnumHumanReadable(offense.name()));
            } else if (type == 2) {
                chrhardref.getClient().disconnect(true, false);
                if (chrhardref.getClient().getSession().isActive()) {
                    chrhardref.getClient().getSession().close();
                }
                log.info("[作弊] " + chrhardref.getName() + " (等級:" + chrhardref.getLevel() + " 職業:" + chrhardref.getJob() + ") 伺服器斷開他的連接。原因: " + StringUtil.makeEnumHumanReadable(offense.name()) + (param == null ? "" : (" - " + param)));
            }
            gm_message = 0;
            return;
        }
        wL.lock();
        try {
            offenses.put(offense, entry);
        } finally {
            wL.unlock();
        }
        if (offense == CheatingOffense.SAME_DAMAGE) {
            gm_message++;
            if (gm_message % 100 == 0) {
                log.info("[作弊] " + MapleCharacterUtil.makeMapleReadable(chrhardref.getName()) + " ID: " + chrhardref.getId() + " (等級 " + chrhardref.getLevel() + ") 使用非法程式! " + StringUtil.makeEnumHumanReadable(offense.name()) + (param == null ? "" : (" - " + param)));
                WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.spouseMessage(UserChatMessageType.淺紫, "[GM訊息] " + MapleCharacterUtil.makeMapleReadable(chrhardref.getName()) + " ID: " + chrhardref.getId() + " (等級 " + chrhardref.getLevel() + ") 使用非法程式! " + StringUtil.makeEnumHumanReadable(offense.name()) + (param == null ? "" : (" - " + param))));
            }
            if (gm_message >= 20 && chrhardref.getLevel() < (offense == CheatingOffense.SAME_DAMAGE ? 180 : 190)) {
                Timestamp chrCreated = chrhardref.getChrCreated();
                long time = System.currentTimeMillis();
                if (chrCreated != null) {
                    time = chrCreated.getTime();
                }
                if (time + (15 * 24 * 60 * 60 * 1000) >= System.currentTimeMillis()) {
                    AutobanManager.getInstance().autoban(chrhardref.getClient(), StringUtil.makeEnumHumanReadable(offense.name()) + " 超過500次 " + (param == null ? "" : (" - " + param)));
                } else {
                    gm_message = 0;
                    WorldBroadcastService.getInstance().broadcastGMMessage(MaplePacketCreator.spouseMessage(UserChatMessageType.淺紫, "[GM訊息] " + MapleCharacterUtil.makeMapleReadable(chrhardref.getName()) + " ID: " + chrhardref.getId() + " (等級 " + chrhardref.getLevel() + ") 使用非法程式! " + StringUtil.makeEnumHumanReadable(offense.name()) + (param == null ? "" : (" - " + param))));
                    log.info("[GM訊息] " + MapleCharacterUtil.makeMapleReadable(chrhardref.getName()) + " ID: " + chrhardref.getId() + " (等級 " + chrhardref.getLevel() + ") 使用非法程式! " + StringUtil.makeEnumHumanReadable(offense.name()) + (param == null ? "" : (" - " + param)));
                }
            }
        }
        CheatingOffensePersister.getInstance().persistEntry(entry);
    }

    public void updateTick(int newTick) {
        wL.lock();
        try {
            if (newTick <= lastTickCount) {
                MapleCharacter player = getPlayer();
                if (tickSame >= 30 && player != null && !player.isGm()) {
                    player.getClient().disconnect(true, false);
                    if (player.getClient().getSession().isActive()) {
                        player.getClient().getSession().close();
                    }
                    log.info("[作弊] " + player.getName() + " (等級 " + player.getLevel() + ") 更新Tick次數過多: " + tickSame + "，伺服器斷開他的連接。");
                } else {
                    tickSame++;
                }
            } else {
                tickSame = 0;
            }
            lastTickCount = newTick;
        } finally {
            wL.unlock();
        }
    }

    /**
     * 檢測是否能使用商城道具喇叭
     */
    public boolean canSmega() {
        MapleCharacter player = getPlayer();
        if (lastSmegaTime > System.currentTimeMillis() && player != null && !player.isGm()) {
            return false;
        }
        lastSmegaTime = System.currentTimeMillis();
        return true;
    }

    /**
     * 檢測是否能使用情景喇叭
     */
    public boolean canAvatarSmega() {
        MapleCharacter player = getPlayer();
        if (lastASmegaTime > System.currentTimeMillis() && player != null && !player.isGm()) {
            return false;
        }
        lastASmegaTime = System.currentTimeMillis();
        return true;
    }

    /**
     * 檢測是否能使用BBS
     */
    public boolean canBBS() {
        MapleCharacter player = getPlayer();
        if (lastBBSTime + 60000 > System.currentTimeMillis() && player != null && !player.isGm()) {
            return false;
        }
        lastBBSTime = System.currentTimeMillis();
        return true;
    }

    /**
     * 檢測是否能使用謎之蛋
     */
    public boolean canMZD() {
        MapleCharacter player = getPlayer();
        if (lastMZDTime > System.currentTimeMillis() && player != null && !player.isGm()) {
            return false;
        }
        lastMZDTime = System.currentTimeMillis();
        return true;
    }

    /**
     * 檢測是否能製作道具
     */
    public boolean canCraftMake() {
        MapleCharacter player = getPlayer();
        if (lastCraftTime + 1000 > System.currentTimeMillis() && player != null) {
            return false;
        }
        lastCraftTime = System.currentTimeMillis();
        return true;
    }

    /**
     * 檢測是否能保存角色數據，只針對 PLAYER_UPDATE 這個封包，設置3分鐘保存，避免頻繁保存數據
     */
    public boolean canSaveDB() {
        if (lastSaveTime + 3 * 60 * 1000 > System.currentTimeMillis() && getPlayer() != null) {
            return false;
        }
        lastSaveTime = System.currentTimeMillis();
        return true;
    }

    public int getlastSaveTime() {
        if (lastSaveTime <= 0) {
            lastSaveTime = System.currentTimeMillis();
        }
        return (int) (((lastSaveTime + (3 * 60 * 1000)) - System.currentTimeMillis()) / 1000);
    }

    /**
     * 檢測是否能使用測謊機
     */
    public boolean canLieDetector() {
        if (lastLieDetectorTime + 5 * 60 * 1000 > System.currentTimeMillis() && getPlayer() != null) {
            return false;
        }
        lastLieDetectorTime = System.currentTimeMillis();
        return true;
    }

    /**
     * 檢測是否能進入商城
     */
    public long getLastlogonTime() {
        if (lastlogonTime <= 0 || getPlayer() == null) {
            lastlogonTime = System.currentTimeMillis();
        }
        return lastlogonTime;
    }

    public void expireEntry(CheatingOffenseEntry coe) {
        wL.lock();
        try {
            offenses.remove(coe.getOffense());
        } finally {
            wL.unlock();
        }
    }

    public int getPoints() {
        int ret = 0;
        CheatingOffenseEntry[] offenses_copy;
        rL.lock();
        try {
            offenses_copy = offenses.values().toArray(new CheatingOffenseEntry[offenses.size()]);
        } finally {
            rL.unlock();
        }
        for (CheatingOffenseEntry entry : offenses_copy) {
            if (entry.isExpired()) {
                expireEntry(entry);
            } else {
                ret += entry.getPoints();
            }
        }
        return ret;
    }

    public Map<CheatingOffense, CheatingOffenseEntry> getOffenses() {
        return Collections.unmodifiableMap(offenses);
    }

    public String getSummary() {
        StringBuilder ret = new StringBuilder();
        List<CheatingOffenseEntry> offenseList = new ArrayList<>();
        rL.lock();
        try {
            for (CheatingOffenseEntry entry : offenses.values()) {
                if (!entry.isExpired()) {
                    offenseList.add(entry);
                }
            }
        } finally {
            rL.unlock();
        }
        offenseList.sort((o1, o2) -> Integer.compare(o2.getPoints(), o1.getPoints()));
        int to = Math.min(offenseList.size(), 4);
        for (int x = 0; x < to; x++) {
            ret.append(StringUtil.makeEnumHumanReadable(offenseList.get(x).getOffense().name()));
            ret.append(": ");
            ret.append(offenseList.get(x).getCount());
            if (x != to - 1) {
                ret.append(" ");
            }
        }
        return ret.toString();
    }

    public void dispose() {
        if (invalidationTask != null) {
            invalidationTask.cancel(false);
        }
        invalidationTask = null;
    }

    public final void start(Integer ownerid) {
        this.ownerid = ownerid;
        takingDamageSince = System.currentTimeMillis();
    }

    public int getLastAttackSkill() {
        return lastAttackSkill;
    }

    public boolean canNext絕殺刃() {
        return this.next絕殺刃 > 0L && this.next絕殺刃 <= System.currentTimeMillis();
    }

    public final void setNext絕殺刃(final long me) {
        this.next絕殺刃 = me;
    }

    public boolean canNext死神契約() {
        if (this.next死神契約 <= System.currentTimeMillis()) {
            this.next死神契約 = System.currentTimeMillis() + 9000L;
            return true;
        }
        return false;
    }

    public boolean canNextBonusAttack(long time) {
        if (this.nextBonusAttack > System.currentTimeMillis()) {
            return false;
        }
        this.nextBonusAttack = System.currentTimeMillis() + time;
        return true;
    }

    public void setLastAttackTime() {
        this.lastAttackTime = System.currentTimeMillis();
    }

    public boolean canNextShadowDodge() {
        if (this.nextShadowDodge <= System.currentTimeMillis()) {
            this.nextShadowDodge = System.currentTimeMillis() + 5000L;
            return true;
        }
        return false;
    }

    public boolean canNextAegisSystem() {
        if (this.nextAegisSystem <= System.currentTimeMillis()) {
            this.nextAegisSystem = System.currentTimeMillis() + 1500L;
            return true;
        }
        return false;
    }

    public int getFinalAttackTime() {
        this.lastChannelTick += (int) (System.currentTimeMillis() - this.lastEnterChannel);
        this.lastEnterChannel = System.currentTimeMillis();
        return this.lastChannelTick;
    }

    public void setLastChannelTick(int lastChannelTick) {
        this.lastChannelTick = lastChannelTick;
    }

    public void setLastEnterChannel() {
        this.lastEnterChannel = System.currentTimeMillis();
    }

    public boolean canNextVampiricTouch() {
        if (this.nextVampiricTouch <= System.currentTimeMillis()) {
            this.nextVampiricTouch = System.currentTimeMillis() + 5000L;
            return true;
        }
        return false;
    }

    public void addShadowBat() {
        this.shadowBat++;
    }

    public boolean canSpawnShadowBat() {
        if (this.shadowBat >= 3) {
            this.shadowBat = 0;
            return true;
        }
        return false;
    }

    public boolean canNext追縱火箭() {
        if (this.next追縱火箭 <= System.currentTimeMillis()) {
            this.next追縱火箭 = System.currentTimeMillis() + 5000L;
            return true;
        }
        return false;
    }

    public final boolean canNextElementalFocus() {
        if (this.nextElementalFocus <= System.currentTimeMillis()) {
            this.nextElementalFocus = System.currentTimeMillis() + 700L;
            return true;
        }
        return false;
    }

    public boolean canNextPantherAttack() {
        if (this.nextPantherAttack <= System.currentTimeMillis()) {
            this.nextPantherAttack = System.currentTimeMillis() + 10000L;
            return true;
        }
        return false;
    }

    public boolean canNextPantherAttackS() {
        return this.nextPantherAttack <= System.currentTimeMillis();
    }

    public boolean canNextHealHPMP() {
        if (this.nextHealHPMP <= System.currentTimeMillis()) {
            this.nextHealHPMP = System.currentTimeMillis() + 4000L;
            return true;
        }
        return false;
    }

    public void setNextHealHPMPS(long time) {
        nextHealHPMPS = System.currentTimeMillis() + time;
    }

    public boolean canNextHealHPMPS() {
        return this.nextHealHPMPS <= System.currentTimeMillis();
    }

    public boolean canNextRecoverPower(boolean overload) {
        if (this.lastRecoveryPowerTime <= System.currentTimeMillis()) {
            this.lastRecoveryPowerTime = System.currentTimeMillis() + (overload ? 2000 : 4000);
            return true;
        }
        return false;
    }

    public int gainMonsterCombo() {
        if (System.currentTimeMillis() - this.lastAttackTime > 10000) {
            clearMonsterCombo();
        }
        return monsterCombo.incrementAndGet();
    }

    public int getMonsterCombo() {
        if (System.currentTimeMillis() - this.lastAttackTime > 10000) {
            monsterCombo.set(1);
        }
        return monsterCombo.get();
    }


    public void clearMonsterCombo() {
        monsterCombo.set(0);
    }

    public void gainMultiKill() {
        multiKill.incrementAndGet();
    }

    public boolean canNextAllRocket(int skillId, int ms) {
        final Long n3;
        if ((n3 = this.bgn.get(skillId)) != null && n3 > System.currentTimeMillis()) {
            return false;
        }
        this.bgn.put(skillId, System.currentTimeMillis() + ms);
        return true;
    }

    public int getMultiKill() {
        return this.multiKill.get();
    }

    public void setLastKillMobOid(int lastKillMobOid) {
        this.lastKillMobOid.set(lastKillMobOid);
    }

    public int getLastKillMobOid() {
        return lastKillMobOid.get();
    }

    public boolean canNext幻影分身符() {
        if (this.next幻影分身符 <= System.currentTimeMillis()) {
            this.next幻影分身符 = System.currentTimeMillis() + 1500;
            return true;
        }
        return false;
    }

    public boolean canNext蝶梦() {
        if (this.next蝶夢 <= System.currentTimeMillis()) {
            this.next蝶夢 = System.currentTimeMillis() + 1000;
            return true;
        }
        return false;
    }

    private class InvalidationTask implements Runnable {

        @Override
        public void run() {
            CheatingOffenseEntry[] offenses_copy;
            rL.lock();
            try {
                offenses_copy = offenses.values().toArray(new CheatingOffenseEntry[offenses.size()]);
            } finally {
                rL.unlock();
            }
            for (CheatingOffenseEntry offense : offenses_copy) {
                if (offense.isExpired()) {
                    expireEntry(offense);
                }
            }
            if (getPlayer() == null) {
                dispose();
            }
        }
    }

    public boolean isAttacking() {
        return lastAttackTime != 0 && System.currentTimeMillis() - lastAttackTime < 5 * 1000;
    }
}
