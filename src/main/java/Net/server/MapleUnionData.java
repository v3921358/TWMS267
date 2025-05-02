package Net.server;

import Client.MapleUnionBoardEntry;
import Config.constants.JobConstants;
import Plugin.provider.MapleData;
import Plugin.provider.MapleDataProviderFactory;
import Plugin.provider.MapleDataTool;
import tools.Triple;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * create by Ethan on 20170811
 *
 * @author Ethan
 */
public class MapleUnionData {

    private final Map<Integer, MapleUnionBoardEntry> BoardInfo = new HashMap<>();
    private final Map<Integer, Map<Integer, Map<Integer, Point>>> sizeInfo = new HashMap<>();
    private final Map<Integer, Integer> skillInfo = new HashMap<>();
    private final Map<Integer, Integer> cardInfo = new HashMap<>();
    private final Map<Integer, Map<Integer, MapleUnionRankData>> rankInfo = new HashMap<>();

    public static MapleUnionData getInstance() {
        return InstanceHolder.instance;
    }

    public Map<Integer, Map<Integer, MapleUnionRankData>> getRankInfo() {
        return rankInfo;
    }

    public Map<Integer, Point> getSizeInfo(int jobGrade, int characterRank) {
        return sizeInfo.containsKey(jobGrade) ? sizeInfo.get(jobGrade).getOrDefault(characterRank, null) : null;
    }

    public Map<Integer, MapleUnionBoardEntry> getBoardInfo() {
        return BoardInfo;
    }

    public Triple<Integer, Integer, Integer> getCardInfo(int job, int level) {
        if (cardInfo.containsKey(job / 10)) {
            int skillId = cardInfo.get(job / 10);
            if (skillId > 0) {
                return new Triple<>((skillId - 71000000), skillId, getCharacterRank(job, level));
            }
        }
        return null;
    }

    public int getCharacterRank(int job, int level) {
        if (JobConstants.is神之子(job)) {
            if (level >= 130 && level < 160) {
                return 1;
            }
            if (level >= 160 && level < 180) {
                return 2;
            }
            if (level >= 180 && level < 200) {
                return 3;
            }
            if (level >= 200 && level < 250) {
                return 4;
            }
            if (level >= 250) {
                return 5;
            }
        } else {
            if (level >= 60 && level < 100) {
                return 1;
            }
            if (level >= 100 && level < 140) {
                return 2;
            }
            if (level >= 140 && level < 200) {
                return 3;
            }
            if (level >= 200 && level < 250) {
                return 4;
            }
            if (level >= 250) {
                return 5;
            }
        }
        return -1;
    }

    public void init() {
        MapleData unionData = MapleDataProviderFactory.getEtc().getData("mapleUnion.img");
        for (MapleData boardData : unionData.getChildByPath("BoardInfo")) {
            BoardInfo.put(Integer.parseInt(boardData.getName()), new MapleUnionBoardEntry(MapleDataTool.getInt("xPos", boardData, 0), MapleDataTool.getInt("yPos", boardData, 0), MapleDataTool.getInt("changeable", boardData, 0) > 0, MapleDataTool.getInt("groupIndex", boardData, 0), MapleDataTool.getInt("openLevel", boardData, 0)));
        }
        for (MapleData sizeData : unionData.getChildByPath("CharacterSize")) {
            int jobGrade = Integer.parseInt(sizeData.getName());
            Map<Integer, Map<Integer, Point>> map = sizeInfo.computeIfAbsent(jobGrade, key -> new HashMap<>());
            for (MapleData rankData : sizeData) {
                int rank = Integer.parseInt(rankData.getName());
                Map<Integer, Point> map2 = map.computeIfAbsent(rank, key -> new HashMap<>());
                for (MapleData info : rankData) {
                    map2.put(Integer.parseInt(info.getName()), MapleDataTool.getPoint(info));
                }
            }
        }
        for (MapleData skillData : unionData.getChildByPath("SkillInfo")) {
            skillInfo.put(Integer.parseInt(skillData.getName()), MapleDataTool.getInt("skillID", skillData, 0));
        }
        for (MapleData cardData : unionData.getChildByPath("Card")) {
            cardInfo.put(Integer.parseInt(cardData.getName()), MapleDataTool.getInt("skillID", cardData, 0));
        }
        for (MapleData rankData : unionData.getChildByPath("unionRank")) {
            String name = MapleDataTool.getString(rankData.getChildByPath("info/name"));
            Map<Integer, MapleUnionRankData> map = new HashMap<>();
            int rank = Integer.parseInt(rankData.getName());
            for (MapleData info : rankData) {
                if (!info.getName().equals("info")) {
                    int grade = Integer.parseInt(info.getName());
                    map.put(grade, new MapleUnionRankData(name, rank, grade, MapleDataTool.getInt("level", info, 0), MapleDataTool.getInt("attackerCount", info, 0), MapleDataTool.getInt("coinStackMax", info, 0)));
                }
            }
            rankInfo.put(rank, map);
        }
    }

    public class MapleUnionRankData {
        private final String name;
        private final int level;
        private final int attackerCount;
        private final int coinStackMax;
        private final int subRank;
        private final int rank;

        public MapleUnionRankData(String name, int rank, int grade, int level, int attackerCount, int coinStackMax) {
            this.name = name;
            this.level = level;
            this.attackerCount = attackerCount;
            this.coinStackMax = coinStackMax;
            this.subRank = grade;
            this.rank = rank;
        }

        public int getRank() {
            return rank;
        }

        public int getAttackerCount() {
            return this.attackerCount;
        }

        public int getCoinStackMax() {
            return coinStackMax;
        }

        public int getSubRank() {
            return subRank;
        }

        public int getLevel() {
            return this.level;
        }

        @Override
        public String toString() {
            return name + subRank + "階段";
        }
    }


    private static class InstanceHolder {
        private static final MapleUnionData instance = new MapleUnionData();
    }
}
