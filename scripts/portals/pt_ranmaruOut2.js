

if (map.getEventMobCount() == 0) {
        if (npc.askYesNo("要完成戰鬥離開嗎？？")) {
                player.changeMap(807300100, 0);
        }
} else {
        player.showSpouseMessage(6, `戰鬥進行中。想要退場的話請點擊傳送點。`)
}
