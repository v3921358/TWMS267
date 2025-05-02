let check = false;
switch (map.getId()) {
        case 958000000:
                check = true;
                break;
        case 958000100:
                if (player.getPQLog("Welcome1") > 0) {
                        check = true;
                } else {
                        player.dropAlertNotice("領取了獎勵再出發到下個階段吧！");
                }
                break;
        case 958000200:
                if (player.getPQLog("Welcome2") > 0) {
                        check = true;
                } else {
                        player.dropAlertNotice("領取了獎勵再出發到下個階段吧！");
                }
                break;
        case 958000300:
                if (player.getPQLog("Welcome3") > 0) {
                        check = true;
                } else {
                        player.dropAlertNotice("領取了獎勵再出發到下個階段吧！");
                }
                break;
}

if (check) {
        portal.playPortalSE();
        player.changeMap(map.getId() + 100, 0);
} else {
        portal.abortWarp();
}


