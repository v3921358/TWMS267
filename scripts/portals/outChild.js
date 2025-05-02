if (!player.isQuestStarted(21001) && !player.isQuestCompleted(21001)) {
        portal.abortWarp();
        player.showSystemMessage("得先解救孩子，不能吧孩子丟下不管！");

} else {
        portal.playPortalSE();
        player.changeMap(914000400, 2);
}
