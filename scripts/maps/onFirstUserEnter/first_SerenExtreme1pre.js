let event = player.getEvent("boss_seren_extreme");
if (event != null && event.getVariable("boss1") == null) {
        event.setVariable("boss1", false)
        
        let boss = map.makeMob(8880650);
        boss.changeBaseHp(1_489_000_000_000_000);
        map.spawnMob(boss, 0, 275);
        map.spawnMob(8880651, 0, 275);
        map.startFieldEvent();
}