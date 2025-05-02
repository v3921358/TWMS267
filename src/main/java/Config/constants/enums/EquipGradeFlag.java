package Config.constants.enums;

public enum EquipGradeFlag {
    Grade_Rest((byte) (-1)),
    Grade_C((byte) 0),
    Grade_B((byte) 1),
    Grade_A((byte) 2),
    Grade_S((byte) 3),
    Grade_SS((byte) 4),
    Grade_Appraisal((byte) 16),
    Grade_Bonus_Unattuned((byte) 32);

    private final byte value;

    EquipGradeFlag(byte value) {
        this.value = value;
    }

    public final byte getValue() {
        return value;
    }

    public final boolean check(int n) {
        return (n & value) == value;
    }

    public static boolean hasPotential(int n) {
        return Grade_B.check(n) || Grade_A.check(n) || Grade_S.check(n) || Grade_SS.check(n);
    }
}
