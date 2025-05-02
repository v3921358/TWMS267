//
// Quest ID:17007
// [凱梅爾茲交易所]第三次航行

let step = player.getQuestRecordEx(17007);
if (!"3".equals(player.getQuestRecordEx(17009, "step"))) {
    player.updateQuestRecordEx(17009, "step", "3");
}
if ("4".equals(step)) {
    npc.sayNextIllusNoEsc("在出航之前,可以瀏覽貿易相關的情報。可以在這檢視艦船情況。", 9390220, 9390220, 1, false);
    npc.sayIllusNoEsc("我們只以1金幣為單位進行貿易,因此有時最小利益會出現0金幣,這時,請要回去購買裝船物品,用其他組合方式進行裝載。請按出航按鈕。", 9390220, 9390220, 1, false);
    player.updateQuestRecordEx(17007, "");
} else {
    npc.sayNextIllusNoEsc("實際上.. 貿易途中會不時出現怪物。雖然我很信任你,但是在吃力時,你也可以尋求其他艦船的幫助。 ", 9390220, 9390220, 1, true);
    npc.sayNextIllusNoEsc("本次航海是我們陪你進行的最後一次航海。請再次將貿易地設為[多爾切],運載2個 #t3100000##i3100000#吧。", 9390220, 9390220, 1, true);
    player.openUI(554);
}

