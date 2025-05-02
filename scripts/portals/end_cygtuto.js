
if (player.isQuestStarted(20839) || player.isQuestCompleted(20839)) {
        portal.playPortalSE();
        for (; player.getLevel() < 10; ) {
                player.gainExp(1000);
        }
        portal.playPortalSE();
        player.changeMap(130030005, 0);
} else {
        player.showSystemMessage("請先完成訓練!");
        portal.abortWarp();
}