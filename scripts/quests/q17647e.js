// 
// Quest ID:17647
// [凱梅爾茲共和國] 被擊斃的普賽依


npc.ui(1).uiMax().next().sayX("你到時候可不能反悔哦。好吧,那就退後吧。不想一起被捲走的話。");
player.setInGameCurNodeEventEnd(true);
player.setInGameDirectionMode(true, true, false, false);
player.setForcedInput(0);
player.setStandAloneMode(true);
player.showScreenAutoLetterBox("Map/EffectBT.img/dawnveil1/Cut3_1", 0);
npc.setDelay(2000);
player.showScreenAutoLetterBox("Map/EffectBT.img/dawnveil1/Cut3_2", 0);
player.soundEffect("Sound/SoundEff.img/thunder3");
npc.setDelay(2000);
player.setInGameDirectionMode(false, true, false, false);
player.setStandAloneMode(false);
npc.ui(1).uiMax().meFlip().next().noEsc().sayX("..........");
npc.id(9390235).ui(1).uiMax().npcFlip().next().noEsc().sayX("..........");
npc.ui(1).uiMax().meFlip().next().noEsc().sayX("還活著嗎...");
npc.id(9390235).ui(1).uiMax().npcFlip().next().noEsc().sayX("可,可能...恐怕不行了吧。");
npc.ui(1).uiMax().meFlip().next().noEsc().sayX("不知怎麼的有些同情他。");
npc.ui(1).uiMax().next().noEsc().sayX("你,報酬可不能少了我的啊。");
npc.completeQuest();
player.gainExp(953667);
