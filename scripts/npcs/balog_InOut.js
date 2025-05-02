// [武英]  |  [1061018]
// MapName:巴洛古的墳墓


if (npc.askYesNo("您要放棄嗎？")) {
        player.changeMap(105100100, 0);
} else {
        npc.next().sayX("請你再努力。");
}