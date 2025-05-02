let event = player.getEvent("boss_kalos_normal");
if (event != null && event.getVariable("boss1") == null) {
        event.setVariable("boss1", false)

        let boss = map.makeMob(8880800);
        boss.changeBaseHp(336_0000_0000_0000);
        map.spawnMob(boss, 0, 398);

        map.spawnMob(8880801, 0, 398);

        map.loadKalosUI();
}
