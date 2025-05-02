let target;
switch (map.getId()) {
        case 100000201:
                target = 450000100;
                break;
        case 400000001:
                target = 450000110;
                break;
        case 105300000:
                if (player.isQuestActive(1464)) {
                        player.showSystemMessage("需要找到另外一個傳送口！");
                } else {
                        target = 450000120;
                }
                break;
        case 105300301:
                if (player.isQuestActive(1464)) {
                        target = 450000120;
                }
                break;
}
if (player.isQuestActive(1461)) {
        portal.playPortalSE();
        player.changeMap(target, 0);
} else {
        portal.abortWarp();
}
