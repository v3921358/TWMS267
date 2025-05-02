let event = player.getEvent("boss_demian_normal")
if (event != null && event.getVariable("reward") == null) {
    event.setVariable("reward", false)
    map.spawnMob(8950111, 895, 15);
}