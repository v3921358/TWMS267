// 
// Quest ID:17636
// [凱梅爾茲共和國] 海盜王普賽依是誰


npc.ui(1).uiMax().next().sayX("我說萊文,普賽依是誰");
npc.next().sayX("嗯?你竟然不知道 #r海盜王普賽依#k?");
npc.ui(1).uiMax().next().sayX("哎,你不是忘記了我是從別的地方來的了吧。");
npc.id(9390203).ui(1).npcFlip().next().sayX("嗯。你來到這裡時間不長,不知道也是正常的。如果你有興趣的話要聽我說說嗎?");
npc.id(9390203).ui(1).npcFlip().next().sayX("凱梅爾茲不是以貿易為主而是以漁業為主的國家,這個我已經跟你說過了吧。");
npc.ui(1).uiMax().next().sayX("是的,那個已經聽說了。");
npc.id(9390203).ui(1).npcFlip().next().sayX("是的。從那時起至今凱梅爾茲沿岸一直受海盜騷擾。那些傢伙小群小群地來搶掠。");
npc.id(9390203).ui(1).npcFlip().next().sayX("可是,凱梅爾茲正式開始做貿易的同時為了減小受海盜的迫害有組織性的利用了這一點。");
npc.id(9390203).ui(1).npcFlip().next().sayX("開始的時候還是有效果的。但是,海盜們也許也覺得再那樣下去可不行,於是就以最厲害的海盜為中心團結起來,結果形成一個龐大的海盜團。");
npc.ui(1).uiMax().next().sayX("那最厲害的海盜就是那叫普賽依的傢伙嗎?");
npc.id(9390203).ui(1).npcFlip().next().sayX("就是那樣。結果回到了原點。");
npc.completeQuest();
player.gainExp(953667);