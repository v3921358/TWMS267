package Client.inventory;

import Config.configs.ServerConfig;
import Config.constants.ItemConstants;
import Net.server.MapleItemInformationProvider;
import tools.Pair;
import tools.Randomizer;

import java.util.*;

public final class NirvanaFlame {
    private static final double[] NORMAL_RATES = new double[]{1.0D, 2.0D, 3.65D, 5.35D, 7.3D, 8.8D, 10.25D, 11.7D, 13.15D};
    private static final double[] SPECIAL_RATES = new double[]{1.0D, 2.0D, 3.0D, 4.4D, 6.05D, 8.0D, 10.25D, 11.7D, 13.17D};

    public enum EquipExFlag {
        FLAGEx_iSTR(0),
        FLAGEx_iDEX(10),
        FLAGEx_iINT(20),
        FLAGEx_iLUK(30),
        FLAGEx_iSTR_DEX(40),
        FLAGEx_iSTR_INT(50),
        FLAGEx_iSTR_LUK(60),
        FLAGEx_iDEX_INT(70),
        FLAGEx_iDEX_LUK(80),
        FLAGEx_iINT_LUK(90),
        FLAGEx_iMAXHP(100),
        FLAGEx_iMAXMP(110),
        FLAGEx_iREQLEVEL(120),
        FLAGEx_iPDD(130),
        FLAGEx_iPAD(170),
        FLAGEx_iMAD(180),
        FLAGEx_iSPEED(190),
        FLAGEx_iJUMP(200),
        FLAGEx_iBDR(210),
        FLAGEx_iDAMR(230),
        FLAGEx_iSTATR(240);

        private final int type;
        private static final Map<Integer, EquipExFlag> Flags = new HashMap<>();

        EquipExFlag(int type) {
            this.type = type;
        }

        public static EquipExFlag getByType(final int n) {
            return Flags.get(n);
        }

        public final int getType() {
            return this.type;
        }

        static {
            EquipExFlag[] values;
            for (int length = (values = values()).length, i = 0; i < length; ++i) {
                final EquipExFlag flag = values[i];
                Flags.put(flag.type, flag);
            }
        }

    }

    private int str;
    private int dex;
    private int _int;
    private int luk;
    private int hp;
    private int mp;
    private int pad;
    private int mad;
    private int reqLevel;
    private int pdd;
    private int mdd;
    private int acc;
    private int avoid;
    private int hands;
    private int speed;
    private int jump;
    private int bossDamage;
    private int ignorePDR;
    private int totalDamage;
    private int allStat;

    private long flag;

    public NirvanaFlame() {
        reset();
    }

    public NirvanaFlame(NirvanaFlame n) {
        str = n.str;
        dex = n.dex;
        _int = n._int;
        luk = n.luk;
        hp = n.hp;
        mp = n.mp;
        pad = n.pad;
        mad = n.mad;
        pdd = n.pdd;
        mdd = n.mdd;
        acc = n.acc;
        avoid = n.avoid;
        hands = n.hands;
        speed = n.speed;
        jump = n.jump;
        bossDamage = n.bossDamage;
        ignorePDR = n.ignorePDR;
        totalDamage = n.totalDamage;
        allStat = n.allStat;
        flag = n.flag;
    }

    public void reset() {
        str = 0;
        dex = 0;
        _int = 0;
        luk = 0;
        hp = 0;
        mp = 0;
        pad = 0;
        mad = 0;
        pdd = 0;
        mdd = 0;
        acc = 0;
        avoid = 0;
        hands = 0;
        speed = 0;
        jump = 0;
        bossDamage = 0;
        ignorePDR = 0;
        totalDamage = 0;
        allStat = 0;
        flag = 0;
    }

