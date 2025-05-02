let event = player.getEvent();
if (event != null) {
    if (map.getEventMobCount() <= 0) {
        var cleared = Math.random() > 0.9;
        if (cleared) {
            party.changeMap(921160400, 0);
        } else {
            player.changeMap(921160300 + (Math.floor(Math.random() * 6) | 0) * 10, 0);
        }
    } else {
        portal.abortWarp();
        map.blowWeather(5120053, "请消灭守护门的警卫！");
    }
} else {
    portal.block();
}
