package Config.configs;

import tools.config.Property;
import tools.json.JSONArray;

import java.util.LinkedList;
import java.util.List;

public class MvpEquipConfig {

    @Property(key = "mvpequip.rentequiplist", defaultValue = "[]")
    public static String RentEquipList;
    public static JSONArray RentEquipListJson;

    @Property(key = "mvpequip.rentmvpequiplist", defaultValue = "[]")
    public static String RentMvpEquipList;
    public static JSONArray RentMvpEquipListJson;

    @Property(key = "mvpequip.mvpequipmakelist", defaultValue = "[]")
    public static String MvpEquipMakeList;
    public static JSONArray MvpEquipMakeListJson;

    @Property(key = "mvpequip.canupgradelimitmvpequip", defaultValue = "true")
    public static boolean CanUpgradeLimitMvpEquip;

    @Property(key = "mvpequip.enhanceitemlist", defaultValue = "")
    public static String EnhanceItemList;
    public static List<Integer> EnhanceItem = new LinkedList<>();

    @Property(key = "mvpequip.enhancemvplevel", defaultValue = "0")
    public static int EnhanceMvpLevel;

    @Property(key = "mvpequip.enhancepricetype", defaultValue = "1")
    public static int EnhancePriceType;

    @Property(key = "mvpequip.enhancecost", defaultValue = "")
    public static String EnhanceCost;
    public static List<Integer> EnhanceCosts = new LinkedList<>();

    @Property(key = "mvpequip.enhancerate", defaultValue = "")
    public static String EnhanceRate;
    public static List<Integer> EnhanceRates = new LinkedList<>();
}
