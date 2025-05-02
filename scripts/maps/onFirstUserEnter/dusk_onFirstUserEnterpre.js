let eventName;
switch (map.getId()) {
    case 450009400:
        eventName = "boss_dusk_normal";
        break;
    case 450009450:
        eventName = "boss_dusk_chaos";
        break;
}
let event = player.getEvent(eventName)
if (event != null && event.getVariable("boss1") == null) {
    event.setVariable("boss1", false)
    switch (map.getId()) {
        case 450009400: {
            //normal
            let boss = map.makeMob(8644650);
            boss.changeBaseHp(26_000_000_000_000);
            // boss.changeZoneType(1);
            map.spawnMob(boss, -45, -157); //main boss

            var mob = map.makeMob(8644658);
            mob.changeBaseHp(26_000_000_000_000);
            map.spawnMob(mob, -80, -157);
            map.startFieldEvent();
            break;
        }
        case 450009450: {
            //hard
            let boss = map.makeMob(8644655);
            boss.changeBaseHp(127_500_000_000_000);
            // boss.changeZoneType(1);
            map.spawnMob(boss, -45, -157); //main boss

            var mob = map.makeMob(8644659);
            mob.changeBaseHp(127_500_000_000_000);
            map.spawnMob(mob, -80, -157);
            map.startFieldEvent();
            break;
        }
    }
}