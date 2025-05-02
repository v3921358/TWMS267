let eventName;
switch (map.getId()) {
    case 450003960:
        eventName = "boss_lucid_easy";
        break;
    case 450004300:
        eventName = "boss_lucid_normal";
        break;
    case 450004600:
        eventName = "boss_lucid_hard";
        break;
}
let event = player.getEvent(eventName)
if (event != null && event.getVariable("reward") == null) {
    event.setVariable("reward", false)
    switch (map.getId()) {
        case 450003960: {//easy
            map.spawnMob(8880156, 93, 36);
            break;
        }
        case 450004300: {//normal
            map.spawnMob(8880167, 93, 36);
            break;
        }
        case 450004600: {//hard
            map.spawnMob(8880177, 93, 36);
            break;
        }
    }
}