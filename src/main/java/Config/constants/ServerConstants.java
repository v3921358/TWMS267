package Config.constants;

import Config.configs.ServerConfig;
import Server.login.JobType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerConstants {

    // 伺服器版本號
    public static final short MapleMajor = 267;
    // 伺服器區域
    public static final String DIR = System.getProperty("user.dir");
    public static final String WZ_DIR = DIR + "/Data";

    // Wz Base to dat.
    public static final String DAT_DIR = DIR + "/dat";
    public static final String RESOURCES_DIR = DIR + "/resources";

    public static byte MapleRegion = 6;

    // 伺服器封包加密密鑰
    public static String OpcodeEncryptionKey = "BrN=r54jQp2@yP6G";
    public static String GamaniaServerIP = "202.80.104.28";
    public static String GamaniaServerIP_1 = "202.80.104.139";
    public static String GamaniaServerIP_2 = "202.80.104.139";
    public static String GamaniaServerIP_3 = "202.80.104.140";
    public static String GamaniaServerIP_4 = "202.80.104.140";
    public static String GamaniaServerIP_5 = "202.80.104.141";
    public static String GamaniaServerIP_6 = "202.80.104.141";
    public static String GamaniaServerIP_7 = "202.80.104.142";
    public static String GamaniaServerIP_8 = "202.80.104.142";
    public static String GamaniaServerIP_9 = "202.80.104.143";
    public static String GamaniaServerIP_10 = "202.80.104.143";
    public static String GamaniaServerIP_11 = "202.80.104.144";
    public static String GamaniaServerIP_12 = "202.80.104.144";
    public static String GamaniaServerIP_13 = "202.80.104.145";
    public static String GamaniaServerIP_14 = "202.80.104.145";
    public static String GamaniaServerIP_15 = "202.80.104.146";
    public static String GamaniaServerIP_16 = "202.80.104.146";
    public static String GamaniaServerIP_17 = "202.80.104.147";
    public static String GamaniaServerIP_18 = "202.80.104.147";
    public static String GamaniaServerIP_19 = "202.80.104.148";
    public static String GamaniaServerIP_20 = "202.80.104.148";
    public static String GamaniaServerIP_21 = "202.80.104.149";
    public static String GamaniaServerIP_22 = "202.80.104.149";
    public static String GamaniaServerIP_23 = "202.80.104.150";
    public static String GamaniaServerIP_24 = "202.80.104.150";
    public static String GamaniaServerIP_25 = "202.80.104.151";
    public static String GamaniaServerIP_26 = "202.80.104.151";
    public static String GamaniaServerIP_27 = "202.80.104.152";
    public static String GamaniaServerIP_28 = "202.80.104.152";
    public static String GamaniaServerIP_29 = "202.80.104.153";
    public static String GamaniaServerIP_30 = "202.80.104.153";
    public static String GamaniaServerIP_31 = "202.80.104.154";
    public static String GamaniaServerIP_32 = "202.80.104.154";
    public static String GamaniaServerIP_33 = "202.80.104.155";
    public static String GamaniaServerIP_34 = "202.80.104.155";
    public static String GamaniaServerIP_35 = "202.80.104.156";
    public static String GamaniaServerIP_36 = "202.80.104.156";
    public static String GamaniaServerIP_37 = "202.80.104.157";
    public static String GamaniaServerIP_38 = "202.80.104.157";
    public static String GamaniaServerIP_39 = "202.80.104.158";
    public static String GamaniaServerIP_40 = "202.80.104.158";
    // 測試服

    public static final boolean TestServer = false;
    public static final int MIN_MTS = 150; //lowest amount an item can be, GMS = 110
    public static final int MTS_BASE = 0; //+amount to everything, GMS = 500, MSEA = 1000
    public static final int MTS_TAX = 5; //+% to everything, GMS = 10
    public static final int MTS_MESO = 2500; //拍賣中的手續費用
    //master login is only used in GMS: fake account for localhost only
    //master and master2 is to bypass all accounts passwords only if you are under the IPs below
    public static final List<String> vpnIp = new LinkedList<>();
    public static final int MAXIMUM_CONNECTIONS = 1000;
    // job.length = 23
    public static final String[] JOB_NAMELIST = Arrays.stream(JobType.values()).map(Enum::name).toArray(String[]::new);
    private static final Logger log = LoggerFactory.getLogger(ServerConstants.class);
    private static final Map<String, Boolean> blockedMapFM = new HashMap<>(); // 禁止顯示給其他角色的技能
    //Inject a DLL that hooks SetupDiGetClassDevsExA and returns 0.
    // Start of Poll
    private static boolean showGMMessage = false;

    static {
        for (int i = 0; i < 256; i++) {
            vpnIp.add("17.1.1." + i);
        }
        for (int i = 0; i < 256; i++) {
            vpnIp.add("17.1.2." + i);
        }
    }

    private static InetAddress getInetAddress(String host) {
        try {
            return InetAddress.getByName(host);
        } catch (UnknownHostException ex) {
            return null;
        }
    }

    public static final String getHostAddress(String host) {
        final InetAddress inetAddr = getInetAddress(host);
        if (inetAddr == null) {
            return "127.0.0.1";
        }
        return inetAddr.getHostAddress();
    }

    public static String getLoopbackAddress() {
        return GamaniaServerIP;
    }

    public static List<String> getLocalAddresses() {
        List<String> localIp = new LinkedList<>();
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> address = ni.getInetAddresses();
                while (address.hasMoreElements()) {
                    ip = address.nextElement();
                    if (!ip.isLoopbackAddress() && !ip.getHostAddress().contains(":")) {
                        if (ip.isSiteLocalAddress()) {
                            localIp.add(ip.getHostAddress());
                        }
                    }
                }
            }
        } catch (SocketException e) {
        }
        return localIp;
    }

    public static String getIPv4Address() {
        String[] hosts = {"http://ip111.cn/", "http://ip.3322.net/", "http://bot.whatismyipaddress.com/"};
        InputStream inputStream = null;
        for (String host : hosts) {
            try {
                URL url = new URL(host);
                URLConnection urlconnnection = url.openConnection();
                inputStream = urlconnnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuffer webContent = new StringBuffer();
                String str = null;
                while ((str = bufferedReader.readLine()) != null) {
                    webContent.append(str);
                }
                Pattern p = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+");
                Matcher matcher = p.matcher(webContent);
                matcher.find();
                return matcher.group();
            } catch (IOException e) {
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return getLoopbackAddress();
    }

    public static byte[] getGamaniaServerIP() {
        return getIPBytes(getLoopbackAddress());
    }

    public static final byte[] getIPBytes(String ip) {
        byte[] localhost = new byte[]{(byte) 127, (byte) 0, (byte) 0, (byte) 1};
        if (ip == null || ip.isEmpty()) return localhost;

        String[] ipArr = ip.split("\\.");
        if (ipArr.length != 4) return localhost;

        byte[] ipBytes = new byte[4];
        for (int i = 0; i < ipArr.length; i++) {
            if (!ipArr[i].matches("^\\d+$")) return localhost;
            ipBytes[i] = (byte) Short.parseShort(ipArr[i]);
        }

        return ipBytes;
    }

    public static boolean isIPLocalhost(String sessionIP) {
        return getLoopbackAddress().contains(sessionIP.replace("/", ""));
    }

    public static boolean isVpn(String sessionIP) {
        return vpnIp.contains(sessionIP.replace("/", ""));
    }

    /**
     * 加載禁止使用FM命令的地圖列表
     */
    public static void loadBlockedMapFM() {
        blockedMapFM.clear();
        Properties settings = new Properties();
        try {
            FileInputStream fis = new FileInputStream("config/blockMapFM.ini");
            settings.load(fis);
            fis.close();
        } catch (IOException ex) {
            System.out.println("未加載 blockMapFM.ini 配置, FM指令將無禁止使用地圖列表");
        }
        for (Map.Entry<Object, Object> entry : settings.entrySet()) {
            String property = (String) entry.getKey();
            String value = (String) entry.getValue();
            try {
                blockedMapFM.put(property, Integer.parseInt(value) > 0);
            } catch (Exception ignore) {
            }
        }
    }

    public static String getCashBlockedMsg(int id) {
        switch (id) {
            case 5050000:
            case 5062000:
            case 5062001:
                return "該物品只能在特殊環境購買";
        }
        return "該商品已被商城封鎖。";
    }


    /**
     * 檢測這個技能是否禁止顯示
     */
    public static boolean isBlockedMapFM(int skillId) {
        if (blockedMapFM.containsKey(String.valueOf(skillId))) {
            return blockedMapFM.get(String.valueOf(skillId));
        }
        return false;
    }

    private static boolean logPacket = true;

    public static boolean isLogPacket() {
        return logPacket;
    }

    public static void setLogPacket(boolean show) {
        logPacket = show;
    }

    public static boolean isShowGMMessage() {
        return showGMMessage;
    }

    public static void setShowGMMessage(boolean b) {
        showGMMessage = b;
    }

    public static boolean isPvpMap(int mapid) {
        return ServerConfig.CHANNEL_PVPMAPS.indexOf(mapid) != -1;
    }

    public static boolean isOpenJob(String jobname) {
        return !ServerConfig.WORLD_CLOSEJOBS.contains(jobname);
    }

    public static int getLinkedSkill(int id) {
        switch (id) {
            case 1141002:
                return 1121052;
            case 1241004:
                return 1221021;
            case 1341001:
                return 1321012;
            case 1341003:
                return 1321052;
            case 2141000:
                return 2121006;
            case 2141003:
                return 2121011;
            case 2141005:
                return 2121003;
            case 2241000:
                return 2221006;
            case 2241002:
                return 2221012;
            case 2241003:
                return 2221007;
            case 2241004:
                return 2220014;
            case 2341000:
                return 2321007;
            case 2341002:
                return 2321001;
            case 2341004:
                return 2311015;
            case 3141000:
                return 3121020;
            case 3141004:
                return 3111013;
            case 3241000:
                return 3221007;
            case 3241001:
                return 3221025;
            case 3241002:
                return 3221026;
            case 3341001:
                return 3321005;
            case 4241007:
                return 4241006;
            case 4361001:
                return 4341052;
            case 5141009:
                return 5121013;
            case 5141501:
            case 5141502:
            case 5141503:
            case 5141504:
            case 5141505:
            case 5141506:
                return 5141500;
            case 5241002:
                return 5221022;
            case 5241003:
                return 5241002;
            case 5341005:
                return 5321003;
            case 5341006:
                return 5321001;
            case 11141002:
                return 11111029;
            case 21141001:
            case 21141002:
            case 21141003:
                return 21141000;
            case 21141501:
            case 21141502:
            case 21141503:
            case 21141504:
            case 21141505:
            case 21141506:
                return 21141500;
            case 23141003:
                return 23121052;
            case 23141005:
                return 23121002;
            case 24141000:
                return 24121005;
            case 25141504:
                return 400051043;
            case 27141000:
                return 27121303;
            case 31141000:
                return 31121001;
            case 31141003:
            case 31141004:
            case 31141005:
                return 31141002;
            case 31241000:
                return 31221001;
            case 31241001:
                return 31241000;
            case 32141000:
                return 32001014;
            case 32141002:
                return 32121052;
            case 32141003:
                return 32120055;
            case 33141006:
                return 33001016;
            case 33141007:
                return 33001025;
            case 33141008:
                return 33101115;
            case 33141010:
                return 33111015;
            case 33141011:
                return 33121017;
            case 33141012:
            case 33141013:
                return 33121155;
            case 37141000:
                return 37001000;
            case 37141001:
                return 37101000;
            case 63141000:
                return 63121002;
            case 63141004:
                return 63121006;
            case 63141007:
                return 63001000;
            case 63141010:
                return 63111007;
            case 63141011:
                return 63121004;
            case 63141100:
                return 63121102;
            case 63141107:
                return 63101100;
            case 63141109:
                return 63101104;
            case 101141000:
                return 101121100;
            case 101141014:
                return 101111100;
            case 101141017:
                return 101111200;
            case 142141000:
                return 142001002;
            case 151141002:
                return 151001001;
            case 151141003:
                return 151101013;
            case 151141005:
                return 151121002;
            case 154141003:
                return 154121009;
            case 154141008:
                return 154121003;
            case 154141001:
                return 154101001;
            case 154141002:
                return 154101002;
            case 154141009:
                return 154121001;
            case 155141000:
                return 155001100;
            case 155141004:
                return 155101100;
            case 155141005:
                return 155101101;
            case 155141006:
                return 155101112;
            case 155141007:
                return 155101013;
            case 155141008:
                return 155101015;
            case 155141011:
                return 155111102;
            case 155141012:
                return 155111111;
            case 155141016:
                return 155121102;
            case 155141017:
                return 155121002;
            case 155141021:
                return 155101200;
            case 155141022:
                return 155101201;
            case 155141024:
                return 155111202;
            case 155141025:
                return 155111211;
            case 155141026:
                return 155111212;
            case 155141027:
                return 155121202;
            case 155141028:
                return 155121215;
            case 164141000:
                return 164121000;
            case 164141005:
                return 164111003;
            case 164141011:
                return 164121003;
            case 164141030:
                return 164001000;
            case 164141033:
                return 164101000;
            case 164141035:
                return 164111000;
            case 5221027:
                return 5221022;
            case 400051097:
                return 400051046;
            case 400011024:
            case 400011025:
                return 400011015;
            case 400041085:
            case 400041086:
                return 400041084;
            case 2221055:
                return 2221054;
            case 400001064:
                return 400001036;
            case 162101006:
            case 162101007:
                return 162100005;
            case 162101003:
            case 162101004:
                return 162100002;
            case 162121044:
                return 162121043;
            case 400021131:
                return 400021130;
            case 162121003:
            case 162121004:
                return 162120002;
            case 162121006:
            case 162121007:
                return 162120005;
            case 162121009:
            case 162121010:
                return 162120008;
            case 162111010:
                return 162111002;
            case 162101009:
            case 162101010:
            case 162101011:
                return 162100008;
            case 162121012:
            case 162121013:
            case 162121014:
            case 162121015:
            case 162121016:
            case 162121017:
            case 162121018:
            case 162121019:
                return 162120011;
            case 135001004:
            case 135003003:
            case 135003004:
                return 135001003;
            case 400041023:
            case 400041024:
            case 400041080:
                return 400041022;
            case 400001052:
                return 400001007;
            case 131002025:
                return 131001025;
            case 131002026:
            case 131003026:
                return 131001026;
            case 131002015:
                return 131001015;
            case 131002023:
            case 131003023:
            case 131004023:
            case 131005023:
            case 131006023:
                return 131001023;
            case 131002022:
            case 131003022:
            case 131004022:
            case 131005022:
            case 131006022:
                return 131001022;
            case 131001113:
            case 131001213:
            case 131001313:
                return 131001013;
            case 132001017:
            case 133001017:
                return 131001017;
            case 63001003:
            case 63001005:
                return 63001002;
            case 135002018:
                return 135001018;
            case 400001060:
                return 400001059;
            case 14001031:
                return 14001023;
            case 155121004:
                return 155121102;
            case 400011065:
                return 400011055;
            case 400011092:
            case 400011093:
            case 400011094:
            case 400011095:
            case 400011096:
            case 400011097:
            case 400011103:
                return 400011091;
            case 37120055:
            case 37120056:
            case 37120057:
            case 37120058:
            case 37120059:
                return 37121052;
            case 37110001:
            case 37110002:
                return 37111000;
            case 14001030:
                return 14001026;
            case 2121055:
                return 2121052;
            case 23111009:
            case 23111010:
            case 23111011:
                return 23111008;
            case 400001011:
                return 400001010;
            case 400041056:
                return 400041055;
            case 400021100:
            case 400021111:
                return 400021099;
            case 400041058:
                return 400041057;
            case 400011135:
                return 400011134;
            case 400021097:
            case 400021098:
            case 400021104:
                return 400021096;
            case 400011119:
            case 400011120:
                return 400011118;
            case 400051069:
                return 400051068;
            case 400011111:
                return 400011110;
            case 400021088:
            case 400021089:
                return 400021087;
            case 400011113:
            case 400011114:
            case 400011115:
            case 400011129:
                return 400011112;
            case 400021112:
                return 400021094;
            case 400031045:
                return 400031044;
            case 400011085:
                return 400011047;
            case 400051079:
                return 400051078;
            case 400051075:
            case 400051076:
            case 400051077:
                return 400051074;
            case 400011132:
                return 400011131;
            case 400011122:
                return 400011121;
            case 400031059:
                return 400031058;
            case 400041060:
                return 400041059;
            case 400051059:
            case 400051060:
            case 400051061:
            case 400051062:
            case 400051063:
            case 400051064:
            case 400051065:
            case 400051066:
            case 400051067:
                return 400051058;
            case 400031047:
            case 400031048:
            case 400031049:
            case 400031050:
            case 400031051:
                return 400031057;
            case 400051071:
                return 400051070;
            case 400041070:
            case 400041071:
            case 400041072:
            case 400041073:
                return 400041069;
            case 400041076:
            case 400041077:
            case 400041078:
                return 400041075;
            case 400041062:
            case 400041079:
                return 400041061;
            case 5120021:
                return 5121013;
            case 25111211:
                return 25111209;
            case 400031031:
                return 400031030;
            case 400031054:
                return 400031053;
            case 400031056:
                return 400031055;
            case 30001078:
            case 30001079:
            case 30001080:
                return 30001068;
            case 61121026:
                return 61121102;
            case 400001040:
            case 400001041:
                return 400001039;
            case 400041051:
                return 400041050;
            case 400001044:
                return 400001043;
            case 151101004:
            case 151101010:
                return 151101003;
            case 131001001:
            case 131001002:
            case 131001003:
                return 131001000;
            case 131001106:
            case 131001206:
            case 131001306:
            case 131001406:
            case 131001506:
                return 131001006;
            case 131001107:
            case 131001207:
            case 131001307:
                return 131001007;
            case 24121010:
                return 24121003;
            case 24111008:
                return 24111006;
            case 151101007:
            case 151101008:
                return 151101006;
            case 142120001:
                return 142120000;
            case 142110003:
                return 142111002;
            case 400041049:
                return 400041048;
            case 400041053:
                return 400041052;
            case 37000009:
                return 37001001;
            case 37100008:
                return 37100007;
            case 151001003:
                return 151001002;
            case 400001051:
            case 400001053:
            case 400001054:
            case 400001055:
                return 400001050;
            case 95001000:
                return 3111013;
            case 95001016:
                return 3141004;
            case 400031018:
            case 400031019:
                return 400031017;
            case 164111016:
                return 164111003;
            case 164111001:
            case 164111002:
            case 164111009:
            case 164111010:
            case 164111011:
                return 164110000;
            case 400001047:
            case 400001048:
            case 400001049:
                return 400001046;
            case 164001002:
                return 164001001;
            case 151121011:
                return 151121004;
            case 164101001:
            case 164101002:
                return 164100000;
            case 164101004:
                return 164101003;
            case 164121001:
            case 164121002:
            case 164121014:
                return 164120000;
            case 164121004:
                return 164121003;
            case 164121015:
                return 164121008;
            case 164120007:
                return 164121007;
            case 164121044:
                return 164121043;
            case 164121011:
            case 164121012:
                return 164121006;
            case 164111004:
            case 164111005:
            case 164111006:
                return 164111003;
            case 400031035:
                return 400031034;
            case 400031038:
            case 400031039:
            case 400031040:
            case 400031041:
            case 400031042:
            case 400031043:
                return 400031037;
            case 31011004:
            case 31011005:
            case 31011006:
            case 31011007:
                return 31011000;
            case 31201007:
            case 31201008:
            case 31201009:
            case 31201010:
                return 31201000;
            case 31211007:
            case 31211008:
            case 31211009:
            case 31211010:
                return 31211000;
            case 31221009:
            case 31221010:
            case 31221011:
            case 31221012:
                return 31221000;
            case 3311011:
                return 3311010;
            case 3011006:
            case 3011007:
            case 3011008:
                return 3011005;
            case 3301009:
                return 3301008;
            case 3301004:
                return 3301003;
            case 3321003:
            case 3321004:
            case 3321005:
            case 3321006:
            case 3321007:
                return 3320002;
            case 3321036:
            case 3321037:
            case 3321038:
            case 3321039:
            case 3321040:
                return 3321035;
            case 3321016:
            case 3321017:
            case 3321018:
            case 3321019:
            case 3321020:
            case 3321021:
                return 3321014;
            case 21000004:
                return 21001009;
            case 142100010:
                return 142101009;
            case 142100008:
                return 142101002;
            case 27120211:
                return 27121201;
            case 33121255:
                return 33121155;
            case 33101115:
                return 33101215;
            case 37000005:
                return 37001004;
            case 400011074:
            case 400011075:
            case 400011076:
                return 400011073;
            case 33001202:
                return 33001102;
            case 152000009:
                return 152000007;
            case 152001005:
                return 152001004;
            case 152120002:
                return 152120001;
            case 152101000:
            case 152101004:
                return 152101003;
            case 152121006:
                return 152121005;
            case 400051019:
            case 400051020:
                return 400051018;
            case 152110004:
            case 152120016:
            case 152120017:
                return 152001001;
            case 400021064:
            case 400021065:
                return 400021063;
            case 1100012:
                return 1101012;
            case 1111014:
                return 1111008;
            case 2100010:
                return 2101010;
            case 61111114:
            case 61111221:
                return 61111008;
            case 14121055:
            case 14121056:
                return 14121054;
            case 61121220:
                return 61121015;
            case 400031008:
            case 400031009:
                return 400031007;
            case 142120030:
                return 142121030;
            case 400051039:
            case 400051052:
            case 400051053:
                return 400051038;
            case 400021043:
            case 400021044:
            case 400021045:
                return 400021042;
            case 400051049:
            case 400051050:
                return 400051040;
            case 400040006:
                return 400041006;
            case 155001204:
                return 155001104;
            case 400031026:
            case 400031027:
                return 400031025;
            case 61121222:
                return 61121105;
            case 400020046:
            case 400020051:
            case 400021013:
            case 400021014:
            case 400021015:
            case 400021016:
                return 400021012;
            case 61121116:
            case 61121124:
            case 61121221:
            case 61121223:
            case 61121225:
                return 61121104;
            case 400011002:
                return 400011001;
            case 400010030:
                return 400011031;
            case 400051051:
                return 400051041;
            case 400021077:
                return 400021070;
            case 2120013:
                return 2121007;
            case 2220014:
                return 2221007;
            case 32121011:
                return 32121004;
            case 400011059:
            case 400011060:
            case 400011061:
                return 400011058;
            case 400021075:
            case 400021076:
                return 400021074;
            case 400011033:
            case 400011034:
            case 400011035:
            case 400011036:
            case 400011037:
            case 400011067:
                return 400011032;
            case 400011080:
            case 400011081:
            case 400011082:
                return 400011079;
            case 400011084:
                return 400011083;
            case 21120026:
                return 21120019;
            case 400020009:
            case 400020010:
            case 400020011:
            case 400021010:
            case 400021011:
                return 400021008;
            case 400041026:
            case 400041027:
                return 400041025;
            case 400040008:
            case 400041019:
                return 400041008;
            case 400041003:
            case 400041004:
            case 400041005:
                return 400041002;
            case 400051045:
                return 400051044;
            case 400011078:
                return 400011077;
            case 400031016:
                return 400031015;
            case 400031013:
            case 400031014:
                return 400031012;
            case 400011102:
                return 400011090;
            case 400020002:
                return 400021002;
            case 22140023:
                return 22140014;
            case 22140024:
                return 22140015;
            case 22141012:
                return 22140022;
            case 22110014:
            case 22110025:
                return 22110014;
            case 22170061:
                return 22170060;
            case 22170093:
                return 22170064;
            case 22171083:
                return 22171080;
            case 22170094:
                return 22170065;
            case 400011069:
                return 400011068;
            case 400031033:
                return 400031032;
            case 25121133:
                return 25121131;
            case 23121015:
                return 23121014;
            case 24120055:
                return 24121052;
            case 31221014:
                return 31221001;
            case 400021031:
            case 400021040:
                return 400021030;
            case 4120019:
                return 4120018;
            case 37000010:
                return 37001001;
            case 155001000:
                return 155001001;
            case 155001009:
                return 155001104;
            case 155100009:
                return 155101008;
            case 155101002:
                return 155101003;
            case 155101013:
            case 155101015:
            case 155101101:
            case 155101112:
                return 155101100;
            case 155101114:
                return 155101104;
            case 155101214:
                return 155101204;
            case 155101201:
            case 155101212:
                return 155101200;
            case 155111002:
            case 155111111:
                return 155111102;
            case 155111103:
            case 155111104:
                return 155111105;
            case 155111106:
                return 155111102;
            case 155111211:
            case 155111212:
                return 155111202;
            case 155121002:
                return 155121102;
            case 155121003:
                return 155121005;
            case 155121006:
            case 155121007:
                return 155121306;
            case 155121215:
                return 155121202;
            case 400041010:
            case 400041011:
            case 400041012:
            case 400041013:
            case 400041014:
            case 400041015:
                return 400041009;
            case 400011099:
                return 400011098;
            case 400011101:
                return 400011100;
            case 400011053:
                return 400011052;
            case 400001016:
                return 400001013;
            case 400021029:
                return 400021028;
            case 400030002:
                return 400031002;
            case 400021049:
            case 400021050:
                return 400021041;
            case 14000027:
            case 14000028:
            case 14000029:
                return 14001027;
            case 4100011:
            case 4100012:
                return 4101011;
            case 5211015:
            case 5211016:
                return 5211011;
            case 5220023:
            case 5220024:
            case 5220025:
                return 5221022;
            case 51001006:
            case 51001007:
            case 51001008:
            case 51001009:
            case 51001010:
            case 51001011:
            case 51001012:
            case 51001013:
                return 51001005;
            case 51141003:
            case 51141004:
            case 51141005:
            case 51141006:
            case 51141007:
            case 51141008:
            case 51141009:
            case 51141010:
            case 51141011:
            case 51141012:
                return 51141002;
            case 25120115:
                return 25120110;
            case 5201005:
                return 5201011;
            case 5320011:
                return 5321004;
            case 33001008:
            case 33001009:
            case 33001010:
            case 33001011:
            case 33001012:
            case 33001013:
            case 33001014:
            case 33001015:
                return 33001007;
            case 65120011:
                return 65121011;
            case 400041034:
                return 400041033;
            case 400041036:
                return 400041035;
            case 21110027:
            case 21110028:
            case 21111021:
                return 21110020;
            case 100000276:
            case 100000277:
                return 100000267;
            case 400001025:
            case 400001026:
            case 400001027:
            case 400001028:
            case 400001029:
            case 400001030:
                return 400001024;
            case 400001015:
                return 400001014;
            case 400011013:
            case 400011014:
                return 400011012;
            case 400001022:
                return 400001019;
            case 400021033:
            case 400021052:
                return 400021032;
            case 400041016:
                return 4001344;
            case 400041017:
                return 4111010;
            case 400041018:
                return 4121013;
            case 400051003:
            case 400051004:
            case 400051005:
                return 400051002;
            case 400051025:
            case 400051026:
                return 400051024;
            case 400051023:
                return 400051022;
            case 2321055:
                return 2321052;
            case 5121055:
                return 5121052;
            case 61111220:
                return 61111002;
            case 12001027:
            case 12001028:
                return 12000023;
            case 36121013:
            case 36121014:
            case 36141004:
            case 36141005:
            case 36141006:
                return 36121002;
            case 36121011:
            case 36121012:
                return 36121001;
            case 400010010:
                return 400011010;
            case 10001253:
            case 10001254:
            case 14001026:
                return 10000252;
            case 142000006:
                return 142001004;
            case 4321001:
                return 4321000;
            case 33101006:
            case 33101007:
                return 33101005;
            case 33101008:
                return 33101004;
            case 35101009:
            case 35101010:
                return 35100008;
            case 35111009:
            case 35111010:
                return 35111001;
            case 35121013:
                return 35111005;
            case 35121011:
                return 35121009;
            case 3000008:
            case 3000009:
            case 3000010:
                return 3001007;
            case 32001007:
            case 32001008:
            case 32001009:
            case 32001010:
            case 32001011:
                return 32001001;
            case 64001007:
            case 64001008:
            case 64001009:
            case 64001010:
            case 64001011:
            case 64001012:
                return 64001000;
            case 64001013:
                return 64001002;
            case 64100001:
                return 64100000;
            case 64001006:
                return 64001001;
            case 64101008:
                return 64101002;
            case 64111012:
                return 64111004;
            case 64121012:
            case 64121013:
            case 64121014:
            case 64121015:
            case 64121017:
            case 64121018:
            case 64121019:
                return 64121001;
            case 64121022:
            case 64121023:
            case 64121024:
                return 64121021;
            case 64121016:
                return 64121003;
            case 64121055:
                return 64121053;
            case 5300007:
                return 5301001;
            case 23101007:
                return 23101001;
            case 31001006:
            case 31001007:
            case 31001008:
                return 31000004;
            case 30010183:
            case 30010184:
            case 30010186:
                return 30010110;
            case 25000001:
                return 25001000;
            case 25000003:
                return 25001002;
            case 25100001:
            case 25100002:
                return 25101000;
            case 25100010:
                return 25100009;
            case 25110001:
            case 25110002:
            case 25110003:
                return 25111000;
            case 25120001:
            case 25120002:
            case 25120003:
                return 25121000;
            case 101000102:
                return 101000101;
            case 101000202:
                return 101000201;
            case 101100202:
                return 101100201;
            case 101110201:
            case 101110204:
                return 101110203;
            case 101120101:
                return 101120100;
            case 101120103:
                return 101120102;
            case 101120105:
            case 101120106:
                return 101120104;
            case 101120203:
                return 101120202;
            case 400031021:
                return 400031020;
            case 101120205:
            case 101120206:
                return 101120204;
            case 101120200:
            case 101141006:
                return 101121200;
            case 100001266:
            case 100001269:
                return 100001265;
            case 1111002:
                return 1101013;
            case 3120019:
                return 3111009;
            case 5201013:
            case 5201014:
                return 5201012;
            case 5210016:
            case 5210017:
            case 5210018:
                return 5210015;
            case 11121055:
                return 11121052;
            case 12120011:
                return 12121001;
            case 12121055:
                return 12121054;
            case 12120013:
            case 12120014:
                return 12121004;
            case 14101029:
                return 14101028;
            case 61110211:
            case 61120007:
            case 61121217:
                return 61101002;
            case 61120008:
                return 61111008;
            case 61121201:
                return 61121100;
            case 65111007:
                return 65111100;
            case 36111009:
            case 36111010:
                return 36111000;
        }
        if (id == 155101204) {
            return 155101104;
        }
        return id;
    }

    public static boolean sub_1F04F40(int a1) {
        boolean v1;
        if (a1 > 13121009) {
            if (a1 == 36110005) {
                return true;
            }
            v1 = (a1 == 65101006);
        } else {
            if (a1 == 13121009 || a1 == 11121013) {
                return true;
            }
            v1 = (a1 == 12100029);
        }
        return v1;
    }

    public static boolean sub_8242D0(int a1) {
        boolean result;
        if (a1 <= 0) {
            return result = (a1 - 90000000 >= 0 && a1 - 90000000 < 12);
        }
        int v1 = a1 / 10000;
        if (a1 / 10000 == 8000) {
            v1 = a1 / 100;
        }
        if (v1 == 9500) {
            result = false;
        } else {
            result = (a1 - 90000000 >= 0 && a1 - 90000000 < 12);
        }
        return result;
    }

    public enum MapleType {

        UNKNOWN(-1, 949),
        한국(1, 949),
        한국_TEST(2, 949),
        日本(3, 932),
        中国(4, 936),
        中国_TEST(5, 936),
        台灣(6, 950),
        SEA(7, 949),
        GLOBAL(8, 949),
        BRAZIL(9, 949);

        byte type;
        int codepage;
        Charset charset;

        MapleType(int type, int codepage) {
            this.type = (byte) type;
            this.codepage = codepage;
            try {
                charset = Charset.forName(String.format("MS%d", codepage));
            } catch (Exception e) {
                this.codepage = 949;
                charset = Charset.forName("MS949");
                System.err.println("設置Charset出錯(" + name() + "):" + e);
            }
        }

        public byte getType() {
            return type;
        }

        public int getCodePage() {
            return codepage;
        }

        public Charset getCharset() {
            return charset;
        }

        public void setType(int type) {
            this.type = (byte) type;
        }

        public static MapleType getByType(byte type) {
            for (MapleType l : MapleType.values()) {
                if (l.getType() == type) {
                    return l;
                }
            }
            return UNKNOWN;
        }
    }
}
