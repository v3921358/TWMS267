// 
// Quest ID:17619
// [凱梅爾茲共和國] 不許逃


npc.next().sayX("幽靈.. 我的交易品.. 幽靈.. 我的交易品.. ");
npc.ui(1).uiMax().next().sayX("啊,原來你在這裡啊。 ");
npc.next().sayX("幽靈.. 我的交易品.. 幽靈..");
npc.ui(1).uiMax().next().sayX(" ... 他的魂好像丟了一半...");
npc.ui(1).uiMax().next().sayX("喂。我把丟失的貿易品重新找回來了。");
npc.next().sayX("幽... 什麼!?找回來了!?在哪呢... 哦!!是真的呢!!這是怎麼回事?你在哪找到的?");
npc.ui(1).uiMax().next().sayX("那個,就是.. 我抓到了幽靈。");
npc.next().sayX("是真的嗎?我之前還沒發現,你真是了不起的冒險家啊!!");
let OBJECT_1 = player.spawnTempNpc(9010000, 2369, 526, 1);
let OBJECT_2 = player.spawnTempNpc(9010010, 2911, 526, 1);
let OBJECT_3 = player.spawnTempNpc(9390251, 4126, 526, 1);
let OBJECT_4 = player.spawnTempNpc(9390234, 5489, 526, 1);
let OBJECT_5 = player.spawnTempNpc(9000086, 1122, 104, 1);
npc.ui(1).uiMax().next().sayX("(你之前是怎麼看我的...)");
npc.next().sayX("十分感謝!!你是我的救命恩人。你有什麼願望就告訴我吧!只要我能做到,我一定會幫你的!");
npc.ui(1).uiMax().next().sayX("是真的嗎?");
npc.next().sayX("當然啦!這是海上男子漢的約定。也是以信任為本的達尼爾拉商團的商人的約定!");
npc.completeQuest();
player.gainExp(530255);