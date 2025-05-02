package Config.constants;

import Client.hexa.ErdaConsumeOption;

import java.util.HashMap;
import java.util.Map;

/**
 * Hexa強化與消耗相關資訊
 *
 * @version 1.0
 * @since Revision 1
 */

public final class HexaConstants {
    /**
     * 開啟技能核心消耗資訊
     *
     * @return Map<Integer, ErdaConsumeOption>
     */
    public static Map<Integer, ErdaConsumeOption> hexaOpenSkillCoreInfo() {
        Map<Integer, ErdaConsumeOption> data = new HashMap<>();
        data.put(0, new ErdaConsumeOption(5, 100)); // 技能核心
        data.put(1, new ErdaConsumeOption(3, 50)); // 技能核心
        data.put(2, new ErdaConsumeOption(4, 75)); // 技能核心
        data.put(3, new ErdaConsumeOption(5, 100)); // 技能核心
        return data;
    }

    /**
     * 強化技能核心消耗資訊
     *
     * @return Map<Integer, Map < Integer, ErdaConsumeOption>>
     */
    public static Map<Integer, Map<Integer, ErdaConsumeOption>> hexaEnforcementSkillInfo() {
        Map<Integer, Map<Integer, ErdaConsumeOption>> data = new HashMap<>();

        // 技能核心強化資訊
        Map<Integer, ErdaConsumeOption> core0 = new HashMap<>();
        core0.put(1, new ErdaConsumeOption(1, 30));
        core0.put(2, new ErdaConsumeOption(1, 35));
        core0.put(3, new ErdaConsumeOption(1, 40));
        core0.put(4, new ErdaConsumeOption(2, 45));
        core0.put(5, new ErdaConsumeOption(2, 50));
        core0.put(6, new ErdaConsumeOption(2, 55));
        core0.put(7, new ErdaConsumeOption(3, 60));
        core0.put(8, new ErdaConsumeOption(3, 65));
        core0.put(9, new ErdaConsumeOption(10, 200));
        core0.put(10, new ErdaConsumeOption(3, 80));
        core0.put(11, new ErdaConsumeOption(3, 90));
        core0.put(12, new ErdaConsumeOption(4, 100));
        core0.put(13, new ErdaConsumeOption(4, 110));
        core0.put(14, new ErdaConsumeOption(4, 120));
        core0.put(15, new ErdaConsumeOption(4, 130));
        core0.put(16, new ErdaConsumeOption(4, 140));
        core0.put(17, new ErdaConsumeOption(4, 150));
        core0.put(18, new ErdaConsumeOption(5, 160));
        core0.put(19, new ErdaConsumeOption(15, 350));
        core0.put(20, new ErdaConsumeOption(5, 170));
        core0.put(21, new ErdaConsumeOption(5, 180));
        core0.put(22, new ErdaConsumeOption(5, 190));
        core0.put(23, new ErdaConsumeOption(5, 200));
        core0.put(24, new ErdaConsumeOption(5, 210));
        core0.put(25, new ErdaConsumeOption(6, 220));
        core0.put(26, new ErdaConsumeOption(6, 230));
        core0.put(27, new ErdaConsumeOption(6, 240));
        core0.put(28, new ErdaConsumeOption(7, 250));
        core0.put(29, new ErdaConsumeOption(20, 500));
        data.put(0, core0);

        // 精通核心強化資訊
        Map<Integer, ErdaConsumeOption> core1 = new HashMap<>();
        core1.put(1, new ErdaConsumeOption(1, 15));
        core1.put(2, new ErdaConsumeOption(1, 18));
        core1.put(3, new ErdaConsumeOption(1, 20));
        core1.put(4, new ErdaConsumeOption(1, 23));
        core1.put(5, new ErdaConsumeOption(1, 25));
        core1.put(6, new ErdaConsumeOption(1, 28));
        core1.put(7, new ErdaConsumeOption(2, 30));
        core1.put(8, new ErdaConsumeOption(2, 33));
        core1.put(9, new ErdaConsumeOption(5, 100));
        core1.put(10, new ErdaConsumeOption(2, 40));
        core1.put(11, new ErdaConsumeOption(2, 45));
        core1.put(12, new ErdaConsumeOption(2, 50));
        core1.put(13, new ErdaConsumeOption(2, 55));
        core1.put(14, new ErdaConsumeOption(2, 60));
        core1.put(15, new ErdaConsumeOption(2, 65));
        core1.put(16, new ErdaConsumeOption(2, 70));
        core1.put(17, new ErdaConsumeOption(2, 75));
        core1.put(18, new ErdaConsumeOption(3, 80));
        core1.put(19, new ErdaConsumeOption(8, 175));
        core1.put(20, new ErdaConsumeOption(3, 85));
        core1.put(21, new ErdaConsumeOption(3, 90));
        core1.put(22, new ErdaConsumeOption(3, 95));
        core1.put(23, new ErdaConsumeOption(3, 100));
        core1.put(24, new ErdaConsumeOption(3, 105));
        core1.put(25, new ErdaConsumeOption(3, 110));
        core1.put(26, new ErdaConsumeOption(3, 115));
        core1.put(27, new ErdaConsumeOption(3, 120));
        core1.put(28, new ErdaConsumeOption(4, 125));
        core1.put(29, new ErdaConsumeOption(10, 250));
        data.put(1, core1);

        // 強化核心強化資訊
        Map<Integer, ErdaConsumeOption> core2 = new HashMap<>();
        core2.put(1, new ErdaConsumeOption(1, 23));
        core2.put(2, new ErdaConsumeOption(1, 27));
        core2.put(3, new ErdaConsumeOption(1, 30));
        core2.put(4, new ErdaConsumeOption(2, 34));
        core2.put(5, new ErdaConsumeOption(2, 38));
        core2.put(6, new ErdaConsumeOption(2, 42));
        core2.put(7, new ErdaConsumeOption(3, 45));
        core2.put(8, new ErdaConsumeOption(3, 49));
        core2.put(9, new ErdaConsumeOption(8, 150));
        core2.put(10, new ErdaConsumeOption(3, 60));
        core2.put(11, new ErdaConsumeOption(3, 68));
        core2.put(12, new ErdaConsumeOption(3, 75));
        core2.put(13, new ErdaConsumeOption(3, 83));
        core2.put(14, new ErdaConsumeOption(3, 90));
        core2.put(15, new ErdaConsumeOption(3, 98));
        core2.put(16, new ErdaConsumeOption(3, 105));
        core2.put(17, new ErdaConsumeOption(3, 113));
        core2.put(18, new ErdaConsumeOption(4, 120));
        core2.put(19, new ErdaConsumeOption(12, 263));
        core2.put(20, new ErdaConsumeOption(4, 128));
        core2.put(21, new ErdaConsumeOption(4, 135));
        core2.put(22, new ErdaConsumeOption(4, 143));
        core2.put(23, new ErdaConsumeOption(4, 150));
        core2.put(24, new ErdaConsumeOption(4, 158));
        core2.put(25, new ErdaConsumeOption(5, 165));
        core2.put(26, new ErdaConsumeOption(5, 173));
        core2.put(27, new ErdaConsumeOption(5, 180));
        core2.put(28, new ErdaConsumeOption(6, 188));
        core2.put(29, new ErdaConsumeOption(15, 375));
        data.put(2, core2);

        // 共用核心強化資訊
        Map<Integer, ErdaConsumeOption> core3 = new HashMap<>();
        core3.put(1, new ErdaConsumeOption(1, 30));
        core3.put(2, new ErdaConsumeOption(1, 35));
        core3.put(3, new ErdaConsumeOption(1, 40));
        core3.put(4, new ErdaConsumeOption(2, 45));
        core3.put(5, new ErdaConsumeOption(2, 50));
        core3.put(6, new ErdaConsumeOption(2, 55));
        core3.put(7, new ErdaConsumeOption(3, 60));
        core3.put(8, new ErdaConsumeOption(3, 65));
        core3.put(9, new ErdaConsumeOption(10, 200));
        core3.put(10, new ErdaConsumeOption(3, 80));
        core3.put(11, new ErdaConsumeOption(3, 90));
        core3.put(12, new ErdaConsumeOption(4, 100));
        core3.put(13, new ErdaConsumeOption(4, 110));
        core3.put(14, new ErdaConsumeOption(4, 120));
        core3.put(15, new ErdaConsumeOption(4, 130));
        core3.put(16, new ErdaConsumeOption(4, 140));
        core3.put(17, new ErdaConsumeOption(4, 150));
        core3.put(18, new ErdaConsumeOption(5, 160));
        core3.put(19, new ErdaConsumeOption(15, 350));
        core3.put(20, new ErdaConsumeOption(5, 170));
        core3.put(21, new ErdaConsumeOption(5, 180));
        core3.put(22, new ErdaConsumeOption(5, 190));
        core3.put(23, new ErdaConsumeOption(5, 200));
        core3.put(24, new ErdaConsumeOption(5, 210));
        core3.put(25, new ErdaConsumeOption(6, 220));
        core3.put(26, new ErdaConsumeOption(6, 230));
        core3.put(27, new ErdaConsumeOption(6, 240));
        core3.put(28, new ErdaConsumeOption(7, 250));
        core3.put(29, new ErdaConsumeOption(20, 500));
        data.put(3, core3);

        return data;
    }

