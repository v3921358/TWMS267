//
// Quest ID:17006
// [凱梅爾茲交易所]第二次航行


let step = player.getQuestRecordEx(17006);
if (!"2".equals(player.getQuestRecordEx(17009, "step"))) {
    player.updateQuestRecordEx(17009, "step", "2");
}
if ("2".equals(step)) {
    npc.sayNextIllusNoEsc("快看,貿易品購買窗出現了吧?剛開始能夠購買的貿易品不是很多,但是以後隨著交易地區逐漸增多,能夠購買的貿易品也會變多的,不用擔心。", 9390220, 9390220, 1, true);
    npc.sayNextIllusNoEsc("把滑鼠放在圖示上,就能檢視到各個貿易品的情況了。", 9390220, 9390220, 1, true);
    npc.sayNextIllusNoEsc("抓怪物時,偶爾可以撿到怪物掉落的物品,可以把其中比較珍貴的物品作為個人貿易品來賣。這種珍貴的物品可以賣到很多金幣的。 ", 9390220, 9390220, 1, true);
    npc.sayIllusNoEsc("購買貿易品需要使用金幣。試著用我剛剛給你的金幣來購買貿易品吧?試著運載2個 #t3100000# 吧。", 9390220, 9390220, 1, true);
    player.updateQuestRecordEx(17006, "");
} else if ("3".equals(step)) {
    npc.sayNextIllusNoEsc("你做得很好。馬上就要出航了,快上船吧。 ", 9390220, 9390220, 1, false);
    player.updateQuestRecordEx(17006, "");
    npc.makeEvent("sail_event", [player, 865000100]);
} else {
    npc.sayNextIllusNoEsc("但第一次航海太高興了,結果忘記要裝載貿易品了。那.. 這次就試著購買貿易品吧。請再點一次多爾切地區吧。 ", 9390220, 9390220, 1, false);
    player.openUI(554);
}