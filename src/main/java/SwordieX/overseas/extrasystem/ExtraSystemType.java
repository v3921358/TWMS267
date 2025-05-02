package SwordieX.overseas.extrasystem;

import lombok.Getter;

@Getter
public enum ExtraSystemType {

    ExtraSystem1(1),
    ExtraTimerSystem(2),
    ExtraSystem3(3),
    ExtraSystem4(4),
    ExtraSystem12(15),
    mukhyunSystemLock(35),
    mukhyunPower(36),
    mukhyunPowerNext(36),
    enableActions(38),
    mukhyunSkill_175111004(39),
    ;
    private final int val;

    ExtraSystemType(int val) {
        this.val = val;
    }
}
