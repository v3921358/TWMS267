// 
// Quest ID:17671
// [凱梅爾茲共和國] 返回桑凱梅爾茲


npc.next().sayX("回來啦。如何?結界裡面有什麼嗎?");
npc.ui(1).uiMax().next().sayX("我沒有往深處去所以也不太清楚。我只到了結界的入口就感到了非同一般的殺氣就沒再往裡去了。");
if (npc.askYesNo("做得好。再往深處去如果發生什麼事的話就麻煩了。好了,那我們回桑凱梅爾茲吧?")) {
    npc.startQuest();
    npc.ui(1).uiMax().next().sayX("好吧,趕緊回去吧。克萊爾也累了。");
    npc.id(9390205).ui(1).npcFlip().next().sayX("我呢...就到入口就行了。因為我可不能碰見我父親...");
    npc.next().sayX("啊,那就那樣吧。好了,走吧");
    player.changeMap(865000000, 10);
} else {
    npc.ui(1).sayX("嗯?你不是說回去嗎。你還有什麼事要辦嗎。");
}