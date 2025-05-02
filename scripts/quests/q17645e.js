//
// Quest ID:17645
// [凱梅爾茲共和國] 與普賽依交戰


npc.next().sayX("嗚哦!豈有此理,這麼厲害的傢伙我真是很久沒遇到了!");
npc.ui(1).uiMax().next().sayX("彼此彼此。果然是和海盜小毛頭有些不一樣啊。");
npc.next().sayX("呼。但是還沒結束呢。");
npc.ui(1).uiMax().next().sayX("你打算搞什麼鬼。不管你做什麼現在對你來說都毫無勝算了。");
npc.id(9390235).ui(1).npcFlip().next().sayX("來吧,還是乖乖地束手就擒吧。");
npc.next().sayX("你知道我是怎麼坐上海盜王之位的嗎。");
npc.ui(1).uiMax().next().sayX("現在說那些有什麼用。");
npc.next().sayX("比其他海盜傭兵技術更好?射擊技術更高?都不是的。我能成為海盜王是因為...");
npc.next().sayX("我對情況判斷和我的腿腳比較快而已。");
npc.completeQuest();
player.gainExp(953667);