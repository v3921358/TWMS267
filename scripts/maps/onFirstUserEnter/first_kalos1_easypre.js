let event = player.getEvent("boss_kalos_easy");
if (event != null && event.getVariable("boss1") == null) {
        event.setVariable("boss1", false)
        let boss = map.makeMob(8881010);
        boss.changeBaseHp(94_5000_0000_0000);
        map.spawnMob(boss, 0, 398);

        let monster = map.makeMob(8881011);
        map.spawnMob(monster,0,398);

        map.startKalosField();
}
