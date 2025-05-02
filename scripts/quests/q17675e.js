//
// Quest ID:17675
// [凱梅爾茲共和國] 他的委託


npc.ui(1).uiMax().meFlip().next().sayX("克萊爾,請稍等。");
npc.ui(1).uiMax().next().sayX("嗯? 啊…那...");
npc.ui(1).uiMax().meFlip().next().sayX("是#h0#。如此看來我還沒介紹過我的名字呢。");
npc.ui(1).uiMax().next().sayX("啊,是的,#h0#。有什麼事嗎?找我有事嗎?");
npc.ui(1).uiMax().meFlip().next().sayX("啊,這是萊文讓我轉交給你的。");
npc.ui(1).uiMax().next().sayX("啊,好的。謝謝你了。辛苦你了。");
npc.completeQuest();
player.gainExp(1058907);