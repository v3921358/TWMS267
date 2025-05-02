let mapId = map.getId();
switch (mapId){
	case 940200300:
		player.changeMap(940200300);
		map.clearMobs();
		map.resetMobsSpawns();
		break;
	case 940200310:
		player.changeMap(940200310);
		map.clearMobs();
		map.resetMobsSpawns();
		break;
	case 940200320:
		player.changeMap(940200320);
		map.clearMobs();
		map.resetMobsSpawns();
		break;
}
portal.abortWarp();