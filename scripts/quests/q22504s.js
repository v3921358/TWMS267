/**
 *
 *
 */
npc.next().sayX("肉有點老，不他適合我，還有沒有其他東西吃？");
let ret = npc.askYesNoS("我也不知道還有什麼能吃的，要不再去問問爸爸看看？");
if (ret == 1) {
    npc.startQuest();
} else {
    npc.next().sayX("你真的沒有好吃的東西嗎？");
}