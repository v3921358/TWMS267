/*      
 *
 *  功能：[拉克蘭]舞會面具
 *

 */
//npc.completeQuest();
player.setInGameCurNodeEventEnd(true);
player.setStandAloneMode(true);
player.setInGameDirectionMode(true, false, false, false);
player.setLayerBlind(true, 255, 1000);
player.setForcedInput(2000);
//player.setOverlapDetail(0, 200, 200, true);
player.setForcedInput(1);
player.setStandAloneMode(false);
player.setInGameDirectionMode(false, false, false, false);
//player.setOverlapDetail(0, 200, 200, true);
//player.removeOverlapDetail(1500);
npc.makeEvent("QuestEvent_02", [player, 450003750]);