if (player.isQuestStarted(20881) || player.isQuestStarted(20882) || player.isQuestCompleted(20881) && !player.isQuestActive(20882)) {
        player.forfeitQuest(20882);
        if (portal.makeEvent("change_job", [player, 922030400]) != null) {
                portal.playPortalSE();
        } else {
                portal.abortWarp();
        }
} else {
        player.showSystemMessage("這裡好像可以去某個地方，但我好像進不去");
        portal.abortWarp();
}