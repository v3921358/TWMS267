/*     
 *  
 *  功能：[啾啾島]尋找天宇鯨山之味&amp;lt;1&gt;
 *  

 */

npc.id(3003164).ui(1).npcFlip().next().sayX("嗚…哇... 你這麼快就把羽毛拿回來啦。");
npc.ui(1).uiMax().npcFlip().next().sayX("好了~ 嗶嘟，現在可以了吧？快點完成你的蛋餅吧~");
npc.id(3003164).ui(1).npcFlip().next().sayX("哼，你怎麼會任務我的任務只有一項呢？在你眼裡我就那麼傻呀？！");
npc.ui(1).uiMax().npcFlip().next().sayX("噢！嗶嘟你！");
npc.id(3003164).ui(1).npcFlip().next().sayX("啊，呵呵呵，我不管…我還需要更多~ 哼，再去蒐集吧…哼");
if (player.isQuestStarted(34213)) {
    player.loseItem(4034954, 20);
    player.loseItem(4034955, 20);
    npc.completeQuest();
    player.gainExp(310000000);
}