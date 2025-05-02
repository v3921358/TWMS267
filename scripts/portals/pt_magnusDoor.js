if (map.getEventMobCount() > 0) {
    player.showSpouseMessage(6, `戰鬥進行中。想要退場的話請點擊傳送點。`)
} else {
    if (npc.askYesNo("結束戰鬥，要出去了嗎？")) {
            if (map.getId() == 940100200) {
                    player.changeMap(940100100, 2);
            } else {
                    player.changeMap(401060000, 1);
            }
    }
}