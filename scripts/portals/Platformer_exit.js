let [map, spawnPoint] = portal.resetRememberedMap("RETURN_MAP");
if (map == 999999999) { //warped to FM without having previous position saved
        map = 100000000;
        spawnPoint = 0;
}
portal.playPortalSE();
player.changeMap(map, spawnPoint);


