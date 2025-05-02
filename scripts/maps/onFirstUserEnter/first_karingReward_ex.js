let event = player.getEvent("boss_karing_extreme");
if (event != null && event.getVariable("reward") == null) {
        map.spawnMob(8880975, 486, 105);
}
