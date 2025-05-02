if (player.isQuestActive(20033)) {
        if (portal.makeEvent("change_job", [player, 913070020]) != null) {
                portal.playPortalSE();
        } else {
                player.showSystemMessage("发生未知错误！");
                portal.abortWarp();
        }
} else {
        portal.abortWarp();
        player.scriptProgressMessage("林伯特有話要跟你說！");
}

