let event = player.getEvent("boss_jinhillah_normal")
if (event != null && event.getVariable("reward") == null) {
    event.setVariable("reward", false)
    map.spawnMob(8950120, 823, 206);
}