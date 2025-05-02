/*     
 *
 *  功能：[消亡旅途]獲取神祕徽章
 *

 */

npc.ui(1).uiMax().next().sayX("#b（拾取了卡奧離開時留下的神祕徽章：消亡旅途。）");
if (player.getFreeSlots(1) > 0) {
    player.startQuest(39364, 3003113);
    npc.startQuest();
    npc.completeQuest();
    let newItem = player.makeItemWithId(1712001);
    player.gainItem(newItem);
    player.showProgressMessageFont("現在你可以在神祕河打獵獲取神祕徽章了，每日跑環獲得-蘑菇金幣也可以兌換神祕徽章。", 3, 20, 20, 0);//綠色的字
    //player.runScript("透過消亡的旅途獎勵")
    //npc.broadcastGachaponMsgEx("[任務公告] 恭喜 " + player.getName() + " 完成了劇情任務-【神祕河-消亡旅途】開啟拍賣選單-特色功能-劇情任務-即可領取培羅德幣、超越證明戒指、世界樹符文石、10億金幣" + newItem.getItemName(), newItem);
} else {
    npc.next().sayX("請保證裝備欄有足夠的空格。");
}