    public static long randomStateFromJson(final Equip equip, int nfId) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (nfId == 0) {
            if (ii.isCash(equip.getItemId())) {
                return 0;
            }
            if (!ItemConstants.類型.口袋道具(equip.getItemId()) &&
                    !ItemConstants.類型.墜飾(equip.getItemId()) &&
                    !ItemConstants.類型.武器(equip.getItemId()) &&
                    !ItemConstants.類型.腰帶(equip.getItemId()) &&
                    !ItemConstants.類型.帽子(equip.getItemId()) &&
                    !ItemConstants.類型.臉飾(equip.getItemId()) &&
                    !ItemConstants.類型.眼飾(equip.getItemId()) &&
                    !ItemConstants.類型.上衣(equip.getItemId()) &&
                    !ItemConstants.類型.套服(equip.getItemId()) &&
                    !ItemConstants.類型.褲裙(equip.getItemId()) &&
                    !ItemConstants.類型.鞋子(equip.getItemId()) &&
                    !ItemConstants.類型.耳環(equip.getItemId()) &&
                    !ItemConstants.類型.手套(equip.getItemId()) &&
                    !ItemConstants.類型.披風(equip.getItemId())) {
                return 0;
            }
        }
        int statCountMin = 1;
        int statCountMax;
        if (equip.getReqLevel() < 30) {
            statCountMax = 2;
        } else if (equip.getReqLevel() < 90) {
            statCountMax = 3;
        } else if (equip.getReqLevel() < 110) {
            statCountMax = 3;
        } else {
            statCountMax = 4;
        }
        final boolean isBossReward = ii.isBossReward(equip.getItemId());
        final int statCount = isBossReward || equip.isMvpEquip() ? 4 : Randomizer.rand(statCountMin, statCountMax);
        final Map<EquipExFlag, Integer> statProps = new EnumMap<>(EquipExFlag.class);
        statProps.put(EquipExFlag.FLAGEx_iSTR, 70);
        statProps.put(EquipExFlag.FLAGEx_iDEX, 70);
        statProps.put(EquipExFlag.FLAGEx_iINT, 70);
        statProps.put(EquipExFlag.FLAGEx_iLUK, 70);
        statProps.put(EquipExFlag.FLAGEx_iSTR_DEX, 70);
        statProps.put(EquipExFlag.FLAGEx_iSTR_INT, 70);
        statProps.put(EquipExFlag.FLAGEx_iSTR_LUK, 70);
        statProps.put(EquipExFlag.FLAGEx_iDEX_INT, 70);
        statProps.put(EquipExFlag.FLAGEx_iDEX_LUK, 70);
        statProps.put(EquipExFlag.FLAGEx_iINT_LUK, 70);
        statProps.put(EquipExFlag.FLAGEx_iMAXHP, 65);
        statProps.put(EquipExFlag.FLAGEx_iMAXMP, 65);
        if (equip.getReqLevel() >= 50) {
            statProps.put(EquipExFlag.FLAGEx_iREQLEVEL, 30);
        }
        statProps.put(EquipExFlag.FLAGEx_iPDD, 70);
        if (ItemConstants.類型.武器(equip.getItemId())) {
            if (ItemConstants.類型.魔法武器(equip.getItemId())) {
                statProps.put(EquipExFlag.FLAGEx_iMAD, 70);
            } else {
                statProps.put(EquipExFlag.FLAGEx_iPAD, 70);
            }
            if (equip.getReqLevel() >= 130) {
                statProps.put(EquipExFlag.FLAGEx_iBDR, 10);
                statProps.put(EquipExFlag.FLAGEx_iDAMR, 10);
            }
        } else {
            statProps.put(EquipExFlag.FLAGEx_iPAD, 60);
            statProps.put(EquipExFlag.FLAGEx_iMAD, 60);
            statProps.put(EquipExFlag.FLAGEx_iSPEED, 50);
            statProps.put(EquipExFlag.FLAGEx_iJUMP, 50);
        }
        statProps.put(EquipExFlag.FLAGEx_iSTATR, 10);

        Pair<Integer, Integer> tierLimit = new Pair<>(1, 3);
        int[] tierRate = new int[]{60, 39, 1};
        switch (nfId) {
            case 2048716: // 強力的輪迴星火
            case 2048724: // 強力的輪迴星火
                tierRate = new int[] {20, 30, 36, 14};
                if (ServerConfig.KMS_NirvanaFlameTier) {
                    tierLimit.left += 2;
                    tierLimit.right += 3;
                }
                break;
            case 2048717: // 永遠的輪迴星火
            case 2048721: // 永遠的輪迴星火
            case 2048723: // 永遠的輪迴星火
                tierRate = new int[] {29, 45, 25, 1};
                if (ServerConfig.KMS_NirvanaFlameTier) {
                    tierLimit.left += 3;
                    tierLimit.right += 4;
                } else {
                    tierLimit.left += 1;
                }
                break;
            default:
                if (nfId == 2048761 || nfId == 5064502 || ServerConfig.KMS_NirvanaFlameTier) { // 覺醒的輪迴星火 或 韓版星火
                    tierLimit.left += 2;
                    tierLimit.right += 2;
                    if (isBossReward) {
                        tierLimit.left += 2;
                        tierLimit.right += 2;
                    }
                }
                break;
        }

