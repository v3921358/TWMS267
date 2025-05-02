if (player.isQuestCompleted(30006)) {
        portal.rememberMap("UNITY_PORTAL");
        portal.playPortalSE();
        player.changeMap(105200000, 0);
} else if (player.isQuestCompleted(30000)) {
        player.changeMap(105010200, 1);
} else {
        player.scriptProgressMessage("被迷霧覆蓋，還是不要進去了。");
}

