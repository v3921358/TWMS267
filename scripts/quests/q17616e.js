//
// Quest ID:17616
// [凱梅爾茲共和國] 失竊的貿易品


npc.next().sayX("歡迎光臨。我能幫你什麼嗎?");
npc.ui(1).uiMax().next().sayX("聽說達尼爾拉商團的交易品丟了,我能瞭解下詳細情況嗎?");
npc.next().sayX("你是說達尼爾拉商團的交易品啊。幾天前,達尼爾拉商團的人在這寄放了大量的交易品,不久前,他又來拿走了。");
npc.ui(1).uiMax().next().sayX("你知道是誰來拿走的嗎?");
npc.next().sayX("嗯,和把東西寄放過來的是同一個人。");
npc.ui(1).uiMax().next().sayX("嗯.. 真是奇怪。是怎麼回事呢。");
npc.completeQuest();
player.gainExp(530255);
