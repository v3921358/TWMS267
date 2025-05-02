/**
 *
 *
 */
let ret = npc.askYesNo("去農場幹活的時候，#b爸爸#k忘了把便當帶過去了。你能去 #b#m100030300##k 給爸爸送#b便當#k嗎？");
if (ret == 1) {
    if (player.canGainItem(4032448, 1)) {
        player.gainItem(4032448, 1);
        npc.startQuest();
        npc.next().sayX("呵呵，小不點果然是個好孩子~#b從家裡出去後，往左邊走。#k爸爸一定餓極了，你最好快點給他送過去。");
        npc.next().sayX("如果不小心把便當弄丟了，就馬上回來。我再給你包一份。");
        let string = ["UI/tutorial/evan/5/0"];
        npc.sayImage(string);
    } else {
        npc.next().sayX("先整理下你的揹包其他欄吧。");
    }
} else {
    npc.next().sayX("不要覺得麻煩就不願意去。你是個好孩子，對吧？再來和我說話吧。");
}
