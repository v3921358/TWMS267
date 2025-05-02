let event = player.getEvent("boss_demian_hard")
if (event != null && event.getVariable("reward") == null) {
    event.setVariable("reward", false)
    map.spawnMob(8950112, 895, 15);
}