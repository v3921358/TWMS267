// makeEvent
if (player.getPQLog("aquaris_tower") >= 100) {
        portal.abortWarp();
        player.showSystemMessage("今天已經突破了100層，無法再次進入了！");
} else if (!player.hasItem(2432461, 1)) {
        portal.abortWarp();
        player.showSystemMessage("你沒有靈魂連結器，請通過阿麗莎的思念體獲取。");
} else {
        if (portal.makeEvent("aquaris_tower", player) == null) {
                portal.abortWarp();
                player.dropAlertNotice("发生未知错误，请联系管理员。");
        }
}

function getQuestPoint(questId) {
        return player.getIntQuestRecordEx(questId, "point");
}

function gainQuestPoint(QuestId, Number) {
        player.updateQuestRecordEx(QuestId, "point", String(getQuestPoint(QuestId) + Number));
        if (Number < 0) {
                player.showSystemMessage("消耗了 " + Math.abs(Number) + " 起源點數，剩餘起源點數 " + getQuestPoint(QuestId));
        } else {
                player.showSystemMessage("增加了 " + Math.abs(Number) + " 起源點數，剩餘起源點數 " + getQuestPoint(QuestId));
        }
}