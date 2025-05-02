if (player.isQuestStarted(22008) || player.isQuestCompleted(22008) || player.getJob() != 2001) {
        player.changeMap(100030103, "west00");
} else {
        player.showSystemMessage("現在還不是離開的時候！");
        portal.abortWarp();
}
