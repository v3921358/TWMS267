// ID:[401060200]
// MapName:暴君的王座
// 暴君的王座

let event = player.getEvent("boss_magnus_normal");
if (event != null && event.getVariable("boss") == null) {
        event.setVariable("boss", false);
        map.spawnMob(8880002, 1860, -1347);
        map.startFieldEvent();;
}