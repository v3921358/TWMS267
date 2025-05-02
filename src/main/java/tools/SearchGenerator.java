package tools;

import Client.MapleJob;
import Client.skills.SkillFactory;
import Config.constants.ItemConstants;
import Config.constants.ServerConstants;
import Net.server.MapleItemInformationProvider;
import Net.server.StructSetItem;
import Net.server.VCoreDataEntry;
import Opcode.Headler.InHeader;
import Opcode.Headler.OutHeader;
import Plugin.provider.loaders.StringData;

import java.util.*;

/**
 * @author hertz 23/8
 * [ 高級檢索工具 ]
 */
public class SearchGenerator {

    public enum SearchType {

        道具(1),
        NPC(2),
        地圖(3),
        怪物(4),
        任務(5),
        技能(6),
        V核心(13),
        職業(7),
        裝備套裝(8),
        伺服器包頭(9),
        用戶端包頭(10),
        膚色(14),
        髮型(11),
        臉型(12),
        未知;

        private final int value;

        SearchType() {
            this.value = 0;
        }

        SearchType(int value) {
            this.value = value;
        }

        public final int getValue() {
            return value;
        }

        public static String nameOf(int value) {
            for (SearchType type : SearchType.values()) {
                if (type.getValue() == value) {
                    return type.name();
                }
            }
            return "未知";
        }
    }

    public static final int 道具 = SearchType.道具.getValue();
    public static final int NPC = SearchType.NPC.getValue();
    public static final int 地圖 = SearchType.地圖.getValue();
    public static final int 怪物 = SearchType.怪物.getValue();
    public static final int 任務 = SearchType.任務.getValue();
    public static final int 技能 = SearchType.技能.getValue();
    public static final int V核心 = SearchType.V核心.getValue();
    public static final int 職業 = SearchType.職業.getValue();
    public static final int 裝備套裝 = SearchType.裝備套裝.getValue();
    public static final int 伺服器包頭 = SearchType.伺服器包頭.getValue();
    public static final int 用戶端包頭 = SearchType.用戶端包頭.getValue();
    public static final int 膚色 = SearchType.膚色.getValue();
    public static final int 髮型 = SearchType.髮型.getValue();
    public static final int 臉型 = SearchType.臉型.getValue();
    private static final Map<SearchType, Map<Integer, String>> searchs = new HashMap<>();

    public static Map<Integer, String> getSearchs(int type) {
        return getSearchs(SearchType.valueOf(SearchType.nameOf(type)));
    }

    public static Map<Integer, String> getSearchs(SearchType type) {
        if (searchs.containsKey(type)) {
            return searchs.get(type);
        }

        Map<Integer, String> values = new TreeMap<>(Integer::compareTo);

        switch (type) {
            case 道具: //DAT
                values.putAll(StringData.getItemStrings());
                break;
            case NPC: //DAT
                values.putAll(StringData.getNpcStrings());
                break;
            case 地圖: //DAT
                values.putAll(StringData.getMapStrings());
                break;
            case 怪物: //DAT
                values.putAll(StringData.getMobStrings());
                break;
            case 任務:
                values.putAll(StringData.getQuestStrings());
                break;
            case 技能: {
                values.putAll(SkillFactory.getAllSkills());
                break;
            }
            case V核心: {
                for (Map.Entry<Integer, VCoreDataEntry> entry : MapleItemInformationProvider.getInstance().getCoreDatas().entrySet()) {
                    values.put(entry.getKey(), entry.getValue().getName());
                }
                break;
            }
            case 職業:
                for (MapleJob job : MapleJob.values()) {
                    values.put(job.getIdWithSub(), job.getName());
                }
                break;
            case 裝備套裝:
                for (StructSetItem setInfo : MapleItemInformationProvider.getInstance().getSetItems().values()) {
                    values.put(setInfo.setItemID, setInfo.setItemName);
                }
                break;
            case 伺服器包頭:
                for (OutHeader send : OutHeader.values()) {
                    values.put((int) send.getValue(), send.name());
                }
                break;
            case 用戶端包頭:
                for (InHeader recv : InHeader.values()) {
                    values.put((int) recv.getValue(), recv.name());
                }
                break;
            case 膚色:
                for (Map.Entry<Integer, String> itemNames : MapleItemInformationProvider.getInstance().getAllItemNames().entrySet()) {
                    if (itemNames.getKey() >= 13000) {
                        continue;
                    }
                    values.put(itemNames.getKey() % 1000, itemNames.getValue());
                }
                break;
            case 髮型:
            case 臉型:
                for (Map.Entry<Integer, String> itemNames : MapleItemInformationProvider.getInstance().getAllItemNames().entrySet()) {
                    if (type == SearchType.髮型 && !ItemConstants.類型.髮型(itemNames.getKey())) {
                        continue;
                    } else if (type == SearchType.臉型 && !ItemConstants.類型.臉型(itemNames.getKey())) {
                        continue;
                    }
                    values.put(itemNames.getKey(), itemNames.getValue());
                }
                break;
        }

        searchs.put(type, values);
        return values;
    }

