// runScript [消亡旅途]安息洞穴 - 通向啾啾岛 劇情
if (player.getLevel() < 210) {
        player.scriptProgressMessage("只有210級以上玩家才能進入該區域！");
        portal.playPortalSE();
        player.changeMap(450001250, 1);
} else if (!player.isQuestCompleted(34120)) {
        player.scriptProgressMessage("請完成任務後離開！");
        portal.playPortalSE();
        player.changeMap(450001250, 1);
} else if (player.isQuestCompleted(34120) && player.getLevel() >= 210) {
        //portal.abortWarp();
        player.runScript("ChewChewfirst");
} else {
        portal.playPortalSE();
        player.changeMap(450001250, 1);
        player.scriptProgressMessage("210級以上才能過去！");
}
