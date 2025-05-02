package Client.inventory;

import Config.constants.ItemConstants;
import Net.server.MapleItemInformationProvider;
import Server.channel.handler.EnchantHandler;

import java.util.Map;

public class StarForce {
    private int str;
    private int dex;
    private int _int;
    private int luk;
    private int hp;
    private int mp;
    private int pad;
    private int mad;
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

    private byte level;

    public StarForce() {
        reset();
    }

    public StarForce(StarForce s) {
        str = s.str;
        dex = s.dex;
        _int = s._int;
        luk = s.luk;
        hp = s.hp;
        mp = s.mp;
        pad = s.pad;
        mad = s.mad;
        pdd = s.pdd;
        mdd = s.mdd;
        acc = s.acc;
        avoid = s.avoid;
        hands = s.hands;
        speed = s.speed;
        jump = s.jump;
        bossDamage = s.bossDamage;
        ignorePDR = s.ignorePDR;
        totalDamage = s.totalDamage;
        allStat = s.allStat;
        level = s.level;
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
        level = 0;
    }

    public void resetEquipStats(final Equip equip) {
        byte nLevel = level;
        reset();
        for (int i = 0; i < nLevel; i++) {
            Map<EnchantScrollFlag, Integer> enchantMap = EnchantHandler.getEnchantScrollList(equip);
            level++;
            if (enchantMap.containsKey(EnchantScrollFlag.力量)) {
                str += enchantMap.get(EnchantScrollFlag.力量);
            }
            if (enchantMap.containsKey(EnchantScrollFlag.敏捷)) {
                dex += enchantMap.get(EnchantScrollFlag.敏捷);
            }
            if (enchantMap.containsKey(EnchantScrollFlag.智力)) {
                _int += enchantMap.get(EnchantScrollFlag.智力);
            }
            if (enchantMap.containsKey(EnchantScrollFlag.幸運)) {
                luk += enchantMap.get(EnchantScrollFlag.幸運);
            }
            if (enchantMap.containsKey(EnchantScrollFlag.Hp)) {
                hp += enchantMap.get(EnchantScrollFlag.Hp);
            }
            if (enchantMap.containsKey(EnchantScrollFlag.Mp)) {
                mp += enchantMap.get(EnchantScrollFlag.Mp);
            }
            if (enchantMap.containsKey(EnchantScrollFlag.物攻)) {
                pad += enchantMap.get(EnchantScrollFlag.物攻);
            }
            if (enchantMap.containsKey(EnchantScrollFlag.魔攻)) {
                mad += enchantMap.get(EnchantScrollFlag.魔攻);
            }
            if (enchantMap.containsKey(EnchantScrollFlag.物防)) {
                pdd += enchantMap.get(EnchantScrollFlag.物防);
            }
            if (enchantMap.containsKey(EnchantScrollFlag.魔防)) {
                mdd += enchantMap.get(EnchantScrollFlag.魔防);
            }
            if (enchantMap.containsKey(EnchantScrollFlag.命中)) {
                acc += enchantMap.get(EnchantScrollFlag.命中);
            }
            if (enchantMap.containsKey(EnchantScrollFlag.迴避)) {
                avoid += enchantMap.get(EnchantScrollFlag.迴避);
            }
            if (enchantMap.containsKey(EnchantScrollFlag.手技)) {
                hands += enchantMap.get(EnchantScrollFlag.手技);
            }
            if (enchantMap.containsKey(EnchantScrollFlag.速度)) {
                speed += enchantMap.get(EnchantScrollFlag.速度);
            }
            if (enchantMap.containsKey(EnchantScrollFlag.跳躍)) {
                jump += enchantMap.get(EnchantScrollFlag.跳躍);
            }
            // MVP裝備額外加成
            if (equip.isMvpEquip()) {
                if (MapleItemInformationProvider.getInstance().isCash(equip.getItemId()) || ItemConstants.類型.圖騰(equip.getItemId())) {
                    switch (level) {
                        case 6:
                            pad += 5;
                            mad += 5;
                            hp += 500;
                            mp += 500;
                            break;
                        case 8:
                            pad += 10;
                            mad += 10;
                            hp += 500;
                            mp += 500;
                            break;
                        case 10:
                            pad += 10;
                            mad += 10;
                            hp += 1000;
                            mp += 1000;
                            break;
                        case 15:
                            pad += 10;
                            mad += 10;
                            hp += 1500;
                            mp += 1500;
                            break;
                        case 20:
                            pad += 10;
                            mad += 10;
                            hp += 2500;
                            mp += 2500;
                            break;
                        case 25:
                            pad += 10;
                            mad += 10;
                            hp += 4000;
                            mp += 4000;
                            break;
                    }
                } else {
                    switch (level) {
                        case 6:
                            ignorePDR += 2;
                            totalDamage += 2;
                            allStat += 2;
                            break;
                        case 8:
                            ignorePDR += 2;
                            totalDamage += 2;
                            allStat += 2;
                            break;
                        case 10:
                            ignorePDR += 3;
                            totalDamage += 3;
                            allStat += 3;
                            break;
                        case 15:
                            ignorePDR += 3;
                            totalDamage += 3;
                            allStat += 3;
                            break;
                        case 20:
                            ignorePDR += 5;
                            totalDamage += 10;
                            allStat += 5;
                            break;
                        case 25:
                            ignorePDR += 10;
                            totalDamage += 10;
                            allStat += 5;
                            break;
                    }
                }
            }
        }
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

    public byte getLevel() {
        return level;
    }

    public void setLevel(byte level) {
        this.level = level;
    }
}
