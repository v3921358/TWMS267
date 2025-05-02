let event = player.getEvent("boss_jinhillah_hard")
if (event != null && event.getVariable("reward") == null) {
    event.setVariable("reward", false)
    map.spawnMob(8950121, 823, 206);
}