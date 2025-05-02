let event = player.getEvent("boss_seren_normal");
if (event != null && event.getVariable("boss2") == null) {
        event.setVariable("boss2", false)

        let boss = map.makeMob(8880632);
        boss.changeBaseHp(180_000_000_000_000);
        map.spawnMob(boss, 0, 305);

        // let mob = map.makeMob(8880637);
        // mob.setSpiritEntityId(boss.getEntityId());
        // map.spawnMob(mob, 0, 305);

        map.spawnMob(8880638, 0, 305);
        map.startFieldEvent();
}