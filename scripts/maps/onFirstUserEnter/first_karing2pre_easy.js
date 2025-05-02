let event = player.getEvent("boss_karing_easy");
if (event != null && event.getVariable("phase1")) {
        let boss = map.makeMob(8880907);
        boss.changeBaseHp(105_0000_0000_0000);
        map.spawnMob(boss, 398, 106);
        
        let dummy = map.makeMob(8880908);
        dummy.changeBaseHp(105_0000_0000_0000);
        dummy.setReduceDamageR(100)
        map.spawnMob(dummy, 398, 106)

        map.showWeatherEffectNotice("吸收四凶的咖凌好像馬上就會暴走。", 384, 5000);
        
        map.startFieldEvent()
}
