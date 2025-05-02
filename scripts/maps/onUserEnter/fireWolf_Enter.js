map.blowWeather(5120159, "要消滅火焰狼的勇士增多了呢，趕緊攻擊那個家伙！能夠停留的時間就只有30秒！", 30);

if (map.getEventMobCount() < 1) {
    let mob_id = 9101227;
    let hp = 1000000000000
    switch (map.getId()) {
        case 993000500:
            mob_id = 9101227
            hp = 1000000000000
            break
        case 993000510:
            mob_id = 9832023
            hp = 1000000000000
            break
        case 993000520:
            mob_id = 9832024
            hp = 1000000000000
            break
    }
    let mob = map.makeMob(mob_id);
    mob.changeBaseHp(hp);
    map.spawnMob(mob, 16, 351);
}