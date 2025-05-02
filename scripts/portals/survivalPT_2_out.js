
let mapId = map.getId();
           
if(mapId == 993000705){
	if(player.hasItem(4009340,1)){
		player.changeMap(993000706, 1);
		player.loseItem(4009340,1);
		portal.showHint("因异世界傳送力量太强大，冰雪仗碎片無法承受而毀壞。 冰雪杖剩餘的魔力已經依附在你身上。", 250, 5);
	}else{
		portal.showHint("沒有冰雪杖，無法通過异世界之門。", 250, 5);
	}
}else if(mapId == 993000706){
	player.changeMap(993000708, 1);
	portal.showHint("冰雪杖碎片的魔力剩餘不多，冒險家抓緊時間了！", 250, 5);
}else if(mapId == 993000708){
	player.changeMap(993000709, 1);
	portal.showHint("冰雪杖碎片的魔力剩餘不多，冒險家抓緊時間了！", 250, 5);
}else if(mapId == 993000709){
	player.changeMap(993000710, 1);
	portal.showHint("冰雪杖碎片的魔力剩餘不多，冒險家抓緊時間了！", 250, 5);
}else if(mapId == 993000710){
	player.changeMap(993000711, 1);
	portal.showHint("冰雪杖碎片的魔力剩餘不多，冒險家抓緊時間了！", 250, 5);
}else if(mapId == 993000711){
	player.changeMap(993000712, 1);
	portal.showHint("冰雪杖碎片的魔力剩餘不多，冒險家抓緊時間了！", 250, 5);
}else if(mapId == 993000712){
	player.changeMap(993000714, 2);
	portal.showHint("冰雪杖碎片剩餘的魔力已經消失殆盡了！", 250, 5);
}
portal.abortWarp();