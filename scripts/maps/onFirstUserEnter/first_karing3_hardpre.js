let event = player.getEvent("boss_karing_hard");
if (event != null && event.getVariable("phase1")  && event.getVariable("phase2")) {
        
        let goongi = map.makeMob(8880939);
        goongi.changeBaseHp(2500_0000_0000_0000);
        map.spawnMob(goongi, -1598, 362);

        let dool = map.makeMob(8880940);
        dool.changeBaseHp(2500_0000_0000_0000);
        map.spawnMob(dool, 402, 362)

        let hondon = map.makeMob(8880941);
        hondon.changeBaseHp(2500_0000_0000_0000);
        map.spawnMob(hondon, -594, 362)


        let boss = map.makeMob(8880942);
        boss.changeBaseHp(5000_0000_0000_0000);
        boss.setHpLimitPercent(0.75)
        map.spawnMob(boss, -243, 398)

        let ref = map.makeMob(8880943);
        ref.changeBaseHp(5000_0000_0000_0000);
        ref.setReduceDamageR(100)
        map.spawnMob(ref, 0, 398)

        map.showWeatherEffectNotice("怪物充溢了凶惡直起，仿佛進入暴走狀態，必須要阻止桃源境被破壞。", 385, 5000);

        map.startFieldEvent()
}
