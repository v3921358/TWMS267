if (map.getId() == 350068600 || map.getId() == 350067600 || map.getId() == 350066600) {
	if (npc.ui(1).npcFlip().meFlip().askYesNoX("已消滅史烏。要回去嗎？")) {
		/* Response is Yes */
		portal.playPortalSE();
		player.changeMap(350060300);
	}
} else {
	player.showSystemMessage("戰鬥進行中，想要退出的話請點擊傳送點。");
}
