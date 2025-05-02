// [拉克兰]噩梦时间塔

if (player.isQuestCompleted(34329)) {
        portal.playPortalSE();
        player.changeMap(450003530, 2);
} else {
        portal.abortWarp();
        player.runScript("QuestLKLCLOSE");
}
