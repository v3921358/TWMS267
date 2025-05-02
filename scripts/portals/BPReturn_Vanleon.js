

let [map, spawnPoint] = portal.resetRememberedMap("RETURN_MAP");
if (map == 999999999) { //warped to FM without having previous position saved
    map = 211060800; //Perion
    spawnPoint = 8; //market00 on Perion
}
portal.playPortalSE();
player.changeMap(map, spawnPoint);
