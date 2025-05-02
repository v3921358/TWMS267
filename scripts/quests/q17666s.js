// 
// Quest ID:17666
// [凱梅爾茲共和國] 黑影殺陣


npc.ui(1).uiMax().next().sayX("也沒什麼。喂,暗殺團團長");
npc.id(9390218).ui(1).uiMax().npcFlip().next().sayX("......");
npc.ui(1).uiMax().next().sayX("該到顯示你的實力的時候了。");
npc.id(9390218).ui(1).uiMax().npcFlip().next().sayX("......好的。");
npc.ui(1).uiMax().meFlip().next().sayX("什麼?什麼呀。");
npc.id(9390218).ui(1).uiMax().npcFlip().next().sayX("......這是不可能活著逃出來的。");
npc.startQuest();
npc.makeEvent("berry_quest", [player, [865030121]]);