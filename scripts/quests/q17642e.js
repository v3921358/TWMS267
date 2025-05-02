// 
// Quest ID:17642
// [凱梅爾茲共和國] 改變主意的男人


npc.ui(1).uiMax().meFlip().next().sayX("啊..對不起。我太輕率了。我應該先和你商量一下的。");
npc.ui(1).uiMax().next().sayX("那樣才對。雖然我們是朋友,但是我現在的身份是這艘船的船長啊。任何小事都應該先向我報告...");
npc.id(9390204).ui(1).uiMax().npcFlip().next().sayX("哎呀,這船怎麼造的這麼沒品味啊?是為了控制在便宜的價格範圍內嗎。");
npc.ui(1).uiMax().next().sayX("!!!!!!!!!!!!!!!!!!");
npc.ui(1).uiMax().meFlip().next().sayX("聽你一說我覺得萊文你說得對。雖然是朋友關係也該公私分明。我好好跟她說讓她下船,你就別操心了。");
npc.ui(1).uiMax().meFlip().next().sayX("喂。我有話跟你說。");
npc.id(9390204).ui(1).uiMax().npcFlip().next().sayX("嗯?什麼事?你應該不會是要我做這低賤的船務吧?");
npc.ui(1).uiMax().meFlip().next().sayX("不,那倒不是的。其實是想對你說抱歉..");
npc.ui(1).uiMax().next().sayX("雖然是抱歉的話,但是今後也許會發生一些危險的事情。關於這點希望你能理解。");
npc.id(9390204).ui(1).uiMax().npcFlip().next().sayX("我以為你要說什麼呢。那個我早就想到了。我要是被抓了一定不會麻煩你來救我的,你就別擔心我了,擔心擔心那邊吧。如果沒別的事我就失陪了。");
npc.ui(1).uiMax().meFlip().next().sayX("萊文怎麼說的？你讓她下船也行。和我也沒什麼深交，不過是比你多見過一次罷了。");
npc.ui(1).uiMax().next().sayX("喔呵呵。船已經離港,想下船都難嘍。讓女孩一個人回去也不安全。反正不管怎樣你知道那麼回事就行了。");
npc.ui(1).uiMax().meFlip().next().sayX("(不管怎麼說,好像瞭解了又貌似完全不瞭解的傢伙。變來變去的。)");
npc.completeQuest();
player.gainExp(953667);