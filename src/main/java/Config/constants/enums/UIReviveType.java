package Config.constants.enums;

public enum UIReviveType {
    UIReviveType_None(-1),
    /*
     * 點擊確認時將會復活。
     * 復活時將移到位於附近安全的村莊。
     * 確認
     */
    UIReviveType_Normal(0),
    /*
     * 組隊點數  10 使用時
     * 可在目前的地圖中復活。
     * (持有點數:       )
     * 確認 取消
     */
    UIReviveType_UsingPartyPoint(1),
    // 跟 UIReviveType_Normal 一樣多個 取消
    UIReviveType_SoulDungeon(2),
    /*
     * 點擊確認時將會復活。
     * 點擊確認後將在目前的地圖復活。
     * 確認
     */
    UIReviveType_MagnusNormalHard(3),
    /*
     * 請稍等。戰鬥結束時將自動復活。
     * 點擊確認按鈕時，將結束戰鬥並回到附近村莊。
     * 確認
     */
    UIReviveType_OnUIDeathCountInfo(4),
    // 靈魂之石(已砍, 變成 UIReviveType_SoulDungeon 提示)
    UIReviveType_SoulStone(5),
    /*
     * 可用原地復活的力量在目前的地圖中復活。
     * 要在目前的地圖中復活嗎？
     * 確認 取消
     */
    UIReviveType_UpgradeTombItem(6),
    /*
     * 在高級網咖連線時可於目前地圖復活。
     * 要在目前的地圖中復活嗎？
     * 確認 取消
     */
    UIReviveType_PremiumUser(7),
    // 伊露納的幸運機會(已砍, 變成 UIReviveType_SoulDungeon 提示)
    UIReviveType_UpgradeTombItemOfCashBuffEvent(8),
    // 楓點原地復活(已砍, 變成 UIReviveType_SoulDungeon 提示)
    UIReviveType_UpgradeTombItemMaplePoint(9),
    // 跟 UIReviveType_MagnusNormalHard 一樣
    // 10
    /*
     * 可透過戰鬥機器人的力量
     * 於目前所在地圖復活。
     * 要於當前地圖復活嗎？
     * 確認 取消
     */
    UIReviveType_CombatAndroid(11),
    // 跟 UIReviveType_PremiumUser 一樣
    UIReviveType_PremiumUser2(12),
    /*
     * 可用復仇女神的紡車力量
     * 於當前的地圖復活。
     * 要在目前的地圖中復活嗎？
     * 確認 取消
     */
    UIReviveType_Nemesis2(13),
    // 女神的紡車復活
    UIReviveType_Nemesis(14);

    private final int type;

    UIReviveType(int type) {
        this.type = type;
    }

    public final int getType() {
        return this.type;
    }
}
