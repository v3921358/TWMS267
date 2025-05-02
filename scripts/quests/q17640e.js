// 
// Quest ID:17640
// [凱梅爾茲共和國] 組織遠征隊（3）


npc.ui(1).uiMax().meFlip().next().sayX("好了,我也現在就去碼頭吧。");
npc.ui(1).uiMax().next().sayX("哎呀,又遇見了呢。");
npc.ui(1).uiMax().meFlip().next().sayX("呃啊!嚇了我一大跳!!幹嘛呀,在別人背後幹嘛呢!");
npc.ui(1).uiMax().next().sayX("就是路過的時候看見了而已。");
npc.ui(1).uiMax().meFlip().next().sayX("原來如此。那麼就請繼續路過吧。我很忙失陪..");
npc.ui(1).uiMax().next().sayX("你要去討伐海盜嗎?");
npc.ui(1).uiMax().meFlip().next().sayX("是的,是那樣的。怎麼了?你感興趣嗎?");
npc.ui(1).uiMax().next().sayX("哦呵呵,怎麼會呢。我怎麼會對那種低俗又野蠻的事情感興趣呢。那我就告辭了");
npc.ui(1).uiMax().meFlip().next().sayX(".....低俗?野蠻?這人真是...算了。趕緊去碼頭吧。");
npc.completeQuest();
player.completeQuest(17730, 0);//Quest Name:第1章.危險的人
player.gainExp(953667);
