/*    
 *
 *  功能：[消亡旅途]每日任務
 *

 */
let ItemId = 1712001;
let Itemcount = 3;

Itemcount = player.isQuestCompleted(34164) ? Itemcount++ : Itemcount;
Itemcount = player.isQuestCompleted(34165) ? Itemcount++ : Itemcount;
Itemcount = player.isQuestCompleted(34166) ? Itemcount++ : Itemcount;
Itemcount = player.isQuestCompleted(34167) ? Itemcount++ : Itemcount;

if (player.canGainItem(1712001, Itemcount)) {
    player.startQuest(39364, "1");
    npc.completeQuest();
    player.gainItem(1712001, Itemcount);
    player.updateQuestRecordEx(34127, "count", String(0));
} else {
    player.dropAlertNotice("裝備欄至少有5格空位。");
}