// 
// Quest ID:17646
// [凱梅爾茲共和國] 海里的暴徒


if (npc.askYesNo("#h0#!!快來幫忙!!")) {
    npc.id(9390202).ui(1).uiMax().npcFlip().next().noEsc().sayX("大家都鎮定點兒!!");
    npc.ui(1).uiMax().next().noEsc().sayX("萊文!!沒事吧?那些令人作嘔的腿腳都是什麼呀");
    npc.ui(1).uiMax().next().noEsc().sayX("深海巨妖是海洋的霸主！請小心，不要死了哦！");
    map.spawnMob(9390835, 33, 250);
    npc.startQuest();
} else {
    npc.ui(1).sayX("喂,你在磨蹭什麼呢!!");
}
