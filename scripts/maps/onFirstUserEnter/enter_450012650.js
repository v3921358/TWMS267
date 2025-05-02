let event = player.getEvent("boss_dunkel_hard")
if (event != null && event.getVariable("reward") == null) {
    event.setVariable("reward", false)
    map.spawnMob(8950119, 0, 29);
}