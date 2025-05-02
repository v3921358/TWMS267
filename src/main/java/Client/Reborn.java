package Client;

import Config.configs.ServerConfig;
import Net.server.MapleItemInformationProvider;
import Net.server.buffs.MapleStatEffect;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static Client.SecondaryStat.*;

public class Reborn {
    private static final Map<Integer, Map<SecondaryStat, Integer>> LEVEL_DATA = new LinkedHashMap<>(40);
    public static final int REBORN_BUFF_ITEM = 2023519;

    private static void inc(Map<SecondaryStat, Integer> map, SecondaryStat stat, int val) {
        map.merge(stat, val, (a, b) -> a + b);
    }

    public static EnumMap<SecondaryStat, Integer> getStatups(int level) {
        EnumMap<SecondaryStat, Integer> map = new EnumMap<>(SecondaryStat.class);
        if (level <= 0) {
            return map;
        }
        inc(map, IndieSTR, 200 * level);
        inc(map, IndieDEX, 200 * level);
        inc(map, IndieINT, 200 * level);
        inc(map, IndieLUK, 200 * level);
        inc(map, IndieMHP, 1000 * level);
        inc(map, IndieMMP, 1000 * level);
        switch (level) {
            default:
            case 30:
                inc(map, IndiePMdR, 20);
                //fallthrough
            case 29:
                inc(map, IndieBooster, -1);
                //fallthrough
            case 28:
                inc(map, IgnoreMobpdpR, 30);
                //fallthrough
            case 27:
                inc(map, IndiePADR, 30);
                inc(map, IndieMADR, 30);
                //fallthrough
            case 26:
                inc(map, IndieBDR, 30);
                //fallthrough
            case 25:
//                inc(map, IndieDamR, 1);
                //fallthrough
            case 24:
                inc(map, IndieCr, 10);
                //fallthrough
            case 23:
                inc(map, IndieDamR, 10);
                //fallthrough
            case 22:
                inc(map, IndieCD, 10);
                //fallthrough
            case 21:
                inc(map, IndieBooster, -1);
                //fallthrough
            case 20:
                inc(map, IndieCD, 2);
                //fallthrough
            case 19:
                inc(map, IndieCD, 2);
                //fallthrough
            case 18:
                inc(map, IndieCD, 2);
                //fallthrough
            case 17:
                inc(map, IndieCD, 2);
                //fallthrough
            case 16:
                inc(map, IndieCD, 2);
                //fallthrough
            case 15:
                inc(map, IndieBDR, 4);
                //fallthrough
            case 14:
                inc(map, IndieBDR, 4);
                //fallthrough
            case 13:
                inc(map, IndieBDR, 4);
                //fallthrough
            case 12:
                inc(map, IndieBDR, 4);
                //fallthrough
            case 11:
                inc(map, IndieBDR, 4);
                //fallthrough
            case 10:
                inc(map, IndieIgnoreMobpdpR, 5);
                //fallthrough
            case 9:
                inc(map, IndieIgnoreMobpdpR, 5);
                //fallthrough
            case 8:
                inc(map, IndieIgnoreMobpdpR, 5);
                //fallthrough
            case 7:
                inc(map, IndieIgnoreMobpdpR, 5);
                //fallthrough
            case 6:
                inc(map, IndieIgnoreMobpdpR, 5);
                //fallthrough
            case 5:
                inc(map, IndieIgnoreMobpdpR, 5);
                inc(map, IndieCr, 5);
                //fallthrough
            case 4:
                inc(map, IndieBDR, 5);
                //fallthrough
            case 3:
                inc(map, IndieBooster, -1);
                inc(map, IndiePAD, 50);
                inc(map, IndieMAD, 50);
                //fallthrough
            case 2:
                inc(map, IndiePAD, 50);
                inc(map, IndieMAD, 50);
                //fallthrough
            case 1:
                //fallthrough
        }
        return map;
    }

    private static MapleStatEffect getEffect(int level) {
        return MapleItemInformationProvider.getInstance().getItemEffect(REBORN_BUFF_ITEM);
    }

    public static void giveRebornBuff(MapleCharacter chr) {
        if (!ServerConfig.EnableRebornBuff) {
            return;
        }
        chr.dispelEffect(REBORN_BUFF_ITEM);
        int level = chr.getReborns();
        if (level <= 0) {
            return;
        }
        MapleStatEffect effect = getEffect(level);
        effect.applyTo(chr, true);
    }
}
