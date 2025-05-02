//
// Quest ID:17615
// [凱梅爾茲共和國] 貿易共和國凱梅爾茲


npc.ui(1).uiMax().next().sayX("打擾了。");
npc.next().sayX("你是?");
npc.ui(1).uiMax().next().sayX("你好。我想去達尼爾拉商團,該怎麼走呢?");
npc.next().sayX("你是誰?我就是達尼爾拉商團的人,我好像之前沒見過你。你找我們商團,是有什麼事嗎??如果你想去那工作的話,就跟我說吧。");
npc.ui(1).uiMax().next().sayX("我是來找#e#b吉爾伯特·達尼爾拉#k#n的。我有話要對他說。");
npc.next().sayX("你說什麼?你以為我們統帥是你的朋友嗎?他不是每個人想見就能見的。你還是走吧。");
npc.ui(1).uiMax().next().sayX("#b(該死... 想要見到地位高的人果然不容易。怎麼辦呢。)#k");
npc.completeQuest();
player.gainExp(530255);