// 
// Quest ID:17653
// [凱梅爾茲共和國] 別來拜託我


npc.next().sayX("來啦。見到班·特來敏了嗎?");
npc.ui(1).uiMax().next().sayX("是的,見到了。可是...");
npc.next().sayX("可是?");
npc.ui(1).uiMax().next().sayX("那個...雖然我不知道書信的內容,但是其中的議事被斷然地拒絕了。說全部都是首領的責任讓您自己看著辦...");
npc.next().sayX("什麼?那是我為了自己好才那樣做的嗎!!");
npc.ui(1).uiMax().next().sayX("說商團的事務繁忙所以沒法幫忙。");
npc.next().sayX("他的意思是商團的事情比國家的事情更重要嗎?呵呵,沒看出來啊,真是可恥的人啊!其實那人一直盯著我的位子呢。果然拒絕幫忙。");
npc.next().sayX("反正我知道了。辛苦你了。趕緊去休息吧。");
npc.completeQuest();
player.completeQuest(17740, 0);//Quest Name:第1章.不速之客
player.gainExp(953667);