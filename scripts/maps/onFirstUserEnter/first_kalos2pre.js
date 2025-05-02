let event = player.getEvent("boss_kalos_normal");
if (event != null && event.getVariable("boss2") == null) {
        event.setVariable("boss2", false)

        let boss = map.makeMob(8880802);
        boss.changeBaseHp(720_0000_0000_0000);
        boss.setHpLimitPercent(0.75)
        map.spawnMob(boss, 0, 398);

        // let mob = map.makeMob(8880803);
        // mob.setSpiritEntityId(sprintMob.getEntityId());
        // map.spawnMob(mob, 0, 399);

        map.spawnMob(8880807, 0, 398);

        map.loadKalosUI();
}
