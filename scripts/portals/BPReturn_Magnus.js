let [map, spawnPoint] = portal.resetRememberedMap("RETURN_MAP");
if (map == 999999999) { //warped to FM without having previous position saved
        map = 401000000;
        spawnPoint = 4;
}
portal.playPortalSE();
player.changeMap(map, spawnPoint);


