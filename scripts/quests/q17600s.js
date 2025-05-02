//
// Quest ID:17600
// [凱梅爾茲共和國] 南哈特的呼喚

if (npc.askAcceptX("你這段時間過得還好嗎？女皇正在找你。你能來聖地嗎？\r\n#b(接受時立刻前往聖地。)#k")) {
    // Response is Yes
    npc.next().sayX("那我就在聖地等著你。");
    npc.startQuest();
    player.changeMap(130000000);
} else {
    npc.next().sayX("現在沒有時間嗎？");
}
