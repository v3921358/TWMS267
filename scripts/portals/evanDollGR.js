if (player.isQuestStarted(25584)) {
        player.updateQuestRecordEx(25583, "enter", "1");
        player.changeMap(910600201, 1);
} else {
        portal.abortWarp();
}
