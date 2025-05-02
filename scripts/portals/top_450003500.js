// [拉克兰]噩梦时间塔

if (player.isQuestCompleted(34327)) {
        portal.playPortalSE();
        player.changeMap(450003510, 1);
} else {
        portal.abortWarp();
        player.runScript("QuestLKLCLOSE");
}
