/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Config.configs;


import tools.config.Property;

/**
 * @author admin
 */
public class FishingConfig {

    /**
     * 是否啟用釣魚系統
     * 默認: True
     */
    @Property(key = "Fishing.Enable", defaultValue = "true")
    public static boolean FISHING_ENABLE;
    /**
     * 釣魚地圖
     * 默認：749050500,749050501,749050502,970020000,970020005
     */
    @Property(key = "Fishing.Check.MAP", defaultValue = "749050500,749050501,749050502,970020000,970020005")
    public static String FISHING_MAP;
    /**
     * 是否使用指定道具的椅子進行釣魚
     * 默認: True
     */
    @Property(key = "Fishing.Check.Chair", defaultValue = "true")
    public static boolean FISHING_CHECK_CHAIR;
    /**
     * 釣魚時做的椅子
     * 默認: 3011000
     */
    @Property(key = "Fishing.Chair", defaultValue = "3011000")
    public static int FISHING_CHAIR;
    /**
     * 普通釣魚竿釣魚的時間間隔
     * 默認: 60000 (毫秒)
     */
    @Property(key = "Fishing.Time", defaultValue = "60000")
    public static int FISHING_TIME;
    /**
     * 高級釣魚竿釣魚的時間間隔
     * 默認: 30000 (毫秒)
     */
    @Property(key = "Fishing.Time.Vip", defaultValue = "30000")
    public static int FISHING_TIME_VIP;
    /**
     * GM釣魚的時間間隔
     * 默認: 10000 (毫秒)
     */
    @Property(key = "Fishing.Time.GM", defaultValue = "10000")
    public static int FISHING_TIME_GM;
    /**
     * 普通釣魚竿成功的概率
     * 默認: 70%
     */
    @Property(key = "Fishing.Chance", defaultValue = "70")
    public static int FISHING_CHANCE;
    /**
     * 高級釣魚竿成功的概率
     * 默認: 90%
     */
    @Property(key = "Fishing.Chance.Vip", defaultValue = "90")
    public static int FISHING_CHANCE_VIP;
    /**
     * GM釣魚成功的概率
     * 默認: 100%
     */
    @Property(key = "Fishing.Chance.GM", defaultValue = "100")
    public static int FISHING_CHANCE_GM;
}
