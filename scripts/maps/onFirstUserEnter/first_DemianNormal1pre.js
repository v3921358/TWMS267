let event = player.getEvent("boss_demian_normal");
if (event != null && event.getVariable("boss1") == null) {
        event.setVariable("boss1", false)
        let boss = map.makeMob(8880110);
        boss.changeBaseHp(840_000_000_000);
        boss.setHpLimitPercent(0.3)
        map.spawnMob(boss, 828, 15);//main dummy boss
        map.startDemianField();
}