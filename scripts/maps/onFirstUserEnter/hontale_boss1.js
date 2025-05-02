let eventName;
let mobId;
switch (map.getId()) {
        case 240060000:
                eventName = "boss_hontale_normal";
                mobId = 8810024;
                break;
        case 240060001:
                eventName = "boss_hontale_chaos";
                mobId = 8810100;
                break;
        case 240060002:
                eventName = "boss_hontale_easy";
                mobId = 8810200;
                break;
}
let event = player.getEvent(eventName);
if (event != null && event.getVariable("boss1") == null) {
        event.setVariable("boss1", false)
        map.spawnMob(mobId, 880, 260);
}
