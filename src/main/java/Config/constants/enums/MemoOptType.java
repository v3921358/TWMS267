package Config.constants.enums;

public enum MemoOptType {

    MemoReq_Send(0),
    MemoReq_Send_Account(1),
    MemoReq_Delete(2),
    MemoReq_Load(3),
    MemoRes_Load(7), // 4
    MemoRes_Send_Succeed(8), // 5
    MemoRes_Send_Warning(9), // 6
    MemoRes_Send_ConfirmOnline(10),
    MemoRes_Delete_Succeed(11),
    MemoNotify_Receive(16); // 9

    public static final byte Memo_Warning_Msg_Not_Name = 1;
    public static final byte Memo_Warning_Msg_Full = 2;
    public static final byte Memo_Warning_Msg_Time_Limit = 3;
    public static final byte Memo_Warning_Msg_Send_Limit = 4;
    public static final byte Memo_Warning_Msg_NotSelf = 5;
    public final int value;

    MemoOptType(final int value) {
        this.value = value;
    }

    public final int getValue() {
        return this.value;
    }

    public static MemoOptType getByType(final int type) {
        MemoOptType[] values = MemoOptType.values();
        for (int i = 0, valuesLength = values.length; i < valuesLength; i++) {
            MemoOptType interaction = values[i];
            if (interaction.getValue() == type) {
                return interaction;
            }
        }
        return null;
    }

    public enum MemoWarningMessage {

    }
}
