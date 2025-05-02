let mapId = map.getId();
let togo = mapId / 100 % 10 == 5 ? mapId + 9500 : mapId + 100;
portal.playPortalSE();
switch (mapId) {
        case 270030400:
                player.changeMap(270030410, "out00");
                break;
        case 270040100:
                player.changeMap(270050000, 0);
                break;
        default:
                player.changeMap(togo, "out00");
                break;
}
