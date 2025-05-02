/**
 *
 *
 */
npc.next().sayX("哈哈，你來啦？紅雅怎麼可能真的讓你吃毒藥呢，這個只是紅雅的考驗啦，並不是毒藥！但是，你一旦背叛，就會永遠與暗影雙刀全員為敵！");
let ret = npc.askYesNo("你做好準備成為飛花院的一員了嗎？");
if (ret == 1) {
    npc.completeQuest();
    player.gainEquipInventorySlots(4);
    player.gainUseInventorySlots(4);
    player.gainSetupInventorySlots(4);
    player.gainEtcInventorySlots(4);
    player.gainCashInventorySlots(4);
    player.setJob(400);
    player.resetStats(4, 4, 4, 4);
    player.gainSp(3);
    npc.next().sayX("好了，現在你也是飛花院的一員了。現在你有了很多暗影雙刀技能，開啟技能窗看看吧。我給了你一些SP，你可以用它提升技能。等級提高之後，你可以使用更多的技能。希望你努力修煉。");
    npc.next().sayX("光是技能還不行。請你開啟屬性窗，按照暗影雙刀的需要對自己的屬性進行分配。暗影雙刀必須以運氣為中心。不知道的話，可以使用#b自動分配#k。");
    npc.next().sayX("此外我再送你一件禮物。我幫你增加了其他欄的空格數。如果是需要的東西，可以放進揹包帶在身上。");
    npc.next().sayX("啊，還有一件事不能忘記。你成為了暗影雙刀，在戰斗的時候必須注意管理體力。死亡的話，之前技能的經驗值會受到損失。好不容易獲得的經驗值，如果損失的話，豈不是太可惜了。");
    npc.next().sayX("好了！我能教你的就是這些。我給了你幾件適合你使用的武器，你去一邊旅行，一邊鍛鍊自己吧。明白了嗎？");
} else {
    npc.next().sayX("好的，那等你準備好了在來吧。");
}