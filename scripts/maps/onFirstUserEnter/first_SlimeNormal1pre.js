let event = player.getEvent("boss_slime_normal");
if (event != null && event.getVariable("boss1") == null) {
        event.setVariable("boss1", false)
        
        let boss = map.makeMob(8880711);
        boss.changeBaseHp(5_000_000_000_000);
        map.spawnMob(boss, 531, -1089);
        map.spawnMob(8880713, 531, -1089);
        map.startFieldEvent();
}