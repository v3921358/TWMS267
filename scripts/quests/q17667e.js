//
// Quest ID:17667
// [凱梅爾茲共和國] 什麼關係


npc.ui(1).uiMax().next().sayX("我當然知道吉,吉爾伯特大人會拒絕和平條約的。");
npc.ui(1).uiMax().meFlip().next().sayX("原來是有計劃的行動啊。如果那樣的話,明知道會被拒絕還來提議的理由是什麼呢?");
npc.ui(1).uiMax().next().sayX("好的，如,如果拒絕了王國的和約後，憤怒的凱梅爾茲首領殺死了海本王國的使節這樣的訊息傳開了……");
npc.id(9390202).ui(1).uiMax().npcFlip().next().sayX("周圍的所有勢力都會孤立凱梅爾茲共和國的。");
npc.ui(1).uiMax().next().sayX("就,就是那樣。");
npc.ui(1).uiMax().meFlip().next().sayX("好的,你得把這事實在首領面前再完完整整地重複一遍。");
npc.ui(1).uiMax().next().sayX("當,當然了。");
npc.completeQuest();
player.gainExp(11058907);