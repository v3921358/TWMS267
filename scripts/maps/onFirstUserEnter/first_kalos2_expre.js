let event = player.getEvent("boss_kalos_extreme");
if (event != null && event.getVariable("boss2") == null) {
        event.setVariable("boss2", false)

        let boss = map.makeMob(8881052);
        boss.changeBaseHp(2_4000_0000_0000_0000);
        boss.setHpLimitPercent(0.75)
        map.spawnMob(boss, 0, 398);

        // let mob = map.makeMob(8881053);
        // mob.setSpiritEntityId(sprintMob.getEntityId());
        // map.spawnMob(mob, 0, 399);

        map.spawnMob(8881057, 0, 398);

        map.loadKalosUI();
}
