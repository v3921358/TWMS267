//
// Quest ID:17665
// [凱梅爾茲共和國] 忍耐性為零


npc.id(9390207).ui(1).uiMax().npcFlip().next().sayX("雖然我想讓你走，但你應該不會聽我的話……那就只能。");
npc.ui(1).uiMax().meFlip().next().sayX("你想幹嘛。");
npc.completeQuest();
player.gainExp(1058907);