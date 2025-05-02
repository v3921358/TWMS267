package Net.server.life;

import Client.SecondaryStat;
import Client.status.MonsterStatus;
import Database.DatabaseLoader.DatabaseConnection;
import Database.tools.SqlTool;
import Net.server.InitializeServer;
import Net.server.MapleStatInfo;
import Plugin.provider.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;
import java.util.*;

public class MobSkillFactory {

    private static final Logger log = LoggerFactory.getLogger(MobSkillFactory.class);

    private static final Map<String, MobSkill> mobSkillData = new HashMap<>();
    private static final Map<Integer, MobSkill> familiarSkillData = new HashMap<>();

    /**
     * 加載怪物技能信息
     */
    public static void initialize() {
        DatabaseConnection.domain(con -> {
            if (InitializeServer.WzSqlName.wz_mobskilldata.check(con)) {//load from sql
                SqlTool.queryAndGetList(con, "SELECT * FROM `wz_mobskilldata`", rs -> {
                    int skillId = rs.getInt("id");
                    int level = rs.getInt("level");
                    MobSkill ret = new MobSkill(skillId, level);
                    List<Integer> list = new ArrayList<>();
                    String toSummon = rs.getString("toSummon");
                    if (!toSummon.isEmpty()) {
                        for (String mobid : toSummon.split(",")) {
                            list.add(Integer.parseInt(mobid));
                        }
                    }
                    ret.setSummons(list);
                    long interval = rs.getLong("interval");
                    ret.setCoolTime(interval);
                    int time = rs.getInt("time");
                    ret.setDuration(time);
                    int hp = rs.getInt("hp");
                    ret.setMobHp(hp);
                    int mpCon = rs.getInt("mpCon");
                    ret.setMobMpCon(mpCon);
                    int summonEffect = rs.getInt("summonEffect");
                    ret.setSummonEffect(summonEffect);
                    int x = rs.getInt("x");
                    ret.setX(x);
                    int y = rs.getInt("y");
                    ret.setY(y);
                    int w = rs.getInt("w");
                    ret.setW(w);
                    int z = rs.getInt("z");
                    ret.setZ(z);
                    float prop = rs.getFloat("prop");
                    ret.setProp(prop);
                    short limit = rs.getShort("limit");
                    ret.setLimit(limit);
                    boolean summonOnce = rs.getBoolean("summonOnce");
                    ret.setSummonOnce(summonOnce);
                    Point lt = decodePointString(rs.getString("lt"));
                    Point rb = decodePointString(rs.getString("rb"));
                    ret.setLtRb(lt, rb);
                    Point lt2 = decodePointString(rs.getString("lt2"));
                    Point rb2 = decodePointString(rs.getString("rb2"));
                    ret.setLtRb2(lt2, rb2);
                    int areaSequenceDelay = rs.getInt("areaSequenceDelay");
                    ret.setAreaSequenceDelay(areaSequenceDelay);
                    int skillAfter = rs.getInt("skillAfter");
                    ret.setSkillAfter(skillAfter);
                    int force = rs.getInt("force");
                    ret.setForce(force);
                    int forcex = rs.getInt("forcex");
                    ret.setForcex(forcex);
                    initMobSkillInfo(ret, level);
                    mobSkillData.put(skillId + ":" + level, ret);
                    return null;
                });
            } else {//load from wz and insert into sql
                InitializeServer.WzSqlName.wz_mobskilldata.drop(con);
                SqlTool.update(con, "CREATE TABLE `wz_mobskilldata` (`id` int(11) NOT NULL, `level` int NOT NULL,`toSummon` text NOT NULL,`interval` bigint NOT NULL,`time` INT(11) NOT NULL,`hp` int NOT NULL,`mpCon` int NOT NULL,`summonEffect` int NOT NULL,`x` int NOT NULL,`y` int NOT NULL,`w` int NOT NULL,`z` int NOT NULL,`prop` float NOT NULL,`limit` smallint NOT NULL,`summonOnce` BOOLEAN NOT NULL,`lt` text NOT NULL,`rb` text NOT NULL,`lt2` text NOT NULL,`rb2` text NOT NULL,`areaSequenceDelay` int NOT NULL,`skillAfter` int NOT NULL,`force` int NOT NULL,`forcex` int NOT NULL,PRIMARY KEY (`id`, `level`))");
                MapleDataProvider dataSource = MapleDataProviderFactory.getSkill();
                MapleDataEntry dataEntry = dataSource.getRoot().getEntry("MobSkill");
                List<MapleDataFileEntry> mobSkillFiles = ((MapleDataDirectoryEntry) dataEntry).getFiles();
                for (MapleDataFileEntry mobSkillFile : mobSkillFiles) {
                    String name = mobSkillFile.getName();
                    MapleData skillData = dataSource.getData("MobSkill/" + name);
                    int skillId = name.endsWith(".img") ? Integer.parseInt(name.substring(0, name.length() - 4)) : Integer.parseInt(name);
                    for (MapleData levelData : skillData.getChildByPath("level").getChildren()) {
                        int level = Integer.parseInt(levelData.getName());
                        List<Integer> toSummon = new ArrayList<>();
                        int i = 0;
                        MapleData data;
                        StringBuilder s = new StringBuilder();
                        while ((data = levelData.getChildByPath(String.valueOf(i++))) != null) {
                            int id = MapleDataTool.getInt(data, 0);
                            toSummon.add(id);
                            s.append(id).append(",");
                        }
                        if (s.length() > 0 && s.charAt(s.length() - 1) == ',') {
                            s.deleteCharAt(s.length() - 1);
                        }
                        List<Point> fixedPos = new ArrayList<>();
                        MapleData fixedPosData = levelData.getChildByPath("fixedPos");
                        if (fixedPosData != null) {
                            for (MapleData d : fixedPosData) {
                                fixedPos.add((Point) d.getData());
                            }
                        }
                        MapleData ltdata = levelData.getChildByPath("lt");
                        Point lt = null;
                        if (ltdata != null) {
                            lt = (Point) ltdata.getData();
                        }
                        MapleData rbdata = levelData.getChildByPath("rb");
                        Point rb = null;
                        if (rbdata != null) {
                            rb = (Point) rbdata.getData();
                        }
                        MapleData ltdata2 = levelData.getChildByPath("lt2");
                        Point lt2 = null;
                        if (ltdata2 != null) {
                            lt2 = (Point) ltdata2.getData();
                        }
                        MapleData rbdata2 = levelData.getChildByPath("rb2");
                        Point rb2 = null;
                        if (rbdata2 != null) {
                            rb2 = (Point) rbdata2.getData();
                        }
                        MobSkill ret = new MobSkill(skillId, level);
                        ret.setSummons(toSummon);
                        ret.setFixedPos(fixedPos);
                        long interval = MapleDataTool.getInt("interval", levelData, 0) * 1000L;
                        ret.setCoolTime(interval);
                        int time = MapleDataTool.getInt("time", levelData, 0) * 1000;
                        ret.setDuration(time);
                        int hp = MapleDataTool.getInt("hp", levelData, 100);
                        ret.setMobHp(hp);
                        int mpCon = MapleDataTool.getInt("mpCon", levelData, 0);
                        ret.setMobMpCon(mpCon);
                        int summonEffect = MapleDataTool.getInt("summonEffect", levelData, 0);
                        ret.setSummonEffect(summonEffect);
                        int x = MapleDataTool.getInt("x", levelData, 1);
                        ret.setX(x);
                        int y = MapleDataTool.getInt("y", levelData, 1);
                        ret.setY(y);
                        int w = MapleDataTool.getInt("w", levelData, 1);
                        ret.setW(w);
                        int z = MapleDataTool.getInt("z", levelData, 1);
                        ret.setZ(z);
                        float prop = MapleDataTool.getInt("prop", levelData, 100) / 100.0f;
                        ret.setProp(prop);
                        short limit = (short) MapleDataTool.getInt("limit", levelData, 0);
                        ret.setLimit(limit);
                        boolean summonOnce = MapleDataTool.getInt("summonOnce", levelData, 0) > 0;
                        ret.setSummonOnce(summonOnce);
                        ret.setLtRb(lt, rb);
                        ret.setLtRb2(lt2, rb2);
                        int areaSequenceDelay = MapleDataTool.getInt("areaSequenceDelay", levelData, 0);
                        ret.setAreaSequenceDelay(areaSequenceDelay);
                        int skillAfter = MapleDataTool.getInt("skillAfter", levelData, 0);
                        ret.setSkillAfter(skillAfter);
                        int force = MapleDataTool.getInt("force", levelData, 0);
                        ret.setForce(force);
                        int forcex = MapleDataTool.getInt("forcex", levelData, 0);
                        ret.setForcex(forcex);
                        initMobSkillInfo(ret, level);
                        mobSkillData.put(skillId + ":" + level, ret);
                        SqlTool.update(con, "INSERT INTO `wz_mobskilldata` (`id`,`level`,`toSummon`,`interval`,`time`,`hp`,`mpCon`,`summonEffect`,`x`,`y`,`w`,`z`,`prop`,`limit`,`summonOnce`,`lt`,`rb`,`lt2`,`rb2`,`areaSequenceDelay`,`skillAfter`,`force`,`forcex`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", skillId, level, s.toString(), interval, time, hp, mpCon, summonEffect, x, y, w, z, prop, limit, summonOnce, lt == null ? "" : lt.toString(), rb == null ? "" : rb.toString(), lt2 == null ? "" : lt2.toString(), rb2 == null ? "" : rb2.toString(), areaSequenceDelay, skillAfter, force, forcex);
                    }
                }

                InitializeServer.WzSqlName.wz_mobskilldata.update(con);
            }
            if (InitializeServer.WzSqlName.wz_familiarskill.check(con)) {
                SqlTool.queryAndGetList(con, "SELECT * FROM wz_familiarskill", rs -> {
                    int skillId = rs.getInt("id");
                    MobSkill ret = new MobSkill(skillId, 1);
                    long interval = rs.getLong("interval");
                    ret.setCoolTime(interval);
                    int time = rs.getInt("time");
                    ret.setDuration(time);
                    int x = rs.getInt("x");
                    ret.setX(x);
                    int y = rs.getInt("y");
                    ret.setY(y);
                    int w = rs.getInt("w");
                    ret.setW(w);
                    int z = rs.getInt("z");
                    ret.setZ(z);
                    float prop = rs.getFloat("prop");
                    ret.setProp(prop);
                    Point lt = decodePointString(rs.getString("lt"));
                    Point rb = decodePointString(rs.getString("rb"));
                    ret.setLtRb(lt, rb);
                    Point lt2 = decodePointString(rs.getString("lt2"));
                    Point rb2 = decodePointString(rs.getString("rb2"));
                    ret.setLtRb2(lt2, rb2);
                    familiarSkillData.put(skillId, ret);
                    return null;
                });
            } else {
                InitializeServer.WzSqlName.wz_familiarskill.drop(con);
                SqlTool.update(con, "CREATE TABLE `wz_familiarskill` (`id` int(11) NOT NULL,`interval` bigint NOT NULL,`time` INT(11) NOT NULL,`x` int NOT NULL,`y` int NOT NULL,`w` int NOT NULL,`z` int NOT NULL,`prop` float NOT NULL,`lt` text NOT NULL,`rb` text NOT NULL,`lt2` text NOT NULL,`rb2` text NOT NULL,PRIMARY KEY (`id`))");
                MapleData skillRoot = MapleDataProviderFactory.getSkill().getData("FamiliarSkill.img");
                for (MapleData skillData : skillRoot.getChildren()) {
                    int skillId = Integer.parseInt(skillData.getName());
                    MapleData ltdata = skillData.getChildByPath("lt");
                    Point lt = null;
                    if (ltdata != null) {
                        lt = (Point) ltdata.getData();
                    }
                    MapleData rbdata = skillData.getChildByPath("rb");
                    Point rb = null;
                    if (rbdata != null) {
                        rb = (Point) rbdata.getData();
                    }
                    MapleData ltdata2 = skillData.getChildByPath("lt2");
                    Point lt2 = null;
                    if (ltdata2 != null) {
                        lt2 = (Point) ltdata2.getData();
                    }
                    MapleData rbdata2 = skillData.getChildByPath("rb2");
                    Point rb2 = null;
                    if (rbdata2 != null) {
                        rb2 = (Point) rbdata2.getData();
                    }
                    MobSkill ret = new MobSkill(skillId, 1);
                    long interval = MapleDataTool.getInt("interval", skillData, 0) * 1000L;
                    ret.setCoolTime(interval);
                    int time = MapleDataTool.getInt("time", skillData, 0) * 1000;
                    ret.setDuration(time);
                    int x = MapleDataTool.getInt("x", skillData, 1);
                    ret.setX(x);
                    int y = MapleDataTool.getInt("y", skillData, 1);
                    ret.setY(y);
                    int w = MapleDataTool.getInt("w", skillData, 1);
                    ret.setW(w);
                    int z = MapleDataTool.getInt("z", skillData, 1);
                    ret.setZ(z);
                    float prop = MapleDataTool.getInt("prop", skillData, 100) / 100.0f;
                    ret.setProp(prop);
                    ret.setLtRb(lt, rb);
                    ret.setLtRb2(lt2, rb2);
                    familiarSkillData.put(skillId, ret);
                    SqlTool.update(con, "INSERT INTO `wz_familiarskill` (`id`,`interval`,`time`,`x`,`y`,`w`,`z`,`prop`,`lt`,`rb`,`lt2`,`rb2`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)", skillId, interval, time, x, y, w, z, prop, lt == null ? "" : lt.toString(), rb == null ? "" : rb.toString(), lt2 == null ? "" : lt2.toString(), rb2 == null ? "" : rb2.toString());
                }
            }
            return null;
        });
        //log.info("共加載 " + mobSkillData.size() + " 個怪物技能訊息..."); /* 暫時關閉 */
    }

