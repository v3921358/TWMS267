package Server.channel.handler;

import Client.inventory.*;
import Config.constants.ItemConstants;
import Config.constants.enums.ScrollIconType;
import Config.constants.enums.ScrollOptionType;
import Config.constants.enums.SpellTraceScrollType;
import Net.server.MapleItemInformationProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class EnchantHandler {

    public static ArrayList<EnchantScrollEntry> getScrollList(Equip equip) {
        ArrayList<EnchantScrollEntry> ret = new ArrayList<>();
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (ii.getSlots(equip.getItemId()) + equip.getTotalHammer() <= 0) {
            return ret;
        }
        if (equip.getRestUpgradeCount() > 0) {
            if (ItemConstants.類型.武器(equip.getItemId()) || ItemConstants.類型.心臟(equip.getItemId()) || MapleWeapon.雙刀.check(equip.getItemId())) {
                Map<EnchantScrollFlag, Integer> stats = new HashMap<>();
                stats.put(EnchantScrollFlag.物攻, 5);
                ret.add(new EnchantScrollEntry("武器攻擊力卷軸", 100, ScrollIconType.卷軸_100Percentage, SpellTraceScrollType.一般卷軸, ScrollOptionType.力量卷軸100, 160, stats));
                stats = new HashMap<>();
                stats.put(EnchantScrollFlag.物攻, 5);
                ret.add(new EnchantScrollEntry("武器攻擊力卷軸", 70, ScrollIconType.卷軸_70Percentage, SpellTraceScrollType.一般卷軸, ScrollOptionType.力量卷軸70, 200, stats));
                stats = new HashMap<>();
                stats.put(EnchantScrollFlag.物攻, 5);
                ret.add(new EnchantScrollEntry("武器攻擊力卷軸", 30, ScrollIconType.卷軸_30Percentage, SpellTraceScrollType.一般卷軸, ScrollOptionType.力量卷軸30, 250, stats));
                stats = new HashMap<>();
                stats.put(EnchantScrollFlag.物攻, 5);
                ret.add(new EnchantScrollEntry("武器攻擊力卷軸", 15, ScrollIconType.卷軸_15Percentage, SpellTraceScrollType.一般卷軸, ScrollOptionType.力量卷軸15, 320, stats));
            } else {
                Map<EnchantScrollFlag, Integer> stats = new HashMap<>();
                stats.put(EnchantScrollFlag.物攻, 5);
                ret.add(new EnchantScrollEntry("攻擊力卷軸", 100, ScrollIconType.卷軸_100Percentage, SpellTraceScrollType.一般卷軸, ScrollOptionType.力量卷軸100, 160, stats));
                stats = new HashMap<>();
                stats.put(EnchantScrollFlag.物攻, 5);
                ret.add(new EnchantScrollEntry("攻擊力卷軸", 70, ScrollIconType.卷軸_70Percentage, SpellTraceScrollType.一般卷軸, ScrollOptionType.力量卷軸70, 200, stats));
                stats = new HashMap<>();
                stats.put(EnchantScrollFlag.物攻, 5);
                ret.add(new EnchantScrollEntry("攻擊力卷軸", 30, ScrollIconType.卷軸_30Percentage, SpellTraceScrollType.一般卷軸, ScrollOptionType.力量卷軸30, 250, stats));
                stats = new HashMap<>();
                stats.put(EnchantScrollFlag.物攻, 5);
                ret.add(new EnchantScrollEntry("攻擊力卷軸", 15, ScrollIconType.卷軸_15Percentage, SpellTraceScrollType.一般卷軸, ScrollOptionType.力量卷軸15, 320, stats));
            }
        }
        if (equip.getRestUpgradeCount() != ii.getSlots(equip.getItemId()) + equip.getTotalHammer()) {
            ret.add(new EnchantScrollEntry("回真卷軸", 100, ScrollIconType.回真卷軸, SpellTraceScrollType.回真卷軸, ScrollOptionType.回真卷軸, 12000, new HashMap<>()));
            ret.add(new EnchantScrollEntry("亞克回真卷軸", 100, ScrollIconType.亞克回真卷軸, SpellTraceScrollType.回真卷軸, ScrollOptionType.亞克回真卷軸, 24000, new HashMap<>()));
        }
        if (equip.getCurrentUpgradeCount() + equip.getRestUpgradeCount() != ii.getSlots(equip.getItemId()) + equip.getTotalHammer()) {
            ret.add(new EnchantScrollEntry("純白的卷軸", 100, ScrollIconType.純白的卷軸, SpellTraceScrollType.純白的卷軸, ScrollOptionType.純白的卷軸, 20000, new HashMap<>()));
        }
        if (equip.getTotalHammer() > 0) {
            ret.add(new EnchantScrollEntry("黃金鐵鎚", 100, ScrollIconType.黃金鐵鎚, SpellTraceScrollType.黃金鐵鎚, ScrollOptionType.黃金鐵鎚, 24000, new HashMap<>()));
        }
        return ret;
    }

    public static long getStarForceMeso(int equiplevel, int enhanced, boolean superior) {
        if (superior) {
            return getSuperiorStarForceMeso(equiplevel);
        }
        final long[] sfMeso;
        if (equiplevel < 100) {
            sfMeso = new long[]{41000, 81000, 121000, 161000, 201000, 241000, 281000, 321000};
        } else if (equiplevel < 110) {
            sfMeso = new long[]{54200, 107500, 160700, 214000, 267200, 320400, 373700, 426900, 480200, 533400};
        } else if (equiplevel < 120) {
            sfMeso = new long[]{70100, 139200, 208400, 277500, 346600, 415700, 484800, 554000, 623100, 692200, 5602100, 7085400, 8794500, 10742400, 12941800};
        } else if (equiplevel < 130) {
            sfMeso = new long[]{88900, 176800, 264600, 352500, 440400, 528300, 616200, 704000, 791900, 879800, 7122300, 9008200, 11181100, 13657700, 16454100, 19586000, 23069100, 26918600, 31149300, 35776100};
        } else if (equiplevel < 140) {
            sfMeso = new long[]{110800, 220500, 330300, 440000, 549800, 659600, 769300, 879100, 988800, 1098600, 8895400, 11250800, 13964700, 17057900, 20550500, 24462200, 28812500, 33620400, 38904500, 44683300, 50974700, 57796700, 65166700, 73102200, 81620200};
        } else {
            sfMeso = new long[]{136000, 271000, 406000, 541000, 676000, 811000, 946000, 1081000, 1216000, 1351000, 10940700, 13837700, 17175800, 20980200, 25275900, 30087200, 35437900, 41351400, 47850600, 54985200, 62696400, 71087200, 80152000, 89912300, 100389000};
        }
        return sfMeso.length + 1 < enhanced ? -1 : sfMeso[enhanced];
    }

    public static long getSuperiorStarForceMeso(int equiplevel) {
        final long[] sfMeso = {55832200, 55832200, 55832200, 55832200, 55832200, 55832200};
        if (equiplevel >= 0 && equiplevel <= 109) {
            equiplevel = 0;
        } else if (equiplevel >= 110 && equiplevel <= 119) {
            equiplevel = 1;
        } else if (equiplevel >= 120 && equiplevel <= 129) {
            equiplevel = 2;
        } else if (equiplevel >= 130 && equiplevel <= 139) {
            equiplevel = 3;
        } else if (equiplevel >= 140 && equiplevel <= 149) {
            equiplevel = 4;
        } else {
            equiplevel = 5;
        }
        return sfMeso[equiplevel];
    }

    public static Map<EnchantScrollFlag, Integer> getEnchantScrollList(Item item) {
        Map<EnchantScrollFlag, Integer> ret = new HashMap<>();
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        int statusFlag = 0;
        int reqJob = ii.getReqJob(item.getItemId());
        int reqLevel = ii.getReqLevel(item.getItemId());
        Equip nEquip = (Equip) item;
        int watk = nEquip.getPad() + nEquip.getStarForce().getPad();
        int matk = nEquip.getMad() + nEquip.getStarForce().getMad();
        int pdd = nEquip.getTotalPdd();
        if ((reqJob & 0x1) != 0) { // 劍士
            statusFlag |= EnchantScrollFlag.力量.getValue();
            statusFlag |= EnchantScrollFlag.敏捷.getValue();
        }
        if ((reqJob & 0x2) != 0) { // 法師
            statusFlag |= EnchantScrollFlag.智力.getValue();
            statusFlag |= EnchantScrollFlag.幸運.getValue();
        }
        if ((reqJob & 0x4) != 0) { // 弓箭手
            statusFlag |= EnchantScrollFlag.敏捷.getValue();
            statusFlag |= EnchantScrollFlag.力量.getValue();
        }
        if ((reqJob & 0x8) != 0) { // 盜賊
            statusFlag |= EnchantScrollFlag.幸運.getValue();
            statusFlag |= EnchantScrollFlag.敏捷.getValue();
        }
        if ((reqJob & 0x10) != 0) { // 海盜
            statusFlag |= EnchantScrollFlag.力量.getValue();
            statusFlag |= EnchantScrollFlag.敏捷.getValue();
        }
        if (reqJob <= 0) { // 全職&&初心者
            statusFlag |= EnchantScrollFlag.力量.getValue();
            statusFlag |= EnchantScrollFlag.敏捷.getValue();
            statusFlag |= EnchantScrollFlag.智力.getValue();
            statusFlag |= EnchantScrollFlag.幸運.getValue();
        }
        int enhance = nEquip.getStarForceLevel();
        if (ii.isSuperiorEquip(item.getItemId())) {
            switch (enhance) {
                case 0:
                    if (EnchantScrollFlag.力量.check(statusFlag)) {
                        ret.put(EnchantScrollFlag.力量, 19);
                    }
                    if (EnchantScrollFlag.敏捷.check(statusFlag)) {
                        ret.put(EnchantScrollFlag.敏捷, 19);
                    }
                    if (EnchantScrollFlag.智力.check(statusFlag)) {
                        ret.put(EnchantScrollFlag.智力, 19);
                    }
                    if (EnchantScrollFlag.幸運.check(statusFlag)) {
                        ret.put(EnchantScrollFlag.幸運, 19);
                    }
                    break;
                case 1:
                    if (EnchantScrollFlag.力量.check(statusFlag)) {
                        ret.put(EnchantScrollFlag.力量, 20);
                    }
                    if (EnchantScrollFlag.敏捷.check(statusFlag)) {
                        ret.put(EnchantScrollFlag.敏捷, 20);
                    }
                    if (EnchantScrollFlag.智力.check(statusFlag)) {
                        ret.put(EnchantScrollFlag.智力, 20);
                    }
                    if (EnchantScrollFlag.幸運.check(statusFlag)) {
                        ret.put(EnchantScrollFlag.幸運, 20);
                    }
                    break;
                case 2:
                    if (EnchantScrollFlag.力量.check(statusFlag)) {
                        ret.put(EnchantScrollFlag.力量, 22);
                    }
                    if (EnchantScrollFlag.敏捷.check(statusFlag)) {
                        ret.put(EnchantScrollFlag.敏捷, 22);
                    }
                    if (EnchantScrollFlag.智力.check(statusFlag)) {
                        ret.put(EnchantScrollFlag.智力, 22);
                    }
                    if (EnchantScrollFlag.幸運.check(statusFlag)) {
                        ret.put(EnchantScrollFlag.幸運, 22);
                    }
                    break;
                case 3:
                    if (EnchantScrollFlag.力量.check(statusFlag)) {
                        ret.put(EnchantScrollFlag.力量, 25);
                    }
                    if (EnchantScrollFlag.敏捷.check(statusFlag)) {
                        ret.put(EnchantScrollFlag.敏捷, 25);
                    }
                    if (EnchantScrollFlag.智力.check(statusFlag)) {
                        ret.put(EnchantScrollFlag.智力, 25);
                    }
                    if (EnchantScrollFlag.幸運.check(statusFlag)) {
                        ret.put(EnchantScrollFlag.幸運, 25);
                    }
                    break;
                case 4:
                    if (EnchantScrollFlag.力量.check(statusFlag)) {
                        ret.put(EnchantScrollFlag.力量, 29);
                    }
                    if (EnchantScrollFlag.敏捷.check(statusFlag)) {
                        ret.put(EnchantScrollFlag.敏捷, 29);
                    }
                    if (EnchantScrollFlag.智力.check(statusFlag)) {
                        ret.put(EnchantScrollFlag.智力, 29);
                    }
                    if (EnchantScrollFlag.幸運.check(statusFlag)) {
                        ret.put(EnchantScrollFlag.幸運, 29);
                    }
                    break;
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                    ret.put(EnchantScrollFlag.物攻, enhance + 4);
                    ret.put(EnchantScrollFlag.魔攻, enhance + 4);
                    break;
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                    ret.put(EnchantScrollFlag.物攻, 15 + 2 * (enhance - 10));
                    ret.put(EnchantScrollFlag.魔攻, 15 + 2 * (enhance - 10));
                    break;
            }
            return ret;
        }
        // 普通強化
        int max = 0;
        switch (enhance) {
            case 0:
            case 1:
            case 2:
                max = 5;
                break;
            case 3:
            case 4:
                max = 10;
                break;
            case 5:
            case 6:
            case 7:
                max = 15;
                break;
            case 8:
                max = 20;
                break;
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
                max = 25;
                break;
            case 14:
                max = 30;
                break;
        }
        if (ItemConstants.類型.武器(item.getItemId()) || MapleWeapon.雙刀.check(item.getItemId())) {
            int allStats;
            if (enhance >= 0 && enhance < 5) {
                allStats = 2;
            } else if (enhance >= 5 && enhance < 15) {
                allStats = 3;
            } else {
                if (reqLevel >= 200) {
                    allStats = 15;
                } else if (reqLevel >= 160) {
                    allStats = 13;
                } else {
                    allStats = 11;
                }
            }
            if (EnchantScrollFlag.力量.check(statusFlag)) {
                ret.put(EnchantScrollFlag.力量, allStats);
            }
            if (EnchantScrollFlag.敏捷.check(statusFlag)) {
                ret.put(EnchantScrollFlag.敏捷, allStats);
            }
            if (EnchantScrollFlag.智力.check(statusFlag)) {
                ret.put(EnchantScrollFlag.智力, allStats);
            }
            if (EnchantScrollFlag.幸運.check(statusFlag)) {
                ret.put(EnchantScrollFlag.幸運, allStats);
            }
            ret.put(EnchantScrollFlag.Hp, max);
            ret.put(EnchantScrollFlag.Mp, max);
            if (enhance < 15) {
                ret.put(EnchantScrollFlag.物攻, (int) Math.floor(watk / 50.0D) + 1);
                ret.put(EnchantScrollFlag.魔攻, (int) Math.floor(matk / 50.0D) + 1);
            } else {
                int value = 0;
                switch (enhance) {
                    case 15:
                        if (reqLevel >= 200) {
                            value = 13;
                        } else if (reqLevel >= 160) {
                            value = 9;
                        } else {
                            value = 8;
                        }
                        break;
                    case 16:
                        if (reqLevel >= 200) {
                            value = 13;
                        } else if (reqLevel >= 160) {
                            value = 9;
                        } else {
                            value = 9;
                        }
                        break;
                    case 17:
                        if (reqLevel >= 200) {
                            value = 14;
                        } else if (reqLevel >= 160) {
                            value = 10;
                        } else {
                            value = 9;
                        }
                        break;
                    case 18:
                        if (reqLevel >= 200) {
                            value = 14;
                        } else if (reqLevel >= 160) {
                            value = 11;
                        } else {
                            value = 10;
                        }
                        break;
                    case 19:
                        if (reqLevel >= 200) {
                            value = 15;
                        } else if (reqLevel >= 160) {
                            value = 12;
                        } else {
                            value = 11;
                        }
                        break;
                    case 20:
                        if (reqLevel >= 200) {
                            value = 16;
                        } else if (reqLevel >= 160) {
                            value = 13;
                        } else {
                            value = 12;
                        }
                        break;
                    case 21:
                        if (reqLevel >= 200) {
                            value = 17;
                        } else if (reqLevel >= 160) {
                            value = 14;
                        } else {
                            value = 13;
                        }
                        break;
                    case 22:
                        if (reqLevel >= 200) {
                            value = 34;
                        } else if (reqLevel >= 160) {
                            value = 32;
                        } else {
                            value = 31;
                        }
                        break;
                    case 23:
                        if (reqLevel >= 200) {
                            value = 35;
                        } else if (reqLevel >= 160) {
                            value = 33;
                        } else {
                            value = 32;
                        }
                        break;
                    case 24:
                        if (reqLevel >= 200) {
                            value = 36;
                        } else if (reqLevel >= 160) {
                            value = 34;
                        } else {
                            value = 33;
                        }
                        break;
                }
                ret.put(EnchantScrollFlag.物攻, value);
                ret.put(EnchantScrollFlag.魔攻, value);
            }
        } else {
            int allStats;
            if (enhance >= 0 && enhance < 5) {
                allStats = 2;
            } else if (enhance >= 5 && enhance < 15) {
                allStats = 3;
            } else {
                if (reqLevel >= 200) {
                    allStats = 15;
                } else if (reqLevel >= 160) {
                    allStats = 13;
                } else {
                    allStats = 11;
                }
            }
            if (EnchantScrollFlag.力量.check(statusFlag)) {
                ret.put(EnchantScrollFlag.力量, allStats);
            }
            if (EnchantScrollFlag.敏捷.check(statusFlag)) {
                ret.put(EnchantScrollFlag.敏捷, allStats);
            }
            if (EnchantScrollFlag.智力.check(statusFlag)) {
                ret.put(EnchantScrollFlag.智力, allStats);
            }
            if (EnchantScrollFlag.幸運.check(statusFlag)) {
                ret.put(EnchantScrollFlag.幸運, allStats);
            }

            if (!ItemConstants.類型.臉飾(item.getItemId()) && !ItemConstants.類型.眼飾(item.getItemId()) && !ItemConstants.類型.耳環(item.getItemId())) {
                ret.put(EnchantScrollFlag.Hp, max);
            }

            if (enhance >= 15) {
                int value = 0;
                switch (enhance) {
                    case 15:
                        if (reqLevel >= 200) {
                            value = 12;
                        } else if (reqLevel >= 160) {
                            value = 10;
                        } else {
                            value = 9;
                        }
                        break;
                    case 16:
                        if (reqLevel >= 200) {
                            value = 13;
                        } else if (reqLevel >= 160) {
                            value = 11;
                        } else {
                            value = 10;
                        }
                        break;
                    case 17:
                        if (reqLevel >= 200) {
                            value = 14;
                        } else if (reqLevel >= 160) {
                            value = 12;
                        } else {
                            value = 11;
                        }
                        break;
                    case 18:
                        if (reqLevel >= 200) {
                            value = 15;
                        } else if (reqLevel >= 160) {
                            value = 13;
                        } else {
                            value = 12;
                        }
                        break;
                    case 19:
                        if (reqLevel >= 200) {
                            value = 16;
                        } else if (reqLevel >= 160) {
                            value = 14;
                        } else {
                            value = 13;
                        }
                        break;
                    case 20:
                        if (reqLevel >= 200) {
                            value = 17;
                        } else if (reqLevel >= 160) {
                            value = 15;
                        } else {
                            value = 14;
                        }
                        break;
                    case 21:
                        if (reqLevel >= 200) {
                            value = 19;
                        } else if (reqLevel >= 160) {
                            value = 17;
                        } else {
                            value = 16;
                        }
                        break;
                    case 22:
                        if (reqLevel >= 200) {
                            value = 21;
                        } else if (reqLevel >= 160) {
                            value = 19;
                        } else {
                            value = 18;
                        }
                        break;
                    case 23:
                        if (reqLevel >= 200) {
                            value = 23;
                        } else if (reqLevel >= 160) {
                            value = 21;
                        } else {
                            value = 20;
                        }
                        break;
                    case 24:
                        if (reqLevel >= 200) {
                            value = 25;
                        } else if (reqLevel >= 160) {
                            value = 23;
                        } else {
                            value = 22;
                        }
                        break;
                }
                ret.put(EnchantScrollFlag.物攻, value);
                ret.put(EnchantScrollFlag.魔攻, value);
            } else if (ItemConstants.類型.手套(item.getItemId())) {
                int value = 0;
                switch (enhance) {
                    case 4:
                    case 6:
                    case 8:
                    case 10:
                    case 12:
                        value = 1;
                        break;
                    case 13:
                        if (reqLevel >= 200) {
                            value = 1;
                        }
                        break;
                    case 14:
                        if (reqLevel >= 200) {
                            value = 1;
                        } else {
                            value = 2;
                        }
                        break;
                }
                if ((reqJob & 0x2) == 0) {
                    ret.put(EnchantScrollFlag.物攻, value);
                }
                if ((reqJob & 0x2) != 0 || reqJob <= 0) {
                    ret.put(EnchantScrollFlag.魔攻, value);
                }
            }
        }
        int stat = pdd;
        int addStat = 0;
        for (int i = 0; i < enhance + 1; i++) {
            addStat = (int) Math.ceil((stat) / 20.0D);
            stat += addStat;
        }
        //ret.put(EnchantScrollFlag.物防, addStat);
        return ret;
    }
}
