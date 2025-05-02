//
// Quest ID:17677
// [凱梅爾茲共和國] 重返明珠港


if (npc.askYesNo("已經做好了遠行的準備了嗎。行李都帶好了吧?那就出發吧?")) {
    npc.next().sayX("出發吧！目的地是明珠港！");
    npc.startQuest();
    player.changeMap(104000000, 0);
} else {
    npc.ui(1).sayX("好的,還有時間再準備準備。好好檢查一下,可有遺漏的東西");
}