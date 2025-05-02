package SwordieX.enums;

import SwordieX.util.Util;

public enum WorldId {
    // UI.wz/Login.img/WorldSelect/BtWorld/release
    艾麗亞(0),
    普力特(1),
    琉德(2),
    優依娜(3),
    愛麗西亞(4),
    殺人鯨(6),
    LAB(30),
    Reboot(45),
    Reboot2(46),
    雪吉拉x皮卡啾(47),
    燃燒2(48),
    燃燒(49),
    燃燒3(52),
    雪吉拉x皮卡啾2(53),
    버닝4(54), // BURNING 4
    ;

    private final int val;

    WorldId(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public static WorldId getByVal(int val) {
        return Util.findWithPred(values(), v -> v.getVal() == val);
    }
}
