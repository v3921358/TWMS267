let event = player.getEvent("boss_demian_hard");
if (event != null && event.getVariable("boss1") == null) {
        event.setVariable("boss1", false)
        
        let boss = map.makeMob(8880100);
        boss.changeBaseHp(25_200_000_000_000);
        boss.setHpLimitPercent(0.3)
        map.spawnMob(boss, 828, 15);//main dummy boss
        map.startFieldEvent();
}