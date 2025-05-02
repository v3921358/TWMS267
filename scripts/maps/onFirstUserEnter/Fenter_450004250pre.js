let eventName;
switch (map.getId()) {
    case 450003920:
        eventName = "boss_lucid_easy";
        break;
    case 450004250:
        eventName = "boss_lucid_normal";
        break;
    case 450004550:
        eventName = "boss_lucid_hard";
        break;
}
let event = player.getEvent(eventName)
if (event != null && event.getVariable("boss2") == null) {
    event.setVariable("boss2", false)
    switch (map.getId()) {
        case 450003920: {//easy
            var mob = map.makeMob(8880155);
            mob.changeBaseHp(6_000_000_000_000);
            map.spawnMob(mob, 711, -490);
            map.spawnMob(8880175, 900, -331);
            map.spawnMob(8880178, 900, -331);
            map.spawnMob(8880179, 900, -331);
            map.startFieldEvent();
            break;
        }
        case 450004250: {//normal
            var mob = map.makeMob(8880150);
            mob.changeBaseHp(12_000_000_000_000);
            map.spawnMob(mob, 711, -490);
            map.spawnMob(8880175, 900, -331);
            map.spawnMob(8880178, 900, -331);
            map.spawnMob(8880179, 900, -331);
            map.startFieldEvent();
            break;
        }
        case 450004550: {//hard
            var mob = map.makeMob(8880151);
            mob.changeBaseHp(54_000_000_000_000);
            map.spawnMob(mob, 711, -490);
            map.spawnMob(8880175, 900, -331);
            map.spawnMob(8880178, 900, -331);
            map.spawnMob(8880179, 900, -331);
            map.startFieldEvent();
            break;
        }
    }
}