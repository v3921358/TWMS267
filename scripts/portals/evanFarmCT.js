
if (player.isQuestStarted(22010) || player.isQuestCompleted(22010) || player.getJob() != 2001) {
        player.changeMap(100030310, 3);
} else {
        player.showSystemMessage("現在還不是離開的時候！");
        portal.abortWarp();
}
