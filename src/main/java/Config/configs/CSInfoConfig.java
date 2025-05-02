package Config.configs;

import tools.config.Property;

public class CSInfoConfig {

    /**
     * 商城信息封包
     */
    @Property(key = "cash.cashshoppack", defaultValue = "")
    public static String CASH_CASHSHOPPACK;
    /**
     * 皮卡啾敲敲樂第一階段獎勵
     */
    @Property(key = "cash.pbtapreward1", defaultValue = "")
    public static String CASH_PBTAPREWARD1;
    /**
     * 皮卡啾敲敲樂第二階段獎勵
     */
    @Property(key = "cash.pbtapreward2", defaultValue = "")
    public static String CASH_PBTAPREWARD2;
    /**
     * 皮卡啾敲敲樂第三階段獎勵
     */
    @Property(key = "cash.pbtapreward3", defaultValue = "")
    public static String CASH_PBTAPREWARD3;
}
