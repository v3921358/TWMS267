/**
 *
 *
 */
npc.next().sayX("主人，乾草真難吃，有沒有其他東西吃啊？");
let ret = npc.askYesNoS("龍說不定只吃肉食，那我們去找一些肉，你試試看？");
if (ret == 1) {
    npc.startQuest();
} else {
    npc.next().sayX("你真的沒有好吃的東西嗎？");
}