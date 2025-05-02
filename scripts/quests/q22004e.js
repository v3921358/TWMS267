/**
 *
 *
 */
npc.next().sayX("哦，#t4032498#蒐集到了嗎？真了不起。我應該給你什麼作為獎賞呢……對了，我有那個東西。 \r\n\r\n#fUI/UIWindow.img/Quest/reward# \r\n#i3010097# #t3010097#1個 \r\n#i2022621# #t2022621# 15個\r\n#i2022622# #t2022622# 15個");
if (player.getFreeSlots(1) >= 1 && player.getFreeSlots(2) >= 2 && player.canGainItem(3010097, 1)) {
    if (player.hasItem(4032498, 3)) {
        npc.completeQuest();
        player.loseItem(4032498, 3);
        player.gainItem(3010097, 1);
        player.gainItem(2022621, 15);
        player.gainItem(2022622, 15);
        player.gainExp(210);
        npc.ui(1).sayX("好了，我用修理籬笆剩下的木板做了一把椅子。雖然不太好看，但卻很結實。就給你用吧。");
        let string = ["UI/tutorial/evan/7/0"];
        npc.sayImage(string);
    }
} else {
    npc.ui(1).sayX("先整理下揹包欄吧！");
}



