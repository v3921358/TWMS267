let [map, spawnPoint] = portal.resetRememberedMap("GUILD_FIELD");
if (map == 999999999) { //warped to FM without having previous position saved
    map = 200000300; //Perion
    spawnPoint = 11; //market00 on Perion
}
portal.playPortalSE();
player.changeMap(map, spawnPoint);
