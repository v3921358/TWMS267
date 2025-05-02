
if (player.isQuestStarted(20827) || player.isQuestCompleted(20827)) {
        portal.playPortalSE();
        player.changeMap(130030102, 0);
} else {
        player.showSystemMessage("請先完成訓練!");
        portal.abortWarp();
}