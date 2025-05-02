let event = player.getEvent("boss_will_easy");
if (event != null && event.getVariable("boss1") == null) {
    event.setVariable("boss1", false)
    switch (map.getId()) {
        case 450007850: { //簡單
            let will = map.makeMob(8880363);
            will.changeBaseHp(7_000_000_000_0);
            map.spawnMob(will, 352, 159);

            let will2 = map.makeMob(8880364);
            will2.changeBaseHp(7_000_000_000_0);
            map.spawnMob(will2, 352,-2020);

            let will3 = map.makeMob(8880360);
            will3.changeBaseHp(7_000_000_000_0);
            map.spawnMob(will3, 352,-2020);

            let monster2 = map.makeMob(8880372);
            map.spawnMob(monster2,352,159);

            let monster3 = map.makeMob(8880373);
            map.spawnMob(monster3,352,-2020);

            let monster4 = map.makeMob(8880376);
            map.spawnMob(monster4,252,159);

            let monster5 = map.makeMob(8880377);
            map.spawnMob(monster5,252,-2020);

            let monster6 = map.makeMob(8880363);
            map.spawnMob(monster6,300,159);

            let monster7 = map.makeMob(8880367);
            map.spawnMob(monster7,-580,80);

            let monster8 = map.makeMob(8880367);
            map.spawnMob(monster8,-450,-250);

            let monster9 = map.makeMob(8880367);
            map.spawnMob(monster9,450,-250);

            let monster10 = map.makeMob(8880367);
            map.spawnMob(monster10,580,80);

            map.startWillField();
            map.showWeatherEffectNotice("必須同時攻擊在兩個完全不同空間的威爾。聚集月光後使用的話，就可以移動到別的地方了。",245,3000);
            break;
        }
    }
}
