let event = player.getEvent("boss_karing_hard");
if (event != null && event.getVariable("phase1")) {
        let boss = map.makeMob(8880937);
        boss.changeBaseHp(2500_0000_0000_0000);
        map.spawnMob(boss, 398, 106);
        
        let dummy = map.makeMob(8880938);
        dummy.changeBaseHp(2500_0000_0000_0000);
        dummy.setReduceDamageR(100)
        map.spawnMob(dummy, 398, 106)
        
        map.showWeatherEffectNotice("吸收四凶的咖凌好像馬上就會暴走。", 384, 5000);

        map.startFieldEvent()
}
