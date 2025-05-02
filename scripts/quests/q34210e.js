/*     
 *  
 *  功能：[啾啾島]尋找呲溜森林的美味&amp;lt;2&gt;
 *  

 */

npc.id(3003162).ui(1).npcFlip().next().sayX("什麼啊？！我還以為你被吃了呢，沒想到活著回來了？！");
npc.id(3003162).ui(1).npcFlip().next().sayX("好啦！快看看我做的肉餅！#b都很奇怪啊！現在夠了！轉轉轉！三明治#k現在可以說是已經完成了！");
npc.id(3003162).ui(1).npcFlip().next().sayX("嗯嗯！那我先嚐一口吧？！");
npc.id(3003163).ui(1).npcFlip().next().sayX("哥哥，如果你現在不拿開你的嘴，我會讓你以後#b很難吃飯#k的哦？哈哈");
npc.id(3003162).ui(1).npcFlip().sayX("該死，現在我的作用已經結束了，現在你去找我的妹妹#b嗶美#k吧！");
if (player.isQuestStarted(34210)) {
    player.loseItem(4034948, 30);
    player.loseItem(4034949, 30);
    npc.completeQuest();
    player.gainExp(310000000);
}