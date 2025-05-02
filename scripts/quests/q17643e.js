// 
// Quest ID:17643
// [凱梅爾茲共和國] 海.盜.出.現


npc.id(9390217).ui(1).npcFlip().next().sayX("近處的海盜們逃跑了!!");
npc.next().sayX("不能讓他們跑掉!!!");
npc.id(9390204).ui(1).npcFlip().next().sayX("吼吼,該輪我上場了。我要用我的魔法把整條船 #r#e全部毀掉#k#n");
npc.ui(1).uiMax().next().sayX("唔哇!!等等等等等等等一下!!");
npc.id(9390204).ui(1).npcFlip().next().sayX("幹嘛攔著我?");
npc.ui(1).uiMax().next().sayX("那個叫普賽依還是叫什麼的傢伙還沒有出現呢。那些傢伙們肯定是往普賽依所在的根據地方向逃跑了,我們就這麼跟過去不是更好嗎?");
npc.next().sayX("對的,果然是人才啊!趕緊跟上去!!");
npc.id(9390204).ui(1).npcFlip().next().sayX("呃嗯..真可惜啊。");
npc.completeQuest();
player.gainExp(953667);
