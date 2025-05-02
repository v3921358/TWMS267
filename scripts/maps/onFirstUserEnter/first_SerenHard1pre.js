let event = player.getEvent("boss_seren_hard");
if (event != null && event.getVariable("boss1") == null) {
        event.setVariable("boss1", false);

        let seren = map.makeMob(8880600);
        seren.changeBaseHp(126_000_000_000_000);
        map.spawnMob(seren, 0, 275);

        let floor = map.makeMob(8880601);
        floor.changeBaseHp(126_000_000_000_000);
        map.spawnMob(floor, 0, 275);

        map.startSerenField();
}
