
let [map, spawnPoint] = portal.resetRememberedMap("UNITY_PORTAL");
if (map == 999999999) { 
        map = 100000000;
        spawnPoint = 0;
}
portal.playPortalSE();
player.changeMap(map, spawnPoint);


