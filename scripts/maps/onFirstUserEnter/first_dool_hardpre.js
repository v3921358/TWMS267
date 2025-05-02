let event = player.getEvent("boss_karing_hard");
if (event != null && event.getVariable("boss_dool") == null) {
    event.setVariable("boss_dool", false)

    let boss = map.makeMob(8880931);
    boss.changeBaseHp(1400_0000_0000_0000);
    map.spawnMob(boss, 0, 405);
    

    let dummy = map.makeMob(8880934);
    dummy.changeBaseHp(1400_0000_0000_0000);
    dummy.setReduceDamageR(100)
    map.spawnMob(dummy, 0, 405)

    map.showWeatherEffectNotice("為了追趕咖凌，必須擊退攻擊桃源境各季節的四凶。", 382, 5000);

}