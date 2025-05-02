// runScript
let eventName;
let key;
let scriptName;
switch (map.getId()) {
        case 105200300:
        case 105200310:
                eventName = "boss_bloody";
                scriptName = "bloody_accept";
                break;
        case 105200100:
        case 105200110:
                eventName = "boss_banban";
                scriptName = "banban_accept";
                break;
}
let event = portal.getEvent(eventName);
if (event != null) {
        portal.abortWarp();
        player.runScript(scriptName);
} else {
        let [map, spawnPoint] = portal.resetRememberedMap("UNITY_PORTAL");
        if (map == 999999999) { //warped to FM without having previous position saved
                map = 105200000; //Perion
                spawnPoint = "unityPortal2"; //market00 on Perion
        }
        portal.playPortalSE();
        player.showSystemMessage("从鲁塔比斯回到原来所在的地方。");
        player.changeMap(map, spawnPoint);
}
