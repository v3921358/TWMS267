//
// Quest ID:17630
// [凱梅爾茲共和國] 擊退阿庫旁多（5）


npc.ui(1).uiMax().next().sayX("呼.. 真是沒完沒了。它們的數量太多了。看來,我們真的是落入了陷阱。");
npc.completeQuest();
player.completeQuest(17720, 0);//Quest Name:第1章.運河之戰
player.gainExp(10793937);
player.changeMap(865020061, 0);