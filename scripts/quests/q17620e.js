//
// Quest ID:17620
// [凱梅爾茲共和國] 禮尚往來


npc.ui(1).uiMax().next().sayX("那個,你是吉爾伯特·達尼爾拉嗎?");
npc.next().sayX("是的。但我好像沒見過你,你是誰?");
npc.ui(1).uiMax().next().sayX("你好。吉爾伯特,我是...");
npc.next().sayX("嗯。如果你沒有提前預約的話,我沒時間接待你。你還是回去吧。");
npc.completeQuest();
// Unhandled Stat Changed [CS_EXP] Packet: 00 00 00 00 01 00 00 00 00 00 91 6C 19 03 00 00 00 00 FF 00 00 00 00 
player.gainExp(530255);