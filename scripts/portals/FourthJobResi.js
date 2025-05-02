
let mapId = -1;
if (player.isQuestStarted(23043) || player.isQuestCompleted(23043)) {
        if (player.isQuestCompleted(23046) || player.hasItem(4032743, 1)) {
                mapId = 931000300;
        }
} else if (player.isQuestStarted(23044) || player.isQuestCompleted(23044)) {
        if (player.isQuestCompleted(23047) || player.hasItem(4032743, 1)) {
                mapId = 931000301;
        }
} else if (player.isQuestStarted(23045) || player.isQuestCompleted(23045)) {
        if (player.isQuestCompleted(23048) || player.hasItem(4032743, 1)) {
                mapId = 931000302;
        }
} else if (player.isQuestStarted(23166) || player.isQuestCompleted(23166)) {
        if (player.isQuestCompleted(23167) || player.hasItem(4032743, 1)) {
                mapId = 931000303;
        }
}

if (mapId > 0) {
        portal.playPortalSE();
        player.changeMap(mapId, 0);
} else {
        player.showSystemMessage("无法进入！");
        portal.abortWarp();
}