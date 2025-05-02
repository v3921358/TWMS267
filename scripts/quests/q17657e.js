//
// Quest ID:17657
// [凱梅爾茲共和國] 她的身份


npc.ui(1).uiMax().next().sayX("好了,準備好了嗎。");
npc.id(9390202).ui(1).uiMax().npcFlip().next().sayX("噢噢....");
npc.ui(1).uiMax().meFlip().next().sayX("萊文你怎麼了?已經見到你真面目了,現在就介紹一下你的來歷吧。");
npc.ui(1).uiMax().next().sayX(" ......我這樣做了你都還認不出我嗎?所以說男人啊..");
npc.ui(1).uiMax().next().sayX("我的名字是 #b克萊爾·特來敏#k。我是特來敏商團的獨生女。");
npc.ui(1).uiMax().meFlip().next().sayX("你說特來敏?你是說讓我受冤屈的那個特來敏嗎?!");
npc.ui(1).uiMax().next().sayX("是的。班·特來敏。那是我爸爸。");
npc.ui(1).uiMax().meFlip().next().sayX("這又是葫蘆裡賣什麼藥來著?");
npc.ui(1).uiMax().next().sayX("葫蘆裡賣什麼藥,你這說法真低俗。");
npc.ui(1).uiMax().meFlip().next().sayX("你說什麼?");
npc.id(9390202).ui(1).uiMax().npcFlip().next().sayX("等會,#h0#!先聽聽特來敏小姐怎麼說。究竟是怎麼一回事。她也許也有什麼隱情呢。");
npc.ui(1).uiMax().meFlip().next().sayX("好的,那就聽聽吧。");
npc.completeQuest();
player.gainExp(1058907);