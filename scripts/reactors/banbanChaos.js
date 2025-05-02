// Boss: Banban

let event = player.getEvent("boss_banban_chaos");
if (event != null && event.getVariable("boss") == null) {
        event.setVariable("boss", true);
        let boss = map.makeMob(8910000);
        boss.setFlip();
        map.spawnMob(boss, -131, 455);
}