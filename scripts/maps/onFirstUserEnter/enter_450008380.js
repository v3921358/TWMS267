let eventName;
switch (map.getId()) {
    case 450008080:
        eventName = "boss_will_easy";
        break;
    case 450008980:
        eventName = "boss_will_normal";
        break;
    case 450008380:
        eventName = "boss_will_hard";
        break;
}
let event = player.getEvent(eventName)
if (event != null && event.getVariable("reward") == null) {
    event.setVariable("reward", false)
    switch (map.getId()) {
        case 450008080: {
            //normal
            map.spawnMob(8950113, -4, 248);
            break;
        }
        case 450008980: {
            //normal
            map.spawnMob(8950114, -4, 248);
            break;
        }
        case 450008380: {
            //normal
            map.spawnMob(8950115, -4, 248);
            break;
        }
    }
}