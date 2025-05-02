// [消亡旅途]安息洞穴 - 三岔路1
if (player.isQuestCompleted(34116)) {
        portal.playPortalSE();
        player.changeMap(450001215, 1);
} else {
        portal.abortWarp();
}
