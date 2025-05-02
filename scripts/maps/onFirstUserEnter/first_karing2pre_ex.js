let event = player.getEvent("boss_karing_extreme");
if (event != null && event.getVariable("phase1")) {
        let boss = map.makeMob(8880967);
        boss.changeBaseHp(1_3300_0000_0000_0000);
        map.spawnMob(boss, 398, 106);
        
        let dummy = map.makeMob(8880968);
        dummy.changeBaseHp(1_3300_0000_0000_0000);
        dummy.setReduceDamageR(100)
        map.spawnMob(dummy, 398, 106)

        map.showWeatherEffectNotice("吸收四凶的咖凌好像馬上就會暴走。", 384, 5000);
        
        map.startFieldEvent()
}
