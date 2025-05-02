if (player.isQuestStarted(38004)) {
        player.startQuest(38996, 0);
        player.setCustomData(38004, "clear");
        player.completeQuest(38004, 0);
}
if (player.isQuestStarted(38011)) {
        player.startQuest(38012, 0);
        player.setCustomData(38011, "clear");
        player.completeQuest(38011, 0);
}
if (player.isQuestStarted(38020)) {
        player.setCustomData(38020, "clear");
        player.completeQuest(38021, 0);
        player.completeQuest(38022, 0);
}
portal.playPortalSE();
player.changeMap(410000002, 0);