    public static Map<Integer, String> getSearchData(int type, String search) {
        return getSearchData(SearchType.valueOf(SearchType.nameOf(type)), search);
    }

    public static Map<Integer, String> getSearchData(SearchType type, String search) {
        Map<Integer, String> values = new TreeMap<>(Integer::compareTo);
        Map<Integer, String> ss = getSearchs(type);

        for (int i : ss.keySet()) {
            if (String.valueOf(i).toLowerCase().contains(search.toLowerCase()) || ss.get(i).toLowerCase().contains(search.toLowerCase())) {
                values.put(i, ss.get(i));
            }
        }

        return values;
    }

    public static String searchData(int type, String search) {
        return searchData(SearchType.valueOf(SearchType.nameOf(type)), search);
    }

    public static String searchData(SearchType type, String search) {
        Map<Integer, String> ss = getSearchData(type, search);
        List<String> ret = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        switch (type) {
            case 道具:
                ss.entrySet().forEach((i) -> ret.add("\r\n#L" + i.getKey() + "##i" + i + ":# " + (i.getKey() >= 6000000 ? i.getValue() : ("#z" + i + "#"))
                        + ((i.getKey() < 2000000 && !ItemConstants.類型.能源(i.getKey()) && !ItemConstants.類型.拼圖(i.getKey()) && ItemConstants.類型.getGender(i.getKey()) < 2) ? "#fUI/CashShop/CSIcon/" + ItemConstants.類型.getGender(i.getKey()) + "#" : "")
                        + "(" + i.getKey() + ")#l"));
                break;
            case NPC:
                ss.keySet().forEach((i) -> ret.add("\r\n#L" + i + "##p" + i + "#(" + i + ")#l"));
                break;
            case 地圖:
                ss.entrySet().forEach((i) -> ret.add("\r\n#L" + i.getKey() + "#" + i.getValue() + "(" + i.getKey() + ")#l"));
                break;
            case 怪物:
                ss.keySet().forEach((i) -> ret.add("\r\n#L" + i + "##o" + i + "#(" + i + ")#l"));
                break;
            case 任務:
                ss.entrySet().forEach((i) -> ret.add("\r\n#L" + i.getKey() + "#" + i.getValue() + "(" + i.getKey() + ")#l"));
                break;
            case 技能:
                ss.entrySet().forEach((i) -> ret.add("\r\n#L" + i.getKey() + "##s" + i.getKey() + "#" + i.getValue() + "(" + i.getKey() + ")#l"));
                break;
            case V核心:
                ss.entrySet().forEach((i) -> {
                    VCoreDataEntry vCore = MapleItemInformationProvider.getInstance().getCoreData(i.getKey());
                    String icon = "";
                    if (vCore != null && vCore.getConnectSkill().size() > 0) {
                        icon = "#s" + vCore.getConnectSkill().get(0) + "#";
                    }
                    ret.add("\r\n#L" + i.getKey() + "#" + icon + i.getValue() + "(" + i.getKey() + ")#l");
                });
                break;
            case 職業:
                ss.entrySet().forEach((i) -> ret.add("\r\n#L" + i.getKey() + "#" + i.getValue() + "(" + i.getKey() + ")#l"));
                break;
            case 裝備套裝:
                ss.entrySet().forEach((i) -> ret.add("\r\n#L" + i.getKey() + "#" + i.getValue() + "(" + i.getKey() + ")#l"));
                break;
            case 伺服器包頭:
            case 用戶端包頭:
                ss.entrySet().forEach((i) -> ret.add("\r\n" + i.getValue() + "(" + i.getKey() + "[" + HexTool.getOpcodeToString(i.getKey()) + "])"));
                break;
            default:
                sb.append("對不起, 這個檢索類型不被支援");
        }

        if (ret.size() > 0) {
            for (String singleRetItem : ret) {
                if (sb.toString().getBytes(ServerConstants.MapleType.getByType(ServerConstants.MapleRegion).getCharset()).length > 40000) {
                    sb.append("\r\n後面還有很多搜尋結果, 但已經無法顯示更多");
                    break;
                }
                sb.append(singleRetItem);
            }
        }

        StringBuilder sbs = new StringBuilder();
        if (!sb.toString().isEmpty() && !sb.toString().equalsIgnoreCase("對不起, 這個檢索指令不被支援")) {
            sbs.append("<<類型: ").append(type.name()).append(" | 搜尋訊息: ").append(search).append(">>");
        }
        sbs.append(sb);
        if (sbs.toString().isEmpty()) {
            sbs.append("搜尋不到此").append(type.name());
        }
        return sbs.toString();
    }

    public static boolean foundData(int type, String search) {
        return !getSearchData(type, search).isEmpty();
    }

    static {
        StringData.loadItemStrings(); //DAT
    }
}
