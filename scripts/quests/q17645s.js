// 
// Quest ID:17645
// [凱梅爾茲共和國] 與普賽依交戰


npc.id(9390235).ui(1).npcFlip().next().sayX("果然是受了普賽依的指揮的傢伙們更有組織性了!");
npc.next().sayX("謝謝你的稱讚。");
npc.id(9390202).ui(1).npcFlip().next().sayX("你是海盜王普賽依!!");
npc.next().sayX("能把我們趕到這裡,實力還算是夠可以得啦。但是玩笑就開到這為止了。");
if (npc.askYesNo("到底誰不行等著瞧就知道了!#h0#!準備好了嗎!?", 9390235)) {
    npc.startQuest();
    for (let i = 0; i < 45; i++) {
        map.spawnMob(9390815, -332 + i * 10, 198);
    }
    npc.id(9390235).ui(1).uiMax().npcFlip().next().noEsc().sayX("把海盜們從這海峽趕出去!!");
    npc.id(9390235).ui(1).uiMax().npcFlip().next().noEsc().sayX("好的,痛快地幹一場吧!!");
} else {
    npc.id(9390235).ui(1).npcFlip().sayX("沒時間了!趕快準備吧。");
}