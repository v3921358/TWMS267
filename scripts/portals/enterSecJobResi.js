
if (player.isQuestActive(23023) || player.isQuestActive(23024) || player.isQuestActive(23025) || player.isQuestActive(23162)) {
        if (portal.makeEvent("change_job", [player, 931000100]) != null) {
                portal.playPortalSE();
        } else {
                player.showSystemMessage("发生未知错误！");
                portal.abortWarp();
        }
} else {
        portal.playPortalSE();
        player.changeMap(310000010, 1);
}