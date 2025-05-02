if (player.isQuestStarted(2570) || player.isQuestCompleted(2570)) {
        portal.playPortalSE();
        player.changeMap(120000101, 0);
} else {
        portal.abortWarp();
        player.showSystemMessage("先跟司徒諾交談吧！");
}