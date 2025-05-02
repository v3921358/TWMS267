// [消亡旅途]安息洞穴 - 洞穴尽头
if (player.isQuestCompleted(34118) && player.isQuestStarted(34119) || !player.isQuestCompleted(34119)) {
        portal.playPortalSE();
        player.changeMap(450001380, 1);
} else {
        portal.playPortalSE();
        player.changeMap(450001219, 1);
}
