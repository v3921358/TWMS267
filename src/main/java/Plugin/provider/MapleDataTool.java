package Plugin.provider;

import Config.configs.Config;
import Plugin.provider.wz.WzFileDataProvider;
import Plugin.provider.wz.WzFileMapleData;
import Plugin.provider.wz.WzIMGFile;
import Plugin.provider.xml.XMLFileDataProvider;
import tools.StringUtil;
import tools.data.BufferedRandomAccessFile;
import tools.data.MaplePacketReader;
import tools.data.RandomAccessByteStream;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static Database.DatabaseLoader.scanner;

public final class MapleDataTool {

    public final class MapleDataProviderFactory {
        private static String WZPATH = Config.getProperty("wzpath", "Data");

        public static void init() {
            File in = new File(WZPATH);
            if (!in.exists()) {
                System.err.print("請輸入Wz所在的位置:");
                WZPATH = scanner.next().replace("\\", "\\\\");
                Config.setProperty("wzpath", WZPATH);
                init();
            }
        }

        private static final Map<File, MapleDataProvider> CACHE = new HashMap<>();

        private static MapleDataProvider getWZ(String name) {
            File in = new File(WZPATH, name);
            if (!in.exists()) {
                in = new File(WZPATH + File.separator + name + ".wz");
                if (!in.exists()) {
                    in = new File(WZPATH + File.separator + name + ".nx");
                    if (!in.exists()) {
                        throw new RuntimeException("檔案不存在" + in.getPath());
                    }
                }
            }
            if (CACHE.containsKey(in)) {
                return CACHE.get(in);
            }
            File checkFile;
            if (!in.getName().endsWith(".wz") && in.isDirectory() && name.matches("^([A-Za-z]+)$")) {
                checkFile = new File(in.getPath() + File.separator + in.getName() + ".wz");
                if (!checkFile.exists()) {
                    checkFile = new File(in.getPath() + File.separator + in.getName() + ".nx");
                    if (!checkFile.exists()) {
                        throw new RuntimeException("檔案不存在" + checkFile.getPath());
                    }
                }
            } else {
                checkFile = in;
            }
            MapleDataProvider fileData;
            if (checkFile.isDirectory()) {
                fileData = new XMLFileDataProvider(in);
            } else {
                try (BufferedRandomAccessFile raf = new BufferedRandomAccessFile(checkFile, "r")) {
                    MaplePacketReader lea = new MaplePacketReader(new RandomAccessByteStream(raf));
                    String magic = lea.readAsciiString(4);
                    raf.close();
                    if (magic.equalsIgnoreCase("PKG1")) {
                        fileData = new WzFileDataProvider(in);
                    } else {
                        throw new RuntimeException("不支援這個" + magic + "格式檔案" + checkFile.getPath());
                    }
                } catch (Exception e) {
                    throw new RuntimeException("讀取檔案時出錯", e);
                }
            }
            CACHE.put(in, fileData);
            return fileData;
        }

        public static MapleDataProvider getEffect() {
            return getWZ("Effect");
        }

        public static MapleDataProvider getItem() {
            return getWZ("Item");
        }

        public static MapleDataProvider getCharacter() {
            return getWZ("Character");
        }

        public static MapleDataProvider getSkill() {
            return getWZ("Packs/Skill");
        }

        public static MapleDataProvider getString() {
            return getWZ("String");
        }

        public static MapleDataProvider getEtc() {
            return getWZ("Etc");
        }

        public static MapleDataProvider getMob() {
            return getWZ("Mob");
        }

        public static MapleDataProvider getNpc() {
            return getWZ("Npc");
        }

        public static MapleDataProvider getMap() {
            return getWZ("Map");
        }

        public static MapleDataProvider getReactor() {
            return getWZ("Reactor");
        }

        public static MapleDataProvider getQuest() {
            return getWZ("Quest");
        }

        public static String HOTFIX_DATA_PATH = Config.getProperty("HotfixDataPath", "Hotfix/Data.wz");
        private static Map<String, MapleData> HotfixDataMap = null;
        private static String HotfixCheck = null;

        private static final Lock hotfixLoadLock = new ReentrantLock(true);

