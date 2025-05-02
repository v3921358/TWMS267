// normal

let event = player.getEvent("boss_zakum_normal");
if (event != null && event.getVariable("boss") == null) {
        event.setVariable("boss", true);
        
        let boss = map.makeMob(8800002);
        var totalHp = 0;
        for (let i = 8800003; i <= 8800010; i++) {
                let hand = map.makeMob(i);
                totalHp += hand.getHp();
                map.spawnMob(hand, -10, -215);
        }
        boss.changeBaseHp(boss.getHp() + totalHp);
        map.spawnMob(boss, -10, -215);
}

