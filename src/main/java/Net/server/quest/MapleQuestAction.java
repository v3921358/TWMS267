package Net.server.quest;

import Client.MapleCharacter;
import Client.MapleQuestStatus;
import Client.MapleStat;
import Client.MapleTraitType;
import Client.inventory.InventoryException;
import Client.inventory.MapleInventoryType;
import Client.skills.Skill;
import Client.skills.SkillEntry;
import Client.skills.SkillFactory;
import Config.constants.GameConstants;
import Config.constants.ItemConstants;
import Config.constants.JobConstants;
import Net.server.MapleInventoryManipulator;
import Net.server.MapleItemInformationProvider;
import Packet.EffectPacket;
import Packet.MaplePacketCreator;
import tools.DateUtil;
import tools.Pair;
import tools.Randomizer;
import tools.Triple;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapleQuestAction {

    private final MapleQuestActionType type;
    private final MapleQuest quest;
    private final List<Integer> applicableJobs = new ArrayList<>();
    private long intStore = 0;
    private List<QuestItem> items = null;
    private List<Triple<Integer, Integer, Integer>> skill = null;
    private List<Pair<Integer, Integer>> state = null;

    public MapleQuestAction(MapleQuestActionType type, ResultSet rse, MapleQuest quest, PreparedStatement pss, PreparedStatement psq, PreparedStatement psi) throws SQLException {
        this.type = type;
        this.quest = quest;

        this.intStore = rse.getLong("intStore");
        String[] jobs = rse.getString("applicableJobs").split(", ");
        if (jobs.length <= 0 && rse.getString("applicableJobs").length() > 0) {
            applicableJobs.add(Integer.parseInt(rse.getString("applicableJobs")));
        }
        for (String j : jobs) {
            if (j.length() > 0) {
                applicableJobs.add(Integer.parseInt(j));
            }
        }
        ResultSet rs;
        switch (type) {
            case item:
                items = new ArrayList<>();
                psi.setInt(1, rse.getInt("uniqueid"));
                rs = psi.executeQuery();
                while (rs.next()) {
                    items.add(new QuestItem(rs.getInt("itemid"), rs.getInt("count"), rs.getInt("period"), rs.getInt("gender"), rs.getInt("job"), rs.getInt("jobEx"), rs.getInt("prop")));
                }
                rs.close();
                break;
            case quest:
                state = new ArrayList<>();
                psq.setInt(1, rse.getInt("uniqueid"));
                rs = psq.executeQuery();
                while (rs.next()) {
                    state.add(new Pair<>(rs.getInt("quest"), rs.getInt("state")));
                }
                rs.close();
                break;
            case skill:
                skill = new ArrayList<>();
                pss.setInt(1, rse.getInt("uniqueid"));
                rs = pss.executeQuery();
                while (rs.next()) {
                    skill.add(new Triple<>(rs.getInt("skillid"), rs.getInt("skillLevel"), rs.getInt("masterLevel")));
                }
                rs.close();
                break;
        }
    }

    private static boolean canGetItem(QuestItem item, MapleCharacter chr) {
        if (item.gender != 2 && item.gender >= 0 && item.gender != chr.getGender()) {
            return false;
        }
        if (item.job > 0) {
            List<Integer> code = getJobBy5ByteEncoding(item.job);
            boolean jobFound = false;
            for (int codec : code) {
                if (codec / 100 == chr.getJob() / 100) {
                    jobFound = true;
                    break;
                }
            }
            if (!jobFound && item.jobEx > 0) {
                List<Integer> codeEx = getJobBySimpleEncoding(item.jobEx);
                for (int codec : codeEx) {
                    if ((codec / 100 % 10) == (chr.getJob() / 100 % 10)) {
                        jobFound = true;
                        break;
                    }
                }
            }
            return jobFound;
        }
        return true;
    }

    private static List<Integer> getJobBy5ByteEncoding(int encoded) {
        List<Integer> ret = new ArrayList<>();
        if ((encoded & 0x1) != 0) {
            ret.add(0);
        }
        if ((encoded & 0x2) != 0) {
            ret.add(100);
        }
        if ((encoded & 0x4) != 0) {
            ret.add(200);
        }
        if ((encoded & 0x8) != 0) {
            ret.add(300);
        }
        if ((encoded & 0x10) != 0) {
            ret.add(400);
        }
        if ((encoded & 0x20) != 0) {
            ret.add(500);
        }
        if ((encoded & 0x400) != 0) {
            ret.add(1000);
        }
        if ((encoded & 0x800) != 0) {
            ret.add(1100);
        }
        if ((encoded & 0x1000) != 0) {
            ret.add(1200);
        }
        if ((encoded & 0x2000) != 0) {
            ret.add(1300);
        }
        if ((encoded & 0x4000) != 0) {
            ret.add(1400);
        }
        if ((encoded & 0x8000) != 0) {
            ret.add(1500);
        }
        if ((encoded & 0x20000) != 0) {
            ret.add(2001); //player not sure of this one
            ret.add(2200);
        }
        if ((encoded & 0x100000) != 0) {
            ret.add(2000);
            ret.add(2001); //?
        }
        if ((encoded & 0x200000) != 0) {
            ret.add(2100);
        }
        if ((encoded & 0x400000) != 0) {
            ret.add(2001); //?
            ret.add(2200);
        }
        if ((encoded & 0x40000000) != 0) { //i haven't seen any higher than this o.o
            ret.add(3000);
            ret.add(3200);
            ret.add(3300);
            ret.add(3500);
        }
        return ret;
    }

    private static List<Integer> getJobBySimpleEncoding(int encoded) {
        List<Integer> ret = new ArrayList<>();
        if ((encoded & 0x1) != 0) {
            ret.add(200);
        }
        if ((encoded & 0x2) != 0) {
            ret.add(300);
        }
        if ((encoded & 0x4) != 0) {
            ret.add(400);
        }
        if ((encoded & 0x8) != 0) {
            ret.add(500);
        }
        return ret;
    }

    public boolean restoreLostItem(MapleCharacter chr, int itemid) {
        if (type == MapleQuestActionType.item) {
            for (QuestItem item : items) {
                if (item.itemid == itemid) {
                    if (!chr.haveItem(item.itemid, item.count, true, false)) {
                        MapleInventoryManipulator.addById(chr.getClient(), item.itemid, item.count, "Obtained from quest (Restored) " + quest.getId() + " on " + DateUtil.getCurrentDate());
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * 開始任務
     */
    public void runStart(MapleCharacter chr, Integer extSelection) {
        MapleQuestStatus status;
        switch (type) {
            case exp:
                status = chr.getQuest(quest);
                if (status.getForfeited() > 0) {
                    break;
                }
                chr.gainExp(intStore * GameConstants.getExpRate_Quest(chr.getLevel()) * ((chr.getTrait(MapleTraitType.sense).getLevel() * 3L / 10) + 100) / 100, true, true, true);
                break;
            case item:
                // first check for randomness in item selection
                Map<Integer, Integer> props = new HashMap<>();
                for (QuestItem item : items) {
                    if (item.prop > 0 && canGetItem(item, chr)) {
                        for (int i = 0; i < item.prop; i++) {
                            props.put(props.size(), item.itemid);
                        }
                    }
                }
                int selection = 0;
                int extNum = 0;
                if (props.size() > 0) {
                    selection = props.get(Randomizer.nextInt(props.size()));
                }
                Map<Integer, Integer> map = new HashMap<>();
                for (QuestItem item : items) {
                    if (!canGetItem(item, chr)) {
                        continue;
                    }
                    int id = item.itemid;
                    if (item.prop != -2) {
                        if (item.prop == -1) {
                            if (extSelection != null && extSelection != extNum++) {
                                continue;
                            }
                        } else if (id != selection) {
                            continue;
                        }
                    }
                    int count = item.count;
                    if (count == 0) {
                        for (MapleQuestRequirement req : quest.getStartReqs()) {
                            if (req.getType() != MapleQuestRequirementType.item) {
                                continue;
                            }
                            for (Pair<Integer, Integer> a : req.getDataStore()) {
                                if (a.getLeft() == id) {
                                    count = -a.getRight();
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    if (count < 0) { // 刪除任務道具
                        try {
                            MapleInventoryManipulator.removeById(chr.getClient(), ItemConstants.getInventoryType(id), id, Math.abs(count), true, false);
                        } catch (InventoryException ie) {
                            // it's better to catch this here so we'll atleast try to remove the other items
                            System.err.println("[h4x] Completing a quest without meeting the requirements" + ie);
                        }
                        chr.send(EffectPacket.getShowItemGain(id, (short) count, true));
                    } else { // 給任務獎勵道具
                        int period = item.period / 1440; //轉換成天
                        String name = MapleItemInformationProvider.getInstance().getName(id);
                        if (id / 10000 == 114 && name != null && name.length() > 0) { //如果是勳章道具
                            String msg = "恭喜您獲得勳章 <" + name + ">";
                            chr.dropMessage(-1, msg);
                            chr.dropMessage(5, msg);
                        }
                        MapleInventoryManipulator.addById(chr.getClient(), id, count, "", null, period, "任務獲得 " + quest.getId() + " 時間: " + DateUtil.getCurrentDate());
                        chr.send(EffectPacket.getShowItemGain(id, (short) count, true));
                    }
                    map.put(id, count);
                }
                if (map.size() > 0) {
                    chr.send(EffectPacket.showQuestItemGain(map));
                }
                break;
            case nextQuest:
                status = chr.getQuest(quest);
                if (status.getForfeited() > 0) {
                    break;
                }
                chr.send(MaplePacketCreator.updateQuestInfo(quest.getId(), status.getNpc(), (int) intStore, false));
                break;
            case money:
                status = chr.getQuest(quest);
                if (status.getForfeited() > 0) {
                    break;
                }
                chr.gainMeso(intStore, true, true);
                break;
            case quest:
                for (Pair<Integer, Integer> q : state) {
                    chr.updateQuest(new MapleQuestStatus(MapleQuest.getInstance(q.left), q.right));
                }
                break;
            case skill:
                Map<Integer, SkillEntry> list = new HashMap<>();
                for (Triple<Integer, Integer, Integer> skills : skill) {
                    int skillid = skills.left;
                    int skillLevel = skills.mid;
                    int masterLevel = skills.right;
                    Skill skillObject = SkillFactory.getSkill(skillid);
                    boolean found = false;
                    for (int applicableJob : applicableJobs) {
                        if (chr.getJob() == applicableJob) {
                            found = true;
                            break;
                        }
                    }
                    if (skillObject.isBeginnerSkill() || found) {
                        list.put(skillid, new SkillEntry((byte) Math.max(skillLevel, chr.getSkillLevel(skillObject)), (byte) Math.max(masterLevel, chr.getMasterLevel(skillObject)), SkillFactory.getDefaultSExpiry(skillObject)));
                    }
                }
                chr.changeSkillsLevel(list);
                break;
            case pop:
                status = chr.getQuest(quest);
                if (status.getForfeited() > 0) {
                    break;
                }
                int fameGain = (int) intStore;
                chr.addFame(fameGain);
                chr.updateSingleStat(MapleStat.人氣, chr.getFame());
                chr.send(MaplePacketCreator.getShowFameGain(fameGain));
                break;
            case buffItemID:
                status = chr.getQuest(quest);
                if (status.getForfeited() > 0) {
                    break;
                }
                int tobuff = (int) intStore;
                if (tobuff <= 0) {
                    break;
                }
                MapleItemInformationProvider.getInstance().getItemEffect(tobuff).applyTo(chr);
                break;
            case infoNumber: {
//		        System.out.println("quest : "+intStore+"");
//		        MapleQuest.getInstance(intStore).forceComplete(c, 0);
                break;
            }
            case sp: {
                status = chr.getQuest(quest);
                if (status.getForfeited() > 0) {
                    break;
                }
                int sp_val = (int) intStore;
                if (applicableJobs.size() > 0) {
                    int finalJob = 0;
                    for (int job_val : applicableJobs) {
                        if (chr.getJob() >= job_val && job_val > finalJob) {
                            finalJob = job_val;
                        }
                    }
                    if (finalJob == 0) {
                        chr.gainSP(sp_val);
                    } else {
                        chr.gainSP(sp_val, JobConstants.getSkillBookByJob(finalJob));
                    }
                } else {
                    chr.gainSP(sp_val);
                }
                break;
            }
            case charmEXP:
            case charismaEXP:
            case craftEXP:
            case insightEXP:
            case senseEXP:
            case willEXP: {
                status = chr.getQuest(quest);
                if (status.getForfeited() > 0) {
                    break;
                }
                chr.getTrait(MapleTraitType.getByQuestName(type.name())).addExp((int) intStore, chr);
                break;
            }
            case transferField: {
                if (chr.getMapId() != intStore) {
                    chr.changeMap((int) intStore, 0);
                }
                break;
            }
        }
    }

    /*
     * 檢查任務是否可以完成
     */
    public boolean checkEnd(MapleCharacter chr, Integer extSelection) {
        switch (type) {
            case item: {
                // first check for randomness in item selection
                Map<Integer, Integer> props = new HashMap<>();

                for (QuestItem item : items) {
                    if (item.prop > 0 && canGetItem(item, chr)) {
                        for (int i = 0; i < item.prop; i++) {
                            props.put(props.size(), item.itemid);
                        }
                    }
                }
                int selection = 0;
                int extNum = 0;
                if (props.size() > 0) {
                    selection = props.get(Randomizer.nextInt(props.size()));
                }
                byte eq = 0, use = 0, setup = 0, etc = 0, cash = 0, decoration = 0;

                for (QuestItem item : items) {
                    if (canGetItem(item, chr)) {
                        int id = item.itemid;
                        if (item.prop == -1 && extSelection != null && extSelection != extNum++) {
                            continue;
                        } else if (item.prop != -2 && id != selection) {
                            continue;
                        }
                        short count = (short) item.count;
                        if (count == 0) {
                            for (MapleQuestRequirement req : quest.getCompleteReqs()) {
                                if (req.getType() != MapleQuestRequirementType.item) {
                                    continue;
                                }
                                for (Pair<Integer, Integer> a : req.getDataStore()) {
                                    if (a.getLeft() == id) {
                                        count = (short) (-a.getRight());
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                        if (count < 0) { // 刪除任務道具檢測
                            if (!chr.haveItem(id, Math.abs(count), false, true)) {
                                chr.dropMessage(1, "您的任務道具不夠，還不能完成任務.");
                                return false;
                            }
                        } else { // 給角色任務獎勵檢測
                            if (MapleItemInformationProvider.getInstance().isPickupRestricted(id) && chr.haveItem(id, 1, true, false)) {
                                chr.dropMessage(1, "你已經擁有這個物品：" + MapleItemInformationProvider.getInstance().getName(id));
                                return false;
                            }
                            switch (ItemConstants.getInventoryType(id)) {
                                case EQUIP:
                                    eq++;
                                    break;
                                case USE:
                                    use++;
                                    break;
                                case SETUP:
                                    setup++;
                                    break;
                                case ETC:
                                    etc++;
                                    break;
                                case CASH:
                                    cash++;
                                case DECORATION:
                                    decoration++;
                                    break;
                            }
                        }
                    }
                }
                if (chr.getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() < eq) {
                    chr.dropMessage(1, "裝備欄空間不足.");
                    return false;
                } else if (chr.getInventory(MapleInventoryType.USE).getNumFreeSlot() < use) {
                    chr.dropMessage(1, "消耗欄空間不足.");
                    return false;
                } else if (chr.getInventory(MapleInventoryType.SETUP).getNumFreeSlot() < setup) {
                    chr.dropMessage(1, "裝飾欄空間不足.");
                    return false;
                } else if (chr.getInventory(MapleInventoryType.ETC).getNumFreeSlot() < etc) {
                    chr.dropMessage(1, "其他欄空間不足.");
                    return false;
                } else if (chr.getInventory(MapleInventoryType.CASH).getNumFreeSlot() < cash) {
                    chr.dropMessage(1, "特殊欄空間不足.");
                    return false;
                } else if (chr.getInventory(MapleInventoryType.DECORATION).getNumFreeSlot() < cash) {
                    chr.dropMessage(1, "時裝欄空間不足.");
                    return false;
                }
                return true;
            }
            case money: {
                long meso = intStore;
                if (chr.getMeso() + meso < 0) { // Giving, overflow
                    chr.dropMessage(1, "攜帶楓幣數量已達限制.");
                    return false;
                } else if (meso < 0 && chr.getMeso() < Math.abs(meso)) { //remove meso
                    chr.dropMessage(1, "楓幣不足.");
                    return false;
                }
                return true;
            }
        }
        return true;
    }

    /*
     * 結束任務
     */
    public void runEnd(MapleCharacter chr, Integer extSelection) {
        switch (type) {
            case exp: {
                chr.gainExp(intStore * GameConstants.getExpRate_Quest(chr.getLevel()) * (chr.getStat().getQuestBonus()) * ((chr.getTrait(MapleTraitType.sense).getLevel() * 3L / 10) + 100) / 100, true, true, true);
                break;
            }
            case item: {
                // first check for randomness in item selection
                Map<Integer, Integer> props = new HashMap<>();
                for (QuestItem item : items) {
                    if (item.prop > 0 && canGetItem(item, chr)) {
                        for (int i = 0; i < item.prop; i++) {
                            props.put(props.size(), item.itemid);
                        }
                    }
                }
                int selection = 0;
                int extNum = 0;
                if (props.size() > 0) {
                    selection = props.get(Randomizer.nextInt(props.size()));
                }
                for (QuestItem item : items) {
                    if (!canGetItem(item, chr)) {
                        continue;
                    }
                    int id = item.itemid;
                    if (item.prop != -2) {
                        if (item.prop == -1) {
                            if (extSelection != null && extSelection != extNum++) {
                                continue;
                            }
                        } else if (id != selection) {
                            continue;
                        }
                    }
                    int count = item.count;
                    if (count == 0) {
                        for (MapleQuestRequirement req : quest.getCompleteReqs()) {
                            if (req.getType() != MapleQuestRequirementType.item) {
                                continue;
                            }
                            for (Pair<Integer, Integer> a : req.getDataStore()) {
                                if (a.getLeft() == id) {
                                    count = -a.getRight();
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    if (count < 0) { // 刪除任務道具
                        MapleInventoryManipulator.removeById(chr.getClient(), ItemConstants.getInventoryType(id), id, Math.abs(count), true, false);
                        chr.send(EffectPacket.getShowItemGain(id, (short) count, true));
                    } else if (count > 0) { // 給任務獎勵道具
                        int period = item.period / 1440; //轉換成天數
                        String name = MapleItemInformationProvider.getInstance().getName(id);
                        if (id / 10000 == 114 && name != null && name.length() > 0) { //如果是勳章道具獎勵
                            String msg = "<" + name + ">獲得稱號。";
                            chr.dropMessage(-1, msg);
                            chr.dropMessage(5, msg);
                        }
                        MapleInventoryManipulator.addById(chr.getClient(), id, count, "", null, period, "任務獲得 " + quest.getId() + " 時間: " + DateUtil.getCurrentDate());
                        chr.send(EffectPacket.getShowItemGain(id, (short) count, true));
                    }
                }
                break;
            }
            case nextQuest: {
                chr.send(MaplePacketCreator.updateQuestInfo(quest.getId(), chr.getQuest(quest).getNpc(), (int) intStore, false));
                break;
            }
            case money: {
                chr.gainMeso(intStore, true, true);
                break;
            }
            case quest: {
                for (Pair<Integer, Integer> q : state) {
                    chr.updateQuest(new MapleQuestStatus(MapleQuest.getInstance(q.left), q.right));
                }
                break;
            }
            case skill:
                Map<Integer, SkillEntry> list = new HashMap<>();
                for (Triple<Integer, Integer, Integer> skills : skill) {
                    int skillid = skills.left;
                    int skillLevel = skills.mid;
                    int masterLevel = skills.right;
                    Skill skillObject = SkillFactory.getSkill(skillid);
                    boolean found = false;
                    for (int applicableJob : applicableJobs) {
                        if (chr.getJob() == applicableJob) {
                            found = true;
                            break;
                        }
                    }
                    if (skillObject.isBeginnerSkill() || found) {
                        list.put(skillid, new SkillEntry((byte) Math.max(skillLevel, chr.getSkillLevel(skillObject)), (byte) Math.max(masterLevel, chr.getMasterLevel(skillObject)), SkillFactory.getDefaultSExpiry(skillObject)));
                    }
                }
                chr.changeSkillsLevel(list);
                break;
            case pop: {
                int fameGain = (int) intStore;
                chr.addFame(fameGain);
                chr.updateSingleStat(MapleStat.人氣, chr.getFame());
                chr.send(MaplePacketCreator.getShowFameGain(fameGain));
                break;
            }
            case buffItemID: {
                int tobuff = (int) intStore;
                if (tobuff <= 0) {
                    break;
                }
                MapleItemInformationProvider.getInstance().getItemEffect(tobuff).applyTo(chr);
                break;
            }
            case infoNumber: {
//		System.out.println("quest : "+intStore+"");
//		MapleQuest.getInstance(intStore).forceComplete(c, 0);
                break;
            }
            case sp: {
                int sp_val = (int) intStore;
                if (applicableJobs.size() > 0) {
                    int finalJob = 0;
                    for (int job_val : applicableJobs) {
                        if (chr.getJob() >= job_val && job_val > finalJob) {
                            finalJob = job_val;
                        }
                    }
                    if (finalJob == 0) {
                        chr.gainSP(sp_val);
                    } else {
                        chr.gainSP(sp_val, JobConstants.getSkillBookByJob(finalJob));
                    }
                } else {
                    chr.gainSP(sp_val);
                }
                break;
            }
            case charmEXP:
            case charismaEXP:
            case craftEXP:
            case insightEXP:
            case senseEXP:
            case willEXP: {
                chr.getTrait(MapleTraitType.getByQuestName(type.name())).addExp((int) intStore, chr);
                break;
            }
            default:
                break;
        }
    }

    public MapleQuestActionType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type.toString();
    }

    public List<Triple<Integer, Integer, Integer>> getSkills() {
        return skill;
    }

    public List<QuestItem> getItems() {
        return items;
    }

    public static class QuestItem {

        public final int itemid;
        public final int count;
        public final int period;
        public final int gender;
        public final int job;
        public final int jobEx;
        public final int prop;

        public QuestItem(int itemid, int count, int period, int gender, int job, int jobEx, int prop) {
            this.itemid = itemid;
            this.count = count;
            this.period = period;
            this.gender = gender;
            this.job = job;
            this.jobEx = jobEx;
            this.prop = prop;
        }
    }
}
