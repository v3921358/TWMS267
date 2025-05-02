
let event = player.getEvent();
if (event != null) {
    sh.debug(map.getId());
    if (map.getId() == 922010700) {
        let complete = event.getVariable("4stageclear");
        if (complete != null && complete) {
            player.loseItem(4001022);
            player.loseItem(4001023);
            portal.playPortalSE();
            player.changeMap(922010800, 0);
        } else {
            portal.block();
        }
    } else if (map.getId() == 922010800) {
        let complete = event.getVariable("5stageclear");
        if (complete != null && complete) {
            player.loseItem(4001022);
            player.loseItem(4001023);
            portal.playPortalSE();
            player.changeMap(922010900, 0);
        } else {
            portal.block();
        }
    }else{
        portal.block();
    }
}
