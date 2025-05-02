/**
 *
 *
 */

npc.next().sayX("你好，#b#e#h0##n#k！\r\n我想為來自#e#b冒險島世界#k#n的勇士提供一些幫助！");
npc.next().sayX("你知道#i2030059##e#b[回城卷軸]#k#n嗎？");
npc.next().sayX("#e#b[回城卷軸]#k#n\r\n是可以從你當前所在位置移動到#r#e最近的村莊#k#n的道具。\r\n在狩獵的過程中想要回到村莊時使用，可以節約走路的時間，因此是非常有用的道具。");
npc.next().sayX("#e#r使用之後會立即移動#k#n到村莊，並消耗#e#r一個#k#n回城卷軸。\r\n此外，在部分#e#r無法傳送的地區#k#n無法使用回城卷軸。希望你能記住這些。");
npc.next().sayX("最後，#b[回城卷軸]#k#n可以在附近村莊的#b#e雜貨商人#n#k那裡購買。");
let ret = npc.askYesNo("現在我可以送你一些#b[回城卷軸]#k。\r\n你想現在領取#i2030059##e#b10個[回城卷軸]#k#n嗎？");
if (ret == 1) {
    if (player.canGainItem(2030059, 10)) {
        player.gainItem(2030059, 10);
        npc.completeQuest();
        npc.next().sayX("#i2030059##e#b10個[回城卷軸]#k#n已經送給你了");
        npc.next().sayX("#e#b#h0##k#n，希望在冒險的時候能對你有用！");
    } else {
        npc.next().sayX("想領取#i2030059##e#b[回城卷軸]#k#n的話，請現在整理下揹包吧。");
    }

} else {
    npc.next().sayX("想領取#i2030059##e#b[回城卷軸]#k#n的話，請點選頭頂上的燈泡。");
}
