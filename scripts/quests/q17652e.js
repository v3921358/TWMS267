// 
// Quest ID:17652
// [凱梅爾茲共和國] 送信


npc.ui(1).uiMax().next().sayX("誰呀。");
npc.ui(1).uiMax().meFlip().next().sayX("請問是班·特來敏嗎?");
npc.ui(1).uiMax().next().sayX("我是,可是你是哪位?");
npc.ui(1).uiMax().meFlip().next().sayX("我是跑腿來給你送吉爾伯特首領的書信的。");
npc.ui(1).uiMax().next().sayX("吉爾伯特?是什麼書信?快給我看看。");
npc.completeQuest();
player.gainExp(953667);