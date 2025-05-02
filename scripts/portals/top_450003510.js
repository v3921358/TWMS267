// [拉克兰]噩梦时间塔

if (player.isQuestCompleted(34328)) {
        portal.playPortalSE();
        player.changeMap(450003520, 1);
} else {
        portal.abortWarp();
        player.runScript("QuestLKLCLOSE");
}
