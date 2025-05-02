package Client;

import Server.Buffstat;

import java.io.Serializable;

public enum MapleDisease implements Serializable, Buffstat {

    封印(SecondaryStat.Seal, 120),
    黑暗(SecondaryStat.Darkness, 121),
    虛弱(SecondaryStat.Weakness, 122),
    昏迷(SecondaryStat.Stun, 123),
    詛咒(SecondaryStat.Curse, 124),
    中毒(SecondaryStat.Poison, 125),
    緩慢(SecondaryStat.Slow, 126),
    誘惑(SecondaryStat.Attract, 128),
    混亂(SecondaryStat.ReverseInput, 132),
    痛苦(SecondaryStat.PainMark, -1),
    不死化(SecondaryStat.BanMap, 133),
    無法使用藥水(SecondaryStat.StopPortion, 134),
    StopMotion(SecondaryStat.StopMotion, 135),
    致盲(SecondaryStat.Fear, 136),
    冰凍(SecondaryStat.Frozen, 137),
    裝備潛能無效化(SecondaryStat.DispelItemOption, 138),
    變身(SecondaryStat.Morph, 172),
    龍捲風(SecondaryStat.DarkTornado, 173),
    死亡束縛(SecondaryStat.Lapidification, 174),
    返回原位置(SecondaryStat.ReturnTeleport, 184),
    誘惑之境(SecondaryStat.Attract, 188),
    精靈帽子(SecondaryStat.CapDebuff, 189),
    精靈帽子2(SecondaryStat.CapDebuff, 190),
    禁止跳躍(SecondaryStat.IndieJump, 229);

    private final SecondaryStat buffStat;
    private final int disease;

    MapleDisease(SecondaryStat buffStat, int disease) {
        this.buffStat = buffStat;
        this.disease = disease;
    }

    public static MapleDisease getBySkill(int skill) {
        for (MapleDisease d : MapleDisease.values()) {
            if (d.getDisease() == skill) {
                return d;
            }
        }
        return null;
    }

    public static boolean containsStat(final SecondaryStat stat) {
        MapleDisease[] values;
        for (int length = (values = values()).length, i = 0; i < length; ++i) {
            if (values[i].buffStat == stat) {
                return true;
            }
        }
        return false;
    }

    public SecondaryStat getBuffStat() {
        return buffStat;
    }

    @Override
    public int getPosition() {
        return buffStat.getPosition();
    }

    @Override
    public int getValue() {
        return buffStat.getValue();
    }

    public int getDisease() {
        return disease;
    }
}
