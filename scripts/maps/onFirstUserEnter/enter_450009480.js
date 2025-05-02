let event = player.getEvent("boss_dusk_chaos")
if (event != null && event.getVariable("reward") == null) {
    event.setVariable("reward", false)
    map.spawnMob(8950117, -2673, -488);
}