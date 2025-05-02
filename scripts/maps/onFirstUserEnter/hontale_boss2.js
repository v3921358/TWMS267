let eventName;
let mobId;
switch (map.getId()) {
        case 240060100:
                eventName = "boss_hontale_normal";
                mobId = 8810100;
                break;
        case 240060101:
                eventName = "boss_hontale_chaos";
                mobId = 8810101;
                break;
        case 240060102:
                eventName = "boss_hontale_easy";
                mobId = 8810201;
                break;
}
let event = player.getEvent(eventName);
if (event != null && event.getVariable("boss2") == null) {
        event.setVariable("boss2", false);
        map.spawnMob(mobId, -350, 260);
}
