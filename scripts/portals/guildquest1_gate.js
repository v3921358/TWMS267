let event = portal.getEvent("guild_quest");
if (event != null) {
        let open = event.getVariable("open");
        if (open) {
                portal.playPortalSE();
                player.changeMap(990000100, 0);
        } else {
                portal.abortWarp();
                player.showSystemMessage("传送口是关闭的。");
        }
} else {
        player.dropAlertNotice("发生错误！");
        player.changeMap(102040200, 0);
}