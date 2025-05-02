if (player.isQuestActive(23033) || player.isQuestActive(23034) || player.isQuestActive(23035) || player.isQuestActive(23164)) {
        if (portal.makeEvent("change_job", [player, 931000200]) != null) {
                portal.playPortalSE();
        } else {
                player.showSystemMessage("发生未知错误！");
                portal.abortWarp();
        }
} else {
        player.showSystemMessage("當前無法進入！");
        portal.abortWarp();
}