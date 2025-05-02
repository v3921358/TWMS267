
if (player.isQuestCompleted(35903)) {
        portal.playPortalSE();
        player.changeMap(100051010, 0);
} else {
        portal.abortWarp();
        player.showSystemMessage("請先檢查檢查一下狀態，然後再離開吧！");
}
