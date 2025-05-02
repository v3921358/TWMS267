let event = player.getEvent("boss_kalos_chaos");
if (event != null && event.getVariable("boss2") == null) {
        event.setVariable("boss2", false)

        let boss = map.makeMob(8881032);
        boss.changeBaseHp(4800_0000_0000_0000);
        map.spawnMob(boss, 0, 398);

        let boss2 = map.makeMob(8881033);
        boss.changeBaseHp(4800_0000_0000_0000);
        map.spawnMob(boss, 0, 398);

        let boss3 = map.makeMob(8881034);
        boss.changeBaseHp(4800_0000_0000_0000);
        map.spawnMob(boss, 0, 398);



        map.loadKalosUI();
}
