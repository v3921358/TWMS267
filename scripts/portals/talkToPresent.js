if (player.getLevel() >= 200 && player.isQuestCompleted(1465)) {
        portal.playPortalSE();
        player.changeMap(450001003, 0);
} else {
        portal.abortWarp();
        player.showSystemMessage("有一股未知的力量阻擋著，無法進入！");
}