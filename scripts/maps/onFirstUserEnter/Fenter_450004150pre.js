let eventName;
switch (map.getId()) {
    case 450003840:
        eventName = "boss_lucid_easy";
        break;
    case 450004150:
        eventName = "boss_lucid_normal";
        break;
    case 450004450:
        eventName = "boss_lucid_hard";
        break;
}
let event = player.getEvent(eventName)
if (event != null && event.getVariable("boss1") == null) {
    event.setVariable("boss1", false)
    switch (map.getId()) {
        case 450003840: {//easy
            var mob = map.makeMob(8645009);
            mob.changeBaseHp(6_000_000_000_000);
            map.spawnMob(mob, 1000, 43);
            map.spawnMob(8880158, 1000, 43);
            map.spawnMob(8880165, 900, 43);
            map.spawnMob(8880168, 900, 43);
            map.spawnMob(8880169, 900, 43);
            map.startFieldEvent();
            break;
        }
        case 450004150: {//normal
            var mob = map.makeMob(8880140);
            mob.changeBaseHp(12_000_000_000);
            map.spawnMob(mob, 1000, 43);
            map.spawnMob(8880158, 1000, 43);
            map.spawnMob(8880165, 900, 43);
            map.spawnMob(8880168, 900, 43);
            map.spawnMob(8880169, 900, 43);
            player.resetPQLog("LucidHornGauge");
            map.startFieldEvent();
            break;
        }
        case 450004450: {//hard
            var mob = map.makeMob(8880141);
            mob.changeBaseHp(58_800_000_000);
            map.spawnMob(mob, 1000, 43);
            map.spawnMob(8880158, 1000, 43);
            map.spawnMob(8880165, 900, 43);
            map.spawnMob(8880168, 900, 43);
            map.spawnMob(8880169, 900, 43);
            player.resetPQLog("LucidHornGauge");
            map.startFieldEvent();
            break;
        }
    }
}
