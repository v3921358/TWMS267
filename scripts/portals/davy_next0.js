
if (player.hasItem(4001260, 7) && map.getEventMobCount() == 0) {
    party.changeMap(925100400, 0);
    player.loseItem(4001260);
} else {
    portal.abortWarp()
    player.dropMessage(11, "請收集老海盜箱子的鑰匙7個並消滅所有怪物，否則無法通過！");
}

