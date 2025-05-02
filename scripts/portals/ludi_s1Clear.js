

let event = player.getEvent();
if (event != null) {
        let complete = event.getVariable("1stageclear");
        if (complete != null && complete) {
                player.loseItem(4001022)
                player.loseItem(4001023)
                portal.playPortalSE();
                player.changeMap(922010400, 0);
        } else {
                portal.block();
        }
}
