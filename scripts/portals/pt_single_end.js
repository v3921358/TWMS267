let mapId = map.getId();
switch (mapId){
	case 911001300:
		if(player.hasItem(4001007,1)){
			player.changeMap(911001100,0);
			player.loseItem(4001007);
			portal.showHint("謝謝大蘑菇的通關憑證，我一定會加油的！", 250, 5);
		}else{
			portal.showHint("請到制高點找到大蘑菇獲取通關憑證", 250, 5);
		}
		
		
		break;
	case 911001100:
		player.changeMap(911001200,0);
		portal.showHint("wow, 我一定會拿到第一的！", 250, 5);
		break;
	case 911001000:
		player.changeMap(911001200,0);
		portal.showHint("wow, 我一定會拿到第一的！", 250, 5);
		break;
	case 911001200:
		player.changeMap(911001400,0);
		portal.showHint("wow, 我一定會拿到第一的！", 250, 5);
		break;
	case 911001400:
		player.changeMap(911000960,0);
		portal.showHint("wow, 我一定會拿到第一的！", 250, 5);
		break;
}
		
portal.abortWarp();

