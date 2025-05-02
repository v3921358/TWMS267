let eventName;
switch (map.getId()) {
    case 450008050:
        eventName = "boss_will_easy";
        break;
    case 450008950:
        eventName = "boss_will_normal";
        break;
    case 450008350:
        eventName = "boss_will_hard";
        break;
}
let event = player.getEvent(eventName)
if (event != null && event.getVariable("boss3") == null) {
    event.setVariable("boss3", false)
    switch (map.getId()) {
        case 450008050: {
            //easy
            let boss = map.makeMob(8880362);
            boss.changeBaseHp(7_000_000_000_000);
            map.spawnMob(boss,-2, 281); //main dummy boss

            map.spawnMob(8880375, 352, 215); //dummy
            map.spawnMob(8880379, 252, 215); //dummy
            map.startFieldEvent();
            break;
        }
        case 450008950: {
            //normal
            let boss = map.makeMob(8880342);
            boss.changeBaseHp(10_500_000_000_000);
            map.spawnMob(boss,-2, 281); //main dummy boss

            map.spawnMob(8880354, 352, 215); //dummy
            map.spawnMob(8880358, 252, 215); //dummy
            map.startFieldEvent();
            break;
        }
        case 450008350: {
            //hard
            let boss = map.makeMob(8880302);
            boss.changeBaseHp(52_500_000_000_000);
            map.spawnMob(boss,-2, 281); //main dummy boss

            map.spawnMob(8880324, 352, 215); //dummy
            map.spawnMob(8880328, 252, 215); //dummy
            map.startFieldEvent();
            break;
        }
    }
}