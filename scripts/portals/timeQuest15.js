let mapId = map.getId();
let togo = mapId / 100 % 10 == 5 ? mapId + 9500 : mapId + 100;
portal.playPortalSE();
if (mapId != 270040100) {
        player.changeMap(togo,"out00");
} else {
        player.changeMap(270050000, 0);
}