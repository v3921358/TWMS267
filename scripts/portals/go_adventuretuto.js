switch (map.getId()) {
    case 4000012:
        if (player.isQuestCompleted(32204)) {
            player.changeMap(4000013, 0);
            portal.playPortalSE();
        } else {
            portal.abortWarp();
        }
        break;
    case 4000013:
        if (player.isQuestCompleted(32207)) {
            player.changeMap(4000014, 0);
            portal.playPortalSE();
        } else {
            portal.abortWarp();
        }
        break;
    case 4000014:
        if (player.isQuestStarted(32210)) {
            player.changeMap(4000020, 0);
            portal.playPortalSE();
        } else {
            portal.abortWarp();
        }
        break;
}
if (!portal.warped()) {
    player.dropAlertNotice("先完成麥加的任務在離開吧！");
}