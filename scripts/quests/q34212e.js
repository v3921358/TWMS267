/*     
 *  
 *  功能：[啾啾島]尋找艾爾谷的美味&amp;lt;2&gt;
 *  

 */

npc.id(3003163).ui(1).npcFlip().next().sayX("噢！你躲過了#b犀牛龜#k犄角的攻擊啊？哈哈~");
npc.id(3003163).ui(1).npcFlip().next().sayX("好了~現在海鮮餅已經完成了~你快看，看起來是不是很美味啊？");
npc.id(3003163).ui(1).npcFlip().next().sayX("穆託肯定會非常~非常喜歡#b三明治#k的，哈哈哈`");
npc.id(3003164).ui(1).npcFlip().next().sayX("我..... 我也…想…幫忙…姐…姐姐…呵呵呵");
npc.id(3003163).ui(1).npcFlip().next().sayX("天啊~我們善良的嗶嘟！好的，好的，你也快點幫助旅行者吧~");
npc.ui(1).uiMax().npcFlip().next().sayX("怎麼覺得要加的東西越來多了....");
if (player.isQuestStarted(34212)) {
    player.loseItem(4034952, 30);
    player.loseItem(4034953, 30);
    npc.completeQuest();
    player.gainExp(310000000);
}