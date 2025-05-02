
let mapId = map.getId();
if(mapId == 350130600){
	player.changeMap(350130700,3);
	portal.showHint("趕快逃離這個鬼地方！", 250, 5);
}else{
	
	player.gainItem(4032472,100);
	player.changeMap(993000800,1);
	portal.showHint("終於逃離了雪歸島！", 250, 5);
	
}
portal.abortWarp();
