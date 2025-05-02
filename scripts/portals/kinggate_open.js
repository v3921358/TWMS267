let event = portal.getEvent("guild_quest");
if (event != null) {
        let stat = map.getReactorStateId("ghostgate");
        if (stat > 0) {
                portal.playPortalSE();
                player.changeMap(990000800, 0);
        } else {
                player.showSystemMessage("神秘的力量封锁著你的前進。"+stat);
                portal.abortWarp();
        }
} else {
        player.dropAlertNotice("发生错误！");
        player.changeMap(990001100, 0);
} 