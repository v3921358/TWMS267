// [消亡旅途]安息洞穴 - 三岔路2
if (player.isQuestCompleted(34117)) {
        portal.playPortalSE();
        player.changeMap(450001218, 1);
} else {
        portal.abortWarp();
}
