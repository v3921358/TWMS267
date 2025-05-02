/*     
 *  
 *  功能：[啾啾島]尋找呲溜森林的美味&amp;lt;1&gt;
 *  

 */

npc.id(3003162).ui(1).npcFlip().next().sayX("你這傢伙真是迅速！這麼快就集齊材料啦？！");
npc.id(3003162).ui(1).npcFlip().next().sayX("不過，我的口味非常非常講究，就這幾種肉食滿足不了我的！");
npc.id(3003162).ui(1).npcFlip().next().sayX("好了！為了#b都很奇怪啊！現在夠了！轉轉轉！三明治#k的完成，以及#b填飽我的肚子#k，去蒐集更多的肉吧！");
npc.ui(1).uiMax().npcFlip().next().sayX("你怎麼總打算製作#b你要吃#k的食物…你不要忘了，我是在為#b穆託#k準備食物！");
npc.id(3003162).ui(1).npcFlip().sayX("都一樣的啦！！別廢話了，快去蒐集肉吧！");
if (player.isQuestStarted(34209)) {
    player.loseItem(4034946, 20);
    player.loseItem(4034947, 20);
    npc.completeQuest();
    player.gainExp(310000000);
}            	