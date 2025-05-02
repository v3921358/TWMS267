// [傳送門]  |  [9091002]
// MapName:暴君的王座

let msg = "要出去了嗎？";
if (map.getEventMobCount() == 0) {
        msg = "結束戰鬥，" + msg;
}
if (npc.askYesNo(msg)) {
        if (map.getId() == 940100200) {
                player.changeMap(940100100, 2);
        } else {
                player.changeMap(401060000, 1);
        }
}