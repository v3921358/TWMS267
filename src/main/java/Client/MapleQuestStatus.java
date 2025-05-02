package Client;

import Config.constants.GameConstants;
import Net.server.life.MapleLifeFactory;
import Net.server.quest.MapleQuest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class MapleQuestStatus {

    private MapleQuest quest;
    private byte status;
    private Map<Integer, Integer> killedMobs = null;
    private int npc;
    private long completionTime;
    private int forfeited = 0;
    private String customData;
    private int fromChrID = -1;

    public MapleQuestStatus(MapleQuest quest, int status) {
        this.quest = quest;
        this.setStatus((byte) status);
        this.completionTime = System.currentTimeMillis();
        if (status == 1 && !quest.getRelevantMobs().isEmpty()) {
            registerMobs();
        }
    }

    public MapleQuestStatus(MapleQuest quest, byte status, int npc) {
        this.quest = quest;
        this.setStatus(status);
        this.setNpc(npc);
        this.completionTime = System.currentTimeMillis();
        if (status == 1) { // 開始任務
            if (!quest.getRelevantMobs().isEmpty()) {
                registerMobs();
            }
        }
    }

    public MapleQuest getQuest() {
        return quest;
    }

    public void setQuest(int qid) {
        this.quest = MapleQuest.getInstance(qid);
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public int getNpc() {
        return npc;
    }

    public void setNpc(int npc) {
        this.npc = npc;
    }

    public boolean isCustom() {
        switch (quest.getId()) {
            case GameConstants.ENTER_CASH_SHOP:
            case GameConstants.CHECK_DAY:
            case GameConstants.OMOK_SCORE:
            case GameConstants.MATCH_SCORE:
            case GameConstants.HP_ITEM:
            case GameConstants.MP_ITEM:
            case GameConstants.BUFF_SKILL:
            case GameConstants.JAIL_TIME:
            case GameConstants.JAIL_QUEST:
            case GameConstants.REPORT_QUEST:
            case GameConstants.ULT_EXPLORER:
            case GameConstants.ENERGY_DRINK:
            case GameConstants.HARVEST_TIME:
            case GameConstants.PENDANT_SLOT:
            case GameConstants.CURRENT_SET:
            case GameConstants.BOSS_PQ:
            case GameConstants.JAGUAR:
            case GameConstants.PARTY_REQUEST:
            case GameConstants.PARTY_INVITE:
            case GameConstants.ALLOW_PET_LOOT:
            case GameConstants.QUICK_SLOT:
            case 99997:
                return true;
        }
        return false;
    }

    private void registerMobs() {
        killedMobs = new LinkedHashMap<>();
        for (int i : quest.getRelevantMobs().keySet()) {
            killedMobs.put(i, 0);
        }
    }

    private int maxMob(int mobid) {
        for (Entry<Integer, Integer> qs : quest.getRelevantMobs().entrySet()) {
            if (qs.getKey() == mobid) {
                return qs.getValue();
            }
        }
        return 0;
    }

    public boolean mobKilled(int id, int skillID) {
        if (quest != null && quest.getSelectedSkillID() > 0 && quest.getSelectedSkillID() != skillID) {
            return false;
        }
        Integer mob = killedMobs.get(id);
        if (mob != null) {
            int mo = maxMob(id);
            if (mob >= mo) {
                return false; //nothing happened
            }
            killedMobs.put(id, Math.min(mob + 1, mo));
            return true;
        }
        for (Entry<Integer, Integer> mo : killedMobs.entrySet()) {
            if (MapleLifeFactory.exitsQuestCount(mo.getKey(), id)) {
                int mobb = maxMob(mo.getKey());
                if (mo.getValue() >= mobb) {
                    return false; //nothing
                }
                killedMobs.put(mo.getKey(), Math.min(mo.getValue() + 1, mobb));
                return true;
            }
        }
        return false;
    }

    public void setMobKills(int id, int count) {
        if (killedMobs == null) {
            registerMobs();
        }
        killedMobs.put(id, count);
    }

    public boolean hasMobKills() {
        return killedMobs != null && killedMobs.size() > 0;
    }

    public int getMobKills(int id) {
        return killedMobs.getOrDefault(id, 0);
    }

    public Map<Integer, Integer> getMobKills() {
        return killedMobs;
    }

    public long getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(long completionTime) {
        this.completionTime = completionTime;
    }

    public int getForfeited() {
        return forfeited;
    }

    public void setForfeited(int forfeited) {
        if (forfeited >= this.forfeited) {
            this.forfeited = forfeited;
        } else {
            throw new IllegalArgumentException("Can't set forfeits to something lower than before.");
        }
    }

    public String getCustomData() {
        return customData;
    }

    public void setCustomData(String customData) {
        this.customData = customData;
    }

    public boolean isWorldShare() {
        return fromChrID > -1;
    }

    public int getFromChrID() {
        return fromChrID;
    }

    public void setFromChrID(int fromChrID) {
        this.fromChrID = fromChrID;
    }
}
