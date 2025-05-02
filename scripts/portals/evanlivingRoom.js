if (player.isQuestStarted(22000) || player.isQuestCompleted(22000)) {
        player.changeMap(100030102, 2);
} else {
        player.showSystemMessage("先跟媽媽談談在出去吧！");
        portal.abortWarp();
}