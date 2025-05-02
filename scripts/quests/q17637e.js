//
// Quest ID:17637
// [凱梅爾茲共和國] 這次的事也由我來解決吧


npc.next().sayX("好吧,希望這次你也能助我們一臂之力。");
npc.id(9390202).ui(1).npcFlip().next().sayX("謝謝。首領!");
npc.next().sayX("你可以藉助他的力量,但不可依賴於他的力量。");
npc.id(9390202).ui(1).npcFlip().next().sayX("好的,我會牢記的。我一定會憑我的力量打倒普賽依的。");
npc.ui(1).uiMax().next().sayX("太好了。萊文。謝謝。首領。");
npc.id(9390202).ui(1).npcFlip().next().sayX("嗯?這次就拜託你了。我還有很多事情要告訴你,準備好的話就跟我說。");
npc.id(9390202).ui(1).npcFlip().next().sayX("好，你拿著這個，這禮物給你是為了讓我們一起好好加油。");
let itemId = 1082607 + player.getJobCategory();
if (player.gainItem(itemId, 1)) {
    npc.completeQuest();
    player.gainExp(953667);
    npc.ui(1).uiMax().next().noEsc().sayX("萊文就拜託給你了。也許是因為還小所以好像有些慾望高脹。");
    npc.ui(1).uiMax().next().noEsc().sayX("請別擔心。根據我一直在旁的觀察來看,我覺得他一定能出色地完成任務的。");
} else {
    npc.id(9390201).ui(1).uiMax().npcFlip().next().sayX("你的揹包滿了，清理下吧！");
}
