portal.playPortalSE();
if (player.isQuestStarted(25564) || player.isQuestCompleted(25564) && !player.isQuestCompleted(25569)) {
        player.changeMap(101000011, 8);
} else {
        player.changeMap(101000003, 8);
}