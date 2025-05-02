let event = player.getEvent("boss_karing_normal");
if (event != null && event.getVariable("reward") == null) {
        map.spawnMob(8880845, 486, 105);
}
