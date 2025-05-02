/**
 *
 *
 */
npc.next().sayX("在這個洞穴深處有一條邪惡的#e#r暗黑龍#k#n. 要想前往那裡, 必須帶著#e#b#t4001086##k#n從祕密通道透過才行. ");
npc.next().sayX("勇敢的人啊, 現在的你應該比我更需要這件東西. 我要將\r\n\r\n#i4001086##e#b#t4001086##k#n給你. ");
npc.next().sayX("只有擁有此物的人才能夠在#e#b敢死隊的暗號石板#k#n中發現祕密通道. 如果不小心將證物丟失, 就請再來和我對話吧. ");
if (player.canGainItem(4001086, 1)) {
    player.gainItem(4001086, 1);
    npc.startQuest();
    npc.completeQuest();
} else {
    npc.next().sayX("啊，請清理下你的揹包其他欄，沒有空間是不能獲取象徵的！");
}
