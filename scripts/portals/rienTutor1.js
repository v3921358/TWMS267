if (player.isQuestCompleted(21010)) {
        portal.playPortalSE();
        player.changeMap(140090200, 1);
} else {
        portal.abortWarp();
        player.scriptProgressMessage("你必須完成任務後，才能進入下一個地圖！");
}
