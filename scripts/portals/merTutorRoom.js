if (player.isQuestCompleted(24004) && player.isQuestStarted(24005)) {
        portal.playPortalSE();
        player.changeMap(910150004, 0);
} else {
        portal.abortWarp();
        player.showSystemMessage("現在還不能休息！");
}
