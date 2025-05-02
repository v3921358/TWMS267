let event = player.getEvent("boss_karing_easy");
if (event != null && event.getVariable("boss_hondon") == null) {
    event.setVariable("boss_hondon", false)

    let boss = map.makeMob(8880900);
    boss.changeBaseHp(96_0000_0000_0000);
    map.spawnMob(boss, 535, 106);
    
    let dummy = map.makeMob(8880903);
    dummy.changeBaseHp(96_0000_0000_0000);
    dummy.setReduceDamageR(100)
    map.spawnMob(dummy, 0, 106)
    map.showWeatherEffectNotice("為了追趕咖凌，必須擊退攻擊桃源境各季節的四凶。", 382, 5000);

}