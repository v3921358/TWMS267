let event = player.getEvent("boss_seren_hard");
if (event != null && event.getVariable("boss2") == null) {
        event.setVariable("boss2", false)

        let seren = map.makeMob(8880607);
        seren.changeBaseHp(360_000_000_000_000);
        map.spawnMob(seren, 0, 305);

        let seren_unk = map.makeMob(8880602);
        map.spawnMob(seren_unk, 0, 305);

        let seren_unk_2 = map.makeMob(8880608);
        map.spawnMob(seren_unk_2, 0, 305);

        map.startSerenFieldEvent_II();
}
