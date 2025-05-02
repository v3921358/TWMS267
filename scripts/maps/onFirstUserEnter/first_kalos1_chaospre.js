let event = player.getEvent("boss_kalos_chaos");
if (event != null && event.getVariable("boss1") == null) {
        event.setVariable("boss1", false)

        let kalos = map.makeMob(8881030);
        kalos.changeBaseHp(1200_0000_0000_0000);
        map.spawnMob(kalos, 0, 398);

        let kalos_unk = map.makeMob(8881031);
        map.spawnMob(kalos_unk, 0, 398);
        map.loadKalosUI();
}
