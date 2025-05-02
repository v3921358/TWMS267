if (!player.isQuestStarted(21000) && !player.isQuestCompleted(21000)) {
        portal.abortWarp();
        player.showSystemMessage("先瞭解下情况在說吧！");

} else {
        portal.playPortalSE();
        player.changeSkillLevel(20000017, 1);
        player.changeSkillLevel(20000018, 1);
        player.changeMap(914000200, 1);
}
