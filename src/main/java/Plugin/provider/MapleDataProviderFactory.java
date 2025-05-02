package Plugin.provider;

import Config.configs.Config;
import Plugin.provider.nx.NXFileDataProvider;
import Plugin.provider.wz.WzFileDataProvider;
import Plugin.provider.wz.WzFileMapleData;
import Plugin.provider.wz.WzIMGFile;
import Plugin.provider.xml.XMLFileDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.data.BufferedRandomAccessFile;
import tools.data.MaplePacketReader;
import tools.data.RandomAccessByteStream;

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

public final class MapleDataProviderFactory {
    private static final Logger log = LoggerFactory.getLogger(MapleDataProviderFactory.class);
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
                } else if (magic.equalsIgnoreCase("PKG4")) {
                    fileData = new NXFileDataProvider(in);
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
        return getWZ("Skill");
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
            log.info("Hotfix檔案不存在, 忽略Hotfix資料{}", file.getPath());
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
