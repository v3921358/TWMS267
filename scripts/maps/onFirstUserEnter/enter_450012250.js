let event = player.getEvent("boss_dunkel_normal")
if (event != null && event.getVariable("reward") == null) {
    event.setVariable("reward", false)
    map.spawnMob(8950118, 0, 29);
}