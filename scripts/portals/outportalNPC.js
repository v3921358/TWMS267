// runScript
portal.abortWarp();
if (player.isQuestStarted(30002)) {
        player.runScript("root_outportal");
} else {
        player.changeMap(105010200, 0);
}