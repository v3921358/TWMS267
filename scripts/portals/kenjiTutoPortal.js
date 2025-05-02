if (!player.isQuestStarted(57101)) {
        portal.abortWarp();
        player.showSystemMessage("先與武田信玄對話吧。");
} else {
        player.completeQuest(57101, 0);
        portal.playPortalSE();
        player.changeMap(807100012);
}

