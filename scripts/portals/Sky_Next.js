let stage = parseInt((map.getId() - 240080000) / 100);
let event = portal.getEvent("party_dragon_rider");
if (event != null) {
        let clear = map.getId() == 240080400 ? true : event.getVariable(stage + "stageclear");
        if (clear != null && clear) {
                portal.playPortalSE();
                party.changeMap(map.getId() + 100, 0);
        } else {
                portal.block();
        }
} else {
        portal.block();
}
