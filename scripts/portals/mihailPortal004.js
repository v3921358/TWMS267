if (portal.makeEvent("change_job", [player, 913070050]) != null) {
        portal.playPortalSE();
} else {
        player.showSystemMessage("发生未知错误！");
        portal.abortWarp();
}