        if (equip.isMvpEquip()) {
            tierLimit.left += 2;
            tierLimit.right += 2;
        }
        final HashMap<EquipExFlag, Integer> nfStats = new HashMap<>();
        long flag = 0L;
        final List<EquipExFlag> list = Arrays.asList(EquipExFlag.values());
        while (nfStats.size() < statCount) {
            Collections.shuffle(list);
            final EquipExFlag equipExFlag = list.get(Randomizer.nextInt(list.size()));
            if (statProps.containsKey(equipExFlag) && !nfStats.containsKey(equipExFlag) && Randomizer.isSuccess(statProps.get(equipExFlag))) {
                int tier;
                if (ServerConfig.KMS_NirvanaFlameTier && nfStats.isEmpty() && (nfId == 2048717 || nfId == 2048721 || nfId == 2048723)) {
                    tier = tierLimit.right;
                } else {
                    tier = randomTier(tierRate, tierLimit);
                }
                nfStats.put(equipExFlag, Math.min(9, tier));
            }
        }
        for (final Map.Entry<EquipExFlag, Integer> entry : nfStats.entrySet()) {
            flag = flag * 1000L + (entry.getKey().getType() + entry.getValue());
        }
        equip.setFlameFlag(flag);
        return flag;
    }

    public static void randomState(final Equip equip, int nfId) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (nfId == 0) {
            if (ii.isCash(equip.getItemId())) {
                return;
            }
            if (!ItemConstants.類型.口袋道具(equip.getItemId()) &&
                    !ItemConstants.類型.墜飾(equip.getItemId()) &&
                    !ItemConstants.類型.圖騰(equip.getItemId()) &&
                    !ItemConstants.類型.武器(equip.getItemId()) &&
                    !ItemConstants.類型.腰帶(equip.getItemId()) &&
                    !ItemConstants.類型.帽子(equip.getItemId()) &&
                    !ItemConstants.類型.臉飾(equip.getItemId()) &&
                    !ItemConstants.類型.眼飾(equip.getItemId()) &&
                    !ItemConstants.類型.上衣(equip.getItemId()) &&
                    !ItemConstants.類型.套服(equip.getItemId()) &&
                    !ItemConstants.類型.褲裙(equip.getItemId()) &&
                    !ItemConstants.類型.鞋子(equip.getItemId()) &&
                    !ItemConstants.類型.耳環(equip.getItemId()) &&
                    !ItemConstants.類型.手套(equip.getItemId()) &&
                    !ItemConstants.類型.披風(equip.getItemId())) {
                return;
            }
        }
        int statCountMin = 1;
        int statCountMax;
        if (equip.getReqLevel() < 30) {
            statCountMax = 2;
        } else if (equip.getReqLevel() < 90) {
            statCountMax = 3;
        } else if (equip.getReqLevel() < 110) {
            statCountMax = 3;
        } else {
            statCountMax = 4;
        }
        final boolean isBossReward = ii.isBossReward(equip.getItemId());
        final int statCount = isBossReward || equip.isMvpEquip() ? 4 : Randomizer.rand(statCountMin, statCountMax);
        final Map<EquipExFlag, Integer> statProps = new EnumMap<>(EquipExFlag.class);
        statProps.put(EquipExFlag.FLAGEx_iSTR, 70);
        statProps.put(EquipExFlag.FLAGEx_iDEX, 70);
        statProps.put(EquipExFlag.FLAGEx_iINT, 70);
        statProps.put(EquipExFlag.FLAGEx_iLUK, 70);
        statProps.put(EquipExFlag.FLAGEx_iSTR_DEX, 70);
        statProps.put(EquipExFlag.FLAGEx_iSTR_INT, 70);
        statProps.put(EquipExFlag.FLAGEx_iSTR_LUK, 70);
        statProps.put(EquipExFlag.FLAGEx_iDEX_INT, 70);
        statProps.put(EquipExFlag.FLAGEx_iDEX_LUK, 70);
        statProps.put(EquipExFlag.FLAGEx_iINT_LUK, 70);
        statProps.put(EquipExFlag.FLAGEx_iMAXHP, 65);
        statProps.put(EquipExFlag.FLAGEx_iMAXMP, 65);
        if (equip.getReqLevel() >= 50) {
            statProps.put(EquipExFlag.FLAGEx_iREQLEVEL, 30);
        }
        statProps.put(EquipExFlag.FLAGEx_iPDD, 70);
        if (ItemConstants.類型.武器(equip.getItemId())) {
            if (ItemConstants.類型.魔法武器(equip.getItemId())) {
                statProps.put(EquipExFlag.FLAGEx_iMAD, 70);
            } else {
                statProps.put(EquipExFlag.FLAGEx_iPAD, 70);
            }
            if (equip.getReqLevel() >= 130) {
                statProps.put(EquipExFlag.FLAGEx_iBDR, 10);
                statProps.put(EquipExFlag.FLAGEx_iDAMR, 10);
            }
        } else {
            statProps.put(EquipExFlag.FLAGEx_iPAD, 60);
            statProps.put(EquipExFlag.FLAGEx_iMAD, 60);
            statProps.put(EquipExFlag.FLAGEx_iSPEED, 50);
            statProps.put(EquipExFlag.FLAGEx_iJUMP, 50);
        }
        statProps.put(EquipExFlag.FLAGEx_iSTATR, 10);

        Pair<Integer, Integer> tierLimit = new Pair<>(1, 3);
        int[] tierRate = new int[]{60, 39, 1};
        switch (nfId) {
            case 2048716: // 強力的輪迴星火
            case 2048724: // 強力的輪迴星火
                tierRate = new int[]{20, 30, 36, 14};
                if (ServerConfig.KMS_NirvanaFlameTier) {
                    tierLimit.left += 2;
                    tierLimit.right += 3;
                }
                break;
            case 2048717: // 永遠的輪迴星火
            case 2048721: // 永遠的輪迴星火
            case 2048723: // 永遠的輪迴星火
                tierRate = new int[]{29, 45, 25, 1};
                if (ServerConfig.KMS_NirvanaFlameTier) {
                    tierLimit.left += 3;
                    tierLimit.right += 4;
                } else {
                    tierLimit.left += 1;
                }
                break;
            default:
                if (nfId == 2048761 || nfId == 5064502 || ServerConfig.KMS_NirvanaFlameTier) { // 覺醒的輪迴星火 或 韓版星火
                    tierLimit.left += 2;
                    tierLimit.right += 2;
                    if (isBossReward) {
                        tierLimit.left += 2;
                        tierLimit.right += 2;
                    }
                }
                break;
        }

        if (equip.isMvpEquip()) {
            tierLimit.left += 2;
            tierLimit.right += 2;
        }
        final HashMap<EquipExFlag, Integer> nfStats = new HashMap<>();
        long flag = 0L;
        final List<EquipExFlag> list = Arrays.asList(EquipExFlag.values());
        while (nfStats.size() < statCount) {
            Collections.shuffle(list);
            final EquipExFlag equipExFlag = list.get(Randomizer.nextInt(list.size()));
            if (statProps.containsKey(equipExFlag) && !nfStats.containsKey(equipExFlag) && Randomizer.isSuccess(statProps.get(equipExFlag))) {
                int tier;
                if (ServerConfig.KMS_NirvanaFlameTier && nfStats.isEmpty() && (nfId == 2048717 || nfId == 2048721 || nfId == 2048723)) {
                    tier = tierLimit.right;
                } else {
                    tier = randomTier(tierRate, tierLimit);
                }
                nfStats.put(equipExFlag, Math.min(9, tier));
            }
        }
        for (final Map.Entry<EquipExFlag, Integer> entry : nfStats.entrySet()) {
            flag = flag * 1000L + (entry.getKey().getType() + entry.getValue());
        }
        equip.setFlameFlag(flag);
    }

    private static int randomTier(int[] tierRate, Pair<Integer, Integer> tierLimit) {
        int tier = tierLimit.getLeft() - 1;
        int randomRate = Randomizer.nextInt(100);
        int rRate = 100;
        for (int rate : tierRate) {
            if (tier >= tierLimit.getRight()) {
                break;
            }
            if (rRate > randomRate) {
                tier++;
            }
            rRate -= rate;
        }
        return tier;
    }

    public void resetEquipExStats(final Equip equip) {
        long lFlag = flag;
        reset();
        flag = lFlag;
        final Equip normalEquip = MapleItemInformationProvider.getInstance().getEquipById(equip.getItemId());
        do {
            final int nFlag = (int) (lFlag % 1000);
            lFlag /= 1000L;
            final EquipExFlag exFlag = EquipExFlag.getByType(nFlag / 10 * 10);
            if (exFlag == null) {
                continue;
            }
            int value = getExStat(normalEquip, exFlag, nFlag % 10);
            switch (exFlag) {
                case FLAGEx_iSTR:
                    str += value;
                    break;
                case FLAGEx_iDEX:
                    dex += value;
                    break;
                case FLAGEx_iINT:
                    _int += value;
                    break;
                case FLAGEx_iLUK:
                    luk += value;
                    break;
                case FLAGEx_iSTR_DEX:
                    str += value;
                    dex += value;
                    break;
                case FLAGEx_iSTR_INT:
                    str += value;
                    _int += value;
                    break;
                case FLAGEx_iSTR_LUK:
                    str += value;
                    luk += value;
                    break;
                case FLAGEx_iDEX_INT:
                    dex += value;
                    _int += value;
                    break;
                case FLAGEx_iDEX_LUK:
                    dex += value;
                    luk += value;
                    break;
                case FLAGEx_iINT_LUK:
                    _int += value;
                    luk += value;
                    break;
                case FLAGEx_iMAXHP:
                    hp += value;
                    break;
                case FLAGEx_iMAXMP:
                    mp += value;
                    break;
                case FLAGEx_iREQLEVEL:
                    reqLevel += value;
                    break;
                case FLAGEx_iPDD:
                    pdd += value;
                    break;
                case FLAGEx_iPAD:
                    pad += value;
                    break;
                case FLAGEx_iMAD:
                    mad += value;
                    break;
                case FLAGEx_iSPEED:
                    speed += value;
                    break;
                case FLAGEx_iJUMP:
                    jump += value;
                    break;
                case FLAGEx_iBDR:
                    bossDamage += value;
                    break;
                case FLAGEx_iDAMR:
                    totalDamage += value;
                    break;
                case FLAGEx_iSTATR:
                    allStat += value;
                    break;
            }
        } while (lFlag > 0);
    }

    public static int getExStat(Equip equip, EquipExFlag stat, int grade) {
        switch (stat) {
            case FLAGEx_iSTR:
            case FLAGEx_iDEX:
            case FLAGEx_iINT:
            case FLAGEx_iLUK:
            case FLAGEx_iPDD:
                return (equip.getReqLevel() / 20 + 1) * grade;
            case FLAGEx_iSTR_DEX:
            case FLAGEx_iSTR_INT:
            case FLAGEx_iSTR_LUK:
            case FLAGEx_iDEX_INT:
            case FLAGEx_iDEX_LUK:
            case FLAGEx_iINT_LUK:
                return (equip.getReqLevel() / 40 + 1) * grade;
            case FLAGEx_iMAXHP:
            case FLAGEx_iMAXMP:
                return Math.max(3, equip.getReqLevel() / 10 * 30) * grade;
            case FLAGEx_iREQLEVEL:
                return grade * 5;
            case FLAGEx_iSPEED:
            case FLAGEx_iJUMP:
            case FLAGEx_iDAMR:
            case FLAGEx_iSTATR:
                return grade;
            case FLAGEx_iBDR:
                return grade * 2;
            case FLAGEx_iPAD:
            case FLAGEx_iMAD:
                if (ItemConstants.類型.武器(equip.getItemId())) {
                    int base = stat == EquipExFlag.FLAGEx_iPAD ? equip.getPad() : equip.getMad();
                    int level = equip.getReqLevel() / 40 + 1;
                    double rate = MapleItemInformationProvider.getInstance().isBossReward(equip.getItemId()) ? SPECIAL_RATES[grade - 1] : NORMAL_RATES[grade - 1];
                    return (int) Math.ceil(base * level * rate / 100.0);
                }
                return grade;
        }
        return 0;
    }

    public int getStr() {
        return str;
    }

    public int getDex() {
        return dex;
    }

    public int getInt() {
        return _int;
    }

    public int getLuk() {
        return luk;
    }

    public int getHp() {
        return hp;
    }

    public int getMp() {
        return mp;
    }

    public int getPad() {
        return pad;
    }

    public int getMad() {
        return mad;
    }

    public int getPdd() {
        return pdd;
    }

    public int getMdd() {
        return mdd;
    }

    public int getAcc() {
        return acc;
    }

    public int getAvoid() {
        return avoid;
    }

    public int getHands() {
        return hands;
    }

    public int getSpeed() {
        return speed;
    }

    public int getJump() {
        return jump;
    }

    public int getBossDamage() {
        return bossDamage;
    }

    public int getIgnorePDR() {
        return ignorePDR;
    }

    public int getTotalDamage() {
        return totalDamage;
    }

    public int getAllStat() {
        return allStat;
    }

    public int getReqLevel() {
        return reqLevel;
    }

    public long getFlag() {
        return flag;
    }

    public void setFlag(long flag) {
        this.flag = flag;
    }

    public int getStatTier(EquipExFlag exFlag) {
        if (exFlag == null) {
            return 0;
        }
        long lFlag = flag;
        do {
            final int nFlag = (int) (lFlag % 1000);
            lFlag /= 1000L;
            if (exFlag == EquipExFlag.getByType(nFlag / 10 * 10)) {
                return nFlag % 10;
            }
        } while (lFlag > 0);
        return 0;
    }

    public void setStatTier(EquipExFlag exFlag, int tier) {
        if (exFlag == null || tier <= 0) {
            return;
        }
        long lFlag = flag;
        flag = 0;
        do {
            int nFlag = (int) (lFlag % 1000);
            lFlag /= 1000L;
            if (exFlag == EquipExFlag.getByType(nFlag / 10 * 10)) {
                nFlag = exFlag.getType() + tier;
            }
            flag = flag * 1000L + nFlag;
        } while (lFlag > 0);
    }

    public void transmitStat1(EquipExFlag src, EquipExFlag drt) {
        if (src == drt) {
            return;
        }
        long lFlag = flag;
        flag = 0;
        do {
            int nFlag = (int) (lFlag % 1000);
            lFlag /= 1000L;
            final EquipExFlag exFlag = EquipExFlag.getByType(nFlag / 10 * 10);
            if (exFlag == null) {
                continue;
            }
            if (exFlag == src) {
                nFlag = drt.getType() + nFlag % 10;
            } else if (exFlag == drt) {
                nFlag = src.getType() + nFlag % 10;
            }
            flag = flag * 1000L + nFlag;
        } while (lFlag > 0);
    }

    public void transmitStat(EquipExFlag src, EquipExFlag drt) {
        if (src == drt) {
            return;
        }

        List<Pair<EquipExFlag, EquipExFlag>> exFlags = new LinkedList<>();
        if ((EquipExFlag.FLAGEx_iSTR == src || EquipExFlag.FLAGEx_iDEX == src) && (EquipExFlag.FLAGEx_iDEX == drt || EquipExFlag.FLAGEx_iSTR == drt)) {
            exFlags.add(new Pair<>(EquipExFlag.FLAGEx_iSTR_INT, EquipExFlag.FLAGEx_iDEX_INT));
            exFlags.add(new Pair<>(EquipExFlag.FLAGEx_iSTR_LUK, EquipExFlag.FLAGEx_iDEX_LUK));
        } else if ((EquipExFlag.FLAGEx_iSTR == src || EquipExFlag.FLAGEx_iINT == src) && (EquipExFlag.FLAGEx_iINT == drt || EquipExFlag.FLAGEx_iSTR == drt)) {
            exFlags.add(new Pair<>(EquipExFlag.FLAGEx_iSTR_DEX, EquipExFlag.FLAGEx_iDEX_INT));
            exFlags.add(new Pair<>(EquipExFlag.FLAGEx_iSTR_LUK, EquipExFlag.FLAGEx_iINT_LUK));
        } else if ((EquipExFlag.FLAGEx_iSTR == src || EquipExFlag.FLAGEx_iLUK == src) && (EquipExFlag.FLAGEx_iLUK == drt || EquipExFlag.FLAGEx_iSTR == drt)) {
            exFlags.add(new Pair<>(EquipExFlag.FLAGEx_iSTR_DEX, EquipExFlag.FLAGEx_iDEX_LUK));
            exFlags.add(new Pair<>(EquipExFlag.FLAGEx_iSTR_INT, EquipExFlag.FLAGEx_iINT_LUK));
        } else if ((EquipExFlag.FLAGEx_iDEX == src || EquipExFlag.FLAGEx_iINT == src) && (EquipExFlag.FLAGEx_iINT == drt || EquipExFlag.FLAGEx_iDEX == drt)) {
            exFlags.add(new Pair<>(EquipExFlag.FLAGEx_iSTR_DEX, EquipExFlag.FLAGEx_iSTR_INT));
            exFlags.add(new Pair<>(EquipExFlag.FLAGEx_iDEX_LUK, EquipExFlag.FLAGEx_iINT_LUK));
        } else if ((EquipExFlag.FLAGEx_iDEX == src || EquipExFlag.FLAGEx_iLUK == src) && (EquipExFlag.FLAGEx_iLUK == drt || EquipExFlag.FLAGEx_iDEX == drt)) {
            exFlags.add(new Pair<>(EquipExFlag.FLAGEx_iSTR_DEX, EquipExFlag.FLAGEx_iSTR_LUK));
            exFlags.add(new Pair<>(EquipExFlag.FLAGEx_iDEX_INT, EquipExFlag.FLAGEx_iINT_LUK));
        } else if ((EquipExFlag.FLAGEx_iINT == src || EquipExFlag.FLAGEx_iLUK == src) && (EquipExFlag.FLAGEx_iLUK == drt || EquipExFlag.FLAGEx_iINT == drt)) {
            exFlags.add(new Pair<>(EquipExFlag.FLAGEx_iSTR_INT, EquipExFlag.FLAGEx_iSTR_LUK));
            exFlags.add(new Pair<>(EquipExFlag.FLAGEx_iDEX_INT, EquipExFlag.FLAGEx_iDEX_LUK));
        }

        long srcFlag = flag;
        long drtFlag = 0;
        do {
            int nFlag = (int) (srcFlag % 1000);
            srcFlag /= 1000L;
            final EquipExFlag exFlag = EquipExFlag.getByType(nFlag / 10 * 10);
            if (exFlag == null) {
                continue;
            }
            if (exFlag == src) {
                nFlag = drt.getType() + nFlag % 10;
            } else if (exFlag == drt) {
                nFlag = src.getType() + nFlag % 10;
            } else {
                for (Pair<EquipExFlag, EquipExFlag> flagPair : exFlags) {
                    if (exFlag == flagPair.getLeft()) {
                        nFlag = flagPair.getRight().getType() + nFlag % 10;
                        break;
                    } else if (exFlag == flagPair.getRight()) {
                        nFlag = flagPair.getLeft().getType() + nFlag % 10;
                        break;
                    }
                }
            }
            drtFlag = drtFlag * 1000L + nFlag;
        } while (srcFlag > 0);
        flag = drtFlag;
    }

    //  0 力量
    //  1 敏捷
    //  2 智力
    //  3 幸運
    //  A maxHP
    //  B maxMP
    //  C 套用等級減少
    //  D 防禦率
    //  E 魔法防禦
    //  f 命中
    //  10 迴避值
    //  11 攻擊力
    //  12 魔法攻擊力
    //  13 移動速度
    //  14 跳躍力
    //  15 BOSS
    //  16 無視
    //  17 總傷害
    //  18 全屬性
    public static Map<Integer, Integer> getExStat(Equip equip, long flag) {
        Map<Integer, Integer> statProps = new HashMap<>();
        long lFlag = flag;
        do {
            int grade = (int) (lFlag%10);
//            int statFlag = lFlag>1000?(int) (lFlag%1000/10):(int) (lFlag/10);
            int statFlag = (int) (lFlag%1000/10);
            int code = 0;
            switch (EquipExFlag.getByType(statFlag*10)) {
                case FLAGEx_iSTR:
                    code = 0x0;
                    statProps.put(code, statProps.get(code) != null ? statProps.get(code) + (equip.getReqLevel() / 20 + 1) * grade : (equip.getReqLevel() / 20 + 1) * grade);
                    break;
                case FLAGEx_iDEX:
                    code =0x1;
                    statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ (equip.getReqLevel() / 20 + 1) * grade:(equip.getReqLevel() / 20 + 1) * grade);
                    break;
                case FLAGEx_iINT:
                    code =0x2;
                    statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ (equip.getReqLevel() / 20 + 1) * grade:(equip.getReqLevel() / 20 + 1) * grade);
                    break;
                case FLAGEx_iLUK:
                    code =0x3;
                    statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ (equip.getReqLevel() / 20 + 1) * grade:(equip.getReqLevel() / 20 + 1) * grade);
                    break;
                case FLAGEx_iPDD:
                    code =0xD;
                    statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ (equip.getReqLevel() / 20 + 1) * grade:(equip.getReqLevel() / 20 + 1) * grade);
                    break;
                case FLAGEx_iSTR_DEX:
                    code =0x0;
                    statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ (equip.getReqLevel() / 40 + 1) * grade:(equip.getReqLevel() / 40 + 1) * grade);
                    code =0x1;
                    statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ (equip.getReqLevel() / 40 + 1) * grade:(equip.getReqLevel() / 40 + 1) * grade);
                    break;
                case FLAGEx_iSTR_INT:
                    code =0x0;
                    statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ (equip.getReqLevel() / 40 + 1) * grade:(equip.getReqLevel() / 40 + 1) * grade);
                    code =0x2;
                    statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ (equip.getReqLevel() / 40 + 1) * grade:(equip.getReqLevel() / 40 + 1) * grade);
                    break;
                case FLAGEx_iSTR_LUK:
                    code =0x0;
                    statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ (equip.getReqLevel() / 40 + 1) * grade:(equip.getReqLevel() / 40 + 1) * grade);
                    code =0x3;
                    statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ (equip.getReqLevel() / 40 + 1) * grade:(equip.getReqLevel() / 40 + 1) * grade);
                    break;
                case FLAGEx_iDEX_INT:
                    code =0x2;
                    statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ (equip.getReqLevel() / 40 + 1) * grade:(equip.getReqLevel() / 40 + 1) * grade);
                    code =0x1;
                    statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ (equip.getReqLevel() / 40 + 1) * grade:(equip.getReqLevel() / 40 + 1) * grade);
                    break;
                case FLAGEx_iDEX_LUK:
                    code =0x3;
                    statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ (equip.getReqLevel() / 40 + 1) * grade:(equip.getReqLevel() / 40 + 1) * grade);
                    code =0x1;
                    statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ (equip.getReqLevel() / 40 + 1) * grade:(equip.getReqLevel() / 40 + 1) * grade);
                    break;
                case FLAGEx_iINT_LUK:
                    code =0x2;
                    statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ (equip.getReqLevel() / 40 + 1) * grade:(equip.getReqLevel() / 40 + 1) * grade);
                    code =0x3;
                    statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ (equip.getReqLevel() / 40 + 1) * grade:(equip.getReqLevel() / 40 + 1) * grade);
                    break;
                case FLAGEx_iMAXHP:
                    code =0xA;
                    statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ Math.max(3, equip.getReqLevel() / 10 * 30) * grade:Math.max(3, equip.getReqLevel() / 10 * 30) * grade);
                    break;
                case FLAGEx_iMAXMP:
                    code =0xB;
                    statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ Math.max(3, equip.getReqLevel() / 10 * 30) * grade:Math.max(3, equip.getReqLevel() / 10 * 30) * grade);
                    break;
                case FLAGEx_iREQLEVEL:
                    code =0xc;
                    statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ grade * 5:grade * 5);
                    break;
                case FLAGEx_iSPEED:
                    code =0x13;
                    statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ grade:grade);
                    break;
                case FLAGEx_iJUMP:
                    code =0x14;
                    statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ grade:grade);
                    break;
                case FLAGEx_iDAMR:
                    code =0x17;
                    statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ grade:grade);
                    break;
                case FLAGEx_iSTATR:
                    code =0x18;
                    statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ grade:grade);
                    break;
                case FLAGEx_iBDR:
                    code =0x15;
                    statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ grade*2:grade*2);
                    break;
                case FLAGEx_iPAD:
                    code =0x11;
                    if (ItemConstants.類型.武器(equip.getItemId())) {
                        int base = EquipExFlag.getByType(statFlag*10) == EquipExFlag.FLAGEx_iPAD ? equip.getPad() : equip.getMad();
                        int level = equip.getReqLevel() / 40 + 1;
                        double rate = MapleItemInformationProvider.getInstance().isBossReward(equip.getItemId()) ? SPECIAL_RATES[grade - 1] : NORMAL_RATES[grade - 1];
                        statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ (int) Math.ceil(base * level * rate / 100.0):(int) Math.ceil(base * level * rate / 100.0));
                        break;
                    }
                    statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ grade:grade);
                    break;
                case FLAGEx_iMAD:
                    code =0x12;
                    if (ItemConstants.類型.武器(equip.getItemId())) {
                        int base = EquipExFlag.getByType(statFlag*10) == EquipExFlag.FLAGEx_iPAD ? equip.getPad() : equip.getMad();
                        int level = equip.getReqLevel() / 40 + 1;
                        double rate = MapleItemInformationProvider.getInstance().isBossReward(equip.getItemId()) ? SPECIAL_RATES[grade - 1] : NORMAL_RATES[grade - 1];
                        statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ (int) Math.ceil(base * level * rate / 100.0):(int) Math.ceil(base * level * rate / 100.0));
                        break;
                    }
                    statProps.put(code, statProps.get(code)!=null? statProps.get(code)+ grade:grade);
                    break;
            }

            lFlag /= 1000L;
        }while (lFlag > 0);

        return statProps;
    }
}