        public static void loadHotfixData() {
            HotfixDataMap = new LinkedHashMap<>();
            File file = new File(HOTFIX_DATA_PATH);
            if (!file.exists()) {
                System.err.println("Hotfix檔案不存在, 忽略Hotfix資料" + file.getPath());
                return;
            }
            try (FileInputStream in = new FileInputStream(file)) {
                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                byte[] buffer = new byte[1024 * 1024 * 1000];
                int len = 0;
                while ((len = in.read(buffer)) > 0) {
                    digest.update(buffer, 0, len);
                }
                String sha1 = new BigInteger(1, digest.digest()).toString(16);
                int length = 40 - sha1.length();
                if (length > 0) {
                    for (int i = 0; i < length; i++) {
                        sha1 = "0" + sha1;
                    }
                }
                HotfixCheck = sha1;
            } catch (Exception e) {
                e.getStackTrace();
            }

            try (BufferedRandomAccessFile raf = new BufferedRandomAccessFile(file, "r")) {
                MaplePacketReader lea = new MaplePacketReader(new RandomAccessByteStream(raf));
                byte magic = lea.readByte();
                raf.close();
                if (magic == 0x73) {
                    WzIMGFile img = new WzIMGFile(file.getPath(), new MapleDataFileEntry("", null, ""));
                    for (MapleData dat : img.getRoot().getChildren()) {
                        String s = dat.getPath();
                        MapleDataEntity dd = dat.getParent();
                        String name = dat.getName().replace("\\", "/");
                        if (dat instanceof WzFileMapleData) {
                            ((WzFileMapleData) dat).setName(name.substring(name.lastIndexOf("/") + 1));
                        }
                        HotfixDataMap.put(name, dat);
                    }
                } else {
                    System.err.println("Data.wz檔案不是正確的Hotfix檔案:" + file.getPath());
                }
            } catch (Exception e) {
                System.err.println("讀取檔案時出錯:" + file.getPath());
            }
        }

        public static Map<String, MapleData> getHotfixDatas() {
            hotfixLoadLock.lock();
            try {
                if (HotfixDataMap == null) {
                    loadHotfixData();
                }
                return new LinkedHashMap<>(HotfixDataMap);
            } finally {
                hotfixLoadLock.unlock();
            }
        }

        public static String getHotfixCheck() {
            hotfixLoadLock.lock();
            try {
                if (HotfixDataMap == null) {
                    loadHotfixData();
                }
                return HotfixCheck;
            } finally {
                hotfixLoadLock.unlock();
            }
        }
    }

    public static String getString(MapleData data) {
        return data == null ? null : ((String) data.getData());
    }

    public static String getString(MapleData data, String def) {
        if (data == null || data.getData() == null) {
            return def;
        } else if (data.getType() == MapleDataType.STRING || data.getData() instanceof String) {
            String ret = String.valueOf(data.getData());
            ret = ret.replace("&lt;", "<").replace("&amp;lt;", "<").replace("&gt;", ">").replace("黎", "黎");
            return ret;
        } else {
            return String.valueOf(getInt(data));
        }
    }

    public static String getString(String path, MapleData data) {
        return getString(data.getChildByPath(path));
    }

    public static String getString(String path, MapleData data, String def) {
        return getString(data == null || data.getChildByPath(path) == null ? null : data.getChildByPath(path), def);
    }

    public static double getDouble(MapleData data) {
        return (Double) data.getData();
    }

    public static float getFloat(MapleData data) {
        return (Float) data.getData();
    }

    public static float getFloat(MapleData data, float def) {
        if (data == null || data.getData() == null) {
            return def;
        } else {
            return (Float) data.getData();
        }
    }

    public static int getInt(MapleData data) {
        return Integer.valueOf(data.getData().toString());
    }

    public static int getInt(MapleData data, int def) {
        if (data == null || data.getData() == null) {
            return def;
        } else {
            if (data.getType() == MapleDataType.STRING) {
                String data_ = getString(data);
                if (data_.isEmpty()) {
                    data_ = "0";
                }
                return Integer.parseInt(data_);
            } else if (data.getType() == MapleDataType.SHORT) {
                return (int) (Short) data.getData();
            } else {
                return (Integer) data.getData();
            }
        }
    }

    public static int getInt(String path, MapleData data) {
        return getInt(data.getChildByPath(path));
    }

    public static int getIntConvert(MapleData data) {
        if (data.getType() == MapleDataType.STRING) {
            return Integer.parseInt(getString(data));
        } else {
            return getInt(data);
        }
    }

    public static int getIntConvert(String path, MapleData data) {
        MapleData d = data.getChildByPath(path);
        if (d.getType() == MapleDataType.STRING) {
            return Integer.parseInt(getString(d));
        } else {
            return getInt(d);
        }
    }

    public static int getInt(String path, MapleData data, int def) {
        if (data == null) {
            return def;
        }
        return getInt(data.getChildByPath(path), def);
    }

    public static int getIntConvert(String path, MapleData data, int def) {
        if (data == null) {
            return def;
        }
        return getIntConvert(data.getChildByPath(path), def);
    }

