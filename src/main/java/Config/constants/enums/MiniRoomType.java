package Config.constants.enums;

public enum MiniRoomType {
    MR_NOT_DEFINED(0),
    MR_OmokRoom(1),
    MR_MemoryGameRoom(2),
    MR_RpsGameRoom(3),
    MR_TradingRoom(4),
    MR_PersonalShop(5),
    MR_EntrustedShop(6),
    MR_CashTradingRoom(7),
    MR_WeddingExRoom(8),
    MR_CandyTradingRoom(9),
    MR_MultiYutRoom(10),
    MR_SignRoom(11),
    MR_TenthAnniversaryBoardGameRoom(12),
    MR_BingoGameRoom(13),
    MR_OmokRenewalRoom(14),
    MR_MemoryGameRoom_2013(15),
    MR_OneCardGameRoom(16),
    MR_SuperMultiYutRoom(17),
    MR_RunnerGameRoom(18),
    MR_TypeNo(19),
    MR_Fisher(75);

    private final int type;

    MiniRoomType(final int type) {
        this.type = type;
    }

    public final int getValue() {
        return this.type;
    }

    public static MiniRoomType getByType(final int type) {
        MiniRoomType[] values = MiniRoomType.values();
        for (int i = 0, valuesLength = values.length; i < valuesLength; i++) {
            MiniRoomType interaction = values[i];
            if (interaction.getValue() == type) {
                return interaction;
            }
        }
        return null;
    }
}
