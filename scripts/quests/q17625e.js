// 
// Quest ID:17625
// [凱梅爾茲共和國] 我來抵抗阿庫旁多


npc.ui(1).uiMax().next().sayX("統帥，我不是輕視凱梅爾茲的實力。只是，作為#b萊文的朋友#k，我想出點力而已。請你再考慮下吧。");
npc.next().sayX("嗯...既然你都這麼說了..好吧，那就這樣吧。看來是我想得太多了。請你現在就去追上萊文吧。他應該還沒跑多遠。希望你幫助那孩子一起擊退阿庫旁多吧。");
let itemId = 1052798 + player.getJobCategory();
npc.next().sayX("而且，這個也請你拿著吧。這能幫助你。\r\n\r\n#fUI/UIWindow2.img/QuestIcon/4/0#\r\n#i" + itemId + "# #t" + itemId + "#\r\n#fUI/UIWindow2.img/QuestIcon/8/0#\r\n530255 exp");
if (player.gainItem(itemId, 1)) {
    npc.completeQuest();
    player.gainExp(530255);
    npc.next().sayX("萊文應該在#b#m865020000##k。你快去找他吧。");
} else {
    npc.id(9390201).ui(1).uiMax().npcFlip().next().sayX("你的揹包滿了，清理下吧！");
}
