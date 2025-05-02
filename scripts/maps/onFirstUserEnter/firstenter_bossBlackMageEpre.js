let event = player.getEvent("boss_blackmage_extreme")
if (event != null) {
    switch (map.getId()) {
        case 450013910: {
            let spiritMob = map.makeMob(8880535);
            spiritMob.changeBaseHp(1_260_000_000_000_000);
            map.spawnMob(spiritMob, -45, -157); 

            let wBoss = map.makeMob(8880530);
            //wBoss.setSpiritEntityId(spiritMob.getEntityId());
            map.spawnMob(wBoss, -350, 85);

            let bBoss = map.makeMob(8880531);
            //bBoss.setSpiritEntityId(spiritMob.getEntityId());
            bBoss.setFlip();
            map.spawnMob(bBoss, 350, 85);

            map.spawnMob(8880542, 5, 85);

            map.startFieldEvent();
            break;
        }
        case 450013930: {
            let boss = map.makeMob(8880532);
            boss.changeBaseHp(1_470_000_000_000_000);
            map.spawnMob(boss,-52, 88);

            map.spawnMob(8880542, 0, 88);
            map.startFieldEvent();
            break;
        }
        case 450013950: {

            let boss = map.makeMob(8880533);
            boss.changeBaseHp(1_575_000_000_000_000);
            map.spawnMob(boss,-52, 88);

            map.spawnMob(8880542, 0, 88);
            map.startFieldEvent();
            break;
        }
        case 450013970: {

            let boss = map.makeMob(8880549);
            boss.changeBaseHp(1_680_000_000_000_000);
            map.spawnMob(boss,0, 218);

            map.spawnMob(8880542, 0, 218);
            map.startFieldEvent();
            break;
        }
        case 450013980: {

            map.spawnMob(8880548, 0, 218);
            map.startFieldEvent();
            break;
        }
    }
}
