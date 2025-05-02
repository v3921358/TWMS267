if (player.isQuestStarted(22003) || player.isQuestCompleted(22003) || player.getJob() != 2001) {
        player.changeMap(100030200, 2);
} else {
        player.showSystemMessage("現在還不是離開的時候！");
        portal.abortWarp();
}
