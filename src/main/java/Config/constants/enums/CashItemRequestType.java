package Config.constants.enums;

public enum CashItemRequestType {
    CashItemReq_LoadLocker(0),
    CashItemReq_LoadWish(1),
    CashItemReq_Buy(3),
    CashItemReq_Gift(4),
    CashItemReq_SetWish(5),
    CashItemReq_IncSlotCount(6),
    CashItemReq_IncTrunkCount(7),
    CashItemReq_IncCharSlotCount(8),
    CashItemReq_IncBuyCharCount(-2),
    CashItemReq_EnableEquipSlotExt(9),
    CashItemReq_CancelPurchase(10),
    CashItemReq_ConfirmPurchase(11),
    CashItemReq_MoveLtoS(12),
    CashItemReq_MoveStoL(13),
    CashItemReq_Destroy(14),
    CashItemReq_Expire(16),
    CashItemReq_Use(17),
    CashItemReq_StatChange(18),
    CashItemReq_SkillChange(19),
    CashItemReq_SkillReset(20),
    CashItemReq_DestroyPetItem(21),
    CashItemReq_SetPetName(22),
    CashItemReq_SetPetLife(23),
    CashItemReq_SetPetSkill(24),
    CashItemReq_SetItemName(25),
    CashItemReq_SetAndroidName(26),
    CashItemReq_SendMemo(27),
    CashItemReq_GetAdditionalCashShopInfo(28),
    CashItemReq_GetMaplePoint(29),
    CashItemReq_UseMaplePointFromGameSvr(30),
    CashItemReq_Rebate(31), // 換購
    UNKNOW_32(-2),
    CashItemReq_UseCoupon(32), //
    CashItemReq_GiftCoupon(33),
    CashItemReq_Couple(33), // 情侶戒指 TMS223
    CashItemReq_BuyPackage(34),
    CashItemReq_GiftPackage(35),
    CashItemReq_BuyNormal(36),
    CashItemReq_ApplyWishListEvent(37),
    CashItemReq_MovePetStat(38),
    CashItemReq_FriendShip(39),
    CashItemReq_ShopScan(40),
    CashItemReq_ShopOptionScan(41),
    CashItemReq_ShopScanSell(42),
    CashItemReq_LoadPetExceptionList(43),
    CashItemReq_UpdatePetExceptionList(44),
    CashItemReq_DestroyScript(45),
    CashItemReq_CashItemCreate(46),
    CashItemReq_PurchaseRecord(47),
    CashItemReq_DeletePurchaseRecord(48),
    CashItemReq_TradeDone(49),
    CashItemReq_BuyDone(50),
    CashItemReq_TradeSave(51),
    CashItemReq_TradeLog(52),
    CashItemReq_CharacterSale(53), // 刪除道具
    CashItemReq_SellCashItemBundleToShop(54),
    CashItemReq_Destroy2(55),
    CashItemReq_Refund(57),
    UNKNOW_63(58),
    CashItemReq_UseCashRandomItem(59);

    private int code = -2;

    CashItemRequestType(int code) {
        this.code = code;
    }

    public static CashItemRequestType getByAction(int packetId) {
        CashItemRequestType[] values = CashItemRequestType.values();
        for (int i = 0, valuesLength = values.length; i < valuesLength; i++) {
            CashItemRequestType interaction = values[i];
            if (interaction.getValue() == packetId) {
                return interaction;
            }
        }
        return null;
    }

    public short getValue() {
        return (short) code;
    }

}
