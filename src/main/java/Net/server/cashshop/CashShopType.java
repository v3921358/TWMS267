package Net.server.cashshop;

public enum CashShopType {

    主頁(new String[0]),
    活動(new String[]{"套組商品特賣", "每日特賣", "里程商店", "魔法馬車"}),
    轉蛋(new String[0]),
    強化(new String[]{"方塊", "強化道具"}),
    遊戲(new String[]{"便利功能", "其他消耗1", "其他消耗2", "通訊物品", "加持特效", "增加欄位", "加倍卡", "練功輔助"}),
    美容(new String[]{"美髮", "整容", "其他整型", "表情", "特效", "傷害字型"}),
    時裝(new String[]{"武器", "帽子", "眼飾", "臉飾", "長袍", "上衣", "褲裙", "披風", "手套", "鞋子", "飾品", "透明區"}),
    寵物(new String[]{"寵物", "寵物裝備", "寵物功能", "寵物飼料"}),
    套組(new String[]{"強化區", "遊戲功能", "時裝區", "整容區", "寵物區"}),
    搜尋結果(new String[0]);

    private final String[] subtype;

    CashShopType(String[] subtype) {
        this.subtype = subtype;
    }

    public String[] getSubtype() {
        return subtype;
    }
}