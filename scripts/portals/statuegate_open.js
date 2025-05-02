let event = portal.getEvent("guild_quest");
if (event != null) {
        let open = event.getVariable("state_s1");
        if ("clear".equals(open)) {
                portal.playPortalSE();
                player.changeMap(990000301, 0);
        } else {
                portal.abortWarp();
                player.showSystemMessage("大門是關閉著的，請完成守門人的考驗。");
        }
} else {
        player.dropAlertNotice("发生错误！");
        player.changeMap(102040200, 0);
}