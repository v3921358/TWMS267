// runScript
portal.abortWarp();
if (player.isQuestStarted(32214)) {
    player.runScript("snail_king");
} else if (player.isQuestCompleted(32214)) {
    player.runScript("set_sail");
}