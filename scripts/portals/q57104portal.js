
var hayato = player.getJob() == 4100;
var warp = false;
var target = 0;
var p = 0;

switch (map.getId()) {
        case 807040000:
                if (hayato) {
                        warp = player.isQuestStarted(57104) || player.isQuestCompleted(57104);
                } else {
                        warp = player.isQuestStarted(57402);
                }
                target = 807040100;
                break;
        case 807040100:
                if (hayato) {
                        warp = player.isQuestCompleted(57104);
                } else {
                        warp = player.isQuestCompleted(57402);
                }
                target = 807000000;
                p = 2;
                break;
}
if (!warp) {
        portal.abortWarp();
        if (hayato) {
                player.showSystemMessage("與山中幸盛對話吧。");
        } else {
                player.showSystemMessage("與直江兼續對話吧。");
        }
} else {
        portal.playPortalSE();
        player.changeMap(target, p);
}