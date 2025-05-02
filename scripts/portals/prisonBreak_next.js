let mapId = map.getId();
switch (mapId) {
    case 921160100:
        party.changeMap(921160200);
        break;
    case 921160200:
        if (map.getEventMobCount() < 1) {
            party.changeMap(921160300);
        } else {
            portal.showHint("请击败所有怪物", 250, 5);
        }
        break;
    case 921160350:
        party.changeMap(921160400);
        break;

    case 921160400:
        if (map.getEventMobCount() < 1) {
            party.changeMap(921160600);
        } else {
            portal.showHint("请击败所有怪物", 250, 5);
        }
        break;
    case 921160500:
        party.changeMap(921160600);
        break;
    case 921160600:
        if (player.hasItem(4001528, 20)) {
            party.loseItem(4001528);
            party.changeMap(921160700);
            portal.showHint("终于逃出来了", 250, 5);
        } else {
            portal.showHint("请击杀怪物获取20把监狱钥匙", 250, 5);
        }
        break;
    case 921160700:
		if (map.getEventMobCount() < 1) {
            party.changeMap(921160701);
        } else {
            portal.showHint("请击败所有怪物", 250, 5);
        }
        break;
}
portal.abortWarp();
