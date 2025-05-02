//
// Quest ID:17635
// [凱梅爾茲共和國] 海上貿易的破壞者


npc.next().sayX("啊, #h0#你休息好了嗎?我感覺還是有點累。看你的臉色,好像你的疲勞也沒有完全消失呢。");
npc.ui(1).uiMax().next().sayX("嗯,我還好啦。也因為這地方比較陌生,還沒熟悉。");
npc.id(9390217).ui(1).npcFlip().next().sayX("統帥!大事不好了!");
npc.next().sayX("什麼大事。又出了什麼事?");
npc.ui(1).uiMax().next().sayX("(好像又出了什麼事情... 要聽聽嗎?)");
npc.id(9390203).ui(1).npcFlip().next().sayX("這次又是什麼事?");
npc.id(9390217).ui(1).npcFlip().next().sayX("據說,我們派去羅薩進行貿易的船隻,受到了普賽依的攻擊!!");
npc.id(9390203).ui(1).npcFlip().next().sayX("什麼?!該死的 #r普賽依#k又來搗亂!!");
npc.id(9390217).ui(1).npcFlip().next().sayX("這次的損害好像比之前都要大。");
npc.next().sayX("應該是的吧。因為那是去羅薩的商船。唉!");
npc.id(9390202).ui(1).npcFlip().next().sayX("這究竟是第幾次受害已經算不清了。難道不該想個什麼對策嗎。");
npc.ui(1).uiMax().next().sayX("(那個叫普賽依的傢伙又是誰。我得去問問。說不定又會有我能幫得上忙的事情。)");
npc.completeQuest();
player.gainExp(953667);
