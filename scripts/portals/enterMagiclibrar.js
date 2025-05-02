if (player.isQuestStarted(25564) || player.isQuestStarted(25564) && player.isQuestStarted(25569)) {
    portal.rememberMap("RETURN_MAP");
    portal.playPortalSE();
    player.changeMap(101000011, 8);
} else {
    portal.playPortalSE();
    player.changeMap(101000003, 8);
}
