// 
// Quest ID:17676
// [凱梅爾茲共和國] 她的心意


npc.ui(1).uiMax().next().sayX("喲,你來啦。書信轉交給她了嗎?");
npc.ui(1).uiMax().meFlip().next().sayX("嗯。我親手轉交的。你放心。");
npc.ui(1).uiMax().next().sayX("好吧,有什麼反應呢?高興嗎?");
npc.ui(1).uiMax().meFlip().next().sayX("哦..嗯…那..當然啦..(萊文你真是的...) 啊,還有讓我把這個交給你。");
npc.ui(1).uiMax().next().sayX("這是什麼?");
npc.ui(1).uiMax().meFlip().next().sayX("那我就不知道嘍。你拆開來看看啊。");
npc.ui(1).uiMax().next().sayX("哪呢....啊,這是..?這是什麼呢?");
npc.ui(1).uiMax().meFlip().next().sayX("好像是什麼符似的。");
npc.ui(1).uiMax().next().sayX("噢,看來是保佑平安的符咒!我要隨身攜帶!");
npc.ui(1).uiMax().meFlip().next().sayX("好了,真是萬幸。(就沒必要告訴他是試驗品而已了吧。希望那可千萬別是詛咒,而得是祝福的符文。)");
npc.completeQuest();
player.gainExp(1058907);