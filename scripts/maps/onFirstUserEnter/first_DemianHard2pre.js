let event = player.getEvent("boss_demian_hard");
if (event != null && event.getVariable("boss2") == null) {
        event.setVariable("boss2", false)

        let boss = map.makeMob(8880101);
        boss.changeBaseHp(10_800_000_000_000);
        map.spawnMob(boss, 828, 15);

        // let shadowZone = map.makeMob(8880102);
        // map.spawnMob(shadowZone, 828, 15);
        // boss.registerMobZone(5, shadowZone.getEntityId());

        map.startFieldEvent();
}
