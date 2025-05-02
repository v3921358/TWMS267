let event = player.getEvent("boss_dusk_normal")
if (event != null && event.getVariable("reward") == null) {
    event.setVariable("reward", false)
    map.spawnMob(8950116, -2673, -488);
}