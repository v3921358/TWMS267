if (player.isQuestStarted(2600)) {
    portal.playPortalSE();
    player.changeMap(103050910, 0);
} else {
    player.scriptProgressMessage("請先與紅雅進行交談！");
    portal.abortWarp();
}
