/**
 *
 *
 */
npc.next().sayX("走雙刀之路所必備的東西就是敏銳的慧眼，去珠房打下渾濁的玻璃珠後，打碎它，就能獲得只向具有成為主人資格的人顯示的慧眼。");
let ret = npc.askYesNo("是否現在就進入珠房？");
if (ret == 1) {
    npc.startQuest();
    player.changeMap(910350000, 0);
} else {
    npc.next().sayX("那你好好準備下。");
}
