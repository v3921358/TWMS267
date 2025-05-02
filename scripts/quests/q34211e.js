/*     
 *  
 *  功能：[啾啾島]尋找艾爾谷的美味&amp;lt;1&gt;
 *  

 */

npc.id(3003163).ui(1).npcFlip().next().sayX("天啊~你回來啦？哎呀，你全身都溼了~快把你蒐集到的東西給我，你先休息下~");
npc.id(3003163).ui(1).npcFlip().next().sayX("嗯~這新鮮的香味！#b都很奇怪啊！現在夠了！轉轉轉！三明治#k肯定會成為最棒的料理~");
npc.id(3003163).ui(1).npcFlip().next().sayX("嗯！你應該知道還沒有結束吧？我會告訴你接下來要蒐集的材料，請你立刻出發~（嫣：這隻死雞...）");
npc.ui(1).uiMax().npcFlip().next().sayX("我休息會再去不行嗎...");
npc.id(3003163).ui(1).npcFlip().next().sayX("嗯~你想休息就休息吧~ 要不要我幫你#r永遠的休息下去#k？哈哈哈！（這隻死雞真的腹黑。）");
if (player.isQuestStarted(34211)) {
    player.loseItem(4034950, 20);
    player.loseItem(4034951, 20);
    npc.completeQuest();
    player.gainExp(310000000);
} 