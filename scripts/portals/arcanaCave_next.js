let mapId = map.getId();
switch (mapId){
	case 940200300:
		if(player.hasItem(4001878,30)){
			player.loseItem(4001878);
			player.changeMap(450001340);
			portal.showHint("请击败第一关的BOSS【阿勒玛】,加油！", 250, 5);
		}else{
			portal.showHint("请击败混沌精灵，并且收集30个<神秘河水滴石>。如果<神秘河水滴石>没有收集齐，请玩家<落入水中>刷新怪物，继续收集通关", 250, 5);
		}
		break;
	case 940200310:
		if(player.hasItem(4001878,40)){
			player.loseItem(4001878);
			player.changeMap(450002250);
			portal.showHint("请击败第二关的BOSS【呲溜树】,加油！", 250, 5);
		}else{
			portal.showHint("请击败绝望精灵，并且收集40个<神秘河水滴石>。如果<神秘河水滴石>没有收集齐，请玩家<落入水中>刷新怪物，继续收集通关", 250, 5);
		}
		break;
	case 940200320:
		if(player.hasItem(4001878,50)){
			player.loseItem(4001878);
			player.changeMap(940200400);
			portal.showHint("请击败第三关的BOSS【调和精灵】,加油！", 250, 5);
		}else{
			portal.showHint("请击败悲叹精灵，并且收集50个<神秘河水滴石>。如果<神秘河水滴石>没有收集齐，请玩家<落入水中>刷新怪物，继续收集通关", 250, 5);
		}
		break;
}
portal.abortWarp();