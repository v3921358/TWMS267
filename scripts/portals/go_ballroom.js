// runScript [拉克兰]原形毕露之地 劇情

if (player.isQuestStarted(34319) || player.isQuestCompleted(34319)) {
        portal.playPortalSE();
        player.changeMap(450003400, 4);
} else {
        portal.abortWarp();
        player.runScript("QuestLKLCLOSE");
}
