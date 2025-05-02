package Plugin.provider.loaders;

import Config.configs.Config;
import Config.constants.ServerConstants;
import Net.server.MapleStatInfo;
import Plugin.provider.*;
import SwordieX.client.character.skills.Skill;
import SwordieX.util.Util;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillData {
    private static final Logger log = LoggerFactory.getLogger(SkillData.class);
    private static MapleDataProvider datasource;
    @Getter
    private static Map<Integer, Skill> skills = new HashMap<>();

    public static void saveSkills(String dir) {
        Util.makeDirIfAbsent(dir);
        File file = new File(String.format("%s/skills.dat", dir));
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            return;
        }
        try (DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file))) {
            dataOutputStream.writeInt(skills.size());
            for (Skill skill : skills.values()) {
                //儲存技能ID
                dataOutputStream.writeInt(skill.getId());
                dataOutputStream.writeInt(skill.getMasterLevel());
                dataOutputStream.writeInt(skill.getFixLevel());
                dataOutputStream.writeInt(skill.getHyper());
                dataOutputStream.writeBoolean(skill.isPsd());
                dataOutputStream.writeBoolean(skill.isCanNotStealableSkill());
                dataOutputStream.writeBoolean(skill.isInvisible());
                dataOutputStream.writeBoolean(skill.isPetAutoBuff());
                dataOutputStream.writeBoolean(skill.isNotRemoved());
                dataOutputStream.writeUTF(skill.getElemAttr());
                // 儲存common
                dataOutputStream.writeInt(skill.getCommon().size());
                for (Map.Entry<MapleStatInfo, String> dat : skill.getCommon().entrySet()) {
                    dataOutputStream.writeUTF(dat.getKey().name());
                    dataOutputStream.writeUTF(dat.getValue());
                }
                // 儲存PVPcommon
                dataOutputStream.writeInt(skill.getPVPcommon().size());
                for (Map.Entry<MapleStatInfo, String> dat : skill.getPVPcommon().entrySet()) {
                    dataOutputStream.writeUTF(dat.getKey().name());
                    dataOutputStream.writeUTF(dat.getValue());
                }
                // 儲存info
                dataOutputStream.writeInt(skill.getInfo().size());
                for (Map.Entry<String, String> dat : skill.getInfo().entrySet()) {
                    dataOutputStream.writeUTF(dat.getKey());
                    dataOutputStream.writeUTF(dat.getValue());
                }
                // 儲存info2
                dataOutputStream.writeInt(skill.getInfo2().size());
                for (Map.Entry<String, String> dat : skill.getInfo2().entrySet()) {
                    dataOutputStream.writeUTF(dat.getKey());
                    dataOutputStream.writeUTF(dat.getValue());
                }
                // 儲存psdSkills
                dataOutputStream.writeInt(skill.getPsdSkills().size());
                for (int dat : skill.getPsdSkills()) {
                    dataOutputStream.writeInt(dat);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadSkill(File file) {
        if (!file.exists()) {
            loadDatFromWz();
        }
        try (DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file))) {
            // 讀取技能總數
            int maxcount = dataInputStream.readInt();
            for (int count = 0; count < maxcount; count++) {
                // 讀取技能ID
                int skillid = dataInputStream.readInt();
                Skill skill = new Skill(skillid);
                skill.setMasterLevel(dataInputStream.readInt());
                skill.setFixLevel(dataInputStream.readInt());
                skill.setHyper(dataInputStream.readInt());
                skill.setPsd(dataInputStream.readBoolean());
                skill.setCanNotStealableSkill(dataInputStream.readBoolean());
                skill.setInvisible(dataInputStream.readBoolean());
                skill.setPetAutoBuff(dataInputStream.readBoolean());
                skill.setNotRemoved(dataInputStream.readBoolean());
                skill.setElemAttr(dataInputStream.readUTF());

                // 讀取Common
                int commonSize = dataInputStream.readInt();
                for (int j = 0; j < commonSize; j++) {
                    String key = dataInputStream.readUTF();
                    String val = dataInputStream.readUTF();
                    try {
                        skill.getCommon().put(MapleStatInfo.valueOf(key), val);
                    } catch (Exception e) {
                        //System.out.println("未知的MapleInfoStat:" + stat);
                    }
                }
                // 讀取PVPCommon
                int commonPVPSize = dataInputStream.readInt();
                for (int j = 0; j < commonPVPSize; j++) {
                    String key = dataInputStream.readUTF();
                    String val = dataInputStream.readUTF();
                    try {
                        skill.getPVPcommon().put(MapleStatInfo.valueOf(key), val);
                    } catch (Exception e) {
                        //System.out.println("未知的MapleInfoStat:" + stat);
                    }
                }
                // 讀取Info
                int infoSize = dataInputStream.readInt();
                for (int j = 0; j < infoSize; j++) {
                    String key = dataInputStream.readUTF();
                    String val = dataInputStream.readUTF();
                    try {
                        skill.getInfo().put(key, val);
                    } catch (Exception e) {
                        //System.out.println("未知的MapleInfoStat:" + stat);
                    }
                }
                // 讀取Info2
                int info2Size = dataInputStream.readInt();
                for (int j = 0; j < info2Size; j++) {
                    String key = dataInputStream.readUTF();
                    String val = dataInputStream.readUTF();
                    try {
                        skill.getInfo2().put(key, val);
                    } catch (Exception e) {
                        //System.out.println("未知的MapleInfoStat:" + stat);
                    }
                }
                // 讀取psdSkills
                int psdSkillsSize = dataInputStream.readInt();
                for (int j = 0; j < psdSkillsSize; j++) {
                    int key = dataInputStream.readInt();
                    try {
                        skill.getPsdSkills().add(key);
                    } catch (Exception e) {
                        //System.out.println("未知的MapleInfoStat:" + stat);
                    }
                }
                skills.put(skillid, skill);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadSkillsFromWz() {

        Map<String, String> ss = new HashMap<>();
        Map<String, String> ii = new HashMap<>();
        Map<String, String> ii2 = new HashMap<>();
        Map<String, Map<String, List<Integer>>> ww = new HashMap<>();

        MapleDataDirectoryEntry root = datasource.getRoot();
        for (MapleDataFileEntry topDir : root.getFiles()) {
            if (topDir.getName().contains("Dragon") || topDir.getName().contains("MobSkill") || topDir.getName().contains("AbyssExpedition") || topDir.getName().contains("Roguelike")) {
                continue;
            }
            if (topDir.getName().length() <= 10) {
                for (MapleData data : datasource.getData(topDir.getName())) {
                    if (data.getName().equals("skill")) {
                        for (MapleData skillData : data.getChildren()) {
                            if (skillData != null) {

                                int skillid = Integer.parseInt(skillData.getName());
                                Skill oSkill = new Skill(skillid);
                                for (MapleData skill : skillData.getChildren()) {
                                    switch (skill.getName()) {
                                        case "PVPcommon": {
                                            for (MapleData dat : skill.getChildren()) {
                                                if (!ss.containsKey(dat.getName())) {
                                                    ss.put(dat.getName(), "");
                                                }
                                                try {
                                                    oSkill.getPVPcommon().put(MapleStatInfo.valueOf(dat.getName()), dat.getData().toString());
                                                } catch (Exception e) {
//                                                System.out.println("未處理的PVPcommon:" + dat.getName());
                                                }

                                            }
                                            break;
                                        }
                                        case "common": {
                                            for (MapleData dat : skill.getChildren()) {
                                                if (!ss.containsKey(dat.getName())) {
                                                    ss.put(dat.getName(), "");
                                                }
                                                try {
                                                    oSkill.getCommon().put(MapleStatInfo.valueOf(dat.getName()), dat.getData().toString());
                                                } catch (Exception e) {
                                                    //System.out.println("未處理的common:" + dat.getName());
                                                }
                                            }
                                            break;
                                        }
                                        case "info": {
                                            for (MapleData dat : skill.getChildren()) {
                                                if (!ii.containsKey(dat.getName())) {
                                                    ii.put(dat.getName(), "");
                                                }
                                                try {
                                                    oSkill.getInfo().put(dat.getName(), dat.getData().toString());
                                                } catch (Exception e) {
                                                    //System.out.println("未處理的info:" + skillData.getName());
                                                }
                                            }
                                            break;
                                        }
                                        case "info2": {
                                            for (MapleData dat : skill.getChildren()) {
                                                if (!ii2.containsKey(dat.getName())) {
                                                    ii2.put(dat.getName(), "");
                                                }
                                                try {
                                                    oSkill.getInfo2().put(dat.getName(), dat.getData().toString());
                                                } catch (Exception e) {
                                                    //System.out.println("未處理的info2:" + skillData.getName());
                                                }
                                            }
                                            break;
                                        }
                                        case "psd": {
                                            oSkill.setPsd(!skill.getData().equals("0"));
                                            break;
                                        }
                                        case "psdSkill": {
                                            for (MapleData dat : skill.getChildren()) {
                                                try {
                                                    oSkill.getPsdSkills().add(Integer.parseInt(dat.getName()));
                                                } catch (Exception e) {
                                                    //System.out.println("未處理的psdSkill:" + skillData.getName());
                                                }
                                            }
                                            break;
                                        }
                                        case "invisible": {
                                            oSkill.setInvisible(!skill.getData().equals("0"));
                                            break;
                                        }
                                        case "masterLevel": {
                                            oSkill.setMasterLevel(Integer.parseInt(skill.getData().toString()));
                                            break;
                                        }
                                        case "fixLevel": {
                                            oSkill.setFixLevel(Integer.parseInt(skill.getData().toString()));
                                            break;
                                        }
                                        case "notRemoved": {
                                            oSkill.setNotRemoved(!skill.getData().equals("0"));
                                            break;
                                        }
                                        case "canNotStealableSkill": {
                                            oSkill.setCanNotStealableSkill(!skill.getData().equals("0"));
                                            break;
                                        }
                                        case "isPetAutoBuff": {
                                            oSkill.setPetAutoBuff(!skill.getData().equals("0"));
                                            break;
                                        }
                                        case "hyper": {
                                            oSkill.setHyper(Integer.parseInt(skill.getData().toString()));
                                            break;
                                        }
                                        case "elemAttr": {
                                            oSkill.setElemAttr(skill.getData().toString());
                                            break;
                                        }
                                        case "weapon":
                                            if (!ww.containsKey(skill.getData().toString())) {
                                                Map<String, List<Integer>> ws = new HashMap<>();
                                                List<Integer> sks = new ArrayList<>();
                                                sks.add(skillid);
                                                ws.put(topDir.getName(), sks);
                                                ww.put(skill.getData().toString(), ws);
                                            } else {
                                                Map<String, List<Integer>> ws = ww.get(skill.getData().toString());
                                                if (!ws.containsKey(topDir.getName())) {
                                                    List<Integer> sks = new ArrayList<>();
                                                    sks.add(skillid);
                                                    ws.put(topDir.getName(), sks);
                                                } else {
                                                    ws.get(topDir.getName()).add(skillid);
                                                }
                                            }
                                            break;
                                        case "subWeapon":

                                            break;
                                        // 不處理或不知道功能的
                                        case "icon":
                                        case "iconMouseOver":
                                        case "iconDisabled":
                                        case "effect":
                                        case "effect0":
                                        case "hit":
                                        case "mob":
                                        case "special":
                                        case "affected":
                                        case "affected0":
                                        case "req":
                                        case "reqLev":
                                        case "skillType":
                                        case "disabledDuringAction":
                                        case "footholdInstallSummoned":
                                        case "footholdAffectedArea":
                                        case "excl":
                                        case "skillList":
                                        case "action":
                                        case "preloadEff":
                                            break;
                                        default: {
                                            //System.out.println("未處理的技能WZ屬性:" + skill.getName());
                                            break;
                                        }
                                    }
                                }
                                skills.put(skillid, oSkill);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void loadAllSkills() {
        long start = System.currentTimeMillis();
        String dir = ServerConstants.DAT_DIR + "/skills/skills.dat";
        File f = new File(dir);
        loadSkill(f);
        //System.out.println(String.format("Loaded %s skills common from data files in %dms.", getSkills().size(), System.currentTimeMillis() - start));
    }

    public static void generateDatFiles() {
        System.out.println("Started generating skill data.");
        long start = System.currentTimeMillis();
        loadSkillsFromWz();
        saveSkills(ServerConstants.DAT_DIR + "/skills");
        System.out.println(String.format("Completed generating skill data in %dms.", System.currentTimeMillis() - start));
        System.out.println("Started loading skill common data.");
        start = System.currentTimeMillis();
        System.out.println(String.format("Completed loaded skill common data in %dms.", System.currentTimeMillis() - start));
    }

    public static void main(String[] args) {
        Config.load();
        MapleDataProviderFactory.init();
        loadDatFromWz();
    }

    public static void loadDatFromWz() {
        datasource = MapleDataProviderFactory.getSkill();
        generateDatFiles();
    }

    public static void clear() {
        getSkills().clear();
    }
}
