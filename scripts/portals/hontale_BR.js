let eventName;
let key;

switch (map.getId()) {
    case 240060000:
        key = "boss1";
        eventName = "boss_hontale_normal";
        break;
    case 240060100:
        key = "boss2";
        eventName = "boss_hontale_normal";
        break;
    case 240060001:
        key = "boss1";
        eventName = "boss_hontale_chaos";
        break;
    case 240060101:
        key = "boss2";
        eventName = "boss_hontale_chaos";
        break;
    case 240060002:
        key = "boss1";
        eventName = "boss_hontale_easy";
        break;
    case 240060102:
        key = "boss2";
        eventName = "boss_hontale_easy";
        break;
}

let event = portal.getEvent(eventName);
if (event != null && event.getVariable(key) != null && event.getVariable(key)) {
    portal.playPortalSE();
    party.changeMap(map.getId() + 100);
} else {
    portal.block();
}