    public static int getIntConvert(MapleData d, int def) {
        if (d == null) {
            return def;
        }
        if (d.getType() == MapleDataType.STRING) {
            String dd = getString(d);
            if (dd.endsWith("%")) {
                dd = dd.substring(0, dd.length() - 1);
            }
            try {
                return Integer.parseInt(dd);
            } catch (NumberFormatException nfe) {
                return def;
            }
        } else {
            return getInt(d, def);
        }
    }

    public static long getLong(MapleData data) {
        return Long.valueOf(data.getData().toString());
    }

    public static long getLong(MapleData data, long def) {
        if (data == null || data.getData() == null) {
            return def;
        } else {
            if (data.getType() == MapleDataType.STRING) {
                String data_ = getString(data);
                if (data_.isEmpty()) {
                    data_ = "0";
                }
                return Long.parseLong(data_);
            } else {
                return ((Number) data.getData()).longValue();
            }
        }
    }

    public static long getLong(String path, MapleData data) {
        return getLong(data.getChildByPath(path));
    }

    public static long getLongConvert(MapleData data) {
        if (data.getType() == MapleDataType.STRING) {
            return Long.parseLong(getString(data));
        } else {
            return getLong(data);
        }
    }

    public static long getLongConvert(String path, MapleData data) {
        MapleData d = data.getChildByPath(path);
        if (d.getType() == MapleDataType.STRING) {
            return Long.parseLong(getString(d));
        } else {
            return getLong(d);
        }
    }

    public static long getLong(String path, MapleData data, long def) {
        if (data == null) {
            return def;
        }
        return getLong(data.getChildByPath(path), def);
    }

    public static long getLongConvert(String path, MapleData data, int def) {
        if (data == null) {
            return def;
        }
        return getLongConvert(data.getChildByPath(path), def);
    }

    public static long getLongConvert(MapleData d, int def) {
        if (d == null) {
            return def;
        }
        if (d.getType() == MapleDataType.STRING) {
            String dd = getString(d);
            if (dd.endsWith("%")) {
                dd = dd.substring(0, dd.length() - 1);
            }
            try {
                return Long.parseLong(dd);
            } catch (NumberFormatException nfe) {
                return def;
            }
        } else {
            return getLong(d, def);
        }
    }

    public static BufferedImage getImage(MapleData data) {
        return ((MapleCanvas) data.getData()).getImage();
    }

    public static Point getPoint(MapleData data) {
        return ((Point) data.getData());
    }

    public static Point getPoint(String path, MapleData data) {
        return getPoint(data.getChildByPath(path));
    }

    public static Point getPoint(String path, MapleData data, Point def) {
        MapleData pointData = data.getChildByPath(path);
        if (pointData == null) {
            return def;
        }
        return getPoint(pointData);
    }

    public static String getFullDataPath(MapleData data) {
        String path = "";
        MapleDataEntity myData = data;
        while (myData != null) {
            path = myData.getName() + "/" + path;
            myData = myData.getParent();
        }
        return path.substring(0, path.length() - 1);
    }

    public static Map<Object, Object> getAllMapleData(MapleData data) {
        Map<Object, Object> ret = new HashMap<>();
        for (MapleData subdata : data) {
            switch (subdata.getName()) {
                case "icon":
                case "iconRaw":
                    processIconData(ret, subdata);
                    continue;
            }
            ret.put(subdata.getName(), subdata.getChildren().isEmpty() ? subdata.getData() : getAllMapleData(subdata));
        }
        return ret;
    }

    private static void processIconData(Map<Object, Object> ret, MapleData subdata) {
        boolean isHash, isInLink, isOutLink;
        for (MapleData subdatum : subdata) {
            isHash = subdatum.getName().equals("_hash");
            isInLink = subdatum.getName().equals("_inlink");
            isOutLink = subdatum.getName().equals("_outlink");

            if (isHash) {
                ret.put(subdatum.getName(), String.valueOf(subdatum.getData()));
            } else if (isInLink || isOutLink) {
                int inlink = extractInLink(subdatum.getData().toString(), isInLink);
                if (inlink != 0) {
                    ret.put(subdatum.getName(), inlink);
                }
            }
        }
    }

    private static int extractInLink(String data, boolean isInLink) {
        int inlink = 0;
        String[] split = data.replace(".img", "").split("/");
        for (int i = 0; i < split.length; i++) {
            if ((isInLink && i == 0 || !isInLink && (i == 2 || i == 3)) && StringUtil.isNumber(split[i])) {
                inlink = Integer.parseInt(split[i]);
            }
        }
        return inlink;
    }
}
