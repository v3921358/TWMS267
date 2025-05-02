
if (map.getEventMobCount() == 0) {
    if (npc.id(2184001).askYesNo("要結束挑戰退出嗎？")) {
        let msg = map.getEventMobCount() == 0 ? "確定要結束挑戰直接離場？" : "希拉還沒被擊退，要放棄離開嗎？";
        if (npc.id(2184001).askYesNo(msg)) {
            player.changeMap(262030000, 2);
        }
    }
} else {
    player.showSpouseMessage(6, `戰鬥進行中。想要退場的話請點擊傳送點。`)
}