    private static void initMobSkillInfo(MobSkill ret, int level) {
        if (ret == null) {
            return;
        }
        if (ret.getInfo() != null && !ret.getInfo().isEmpty()) {
            return;
        }
        ret.setInfo(new EnumMap<>(MapleStatInfo.class));
        for (MapleStatInfo info : MapleStatInfo.values()) {
            ret.getInfo().put(info, info.getDefault());
        }
        ret.setStatups(new EnumMap<>(SecondaryStat.class));
        ret.setMonsterStatus(new EnumMap<>(MonsterStatus.class));
        switch (ret.getSourceId()) {
            case 100:
            case 110: {
                ret.getMonsterStatus().put(MonsterStatus.PowerUp, ret.getX());
                break;
            }
            case 101:
            case 111: {
                ret.getMonsterStatus().put(MonsterStatus.MagicUp, ret.getX());
                break;
            }
            case 102:
            case 112: {
                ret.getMonsterStatus().put(MonsterStatus.PGuardUp, ret.getX());
                break;
            }
            case 103:
            case 113: {
                ret.getMonsterStatus().put(MonsterStatus.MGuardUp, ret.getX());
                break;
            }
            case 150: {
                ret.getMonsterStatus().put(MonsterStatus.PAD, ret.getX());
                break;
            }
            case 151: {
                ret.getMonsterStatus().put(MonsterStatus.MAD, ret.getX());
                break;
            }
            case 152: {
                ret.getMonsterStatus().put(MonsterStatus.PDR, ret.getX());
                break;
            }
            case 153: {
                ret.getMonsterStatus().put(MonsterStatus.MDR, ret.getX());
                break;
            }
            case 154: {
                ret.getMonsterStatus().put(MonsterStatus.ACC, ret.getX());
                break;
            }
            case 155: {
                ret.getMonsterStatus().put(MonsterStatus.EVA, ret.getX());
                break;
            }
            case 104:
            case 115:
            case 156: {
                ret.getMonsterStatus().put(MonsterStatus.Speed, ret.getX());
                break;
            }
            case 157: {
                ret.getMonsterStatus().put(MonsterStatus.Seal, ret.getX());
                break;
            }
            case 236: {
                ret.getMonsterStatus().put(MonsterStatus.HangOver, ret.getX());
                break;
            }
            case 188: {
                ret.setEmotion(1);
                ret.getStatups().put(SecondaryStat.Slow, ret.getX());
                ret.getStatups().put(SecondaryStat.Stance, ret.getProp());
                ret.getStatups().put(SecondaryStat.Attract, 1);
                if (ret.getDuration() == 0) {
                    ret.setDuration(3000);
                }
                ret.getMonsterStatus().put(MonsterStatus.Dazzle, ret.getX());
                break;
            }
            case 140: {
                ret.getMonsterStatus().put(MonsterStatus.PImmune, ret.getX());
                break;
            }
            case 141: {
                ret.getMonsterStatus().put(MonsterStatus.MImmune, ret.getX());
                break;
            }
            case 142: {
                ret.getMonsterStatus().put(MonsterStatus.HardSkin, ret.getX());
                break;
            }
            case 143: {
                ret.getMonsterStatus().put(MonsterStatus.PImmune, ret.getX());
                ret.getMonsterStatus().put(MonsterStatus.PCounter, ret.getX());
                ret.getMonsterStatus().put(MonsterStatus.SealSkill, ret.getX());
                break;
            }
            case 144: {
                ret.getMonsterStatus().put(MonsterStatus.MImmune, ret.getX());
                ret.getMonsterStatus().put(MonsterStatus.SealSkill, ret.getX());
                ret.getMonsterStatus().put(MonsterStatus.MCounter, ret.getX());
                break;
            }
            case 145: {
                ret.getMonsterStatus().put(MonsterStatus.PCounter, ret.getX());
                ret.getMonsterStatus().put(MonsterStatus.PImmune, ret.getX());
                ret.getMonsterStatus().put(MonsterStatus.MCounter, ret.getX());
                ret.getMonsterStatus().put(MonsterStatus.SealSkill, ret.getX());
                ret.getMonsterStatus().put(MonsterStatus.MImmune, ret.getX());
                break;
            }
            case 146: {
                ret.getMonsterStatus().put(MonsterStatus.Invincible, ret.getX());
                break;
            }
            case 223: {
                ret.getMonsterStatus().put(MonsterStatus.Laser, 1);
                break;
            }
            case 120: {
                ret.getStatups().put(SecondaryStat.Seal, ret.getX());
                break;
            }
            case 121: {
                ret.getStatups().put(SecondaryStat.Darkness, ret.getX());
                break;
            }
            case 122: {
                ret.getStatups().put(SecondaryStat.Weakness, ret.getX());
                break;
            }
            case 123: {
                if (level == 75) {
                    ret.setDuration(3000);
                }
                ret.getStatups().put(SecondaryStat.Stun, ret.getX());
                break;
            }
            case 124: {
                ret.getStatups().put(SecondaryStat.Curse, ret.getX());
                break;
            }
            case 125: {
                ret.getStatups().put(SecondaryStat.Poison, ret.getX());
                break;
            }
            case 126: {
                ret.getStatups().put(SecondaryStat.Slow, ret.getX());
                break;
            }
            case 128: {
                ret.getStatups().put(SecondaryStat.Attract, ret.getX());
                break;
            }
            case 132: {
                ret.getStatups().put(SecondaryStat.ReverseInput, ret.getX());
                break;
            }
            case 133: {
                ret.getStatups().put(SecondaryStat.BanMap, ret.getX());
                break;
            }
            case 134: {
                ret.getStatups().put(SecondaryStat.StopPortion, ret.getX());
                break;
            }
            case 135: {
                ret.getStatups().put(SecondaryStat.StopMotion, ret.getX());
                break;
            }
            case 136: {
                ret.getStatups().put(SecondaryStat.Fear, ret.getX());
                break;
            }
            case 137: {
                ret.getStatups().put(SecondaryStat.Frozen, ret.getX());
                break;
            }
            case 138: {
                ret.getStatups().put(SecondaryStat.DispelItemOption, ret.getX());
                break;
            }
            case 171: {
                ret.getStatups().put(SecondaryStat.TimeBomb, ret.getX());
                break;
            }
            case 172: {
                ret.getStatups().put(SecondaryStat.Morph, ret.getX());
                break;
            }
            case 173: {
                ret.getStatups().put(SecondaryStat.Web, ret.getX());
                break;
            }
            case 174: {
                ret.getStatups().put(SecondaryStat.Lapidification, ret.getX());
                break;
            }
            case 175: {
                ret.getStatups().put(SecondaryStat.DeathMark, ret.getX());
                break;
            }
            case 184: {
                ret.getStatups().put(SecondaryStat.ReturnTeleport, ret.getX());
                break;
            }
            case 189: {
                ret.getStatups().put(SecondaryStat.CapDebuff, ret.getX());
                break;
            }
            case 190: {
                ret.getStatups().put(SecondaryStat.CapDebuff, ret.getX());
                break;
            }
            case 234: {
                ret.getStatups().put(SecondaryStat.Contagion, ret.getX());
                break;
            }
            case 241: {
                if (level == 8) {
                    ret.getStatups().put(SecondaryStat.Stun, 1);
                }
                ret.getMonsterStatus().put(MonsterStatus.AreaPDR, ret.getX());
                break;
            }
            case 800: {
                ret.setDuration(2100000000);
                ret.getStatups().put(SecondaryStat.GiantBossDeathCnt, ret.getY());
                break;
            }
        }
    }

    /*
     * 通過技能ID 和 等級 獲取怪物的技能信息
     */
    public static MobSkill getMobSkill(int skillId, int level) {
        MobSkill skill = mobSkillData.get(skillId + ":" + level);
        if (skill == null) {
            //log.warn("MobSkill 不存在: skillId={}, level={}", skillId, level);
            return null;
        }
        initMobSkillInfo(skill, level);
        return skill;
    }

    private static Point decodePointString(String str) {
        if (str.isEmpty()) {
            return null;
        }
        String[] s = str.substring(15, str.length() - 1).split(",");
        return new Point(Integer.parseInt(s[0].split("=")[1]), Integer.parseInt(s[1].split("=")[1]));
    }
}
