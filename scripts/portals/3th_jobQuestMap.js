
let mapID = 0;
if (player.isQuestStarted(1431) || player.isQuestStarted(1432) || player.isQuestStarted(1433)) {//warrior
    mapID = 910540100;
} else if (player.isQuestStarted(1435) || player.isQuestStarted(1436) || player.isQuestStarted(1437)) {//mage
    mapID = 910540200;
} else if (player.isQuestStarted(1439) || player.isQuestStarted(1440)) {//bowman
    mapID = 910540300;
} else if (player.isQuestStarted(1442) || player.isQuestStarted(1443) || player.isQuestStarted(1447)) {//theif
    mapID = 910540400;
} else if (player.isQuestStarted(1445) || player.isQuestStarted(1446) || player.isQuestStarted(1448)) {//pirate
    mapID = 910540500;
}
if (mapID > 0 && portal.makeEvent("change_job", [player, mapID]) != null) {
    portal.playPortalSE();
} else {
    player.changeMap(211040401, 0);
}
