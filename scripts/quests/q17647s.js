// 
// Quest ID:17647
// [凱梅爾茲共和國] 被擊斃的普賽依


npc.next().sayX("話說回來,那海盜該怎麼辦呢?");
npc.ui(1).uiMax().next().sayX("海盜?啊..普賽依!!");
npc.id(9390235).ui(1).npcFlip().next().sayX("普賽依那傢伙在哪?因為深海巨妖的出現我都沒心思顧及他了。");
npc.next().sayX("像老鼠一樣悄悄地逃跑了,往那邊去了。");
npc.id(9390235).ui(1).npcFlip().next().sayX("怎麼搞的,什麼時候跑那麼遠去了!!趕緊去追吧!!");
npc.ui(1).uiMax().next().sayX("那是不太可能。用商船追海盜船那是不可能的。");
npc.id(9390235).ui(1).npcFlip().next().sayX("就這樣失之交臂了嗎..幾乎就要抓到他了。切。");
if (npc.askYesNo("好像該輪到我登場的時候了吧。怎麼樣..要我幫忙嗎?")) {
    npc.startQuest();
    npc.ui(1).uiMax().meFlip().next().sayX("你打算怎麼辦呢?你不會是打算游泳追過去吧");
    npc.ui(1).uiMax().next().sayX("怎麼會有這麼低俗的人啊。");
    npc.ui(1).uiMax().meFlip().next().sayX("什麼,你說什麼..(這女人做什麼都說是低俗...)");
    npc.ui(1).uiMax().next().sayX("怎麼辦呢?要不要我出馬。當然你得給我適當的報酬。");
    npc.id(9390235).ui(1).uiMax().npcFlip().next().sayX("呃嗯...好吧,就拜託你了。");
} else {
    npc.ui(1).sayX("真的不要嗎?你不會後悔吧。");
}