// 
// Quest ID:17660
// [凱梅爾茲共和國] ]消失的克萊爾


npc.id(9390227).ui(1).npcFlip().next().sayX("嗚。不是省油的燈啊。撤退!");
npc.next().sayX("等著瞧!");
npc.ui(1).uiMax().next().sayX("萊文別追了!又想上那些傢伙們的當嗎?");
npc.next().sayX("咳呃..");
npc.completeQuest();
player.completeQuest(17741, 0);//Quest Name:第2章.奇怪女人的身份
player.gainExp(1058907);