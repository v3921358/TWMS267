package Config.constants.enums;

public enum UserChatMessageType {

    // 白
    普通(0),
    // 綠
    密語(1),
    // 粉
    隊伍群組(2),
    // 橙
    好友群組(3),
    // 淺紫
    公會群組(4),
    // 淺綠
    聯盟群組(5),
    // 灰
    遊戲描述(6),
    // 黃
    提示(7),
    // 淺黃
    通知(8),
    // 藍
    公告(9),
    // 黑_白
    管理員對話(10),
    // 紅
    系統(11),
    // 藍_藍
    頻道喇叭(12),
    // 紅_粉
    世界喇叭(13, true),
    // 紅_粉(第一個冒號前字符不顯示)
    世界喇叭_公會技能(14, true),
    // 黑_黃
    道具喇叭(15, true),
    // 黃綠
    超性能擴音器(16, true),
    紫(17),
    淺黃(18),
    // 褐_綠
    抽獎喇叭_世界(19, true),
    灰喇叭(20, true),
    黃(21),
    青(22),
    黑_黃(23),
    // 藍[黃](忽略"[]")
    道具訊息(24),
    粉(25),
    // 藍[粉](不忽略"[]")
    藍加粉(5),
    粉喇叭(27, true),
    // 藍[黃](不忽略"[]")
    方塊洗洗樂(28),
    暗黃(29),
    淺紫(30),
    // 31 - 不顯示
    白(32),
    // 33 - 不顯示
    黑_暗黃(34),
    紅(35),
    // 36 - 不顯示
    // 粉_白
    骷髏喇叭(37, true),
    黑_紅喇叭(38, true),
    黃_綠喇叭(39, true),
    黃_紅喇叭(40, true),
    黑_粉喇叭(41, true),
    黑_黃喇叭(42, true),
    黃_紅喇叭2(43, true),
    白_紅喇叭(44, true),
    世界廣播(44),
    白2(45),
    白3(46),
    // 覆蓋CH圖標
    黑_綠喇叭(47, true),
    // 覆蓋CH圖標
    黑_紅喇叭2(48, true),
    // 覆蓋CH圖標
    黑_黃喇叭2(49, true),
    白4(50),
    ;

    private final int type;
    private final boolean isSpeaker;

    UserChatMessageType(int type) {
        this(type, false);
    }

    UserChatMessageType(int type, boolean isSpeaker) {
        this.type = type;
        this.isSpeaker = isSpeaker;
    }

    public static UserChatMessageType getByType(int type) {
        for (UserChatMessageType cType : values()) {
            if (cType.getType() == type) {
                return cType;
            }
        }
        return null;
    }

    public short getType() {
        return (short) type;
    }

    public boolean isSpeaker() {
        return isSpeaker;
    }

    public String getMsg(String msg) {
        if (!isSpeaker() || !msg.contains(":")) {
            return msg;
        }
        return msg.substring(0, msg.indexOf(":")) + " " + msg.substring(msg.indexOf(":"));
    }
}
