
if (player.isQuestStarted(2605) || player.isQuestCompleted(2605)) {
    portal.playPortalSE();
    player.changeMap(103050500, 0);
} else {
    player.scriptProgressMessage("請先執行任務後才可以出去！");
    portal.abortWarp();
}
