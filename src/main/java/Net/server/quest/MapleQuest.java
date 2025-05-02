package Net.server.quest;

import Client.MapleCharacter;
import Client.MapleQuestStatus;
import Config.constants.ItemConstants;
import Config.constants.JobConstants;
import Config.constants.enums.UserChatMessageType;
import Opcode.Opcode.EffectOpcode;
import Packet.EffectPacket;
import tools.Pair;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MapleQuest implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private static final List<MapleQuest> BulbQuest = new LinkedList<>();
    protected static final Map<Integer, MapleQuest> quests = new LinkedHashMap<>();
    protected final List<MapleQuestRequirement> startReqs = new LinkedList<>(); //開始任務需要的條件
    protected final List<MapleQuestRequirement> completeReqs = new LinkedList<>(); //完成任務需要的條件
    protected final List<MapleQuestAction> startActs = new LinkedList<>(); //開始任務的操作
    protected final List<MapleQuestAction> completeActs = new LinkedList<>(); //完成任務的操作
    protected final Map<String, List<Pair<String, Pair<String, Integer>>>> partyQuestInfo = new LinkedHashMap<>(); //組隊任務 [rank, [more/less/equal, [property, value]]]
    protected final Map<Integer, Integer> relevantMobs = new LinkedHashMap<>(); //完成任務需要殺的怪物和數量
    protected final Map<Integer, Integer> questItems = new LinkedHashMap<>(); //完成任務需要的道具和數量
    protected int id; //任務ID
    protected String name = "";
    private boolean autoStart = false, autoPreComplete = false, repeatable = false, blocked = false, autoAccept = false, autoComplete = false, selfStart = false;
    private int viewMedalItem = 0, selectedSkillID = 0;
    private String startscript = "", endscript = "";

    protected MapleQuest(int id) {
        this.id = id;
    }

    private static MapleQuest loadQuest(ResultSet rs, PreparedStatement psr, PreparedStatement psa, PreparedStatement pss, PreparedStatement psq, PreparedStatement psi, PreparedStatement psp) throws SQLException {
        MapleQuest ret = new MapleQuest(rs.getInt("questid"));
        ret.name = rs.getString("name");
        ret.autoStart = rs.getInt("autoStart") > 0;
        ret.autoPreComplete = rs.getInt("autoPreComplete") > 0;
        ret.autoAccept = rs.getInt("autoAccept") > 0;
        ret.autoComplete = rs.getInt("autoComplete") > 0;
        ret.viewMedalItem = rs.getInt("viewMedalItem");
        ret.selectedSkillID = rs.getInt("selectedSkillID");
        ret.blocked = rs.getInt("blocked") > 0; //ult.explorer quests will dc as the item isn't there...
        ret.selfStart = rs.getInt("selfStart") > 0;

        psr.setInt(1, ret.id);
        ResultSet rse = psr.executeQuery();
        MapleQuestRequirement jobStart = null;
        MapleQuestRequirement jobComplete = null;
        while (rse.next()) {
            MapleQuestRequirementType type = MapleQuestRequirementType.getByWZName(rse.getString("name"));
            MapleQuestRequirement req = new MapleQuestRequirement(ret, type, rse);
            switch (type) {
                case dayN:
                case dayByDay:
                case interval:
                    ret.repeatable = true;
                    break;
                case normalAutoStart:
                    ret.repeatable = true;
                    ret.autoStart = true;
                    break;
                case startscript:
                    ret.startscript = rse.getString("stringStore");
                    if (ret.startscript == null) ret.startscript = "";
                    break;
                case endscript:
                    ret.endscript = rse.getString("stringStore");
                    if (ret.endscript == null) ret.endscript = "";
                    break;
                case mob:
                    for (Pair<Integer, Integer> mob : req.getDataStore()) {
                        ret.relevantMobs.put(mob.left, mob.right);
                    }
                    break;
                case item:
                    for (Pair<Integer, Integer> it : req.getDataStore()) {
                        ret.questItems.put(it.left, it.right);
                    }
                    break;
                case job:
                case job_CN:
                case job_TW: {
                    boolean start = rse.getInt("type") == 0;
                    if (start) {
                        if (jobStart == null) {
                            jobStart = req;
                        } else {
                            List<Pair<Integer, Integer>> stores = new LinkedList<>(jobStart.getDataStore());
                            for (Pair<Integer, Integer> pair : req.getDataStore()) {
                                boolean found = false;
                                for (Pair<Integer, Integer> p : jobStart.getDataStore()) {
                                    if (p.getLeft() == pair.getLeft() && p.getRight() == pair.getRight()) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    stores.add(pair);
                                }
                            }
                            jobStart.getDataStore().clear();
                            for (Pair<Integer, Integer> pair : stores) {
                                jobStart.getDataStore().add(pair);
                            }
                        }
                    } else {
                        if (jobComplete == null) {
                            jobComplete = req;
                        } else {
                            List<Pair<Integer, Integer>> stores = new LinkedList<>(jobComplete.getDataStore());
                            for (Pair<Integer, Integer> pair : req.getDataStore()) {
                                boolean found = false;
                                for (Pair<Integer, Integer> p : jobComplete.getDataStore()) {
                                    if (p.getLeft() == pair.getLeft() && p.getRight() == pair.getRight()) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    stores.add(pair);
                                }
                            }
                            jobComplete.getDataStore().clear();
                            for (Pair<Integer, Integer> pair : stores) {
                                jobComplete.getDataStore().add(pair);
                            }
                        }
                    }
                    break;
                }
            }
            if (req.getType() != MapleQuestRequirementType.job) {
                if (rse.getInt("type") == 0) {
                    ret.startReqs.add(req);
                } else {
                    ret.completeReqs.add(req);
                }
            }
        }
        if (jobStart != null) {
            ret.startReqs.add(jobStart);
        }
        if (jobComplete != null) {
            ret.completeReqs.add(jobComplete);
        }
        if (ret.isSelfStart()) {
            BulbQuest.add(ret);
        }
        rse.close();

        psa.setInt(1, ret.id);
        rse = psa.executeQuery();
        while (rse.next()) {
            MapleQuestActionType ty = MapleQuestActionType.getByWZName(rse.getString("name"));
            if (rse.getInt("type") == 0) { //pass it over so it will set ID + type once done
                if (ty == MapleQuestActionType.item && ret.id == 7103) { //拉圖斯任務
                    continue;
                }
                ret.startActs.add(new MapleQuestAction(ty, rse, ret, pss, psq, psi));
            } else {
                if (ty == MapleQuestActionType.item && ret.id == 7102) { //時間球任務
                    continue;
                }
                ret.completeActs.add(new MapleQuestAction(ty, rse, ret, pss, psq, psi));
            }
        }
        rse.close();

        psp.setInt(1, ret.id);
        rse = psp.executeQuery();
        while (rse.next()) {
            if (!ret.partyQuestInfo.containsKey(rse.getString("rank"))) {
                ret.partyQuestInfo.put(rse.getString("rank"), new ArrayList<>());
            }
            ret.partyQuestInfo.get(rse.getString("rank")).add(new Pair<>(rse.getString("mode"), new Pair<>(rse.getString("property"), rse.getInt("value"))));
        }
        rse.close();
        return ret;
    }

    public static void initQuests(Connection con) {
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM wz_questdata");
            PreparedStatement psr = con.prepareStatement("SELECT * FROM wz_questreqdata WHERE questid = ?");
            PreparedStatement psa = con.prepareStatement("SELECT * FROM wz_questactdata WHERE questid = ?");
            PreparedStatement pss = con.prepareStatement("SELECT * FROM wz_questactskilldata WHERE uniqueid = ?");
            PreparedStatement psq = con.prepareStatement("SELECT * FROM wz_questactquestdata WHERE uniqueid = ?");
            PreparedStatement psi = con.prepareStatement("SELECT * FROM wz_questactitemdata WHERE uniqueid = ?");
            PreparedStatement psp = con.prepareStatement("SELECT * FROM wz_questpartydata WHERE questid = ?");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                quests.put(rs.getInt("questid"), loadQuest(rs, psr, psa, pss, psq, psi, psp));
            }
            rs.close();
            ps.close();
            psr.close();
            psa.close();
            pss.close();
            psq.close();
            psi.close();
            psp.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //LoggerFactory.getLogger(MapleQuest.class).info("共加載 " + quests.size() + " 個任務訊息."); /* 暫時關閉 */
    }

    public static MapleQuest getInstance(int id) {
        //by this time we have already initialized
        return quests.computeIfAbsent(id, MapleQuest::new);
    }

    public static Collection<MapleQuest> getAllInstances() {
        return quests.values();
    }

    /**
     * 燈泡任務列表
     *
     * @return
     */
    public static List<MapleQuest> GetBulbQuest() {
        return BulbQuest;
    }

    public List<Pair<String, Pair<String, Integer>>> getInfoByRank(String rank) {
        return partyQuestInfo.get(rank);
    }

    public boolean isPartyQuest() {
        return partyQuestInfo.size() > 0;
    }

    public int getSelectedSkillID() {
        return selectedSkillID;
    }

    public String getName() {
        return name;
    }

    public List<MapleQuestAction> getCompleteActs() {
        return completeActs;
    }

    public boolean canStart(MapleCharacter chr, Integer npcid) {
        if (chr.getQuest(this).getStatus() != 0) {
            if (chr.getQuest(this).getStatus() != 2) {
                if (chr.isAdmin()) {
                    chr.dropMessage(6, "[Quest Start] canStart - status != 0 && status != 2");
                }
                return false;
            } else if (!repeatable) {
                if (chr.isAdmin()) {
                    chr.dropMessage(6, "[Quest Start] canStart - status != 0 && repeatable == false");
                }
                return false;
            }
        }
        if (blocked && !chr.isGm()) {
            if (chr.isAdmin()) {
                chr.dropMessage(6, "[Quest Start] canStart - blocked");
            }
            return false;
        }
        for (MapleQuestRequirement r : startReqs) {
            if (!r.check(chr, npcid)) {
                if (chr.isAdmin()) {
                    chr.dropMessage(6, "[Quest Start] canStart - check false");
                }
                return false;
            }
        }
        return true;
    }

    public boolean canComplete(MapleCharacter chr, Integer npcid) {
        if (chr.getQuest(this).getStatus() != 1) {
            return false;
        }
        if (blocked && !chr.isGm()) {
            return false;
        }
        if (autoComplete && npcid != null && viewMedalItem <= 0) {
//            forceComplete(chr, npcid);
            return true; //skip Plugin.script
        }
        for (MapleQuestRequirement r : completeReqs) {
            if (!r.check(chr, npcid)) {
                return false;
            }
        }
        return true;
    }

    public void restoreLostItem(MapleCharacter chr, int itemid) {
        if (blocked && !chr.isGm()) {
            return;
        }
        for (MapleQuestAction action : startActs) {
            if (action.restoreLostItem(chr, itemid)) {
                break;
            }
        }
    }

    /**
     * 開始任務
     */
    public void start(MapleCharacter chr, int npc) {
        start(chr, npc, false);
    }

    public void start(MapleCharacter chr, int npc, boolean isWorldShare) {
        if (chr.isDebug()) {
            chr.dropMessage(6, "[開始任務] " + this + " NPC: " + npc + " autoStart：" + autoStart + " NPC exist: " + checkNPCOnMap(chr, npc) + " canStart: " + canStart(chr, npc));
        }
        if ((autoStart || checkNPCOnMap(chr, npc))) {
            for (MapleQuestAction a : startActs) {
                if (!a.checkEnd(chr, null)) { //just in case
                    if (chr.isDebug()) {
                        chr.dropMessage(6, "開始任務 checkEnd 錯誤...");
                    }
                    return;
                }
            }
            // 紀錄勳章的相關的任務自動完成
            if (getMedalItem() > 0 && ItemConstants.類型.勳章(getMedalItem())) {
                if (chr.haveItem(getMedalItem()) && chr.getQuestStatus(getId()) != 2) {
                    forceComplete(chr, npc);
                    return;
                }
            }
            if (!startscript.isEmpty()) {
                chr.getScriptManager().startQuestSScript(npc, this.id);
//                NPCScriptManager.getInstance().startQuest(chr.getClient(), npc, this.id);
            } else {
                forceStart(chr, npc, null, isWorldShare);
            }
        }
    }

    public void complete(MapleCharacter chr, int npc) {
        complete(chr, npc, false);
    }

    public void complete(MapleCharacter chr, int npc, boolean isWorldShare) {
        complete(chr, npc, null, isWorldShare);
    }

    public void complete(MapleCharacter chr, int npc, Integer selection) {
        complete(chr, npc, selection, false);
    }

    public void complete(MapleCharacter chr, int npc, Integer selection, boolean isWorldShare) {
        if (chr.getMap() != null && (autoComplete || autoPreComplete || checkNPCOnMap(chr, npc)) && canComplete(chr, npc)) {
            for (MapleQuestAction action : completeActs) {
                if (!action.checkEnd(chr, selection)) {
                    return;
                }
            }
            forceComplete(chr, npc, selection, isWorldShare);
        }
    }

    public void reset(MapleCharacter chr) {
        MapleQuestStatus status = chr.getQuest(this);
        status.setStatus((byte) 0);
        chr.updateQuest(status);
    }

    public void forfeit(MapleCharacter chr) {
        if (chr.getQuest(this).getStatus() != (byte) 1) {
            return;
        }
        MapleQuestStatus oldStatus = chr.getQuest(this);
        MapleQuestStatus newStatus = new MapleQuestStatus(this, (byte) 0);
        newStatus.setForfeited(oldStatus.getForfeited() + 1);
        newStatus.setCompletionTime(oldStatus.getCompletionTime());
        newStatus.setFromChrID(oldStatus.getFromChrID());
        chr.updateQuest(newStatus);
    }

    public void forceStart(MapleCharacter chr, int npc, String customData) {
        forceStart(chr, npc, customData, false);
    }

    public void forceStart(MapleCharacter chr, int npc, String customData, boolean isWorldShare) {
        if (chr.isDebug()) {
            chr.dropSpouseMessage(UserChatMessageType.青, "[Start] 開始任務 任務ID： " + getId() + " 任務Npc: " + npc);
        }
        MapleQuestStatus newStatus = new MapleQuestStatus(this, (byte) 1, npc);
        newStatus.setForfeited(chr.getQuest(this).getForfeited());
        newStatus.setCompletionTime(chr.getQuest(this).getCompletionTime());
        newStatus.setCustomData(customData);
        if (isWorldShare) {
            newStatus.setFromChrID(chr.getId());
        }
        chr.updateQuest(newStatus);
        for (MapleQuestAction action : startActs) {
            action.runStart(chr, null);
        }
    }

    public void forceComplete(MapleCharacter chr, int npc) {
        forceComplete(chr, npc, false);
    }

    public void forceComplete(MapleCharacter chr, int npc, boolean isWorldShare) {
        forceComplete(chr, npc, null, isWorldShare);
    }

    public void forceComplete(MapleCharacter chr, int npc, Integer selection, boolean isWorldShare) {
        if (chr.isDebug() && chr != null) {
            chr.dropSpouseMessage(UserChatMessageType.青, "[任務完成] " + this + " Npc: " + npc + " selection:" + selection);
        }
        MapleQuestStatus newStatus = new MapleQuestStatus(this, (byte) 2, npc);
        newStatus.setForfeited(chr.getQuest(this).getForfeited());
        if (isWorldShare) {
            newStatus.setFromChrID(chr.getId());
        }
        chr.updateQuest(newStatus);
        for (MapleQuestAction action : completeActs) {
            action.runEnd(chr, selection);
        }
        chr.send(EffectPacket.showSpecialEffect(EffectOpcode.UserEffect_QuestComplete)); // 任務完成
        chr.getMap().broadcastMessage(chr, EffectPacket.showForeignEffect(chr.getId(), EffectOpcode.UserEffect_QuestComplete), false);
    }

    public int getId() {
        return id;
    }

    public Map<Integer, Integer> getRelevantMobs() {
        return relevantMobs;
    }

    private static boolean checkNPCOnMap(MapleCharacter player, int npcId) {
        return (JobConstants.is龍魔導士(player.getJob()) && npcId == 1013000) //米樂
                || (JobConstants.is惡魔殺手(player.getJob()) && npcId == 0)
                || (JobConstants.is精靈遊俠(player.getJob()) && npcId == 0)
                || npcId == 2159421 //馬斯特瑪
                || npcId == 3000018 //愛絲卡達
                || npcId == 9010000 //楓之谷運營員
                || (npcId >= 2161000 && npcId <= 2161011) //獅子城任務
                || npcId == 9000040 //勳章老人
                || npcId == 9000066 //勳章老人
                || npcId == 2010010 //黛雅
                || npcId == 1032204 //奇怪的修行者
                || npcId == 0 //玩家頭上的任務
                || (player.getMap() != null && player.getMap().containsNPC(npcId));
    }

    public int getMedalItem() {
        return viewMedalItem;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public int getAmountofItems(int itemId) {
        return questItems.get(itemId) != null ? questItems.get(itemId) : 0;
    }

    public String getStartScript() {
        return startscript;
    }

    public String getEndScript() {
        return endscript;
    }

    public boolean isSelfStart() {
        return selfStart;
    }

    public List<MapleQuestRequirement> getStartReqs() {
        return this.startReqs;
    }

    public List<MapleQuestRequirement> getCompleteReqs() {
        return this.completeReqs;
    }

    public List<MapleQuestAction> getStartActs() {
        return this.startActs;
    }

    public Map<String, List<Pair<String, Pair<String, Integer>>>> getPartyQuestInfo() {
        return this.partyQuestInfo;
    }

    public Map<Integer, Integer> getQuestItems() {
        return this.questItems;
    }

    public boolean isAutoStart() {
        return this.autoStart;
    }

    public boolean isAutoPreComplete() {
        return this.autoPreComplete;
    }

    public boolean isRepeatable() {
        return this.repeatable;
    }

    public boolean isAutoAccept() {
        return this.autoAccept;
    }

    public boolean isAutoComplete() {
        return this.autoComplete;
    }

    public int getViewMedalItem() {
        return this.viewMedalItem;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public void setAutoPreComplete(boolean autoPreComplete) {
        this.autoPreComplete = autoPreComplete;
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public void setAutoAccept(boolean autoAccept) {
        this.autoAccept = autoAccept;
    }

    public void setAutoComplete(boolean autoComplete) {
        this.autoComplete = autoComplete;
    }

    public void setSelfStart(boolean selfStart) {
        this.selfStart = selfStart;
    }

    public void setViewMedalItem(int viewMedalItem) {
        this.viewMedalItem = viewMedalItem;
    }

    public void setSelectedSkillID(int selectedSkillID) {
        this.selectedSkillID = selectedSkillID;
    }

    @Override
    public String toString() {
        return getName() + "(" + getId() + ")";
    }
}
