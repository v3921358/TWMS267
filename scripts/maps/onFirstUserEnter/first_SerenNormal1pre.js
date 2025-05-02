let event = player.getEvent("boss_seren_normal");
if (event != null && event.getVariable("boss1") == null) {
        event.setVariable("boss1", false)
        
        let boss = map.makeMob(8880630);
        boss.changeBaseHp(63_000_000_000_000);
        map.spawnMob(boss, 0, 275);
        map.spawnMob(8880631, 0, 275);
        map.startFieldEvent();
}