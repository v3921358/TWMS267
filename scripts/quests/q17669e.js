//
// Quest ID:17669
// [凱梅爾茲共和國] 發現結界（2）


npc.ui(1).uiMax().next().sayX("是這裡嗎?這是...這好像是跟我原來見過的阿庫旁多的結界是同種類的結界。");
npc.id(9390202).ui(1).npcFlip().next().sayX("那樣的話,從這進去就是另一個地方了吧。");
npc.completeQuest();
player.gainExp(1058907);