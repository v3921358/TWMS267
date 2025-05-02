let event = player.getEvent("boss_banban_normal");
if (event != null && event.getVariable("boss") == null) {
        event.setVariable("boss", true);
        let boss = map.makeMob(8910100);
        map.spawnMob(boss, -131, 455);
}