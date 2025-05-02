//
// Quest ID:17666
// [凱梅爾茲共和國] 黑影殺陣


npc.id(9390218).ui(1).uiMax().npcFlip().next().noEsc().sayX("...我的黑影殺陣...居然被擊破了。");
npc.ui(1).uiMax().next().noEsc().sayX("非同一般的殺陣。擊破它是得花些力氣。");
npc.ui(1).uiMax().next().noEsc().sayX("真可惜那傢伙好像逃跑了。");
npc.ui(1).uiMax().next().noEsc().sayX("好了,那麼叫西溫什麼的那位。現在該聽聽你說的了吧。");
npc.completeQuest();
player.gainExp(1058907);
player.changeMap(865030111, 0);