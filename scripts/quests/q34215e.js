/*     
 *  
 *  功能：[啾啾島]彌補缺失的那2%的味道
 *  

 */

if (player.isQuestStarted(34215) && player.hasItem(4034958, 1)) {
    player.loseItem(4034958, 1);
    npc.completeQuest();
    player.gainExp(315000000);
    player.changeMap(450002205, 0);
}
