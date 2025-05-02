// ID:[401060300]
// MapName:暴君的王座
// 暴君的王座

let event = player.getEvent("boss_magnus_easy");
if (event != null && event.getVariable("boss") == null) {
        event.setVariable("boss", false);
        map.spawnMob(8880010, 1860, -1347);
        map.startFieldEvent();;
}