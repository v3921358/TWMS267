if (player.isQuestActive(21202)) {
        portal.playPortalSE();
        player.changeMap(914021010, 1);
} else {
        player.scriptProgressMessage("先跟熊鐵匠交談交談吧！");
        portal.abortWarp();
}

