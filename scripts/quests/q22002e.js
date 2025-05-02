/**
 *
 *
 */
npc.next().sayX("早飯吃了嗎，小不點？你能幫媽媽做件事嗎？\r\n\r\n#fUI/UIWindow.img/Quest/reward#\r\n#i1003028# #t1003028# 1個  \r\n#i2022621# #t2022621# 5個 \r\n#i2022622# #t2022622# 5個");
if (player.getFreeSlots(1) >= 1 && player.getFreeSlots(2) >= 2) {
    npc.completeQuest();
    player.gainExp(60);
    player.gainItem(1003028, 1);
    player.gainItem(2022621, 5);
    player.gainItem(2022622, 5);
    let string = ["UI/tutorial/evan/4/0"];
    npc.sayImage(string);
} else {
    npc.ui(1).sayX("先整理下揹包欄吧！");
}



