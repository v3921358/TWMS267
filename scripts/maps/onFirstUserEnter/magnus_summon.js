// ID:[401060100]
// MapName:暴君的王座
// 暴君的王座

let event = player.getEvent("boss_magnus_hard");
if (event != null && event.getVariable("boss") == null) {
        event.setVariable("boss", false);
        map.spawnMob(8880000, 1860, -1347);
        map.startFieldEvent();;
}