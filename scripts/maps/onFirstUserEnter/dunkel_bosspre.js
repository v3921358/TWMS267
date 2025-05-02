let eventName;
switch (map.getId()) {
    case 450012210:
        eventName = "boss_dunkel_normal";
        break;
    case 450012600:
        eventName = "boss_dunkel_hard";
        break;
}
let event = player.getEvent(eventName)
if (event != null && event.getVariable("boss1") == null) {
    event.setVariable("boss1", false)
    switch (map.getId()) {
        case 450012210: {
            //normal
            var mob = map.makeMob(8645009);
            mob.changeBaseHp(26_000_000_000_000);
            map.spawnMob(mob, 0, 29);
            map.spawnMob(8645067, 0, 29); //dummy
            map.startFieldEvent();
            break;
        }
        case 450012600: {
            //hard
            var mob = map.makeMob(8645066);
            mob.changeBaseHp(157_500_000_000_000);
            map.spawnMob(mob, 0, 29);
            map.spawnMob(8645068, 0, 29); //dummy
            map.startFieldEvent();
            break;
        }
    }
}