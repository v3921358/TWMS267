
let event = portal.getEvent("guild_quest");
if (event != null) {
        let clear = event.getVariable("state_s3");
        if ("clear".equals(clear)) {
                portal.playPortalSE();
                player.changeMap(990000600, 0);
        } else {
                portal.abortWarp();
        }
} else {
        player.dropAlertNotice("发生错误！");
        player.changeMap(102040200, 0);
} 