// [消亡旅途]安息洞穴 - 洞穴尽头
portal.playPortalSE();
if (player.isQuestCompleted(34119) && !player.isQuestCompleted(34120)) {
        player.changeMap(450001360, 0);
} else {
        player.changeMap(450001250, 1);
}
