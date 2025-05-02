/*     
 *
 *  功能：[啾啾島]循著美味而來
 *

 */
npc.id(3003160).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#嗯！今天做的料理好像也很贊！");
npc.id(3003153).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#那個還是等我嘗過後再判斷吧。");
npc.id(3003154).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#嗶比哥哥，怎麼一直吐槽猴姐啊？");
npc.id(3003153).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#你這小傢伙不要頂嘴，我是你哥哥！");
npc.id(3003154).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#我知道，“哥哥”你總是沒禮貌，所以#r#fs26#我有點生氣了！");
npc.id(3003155).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#嗶~~~米嗶嗶美姐…姐好可怕…我害怕...哼哼哼...");
npc.id(3003153).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#哎…哎，這麼開心的用餐時間，幹嘛要這樣…快點吃飯吧！");
npc.id(3003154).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#就是嘛，嗶比哥哥，你早點像這樣禮貌該多好啊？就算是為了我們辛苦做飯的猴姐，也請你以後一直~這麼懂禮貌，知道了嗎？");
npc.id(3003154).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#如若不然，#r#fs26#我就把...你..的...頭...#r#fs20#我就不跟你多說了，哥哥...");
npc.id(3003153).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#我…知道了！");
npc.id(3003155).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#呵呵呵，嗶美姐..姐姐又教訓..嗶比哥哥了…呵呵呵");
npc.id(3003154).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#我們嗶嘟~你要多吃點飯，讓你#b受傷的頭#k快點好起來..！");
npc.id(3003155).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#我…我…我本來就這樣啊…？呵呵呵");
npc.id(3003154).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#嗶嘟啊…你原來並不是傻瓜…不對，你原來是個#b正常的孩子#k…所以你要快點好起來....哎...");
npc.id(3003160).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face3#嗶比！嗶美！嗶嘟！你們這樣吵吵鬧鬧，同時又互相關心對方的樣子看起來很好！"); //3正常 0 微笑
npc.id(3003160).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#這裡大部分和我一樣，#b沒有家人#k，你們#b三個可以互相依靠#k，真是太幸福了！");
npc.id(3003154).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#猴姐，你怎麼會沒有家人呢~你收留了#b因為口味奇怪而被趕出村莊的#k我們啊~姐姐你就和我們家人沒有區別~");
npc.id(3003160).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#沒錯，嗶美！雖然我也一樣無家可歸！我們像現在這樣，永遠生活在一起吧！");
npc.id(3003153).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#切…什麼家人啊…長的完全不一樣啊...");
npc.id(3003154).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#喂…適可而止…吧？");
npc.id(3003153).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#我…我的意思是，所以我們更該心懷感激！猴姐的個頭比我們高大很多，要照顧我們這些小不點…我們的口味這麼奇特，她還要給我們做好吃的....");
npc.id(3003153).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#謝謝你...猴姐...");
npc.id(3003160).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#嘻嘻，我知道嗶比你非常喜歡我~嗶美啊，你也不要總這樣對待你哥哥~家人之間也是不能互相打罵的~");
npc.id(3003155).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face0#應該不是…互相…打罵吧…？呵呵呵...好像是#b單方#k…呵呵呵");
npc.id(3003160).ui(1).uiMax().npcFlip().next().noEsc().sayX("#face3#好啦~好啦~放著美味不吃，我們這是在幹嘛呢？今天我特意為你們準備了你們愛吃的#b勁道腳掌湯#k！快吃吧~");
npc.ui(1).uiMax().meFlip().next().noEsc().sayX("美食的味道是從這裡傳來的...\r\n正好到了飯點，趁機蹭點飯吃吧..");
if (player.isQuestStarted(34205)) {
    npc.completeQuest();
    player.gainExp(300000000);
}