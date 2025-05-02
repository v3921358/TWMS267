if (map.getId() == 350068600 || map.getId() == 350067600 || map.getId() == 350066600) {
	if (npc.askYesNo("已消滅史烏。要回去嗎？")) {
		player.changeMap(350060300);
	}
} else {
	if (npc.askYesNo("要在這裡退場嗎？")) {
		player.changeMap(350060300);
	}
}