let event = player.getEvent("boss_seren_extreme");
if (event != null && event.getVariable("boss2") == null) {
        event.setVariable("boss2", false)

        let boss = map.makeMob(8880652);
        boss.changeBaseHp(5_800_000_000_000_000);
        map.spawnMob(boss, 0, 305);

        // let mob = map.makeMob(8880657);
        // mob.setSpiritEntityId(boss.getEntityId());
        // map.spawnMob(mob, 0, 305);


        map.spawnMob(8880658, 0, 305);
        map.startFieldEvent();
}