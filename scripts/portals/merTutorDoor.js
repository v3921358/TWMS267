if (!player.isQuestCompleted(24004) && (player.isQuestActive(24001) || player.isQuestCompleted(24001))) {
        portal.playPortalSE();
        player.changeMap(910150002, 2);
} else {
        portal.abortWarp();
        player.showSystemMessage("現在還不能離開村子！");
}
