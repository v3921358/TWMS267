// runScript
if (player.isQuestCompleted(24002) && !player.isQuestStarted(24093)) {
        portal.abortWarp();
        player.runScript("outEurel");
} else {
        if (player.isQuestCompleted(24045) || player.isQuestStarted(24045) || player.getJob() != 2300) {
                portal.playPortalSE();
                player.changeMap(101050100, 2);
        } else {
                portal.abortWarp();
                player.scriptProgressMessage("先看看村子的情况在出去吧！");
        }
}
