if (map.getEventMobCount() <= 0) {
        portal.playPortalSE();
        player.changeMap(272020210, 0);
} else {
        portal.abortWarp();
        player.showSystemMessage("請消滅怪物後才能出去。");
}