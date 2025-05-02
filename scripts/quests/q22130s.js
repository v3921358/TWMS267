/**
 *
 *
 */
if (player.getJob() == 2001) {
    npc.completeQuest();
    player.scriptProgressMessage("孵化器裡的蛋中孵化出了幼龍，獲得了可以提升龍的技能的3點SP，幼龍好像想說話。點選幼龍，和它說話吧！");
    player.gainEquipInventorySlots(4);
    player.gainUseInventorySlots(4);
    player.gainSetupInventorySlots(4);
    player.gainEtcInventorySlots(4);
    player.gainCashInventorySlots(4);
    player.setJob(2200);
    player.gainSp(5);
    let string = ["UI/tutorial/evan/14/0"];
    npc.sayImage(string);
}