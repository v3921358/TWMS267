package Config.constants.enums;

public enum SummonAttackType {
    ASSIST_NONE(0),
    ASSIST_ATTACK(1),
    ASSIST_HEAL(2),
    ASSIST_ATTACK_EX(3),
    ASSIST_ATTACK_EX2(4),
    ASSIST_UNKNOWN_5(5),
    ASSIST_SUMMON(6),
    ASSIST_ATTACK_MANUAL(7),
    ASSIST_ATTACK_COUNTER(8),
    ASSIST_CREATE_AREA(9),
    ASSIST_ATTACK_BODYGUARD(10),
    ASSIST_ATTACK_JAGUAR(11),
    ASSIST_UNKNOWN_12(12),
    ASSIST_ATTACK_REPET(13),
    ASSIST_UNKNOWN_14(14),
    ASSIST_UNKNOWN_15(15),
    ASSIST_UNKNOWN_16(16),
    ASSIST_UNKNOWN_17(17),
    ASSIST_UNKNOWN_18(18),
    ASSIST_UNKNOWN_20(20);

    public final byte value;

    SummonAttackType(final int value) {
        this.value = (byte) value;
    }
}
