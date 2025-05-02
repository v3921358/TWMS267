
    if (map.getEventMobCount() == 0) {
        party.changeMap(925100400, 0); //next
    } else {
		portal.abortWarp();
        player.dropMessage(11, "請消滅所有的怪物");
    }

