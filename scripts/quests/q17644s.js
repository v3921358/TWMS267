//
// Quest ID:17644
// [凱梅爾茲共和國] 追擊！


npc.ui(1).uiMax().next().sayX("是因為是海盜,所以航海技術果然更勝一籌嗎?");
npc.next().sayX("就是那樣。比起商船移動起來迅猛地多了。");
npc.ui(1).uiMax().next().sayX("看見普賽依的船了!!");
if (npc.askYesNo("普賽依果然出現了。準備好了嗎?")) {
    npc.startQuest();
    for (let i = 0; i < 45; i++) {
        map.spawnMob(9390815, -332 + i * 10, 198);
    }
    npc.id(9390217).ui(1).uiMax().npcFlip().next().noEsc().sayX("海盜們從普賽依的船湧來了!!");
    npc.ui(1).uiMax().next().noEsc().sayX("好的,痛快地幹一場吧!!");
} else {
    npc.ui(1).sayX("你在幹什麼呀。趕快準備啊。");
}

