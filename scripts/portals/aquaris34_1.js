var event = portal.getEvent();
if (event != null) {
        if (player.hasItem(4009234, 10)) {
                player.loseItem(4009234, 10);
                player.teleportToPortalId(1);
        } else {
                map.blowWeatherEffectNotice("需要10張黃色皮革收集到才能上去。", 147, 3000);
        }
}
portal.abortWarp();