let eventName;
switch (map.getId()) {
    case 450010930:
        eventName = "boss_jinhillah_normal";
        break;
    case 450010500:
        eventName = "boss_jinhillah_hard";
        break;
}
let event = player.getEvent(eventName)
if (event != null && event.getVariable("boss1") == null) {
    event.setVariable("boss1", false)
    switch (map.getId()) {
        case 450010930: {
            //normal
            let boss = map.makeMob(8880405);
            boss.changeBaseHp(88_000_000_000_000);
            map.spawnMob(boss, 0, 266);

            map.spawnMob(8880406, 0, 266); //dummy
            map.spawnMob(8880407, 0, 266); //dummy
            map.startFieldEvent();
            break;
        }
        case 450010500: {
            //hard
            let boss = map.makeMob(8880410);
            boss.changeBaseHp(176_000_000_000_000);
            map.spawnMob(boss, 0, 266);

            map.spawnMob(8880411, 0, 266); //dummy
            map.spawnMob(8880412, 0, 266); //dummy
            map.startFieldEvent();
            break;
        }
    }
}