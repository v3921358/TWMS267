
let event = player.getEvent("boss_pierre_chaos");
if (event != null && event.getVariable("boss") == null) {
        event.setVariable("boss", false);
        let mob = map.makeMob(8900000);
        map.spawnMob(mob, 1000, 551);
        map.startFieldEvent();
}