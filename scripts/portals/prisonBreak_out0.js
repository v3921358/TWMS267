portal.abortWarp();
let mapId = map.getId();
let event = player.getEvent();
if (event != null) {
    if (mapId == 921160700) {
        if (!!event.getVariable("boss")) {
            player.changeMap(921160701);
        } else {
            portal.showHint("到这里已经回不了头了，请击败BOSS！", 250, 5);
        }
    } else {
        player.runScript("逃脱出口");
    }
} else {
    player.changeMap(921160000);
}

