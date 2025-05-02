package Client.hexa;

import Plugin.provider.MapleData;
import Plugin.provider.MapleDataProviderFactory;
import Plugin.provider.MapleDataTool;

import java.util.HashMap;
import java.util.Map;

public class HexaFactory {
    private static final MapleData hexaCoreData = MapleDataProviderFactory.getEtc().getData("HexaCore.img");
    private static final Map<Integer, HexaSkillCoreEntry> hexaSkillList = new HashMap<>();
    private static final Map<Integer, HexaStatCoreEntry> hexaMainStatList = new HashMap<>();
    private static final Map<Integer, HexaStatCoreEntry> hexaAdditionalStatList = new HashMap<>();

    public static void loadAllHexaSkill() {
        MapleData hexaSkillData = hexaCoreData.getChildByPath("hexaSkill");

        if (hexaSkillData != null) {
            for (MapleData c : hexaSkillData.getChildByPath("coreData")) {
                hexaSkillList.put(Integer.parseInt(c.getName()), new HexaSkillCoreEntry(Integer.parseInt(c.getName()), MapleDataTool.getInt("0", c.getChildByPath("connectSkill"), 0), MapleDataTool.getInt("type", c, 0), MapleDataTool.getInt("maxLevel", c, 0)));
            }
        } else {
            System.out.println("載入hexaSkill失敗");
        }

        MapleData hexaStatData = hexaCoreData.getChildByPath("hexaStat");

        if (hexaStatData != null) {
            MapleData main = hexaStatData.getChildByPath("stat").getChildByPath("main");
            MapleData additional = hexaStatData.getChildByPath("stat").getChildByPath("additional");
            int maxLevel = MapleDataTool.getInt("maxLevel", main, 0);
            for (MapleData c : main.getChildByPath("type")) {
                Map<Integer, Integer> levelData = new HashMap<>();
                for (MapleData level : c.getChildByPath("level")) {
                    levelData.put(Integer.parseInt(level.getName()), MapleDataTool.getInt(level.getChildren().get(0), 0));
                }
                String stat = c.getChildByPath("level").getChildByPath("0").getChildren().get(0).getName();
                hexaMainStatList.put(Integer.parseInt(c.getName()), new HexaStatCoreEntry(Integer.parseInt(c.getName()), maxLevel, stat, levelData));
            }
            maxLevel = MapleDataTool.getInt("maxLevel", additional, 0);
            for (MapleData c : additional.getChildByPath("type")) {
                Map<Integer, Integer> levelData = new HashMap<>();
                for (MapleData level : c.getChildByPath("level")) {
                    levelData.put(Integer.parseInt(level.getName()), MapleDataTool.getInt(level.getChildren().get(0), 0));
                }
                String stat = c.getChildByPath("level").getChildByPath("0").getChildren().get(0).getName();
                hexaAdditionalStatList.put(Integer.parseInt(c.getName()), new HexaStatCoreEntry(Integer.parseInt(c.getName()), maxLevel, stat, levelData));
            }
        } else {
            System.out.println("載入hexaSkill失敗");
        }

    }

    public static HexaSkillCoreEntry getHexaSkills(int id) {
        return hexaSkillList.get(id);
    }

    public static HexaStatCoreEntry getHexaMainStats(int type) {
        return hexaMainStatList.get(type);
    }

    public static HexaStatCoreEntry getAdditionalHexaStats(int type) {
        return hexaAdditionalStatList.get(type);
    }

}
