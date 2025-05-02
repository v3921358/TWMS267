// 
// Quest ID:17656
// [凱梅爾茲共和國] 與狼共舞（1）


npc.ui(1).uiMax().next().sayX("所幸好像已經全部擊退了...");
npc.id(9390202).ui(1).npcFlip().next().sayX("你沒事吧?沒受傷吧?");
npc.next().sayX("啊..哦..我沒事。受傷了..哎呀,手受傷了。");
npc.id(9390202).ui(1).npcFlip().next().sayX("沒關係。沒什麼大不了的。剛才被咬了一下。哈哈..");
npc.next().sayX("你還說沒關係。即使是小傷口放任不管的話也會變嚴重的。");
npc.ui(1).uiMax().next().sayX("你們...幹嘛呢。");
npc.id(9390202).ui(1).npcFlip().next().sayX("哦?喔呵呵。所以呢最終揭曉你的身份的話..");
npc.completeQuest();
player.gainExp(1058907);