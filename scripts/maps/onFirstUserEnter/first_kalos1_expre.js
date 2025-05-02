let event = player.getEvent("boss_kalos_extreme");
if (event != null && event.getVariable("boss1") == null) {
        event.setVariable("boss1", false)

        let boss = map.makeMob(8881050);
        boss.changeBaseHp(6720_0000_0000_0000);
        map.spawnMob(boss, 0, 398);

        map.spawnMob(8881051, 0, 398);

        map.loadKalosUI();
}
