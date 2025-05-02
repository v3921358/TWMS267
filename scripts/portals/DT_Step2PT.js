

let mapId = map.getId();
switch (mapId){
	case 706050200:
		player.changeMap(706050300,0);
		portal.showHint("沖鴨！！！ （請一直連按跳躍鍵）", 250, 5);
		break;
}
		
portal.abortWarp();

