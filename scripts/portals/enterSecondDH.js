if (player.isQuestActive(20871) || player.isQuestActive(20872) || player.isQuestActive(20873) || player.isQuestActive(20874) || player.isQuestActive(20875)) {
        portal.playPortalSE();
        player.changeMap(913001000, 0);
} else {
        player.showSystemMessage("请先完成训练!");
        portal.abortWarp();
}

