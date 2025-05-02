
portal.abortWarp();
portal.playPortalSE();
//if (player.isQuestStarted(32121) || player.isQuestCompleted(32121)){ 
if (player.isQuestCompleted(32121)) {
//if (player.isQuestCompleted(32120)) {
        player.changeMap(101073000, 3);//32135
        player.startQuest(32135, 1500000, "0");
} else {
        player.scriptProgressMessage("現在還不能進去。");
}
