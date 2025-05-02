let event = player.getEvent("boss_slime_chaos");
if (event != null && event.getVariable("boss1") == null) {
        event.setVariable("boss1", false)
        
        let boss = map.makeMob(8880700);
        boss.changeBaseHp(115_000_000_000_000);
        map.spawnMob(boss, 531, -1089);
        map.spawnMob(8880702, 531, -1089);
        map.startFieldEvent();
}