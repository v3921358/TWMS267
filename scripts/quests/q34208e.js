/*     
 *  
 *  功能：[啾啾島]尋找五色東山的美味&amp;lt;2&gt;
 *  

 */

npc.id(3003160).ui(1).npcFlip().next().sayX("#b#ho#！#k\r\n你已經集齊材料啦？麵包已經做好了…現在只要像這樣混合，然後像這樣放上去...");
npc.id(3003160).ui(1).npcFlip().sayX("好啦！現在#b都很奇怪啊！現在夠了！轉轉轉！三明治#k已經基本完成了！接下來試著製作肉…肉餅吧？請在我們#b嗶比#k的幫助下，獲得第一塊肉餅吧！");
if (player.isQuestStarted(34208)) {
    player.loseItem(4034944, 30);
    player.loseItem(4034945, 30);
    npc.completeQuest();
    player.gainExp(320000000);
}