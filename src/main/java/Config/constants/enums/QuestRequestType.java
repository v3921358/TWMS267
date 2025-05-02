package Config.constants.enums;

import java.util.Arrays;

public enum QuestRequestType {
    QuestReq_LostItem,
    QuestReq_AcceptQuest,
    QuestReq_CompleteQuest,
    QuestReq_ResignQuest,
    QuestReq_OpeningScript,
    QuestReq_CompleteScript,
    QuestReq_LaterStep,

    QuestReq_UNK, // TODO: 位置待確定
    QuestRes_Start_QuestTimer,
    QuestRes_End_QuestTimer,
    QuestRes_Start_TimeKeepQuestTimer,
    QuestRes_End_TimeKeepQuestTimer,

    QuestRes_Act_Success,
    QuestRes_Act_Failed_Unknown,
    QuestRes_Act_Failed_Inventory,
    QuestRes_Act_Failed_Meso,
    QuestRes_Act_Failed_OverflowMeso,
    QuestRes_Act_Failed_Pet,
    QuestRes_Act_Failed_Equipped,
    QuestRes_Act_Failed_OnlyItem,
    QuestRes_Act_Failed_TimeOver,
    QuestRes_Act_Failed_State,
    QuestRes_Act_Failed_Quest,
    QuestRes_Act_Failed_Block,
    QuestRes_Act_Failed_Universe,
    QuestRes_Act_Reset_QuestTimer,
    QuestRes_UNK2,
    MakingRes_Success_SoSo,
    MakingRes_Success_Good,
    MakingRes_Success_Cool,
    MakingRes_Fail_Unknown,
    MakingRes_Fail_Prob,
    MakingRes_Fail_NoDecomposer,
    MakingRes_Fail_MesoOverflow,
    MakingRes_Fail_TooHighFee,
    MakingRes_Fail_NotEnoughMeso,
    ;

    private byte val;

    QuestRequestType() {
        this.val = (byte) this.ordinal();
    }

    QuestRequestType(int val) {
        this.val = (byte) val;
    }

    public byte getVal() {
        return val;
    }

    public static QuestRequestType getQTFromByte(byte type) {
        return Arrays.stream(values()).filter(qt -> qt.getVal() == type).findAny().orElse(null);
    }
}
