/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

/**
 * @author ODINMR
 */
public class MapleEnumClass {

    /**
     * 專業技術消息
     */
    public enum HarvestMsg {

        HARVEST_NO_TOOLS(1), //沒有可以用於採集的工具
        HARVEST_LEVEL_LOW(2), //採集等級太低，無法採集
        HARVEST_NOT_LEARNING_HERBS(3), //尚未學習採藥
        HARVEST_NOT_LEARNING_MINING(4), //尚未學習採礦
        HARVEST_FATIGUE_FULL(5), //疲勞度已滿，無法採集
        HARVEST_DISTANCE_FAR(6), //採集物距離太遠，取消採集
        HARVEST_CANCELLED(7), //已取消採集
        HARVEST_PLAYER_PROCESSING(8), //已經有人正在採集
        HARVEST_UNABLE_COLLECT(9), //還無法採集
        HARVEST_UNKNOWN_ERROR(10), //由於未知錯誤，採集無法進行
        HARVEST_DONT_DOWN(11), //坐在椅子上無法採集
        // 12 等級太低，無法在現在的地圖中採集
        HARVEST_ACTION_START(13); //開始採集

        private final int code;

        HarvestMsg(int code) {
            this.code = code;
        }

        public int getCode() {
            return this.code;
        }

        public boolean is(HarvestMsg other) {
            return other != null && this.code == other.code;
        }
    }

    /**
     * 賬號驗證結果
     */
    public enum AuthReply {

        GAME_LOGIN_SUCCESSFUL(0),
        GAME_ACCOUNT_BANNED(2),
        GAME_ACCOUNT_DELETE(3),
        GAME_PASSWORD_ERROR(4),
        GAME_ACCOUNT_NOT_LANDED(5),
        GAME_SYSTEM_ERROR(6),
        GAME_CONNECTING_ACCOUNT(7),
        GAME_CONNECTION_BUSY(10),
        GAME_CONNECTION_LOCKING(13),
        GAME_DEFINITION_INFO(16),
        GAME_PROTOCOL_INFO(22);

        private final int code;

        AuthReply(int code) {
            this.code = code;
        }

        public int getCode() {
            return this.code;
        }

        public boolean is(AuthReply other) {
            return other != null && this.code == other.code;
        }
    }

}
