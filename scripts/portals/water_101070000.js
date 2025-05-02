// 仲夏夜森林 掉入水中 runScript

if (player.isQuestStarted(32102)) {
        player.runScript("Diving");
} else {
        portal.playPortalSE();
        player.changeMap(101070000, 0);
}

