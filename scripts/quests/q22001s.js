/**
 *
 *
 */
npc.next().sayX("一大早就來開玩笑，哈哈哈。別亂說了，快去給#p1013102#餵飯吧。");
npc.ui(1).uiMax().npcFlip().next().sayX("#b嗯？那不是#p1013101#的事情嗎？");
let ret = npc.askYesNo("你這傢伙！快去喂呀！！ #p1013102#有多討厭我，你也知道。哥哥我去的話，它一定會咬我的。獵犬喜歡你，你去給它送飯。");
if (ret == 1) {
    if (player.canGainItem(4032447, 1)) {
        player.gainItem(4032447, 1);
        npc.startQuest();
        npc.next().sayX("你快到#b左邊#k去給 #b#p1013102##k 喂飼料。那個傢伙好象肚子餓了，從剛才開始就一直在叫。");
        npc.next().sayX("給#p1013102#喂完食之後，趕快回來。");
    } else {
        npc.next().sayX("先整理下你的揹包其他欄吧。");
    }
} else {
    npc.next().sayX("你，不願意去嗎？你想看到哥哥我被狗咬嗎？快重新和我說話，接受任務！");
}
