/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Config.configs;

import Config.constants.ServerConstants;
import Net.server.maps.MapleQuickMove;
import Server.channel.ChannelServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Randomizer;
import tools.config.PropertiesUtils;
import tools.json.JSONArray;
import tools.json.JSONObject;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * @author zisedk
 */
public class Config {

    private static final String dirpath = "./config";
    private static final Logger log = LoggerFactory.getLogger(Config.class);
    private static final List<Properties> props = new ArrayList<>();

    /**
     * 加載配置文件
     */

    public static void load() {
        File dir = new File(dirpath);
        if (dir.exists() && dir.isDirectory()) {
            try {
                props.clear();
                props.addAll(Arrays.asList(PropertiesUtils.loadAllFromDirectory(dirpath)));
            } catch (Exception e) {
                System.err.println("加載配置文件出現錯誤" + e.getMessage());
                throw new Error("加載配置文件出現錯誤.", e);
            }
        }
        Properties[] propsArr = new Properties[props.size()];
        props.toArray(propsArr);
        ConfigurableProcessor2.process(ServerConfig.class, propsArr);
        ConfigurableProcessor2.process(CSInfoConfig.class, propsArr);
        ConfigurableProcessor2.process(FishingConfig.class, propsArr);
        ConfigurableProcessor2.process(NebuliteConfig.class, propsArr);
        ConfigurableProcessor2.process(CubeConfig.class, propsArr);
        ConfigurableProcessor2.process(EquipConfig.class, propsArr);
        ConfigurableProcessor2.process(FireRangbConfig.class, propsArr);
        ConfigurableProcessor2.process(MvpEquipConfig.class, propsArr);
        if (Config.isDevelop()) {
            ConfigurableProcessor2.process(OpcodeConfig.class, propsArr);
            OpcodeConfig.load();
        }

        ServerConstants.loadBlockedMapFM();
        BossConfig.load();
        try {
            JSONObject object = new JSONObject(ServerConfig.WORLD_HIDENPCS);
            ServerConfig.WORLD_HIDENPCS_MAP.clear();
            for (String mapId : object.keySet()) {
                JSONArray array = object.getJSONArray(mapId);
                Set<Integer> set = new HashSet<>();
                for (int i : array.toIntArray()) {
                    set.add(i);
                }
                ServerConfig.WORLD_HIDENPCS_MAP.put(Integer.valueOf(mapId), set);
            }
        } catch (Exception ex) {
            System.out.println("隱藏的NPC配置格式錯誤，該配置未生效。錯誤訊息：" + ex);
        }
        ServerConfig.CHEAT_ITEM_EXCLUDES_LIST.clear();
        for (String itemId : ServerConfig.CHEAT_ITEM_EXCLUDES.split(",")) {
            if (itemId.isEmpty() || !itemId.matches("^\\d+$")) {
                continue;
            }
            ServerConfig.CHEAT_ITEM_EXCLUDES_LIST.add(Integer.valueOf(itemId));
        }
        ServerConfig.WORLD_LIMITEDNAMES_LIST.clear();
        for (String limitedname : ServerConfig.WORLD_LIMITEDNAMES.split(",")) {
            if (limitedname.isEmpty()) {
                continue;
            }
            ServerConfig.WORLD_LIMITEDNAMES_LIST.add(limitedname);
        }
        ServerConfig.CAN_CUT_ITEMS_LIST.clear();
        for (String itemId : ServerConfig.CAN_CUT_ITEMS.split(",")) {
            if (itemId.isEmpty() || !itemId.matches("^\\d+$")) {
                continue;
            }
            ServerConfig.CAN_CUT_ITEMS_LIST.add(Integer.valueOf(itemId));
        }
        ServerConfig.ACCOUNT_SHARE_ITEMS_LIST.clear();
        for (String itemId : ServerConfig.ACCOUNT_SHARE_ITEMS.split(",")) {
            if (itemId.isEmpty() || !itemId.matches("^\\d+$")) {
                continue;
            }
            ServerConfig.ACCOUNT_SHARE_ITEMS_LIST.add(Integer.valueOf(itemId));
        }
        ServerConfig.BLOCK_CHAIRS_SET.clear();
        for (String itemId : ServerConfig.BLOCK_CHAIRS.split(",")) {
            if (itemId.isEmpty() || !itemId.matches("^\\d+$")) {
                continue;
            }
            ServerConfig.BLOCK_CHAIRS_SET.add(Integer.valueOf(itemId));
        }
        try {
            JSONObject object = new JSONObject(ServerConfig.ITEM_MAXSLOT);
            ServerConfig.ITEM_MAXSLOT_MAP.clear();
            for (String itemId : object.keySet()) {
                ServerConfig.ITEM_MAXSLOT_MAP.put(Randomizer.rand(2000000, 5999999), (short) object.getInt(String.valueOf(itemId)));
            }
        } catch (Exception ex) {
            System.out.println("道具可堆疊最大數量配置格式錯誤，該配置未生效。錯誤訊息：" + ex);
            throw new Error("道具可堆疊最大數量配置格式錯誤.", ex);
        }
        MvpEquipConfig.RentEquipListJson = new JSONArray(MvpEquipConfig.RentEquipList);
        MvpEquipConfig.RentMvpEquipListJson = new JSONArray(MvpEquipConfig.RentMvpEquipList);
        MvpEquipConfig.MvpEquipMakeListJson = new JSONArray(MvpEquipConfig.MvpEquipMakeList);
        MvpEquipConfig.EnhanceItem.clear();
        for (String itemId : MvpEquipConfig.EnhanceItemList.split(",")) {
            if (itemId.isEmpty() || !itemId.matches("^\\d+$")) {
                continue;
            }
            MvpEquipConfig.EnhanceItem.add(Integer.valueOf(itemId));
        }
        MvpEquipConfig.EnhanceCosts.clear();
        for (String itemId : MvpEquipConfig.EnhanceCost.split(",")) {
            if (itemId.isEmpty() || !itemId.matches("^\\d+$")) {
                continue;
            }
            MvpEquipConfig.EnhanceCosts.add(Integer.valueOf(itemId));
        }
        MvpEquipConfig.EnhanceRates.clear();
        for (String itemId : MvpEquipConfig.EnhanceRate.split(",")) {
            if (itemId.isEmpty() || !itemId.matches("^\\d+$")) {
                continue;
            }
            MvpEquipConfig.EnhanceRates.add(Integer.valueOf(itemId));
        }
        try {
            Path path = Paths.get("config", "quickmove.json");
            if (Files.exists(path)) {
                JSONArray jsonArray = new JSONArray(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
                ServerConfig.QUICK_MOVE_LIST.clear();
                MapleQuickMove mqm;
                for (Object o : jsonArray) {
                    assert o instanceof JSONObject;
                    JSONObject jsonObject = (JSONObject) o;
                    mqm = new MapleQuickMove();
                    mqm.VALUE = jsonObject.getInt("VALUE");
                    mqm.NPC = jsonObject.getInt("NPC");
                    mqm.SCRIPT = jsonObject.getString("SCRIPT");
                    mqm.MIN_LEVEL = jsonObject.getInt("MIN_LEVEL");
                    mqm.DESC = jsonObject.getString("DESC");
                    if (jsonObject.has("CLOSE_AFTER_CLICK")) {
                        mqm.CLOSE_AFTER_CLICK = jsonObject.getBoolean("CLOSE_AFTER_CLICK");
                    }
                    if (jsonObject.has("TESTPIA")) {
                        mqm.TESTPIA = jsonObject.getBoolean("TESTPIA");
                    }
                    if (jsonObject.has("GM_LEVEL")) {
                        mqm.GM_LEVEL = jsonObject.getInt("GM_LEVEL");
                    }
                    ServerConfig.QUICK_MOVE_LIST.add(mqm);
                }
                for (ChannelServer cs : ChannelServer.getAllInstances()) {
                    if (cs == null || cs.getMapFactory() == null) {
                        continue;
                    }
                    cs.getMapFactory().loadQuickMove();
                }
            } else {
                System.out.println("未讀取quickmove.json配置, 快速移動將使用預設值");
            }
        } catch (Exception ex) {
            System.out.println("讀取快速移動配置錯誤，快速移動將使用預設值。錯誤訊息：" + ex);
        }
        ServerConfig.noEncryptHost_List.clear();
        for (String host : ServerConfig.noEncryptHosts.split(",")) {
            if (host.isEmpty()) {
                continue;
            }
            ServerConfig.noEncryptHost_List.add(host);
        }
        ServerConfig.TeachCost.clear();
        for (String cost : ServerConfig.TeachCostData.split(",")) {
            if (cost.isEmpty()) {
                continue;
            }
            ServerConfig.TeachCost.add(Integer.parseInt(cost));
        }
    }

    public static String getProperty(String key, String defaultValue) {
        String ret = defaultValue;
        for (Properties prop : props) {
            if (prop.containsKey(key)) {
                ret = prop.getProperty(key);
            }
        }
        return ret;
    }

    public static void setProperty(String key, String value) {
        boolean found = false;
        for (Properties prop : props) {
            if (prop.containsKey(key)) {
                prop.setProperty(key, value);
                changeFiles(key, value);
                found = true;
            }
        }
        if (!found) {
            changeFiles(key, value);
        }
    }

    private static void changeFiles(String key, String value) {
        File root = new File(dirpath);
        try {
            if (!root.exists()) {
                root.mkdir();
            }
            List<File> files = PropertiesUtils.getAllPropertiesFiles(root);
            String defaultName = "settings.properties";
            boolean found = false;
            for (File file : files) {
                found = file.getName().equalsIgnoreCase(defaultName);
                if (found) {
                    break;
                }
            }
            if (files.isEmpty() || !found) {
                File settings = new File(dirpath + "/" + defaultName);
                settings.createNewFile();
                files.add(settings);
            }
            found = false;
            for (File file : files) {
                found = found || changeFiles(file, key, value, !found && file.getName().equalsIgnoreCase(defaultName));
            }
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void LoadALLSkill(String key, String value) {
        File root = new File(dirpath);
        try {
            if (!root.exists()) {
                root.mkdir();
            }
            List<File> files = PropertiesUtils.getAllPropertiesFiles(root);
            String defaultName = "DataBase.properties";
            boolean found = false;
            for (File file : files) {
                found = file.getName().equalsIgnoreCase(defaultName);
                if (found) {
                    break;
                }
            }
            if (files.isEmpty() || !found) {
                File settings = new File(dirpath + "/" + defaultName);
                settings.createNewFile();
                files.add(settings);
            }
            found = false;
            for (File file : files) {
                found = found || changeFiles(file, key, value, !found && file.getName().equalsIgnoreCase(defaultName));
            }
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }
    }


    private static void bossHpSetting(String key, String value) {
        File root = new File(dirpath);
        try {
            if (!root.exists()) {
                root.mkdir();
            }
            List<File> files = PropertiesUtils.getAllPropertiesFiles(root);
            String defaultName = "BossHpSetting.properties";
            boolean found = false;
            for (File file : files) {
                found = file.getName().equalsIgnoreCase(defaultName);
                if (found) {
                    break;
                }
            }
            if (files.isEmpty() || !found) {
                File settings = new File(dirpath + "/" + defaultName);
                settings.createNewFile();
                files.add(settings);
            }
            found = false;
            for (File file : files) {
                PropertiesUtils.getAllPropertiesFiles(file);
                found = true;
            }
            if (!found) {
                System.out.println("Failed to find or create BossHpSetting.properties file.");
            }
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void RuneConfig(String key, String value) {
        File root = new File(dirpath);
        try {
            if (!root.exists()) {
                root.mkdir();
            }
            List<File> files = PropertiesUtils.getAllPropertiesFiles(root);
            String defaultName = "RuneConfig.properties";
            boolean found = false;
            for (File file : files) {
                found = file.getName().equalsIgnoreCase(defaultName);
                if (found) {
                    break;
                }
            }
            if (files.isEmpty() || !found) {
                File settings = new File(dirpath + "/" + defaultName);
                settings.createNewFile();
                files.add(settings);
            }
            found = false;
            for (File file : files) {
                PropertiesUtils.getAllPropertiesFiles(file);
                found = true;
            }
            if (!found) {
                System.out.println("Failed to find or create BossHpSetting.properties file.");
            }
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void BroubRang(String key, String value) {
        File root = new File(dirpath);
        try {
            if (!root.exists()) {
                root.mkdir();
            }
            List<File> files = PropertiesUtils.getAllPropertiesFiles(root);
            String defaultName = "FireRang.properties";
            boolean found = false;
            for (File file : files) {
                found = file.getName().equalsIgnoreCase(defaultName);
                if (found) {
                    break;
                }
            }
            if (files.isEmpty() || !found) {
                File settings = new File(dirpath + "/" + defaultName);
                settings.createNewFile();
                files.add(settings);
            }
            found = false;
            for (File file : files) {
                PropertiesUtils.getAllPropertiesFiles(file);
                found = true;
            }
            if (!found) {
                System.out.println("Failed to find or create BossHpSetting.properties file.");
            }
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void BossHpSetting(String key, String value) {
        File root = new File(dirpath);
        try {
            if (!root.exists()) {
                root.mkdir();
            }
            List<File> files = PropertiesUtils.getAllPropertiesFiles(root);
            String defaultName = "BossHpConfig.properties";
            boolean found = false;
            for (File file : files) {
                found = file.getName().equalsIgnoreCase(defaultName);
                if (found) {
                    break;
                }
            }
            if (files.isEmpty() || !found) {
                File settings = new File(dirpath + "/" + defaultName);
                settings.createNewFile();
                files.add(settings);
            }
            found = false;
            for (File file : files) {
                PropertiesUtils.getAllPropertiesFiles(file);
                found = true;
            }
            if (!found) {
                System.out.println("Failed to find or create BossHpSetting.properties file.");
            }
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }
    }


    private static boolean changeFiles(File file, String key, String value, boolean add) {
        boolean found = false;
        if (file.isFile()) {
            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                while ((line = br.readLine()) != null) {
                    if (line.startsWith(key)) {
                        sb.append(key);
                        sb.append("=");
                        sb.append(value);
                        sb.append("\r\n");
                        found = true;
                        continue;
                    }
                    sb.append(line);
                    sb.append("\r\n");
                }
                if (add && !found) {
                    sb.append(key);
                    sb.append("=");
                    sb.append(value);
                    sb.append("\r\n");
                    found = true;
                }
                if (found) {
                    try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                        bw.write(sb.toString());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return found;
    }

    public static int getServerBuildVersion() {
        Date d = null;
        Class<?> currentClass = new Object() {
        }.getClass().getEnclosingClass();
        URL resource = currentClass.getResource(currentClass.getSimpleName() + ".class");
        if (resource != null) {
            if (resource.getProtocol().equals("file")) {
                try {
                    d = new Date(new File(resource.toURI()).lastModified());
                } catch (URISyntaxException ignored) {
                }
            } else if (resource.getProtocol().equals("jar")) {
                String path = resource.getPath();
                d = new Date(new File(path.substring(5, path.indexOf("!"))).lastModified());
            } else if (resource.getProtocol().equals("zip")) {
                String path = resource.getPath();
                File jarFileOnDisk = new File(path.substring(0, path.indexOf("!")));
                try (JarFile jf = new JarFile(jarFileOnDisk)) {
                    ZipEntry ze = jf.getEntry(path.substring(path.indexOf("!") + 2));
                    long zeTimeLong = ze.getTime();
                    Date zeTimeDate = new Date(zeTimeLong);
                    d = zeTimeDate;
                } catch (IOException | RuntimeException ignored) {
                }
            } else {
                // 新增相容 native-image 的獲取檔案最後編輯時間的方法
                String fileString = currentClass.getProtectionDomain()
                        .getCodeSource()
                        .getLocation().getFile();
                d = new Date(new File(fileString).lastModified());
            }
        }
        return Integer.valueOf(new SimpleDateFormat("yyyyMMdd").format(d));
    }

    public static boolean isDevelop() {
        return "dev".equalsIgnoreCase(System.getProperty("debug"));
    }

    public static boolean isOpenDefaultScript() {
        return true;
    }
}
