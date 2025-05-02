package Config.constants.enums;

public enum ActionBarResultType {

    Create_Request(0),
    Remove_Request(1),
    Verify_Request(2),
    VerifyAck_Result(3),
    ResetCool_Request(4),
    Create_Result(5),
    Remove_Result(6),
    Create_Selected(7),
    VerifyAck(8),
    VerifyState(9),
    SetCoolTime(10),
    UsableCnt_Modify(11),
    UsableCnt_Reset(12),
    UsableCnt_ResetAll(13),
    UsableCnt_PickUpFail(14);

    private final int value;

    ActionBarResultType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
