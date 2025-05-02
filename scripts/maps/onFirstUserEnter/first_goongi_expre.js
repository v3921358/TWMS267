let event = player.getEvent("boss_karing_extreme");
if (event != null && event.getVariable("boss_goongi") == null) {
    event.setVariable("boss_goongi", false)

    let boss = map.makeMob(8880960);
    boss.changeBaseHp(9500_0000_0000_0000);
    map.spawnMob(boss, 513, 106);

    let dummy = map.makeMob(8880963);
    dummy.changeBaseHp(9500_0000_0000_0000);
    dummy.setReduceDamageR(100)
    map.spawnMob(dummy, 167, 106)

    map.showWeatherEffectNotice("為了追趕咖凌，必須擊退攻擊桃源境各季節的四凶。", 382, 5000);
}