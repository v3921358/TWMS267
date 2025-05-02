portal.abortWarp();
if (map.getEventMobCount() <= 0) {
	player.changeMap(map.getId() + 1000, 0);
} else {
	player.showSystemMessage("地圖上還剩餘：" + map.getEventMobCount() + "怪物，請清理之後進入下一關卡.");
}

