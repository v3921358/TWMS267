let event = portal.getEvent("guild_quest");
if (event != null) {
        let open = event.getVariable("stonegate");
        if ("open".equals(open)) {
                portal.playPortalSE();
                player.changeMap(990000430, 0);
        } else {
                portal.abortWarp();
                player.showSystemMessage("這個門是關閉的。");
        }
} else {
        player.dropAlertNotice("发生错误！");
        player.changeMap(102040200, 0);
} 