if ("1".equals(player.getQuestRecordEx(3178))) {
        portal.playPortalSE();
        player.changeMap(921140001, 2);
} else {
        portal.abortWarp();
        player.scriptProgressMessage("王妃必须在这里" + player.getQuestRecordEx(3178) + "。");
}