    /**
     * 開啟屬性核心消耗資訊
     *
     * @return ErdaConsumeOption
     */
    public static ErdaConsumeOption hexaOpenStatCoreInfo() {
        return new ErdaConsumeOption(5, 10);
    }

    /**
     * 屬性核心強化消耗資訊
     *
     * @return Map<Integer, ErdaConsumeOption>
     */
    public static Map<Integer, ErdaConsumeOption> hexaEnforcementStatInfo() {
        Map<Integer, ErdaConsumeOption> data = new HashMap<>();
        data.put(0, new ErdaConsumeOption(0, 10));
        data.put(1, new ErdaConsumeOption(0, 10));
        data.put(2, new ErdaConsumeOption(0, 10));
        data.put(3, new ErdaConsumeOption(0, 20));
        data.put(4, new ErdaConsumeOption(0, 20));
        data.put(5, new ErdaConsumeOption(0, 20));
        data.put(6, new ErdaConsumeOption(0, 20));
        data.put(7, new ErdaConsumeOption(0, 30));
        data.put(8, new ErdaConsumeOption(0, 30));
        data.put(9, new ErdaConsumeOption(0, 50));
        data.put(10, new ErdaConsumeOption(0, 50));
        return data;
    }

    /**
     * 屬性核心強化機率表
     */
    public static double[] hexaEnforcementStatProbability = {0.35d, 0.35d, 0.35d, 0.2d, 0.2d, 0.2d, 0.2d, 0.15d, 0.1d, 0.05d};

    public static int getHexaSkillCoreType(int statid) {
        return statid / 40000000; // todo: 靈魂亞努斯
    }
}
