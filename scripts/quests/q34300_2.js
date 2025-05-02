/*     
 *  
 *  功能：[拉克蘭]慶典正酣之都
 *  

 */

npc.next().sayX("你是從外面進來的嗎？");
npc.ui(1).uiMax().npcFlip().next().sayX("？");
npc.next().sayX("這個地方很危險…該死，已經晚了。");
player.setStandAloneMode(true);
player.setInGameDirectionMode(true, false, false, false);
player.setLayerBlind(true, 255, 1000);
player.setForcedInput(1000);
//player.setOverlapDetail(0, 500, 500, true);	
//player.removeOverlapDetail(1500);
npc.completeQuest();
player.changeMap(450003710, 0);
player.setStandAloneMode(false);
player.setInGameDirectionMode(false, false, false, false);